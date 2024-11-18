package org.kitodo.production.services.relayserver.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.KitodoConfig;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.services.relayserver.RelayServerServiceConfig;
import org.kitodo.production.services.relayserver.helper.JobHelper;
import org.kitodo.production.services.relayserver.helper.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SIPHandler {

    private static Properties kitodoProperties = new Properties();
    private static final String KITODO_PROPERTIES_FILE = RelayServerServiceConfig.getKitodoPropertiesFile();
    private static final String CONTENT_DIR_XPATH = "/paket/inhaltsverzeichnis/ordner[name='content']/ordner/name/text()";
    private static final String DELIVERING_PLACE = "Vecteur";
    private static final String IMAGES_EXPORT_DIR = "images_export";
    private static final String PREMIS_DIR = "images_export";
    private static final String PROCESS_PREMIS_FILE = "Prozess_Digitalisierung_PREMIS.xml";
    private static final String VIADUC_ID = "AuftragsId";
    private static final String VIADUC_TASK_CREATE_SIP = "SIP erstellen";
    private static final FileService fileService = ServiceManager.getFileService();
    private static final Logger logger = LogManager.getLogger(SIPHandler.class);
    private static final XPath xPath = XPathFactory.newInstance().newXPath();
    private Process process;
    private URI rootPath;
    private URI processUri;

    private static final Map<String, String> fileExportMapping = new HashMap<>();
    private static String premisFileExtension;

    /**
     * Default constructor.
     *
     * @param process passes the process to work with
     */
    public SIPHandler(Process process) {
        this.process = process;
        rootPath = Paths.get(KitodoConfig.getKitodoDataDirectory()).toUri();
        URI uri = ServiceManager.getFileService().getProcessBaseUriForExistingProcess(process);
        this.processUri = rootPath.resolve(uri + "/");
        loadProperties();
    }

    /**
     * Load configuration of the Vecteur/Kitodo REST API from properties file.
     */
    private void loadProperties() {
        try (FileInputStream kitodoConfiguration = new FileInputStream(KITODO_PROPERTIES_FILE)) {
            kitodoProperties.load(kitodoConfiguration);
            String mapping = kitodoProperties.getProperty("file_export_mapping");
            Arrays.stream(mapping.split(" *, *"))
                    .map(keyValue -> keyValue.split(" *: *", 2))
                    .forEach(pairs -> fileExportMapping.put(pairs[0], pairs.length == 1 ? "" : pairs[1]));
            premisFileExtension = kitodoProperties.getProperty("premis_file_extension");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates the SIP.
     * This function calls other functions to create the metadata.xml and copy the files to the SIP directory.
     */
    public boolean generateSIP() {
        SIPMetadata sipMetadata = new SIPMetadata(process);

        try {
            // Create metadata.xml
            Document metadata = sipMetadata.createSIPMetadata();

            // Remove old SIP directories
            removeSIPDirectories();

            // Create SIP itself
            String sipName = createSIPDirectory(metadata);

            // Compress SIP
            compressSip(sipName);
        } catch (IOException | SAXException | IllegalStateException e) {
            logger.error(VIADUC_TASK_CREATE_SIP + " for process ID " + process.getId() + " failed. Error creating 'metadata.xml' document: " + e.getLocalizedMessage());
            e.printStackTrace();
            Process updatedProcess = JobHelper.jumpToFallbackTask(process, e.getLocalizedMessage(), VIADUC_TASK_CREATE_SIP);
            if (Objects.nonNull(updatedProcess)) {
                process = updatedProcess;
            }
            return false;
        }

        // write mapping between digitization job and sip
        sipMetadata.createImportExportMapping();

        return true;
    }

    /**
     * Removes all existing SIP directories and zip files to make sure only the new SIP exists.
     */
    private void removeSIPDirectories() {
        try (Stream<Path> pathStream = Files.list(Path.of(processUri.getPath()))) {
            List<Path> pathsToBeDeleted = pathStream
                    .filter(path -> path.getFileName().toString().startsWith("SIP_"))
                    .collect(Collectors.toList());
            for (Path pathToBeDeleted : pathsToBeDeleted) {
                try {
                    fileService.delete(pathToBeDeleted.toUri());
                } catch (IOException e) {
                    logger.error("Could not delete old SIP files with path '" + pathToBeDeleted + "': " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Could not list old SIP files: " + e.getMessage());
        }
    }

    /**
     * Generates the SIP file.
     *
     * @param sipMetadata Document containing the metadata.xml of the SIP
     * @return String containing the name of the SIP directory
     */
    private String createSIPDirectory(Document sipMetadata) {
        String viaducID = "";
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String creationDate = format.format(date);

        for (Property property : this.process.getProperties()) {
            if (property.getTitle().equals(VIADUC_ID)) {
                viaducID = property.getValue();
                break;
            }
        }

        String sipName = "SIP_" + creationDate + "_" + DELIVERING_PLACE + "_" + viaducID;
        try {
            //Create SIP directory
            URI sipDir = fileService.createDirectory(processUri, sipName);
            //create header directory
            createHeaderDIR(sipMetadata, sipDir);
            //create content directory
            createContentDIR(sipMetadata, sipDir);
        } catch (IOException e) {
            logger.error("Error: SIP Directory was not created " + e.getMessage());
        }
        return sipName;
    }

    /**
     * Generates the header directory and its subdirectories.
     *
     * @param sipMetadata Document containing the metadata.xml for the SIP
     * @param sipDir      URI to the root directory of the SIP
     */
    private void createHeaderDIR(Document sipMetadata, URI sipDir) {
        try {
            //create header
            URI header = fileService.createDirectory(sipDir, "header");

            // copy metadata.xml to header directory
            URI pathToMetadata = processUri.resolve("metadata.xml");
            fileService.copyFileToDirectory(pathToMetadata, header);

            //create xsd Dir
            URI xsd = fileService.createDirectory(header, "xsd");

            // copy xsd files to xsd directory
            // FIXME: loading the xml file works, but using the passed one does not.
            sipMetadata = XMLHelper.loadXMLFile(processUri.getPath() + "metadata.xml");
            String pathToXsdFiles = "/paket/inhaltsverzeichnis/ordner[name='header']/ordner[name='xsd']/datei/name";
            NodeList xsdFileList = (NodeList) xPath.compile(pathToXsdFiles).evaluate(sipMetadata, XPathConstants.NODESET);

            for (int i = 0; i < xsdFileList.getLength(); i++) {
                Node fileNode = xsdFileList.item(i);
                String fileName = fileNode.getTextContent();
                String filePath = RelayServerServiceConfig.class.getResource("/configuration").getPath() + File.separator + "xsd" + File.separator + fileName;
                File file = new File(filePath);
                URI targetPath = rootPath.resolve(xsd).resolve(file.getName());

                // FIXME fileService provides methods for copying, but they do not work here throwing FileNotFoundException.
                //fileService.copyFileToDirectory(fileUri, xsd);
                Files.copy(file.toPath(), new File(targetPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }


        } catch (IOException | XPathExpressionException e) {
            logger.error("Error: header or xsd directory was not created " + e.getMessage());
        }
    }

    /**
     * Generates the content directory and its subdirectories.
     *
     * @param sipMetadata Document containing the metadata.xml for the SIP
     * @param sipDir      URI to the root directoy for the SIP
     */
    private void createContentDIR(Document sipMetadata, URI sipDir) {
        try {
            URI content = fileService.createDirectory(sipDir, "content");
            // FIXME: loading the xml file works, but using the passed one does not.
            sipMetadata = XMLHelper.loadXMLFile(processUri.getPath() + "metadata.xml");
            try {
                NodeList contentSubdirList = (NodeList) xPath.compile(CONTENT_DIR_XPATH).evaluate(sipMetadata, XPathConstants.NODESET);
                for (int i = 0; i < contentSubdirList.getLength(); i++) {

                    URI dossier = fileService.createDirectory(content, contentSubdirList.item(i).getNodeValue());
                    String datei = "/paket/inhaltsverzeichnis/ordner[name='content']/ordner[name='" + contentSubdirList.item(i).getNodeValue() + "']/datei/name/text()";
                    NodeList fileNodeList = (NodeList) xPath.compile(datei).evaluate(sipMetadata, XPathConstants.NODESET);

                    for (int j = 0; j < fileNodeList.getLength(); j++) {
                        if (fileExportMapping.keySet().parallelStream().anyMatch(fileNodeList.item(j).getNodeValue()::endsWith)) {
                            // media
                            URI source = URI.create(processUri + File.separator + IMAGES_EXPORT_DIR + File.separator + fileNodeList.item(j).getNodeValue());
                            fileService.copyFileToDirectory(source, dossier);
                        } else if (fileNodeList.item(j).getNodeValue().endsWith(premisFileExtension)) {
                            // premis
                            if ((fileNodeList.item(j).getNodeValue().contains(PROCESS_PREMIS_FILE))) {
                                URI source = URI.create(processUri + File.separator + fileNodeList.item(j).getNodeValue());
                                fileService.copyFileToDirectory(source, dossier);
                            } else {
                                URI source = URI.create(processUri + File.separator + PREMIS_DIR + File.separator + fileNodeList.item(j).getNodeValue());
                                fileService.copyFileToDirectory(source, dossier);
                            }
                        } else {
                            logger.error("File not Found " + fileNodeList.item(j).getNodeValue());
                        }
                    }
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            logger.error("Error : Content directory not created " + e.getMessage());
        }
    }

    /**
     * Compresses the generated SIP directory as zip.
     *
     * @param sipName String containing the name of the SIP directory
     */
    private void compressSip(String sipName) { // TODO check if this method works
        URI sourceDirectory = processUri.resolve(sipName);
        String targetFilePath = processUri.resolve(sipName).getPath();

        ZIPHelper zipHelper = new ZIPHelper(sourceDirectory.getPath());
        zipHelper.generateFileList(new File(sourceDirectory));
        zipHelper.compressDirectory(targetFilePath + ".zip");

        logger.info("SIP successfully compressed.");
    }

    // TODO check this function - might contain unnecessary lines
    /**
     * Returns the generated SIP file.
     * @return compressed SIP file
     */
    File getSIP() {
        File sipFile = null;
        File root = new File(processUri);
        try {
            Collection files = FileUtils.listFiles(root, new String[]{"zip"}, false);
            for (Object currentFile : files) {
                File file = (File) currentFile;
                if (file.getName().startsWith("SIP_")) {
                    // TODO do we need this change of delivery date?
                    //change date to the delivery day: YYYYMMDD
                    //Date date = new Date();
                    //SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                    //String creationDate = format.format(date);
                    // TODO use file.renameTo(line_below) to rename file with current Date
                    //file.getName().replace(file.getName().substring(4, file.getName().indexOf("_", 5)), creationDate);
                    sipFile = file;

                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sipFile;
    }
}
