/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.services.relayserver.services;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.kitodo.api.MdSec;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.KitodoConfig;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.config.xml.fileformats.FileFormat;
import org.kitodo.config.xml.fileformats.FileFormatsConfig;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.CommandException;
import org.kitodo.exceptions.PremisException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.process.ProcessGenerator;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.relayserver.RelayServerServiceConfig;
import org.kitodo.production.services.relayserver.helper.JobHelper;
import org.kitodo.production.services.relayserver.helper.PremisHelper;
import org.kitodo.production.services.relayserver.helper.XMLHelper;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@Path("/job")
public class JobService {

    private static final Properties kitodoProperties = new Properties();
    private static final String KITODO_PROPERTIES_FILE = RelayServerServiceConfig.getKitodoPropertiesFile();
    private static final XPath xPath = XPathFactory.newInstance().newXPath();

    private static final Logger logger = LogManager.getLogger(JobService.class);

    private static int kitodo_capacity;
    private static int vecteur_project_id;
    private static String root_kitodo_api;
    private static String script_clean_up;
    private static String script_create_sip;
    private static String script_evaluate_dockets;
    private static String script_integrate_external_digitization_results;
    private static String script_send_check_results;
    private static String script_send_sip;
    private static boolean validate_job_xml;
    private static String barcode_service_host;
    private static String barcode_service_port;
    private static String barcode_service_path_decode;
    private static String barcode_service_user;
    private static String barcode_service_password;
    private static int scanner_task_order;
    private static Integer usedCapacity;
    private static int barcode_service_connection_timeout;
    private static int barcode_service_read_timeout;

    private static final String ORIGINAL_METADATA_DIR = "originalMetadata";
    private static final String ORIGINAL_METADATA_FILE = "meta.xml";
    private static final String IMAGES_DIR = "images";
    private static final String IMAGES_EXPORT_DIR = "images_export";
    private static final String IMAGES_ARCHIVE_DIR = "archive";
    private static final String VIADUC_ID = "AuftragsId";
    private static final String VIADUC_MESSAGE = "viaducMessage";
    private static final String VIADUC_SIZE = "viaducSize";
    private static final String DOSSIER_TITLE = "Dossier-Titel";

    private static final String ACCEPTED_BY_KITODO = "ACCEPTED_BY_KITODO";
    private static final String READY_FOR_DIGITIZATION = "READY_FOR_DIGITIZATION";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String EXTERNAL = "EXTERNAL";
    private static final String SCANNED = "SCANNED";
    private static final String CANCELED = "CANCELED";
    private static final String INGEST_FAILED = "INGEST_FAILED";
    private static final String DONE = "DONE";
    private static final String UNKNOWN_STATE = "UNKNOWN_STATE";

    private static final String RECEIVE_DIGITIZATION_JOB = "ReceiveDigitizationJob";
    private static final String RECEIVE_DIGITIZATION_JOB_DE = "Digitalisierungsauftrag empfangen";
    private static final String REQUEST_DOSSIER_FROM_VIADUC = "RequestDossierFromViaduc";
    private static final String REQUEST_DOSSIER_FROM_VIADUC_DE = "Dossier anfordern";
    private static final String EVALUATE_DOCKETS = "EvaluateDockets";
    private static final String EVALUATE_DOCKETS_DE = "Strukturtrennblatterkennung";
    private static final String IMAGE_PROCESSING = "ImageProcessing";
    private static final String IMAGE_PROCESSING_DE = "Automatische Bildbearbeitung";
    private static final String CHECK_QUALITY = "CheckQuality";
    private static final String CHECK_QUALITY_DE = "Qualitätsprüfung";
    private static final String CHECK_DOSSIER = "CheckDossier";
    private static final String CHECK_DOSSIER_DE = "Dossier prüfen";
    private static final String PREPARE_DOSSIER = "PrepareDossier";
    private static final String PREPARE_DOSSIER_DE = "Dossier vorbereiten";
    private static final String CHECK_SCANS = "CheckScans";
    private static final String CHECK_SCANS_DE = "Manuelle Bildbearbeitung";
    private static final String CREATE_SIP = "CreateSIP";
    private static final String CREATE_SIP_DE = "SIP erstellen";
    private static final String SEND_SIP = "SendSIP";
    private static final String SEND_SIP_DE = "SIP senden";
    private static final String CLEANUP = "Cleanup";
    private static final String CLEANUP_DE = "Auftrag abschliessen";

    private static final String JOB_CREATED = "Job created";
    private static final String JOB_NOT_CREATED = "Job not created";

    private static final String COMMENT_ADDED = "Comment added to job";

    private static final String METADATA_FOLDER = "directory.metadata";

    private static final String DOC_TYPE_STRING = "DocType";
    private static final String DOSSIER_STRING = "dossier";
    private static final String TEMPLATE_ID_STRING = "TemplateID";
    private static final String BEHAELTNISSE_STRING = "Behältnisse";
    // TODO: retrieve name from project settings!
    private static final String SCANS_DIRECTORY = "scans";
    private static final String PARTIAL_JOB = "partialJob";
    private static final String PROFILE = "profile";
    private static final String EXTERN = "extern";
    private static final String NUMBER_DOCKETS = "numberDockets";
    private static final String NUMBER_ACTUAL_IMAGES = "numberActualImages";

    private static final String DOSSIER_TITLE_XPATH = "/DigitalisierungsAuftrag/Dossier/Titel";
    private static final String DOSSIER_CONTAINER_XPATH = "/DigitalisierungsAuftrag/Dossier/Behaeltnisse";
    private static final String CONTAINER_CODE_XPATH = "/Behaeltnis/BehaeltnisCode";

    private static final String ERROR_STATUS = "ERROR";
    private static final String STATUS_PLACEHOLDER = "[STATUS]";
    private static final String DESCRIPTION_PLACEHOLDER = "[DESCRIPTION]";

    private static final String DUE_DATE = "returnDate";
    private static final String ORIGINAL_FORMAT = "originalFormat";

    private static final String CSS_CLASS_EXTERNAL = "external";
    private static final String CSS_CLASS_OVERDUE = "overdue";

    private static final String NA = "N/A";
    private static final String EXTERNAL_DIGITIZATION_STRING = "Externer Digitalisierungsteilauftrag ";
    private static final String ORIGINAL_FORMAT_STRING = "Originalformat: ";
    private static final String EXPECTED_RETURN_DATE_STRING = "Erwarteter Rueckgabetermin: ";

    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private static final String STATE_RESPONSE_BODY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<job>"
            + "<status>" + STATUS_PLACEHOLDER + "</status>"
            + "<description>" + DESCRIPTION_PLACEHOLDER + "</description>"
            + "</job>";
    private static final String DEFAULT_RESPONSE_BODY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<response>"
            + "<success>%s</success>"
            + "<message>%s</message>"
            + "</response>";
    private static final String CLEANABLE_BODY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<response>"
            + "<cleanable>%s</cleanable>"
            + "</response>";
    private static final String CLEANABLE_BODY_ERROR = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<response>"
            + "<cleanable>%s</cleanable>"
            + "<error>%s</error>"
            + "</response>";
    public static final List<String> MEDIA_FILE_TYPES = Arrays.asList(".mkv", ".mp4", ".tif", ".wav");
    private static final String ANALYZABLE_FILE_TYPE = ".tif";
    private static final String UNABLE_TO_FIND_JOB_MESSAGE
            = "Error: unable to retrieve process (Viaduc ID %s): process not found!";

    private static final HashMap<String, String> processPropertyMap;
    private DocketsHandler docketsHandler;
    private final List<String> imagesPaths = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String DATE_TIME_STRING = "%s-%s-%s %s:00:00";

    public static final String WORKING_COPY = "benutzungskopie";

    static {
        processPropertyMap = new HashMap<>();
        processPropertyMap.put("AuftragsId", "/DigitalisierungsAuftrag/Auftragsdaten/" + VIADUC_ID);
        processPropertyMap.put("Signatur", "/DigitalisierungsAuftrag/Dossier/Signatur");
        processPropertyMap.put("AblieferndeStelle", "/DigitalisierungsAuftrag/Ablieferung/AblieferndeStelle");
        processPropertyMap.put("Entstehungszeitraum", "/DigitalisierungsAuftrag/Dossier/Entstehungszeitraum");
        processPropertyMap.put("Aktenzeichen", "/DigitalisierungsAuftrag/Dossier/Aktenzeichen");
        processPropertyMap.put("Behaeltniscode", DOSSIER_CONTAINER_XPATH + CONTAINER_CODE_XPATH);
    }

    // TODO: load digitization profile values from ruleset instead of hard coding them here!
    private static final LinkedList<String> digitizationProfiles;

    static {
        digitizationProfiles = new LinkedList<>();
        digitizationProfiles.add("Kodaki4250A");
        digitizationProfiles.add("Kodaki4250B");
        digitizationProfiles.add("MicroboxUltraIIA");
        digitizationProfiles.add("Einzug-Scanner - Charge 1");
        digitizationProfiles.add("Einzug-Scanner - Charge 2");
        digitizationProfiles.add("Einzug-Scanner - Charge 3");
        digitizationProfiles.add("Buch-Scanner - Charge 1");
        digitizationProfiles.add("Buch-Scanner - Charge 2");
        digitizationProfiles.add("Grossformat-Scanner");
        digitizationProfiles.add("Extern - Charge 1");
        digitizationProfiles.add("Extern - Charge 2");
        digitizationProfiles.add("Extern - Charge 3");
    }

    public JobService() {
        loadProperties();
    }

    /**
     * Load configuration of the Vecteur/Kitodo REST API from properties file.
     */
    private void loadProperties() {
        try (FileInputStream kitodoConfiguration = new FileInputStream(KITODO_PROPERTIES_FILE)) {
            kitodoProperties.load(kitodoConfiguration);
            vecteur_project_id = Integer.parseInt(kitodoProperties.getProperty("vecteur_project_id"));
            kitodo_capacity = Integer.parseInt(kitodoProperties.getProperty("kitodo_capacity"));
            root_kitodo_api = kitodoProperties.getProperty("root_kitodo_api");
            script_clean_up = kitodoProperties.getProperty("script_clean_up");
            script_create_sip = kitodoProperties.getProperty("script_create_sip");
            script_evaluate_dockets = kitodoProperties.getProperty("script_evaluate_dockets");
            script_integrate_external_digitization_results = kitodoProperties.getProperty("script_integrate_external_digitization_results");
            script_send_check_results = kitodoProperties.getProperty("script_send_check_results");
            script_send_sip = kitodoProperties.getProperty("script_send_sip");
            validate_job_xml = Boolean.parseBoolean(kitodoProperties.getProperty("validateJobXML"));
            barcode_service_host = kitodoProperties.getProperty("barcode_service_host");
            barcode_service_port = kitodoProperties.getProperty("barcode_service_port");
            barcode_service_path_decode = kitodoProperties.getProperty("barcode_service_path_decode");
            barcode_service_user = kitodoProperties.getProperty("barcode_service_user");
            barcode_service_password = kitodoProperties.getProperty("barcode_service_password");
            scanner_task_order = Integer.parseInt(kitodoProperties.getProperty("scanner_task_order"));

            String connectionTimeoutValue = kitodoProperties.getProperty("barcode_service_connection_timeout");
            barcode_service_connection_timeout = numericValueDefined(connectionTimeoutValue) ? Integer.parseInt(connectionTimeoutValue) : 10000;

            String readTimeoutValue = kitodoProperties.getProperty("barcode_service_read_timeout");
            barcode_service_read_timeout = numericValueDefined(readTimeoutValue) ? Integer.parseInt(readTimeoutValue) : 10000;

            if (Objects.isNull(usedCapacity)) {
                calculateUsedCapacity();
            }
        } catch (IOException | DAOException e) {
            e.printStackTrace();
        }
    }

    private boolean numericValueDefined(String inputString) {
        return (Objects.nonNull(inputString) && StringUtils.isNumeric(inputString));
    }

    /**
     * Set script paths for all automatic tasks in given Process 'process'.
     *
     * @param process Process for which script paths are set
     * @throws DataException thrown when task cannot be saved
     */
    private static void setScriptPaths(Process process) throws DataException {
        int processId = process.getId();
        for (Task task : process.getTasks()) {
            switch (task.getTitle()) {
                case "SendCheckResults":
                    task.setScriptName("script_sendCheckResults.sh");
                    task.setScriptPath(script_send_check_results);
                    ServiceManager.getTaskService().save(task);
                    break;
                case EVALUATE_DOCKETS:
                case EVALUATE_DOCKETS_DE:
                    task.setScriptName("script_evaluateDockets.sh");
                    task.setScriptPath(script_evaluate_dockets + " "
                            + root_kitodo_api + "/viaduc/job/" + processId + "/evaluatedockets");
                    ServiceManager.getTaskService().save(task);
                    break;
                case "IntegrateExternalDigitizationResults":
                    task.setScriptName("script_integrateExternalDigitizationResults.sh");
                    task.setScriptPath(script_integrate_external_digitization_results);
                    ServiceManager.getTaskService().save(task);
                    break;
                case CREATE_SIP:
                case CREATE_SIP_DE:
                    task.setScriptName("script_createSIP.sh");
                    task.setScriptPath(script_create_sip + " " + root_kitodo_api + "/viaduc/job/" + processId + "/createsip");
                    ServiceManager.getTaskService().save(task);
                    break;
                case CLEANUP:
                case CLEANUP_DE:
                    task.setScriptName("script_cleanUp.sh");
                    task.setScriptPath(script_clean_up + " " + root_kitodo_api + "/viaduc/job/" + processId + "/cleanup");
                    ServiceManager.getTaskService().save(task);
                    break;
                default:
                    break;
            }
        }
    }

    private void createJobProperties(Document job, Process process) {
        // add properties from static 'processPropertyMap' with keys as titles and values as xPaths instances
        // to retrieve corresponding values from given job document
        for (Map.Entry<String, String> processProperty : processPropertyMap.entrySet()) {
            ProcessGenerator.addPropertyForProcess(process, processProperty.getKey(), XMLHelper.getStringValue(job,
                    processProperty.getValue(), false));
        }

        ProcessGenerator.addPropertyForProcess(process, DOSSIER_TITLE, createProcessTitle(job));
        ProcessGenerator.addPropertyForProcess(process, VIADUC_SIZE, Integer.toString(XMLHelper.getJobSize(job)));
        ProcessGenerator.addPropertyForProcess(process, DOC_TYPE_STRING, DOSSIER_STRING);
        ProcessGenerator.addPropertyForProcess(process, TEMPLATE_ID_STRING, String.valueOf(process.getTemplate().getId()));

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            Node containerNode = (Node) xPath.compile(DOSSIER_CONTAINER_XPATH).evaluate(job, XPathConstants.NODE);
            if (Objects.nonNull(containerNode)) {
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document newDoc = docBuilder.newDocument();
                ((Element) containerNode).setAttribute("xmlns:i", "http://www.w3.org/2001/XMLSchema-instance");

                Node newContainerNode = newDoc.importNode(containerNode, true);
                newDoc.appendChild(newContainerNode);
                Document document = XMLHelper.transformXML(XMLHelper.convertDocumentToString(newDoc, false),
                        RelayServerServiceConfig.getContainersXsltFile(), false);
                String containersString = XMLHelper.convertDocumentToString(document, false);
                ProcessGenerator.addPropertyForProcess(process, BEHAELTNISSE_STRING, containersString);
            }
        } catch (XPathExpressionException | ParserConfigurationException e) {
            logger.error("Could not transfer 'Behältnisse' to separate document. (" + e.getLocalizedMessage() + ")");
        }
    }

    private String createProcessTitle(Document job) {
        int titleMaxLength = 185
                - KitodoConfig.getParameter(ParameterCore.DIRECTORY_SUFFIX).length()
                - KitodoConfig.getParameter(ParameterCore.DIRECTORY_PREFIX).length();
        String title = XMLHelper.getStringValue(job, DOSSIER_TITLE_XPATH, false);
        if (title.length() > titleMaxLength) {
            title = title.substring(0, titleMaxLength);
            logger.info("Truncating job's title because it is longer than 255 characters.");
        }
        return title;
    }

    /**
     * Check and return whether Kitodo has free capacity to accept and process additional digitization jobs.
     *
     * @return Response object containing the information if Kitodo has free digitization capacity.
     */
    @GET
    @Path("/freeCapacity")
    @Produces(MediaType.APPLICATION_XML)
    public Response hasFreeCapacity() {
        if (isIndexCorrupted()) {
            logger.error("Unable to handle request. Index is currently corrupted or indexing is in progress.");
            return Response.status(SERVICE_UNAVAILABLE).build();
        }
        boolean freeCapacity = kitodo_capacity > usedCapacity;
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<freeCapacity>" + freeCapacity + "</freeCapacity>";
        return Response.status(OK).entity(response).build();
    }

    /**
     * Handle requests for adding a new job to Kitodo.
     *
     * @param requestBody contains the request body.
     * @return Response object containing either status 200 or 902 and a response string.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response receiveJob(String requestBody) {
        if (isIndexCorrupted()) {
            logger.error("Unable to handle request. Index is currently corrupted or indexing is in progress.");
            return Response.status(SERVICE_UNAVAILABLE).build();
        }

        if (validate_job_xml) {
            try {
                XMLHelper.validateXMLAgainstSchema(requestBody, RelayServerServiceConfig.getDigitizationJobXsdFile());
            } catch (SAXException e) {
                String message = JOB_NOT_CREATED + " (job XML is invalid: " + e.getLocalizedMessage() + ")";
                logger.error(message);
                return Response.status(902).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, message)).build();
            } catch (IOException e) {
                String message = JOB_NOT_CREATED + " (unable to locate schema definition: " + e.getLocalizedMessage() + ")";
                logger.error(message);
                return Response.status(902).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, message)).build();
            }
        }

        Document job = XMLHelper.parseXML(requestBody);
        int jobSize = XMLHelper.getJobSize(job);
        // log if Kitodo capacity is reached / exceeded after accepting this job!
        if (jobSize > kitodo_capacity - usedCapacity) {
            logger.warn("Warning: Kitodo capacity exceeded after accepting digitization job ("
                    + (usedCapacity + jobSize) + "/" + kitodo_capacity + ")!");
        }

        Process process;
        try {
            process = this.prepareProcessCreation();
        } catch (ProcessGenerationException | DAOException e) {
            String message = JOB_NOT_CREATED + " (" + e.getMessage() + ")!";
            logger.error(message);
            return Response.status(902).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, message)).build();
        }

        if (Objects.isNull(process)) {
            String message = JOB_NOT_CREATED + " (unable to load required template)!";
            logger.error(message);
            return Response.status(902).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, message)).build();
        }

        process.setTitle(createProcessTitle(job));
        createJobProperties(job, process);
        String viaducID = getViaducId(process);

        Process existingProcess = getProcessByViaducID(viaducID);
        if (Objects.nonNull(existingProcess)) {
            String message = JOB_NOT_CREATED + " (Process with Viaduc ID " + viaducID
                    + " already exists in Kitodo database with Kitodo ID " + existingProcess.getId()
                    + " => rejecting job!)";
            logger.error(message);
            return Response.status(901).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, message)).build();
        }

        String metaXML = ServiceManager.getDataEditorService().createMetaXml(XMLHelper.transformXML(
                requestBody,
                RelayServerServiceConfig.getDigitizationJobXsltFile(),
                true));

        // save process
        try {
            // save process via service class
            ServiceManager.getProcessService().save(process, true);

            // write images path to meta.xml
            Document metaXml = XMLHelper.parseXML(metaXML);
            String pathToDmdSec =
                    "/mets/dmdSec[@ID='DMDPHYS_0000']/mdWrap/xmlData/kitodo/metadata[@name='pathimagefiles']";
            Node dmdSec = (Node) xPath.compile(pathToDmdSec).evaluate(metaXml, XPathConstants.NODE);
            dmdSec.setTextContent("file://" + process.getId() + "/images/" + SCANS_DIRECTORY);
            // remove anchorId attribute as the fileGrp is not created when it is present
            ((Element) dmdSec).removeAttribute("anchorId");
            // reference dmd sec in physical struct map
            String pathToStructMap = "mets/structMap[@TYPE='PHYSICAL']/div[@DMDID='DMDPHYS_0000']";
            Element structMapEntry = (Element) xPath.compile(pathToStructMap).evaluate(metaXml, XPathConstants.NODE);
            structMapEntry.setAttribute("ID", "PHYS_0000");
            metaXML = XMLHelper.convertDocumentToString(metaXml, false);

            // save meta.xml manually!
            URI metadataDirectory = ServiceManager.getFileService().createProcessLocation(process, true);
            URI fileURI = new URI(metadataDirectory.toString() + "meta.xml");
            XMLHelper.saveXML(fileURI, metaXML);

            // read metadata file to check whether it's valid against the current ruleset!
            ServiceManager.getProcessService().readMetadataFile(process);

        } catch (DataException | IOException | XPathExpressionException | URISyntaxException | CommandException e) {
            deleteProcess(process);
            logger.error("process for digitization job " + viaducID + " NOT created (" + e.getMessage() + ")!");
            return Response.status(902).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, JOB_NOT_CREATED + " (" + e.getMessage() + ")")).build();
        }

        // set scripts to execute for process' tasks
        try {
            setScriptPaths(process);
        } catch (DataException e) {
            logger.error("Error setting script path: " + e.getMessage());
        }

        // save original xml to metadata directory
        URI rootPath = Paths.get(ConfigCore.getParameter(METADATA_FOLDER)).toUri();
        URI uri = ServiceManager.getFileService().getProcessBaseUriForExistingProcess(process);

        try {
            URI originalMetadataDir = ServiceManager.getFileService().createDirectory(rootPath.resolve(uri), ORIGINAL_METADATA_DIR);
            ServiceManager.getFileService().createResource(originalMetadataDir, ORIGINAL_METADATA_FILE);
            URI originalMetadataFile = rootPath.resolve(originalMetadataDir).resolve(ORIGINAL_METADATA_FILE);
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(Paths.get(originalMetadataFile).toString()), StandardCharsets.UTF_8));
            writer.write(requestBody);
            writer.close();
        } catch (IOException | NullPointerException e) {
            deleteProcess(process);
            logger.error("process for digitization job " + viaducID + " NOT created (" + e.getMessage() + ")!");
            return Response.status(902).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, JOB_NOT_CREATED)).build();
        }

        // create images directory
        try {
            URI imagesDir = ServiceManager.getFileService().createDirectory(rootPath.resolve(uri), IMAGES_DIR);
            ServiceManager.getFileService().createDirectory(imagesDir,
                    ConfigCore.getParameterOrDefaultValue(ParameterCore.SCANNED_IMAGES_SUBDIR));
        } catch (IOException e) {
            deleteProcess(process);
            logger.error("process for digitization job " + viaducID + " NOT created(" + e.getMessage() + ")!");
            return Response.status(902).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, JOB_NOT_CREATED)).build();
        }

        // create initial PREMIS file (augmented with 'event' elements when tasks are activated or closed and when adding comments)
        try {
            PremisHelper.createInitialPremisFile(process);
        } catch (ParserConfigurationException | IOException e) {
            deleteProcess(process);
            logger.error("process for digitization job " + viaducID + " NOT created(" + e.getMessage() + ")!");
            return Response.status(902).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, JOB_NOT_CREATED)).build();
        }

        // after successfully receiving the digitization job, close the corresponding task in the process!
        List<Task> currentTasks = ServiceManager.getProcessService().getOpenAndInWorkTasks(process);
        if (currentTasks.size() == 1 && (currentTasks.get(0).getTitle().equals(RECEIVE_DIGITIZATION_JOB) ||
                currentTasks.get(0).getTitle().equals(RECEIVE_DIGITIZATION_JOB_DE))) {
            try {
                Date processingStart = new Date();
                currentTasks.get(0).setProcessingBegin(processingStart);
                currentTasks.get(0).setProcessingTime(processingStart);
                PremisHelper.addTaskEventToPremisFile(currentTasks.get(0), null, null, false);
                new WorkflowControllerService().close(currentTasks.get(0));
            } catch (DataException | IOException | DAOException e) {
                deleteProcess(process);
                logger.error("process for digitization job " + viaducID + " NOT created (unable to close initial process task)!");
                return Response.status(902).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, JOB_NOT_CREATED)).build();
            } catch (PremisException | ParserConfigurationException e) {
                deleteProcess(process);
                logger.error("Error adding event element to PREMIS file for process (Viaduc ID: " + viaducID + ", Kitodo ID: " + process.getId() + "): " + e.getMessage());
                return Response.status(902).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, JOB_NOT_CREATED)).build();
            }
        }
        usedCapacity += jobSize;
        logger.info("process for digitization job " + viaducID + " successfully created!");
        return Response.status(OK).entity(String.format(DEFAULT_RESPONSE_BODY, TRUE, JOB_CREATED)).build();
    }

    @GET
    @Path("/{id}/cleanable")
    @Produces(MediaType.APPLICATION_XML)
    public Response canBeAutomaticallyCleaned(@PathParam("id") String id) {
        if (isIndexCorrupted()) {
            String indexError = "Unable to handle request. Index is currently corrupted or indexing is in progress.";
            logger.error(indexError);
            return Response.status(SERVICE_UNAVAILABLE).entity(String.format(CLEANABLE_BODY_ERROR, false, indexError)).build();
        }

        Process process = getProcessByViaducID(id);
        if (Objects.isNull(process)) {
            String processTitle = "Auftrag " + id;
            logger.info("Unable to retrieve process with property 'Viaduc ID = " + id
                    + "' => try finding process by title '" + processTitle + "' instead!");
            process = getProcessByTitle(processTitle);
        }
        if (Objects.isNull(process)) {
            String couldNotFindJob = String.format(UNABLE_TO_FIND_JOB_MESSAGE, id);
            return Response.status(902).entity(String.format(CLEANABLE_BODY_ERROR, false, couldNotFindJob)).build();
        }
        String viaducState = getViaducState(process);
        Document metadataDocument = XMLHelper.getMetadataDocument(process);
        boolean isWorkingCopy = SIPMetadata.getWorkingCopyType(metadataDocument)
                || Boolean.parseBoolean(getPropertyValue(process, WORKING_COPY));
        boolean cleanable = DONE.equals(viaducState) && !isWorkingCopy;
        return Response.status(OK).entity(String.format(CLEANABLE_BODY, cleanable)).build();
    }

    @PUT
    @Path("/{id}/comment")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response addComment(@PathParam("id") String id, String xmlRequestBody) {
        String response = "Error: unable to retrieve process (Viaduc ID " + id + ")";
        Process process = getProcessByViaducID(id);
        Response.Status responseStatus = NOT_FOUND;
        if (Objects.nonNull(process)) {
            if (xmlRequestBody.isEmpty()) {
                response = "Error: unable to extract comment (request body is empty!)";
                responseStatus = Response.Status.BAD_REQUEST;
            } else {
                Document setStateDocument = XMLHelper.parseXML(xmlRequestBody);
                String message = XMLHelper.getJobMessage(setStateDocument);
                if (message.isEmpty()) {
                    response = "Error: unable to add comment to job (message string is empty!)";
                    responseStatus = Response.Status.BAD_REQUEST;
                } else {
                    Comment comment = new Comment();
                    comment.setAuthor(ServiceManager.getUserService().getCurrentUser());
                    comment.setMessage(message);
                    comment.setProcess(process);
                    comment.setCreationDate(new Date());
                    comment.setType(CommentType.INFO);
                    try {
                        ServiceManager.getCommentService().saveToDatabase(comment);
                        return Response.status(OK).entity(String.format(DEFAULT_RESPONSE_BODY, TRUE, COMMENT_ADDED)).build();
                    } catch (DAOException e) {
                        response = "Error: unable to add comment to job (" + e.getMessage() + "!)";
                        responseStatus = INTERNAL_SERVER_ERROR;
                    }
                }
            }
        }
        return Response.status(responseStatus).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, response)).build();
    }

    /**
     * Handles requests for setting a job's status.
     *
     * @param id        identifies the job
     * @param xmlString XML String containing the new status and optionally a message
     * @return Response object containing status 200 and a response string.
     */
    @PUT
    @Path("/{id}/status/{status}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response setStatus(@PathParam("id") String id, @PathParam("status") String status, String xmlString) {
        if (isIndexCorrupted()) {
            logger.error("Unable to handle request. Index is currently corrupted or indexing is in progress.");
            return Response.status(SERVICE_UNAVAILABLE).build();
        }

        String response = "Error: unable to retrieve process (Viaduc ID " + id + ")";
        Process process = getProcessByViaducID(id);

        if (Objects.nonNull(process)) {
            String message = "";
            // parse non-empty XML string to extract optional message!
            if (!xmlString.isEmpty()) {
                Document setStateDocument = XMLHelper.parseXML(xmlString);
                message = XMLHelper.getJobMessage(setStateDocument);
            }
            try {
                updateTask(process, status, message);
                response = "Successfully set Viaduc status of process (Viaduc ID: " + id + "; Kitodo ID: "
                        + process.getId() + ") to '" + status + "' and status message '" + message + "'";
                logger.info(response);
                return Response.status(OK).entity(String.format(DEFAULT_RESPONSE_BODY, TRUE, response)).build();
            } catch (IOException | DataException | IllegalStateException | DAOException e) {
                response = "Error: Unable to set state of process (Viaduc ID: " + id + "; Kitodo ID: "
                        + process.getId() + ") to '" + status + "' and status message '" + message + "' (" + e.getMessage() + ")";
                logger.error(response);
                return Response.status(FORBIDDEN).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, response)).build();
            }
        }

        return Response.status(NOT_FOUND).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, response)).build();
    }

    /**
     * Handles requests for retrieving a job's status.
     *
     * @param id identifies the job
     * @return Response object containing status 200 and a response string.
     */
    @GET
    @Path("/{id}/status")
    @Produces(MediaType.APPLICATION_XML)
    public Response getStatus(@PathParam("id") String id) {
        if (isIndexCorrupted()) {
            logger.error("Unable to handle request. Index is currently corrupted or indexing is in progress.");
            return Response.status(SERVICE_UNAVAILABLE).build();
        }

        Process process = getProcessByViaducID(id);

        // Fallback solution for 'Closed' and 'Canceled' jobs that do not have any process properties anymore (including
        // VIADUC_ID): check process title instead!
        if (Objects.isNull(process)) {
            String processTitle = "Auftrag " + id;
            logger.info("Unable to retrieve process with property 'Viaduc ID = " + id
                    + "' => try finding process by title '" + processTitle + "' instead!");
            process = getProcessByTitle(processTitle);
        }

        if (Objects.isNull(process)) {
            String responseMsg = String.format(UNABLE_TO_FIND_JOB_MESSAGE, id);
            return Response.status(NOT_FOUND).entity(createErrorResponseBody(responseMsg)).build();
        }
        String status;
        try {
            status = getViaducState(process);
        } catch (IllegalStateException e) {
            String responseMsg = "Error: unable to retrieve status for process (Viaduc ID " + id + "; Kitodo ID: "
                    + process.getId() + "): " + e.getMessage() + "";
            return Response.status(INTERNAL_SERVER_ERROR).entity(createErrorResponseBody(responseMsg)).build();
        }
        // if "state" = "EXTERN", the "message" should contain the "OriginalFormat" and the "ExpectedReturnDate"!
        StringBuilder message = new StringBuilder(getViaducMessage(process));
        if (EXTERNAL.equals(status)) {
            try {
                Document metadataDocument = XMLHelper.getMetadataDocument(process);
                for (String externalProfile : new String[]{"extern_1", "extern_2", "extern_3"}) {
                    String profileXpath = "//*[text()[contains(.,'REPLACE_ME')]]".replace("REPLACE_ME", externalProfile);
                    String originalFormat = XMLHelper.getOriginalFormat(metadataDocument, profileXpath);
                    String returnDate = XMLHelper.getReturnDate(metadataDocument, profileXpath);
                    if (!NA.equals(originalFormat) || !NA.equals(returnDate)) {
                        message.append(EXTERNAL_DIGITIZATION_STRING).append(" (")
                                .append(ORIGINAL_FORMAT_STRING).append(originalFormat).append(", ")
                                .append(EXPECTED_RETURN_DATE_STRING).append(returnDate).append(");");
                    }
                }
            } catch (XPathExpressionException e) {
                logger.error("Unable to determine original format and return date for externally processed partial jobs of digitization job " + process.getId() + "!");
            }
        }
        String response = STATE_RESPONSE_BODY
                .replace(STATUS_PLACEHOLDER, status)
                .replace(DESCRIPTION_PLACEHOLDER, StringEscapeUtils.escapeXml(message.toString()));
        logger.debug("Return Viaduc message '" + message + "' to RelayServer.");
        return Response.status(OK).entity(response).build();
    }

    /**
     * Handles requests for retrieving a job's a SIP file.
     *
     * @param id identifies the job
     * @return Response object containing status 200 and SIP file
     */
    @GET
    @Path("/{id}/sip")
    @Produces("application/zip")
    public Response getSIP(@PathParam("id") String id) {
        if (isIndexCorrupted()) {
            logger.error("Unable to handle request. Index is currently corrupted or indexing is in progress.");
            return Response.status(SERVICE_UNAVAILABLE).build();
        }

        ProcessService processService = ServiceManager.getProcessService();
        Process process;
        try {
            ProcessDTO processDTO = processService.findByProperty(VIADUC_ID, id).get(0);
            process = processService.getById(processDTO.getId());
        } catch (DataException | DAOException | RuntimeException e) {
            String message = "Error retrieving SIP for process (Viaduc ID: " + id + "): unable to load process!";
            logger.error(message);
            return Response.status(NOT_FOUND).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, message)).build();
        }
        SIPHandler sipHandler = new SIPHandler(process);
        File sipFile = sipHandler.getSIP();
        if (Objects.isNull(sipFile)) {
            String message = "Error retrieving SIP for process (Viaduc ID: " + id + "): SIP does not exist!";
            logger.error(message);
            return Response.status(NOT_FOUND).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, message)).build();
        } else {
            Response.ResponseBuilder response = Response.ok(sipFile);
            response.header("Content-Disposition", "attachment; filename=" + sipFile.getName());
            logger.info("Sending SIP file for job '" + id + "' to RelayServer...");
            return response.build();
        }
    }

    @GET
    @Path("/reports/taskevents/{fromDay}/{fromMonth}/{fromYear}/to/{toDay}/{toMonth}/{toYear}")
    @Produces("text/csv")
    public Response getTaskEvents(@PathParam("fromDay") String fromDay, @PathParam("fromMonth") String fromMonth,
                                  @PathParam("fromYear") String fromYear, @PathParam("toDay") String toDay,
                                  @PathParam("toMonth") String toMonth, @PathParam("toYear") String toYear) {
        return getTaskEvents(fromDay, fromMonth, fromYear, "00", toDay, toMonth, toYear, "00");
    }

    @GET
    @Path("/reports/taskevents/{fromDay}/{fromMonth}/{fromYear}/{fromHour}/to/{toDay}/{toMonth}/{toYear}/{toHour}")
    @Produces("text/csv")
    public Response getTaskEvents(@PathParam("fromDay") String fromDay, @PathParam("fromMonth") String fromMonth,
                                  @PathParam("fromYear") String fromYear, @PathParam("fromHour") String fromHour,
                                  @PathParam("toDay") String toDay, @PathParam("toMonth") String toMonth,
                                  @PathParam("toYear") String toYear, @PathParam("toHour") String toHour) {
        String fromString = String.format(DATE_TIME_STRING, fromYear, fromMonth, fromDay, fromHour);
        String toString = String.format(DATE_TIME_STRING, toYear, toMonth, toDay, toHour);
        String taskEventReports;
        try {
            taskEventReports = ServiceManager.getReportService().getTaskEventReportsInDateRange(
                    DEFAULT_DATE_FORMAT.parse(fromString), DEFAULT_DATE_FORMAT.parse(toString));
        } catch (DAOException | ParseException | IOException e) {
            logger.error(e.getMessage());
            return Response.serverError().build();
        }
        Response.ResponseBuilder responseBuilder = Response.ok(taskEventReports);
        return responseBuilder.build();
    }

    @GET
    @Path("/reports/base/{fromDay}/{fromMonth}/{fromYear}/to/{toDay}/{toMonth}/{toYear}")
    @Produces("text/csv")
    public Response getReports(@PathParam("fromDay") String fromDay, @PathParam("fromMonth") String fromMonth,
                               @PathParam("fromYear") String fromYear, @PathParam("toDay") String toDay,
                               @PathParam("toMonth") String toMonth, @PathParam("toYear") String toYear) {
        return getReports(fromDay, fromMonth, fromYear, "00", toDay, toMonth, toYear, "00");
    }

    @GET
    @Path("/reports/base/{fromDay}/{fromMonth}/{fromYear}/{fromHour}/to/{toDay}/{toMonth}/{toYear}/{toHour}")
    @Produces("text/csv")
    public Response getReports(@PathParam("fromDay") String fromDay, @PathParam("fromMonth") String fromMonth,
                               @PathParam("fromYear") String fromYear, @PathParam("fromHour") String fromHour,
                               @PathParam("toDay") String toDay, @PathParam("toMonth") String toMonth,
                               @PathParam("toYear") String toYear, @PathParam("toHour") String toHour) {
        byte[] csvData;
        try {
            String fromString = String.format(DATE_TIME_STRING, fromYear, fromMonth, fromDay, fromHour);
            String toString = String.format(DATE_TIME_STRING, toYear, toMonth, toDay, toHour);
            csvData = ServiceManager.getReportService().getReportsInDateRange(DEFAULT_DATE_FORMAT.parse(fromString),
                    DEFAULT_DATE_FORMAT.parse(toString));
        } catch (CsvRequiredFieldEmptyException | IOException | CsvDataTypeMismatchException | DAOException
                | ParseException e) {
            logger.error(e.getMessage());
            return Response.serverError().build();
        }
        Response.ResponseBuilder responseBuilder = Response.ok(csvData);
        return responseBuilder.build();
    }

    @GET
    @Path("/reports/comments/{fromDay}/{fromMonth}/{fromYear}/to/{toDay}/{toMonth}/{toYear}")
    @Produces("text/csv")
    public Response getCommentReports(@PathParam("fromDay") String fromDay, @PathParam("fromMonth") String fromMonth,
                               @PathParam("fromYear") String fromYear, @PathParam("toDay") String toDay,
                               @PathParam("toMonth") String toMonth, @PathParam("toYear") String toYear) {
        return getCommentReports(fromDay, fromMonth, fromYear, "00", toDay, toMonth, toYear, "00");
    }

    @GET
    @Path("/reports/comments/{fromDay}/{fromMonth}/{fromYear}/{fromHour}/to/{toDay}/{toMonth}/{toYear}/{toHour}")
    @Produces("text/csv")
    public Response getCommentReports(@PathParam("fromDay") String fromDay, @PathParam("fromMonth") String fromMonth,
                               @PathParam("fromYear") String fromYear, @PathParam("fromHour") String fromHour,
                               @PathParam("toDay") String toDay, @PathParam("toMonth") String toMonth,
                               @PathParam("toYear") String toYear, @PathParam("toHour") String toHour) {
        String fromString = String.format(DATE_TIME_STRING, fromYear, fromMonth, fromDay, fromHour);
        String toString = String.format(DATE_TIME_STRING, toYear, toMonth, toDay, toHour);
        String commentsReport;
        try {
            commentsReport = ServiceManager.getReportService().getCommentsReport(DEFAULT_DATE_FORMAT.parse(fromString),
                    DEFAULT_DATE_FORMAT.parse(toString));
        } catch (DAOException | ParseException e) {
            logger.error(e.getMessage());
            return Response.serverError().build();
        }
        Response.ResponseBuilder responseBuilder = Response.ok(commentsReport);
        return responseBuilder.build();
    }

    /**
     * Handles requests to start the creation of SIPs for the given process id.
     *
     * @param id int containing the process id
     * @return Response object containing status 200 or 500
     */
    @GET
    @Path("/{processId}/createsip")
    @Produces("plain/text")
    public Response createSIP(@PathParam("processId") int id) {
        if (isIndexCorrupted()) {
            logger.error("Unable to handle request. Index is currently corrupted or indexing is in progress.");
            return Response.status(SERVICE_UNAVAILABLE).build();
        }

        boolean successful;
        String viaducID;
        try {
            Process process = ServiceManager.getProcessService().getById(id);
            viaducID = getViaducId(process);
            SIPHandler sipHandler = new SIPHandler(process);
            successful = sipHandler.generateSIP();
        } catch (DAOException e) {
            logger.error("Error while creating SIP file for process (Kitodo ID: " + id + "): " + e.getLocalizedMessage() + "");
            return Response.serverError().build();
        }
        if (successful) {
            logger.info("Successfully created SIP file for process (Viaduc ID: " + viaducID + ", Kitodo ID: " + id + ")!");
            return Response.status(OK).build();
        } else {
            logger.error("Error while creating SIP file for process (Kitodo ID: " + id + "). No further information available.");
            return Response.status(INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handles requests to start the evaluation of scanned dockets for the given process.
     *
     * @param id int containing the process id
     * @return Response object containing status 200 or 500
     */
    @GET
    @Path("/{processId}/evaluatedockets")
    @Produces("plain/text")
    public Response evaluateDockets(@PathParam("processId") int id) {
        if (isIndexCorrupted()) {
            logger.error("Unable to handle request. Index is currently corrupted or indexing is in progress.");
            return Response.status(SERVICE_UNAVAILABLE).build();
        }

        Process process;
        try {
            process = ServiceManager.getProcessService().getById(id);
            logger.debug("Loaded process with id " + id);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return Response.status(INTERNAL_SERVER_ERROR).entity("Error in evaluate dockets.").build();
        }
        try {
            String processIDs = "(Viaduc ID: " + getViaducId(process) + ", Kitodo ID: " + id + ")";
            logger.debug("Starting EvaluateDockets for process " + processIDs);
            this.docketsHandler = new DocketsHandler(process);

            try {
                this.docketsHandler.removePreviewImages();
                removeOldFiles(process);
            } catch (IOException e) {
                String errorMessage = "Error removing old image and PREMIS files for process " + processIDs + ": "
                        + e.getMessage();
                JobHelper.jumpToFallbackTask(process, errorMessage, EVALUATE_DOCKETS_DE, PREPARE_DOSSIER_DE);
                return Response.status(INTERNAL_SERVER_ERROR).entity(errorMessage).build();
            }

            logger.debug("Start retrieving images for process " + processIDs);
            List<String> allScannedTiffs;
            try {
                allScannedTiffs = this.docketsHandler.getInputMedia(MEDIA_FILE_TYPES);
            } catch (NoSuchElementException e) {
                String errorMessage = "Error retrieving images: " + e.getMessage() + "for process " + processIDs;
                Helper.setErrorMessage(errorMessage);
                return Response.status(INTERNAL_SERVER_ERROR).entity(errorMessage).build();
            }
            logger.debug("Finished retrieving images for for process " + processIDs);

            if (allScannedTiffs.isEmpty()) {
                String errorMessage = "No TIFF images found in image directory for process " + processIDs;
                JobHelper.jumpToFallbackTask(process, errorMessage, EVALUATE_DOCKETS_DE, PREPARE_DOSSIER_DE);
                return Response.status(NOT_FOUND).entity(errorMessage).build();
            }

            boolean metaXmlIsUpdated;
            logger.debug("Start handling images for process " + processIDs);
            try {
                metaXmlIsUpdated = handleScannedMedia(process, allScannedTiffs, processIDs);
            } catch (NullPointerException e) {
                String message = "Could not handle scanned media: ";
                JobHelper.jumpToFallbackTask(process, e.getMessage(), EVALUATE_DOCKETS_DE, PREPARE_DOSSIER_DE);
                return Response.status(INTERNAL_SERVER_ERROR).entity(message + e.getMessage()).build();
            }
            logger.debug("Finished handling images for process " + processIDs);

            if (metaXmlIsUpdated) {
                try {
                    writeMetaXmlFile(process, processIDs, false);
                } catch (IOException e) {
                    handleError("Error saving meta.xml changes for process with id " + id + ": " + e.getMessage());
                    JobHelper.jumpToFallbackTask(process, prepareErrorMessage(), EVALUATE_DOCKETS_DE, PREPARE_DOSSIER_DE);
                    return Response.status(INTERNAL_SERVER_ERROR).entity(prepareErrorMessage()).build();
                }
                saveNumberOfPartialJobDocketsToMetadataFile(process, this.docketsHandler.partialJobNumberOfDocketsMapping);
            }

            try {
                ServiceManager.getDataEditorService().createImageProcessingParameterFile(process);
                logger.info("Image processing parameter file for process " + processIDs + " created.");
            } catch (IOException e) {
                handleError("Error creating image processing parameter file for process " + id + ": " + e.getMessage());
                JobHelper.jumpToFallbackTask(process, prepareErrorMessage(), EVALUATE_DOCKETS_DE, PREPARE_DOSSIER_DE);
                return Response.status(INTERNAL_SERVER_ERROR).entity(prepareErrorMessage()).build();
            }

            if (!errors.isEmpty()) {
                JobHelper.jumpToFallbackTask(process, prepareErrorMessage(), EVALUATE_DOCKETS_DE, PREPARE_DOSSIER_DE);
                return Response.status(INTERNAL_SERVER_ERROR).entity(prepareErrorMessage()).build();
            }

            logger.debug("Finished EvaluateDockets for process " + processIDs);
            return Response.status(OK).build();
        } catch (Exception e) {
            String message = "Evaluate dockets failed. Error: " + e.getMessage();
            handleError(message);
            JobHelper.jumpToFallbackTask(process, prepareErrorMessage(), EVALUATE_DOCKETS_DE, PREPARE_DOSSIER_DE);
            return Response.status(INTERNAL_SERVER_ERROR).entity(message).build();
        }
    }

    /**
     * Get the content of the mapping.xml containing the mapping between digitization job and SIP.
     *
     * @param id String containing the viaducId
     * @return Response object containing the mapping
     */
    @GET
    @Path("{id}/mapping")
    @Produces(MediaType.APPLICATION_XML)
    public Response getMapping(@PathParam("id") String id) {
        Process process = getProcessByViaducID(id);
        if (Objects.isNull(process)) {
            if (isIndexCorrupted()) {
                logger.error("Unable to handle request. Index is currently corrupted or indexing is in progress.");
                return Response.status(SERVICE_UNAVAILABLE).build();
            }
            String message = "Unable to retrieve process (Viaduc ID: " + id + ")";
            logger.error(message);
            return Response.status(NOT_FOUND).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, message)).build();
        }

        URI processBaseDir = ServiceManager.getProcessService().getProcessDataDirectory(process);
        URI rootPath = Paths.get(ConfigCore.getParameter(JobService.getMetadataFolder())).toUri();
        URI path = rootPath.resolve(processBaseDir + File.separator + "mapping.xml");

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            String mapping = new String(encoded, StandardCharsets.UTF_8);
            logger.info("Successfully retrieved mapping for process (Viaduc ID: " + id + "; Kitodo ID: " + process.getId() + "); returning mapping to RelayServer...");
            return Response.status(OK).entity(mapping).build();
        } catch (IOException e) {
            String message = "Unable to retrieve mapping file for process (Viaduc ID: " + id + "; Kitodo ID: " + process.getId() + ") with URI: " + path;
            logger.error(message);
            return Response.status(NOT_FOUND).entity(String.format(DEFAULT_RESPONSE_BODY, FALSE, message)).build();
        }
    }

    private boolean isIndexCorrupted() {
        try {
            return ServiceManager.getIndexingService().isIndexCorrupted() || ServiceManager.getIndexingService().indexingInProgress();
        } catch (DAOException | DataException e) {
            return true;
        }
    }

    /**
     * Archive the file with the given file path.
     *
     * @param tiffPath Path to file
     */
    private void archiveFile(String tiffPath, Process process, String partialJobId) {
        logger.debug("Start archiving file " + tiffPath);
        String processDir = "file://" + KitodoConfig.getKitodoDataDirectory() + process.getId() + File.separator;
        String imagesArchive = processDir + IMAGES_DIR + File.separator + IMAGES_ARCHIVE_DIR;
        // remove optional partial job prefix and add partial job id
        String newFileName = tiffPath.replaceFirst("^.*/", "")
                .replaceFirst("^.*_", "")
                .replaceFirst("\\" + ANALYZABLE_FILE_TYPE + "$", "_" + partialJobId + ANALYZABLE_FILE_TYPE);

        try {
            if (Files.notExists(Paths.get(KitodoConfig.getKitodoDataDirectory() + process.getId() + File.separator + IMAGES_DIR + File.separator + IMAGES_ARCHIVE_DIR))) {
                ServiceManager.getFileService().createDirectory(URI.create(processDir + IMAGES_DIR), IMAGES_ARCHIVE_DIR);
            }
            docketsHandler.moveAndMapFile(Paths.get(URI.create("file://" + tiffPath)), Paths.get(URI.create(imagesArchive + File.separator + newFileName)), false);
            logger.info("File " + tiffPath + " successfully archived.");
        } catch (IOException e) {
            logger.error("Could not archive file \"" + tiffPath + "\" -> \"" + imagesArchive + File.separator + newFileName + "\": " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            logger.error("DocketsHandler is not instantiated: " + e.getMessage());
        }
        logger.debug("Finished archiving file " + tiffPath);
    }

    /**
     * Restore partial job's media files. Move files back to "scanned_images" directory and restore archived dockets.
     *
     * @param process    process which is currently being edited
     * @param partialJob The partial job is used to correctly rename the images in the target directory
     * @param imagePaths paths of the images allocated to the partial job. This does not contains the archived dockets removed during EvaluateDockets.
     * @throws IOException when files could not be moved
     */
    public static void restorePartialJob(Process process, PhysicalDivision partialJob, List<URI> imagePaths) throws IOException {
        DocketsHandler docketsHandler = new DocketsHandler(process);
        docketsHandler.getImagePathMapping().putAll(docketsHandler.loadImagePathMapping());
        String processDir = "file://" + KitodoConfig.getKitodoDataDirectory() + process.getId() + File.separator;
        String archiveDir = processDir + IMAGES_DIR + File.separator + IMAGES_ARCHIVE_DIR;

        imagePaths = imagePaths.stream().map(a -> URI.create(processDir + a)).collect(Collectors.toList());

        for (String docket : JobHelper.getMediaPaths(Paths.get(URI.create(archiveDir)), Collections.singletonList(partialJob.getDivId() + ANALYZABLE_FILE_TYPE))) {
            java.nio.file.Path currentPath = Paths.get(docket); // Make sure the path is absolute and contains a file scheme
            java.nio.file.Path targetPath = Paths.get(docketsHandler.getImagePathMapping().get(docket));
            docketsHandler.moveAndRestoreFile(currentPath, targetPath);
        }

        for (URI imagePath : imagePaths) {
            java.nio.file.Path currentPath = Paths.get(imagePath);
            java.nio.file.Path targetPath = Paths.get(docketsHandler.getImagePathMapping().get(currentPath.toString()));
            docketsHandler.moveAndRestoreFile(currentPath, targetPath);
        }

        docketsHandler.writeImagePathMapping(docketsHandler.getImagePathMapping());
    }

    /**
     * Reorder the content of the meta.xml. The logical structMap must be before the physical structMap.
     */
    private String reorderMetaXML(String kitodoMeta) {
        String physicalStructMap = kitodoMeta.substring(kitodoMeta.indexOf("<mets:structMap TYPE=\"PHYSICAL\">"),
                kitodoMeta.indexOf("</mets:structMap>") + 17);
        kitodoMeta = kitodoMeta.replace(physicalStructMap, "");
        String firstXmlPart = kitodoMeta.substring(0, kitodoMeta.indexOf("</mets:structMap>") + 17);
        String thirdXmlPart = kitodoMeta.substring(kitodoMeta.indexOf("</mets:structMap>") + 17);
        return firstXmlPart + physicalStructMap + thirdXmlPart;
    }

    /**
     * Assign the previously recognized images to the logical structure element.
     *
     * @param physicalDivId ID of the physical structure element
     * @param logicalDivId  ID of the logical structure element
     */
    private boolean assignPreviousImages(String physicalDivId, String logicalDivId, Map<String, String> mimeTypeMapping,
                                         Map<String, String> suffixMapping) {
        logger.debug("Start assigning images to logical element");
        // Assign previous images before updating logicalDivId to next docket
        if (!this.imagesPaths.isEmpty()) {
            if (physicalDivId.isEmpty()) {
                handleError("Cannot assign " + imagesPaths.size() + " images: No partial job docket found!");
                this.imagesPaths.clear();
                return false;
            }

            try {
                this.docketsHandler.addMedia(this.imagesPaths, physicalDivId, logicalDivId, mimeTypeMapping, suffixMapping);
            } catch (Exception e) {
                handleError("Error adding media to meta.xml: " + e.getMessage());
                this.imagesPaths.clear();
                return false;
            }
            this.imagesPaths.clear();
            logger.debug("Finished assigning images to logical element");
            return true;
        } else if (this.docketsHandler.getActualNumberOfImagesLogical(logicalDivId) > 0) {
            logger.debug("No images to assign, but logical element (" + logicalDivId + ") already has images assigned.");
            return true;
        } else if (!physicalDivId.isEmpty()) {
            handleError("Docket (ID: " + logicalDivId + ") without images detected.");
        }
        logger.debug("Finished assigning images to logical element");
        return false;
    }

    /**
     * Analyse all scanned media files and handle every file according to the recognized type.
     * All files types other than TIFF are not evaluated for barcodes but directly handled as media.
     *
     * @param process         digitization job process
     * @param allScannedMedia List of file paths
     * @param processIDs      String containing Viaduc and Kitodo IDs of current process for logging
     * @return boolean representing whether the meta.xml was changed
     */
    private boolean handleScannedMedia(Process process, List<String> allScannedMedia, String processIDs) throws JAXBException {
        Map<String, String> mimeTypeMapping = process.getProject().getFolders().stream()
                .collect(Collectors.toMap(Folder::getMimeType, Folder::getFileGroup, (a, b) -> b));
        Map<String, String> suffixMapping = FileFormatsConfig.getFileFormats().stream()
                .collect(Collectors.toMap(fileFormat -> fileFormat.getExtension(false), FileFormat::getMimeType, (a, b) -> b));
        boolean metaXmlIsUpdated = false;
        String dossierId = this.docketsHandler.getDossierElementLogicalID();
        String originalDossierId = dossierId;
        String logicalDivId = "";
        String physicalDivId = "";
        List<String> partialJobImages = new ArrayList<>();
        int partialJobCounter = 0;
        int structureElementCounter = 0;
        int partialJobDockets = 0;
        docketsHandler.getImagePathMapping().clear();
        try {
            docketsHandler.getImagePathMapping().putAll(docketsHandler.loadImagePathMapping());
        } catch (FileNotFoundException e) {
            logger.debug("No imagePathMapping file found for process " + process.getId());
        } catch (IOException e) {
            logger.error("Could not load mapping imagePathMapping file for process " + process.getId() + ": " + e.getMessage());
        }

        for (String mediaPath : allScannedMedia) {
            logger.info("Evaluating " + mediaPath + " for process " + processIDs);
            String fileType;
            try {
                fileType = getMediaFileType(process, mediaPath);
            } catch (ProcessingException e) {
                handleError("Could not connect to barcode service: " + e.getMessage());
                break;
            } catch (Exception e) {
                handleError("Could not determine media file type for " + mediaPath + ": " + e.getMessage());
                break;
            }

            if (physicalDivId.equals("") && (!fileType.contains("Teilauftrag_") || !docketsHandler.isPhysElementPresent(fileType.substring(fileType.indexOf("_") + 1)))) {
                /*
                    A media file was recognized while no (matching) partial job docket has been scanned beforehand. This is a fatal error because the
                    current media file cannot be allocated to a partial job. EvaluateDockets will now be aborted.
                    This can happen if the first partial job docket could not be recognized correctly or if none was scanned at all.
                 */
                handleError("The media file [" + mediaPath + "] (type: " + fileType + ") could not be processed because no (matching) partial job docket was detected beforehand. Make sure that a valid partial job docket is scanned before any type of media is scanned.");
                return false;
            }

            if (fileType.equals("IMAGE")) {
                if (logicalDivId.isEmpty()) {
                    handleError("No structural docket found for this image '" + mediaPath + "' for process " + processIDs + "!");
                    continue;
                }
                this.imagesPaths.add(mediaPath);
                partialJobImages.add(mediaPath);
                continue;
            }

            if (fileType.equals("RUECKSEITE")) {
                logger.info(mediaPath + " identified as docket rear for process " + processIDs + ".");
                partialJobDockets++;
                archiveFile(mediaPath, process, physicalDivId);
                continue;
            }

            // TIFF file is a docket (Neither an image nor a docket rear).

            if (!logicalDivId.isEmpty() && assignPreviousImages(physicalDivId, logicalDivId, mimeTypeMapping, suffixMapping)) {
                logicalDivId = "";
                metaXmlIsUpdated = true;
            }

            if (fileType.equals("Umschlag")
                    || fileType.equals("Dokument")
                    || fileType.equals("Unterlagen")
                    || fileType.equals("Dokument-Fortsetzung")) {
                logger.info(mediaPath + " identified as docket for process " + processIDs + ".");
                logicalDivId = this.docketsHandler.addnewElement(fileType.toLowerCase(), dossierId, physicalDivId);
                logger.debug("Added new element " + logicalDivId);
                metaXmlIsUpdated = true;
                partialJobDockets++;
                archiveFile(mediaPath, process, physicalDivId);
                if (fileType.equals("Umschlag")) {
                    String checkResult = docketsHandler.checkCoverOrder(logicalDivId);
                    if (!checkResult.isEmpty()) {
                        handleError(checkResult);
                    }
                } else if (fileType.equals("Dokument-Fortsetzung")) {
                    String checkResult = docketsHandler.checkDocumentInstallmentOrder(logicalDivId);
                    if (!checkResult.isEmpty()) {
                        handleError(checkResult);
                    }
                }
            } else if (fileType.contains("Dokument_") && fileType.endsWith("_LINK")) {
                // Element should link to the media from the previous element.
                logicalDivId = fileType.replace("Dokument_", "").replace("_LINK", "");
                logger.debug("Linking media of preceding element to " + logicalDivId);
                docketsHandler.linkMediaFromPrecedingElement(logicalDivId);
                partialJobDockets++;
                archiveFile(mediaPath, process, physicalDivId);
                structureElementCounter++;
                if (!docketsHandler.isLogElementPresent(logicalDivId)) {
                    handleError("Unable to find logical element with id \"" + logicalDivId + "\" while evaluating docket [" + mediaPath + "].");
                }
            } else if (fileType.contains("Dokument_") || fileType.contains("Umschlag_")) {
                logger.info(mediaPath + " identified as docket for process " + processIDs + ".");
                logicalDivId = fileType.substring(fileType.indexOf("_") + 1);
                partialJobDockets++;
                archiveFile(mediaPath, process, physicalDivId);
                //verzeichnete Dokumente oder Umschläge
                structureElementCounter++;
                if (!docketsHandler.isLogElementPresent(logicalDivId)) {
                    handleError("Unable to find logical element with id \"" + logicalDivId + "\" while evaluating docket [" + mediaPath + "].");
                }
            } else if (fileType.contains("Teilauftrag_")) {
                logger.info(mediaPath + " identified as docket for process " + processIDs + ".");
                if (!docketsHandler.isPhysElementPresent(fileType.substring(fileType.indexOf("_") + 1))) {
                    handleError("Unable to find partial job with id \"" + fileType.substring(fileType.indexOf("_") + 1) + "\" while evaluating partial job docket [" + mediaPath + "].");
                    continue;
                }
                if (!physicalDivId.equals("")) {
                    // handle previous partial job
                    try {
                        // TODO Zähler auswerten & zurücksetzen
                        this.docketsHandler.partialJobNumberOfDocketsMapping.put(physicalDivId, partialJobDockets);
                        //writeNumberOfPartialJobDocketsToMetadata(process, physicalDivId, partialJobDockets); // physicalDivId = partial job uuid
                        partialJobDockets = 0;
                        handlePartialJobAfterImagesAssignment(physicalDivId, partialJobImages, process, processIDs);
                        partialJobImages.clear();
                    } catch (IOException | NoSuchElementException e) {
                        handleError("Could not rename images of partial jobs following partial job '"
                                + physicalDivId + "': " + e.getMessage()
                                + "\n(All following partial jobs might be corrupt.)");
                        break;
                    }
                    try {
                        docketsHandler.writeImagePathMapping(docketsHandler.getImagePathMapping());
                    } catch (IOException e) {
                        docketsHandler.addInfoComment("Could not write imagePathMapping file properly. Resetting partial jobs might not work correctly. (" + e.getMessage() + ")");
                    }

                }

                // handle current partial job
                physicalDivId = fileType.substring(fileType.indexOf("_") + 1);
                partialJobDockets++;
                archiveFile(mediaPath, process, physicalDivId);
                partialJobCounter++;

                logger.debug("Start removing old images of partial job " + physicalDivId + " for process " + processIDs);
                try {
                    // delete existing images of current partial job
                    this.docketsHandler.deletePartialJobImages(physicalDivId);
                } catch (IOException | NoSuchElementException e) {
                    handleError("Unable to delete existing images of partial job '" + physicalDivId + "' for process " + processIDs + ": " + e.getMessage());
                }
                logger.debug("Finished removing old images of partial job " + physicalDivId + " for process " + processIDs);

            } else if (fileType.contains("Subdossier")) {
                logger.info(mediaPath + " identified as subddossier for process " + processIDs + ".");
                dossierId = handleSubdossier(fileType, dossierId, physicalDivId);
                if (!docketsHandler.isLogElementPresent(dossierId)) {
                    handleError("Unable to find subdossier with id \"" + dossierId + "\" while evaluating subdossier docket [" + mediaPath + "].");
                }
                partialJobDockets++;
                archiveFile(mediaPath, process, physicalDivId);
                if (!fileType.startsWith("Subdossier_Ende") && fileType.startsWith("Subdossier_")) {
                    //verzeichnete Subdossier
                    structureElementCounter++;
                }
            } else {
                logger.warn("Unable to handle unknown TIFF file type: " + mediaPath + " for process " + processIDs);
            }
        }

        if (!originalDossierId.equals(dossierId)) {
            handleError("Dockets for subdossiers might be wrong: After closing all subdossiers ID should be " + originalDossierId + " but is " + dossierId + " for process " + processIDs + ". (Cause might be an subddosier docket that is spare, missing or placed at the wrong position.");
        }

        if (!logicalDivId.isEmpty() && assignPreviousImages(physicalDivId, logicalDivId, mimeTypeMapping, suffixMapping)) {
            metaXmlIsUpdated = true;
        }
        try {
            // TODO Zähler auswerten & zurücksetzen
            //writeNumberOfPartialJobDocketsToMetadata(process, physicalDivId, partialJobDockets); // physicalDivId = partial job uuid
            this.docketsHandler.partialJobNumberOfDocketsMapping.put(physicalDivId, partialJobDockets);
            handlePartialJobAfterImagesAssignment(physicalDivId, partialJobImages, process, processIDs);
        } catch (IOException | NoSuchElementException e) {
            handleError("Could not rename images of partial jobs following partial job '"
                    + physicalDivId + "': " + e.getMessage()
                    + "\n(All following partial jobs might be corrupt.)");
        }
        int partialJobNumber = 0;
        int structureElementNumber = 0;
        try {
            partialJobNumber = this.docketsHandler.getNumberOfPartialJob();
        } catch (XPathExpressionException | IOException e) {
            handleError("Could not get total number of partial jobs: " + e.getMessage());
        }
        try {
            structureElementNumber = this.docketsHandler.getNumberOfStrucureElement();
        } catch (XPathExpressionException | IOException e) {
            handleError("Could not get total number of structure elements: " + e.getMessage());
        }
        for (String message : this.docketsHandler.checkPartialJobsCompleteness()) {
            JobHelper.createComment(process, message);
        }
        if (Objects.equals(partialJobNumber, partialJobCounter)) {
            if (!Objects.equals(structureElementCounter, structureElementNumber)) {
                handleError("Number of scanned structure dockets doesn't match with expected elements: Found " + structureElementCounter + ", Expected " + structureElementNumber + " .");
            }
        } else {
            this.docketsHandler.addInfoComment("Only " + partialJobCounter + " of " + partialJobNumber + " partial jobs have been scanned.");
        }
        try {
            docketsHandler.writeImagePathMapping(docketsHandler.getImagePathMapping());
        } catch (IOException e) {
            docketsHandler.addInfoComment("Could not write imagePathMapping file properly. Resetting partial jobs might not work correctly. (" + e.getMessage() + ")");
        }
        return metaXmlIsUpdated;
    }

    private static String getMediaFileType(Process process, String mediaPath) throws Exception {
        if (mediaPath.endsWith(ANALYZABLE_FILE_TYPE)) {
            return getTiffFileType(process, mediaPath);
        } else {
            return "IMAGE";
        }
    }

    private static String getTiffFileType(Process process, String tiffPath) throws Exception {
        HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basic(barcode_service_user, barcode_service_password);
        Client client = ClientBuilder.newClient();
        client.register(authFeature);
        client.property(ClientProperties.CONNECT_TIMEOUT, barcode_service_connection_timeout);
        client.property(ClientProperties.READ_TIMEOUT, barcode_service_read_timeout);

        WebTarget webTarget = client
                .target(barcode_service_host + ":" + barcode_service_port)
                .path(barcode_service_path_decode)
                .queryParam("id", process.getId())
                .queryParam("file", FilenameUtils.getName(URI.create(tiffPath).getPath()));
        Response response = webTarget.request(MediaType.APPLICATION_XML).get();

        String responseContent = response.readEntity(String.class);
        if (response.getStatus() == 200 && StringUtils.isNotBlank(responseContent)) {
            return responseContent;
        } else {
            throw new Exception("Error evaluating image '" + tiffPath + "' for process '" + process.getId() + "'. Barcode-service responded: " + responseContent);
        }
    }

    private void writeMetaXmlFile(Process process, String processIDs, boolean temporary) throws IOException {
        logger.debug("Start updating meta.xml for process " + processIDs);
        String metaXml = this.docketsHandler.getUpdatedXmlString();
        metaXml = reorderMetaXML(metaXml);
        URI fileURI = ServiceManager.getFileService().getMetadataFilePath(process);
        if (temporary) {
            fileURI = URI.create(fileURI.toString() + ".tmp");
        }
        XMLHelper.saveXML(fileURI, metaXml);
        logger.debug("Finished updating meta.xml for process " + processIDs);
        if (!temporary) {
            URI tmpFileToBeDeleted = URI.create(fileURI.toString() + ".tmp");
            if (ServiceManager.getFileService().fileExist(tmpFileToBeDeleted)) {
                ServiceManager.getFileService().delete(tmpFileToBeDeleted);
            }
        }
    }


    private void handlePartialJobAfterImagesAssignment(String physicalDivId, List<String> partialJobImages, Process process, String processIDs) throws IOException, NoSuchElementException {
        logger.debug("Start assigning " + partialJobImages.size() + " images to partial job " + physicalDivId);
        if (partialJobImages.isEmpty()) {
            handleError("Partial job (ID: " + physicalDivId + ") without images detected.");
            return;
        }

        // rename images of partial job
        int startIndexNextImages = this.docketsHandler.getStartIndex(physicalDivId) + partialJobImages.size();

        // rename images of following partial jobs
        logger.debug("Start renaming images of following partial jobs");
        this.docketsHandler.renameNextPartialJobImages(physicalDivId, startIndexNextImages);
        logger.debug("Finished renaming images of following partial jobs");

        // rename and move files of current partial job to images directory
        logger.debug("Start moving images to target dir");
        try {
            this.docketsHandler.renameAndMoveTiffs(this.docketsHandler.getStartIndex(physicalDivId), partialJobImages);
        } catch (IOException e) {
            handleError("Could not move images to target dir: " + e.getMessage());
        }
        try {
            writeMetaXmlFile(process, processIDs, true);
        } catch (IOException e) {
            handleError("Error saving meta.xml changes for process " + processIDs + ": " + e.getMessage());
        }
        logger.debug("Finished moving images to target dir");
        logger.debug("Finished assigning images to partial job " + physicalDivId);
    }

    private String handleSubdossier(String fileType, String dossierId, String physicalDivId) {
        if (fileType.contains("Subdossier_Ende")) {
            if (fileType.contains("Subdossier_Ende_") && !fileType.replace("Subdossier_Ende_", "").equals(dossierId)) {
                handleError("Wrong docket for end of subdossier found: " + fileType + " does not match " + dossierId + "!");
            }
            return this.docketsHandler.getParentDossierId(dossierId);
        } else if (fileType.contains("Subdossier_")) {
            return fileType.substring(fileType.indexOf("_") + 1);
        } else {
            return this.docketsHandler.addnewElement("dossier", dossierId, physicalDivId);
        }
    }

    /**
     * Remove all previously generated PREMIS, JP2 and PNG files for this process.
     * These file will be regenerated later.
     *
     * @param process Process
     */
    private void removeOldFiles(Process process) throws IOException {
        URI imagesSubDir = ServiceManager.getFileService().getImagesDirectory(process);
        String imagesDir = "file://" + KitodoConfig.getKitodoDataDirectory() + imagesSubDir;
        String processDir = "file://" + KitodoConfig.getKitodoDataDirectory() + process.getId() + File.separator;
        Set<Folder> previewFolders = process.getProject().getFolders().stream()
                .filter(folder -> folder.equals(process.getProject().getPreview()) || folder.equals(process.getProject().getMediaView()))
                .collect(Collectors.toSet());

        for (Folder folder : previewFolders) {
            logger.debug("Start clearing directory " + folder.getRelativePath());
            clearDirectory(imagesDir, folder.getRelativePath());
            logger.debug("Finished clearing directory " + folder.getRelativePath());
        }
        logger.debug("Start clearing directory " + IMAGES_EXPORT_DIR);
        ServiceManager.getFileService().delete(URI.create(processDir + IMAGES_EXPORT_DIR));
        ServiceManager.getFileService().createDirectory(URI.create(processDir), IMAGES_EXPORT_DIR);
        logger.debug("Finished clearing directory" + IMAGES_EXPORT_DIR);
    }

    private void clearDirectory(String parentDir, String subDir) throws IOException {
        String[] subDirParts = subDir.split("/");
        if (subDirParts.length < 1) {
            throw new IOException("Path to images directory is wrong: " + parentDir + subDir);
        }
        String fullPath = parentDir + subDirParts[subDirParts.length - 1];
        String[] fullPathParts = fullPath.split("/");
        if (fullPathParts.length <= 1) {
            throw new IOException("Path to images directory is wrong: " + fullPath);
        }
        String dirName = fullPathParts[fullPathParts.length - 1];
        String parentSubDir = String.join("/", Arrays.copyOf(fullPathParts, fullPathParts.length - 1));


        if (ServiceManager.getFileService().fileExist(URI.create(fullPath))) {
            ServiceManager.getFileService().delete(URI.create(fullPath));
            ServiceManager.getFileService().createDirectory(URI.create(parentSubDir), dirName);
        }
    }

    /**
     * Prepares the generation of a new process.
     *
     * @return prepared process
     */
    private Process prepareProcessCreation() throws ProcessGenerationException, DAOException {
        Template template;
        try {
            template = getVecteurTemplate();
        } catch (DAOException e) {
            logger.error(e.getMessage());
            Helper.setErrorMessage("Vecteur template not found.");
            return null;
        }

        try {
            ServiceManager.getTemplateService().checkForUnreachableTasks(template.getTasks());
        } catch (ProcessGenerationException e) {
            return null;
        }

        Process process = new Process();
        process.setTitle("");
        process.setTemplate(template);
        process.setProject(ServiceManager.getProjectService().getById(vecteur_project_id));
        process.setRuleset(template.getRuleset());
        process.setDocket(template.getDocket());

        ProcessGenerator.copyTasks(template, process);

        return process;
    }

    /*
    The order of VIADUC status in Kitodo is as follows:
        - ACCEPTED_BY_KITODO
        - READY_FOR_DIGITIZATION
        - IN_PROGRESS | EXTERNAL | CANCELLED
        - DONE
     */

    private void updateTask(Process process, String viaducStatus, String message)
            throws IOException, DataException, IllegalStateException, DAOException {

        String viaducState = getViaducState(process);

        List<Task> currentTasks = ServiceManager.getProcessService().getOpenAndInWorkTasks(process);
        if (currentTasks.isEmpty()) {
            throw new IllegalStateException("Unable to update process with ID " + process.getId() + " to state '"
                    + viaducStatus + "' because current state is '" + viaducState + "'!");
        }

        Task currentTask = currentTasks.get(0);
        switch (viaducStatus) {
            case READY_FOR_DIGITIZATION:
                // Jobs can only be set to 'READY_FOR_DIGITIZATION' once they are retrieved in Viaduc!
                if (currentTask.getTitle().equals(REQUEST_DOSSIER_FROM_VIADUC)
                        || currentTask.getTitle().equals(REQUEST_DOSSIER_FROM_VIADUC_DE)) {
                    new WorkflowControllerService().close(currentTask);
                } else {
                    throw new IllegalStateException("Process must be in state '" + REQUEST_DOSSIER_FROM_VIADUC_DE + "' or '" + REQUEST_DOSSIER_FROM_VIADUC + "'.");
                }
                break;
            case CANCELED:
                // canceling jobs from Viaduc is processed in case "DONE"
                break;
            case INGEST_FAILED:
                if (currentTask.getTitle().equals(SEND_SIP) || currentTask.getTitle().equals(SEND_SIP_DE)) {
                    JobHelper.jumpToFallbackTask(process, message, currentTask.getTitle());
                } else {
                    throw new IllegalStateException("Cannot reinitialize DIR ingest with SIP in state '"
                            + currentTask.getTitle() + "' (must be 'SendSIP'!)");
                }
                break;
            case DONE:
                if (Objects.nonNull(currentTask)) {
                    switch (currentTask.getTitle()) {
                        case SEND_SIP:
                        case SEND_SIP_DE:
                            // Jobs can only be set to "DONE" when the SIP was successfully send to DIR / SFTP!
                            new WorkflowControllerService().close(currentTask);
                            break;
                        case REQUEST_DOSSIER_FROM_VIADUC:
                        case REQUEST_DOSSIER_FROM_VIADUC_DE:
                            // Jobs can only be canceled from Viaduc before their processing begins in Kitodo!
                            cancelJob(process, "Canceled by Viaduc");
                            break;
                        default:
                            throw new DataException("Cannot cancel/clean process (Viaduc ID: " + getViaducId(process)
                                    + "; Kitodo ID: " + process.getId() + ") because it has Viaduc status '" + viaducState
                                    + "' (only processes in Viaduc state 'ACCEPTED_BY_KITODO' can be canceled by Viaduc and "
                                    + "processes in Viaduc state 'READY_FOR_RETRIEVAL' can be set to to 'DONE')");
                    }
                    break;
                } else {
                    throw new DataException("Cannot update task of process " + process.getId()
                            + " because the process has no task with state 'open' or 'in work'.");
                }
            default:
                throw new DataException("Cannot change Viaduc status of process " + process.getId() + " from '"
                        + viaducState + "' to '" + viaducStatus
                        + "' (only 'READY_FOR_DIGITIZATION' and 'DONE' are allowed!)");
        }
    }

    private void deleteProcess(Process process) {
        try {
            ServiceManager.getFileService().delete(ServiceManager.getProcessService().getProcessDataDirectory(process));
            process.getProject().getProcesses().remove(process);
            process.setProject(null);
            process.getTemplate().getProcesses().remove(process);
            process.setTemplate(null);
            ServiceManager.getProcessService().remove(process);
        } catch (DataException | IOException e1) {
            logger.error("unable to remove process with ID " + process.getId());
        }
    }

    private static final HashMap<String, String> viaducStatusTaskMapping;

    static {
        viaducStatusTaskMapping = new HashMap<>();
        viaducStatusTaskMapping.put(REQUEST_DOSSIER_FROM_VIADUC, ACCEPTED_BY_KITODO);
        viaducStatusTaskMapping.put(REQUEST_DOSSIER_FROM_VIADUC_DE, ACCEPTED_BY_KITODO);
        viaducStatusTaskMapping.put(CHECK_DOSSIER, READY_FOR_DIGITIZATION);
        viaducStatusTaskMapping.put(CHECK_DOSSIER_DE, READY_FOR_DIGITIZATION);
        viaducStatusTaskMapping.put(PREPARE_DOSSIER, IN_PROGRESS);
        viaducStatusTaskMapping.put(PREPARE_DOSSIER_DE, IN_PROGRESS);

        /*
         Scanning tasks are now mapped using their "ordering" attribute
         (configure "scanner_task_order" in the "kitodoConfiguration" accordingly)
         These mappings from specific scanner tasks to Viaduc states are only still here
         for existing processes and legacy reasons!
         */
        // tasks for external digitization job assignments
        viaducStatusTaskMapping.put("Auftragserteilung Extern - Charge 1", IN_PROGRESS);
        viaducStatusTaskMapping.put("Auftragserteilung Extern - Charge 2", IN_PROGRESS);
        viaducStatusTaskMapping.put("Auftragserteilung Extern - Charge 3", IN_PROGRESS);
        // scanner tasks of _old_ workflow
        viaducStatusTaskMapping.put("Kodaki4250A", IN_PROGRESS);
        viaducStatusTaskMapping.put("Kodaki4250B", IN_PROGRESS);
        viaducStatusTaskMapping.put("MicroboxUltraIIA", IN_PROGRESS);
        // scanner tasks of _new_ workflow
        viaducStatusTaskMapping.put("Einzug-Scanner - Charge 1", IN_PROGRESS);
        viaducStatusTaskMapping.put("Einzug-Scanner - Charge 2", IN_PROGRESS);
        viaducStatusTaskMapping.put("Einzug-Scanner - Charge 3", IN_PROGRESS);
        viaducStatusTaskMapping.put("Buch-Scanner - Charge 1", IN_PROGRESS);
        viaducStatusTaskMapping.put("Buch-Scanner - Charge 2", IN_PROGRESS);
        viaducStatusTaskMapping.put("Grossformat-Scanner", IN_PROGRESS);
        // data import tasks run parallel to regular scanner tasks and import images from external digitization
        viaducStatusTaskMapping.put("Datenimport - Charge 1", EXTERNAL);
        viaducStatusTaskMapping.put("Datenimport - Charge 2", EXTERNAL);
        viaducStatusTaskMapping.put("Datenimport - Charge 3", EXTERNAL);

        viaducStatusTaskMapping.put(CHECK_SCANS, IN_PROGRESS);
        viaducStatusTaskMapping.put(CHECK_SCANS_DE, IN_PROGRESS);
        viaducStatusTaskMapping.put(EVALUATE_DOCKETS, IN_PROGRESS);
        viaducStatusTaskMapping.put(EVALUATE_DOCKETS_DE, IN_PROGRESS);
        viaducStatusTaskMapping.put(IMAGE_PROCESSING, IN_PROGRESS);
        viaducStatusTaskMapping.put(IMAGE_PROCESSING_DE, IN_PROGRESS);
        viaducStatusTaskMapping.put(CHECK_QUALITY, IN_PROGRESS);
        viaducStatusTaskMapping.put(CHECK_QUALITY_DE, IN_PROGRESS);
        viaducStatusTaskMapping.put(CREATE_SIP, IN_PROGRESS);
        viaducStatusTaskMapping.put(CREATE_SIP_DE, IN_PROGRESS);
        viaducStatusTaskMapping.put(SEND_SIP, SCANNED);
        viaducStatusTaskMapping.put(SEND_SIP_DE, SCANNED);
    }

    private static String mapJobWithoutOpenTasksToViaducState(Process process) {
        // 0 tasks OPEN or INWORK: job DONE or CANCELED
        if (allTasksDone(process)) {
            return DONE;
        } else if (allTasksExceptCleanupLocked(process)) {
            return CANCELED;
        } else {
            return UNKNOWN_STATE;
            //throw new IllegalStateException("Invalid combination of task states found (no open task)!");
        }
    }

    private static String mapSingleTaskToViaducState(Task task) {
        // 1 task OPEN or INWORK: job DONE or CANCELED or map task to VIADUC_STATE
        if (CLEANUP.equals(task.getTitle()) || CLEANUP_DE.equals(task.getTitle())) {
            if (allPriorTasksClosed(task.getProcess())) {
                return DONE;
            } else if (allPriorTasksLocked(task.getProcess())) {
                return CANCELED;
            } else {
                return UNKNOWN_STATE;
                //throw new IllegalStateException("Invalid combination of task states found (one open task)!");
            }
        } else if (viaducStatusTaskMapping.containsKey(task.getTitle())) {
            return viaducStatusTaskMapping.get(task.getTitle());
        } else if (scanner_task_order != 0 && scanner_task_order == task.getOrdering()) {
            return IN_PROGRESS;
        } else {
            return UNKNOWN_STATE;
        }
    }

    private static String mapMultipleTasksToViaducState(List<Task> tasks) {
        Set<Integer> ordering = tasks.stream().map(Task::getOrdering).collect(Collectors.toSet());
        if (ordering.size() == 1) {
            Set<String> states = tasks.stream()
                    .map(t -> viaducStatusTaskMapping.get(t.getTitle()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (states.size() == 1) {
                return (String) states.toArray()[0];
            } else if (states.contains(EXTERNAL)) {
                try {
                    Process job = tasks.get(0).getProcess();
                    setViaducMessage(job, getExternalDigitizationData(getExternalPartialJobs(job)));
                    //setViaducMessage(job, "Die Bearbeitung dieses Digitalisierungsauftrags kann sich aufgrund externer Digitalisierung gegebenenfalls verzögern.");
                } catch (DataException | IOException | DAOException e) {
                    logger.error(e.getMessage());
                }
                return EXTERNAL;
            } else if (scanner_task_order != 0 && ordering.toArray()[0].equals(scanner_task_order)) {
                return IN_PROGRESS;
            }
        }
        return UNKNOWN_STATE;
    }

    /**
     * Retrieve and return Viaduc state of given Process 'job'.
     *
     * @param job Process for which the Viaduc state is returned
     * @return Viaduc state of given Process
     */
    public static String getViaducState(Process job) {
        List<Task> currentTasks = ServiceManager.getProcessService().getOpenAndInWorkTasks(job);
        // list of OPEN and INWORK tasks is empty if:
        // - all tasks are DONE (process DONE)
        // - all tasks are LOCKED (process CANCELED, not yet cleaned up)
        // - all tasks but 'Cleanup' are LOCKED, 'Cleanup' is DONE (process CANCELED, cleanup finished)
        switch (currentTasks.size()) {
            case 0:
                return mapJobWithoutOpenTasksToViaducState(job);
            case 1:
                return mapSingleTaskToViaducState(currentTasks.get(0));
            default:
                return mapMultipleTasksToViaducState(currentTasks);
        }
    }

    private static boolean allPriorTasksInState(Process job, int state) {
        for (Task task : job.getTasks()) {
            if (task.getId().equals(ServiceManager.getProcessService().getCurrentTask(job).getId())) {
                return true;
            } else if (!task.getProcessingStatus().getValue().equals(state)) {
                return false;
            }
        }
        return true;
    }

    private static boolean allPriorTasksClosed(Process job) {
        return allPriorTasksInState(job, TaskStatus.DONE.getValue());
    }

    private static boolean allPriorTasksLocked(Process job) {
        return allPriorTasksInState(job, TaskStatus.LOCKED.getValue());
    }

    private static boolean allTasksDone(Process job) {
        for (Task task : job.getTasks()) {
            if (!DONE.equals(task.getProcessingStatus().toString())) {
                return false;
            }
        }
        return true;
    }

    private static boolean allTasksExceptCleanupLocked(Process job) {
        return job.getTasks().stream()
                .filter(t -> !CLEANUP.equals(t.getTitle()) && !CLEANUP_DE.equals(t.getTitle()))
                .allMatch(t -> TaskStatus.LOCKED.getValue().equals(t.getProcessingStatus().getValue()))
                && job.getTasks().stream()
                .filter(t -> CLEANUP.equals(t.getTitle()) || CLEANUP_DE.equals(t.getTitle()))
                .filter(t -> !TaskStatus.LOCKED.getValue().equals(t.getProcessingStatus().getValue()))
                .count() == 1;
    }

    private Process getProcessByViaducID(String viaducID) {
        ProcessService processService = ServiceManager.getProcessService();
        try {
            List<ProcessDTO> processes = processService.findByProperty(VIADUC_ID, viaducID);
            if (!processes.isEmpty()) {
                return processService.getById(processes.get(0).getId());
            }
        } catch (DAOException | DataException e) {
            logger.error("ERROR: unable to retrieve process (Viaduc ID: " + viaducID + ")!");
        }
        return null;
    }

    private Process getProcessByTitle(String processTitle) {
        try {
            List<ProcessDTO> processDTOS = ServiceManager.getProcessService().findByTitle(processTitle);
            List<Process> processes = new LinkedList<>();
            for (ProcessDTO processDTO : processDTOS) {
                processes.add(ServiceManager.getProcessService().getById(processDTO.getId()));
            }
            // always get the newest process if multiple processes with the same title exist
            processes.sort(Comparator.comparing(Process::getCreationDate).reversed());
            return processes.get(0);
        } catch (DataException | DAOException | IndexOutOfBoundsException e) {
            logger.error("ERROR: unable to retrieve process with title " + processTitle + "!");
        }
        return null;
    }

    public static String getViaducMessage(Process process) {
        return getPropertyValue(process, VIADUC_MESSAGE);
    }

    public static String getViaducId(Process process) {
        return getPropertyValue(process, VIADUC_ID);
    }

    private static Property getProperty(Process process, String propertyTitle) {
        for (Property property : process.getProperties()) {
            if (property.getTitle().equals(propertyTitle)) {
                return property;
            }
        }
        return null;
    }

    private static String getPropertyValue(Process process, String propertyTitle) {
        Property property = getProperty(process, propertyTitle);
        if (Objects.isNull(property)) {
            return "";
        }
        return property.getValue();
    }

    private static void performCleanup(Process job) throws DataException, IOException {
        List<Task> cleanUpTasks = job.getTasks().stream().filter(t -> CLEANUP.equals(t.getTitle()) || CLEANUP_DE.equals(t.getTitle()))
                .collect(Collectors.toList());
        if (cleanUpTasks.size() != 1) {
            throw new DataException("Unable to find unique CleanUp task!");
        }
        Task cleanUpTask = cleanUpTasks.get(0);
        if (StringUtils.isBlank(cleanUpTask.getScriptPath())) {
            throw new IOException("Cleanup script not defined!");
        }
        String cleanupScriptPath = cleanUpTask.getScriptPath().split(" ")[0];
        File cleanupFile = new File(Paths.get(cleanupScriptPath).toUri());
        if (!cleanupFile.exists() || !cleanupFile.canRead()) {
            throw new IOException("Unable to read cleanup script '" + cleanupScriptPath + "'!");
        }
        for (Task t : job.getTasks()) {
            cleanupTask(t);
        }
        ServiceManager.getTaskService().executeScript(cleanUpTask, cleanUpTask.getScriptPath(), true);
    }

    private static void cleanupTask(Task task) throws DataException {
        if (CLEANUP.equals(task.getTitle()) || CLEANUP_DE.equals(task.getTitle())) {
            task.setProcessingStatus(TaskStatus.OPEN);
        } else {
            task.setProcessingStatus(TaskStatus.LOCKED);
        }
        ServiceManager.getTaskService().save(task, true);
    }

    private static void setViaducMessage(Process job, String message) throws DataException, DAOException {
        Property viaducMessage = getProperty(job, VIADUC_MESSAGE);
        if (Objects.isNull(viaducMessage)) {
            viaducMessage = ProcessGenerator.addPropertyForProcess(job, VIADUC_MESSAGE, message);
        } else {
            viaducMessage.setValue(message);
        }
        ServiceManager.getPropertyService().saveToDatabase(viaducMessage);
        ServiceManager.getProcessService().save(job, true);
    }

    /**
     * Cancel given process 'job' and set the given String 'cancellationMessage' as value of the process' Property
     * 'VIADUC_STATUS'.
     *
     * @param job                 process to be canceled
     * @param cancellationMessage value to be set to process property 'VIADUC_MESSAGE'
     */
    public static void cancelJob(Process job, String cancellationMessage) throws DataException, DAOException,
            IOException {
        setViaducMessage(job, cancellationMessage);
        performCleanup(job);
        Process updatedProcess = ServiceManager.getProcessService().getById(job.getId());
        ServiceManager.getProcessService().save(updatedProcess, "100000000");
    }

    /**
     * Return constant containing the name of the property key for the metadata directory.
     *
     * @return property key of metadata directory
     */
    static String getMetadataFolder() {
        return METADATA_FOLDER;
    }

    private static final List<String> cancelCodes;

    static {
        cancelCodes = new LinkedList<>();
        cancelCodes.add("01 Auftragseinheit fehlt in Behältnis");
        cancelCodes.add("02 Auftragseinheit nicht identifizierbar");
        cancelCodes.add("03 Aufnahmetauglichkeit negativ");
        cancelCodes.add("04 Spezialmaterial");
        cancelCodes.add("05 Ingest DIR fehlgeschlagen (ID bereits vorhanden)");
        cancelCodes.add("06 Andere");
    }

    public static List<String> getCancelCodes() {
        return cancelCodes;
    }

    public static String getDigitizationProfile(String taskName) {
        return digitizationProfiles.contains(taskName) ? taskName : "";
    }

    /**
     * Return list of task titles in Vecteur workflow.
     *
     * @return list of task titles in Vecteur workflow
     */
    public static List<String> getWorkflowTasks() throws DAOException, ProcessGenerationException {
        Template template = getVecteurTemplate();
        List<String> taskTitles = new LinkedList<>();
        for (Task task : template.getTasks()) {
            if (!taskTitles.contains(task.getTitle())) {
                List<String> concurrentTaskTitles = new LinkedList<>();
                concurrentTaskTitles.add(task.getTitle());
                for (Task concurrentTaskCandidate : template.getTasks()) {
                    if (task.getId().equals(concurrentTaskCandidate.getId())) {
                        continue;
                    }
                    if (task.getOrdering().equals(concurrentTaskCandidate.getOrdering()) && concurrentTaskCandidate.isConcurrent()) {
                        concurrentTaskTitles.add(concurrentTaskCandidate.getTitle());
                    }
                }
                Collections.sort(concurrentTaskTitles);
                taskTitles.addAll(concurrentTaskTitles);
            }
        }
        return taskTitles;
    }

    /**
     * Compute and return the runtime of the task with the provided name 'taskTitle' for the provided process
     * 'processDTO'.
     *
     * @param processDTO process for which the runtime of the task with the provided name 'taskTitle' is computed
     * @param taskTitle  name of task whose runtime is computed for provided process 'processDTO'
     * @return runtime of task
     * @throws DAOException when process cannot be found
     */
    public static String computeTaskRuntime(ProcessDTO processDTO, String taskTitle) throws DAOException {
        if (Objects.nonNull(processDTO) && Objects.nonNull(taskTitle)) {
            Process process = ServiceManager.getProcessService().getById(processDTO.getId());
            if (Objects.nonNull(process)) {
                for (Task task : process.getTasks()) {
                    if (task.getTitle().equals(taskTitle)
                            && Objects.nonNull(task.getProcessingBegin())
                            && Objects.nonNull(task.getProcessingEnd())) {
                        LocalDateTime start = task.getProcessingBegin().toInstant().atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        LocalDateTime end = task.getProcessingEnd().toInstant().atZone(ZoneId.systemDefault())
                                .toLocalDateTime();
                        Duration duration = Duration.between(start, end);
                        return String.format("%sh; %sm; %ss",
                                duration.toHours(),
                                duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours()),
                                duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
                    }
                }
            }
        }
        return "-";
    }

    private void handleError(String message) {
        if (errors.isEmpty()) {
            errors.add("EvaluateDockets failed (There might be subsequent errors):");
        }
        logger.error(message);
        errors.add(message);
    }

    private String prepareErrorMessage() {
        return String.join("\n", errors);
    }

    /**
     * Handles requests to cleanup a process with the given process id.
     *
     * @param id int containing the process id
     * @return Response object containing status 200 or 500
     */
    @GET
    @Path("/{processId}/cleanup")
    @Produces("plain/text")
    public Response cleanUp(@PathParam("processId") int id) {
        String message;
        try {
            Process process = ServiceManager.getProcessService().getById(id);
            String viaducID = getViaducId(process);
            if (!canBeCleanedUp(process)) {
                message = "Unable to cleanup process (Viaduc ID: " + viaducID + ", Kitodo ID: " + id + "); reason: invalid open tasks (only active task allowed is 'Cleanup')!";
                logger.error(message);
                return Response.status(INTERNAL_SERVER_ERROR).entity(message).build();
            }
            // remove job 'capacity' from 'usedCapacity'
            removeJobFromUsedCapacity(process);
            // create report for digitization report
            ServiceManager.getReportService().storeJobReportToDatabase(process);
            // set process title to 'Auftrag ' + [job ID]
            process.setTitle("Auftrag " + getViaducId(process));
            // save VIADUC message in case it contains a cancellation message
            String viaducMessage = getViaducMessage(process);
            // remove process properties
            List<Integer> propertiesToRemove = new LinkedList<>();
            for (Property property : process.getProperties()) {
                property.getProcesses().clear();
                propertiesToRemove.add(property.getId());
            }
            process.getProperties().clear();
            // add process property containing the number of pages of this process!
            ProcessGenerator.addPropertyForProcess(process, "numberOfPages", String.valueOf(countProcessScans(process)));
            // re-add Viaduc message which potentially contains cancellation message
            if (!viaducMessage.isEmpty()) {
                ProcessGenerator.addPropertyForProcess(process, VIADUC_MESSAGE, viaducMessage);
            }

            // Add value of "benutzungskopie" as process property (required by REST endpoint "cleanable")
            Document metadataDocument = XMLHelper.getMetadataDocument(process);
            ProcessGenerator.addPropertyForProcess(process, WORKING_COPY,
                    String.valueOf(SIPMetadata.getWorkingCopyType(metadataDocument)));

            // save updated process
            ServiceManager.getProcessService().save(process, true);
            // remove old process properties
            for (int propertyId : propertiesToRemove) {
                ServiceManager.getPropertyService().removeFromDatabase(propertyId);
            }
            // remove 'metadata' subfolder of process with given ID
            // FIXME: "cleanup" shouldn't be considered "successful" when removing the process' metadata folder failed!
            try {
                ServiceManager.getFileService().delete(ServiceManager.getProcessService().getProcessDataDirectory(process));
            } catch (IOException e) {
                logger.error("Process (Viaduc ID: " + viaducID + ", Kitodo ID: " + viaducID + ") cleanup successful, but an error occurred while deleting the process metadata folder (" + e.getLocalizedMessage() + ")");
            }
            message = "Successfully cleaned up process (Viaduc ID: " + viaducID + ", Kitodo ID: " + id + ")!";
            logger.info(message);
            return Response.status(OK).entity(message).build();
        } catch (NullPointerException | DAOException | DataException e) {
            message = "Error while cleaning up process (Kitodo ID: " + id + ")! (" + e.getLocalizedMessage() + ")";
            logger.error(message);
            return Response.status(INTERNAL_SERVER_ERROR).entity(message).build();
        }
    }

    public static void removeJobFromUsedCapacity(Process job) throws DAOException {
        if (Objects.isNull(usedCapacity)) {
            calculateUsedCapacity();
        }
        try {
            usedCapacity -= Integer.parseInt(getPropertyValue(job, VIADUC_SIZE));
        } catch (NumberFormatException e) {
            logger.error("Unable to update 'used capacity': " + e.getMessage());
        }
    }

    public static void addJobToUsedCapacity(Process job) throws DAOException {
        if (Objects.isNull(usedCapacity)) {
            calculateUsedCapacity();
            return;
        }
        try {
            usedCapacity += Integer.parseInt(getPropertyValue(job, VIADUC_SIZE));
        } catch (NumberFormatException e) {
            logger.error("Unable to update 'used capacity': " + e.getMessage());
        }
    }

    private boolean canBeCleanedUp(Process process) {
        boolean canBeCleanedUp = false;
        for (Task task : process.getTasks()) {
            if ((CLEANUP.equals(task.getTitle()) || CLEANUP_DE.equals(task.getTitle())) && (task.getProcessingStatus().equals(TaskStatus.OPEN)
                    || task.getProcessingStatus().equals(TaskStatus.INWORK))) {
                canBeCleanedUp = true;
                break;
            }
        }
        return canBeCleanedUp && (process.getTasks().stream()
                .filter(t -> (t.getProcessingStatus().equals(TaskStatus.INWORK)
                        || t.getProcessingStatus().equals(TaskStatus.OPEN))).count() == 1);
    }

    public static int countProcessScans(Process process) {
        List<Folder> folders = process.getProject().getFolders();
        for (Folder folder : folders) {
            if (folder.getFileGroup().equals("LOCAL")) {
                java.nio.file.Path imagesDir = Paths.get(KitodoConfig.getKitodoDataDirectory()
                        + ServiceManager.getProcessService().getProcessDataDirectory(process)
                        + File.separator
                        + folder.getRelativePath());
                if (imagesDir.toFile().exists() && imagesDir.toFile().isDirectory()) {
                    List<String> tiffsPaths = JobHelper.getMediaPaths(imagesDir, MEDIA_FILE_TYPES);
                    return tiffsPaths.size();
                }
            }
        }
        return 0;
    }

    private static String createErrorResponseBody(String errorMessage) {
        logger.error(errorMessage);
        return STATE_RESPONSE_BODY
                .replace(STATUS_PLACEHOLDER, ERROR_STATUS)
                .replace(DESCRIPTION_PLACEHOLDER, errorMessage);
    }

    private static Template getVecteurTemplate() throws DAOException, ProcessGenerationException {
        if (vecteur_project_id < 1) {
            throw new ProcessGenerationException("Vecteur project ID not configured correctly");
        }
        Project vecteurProject = ServiceManager.getProjectService().getById(vecteur_project_id);
        if (Objects.isNull(vecteurProject)) {
            throw new ProcessGenerationException("unable to find project with ID " + vecteur_project_id);
        }
        logger.info("Project '" + vecteurProject.getTitle() + "' (ID: " + vecteur_project_id + ") loaded.");
        if (vecteurProject.getTemplates().size() > 1) {
            throw new ProcessGenerationException("multiple templates configured for Vecteur project! Only exactly one template may be configured at any time");
        }
        if (vecteurProject.getTemplates().size() < 1) {
            throw new ProcessGenerationException("no template configured for Vecteur project! Only exactly one template may be configured at any time");
        }
        return vecteurProject.getTemplates().get(0);
    }

    private static List<PhysicalDivision> getExternalPartialJobs(Process process) throws IOException {
        URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(process);
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);
        return workpiece.getAllPhysicalDivisions().stream().filter(JobService::isExternalPartialJob)
                .collect(Collectors.toList());
    }

    private static String getExternalDigitizationDueDate(PhysicalDivision partialJob) {
        return getMetadataValue(partialJob, DUE_DATE);
    }

    private static String getExternalDigitizationOriginalFormat(PhysicalDivision partialJob) {
        return getMetadataValue(partialJob, ORIGINAL_FORMAT);
    }

    private static String getExternalDigitizationData(List<PhysicalDivision> partialJobs) {
        LinkedList<String> externalDigitizationData = new LinkedList<>();
        for (PhysicalDivision partialJob : partialJobs) {
            LinkedList<String> currentJobsData = new LinkedList<>();
            currentJobsData.add(getExternalDigitizationDueDate(partialJob));
            currentJobsData.add(getExternalDigitizationOriginalFormat(partialJob));
            externalDigitizationData.add(String.join(", ", currentJobsData));
        }
        return String.join("; ", externalDigitizationData);
    }

    private static String getMetadataValue(PhysicalDivision physicalDivision, String metadataName) {
        for (Metadata metadata : physicalDivision.getMetadata()) {
            if (metadata instanceof MetadataEntry) {
                MetadataEntry entry = (MetadataEntry) metadata;
                if (metadataName.equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return "";
    }

    private static boolean isExternalPartialJob(PhysicalDivision physicalDivision) {
        return (PARTIAL_JOB.equals(physicalDivision.getType()) && getMetadataValue(physicalDivision, PROFILE).contains(EXTERN));
    }

    private static boolean isOverdue(PhysicalDivision partialJob) {
        String dueDate = getExternalDigitizationDueDate(partialJob);
        return StringUtils.isNotBlank(dueDate) && LocalDate.parse(dueDate).isBefore(LocalDate.now());
    }

    /**
     * Check whether a job has external partial jobs and if any of them is overdue or not and return corresponding CSS
     * class names.
     *
     * @param job digitization job as Process
     * @return CSS class names
     * @throws IOException if unable to load process metadata file
     */
    public static String getExternalStatus(Process job) throws IOException {
        if (EXTERNAL.equals(JobService.getViaducState(job))) {
            if (getExternalPartialJobs(job).stream().anyMatch(JobService::isOverdue)) {
                return CSS_CLASS_EXTERNAL + " " + CSS_CLASS_OVERDUE;
            } else {
                return CSS_CLASS_EXTERNAL;
            }
        } else {
            return "";
        }
    }

    public int getKitodoCapacity() {
        return kitodo_capacity;
    }

    public int getUsedCapacity() {
        return usedCapacity;
    }

    private void writeNumberOfPartialJobDocketsToMetadata(Workpiece workpiece, String physicalDivId, int partialJobDockets) {
        List<PhysicalDivision> partialJobs = workpiece.getAllPhysicalDivisions().stream()
                .filter(m -> PARTIAL_JOB.equals(m.getType()) && physicalDivId.equals(m.getDivId()))
                .collect(Collectors.toList());
        if (partialJobs.size() == 1) {

            PhysicalDivision partialJob = partialJobs.get(0);
            MetadataEntry docketsMetadata = new MetadataEntry();
            docketsMetadata.setKey(NUMBER_DOCKETS);
            docketsMetadata.setValue(String.valueOf(partialJobDockets));
            docketsMetadata.setDomain(MdSec.DMD_SEC);

            MetadataEntry actualImageNumber = new MetadataEntry();
            actualImageNumber.setKey(NUMBER_ACTUAL_IMAGES);
            actualImageNumber.setValue(String.valueOf(docketsHandler.getActualNumberOfImagesPartialJob(physicalDivId)));
            actualImageNumber.setDomain(MdSec.DMD_SEC);

            Metadata oldMetadataDockets = null;
            Metadata oldMetadataImages = null;
            for (Metadata metadata : partialJob.getMetadata()) {
                if (metadata instanceof MetadataEntry) {
                    if (NUMBER_DOCKETS.equals(metadata.getKey())){
                        oldMetadataDockets = metadata;
                    } else if (NUMBER_ACTUAL_IMAGES.equals(metadata.getKey())) {
                        oldMetadataImages = metadata;
                    }
                }
            }
            if (Objects.nonNull(oldMetadataDockets)) {
                partialJob.getMetadata().remove(oldMetadataDockets);
            }
            if (Objects.nonNull(oldMetadataImages)) {
                partialJob.getMetadata().remove(oldMetadataImages);
            }
            partialJob.getMetadata().add(docketsMetadata);
            partialJob.getMetadata().add(actualImageNumber);
        } else {
            logger.error("Number of partial jobs with ID '" + physicalDivId + "' = " + partialJobs.size()
                    + " => Unable to write number of dockets to partial job!");
        }
    }

    private void saveMetadataFile(Process process, Workpiece workpiece, URI metadataFilepath) {
        try (OutputStream outputStream = ServiceManager.getFileService().write(metadataFilepath)) {
            ServiceManager.getMetsService().save(workpiece, outputStream);
            ServiceManager.getProcessService().saveToIndex(process, false);
        } catch (IOException | DataException | CustomResponseException e) {
            e.printStackTrace();
        }
    }

    private void saveNumberOfPartialJobDocketsToMetadataFile(Process process, HashMap<String, Integer> mapping) {
        try {
            URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(process);
            Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);

            for (Map.Entry<String, Integer> entry : mapping.entrySet()) {
                writeNumberOfPartialJobDocketsToMetadata(workpiece, entry.getKey(), entry.getValue());
            }

            saveMetadataFile(process, workpiece, metadataFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void calculateUsedCapacity() throws DAOException {
        int capacity = 0;
        for (Process process : ServiceManager.getProcessService().getAll()) {
            String viaducStatus = null;
            try {
                viaducStatus = getViaducState(process);
            } catch (IllegalStateException e) {
                logger.error(e.getMessage());
            }
            if (Objects.isNull(viaducStatus)
                    || viaducStatus.equals(ACCEPTED_BY_KITODO)
                    || viaducStatus.equals(READY_FOR_DIGITIZATION)
                    || viaducStatus.equals(IN_PROGRESS)) {
                {
                    List<Property> processProperties = process.getProperties();
                    for (Property property : processProperties) {
                        if (property.getTitle().equals(VIADUC_SIZE)) {
                            capacity += Integer.parseInt(property.getValue());
                        }
                    }
                }
            }
        }
        usedCapacity = capacity;
    }
}
