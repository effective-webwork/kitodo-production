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

package org.kitodo.production.services.relayserver.helper;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.DOMOutputter;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.kitodo.config.KitodoConfig;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.PremisException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.relayserver.helper.validator.ResourceResolver;
import org.kitodo.production.version.KitodoVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLHelper {

    private static final Logger logger = LogManager.getLogger(XMLHelper.class);

    // digitization job XPaths
    private static final String JOB_ID_XPATH = "/DigitalisierungsAuftrag/Auftragsdaten/AuftragsId";
    private static final String JOB_TITLE = "/DigitalisierungsAuftrag/Dossier/Titel";
    private static final String JOB_SIGNATURE = "/DigitalisierungsAuftrag/Dossier/Signatur";
    private static final String JOB_SIZE = "//DigitalisierungsAuftrag/Dossier/Behaeltnisse/Behaeltnis";

    // meta.xml XPaths
    private static final String COMPLEXITY_METADATA_XPATH = "//kitodo/metadata[@name='complexity']";

    private static final String JOB_STATE = "/job/status";
    private static final String JOB_MESSAGE = "/job/message";

    // PREMIS constants

    private static final String VERSION = "version";
    private static final String XMLNS = "xmlns";
    private static final String XSI = "xsi";
    private static final String SCHEMA_LOCATION = "schemaLocation";
    private static final String VECTEUR = "vecteur";

    private static final String OBJECT = "object";
    private static final String TYPE = "type";
    private static final String FILE = "file";
    private static final String OBJECT_IDENTIFIER = "objectIdentifier";
    private static final String OBJECT_IDENTIFIER_TYPE = "objectIdentifierType";
    private static final String OBJECT_IDENTIFIER_VALUE = "objectIdentifierValue";
    private static final String VALUE_URI = "valueURI";
    private static final String LOCAL = "local";
    private static final String TRANSFER = "transfer";
    private static final String OBJECT_CHARACTERISTICS = "objectCharacteristics";
    private static final String FORMAT = "format";
    private static final String FORMAT_DESIGNATION = "formatDesignation";
    private static final String FORMAT_NAME = "formatName";
    private static final String XML = "XML";
    private static final String VIAF = "viaf";

    private static final String GRANTOR_IDENTIFIER_VALUE = "121997218";
    private static final String RIGHTSHOLDER_IDENTIFIER_VALUE = "134892105";

    private static final String EVENT = "event";
    private static final String EVENT_TYPE = "eventType";
    private static final String EVENT_IDENTIFIER = "eventIdentifier";
    private static final String EVENT_IDENTIFIER_TYPE = "eventIdentifierType";
    private static final String EVENT_IDENTIFIER_VALUE = "eventIdentifierValue";
    private static final String EVENT_DATE_TIME = "eventDateTime";
    private static final String EVENT_DETAIL_INFORMATION = "eventDetailInformation";
    private static final String EVENT_DETAIL = "eventDetail";
    private static final String EVENT_OUTCOME_INFORMATION = "eventOutcomeInformation";
    private static final String EVENT_OUTCOME = "eventOutcome";

    private static final String AGENT = "agent";
    private static final String AGENT_IDENTIFIER = "agentIdentifier";
    private static final String AGENT_IDENTIFIER_TYPE = "agentIdentifierType";
    private static final String AGENT_IDENTIFIER_VALUE = "agentIdentifierValue";
    private static final String AGENT_NAME = "agentName";
    private static final String AGENT_TYPE = "agentType";
    private static final String AGENT_VERSION = "agentVersion";
    private static final String BAR_AGENT_NAME = "Schweizerisches Bundesarchiv BAR";
    private static final String VECTEUR_AGENT_NAME = "Vecteur";

    private static final String RIGHTS = "rights";
    private static final String RIGHTS_STATEMENT = "rightsStatement";
    private static final String RIGHTS_BASIS = "rightsBasis";
    private static final String RIGHTS_STATEMENT_IDENTIFIER = "rightsStatementIdentifier";
    private static final String RIGHTS_STATEMENT_IDENTIFIER_TYPE = "rightsStatementIdentifierType";
    private static final String RIGHTS_STATEMENT_IDENTIFIER_VALUE = "rightsStatementIdentifierValue";
    private static final String RIGHTS_RELATED_AGENT_ROLE = "rightsRelatedAgentRole";

    private static final String LINKING_AGENT_IDENTIFIER = "linkingAgentIdentifier";
    private static final String LINKING_AGENT_IDENTIFIER_TYPE = "linkingAgentIdentifierType";
    private static final String LINKING_AGENT_IDENTIFIER_VALUE = "linkingAgentIdentifierValue";
    private static final String LINKING_AGENT_ROLE = "linkingAgentRole";

    private static final String PREMIS_NAMESPACE = "http://www.loc.gov/premis/v3";
    private static final String PREMIS_XSD_LOCATION = "http://www.loc.gov/premis/v3 http://www.loc.gov/standards/premis/premis.xsd";
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String VOCABULARY_IDENTIFIERS_URL = "http://id.loc.gov/vocabulary/identifiers/";
    private static final String LOCAL_VOCABULARY_URL = VOCABULARY_IDENTIFIERS_URL + "local";
    private static final String VIAF_VOCABULARY_URL = VOCABULARY_IDENTIFIERS_URL + VIAF;
    private static final String PRESERVATION_VOCABULARY_BASE_URL = "http://id.loc.gov/vocabulary/preservation/";
    private static final String TRANSFER_VOCABULARY_URL = PRESERVATION_VOCABULARY_BASE_URL + EVENT_TYPE + "/tra.html";
    private static final String ORGANISATION_VOCABULARY_URL = PRESERVATION_VOCABULARY_BASE_URL + AGENT_TYPE + "/org";
    private static final String SOFTWARE_VOCABULARY_URL = PRESERVATION_VOCABULARY_BASE_URL + AGENT_TYPE + "/sof";
    private static final String COPYRIGHT_VOCABULARY_URL = PRESERVATION_VOCABULARY_BASE_URL + RIGHTS_BASIS + "/cop";
    private static final String STATUTE_VOCABULARY_URL = PRESERVATION_VOCABULARY_BASE_URL + RIGHTS_BASIS + "/sta";
    private static final String GRANTOR_VOCABULARY_URL = PRESERVATION_VOCABULARY_BASE_URL + RIGHTS_RELATED_AGENT_ROLE + "/gra";
    private static final String RIGHTSHOLDER_VOCABULARY_URL = PRESERVATION_VOCABULARY_BASE_URL + RIGHTS_RELATED_AGENT_ROLE + "/rig";

    private static final String ORGANISATION = "organization";
    private static final String SOFTWARE = "software";
    private static final String COPYRIGHT = "copyright";
    private static final String STATUTE = "statute";

    private static final String CREATOR = "Creator";
    private static final String CONTRIBUTOR = "Contributor";
    private static final String PUBLISHER = "Publisher";
    private static final String GRANTOR = "grantor";
    private static final String RIGHTSHOLDER = "rightsholder";
    private static final String COMMENT = "Comment";

    private static final String NOT_AVAILABLE = "N/A";

    private static final String RECEIVE_DIGITIZATION_JOB = "ReceiveDigitizationJob";
    private static final String RECEIVE_DIGITIZATION_JOB_DE = "Digitalisierungsauftrag empfangen";

    private static final String CREATE_SIP = "CreateSIP";
    private static final String CREATE_SIP_DE = "SIP erstellen";

    public static final String STARTED = "Started";
    public static final String FINISHED = "Finished";
    public static final String HAND_OVER = "Hand over";
    public static final String USER = "user";
    private static final String TIMESTAMP = "timestamp";
    private static final String TASK = "task";
    private static final String CORRECTION_COMMENT = "correctionComment";
    private static final String FINISHED_WITH_ERRORS_STRING = "Finished with error; comment: ";
    private static final String TASK_OUTCOME_TYPE = "outcomeType";
    private static final String CORRECTION_WORKFLOW = "correctionWorkflow";
    public static final String NUMBER_OF_SCANNED_PAGES = "numberOfScannedPages";

    private static final Set<ZoneId> PREFERRED_ZONE_IDS = new HashSet<>();
    static {
        PREFERRED_ZONE_IDS.add(ZoneId.of("Europe/Zurich"));
    }
    private static final DateTimeFormatter TIMESTAMP_FORMATTER_A = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER_B = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER_C = new DateTimeFormatterBuilder()
            .appendPattern("E MMM d HH:mm:ss ")
            .optionalStart().appendZoneText(TextStyle.SHORT, PREFERRED_ZONE_IDS).optionalEnd()
            .appendPattern(" yyyy")
            .toFormatter( Locale.ENGLISH);

    private static final Pattern PREMIS_USER_PATTERN = Pattern.compile("[^0-9]+:\\s([0-9]+)$");

    private static final String NA = "N/A";
    private static final String SYSTEM = "System";

    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder documentBuilder;

    private static final HashMap<String, String> agentIdentifierTypeMapping = new HashMap<>();

    static {
        agentIdentifierTypeMapping.put("Creator", ORGANISATION);
        agentIdentifierTypeMapping.put("Contributor", ORGANISATION);
        agentIdentifierTypeMapping.put("Publisher", ORGANISATION);
        agentIdentifierTypeMapping.put("Version", SOFTWARE);
    }

    private static final HashMap<String, String> agentTypeURIMapping = new HashMap<>();

    static {
        agentTypeURIMapping.put(ORGANISATION, ORGANISATION_VOCABULARY_URL);
        agentTypeURIMapping.put(SOFTWARE, SOFTWARE_VOCABULARY_URL);
    }

    /**
     * Parse the XML in the given string and return it as a Document.
     *
     * @param xmlString String containing XML
     * @return given XML string parsed into a Document
     */
    public static Document parseXML(String xmlString) {

        Document document = null;

        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(new InputSource(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            logger.error("could not parse XML string '" + xmlString + "'!");
        }

        return document;
    }

    /**
     * Load the file containing XML as text from and return it as a Document.
     *
     * @param filePath the filepath of the file containg XML content
     * @return XML content of the specified file parsed into a Document
     */
    public static Document loadXMLFile(String filePath) {

        Document document = null;

        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(new File(filePath));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return document;

    }

    private static int getIntValue(Document job, String nodeXpath) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            return Integer.parseInt(xPath.evaluate(nodeXpath, job));
        } catch (XPathExpressionException | NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Retrieve and return String value from provided Document 'job' and XPath 'nodeXpath'.
     * If given boolean parameter 'sanitize' is true, the String will be stripped of all non-alpha-numeric symbols.
     * Otherwise, the String is returned as-is.
     *
     * @param job the Document from which the String value is extracted
     * @param nodeXPath the XPath describing the node whose String value is extracted
     * @param sanitize boolean flag indicating whether extracted String value should be stripped of all non-alpha-numeric
     *                symbols or not.
     * @return the extracted String value
     */
    public static String getStringValue(Document job, String nodeXPath, boolean sanitize) {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        try {
            if (sanitize) {
                return sanitizeString(xPath.evaluate(nodeXPath, job));
            } else {
                return xPath.evaluate(nodeXPath, job);
            }
        } catch (XPathExpressionException e) {
            return "";
        }
    }

    private static String getStringValue(Document job, String nodeXpath) {
        return getStringValue(job, nodeXpath, true);
    }

    /**
     * Retrieve and return the job ID of the digitization job contained in the given document 'job'.
     *
     * @param job Document containing a digitization job
     * @return the ID of the digitization job
     */
    public static int getJobId(Document job) {
        return getIntValue(job, JOB_ID_XPATH);
    }

    public static String getComplexity(Document job) {
        return getStringValue(job, COMPLEXITY_METADATA_XPATH);
    }

    public static int getNumberOfPartialJobFiles(Document job, String profileXpath, String metadataName)
            throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList profileNodes = (NodeList) xPath.evaluate(profileXpath, job, XPathConstants.NODESET);
        int numberOfFiles = 0;
        for (int i = 0; i < profileNodes.getLength(); i++) {
            Document parentDocument = documentBuilder.newDocument();
            Node newRootNode = parentDocument.importNode(profileNodes.item(i).getParentNode(), true);
            parentDocument.appendChild(newRootNode);
            numberOfFiles += getIntValue(parentDocument, "/kitodo/metadata[@name='" + metadataName + "']");
        }
        return numberOfFiles;
    }

    public static String getOriginalFormat(Document job, String profileXpath) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList profileNodes = (NodeList) xPath.evaluate(profileXpath, job, XPathConstants.NODESET);
        if (profileNodes.getLength() > 0) {
            Document parentDocument = documentBuilder.newDocument();
            Node newRootNode = parentDocument.importNode(profileNodes.item(0).getParentNode(), true);
            parentDocument.appendChild(newRootNode);
            return getStringValue(parentDocument, "/kitodo/metadata[@name='originalFormat']");
        }
        return "N/A";
    }

    public static String getDeliveryFormat(Document job, String profileXpath) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList profileNodes = (NodeList) xPath.evaluate(profileXpath, job, XPathConstants.NODESET);
        if (profileNodes.getLength() > 0) {
            Document parentDocument = documentBuilder.newDocument();
            Node newRootNode = parentDocument.importNode(profileNodes.item(0).getParentNode(), true);
            parentDocument.appendChild(newRootNode);
            return getStringValue(parentDocument, "/kitodo/metadata[@name='deliveryFormat']");
        }
        return "N/A";
    }

    public static String getReturnDate(Document job, String profileXpath) throws XPathExpressionException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList profileNodes = (NodeList) xPath.evaluate(profileXpath, job, XPathConstants.NODESET);
        if (profileNodes.getLength() > 0) {
            Document parentDocument = documentBuilder.newDocument();
            Node newRootNode = parentDocument.importNode(profileNodes.item(0).getParentNode(), true);
            parentDocument.appendChild(newRootNode);
            return getStringValue(parentDocument, "/kitodo/metadata[@name='returnDate']");
        }
        return "N/A";
    }

    /**
     * Retrieve and return the signature of the digitization job contained in the given document 'job'.
     *
     * @param job Document containing a digitization job
     * @return the signature of the digitization job
     */
    public static String getJobSignature(Document job) {
        return getStringValue(job, JOB_SIGNATURE);
    }

    /**
     * Retrieve and return the state of the digitization job contained in the given document 'job'.
     *
     * @param job Document containing a digitization job
     * @return the state of the digitization job
     */
    public static String getJobState(Document job) {
        return getStringValue(job, JOB_STATE);
    }

    /**
     * Retrieve and return the message of the digitization job contained in the given document 'job'.
     * @param job Document containing the digitization job
     * @return the message of the digitization job
     */
    public static String getJobMessage(Document job) {
        return getStringValue(job, JOB_MESSAGE, false);
    }

    /**
     * Retrieve and return the title of the digitization job contained in the given document 'job'.
     *
     * @param job Document containing a digitization job
     * @return the title of the digitization job
     */
    public static String getJobTitel(Document job) {
        return getStringValue(job, JOB_TITLE);
    }

    public static int getJobSize(Document job) {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        try {
            XPathExpression expr = xpath.compile("count(" + JOB_SIZE + ")");
            Number result = (Number) expr.evaluate(job, XPathConstants.NUMBER);
            return result.intValue();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Convert given Document 'doc' to String and return it.
     *
     * @param doc the Document to be converted
     * @return the String content of the given Document
     */
    public static String convertDocumentToString(Document doc, boolean prettyPrint) {
        String xmlString = "";
        try {
            StringWriter writer = new StringWriter();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            if (prettyPrint) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            }

            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            xmlString = writer.toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return xmlString;
    }

    /**
     * Extract and return the text value of all elements in
     * the XML Document in the given String 'xmlString'.
     *
     * @param xmlString the XML from which the text value should be extracted
     * @return the extracted text value of all elements
     */
    public static String extractTextValue(String xmlString) {
        Document document = parseXML(xmlString);
        Element rootElement = document.getDocumentElement();
        return rootElement.getTextContent().trim();
    }

    /**
     * Validates the XML container in given String 'xmlString' against the
     * schema definition file 'Digitalisierungsauftrag.xsd'.
     * Throws an exception if validation fails.
     *
     * @param xmlString String containing the XML to be validated
     */
    public static void validateXMLAgainstSchema(String xmlString, String xsdPath) throws SAXException, IOException {
        URL xsdFile = new URL("file://" + xsdPath);

        SchemaFactory jobSchemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        jobSchemaFactory.setResourceResolver(new ResourceResolver(FilenameUtils.getPath(xsdPath)));
        Schema jobSchema = jobSchemaFactory.newSchema(xsdFile);

        Validator jobValidator = jobSchema.newValidator();
        jobValidator.validate(new StreamSource(new StringReader(xmlString)));
    }

    /**
     * Transform given XML string containing a Viaduc digitization job to the Kitodo internal XML format
     * and return it as a Document.
     *
     * @param xmlString Viaduc digitization job
     * @return job transformed into the Kitodo internal format as Document
     */
    public static Document transformXML(String xmlString, String xsltFile, boolean outputFilenameFromJobID) {

        SAXBuilder saxBuilder = new SAXBuilder();
        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        DOMOutputter outputter = new DOMOutputter();
        try (InputStream stylesheetInputStream = new FileInputStream(xsltFile)) {
            StreamSource transformSource = new StreamSource(stylesheetInputStream);
            TransformerFactory transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
            String transformedFilename = "transformed";
            if (outputFilenameFromJobID) {
                Document inputXMLDOM = parseXML(xmlString);
                transformedFilename = "digitizationJob" + getJobId(inputXMLDOM) + "_transformed";
            }
            File outputFile = File.createTempFile(transformedFilename, ".xml");
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            Transformer xslTransformer = transformerFactory.newTransformer(transformSource);
            TransformerHandler handler = ((SAXTransformerFactory) SAXTransformerFactory.newInstance()).newTransformerHandler();
            handler.setResult(new StreamResult(outputStream));
            Result saxResult = new SAXResult(handler);
            SAXSource saxSource = new SAXSource(new InputSource(new StringReader(xmlString)));
            xslTransformer.transform(saxSource, saxResult);
            return outputter.output(saxBuilder.build(outputFile));
        } catch (JDOMException | IOException | TransformerException e) {
            logger.error("Error transforming XML", e);
            return null;
        }
    }

    public static void saveXML(URI saveURI, String xmlContent) throws IOException {
        try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(ServiceManager.getFileService().write(saveURI)))) {
            output.write(xmlContent);
        }
    }

    public static String sanitizeString(String dirtyString) {
        return dirtyString.replaceAll("[^A-Za-z0-9]", "_");
    }

    public static void editPremisOriginalNameNode(URI premisURI, String name) {
        Document doc = loadXMLFile(premisURI.getPath().replace("file:", ""));
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node originalName;
        try {
            originalName = (Node) xPath.compile("/premis/object/originalName").evaluate(doc, XPathConstants.NODE);
            originalName.setTextContent(name + ".jp2");
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult sr = new StreamResult(new File(premisURI));
            transformer.transform(source, sr);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static Document createPremisDocument(String dossierSignature, String creator, Process process) throws ParserConfigurationException {

        if (Objects.isNull(documentBuilder)) {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }

        Document premisDocument = documentBuilder.newDocument();

        Element premisRoot = premisDocument.createElement("premis");
        premisRoot.setAttribute(VERSION, "3.0");
        premisRoot.setAttribute(XMLNS, PREMIS_NAMESPACE);
        premisRoot.setAttribute(XMLNS + ":" + XSI, XSI_NAMESPACE);
        premisRoot.setAttribute(XSI + ":" + SCHEMA_LOCATION, PREMIS_XSD_LOCATION);

        // Object element
        premisRoot.appendChild(createObjectElement(premisDocument, dossierSignature));

        // append 'agent' elements for Creator, Publisher, Contributor and Vecteur version
        premisRoot.appendChild(createAgentElement(premisDocument, CREATOR, creator, null));
        premisRoot.appendChild(createAgentElement(premisDocument, PUBLISHER, BAR_AGENT_NAME, null));
        premisRoot.appendChild(createAgentElement(premisDocument, CONTRIBUTOR, BAR_AGENT_NAME, null));
        premisRoot.appendChild(createAgentElement(premisDocument, StringUtils.capitalize(VERSION), VECTEUR_AGENT_NAME, KitodoVersion.getVersion()));

        // append 'rights' elements for 'copyright' and 'statute'
        premisRoot.appendChild(createRightsElement(premisDocument, GRANTOR_IDENTIFIER_VALUE, COPYRIGHT, COPYRIGHT_VOCABULARY_URL, GRANTOR, GRANTOR_VOCABULARY_URL));
        premisRoot.appendChild(createRightsElement(premisDocument, RIGHTSHOLDER_IDENTIFIER_VALUE, STATUTE, STATUTE_VOCABULARY_URL, RIGHTSHOLDER, RIGHTSHOLDER_VOCABULARY_URL));

        premisDocument.appendChild(premisRoot);

        return premisDocument;
    }

    public static Document addEventToPremis(Document premisDocument, Task task, Comment comment, boolean isCorrection,
                                            Date processingTime)
            throws PremisException {
        Element eventElement = null;
        if (Objects.isNull(task) && Objects.nonNull(comment)) {
            // add 'general comment' event to PREMIS file if no task is provided
            eventElement = createEventElement(premisDocument, null, comment, null, false, isCorrection, processingTime);
        } else if (TaskStatus.OPEN.equals(task.getProcessingStatus())) {
            if ((RECEIVE_DIGITIZATION_JOB.equals(task.getTitle()) || RECEIVE_DIGITIZATION_JOB_DE.equals(task.getTitle()))) {
                // exception for first task in workflow: this needs a "started" event, too, even though it's never "INWORK"!
                eventElement = createEventElement(premisDocument, task, null, true, false, isCorrection, processingTime);
            } else {
                // add 'unassign'/'hand over' event to premis!
                eventElement = createEventElement(premisDocument, task, null, false, true, isCorrection, processingTime);
            }
        } else if (TaskStatus.INWORK.equals(task.getProcessingStatus()) && Objects.isNull(comment)) {
            // add 'started' event when task is "INWORK"
            eventElement = createEventElement(premisDocument, task, null, true, false, isCorrection, processingTime);
        } else if (TaskStatus.DONE.equals(task.getProcessingStatus())) {
            // add 'finished without error' event when task is "DONE"
            eventElement = createEventElement(premisDocument, task, null, false, false, isCorrection, processingTime);
        } else if (TaskStatus.INWORK.equals(task.getProcessingStatus()) || TaskStatus.OPEN.equals(task.getProcessingStatus()) && Objects.nonNull(comment)) {
            // add 'finished with error' event when task is "OPEN" or "INWORK" and a correction comment is given
            eventElement = createEventElement(premisDocument, task, comment, false, false, isCorrection, processingTime);
        }
        return appendEventElement(premisDocument, eventElement);
    }

    private static Document appendEventElement(Document premisDocument, Element eventElement) throws PremisException {
        // insert 'eventElement' before first 'agentElement' (which makes it the _last_ 'eventElement' in the list of events!)
        if (Objects.isNull(eventElement)) {
            throw new PremisException("Event element for current task is null!");
        }
        NodeList agentNodes = premisDocument.getElementsByTagName(AGENT);
        if (agentNodes.getLength() < 1) {
            throw new PremisException("No agent elements found in PREMIS file!");
        }
        Element firstAgent = (Element)agentNodes.item(0);
        firstAgent.getParentNode().insertBefore(eventElement, firstAgent);
        return premisDocument;
    }

    private static Element createObjectElement(Document premisDocument, String signature) {
        Element objectElement = premisDocument.createElement(OBJECT);
        objectElement.setAttribute(XSI + ":" + TYPE, FILE);

        Element objectIdentifierElement = premisDocument.createElement(OBJECT_IDENTIFIER);
        Element objectIdentifierTypeElement = premisDocument.createElement(OBJECT_IDENTIFIER_TYPE);
        objectIdentifierTypeElement.setAttribute(VALUE_URI, LOCAL_VOCABULARY_URL);
        objectIdentifierTypeElement.appendChild(createTextNode(premisDocument, LOCAL));

        Element objectIdentifierValueElement = premisDocument.createElement(OBJECT_IDENTIFIER_VALUE);
        objectIdentifierValueElement.appendChild(createTextNode(premisDocument, signature));

        objectIdentifierElement.appendChild(objectIdentifierTypeElement);
        objectIdentifierElement.appendChild(objectIdentifierValueElement);
        objectElement.appendChild(objectIdentifierElement);

        Element objectCharacteristicsElement = premisDocument.createElement(OBJECT_CHARACTERISTICS);
        Element formatElement = premisDocument.createElement(FORMAT);
        Element formatDesignationElement = premisDocument.createElement(FORMAT_DESIGNATION);
        Element formatNameElement = premisDocument.createElement(FORMAT_NAME);
        formatNameElement.appendChild(createTextNode(premisDocument, XML));

        formatDesignationElement.appendChild(formatNameElement);
        formatElement.appendChild(formatDesignationElement);
        objectCharacteristicsElement.appendChild(formatElement);
        objectElement.appendChild(objectCharacteristicsElement);

        return objectElement;
    }

    private static Element createEventElement(Document document, Task task, Comment comment, Boolean start,
                                              Boolean unassign, boolean isCorrection, Date processingTime) {
        Element eventElement = document.createElement(EVENT);
        Element eventIdentifierElement = document.createElement(EVENT_IDENTIFIER);
        Element eventIdentifierTypeElement = document.createElement(EVENT_IDENTIFIER_TYPE);
        eventIdentifierTypeElement.setAttribute(VALUE_URI, LOCAL_VOCABULARY_URL);
        eventIdentifierTypeElement.appendChild(createTextNode(document, LOCAL));
        Element eventIdentifierValueElement = document.createElement(EVENT_IDENTIFIER_VALUE);
        if (Objects.nonNull(task)) {
            eventIdentifierValueElement.appendChild(createTextNode(document, task.getTitle()));
        } else {
            eventIdentifierValueElement.appendChild(createTextNode(document, COMMENT));
        }
        eventIdentifierElement.appendChild(eventIdentifierTypeElement);
        eventIdentifierElement.appendChild(eventIdentifierValueElement);
        eventElement.appendChild(eventIdentifierElement);

        Element eventTypeElement = document.createElement(EVENT_TYPE);
        eventTypeElement.setAttribute(VALUE_URI, TRANSFER_VOCABULARY_URL);
        eventTypeElement.appendChild(createTextNode(document, TRANSFER));
        eventElement.appendChild(eventTypeElement);

        // add task date time element
        eventElement.appendChild(createEventDateTimeElement(document, start, comment, task, unassign));

        // add user information
        eventElement.appendChild(createEventDetailInformationElement(document, comment, task, unassign));

        // add outcome information
        eventElement.appendChild(createEventOutcomeInformationElement(document, comment, start, task, unassign));

        // add information about whether task was processes during correction workflow or not
        eventElement.appendChild(createCorrectionWorkflowElement(document, isCorrection));

        // add information about number of scanned images if this is a "closing" event (unassigned/finished) and the task is a scanning task!
        if (Objects.nonNull(task) && task.isTypeImagesWrite() && Boolean.FALSE.equals(start)) {
            if (Objects.nonNull(processingTime)) {
                eventElement.appendChild(createNumberOfScannedImagesElement(document, task, processingTime));
            } else {
                logger.error("'ProcessingTime' is null for task '" + task.getTitle() + "(ID: " + task.getId() + ") -> Unable to determine number of scanned images for PREMIS event!");
            }
        }

        Element linkingAgentIdentifierElement = createLinkingAgentIdentifierElement(document,
                LOCAL_VOCABULARY_URL, LOCAL, VECTEUR + "-" + KitodoVersion.getVersion(), null, null);

        eventElement.appendChild(linkingAgentIdentifierElement);

        return eventElement;
    }

    private static Element createAgentElement(Document document,
                                              String identifierValue,
                                              String nameValue,
                                              String versionValue) {
        Element agentElement = document.createElement(AGENT);
        Element agentIdentifierElement = document.createElement(AGENT_IDENTIFIER);

        Element agentIdentifierTypeElement = createElement(document, AGENT_IDENTIFIER_TYPE, VALUE_URI, LOCAL_VOCABULARY_URL, LOCAL);

        Element agentIdentifierValueElement;
        if (StringUtils.capitalize(VERSION).equals(identifierValue)) {
            agentIdentifierValueElement = createElement(document, AGENT_IDENTIFIER_VALUE, null, null, VECTEUR + "-" + versionValue);
        } else {
            agentIdentifierValueElement = createElement(document, AGENT_IDENTIFIER_VALUE, null, null, identifierValue);
        }

        agentIdentifierElement.appendChild(agentIdentifierTypeElement);
        agentIdentifierElement.appendChild(agentIdentifierValueElement);

        Element agentNameElement = createElement(document, AGENT_NAME, null, null, nameValue);
        Element agentTypeElement;
        Element agentVersionElement = null;

        if (Objects.nonNull(identifierValue)
                && !identifierValue.isEmpty()
                && agentIdentifierTypeMapping.containsKey(identifierValue)) {

            String agentTypeText = agentIdentifierTypeMapping.get(identifierValue);
            String agentTypeValueURI = agentTypeURIMapping.get(agentTypeText);
            agentTypeElement = createElement(document, AGENT_TYPE, VALUE_URI, agentTypeValueURI, agentTypeText);

            if (StringUtils.capitalize(VERSION).equals(identifierValue) && Objects.nonNull(versionValue) && !versionValue.isEmpty()) {
                agentVersionElement = createElement(document, AGENT_VERSION, null, null, versionValue);
            }
        } else {
            throw new IllegalArgumentException("Missing agent identifier value!");
        }

        agentElement.appendChild(agentIdentifierElement);
        agentElement.appendChild(agentNameElement);
        agentElement.appendChild(agentTypeElement);
        if (Objects.nonNull(agentVersionElement)) {
            agentElement.appendChild(agentVersionElement);
        }

        return agentElement;
    }

    private static Element createRightsElement(Document document,
                                               String linkingAgentIdentifierValue,
                                               String rightsBasisValue,
                                               String rightsBasisValueURI,
                                               String linkingAgentRole,
                                               String linkingAgentRoleURI) {
        Element rightsElement = document.createElement(RIGHTS);
        Element rightsStatementElement = document.createElement(RIGHTS_STATEMENT);

        // "rightsStatement" element with children
        Element rightsStatementIdentifierElement = document.createElement(RIGHTS_STATEMENT_IDENTIFIER);
        Element rightsStatementIdentifierTypeElement = createElement(document, RIGHTS_STATEMENT_IDENTIFIER_TYPE, VALUE_URI, LOCAL_VOCABULARY_URL, LOCAL);
        Element rightsStatementIdentifierValueElement = createElement(document, RIGHTS_STATEMENT_IDENTIFIER_VALUE, null, null, rightsBasisValue);

        rightsStatementIdentifierElement.appendChild(rightsStatementIdentifierTypeElement);
        rightsStatementIdentifierElement.appendChild(rightsStatementIdentifierValueElement);

        // "rightsBasis" element
        Element rightsBasisElement = createElement(document, RIGHTS_BASIS, VALUE_URI, rightsBasisValueURI, rightsBasisValue);

        // "linkingAgentIdentifier" element with children
        Element linkingAgentIdentifierElement = createLinkingAgentIdentifierElement(document,
                VIAF_VOCABULARY_URL, VIAF, linkingAgentIdentifierValue, linkingAgentRoleURI, linkingAgentRole);

        rightsStatementElement.appendChild(rightsStatementIdentifierElement);
        rightsStatementElement.appendChild(rightsBasisElement);
        rightsStatementElement.appendChild(linkingAgentIdentifierElement);

        rightsElement.appendChild(rightsStatementElement);

        return rightsElement;
    }

    private static Element createLinkingAgentIdentifierElement(Document document,
                                                               String typeURI,
                                                               String type,
                                                               String value,
                                                               String roleURI,
                                                               String role) {
        Element linkingAgentIdentifierElement = document.createElement(LINKING_AGENT_IDENTIFIER);
        Element linkingAgentIdentifierTypeElement = createElement(document, LINKING_AGENT_IDENTIFIER_TYPE, VALUE_URI, typeURI, type);
        Element linkingAgentIdentifierValueElement = createElement(document, LINKING_AGENT_IDENTIFIER_VALUE, null, null, value);

        linkingAgentIdentifierElement.appendChild(linkingAgentIdentifierTypeElement);
        linkingAgentIdentifierElement.appendChild(linkingAgentIdentifierValueElement);

        if (Objects.nonNull(roleURI) && !roleURI.isEmpty() && Objects.nonNull(role) && !role.isEmpty()) {
            Element linkingAgentRoleElement = createElement(document, LINKING_AGENT_ROLE, VALUE_URI, roleURI, role);
            linkingAgentIdentifierElement.appendChild(linkingAgentRoleElement);
        }

        return linkingAgentIdentifierElement;
    }

    private static Element createEventDetailInformationElement(Document document, Comment comment, Task task,
                                                               Boolean handover) {
        Element eventDetailInformationElement = document.createElement(EVENT_DETAIL_INFORMATION);
        Element eventDetailElement = document.createElement(EVENT_DETAIL);
        User processingUser;
        // use session user for handover tasks because former user has already been removed from task at this point
        if (Boolean.TRUE.equals(handover)) {
            processingUser = ServiceManager.getUserService().getCurrentUser();
        } else if (Objects.nonNull(task)) {
            processingUser = task.getProcessingUser();
        } else {
            processingUser = comment.getAuthor();
        }
        if (Objects.nonNull(processingUser)) {
            eventDetailElement.appendChild(createTextNode(document, "manual process, UserID: " + processingUser.getId()));
        } else {
            eventDetailElement.appendChild(createTextNode(document,"automatic process, UserID: 0"));
        }

        eventDetailInformationElement.appendChild(eventDetailElement);
        return eventDetailInformationElement;
    }

    private static Element createEventOutcomeInformationElement(Document document, Comment comment, Boolean start,
                                                                Task task, Boolean unassign) {
        Element eventOutcomeInformationElement = document.createElement(EVENT_OUTCOME_INFORMATION);
        Element eventOutcomeElement = document.createElement(EVENT_OUTCOME);

        if (Objects.nonNull(start)) {
            if (start) {
                eventOutcomeElement.appendChild(createTextNode(document, "Started without errors"));
            } else if (unassign) {
                eventOutcomeElement.appendChild(createTextNode(document, "Hand over from current user"));
            } else {
                String taskFinishedMessage = "Finished without errors";
                if (Objects.nonNull(comment) && Objects.nonNull(comment.getCurrentTask())
                        && Objects.nonNull(task) && comment.getCurrentTask().getId().equals(task.getId())) {
                    taskFinishedMessage = "Finished with error; comment: " + comment.getMessage();
                }
                eventOutcomeElement.appendChild(createTextNode(document, taskFinishedMessage));
            }
        } else {
            eventOutcomeElement.appendChild(createTextNode(document, comment.getMessage()));
        }

        eventOutcomeInformationElement.appendChild(eventOutcomeElement);
        return eventOutcomeInformationElement;
    }

    private static Element createCorrectionWorkflowElement(Document document, boolean isCorrection) {
        Element eventCorrectionWorkflowElement = document.createElement(CORRECTION_WORKFLOW);
        eventCorrectionWorkflowElement.appendChild(createTextNode(document, String.valueOf(isCorrection)));
        return eventCorrectionWorkflowElement;
    }

    private static Element createNumberOfScannedImagesElement(Document document, Task task, Date processingTime) {
        Element numberOfScannedImagesElement = document.createElement(NUMBER_OF_SCANNED_PAGES);
        long numberOfScannedImages = -1;
        try {
             numberOfScannedImages = PremisHelper.calculateNumberOfScansForCurrentTaskRun(task, processingTime);
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        numberOfScannedImagesElement.appendChild(createTextNode(document, String.valueOf(numberOfScannedImages)));
        return numberOfScannedImagesElement;
    }


    private static Element createEventDateTimeElement(Document document, Boolean start, Comment comment, Task task,
                                                      Boolean unassign) {
        Element eventDateTimeElement = document.createElement(EVENT_DATE_TIME);
        String timeStamp = NOT_AVAILABLE;
        if (Objects.nonNull(start) && Objects.nonNull(task)) {
            // only use timestamps of successful tasks (e.g. without error comment)!
            if (Objects.isNull(comment)) {
                if (start || Boolean.TRUE.equals(unassign)) {
                    // Using "ProcessingTime" because it is updated during correction workflows when a task is started a second time!
                    if (Objects.nonNull(task.getProcessingTime())) {
                        timeStamp = formatLocalDateTime(task.getProcessingTime());
                    }
                } else {
                    if (Objects.nonNull(task.getProcessingEnd())) {
                        timeStamp = formatLocalDateTime(task.getProcessingEnd());
                    }
                }
            } else {
                // use creation date of correction comment if present (e.g. "task" and "comment" are _not_ "null")
                timeStamp = formatLocalDateTime(comment.getCreationDate());
            }
        } else {
            // use creation date of normal comments not associated with a task (e.g. "task" is "null"!)
            timeStamp = formatLocalDateTime(comment.getCreationDate());
        }
        eventDateTimeElement.appendChild(createTextNode(document, timeStamp));
        return eventDateTimeElement;
    }

    private static Element createElement(Document doc,
                                         String tagName,
                                         String attributeName,
                                         String attributeValue,
                                         String textContent) {
        Element element = doc.createElement(tagName);
        if (Objects.nonNull(attributeName) && !attributeName.isEmpty() && Objects.nonNull(attributeValue) && !attributeValue.isEmpty()) {
            element.setAttribute(attributeName, attributeValue);
        }
        if (Objects.nonNull(textContent) && !textContent.isEmpty()) {
            element.appendChild(createTextNode(doc, textContent));
        }
        return element;
    }

    private static Text createTextNode(Document doc, String text) {
        if (Objects.isNull(text)) {
            return doc.createTextNode("");
        } else {
            return doc.createTextNode(text);
        }
    }

    public static boolean checkDoc(Node n) {
        if (n instanceof Text) {
            if (((Text) n).getData() == null) {
                System.err.println("null data!!!!");
                return false;
            }
        }

        NodeList l = n.getChildNodes();
        for (int i = 0; i < l.getLength(); ++i) {
            checkDoc(l.item(i));
        }
        return true;
    }

    /**
     * Helper function to add event elements to processes that were currently in work when the PREMIS creation
     * was updated. This function adds event elements for all tasks that have been DONE or are currently INWORK via the
     * legacy functionality of PREMIS creation. All further task events will be added using the new PREMIS creation
     * functionality when tasks are opened and closed.
     *
     * @param process Process for which PREMIS event elements are added
     * @param premisDocument PREMIS document
     * @throws PremisException thrown if event element could not be added to PREMIS document
     */
    public static void addEventsForExistingProcess(Process process, Document premisDocument) throws PremisException {
        logger.info("PREMIS legacy method 'addEventsForExistingProcess' called for process " + process.getId() + "...");
        for (Task task : process.getTasks()) {

            // skip concurrent/parallel tasks that weren't activated (because there condition wasn't met, for example)
            if (task.isConcurrent() && Objects.isNull(task.getProcessingBegin())) {
                continue;
            }

            // add events only for DONE or INWORK tasks _before_ SIP creation
            if (CREATE_SIP.equals(task.getTitle()) || CREATE_SIP_DE.equals(task.getTitle()) || TaskStatus.OPEN.equals(task.getProcessingStatus())
                    || TaskStatus.LOCKED.equals(task.getProcessingStatus())) {
                break;
            }

            // create one event for each correction comment before creating the 'task successfully completed' event!
            for (Comment comment : task.getProcess().getComments()) {
                if (CommentType.ERROR.equals(comment.getType()) && comment.getCurrentTask().getId().equals(task.getId())) {
                    // add "Started without errors" event
                    appendEventElement(premisDocument, createEventElement(premisDocument, task, comment, true, false, task.isCorrection(), null));
                    if (TaskStatus.DONE.equals(task.getProcessingStatus())) {
                        // add "Finished with errors" event
                        appendEventElement(premisDocument, createEventElement(premisDocument, task, comment, false, false, task.isCorrection(), null));
                    }
                }
            }

            // add "Started without errors" event
            appendEventElement(premisDocument, createEventElement(premisDocument, task, null, true, false, task.isCorrection(), null));
            if (TaskStatus.DONE.equals(task.getProcessingStatus())) {
                // add "Finished without errors" event
                appendEventElement(premisDocument, createEventElement(premisDocument, task, null, false, false, task.isCorrection(), null));
            }
        }

        // add events for general comments
        for (Comment comment : process.getComments()) {
            if (CommentType.INFO.equals(comment.getType())) {
                boolean correction = false;
                List<Task> currentTasks = ServiceManager.getProcessService().getCurrentTasks(comment.getProcess());
                if (!currentTasks.isEmpty()) {
                    correction = currentTasks.stream().anyMatch(Task::isCorrection);
                }
                addEventToPremis(premisDocument, null, comment, correction, null);
            }
        }
    }

    public static List<HashMap<String, String>> getPremisTaskStartedEvents(Document premisDocument, Task task)
            throws PremisException {
        return getPremisTaskEvents(premisDocument, task, STARTED);
    }

    public static List<HashMap<String, String>> getPremisTaskFinishedEvents(Document premisDocument, Task task)
            throws PremisException {
        return getPremisTaskEvents(premisDocument, task, FINISHED);
    }

    public static List<HashMap<String, String>> getPremisTaskHandoverEvents(Document premisDocument, Task task)
            throws PremisException {
        return getPremisTaskEvents(premisDocument, task, HAND_OVER);
    }

    private static List<HashMap<String, String>> getPremisTaskEvents(Document premisDocument, Task task, String type)
            throws PremisException {
        List<HashMap<String, String>> taskEvents = new LinkedList<>();
        NodeList eventNodes = premisDocument.getElementsByTagName(EVENT);

        for (int i = 0; i < eventNodes.getLength(); i++) {
            Element eventElement = (Element)eventNodes.item(i);
            if (getEventOutcome(eventElement).startsWith(type)) {
                NodeList eventIds = eventElement.getElementsByTagName(EVENT_IDENTIFIER_VALUE);
                for (int j = 0; j < eventIds.getLength(); j++) {
                    Element eventId = (Element)eventIds.item(j);
                    String taskTitle = eventId.getTextContent().trim();
                    if (Objects.equals(taskTitle, task.getTitle())) {
                        taskEvents.add(readTaskEvent(eventElement, taskTitle));
                    }
                }
            }
        }

        return taskEvents;
    }

    private static HashMap<String, String> readTaskEvent(Element eventElement, String eventIdentifier)
            throws PremisException {
        HashMap<String, String> taskEvent = new HashMap<>();
        taskEvent.put(USER, getEventDetail(eventElement));
        taskEvent.put(TIMESTAMP, getEventDateTime(eventElement));
        taskEvent.put(TASK, eventIdentifier);
        taskEvent.put(CORRECTION_COMMENT, getCorrectionComment(eventElement));
        taskEvent.put(TASK_OUTCOME_TYPE, getEventOutcome(eventElement));
        taskEvent.put(CORRECTION_WORKFLOW, getCorrectionWorkflow(eventElement));
        String numberOfImages = getNumberOfScannedImages(eventElement);
        if (StringUtils.isNotBlank(numberOfImages)) {
            taskEvent.put(NUMBER_OF_SCANNED_PAGES, numberOfImages);
        }
        return taskEvent;
    }

    private static String getEventDetail(Element eventElement) throws PremisException {
        return getElementValue(eventElement, EVENT_DETAIL, true);
    }

    private static String getEventDateTime(Element eventElement) throws PremisException {
        return getElementValue(eventElement, EVENT_DATE_TIME, true);
    }

    private static String getEventOutcome(Element eventElement) throws PremisException {
        return getElementValue(eventElement, EVENT_OUTCOME, true);
    }

    private static String getCorrectionWorkflow(Element eventElement) throws PremisException {
        return getElementValue(eventElement, CORRECTION_WORKFLOW, true);
    }

    private static String getNumberOfScannedImages(Element eventElement) throws PremisException {
        return getElementValue(eventElement, NUMBER_OF_SCANNED_PAGES, false);
    }

    private static String getCorrectionComment(Element eventElement) throws PremisException {
        String eventOutcome = getEventOutcome(eventElement);
        if (eventOutcome.startsWith(FINISHED_WITH_ERRORS_STRING)) {
            return eventOutcome.replace(FINISHED_WITH_ERRORS_STRING, "");
        } else {
            return NA;
        }
    }

    private static String getElementValue(Element eventElement, String tagName, boolean mandatory) throws PremisException {
        NodeList elementNodes = eventElement.getElementsByTagName(tagName);
        if (elementNodes.getLength() < 1) {
            if (mandatory) {
                throw new PremisException(String.format("Missing mandatory PREMIS event element '%s'!", tagName));
            } else {
                return null;
            }
        } else {
            Element element = (Element)elementNodes.item(0);
            return element.getTextContent();
        }
    }

    private static String getUserName(String premisDetail) {
        Matcher idMatcher = PREMIS_USER_PATTERN.matcher(premisDetail);
        if (idMatcher.find()) {
            String userIDString = idMatcher.group(1);
            try {
                int userId = Integer.parseInt(userIDString);
                if (userId == 0) {
                    return SYSTEM;
                } else {
                    User user = ServiceManager.getUserService().getById(userId);
                    return user.getFullName();
                }
            } catch (DAOException e) {
                logger.error(e.getMessage());
                return NA;
            }
        }
        logger.error("Error: unable to extract user ID from PREMIS user entry '" + premisDetail + "'!");
        return NA;
    }

    public static List<HashMap<String, Object>> mapToEventPairs(Document premisDocument, Task task)
            throws PremisException {
        List<HashMap<String, String>> startEvents = getPremisTaskStartedEvents(premisDocument, task);
        List<HashMap<String, String>> closingEvents = getPremisTaskFinishedEvents(premisDocument, task);
        closingEvents.addAll(getPremisTaskHandoverEvents(premisDocument, task));
        List<HashMap<String, Object>> taskEvents = new LinkedList<>();
        // iterate over startEvents and find matching finished or hand over event
        for (HashMap<String, String> startEvent : startEvents) {
            LocalDateTime startEventLocalDateTime = parseTimestamp(startEvent.get(TIMESTAMP));
            HashMap<String, Object> taskEvent = new HashMap<>();
            taskEvent.put(USER, getUserName(startEvent.get(USER)));
            taskEvent.put(STARTED, Date.from(startEventLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()));
            // "CORRECTION_WORKFLOW" should always have the same value in matching start and end events!
            taskEvent.put(CORRECTION_WORKFLOW, Boolean.valueOf(startEvent.get(CORRECTION_WORKFLOW)));
            HashMap<String, String> matchingClosingEvent = getMatchingEvent(startEvent, closingEvents,
                    startEventLocalDateTime);
            if (Objects.nonNull(matchingClosingEvent)) {
                String taskOutcome = matchingClosingEvent.get(TASK_OUTCOME_TYPE);
                if (Objects.nonNull(taskOutcome)) {
                    String numberOfImages = matchingClosingEvent.get(NUMBER_OF_SCANNED_PAGES);
                    if (StringUtils.isNotBlank(numberOfImages)) {
                        taskEvent.put(NUMBER_OF_SCANNED_PAGES, numberOfImages);
                    }
                    if (taskOutcome.startsWith(FINISHED)) {
                        taskEvent.put(FINISHED, Date.from(parseTimestamp(matchingClosingEvent.get(TIMESTAMP)).atZone(ZoneId.systemDefault()).toInstant()));
                        taskEvent.put(CORRECTION_COMMENT, matchingClosingEvent.getOrDefault(CORRECTION_COMMENT, null));
                    } else if (taskOutcome.startsWith(HAND_OVER)) {
                        taskEvent.put(HAND_OVER, Date.from(parseTimestamp(matchingClosingEvent.get(TIMESTAMP)).atZone(ZoneId.systemDefault()).toInstant()));
                    }
                }
            } else {
                taskEvent.put(FINISHED, null);
                taskEvent.put(CORRECTION_COMMENT, null);
                logger.error("ERROR: no closing event found for PREMIS starting event!");
                Thread.dumpStack();
            }
            taskEvents.add(taskEvent);
        }

        return taskEvents.stream()
                .sorted(Comparator.comparing(e -> ((Date) e.get(STARTED))))
                .collect(Collectors.toList());
    }

    private static HashMap<String, String> getMatchingEvent(HashMap<String, String> startEvent,
                                                            List<HashMap<String, String>> matchCandidates,
                                                            LocalDateTime startEventLocalDateTime) {
        HashMap<String, String> matchingEvent = null;
        for (HashMap<String, String> eventCandidate : matchCandidates) {
            if (startEvent.get(TASK).equals(eventCandidate.get(TASK))
                    && startEvent.get(USER).equals(eventCandidate.get(USER))) {
                // compare timestamps of finished elements and choose event closest after started event
                LocalDateTime finishedEventLocalDateTime = parseTimestamp(eventCandidate.get(TIMESTAMP));
                if (Objects.isNull(matchingEvent)) {
                    if (startEventLocalDateTime.isBefore(finishedEventLocalDateTime)
                            || startEventLocalDateTime.isEqual(finishedEventLocalDateTime)) {
                        matchingEvent = eventCandidate;
                    }
                } else {
                    LocalDateTime currentMatchLocalDateTime = parseTimestamp(matchingEvent.get(TIMESTAMP));
                    if ((startEventLocalDateTime.isBefore(finishedEventLocalDateTime)
                            || startEventLocalDateTime.isEqual(finishedEventLocalDateTime))
                            && finishedEventLocalDateTime.isBefore(currentMatchLocalDateTime)) {
                        matchingEvent = eventCandidate;
                    }
                }
            }
        }
        return matchingEvent;
    }

    private static LocalDateTime parseTimestamp(String timestamp) {
        try {
            return LocalDateTime.from(TIMESTAMP_FORMATTER_A.parse(timestamp));
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.from(TIMESTAMP_FORMATTER_B.parse(timestamp));
            } catch (DateTimeParseException e2) {
                return LocalDateTime.from(TIMESTAMP_FORMATTER_C.parse(timestamp));
            }
        }
    }

    public static String formatLocalDateTime(Date date) {
        return TIMESTAMP_FORMATTER_A.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    public static Document getMetadataDocument(Process process) {
        URI rootPath = Paths.get(KitodoConfig.getKitodoDataDirectory()).toUri();
        URI uri = ServiceManager.getFileService().getProcessBaseUriForExistingProcess(process);
        URI processUri = rootPath.resolve(uri);
        return XMLHelper.loadXMLFile(processUri.getPath() + "/meta.xml");
    }
}
