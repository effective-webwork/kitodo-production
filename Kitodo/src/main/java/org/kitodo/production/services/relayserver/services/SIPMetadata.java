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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.KitodoConfig;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.relayserver.RelayServerServiceConfig;
import org.kitodo.production.services.relayserver.helper.XMLHelper;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class SIPMetadata {
    private static Properties kitodoProperties = new Properties();
    private static final String KITODO_PROPERTIES_FILE = RelayServerServiceConfig.getKitodoPropertiesFile();
    private static final Map<String, String> fileExportMapping = new HashMap<>();
    private Document metaXml;
    private Process process;
    private URI processUri;
    private Document newDoc;
    private ArrayList<String> sourceImageIds = new ArrayList<>();
    private Map<String, Node> targetImages = new HashMap<>();
    private Map<String, Node> premisFiles = new HashMap<>();
    private Node processPremisFile = null;
    private HashMap<String, Node> logicalNodeMapping = new HashMap<>();
    private HashMap<String, String> importExportMapping = new HashMap<String, String>();
    private final List<String> sourceFileUses;
    private String dossierId = "";
    private Node rootDossier = null;
    private boolean workingCopyType = false;
    private int indexDocument = 0;
    private int hierarchicalLevel = 0;
    private ArrayList<Integer> currentHierarchicalNumber = new ArrayList<>();
    private static final XPath xPath = XPathFactory.newInstance().newXPath();
    private static final String XMLNS = "http://bar.admin.ch/arelda/v4";
    private static final String PREMIS_PATH = "images_export";
    private static final String PREMIS_SUFFIX = "_PREMIS.xml";
    private static final String PROCESS_PREMIS_FILE = "Prozess_Digitalisierung_PREMIS.xml";
    private static final String PHYS_STRUCTMAP = "/mets/structMap[@TYPE='PHYSICAL']/div";
    private static final String SIP_DOCUMENT_TAGNAME = "dokument";
    private static final String SIP_FILEREF_TAGNAME = "dateiRef";
    private static final String WORKING_COPY = "/mets/dmdSec/mdWrap/xmlData/kitodo/metadata[@name='benutzungskopie']";
    private static final String CONFIGURATION_DIR = RelayServerServiceConfig.class.getResource("/configuration").getPath() + File.separator;
    private static final Logger logger = LogManager.getLogger(SIPMetadata.class);

    private static final String[] DELIVERABLE_ATTRIBUTE_ORDER = {
            "ablieferndeStelle",
            "ablieferungsnummer",
            "provenienz",
            "ordnungssystem"
    };

    private static final String[] DOCUMENT_ATTRIBUTE_ORDER = {
            "titel",
            "autor",
            "erscheinungsform",
            "dokumenttyp",
            "registrierdatum",
            "entstehungszeitraum",
            "klassifizierungskategorie",
            "datenschutz",
            "oeffentlichkeitsstatus",
            "oeffentlichkeitsstatusBegruendung",
            "sonstigeBestimmungen",
            "bemerkung",
            "zusatzDaten",
            "dateiRef"
    };
    private static final String[] DOSSIER_ATTRIBUTE_ORDER = {
            "zusatzmerkmal",
            "titel",
            "inhalt",
            "erscheinungsform",
            "entstehungszeitraum",
            "zusatzDaten",
            "aktenzeichen",
            "dossier",
            "dokument"
    };

    private static final String[] INVENTORY_ATTRIBUTE_ORDER = {
            "nummer",
            "titel",
            "ordnungssystemposition",
            "dossier"
    };


    /**
     * Default constructor.
     * @param process The process to create the SIP for.
     */
    SIPMetadata(Process process) {
        this.process = process;
        URI rootPath = Paths.get(KitodoConfig.getKitodoDataDirectory()).toUri();
        URI uri = ServiceManager.getFileService().getProcessBaseUriForExistingProcess(process);
        this.processUri = rootPath.resolve(uri);
        metaXml = XMLHelper.loadXMLFile(processUri.getPath() + "/meta.xml");
        sourceFileUses = process.getProject().getFolders().stream()
                .filter(folder -> !folder.equals(process.getProject().getPreview()) && !folder.equals(process.getProject().getMediaView()))
                .map(Folder::getFileGroup)
                .collect(Collectors.toList());
        this.currentHierarchicalNumber.add(1);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the SIP metadata.xml file.
     * Uses Kitodo's internal metadata file 'meta.xml' to create the 'metadata.xml' for the SIP.
     *
     * @return Document containing the created metadata
     */
    Document createSIPMetadata() throws IOException, SAXException, IllegalStateException {
        workingCopyType = getWorkingCopyType(metaXml);

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            newDoc = docBuilder.newDocument();

            // create root element 'paket'
            Element rootElement = newDoc.createElementNS(XMLNS, "paket");
            rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            rootElement.setAttribute("xsi:type", "paketSIP");
            rootElement.setAttribute("schemaVersion", "4.0");
            newDoc.appendChild(rootElement);

            // create element 'paketTyp'
            Element paketTyp = newDoc.createElementNS(XMLNS, "paketTyp");
            paketTyp.appendChild(newDoc.createTextNode("SIP"));
            rootElement.appendChild(paketTyp);

            // create element 'inhaltsverzeichnis'
            createTableOfContentsSec();

            // create element 'ablieferung'
            createDeliverySec();

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(newDoc);
            URI rootPath = Paths.get(KitodoConfig.getKitodoDataDirectory()).toUri();
            URI uri = ServiceManager.getFileService().getProcessBaseUriForExistingProcess(process);
            URI processUri = rootPath.resolve(uri);
            StreamResult result = new StreamResult(new File(processUri.getPath() + "/metadata.xml"));
            transformer.transform(source, result);

            // validate metadata
            validateSipMetadata(newDoc);

            logger.info("metadata.xml file successfully saved!");
            return newDoc;
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create the delivery section 'ablieferung' for the SIP metadata.xml file.
     *
     */
    private void createDeliverySec() {
        // get logical structMap's children and map them
        String pathToLogicalStructMap = "/mets/structMap[@TYPE='LOGICAL']";
        try {
            Node logicalStructMap = (Node) xPath.compile(pathToLogicalStructMap).evaluate(metaXml, XPathConstants.NODE);
            NodeList children = logicalStructMap.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (noElementNode(children.item(i))) {
                    continue;
                }
                mapNode(children.item(i), newDoc.getDocumentElement());
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        // map the references to the physical files
        mapFileReferences();
    }

    /**
     * Create the 'inhaltsverzeichnis' table of contents section for the SIP metadata.xml file.
     *
     */
    private void createTableOfContentsSec() throws IOException, SAXException {
        //String filePath = CONFIGURATION_DIR + "SIP_header.xml";
        //Document header = XMLHelper.loadXMLFile(filePath);
        //Element headerDir = header.getDocumentElement();
        Element inhaltsverzeichnis = newDoc.createElementNS(XMLNS, "inhaltsverzeichnis");
        newDoc.getDocumentElement().appendChild(inhaltsverzeichnis);

        // create node 'header'
        //TODO: Delete namespace from imported Node 'ordner'
        Node header = newDoc.createElementNS(XMLNS, "ordner");
        inhaltsverzeichnis.appendChild(header);

        // create node 'ordner' with name 'header'
        Node headerName = newDoc.createElementNS(XMLNS, "name");
        headerName.appendChild(newDoc.createTextNode("header"));
        header.appendChild(headerName);
        Node headerOriginalName = newDoc.createElementNS(XMLNS, "originalName");
        headerOriginalName.appendChild(newDoc.createTextNode("header"));
        header.appendChild(headerOriginalName);

        // create subnode 'ordner' with name 'xsd'
        Node xsd = newDoc.createElementNS(XMLNS, "ordner");
        Node xsdName = newDoc.createElementNS(XMLNS, "name");
        xsdName.appendChild(newDoc.createTextNode("xsd"));
        xsd.appendChild(xsdName);
        Node xsdOriginalName = newDoc.createElementNS(XMLNS, "originalName");
        xsdOriginalName.appendChild(newDoc.createTextNode("xsd"));
        xsd.appendChild(xsdOriginalName);
        header.appendChild(xsd);

        // create file nodes
        List<String> xsdPaths = getXsd();
        for (int i = 0; i < xsdPaths.size(); i++) {
            createFileNode(xsdPaths.get(i), xsd);
        }

        // create node 'content'
        Element ordner = newDoc.createElementNS(XMLNS, "ordner");
        inhaltsverzeichnis.appendChild(ordner);
        Element name = newDoc.createElementNS(XMLNS, "name");
        name.appendChild(newDoc.createTextNode("content"));
        ordner.appendChild(name);
        Element originalName = newDoc.createElementNS(XMLNS, "originalName");
        originalName.appendChild(newDoc.createTextNode("content"));
        ordner.appendChild(originalName);

        // create child node of 'content'
        Element subDir = newDoc.createElementNS(XMLNS, "ordner");
        Element subDirName = newDoc.createElementNS(XMLNS, "name");
        subDirName.appendChild(newDoc.createTextNode("d_0000001"));
        subDir.appendChild(subDirName);
        Element subDirOriginalName = newDoc.createElementNS(XMLNS, "originalName");
        subDirOriginalName.appendChild(newDoc.createTextNode("d_0000001"));
        subDir.appendChild(subDirOriginalName);
        ordner.appendChild(subDir);

        Map<String, String> imagesMap = getImages(); // imagePath, physId
        Map<String, String> premisMap = getPremis(); // fileName, PremisPath
        Map<String, String> premisHelperMap = new HashMap<>(); // fileName, physId
        if (imagesMap.size() != premisMap.size()) {
            throw new IOException("Number of images and premis files do not match. " + imagesMap.size() + " images and " + premisMap.size() + " premis files.");
        }
        validatePremisFiles(premisMap);

        // create nodes for image files
        List<String> sortedImagePaths = new ArrayList<>(imagesMap.keySet());
        sortedImagePaths.sort(Comparator.naturalOrder());
        for (String imagePath : sortedImagePaths) {
            // converted images are exported, not the original scanned images
            String[] fileNameParts = imagePath.split("/");
            String fileName = fileNameParts[fileNameParts.length - 1];
            String exportFileName = fileExportMapping.keySet().stream()
                    .filter(fileName::contains)
                    .findAny()
                    .map(s -> fileName.replace(s, fileExportMapping.get(s)))
                    .orElse(fileName);
            String exportImagePath = processUri.getPath() + File.separator + "images_export" + File.separator + exportFileName;
            targetImages.put(imagesMap.get(imagePath), createFileNode(exportImagePath, subDir));
            premisHelperMap.put(fileName, imagesMap.get(imagePath));
        }

        // create nodes for premis files
        List<String> sortedPremisFileNames = new ArrayList<>(premisHelperMap.keySet());
        sortedPremisFileNames.sort(Comparator.naturalOrder());
        for (String fileName : sortedPremisFileNames) {
            String fileNameNoSuffix = fileExportMapping.keySet().parallelStream()
                    .filter(fileName::endsWith)
                    .findAny()
                    .map(s -> fileName.substring(0, fileName.lastIndexOf(s)))
                    .orElse(fileName);
            premisFiles.put(premisHelperMap.get(fileName), createFileNode(premisMap.get(fileNameNoSuffix),  subDir));
        }

        // create node for process premis file
        String processPremisFilePath = processUri.getPath() + File.separator + PROCESS_PREMIS_FILE;
        this.processPremisFile = createFileNode(processPremisFilePath, subDir);
    }

    /**
     * Maps the given node from the logical structMap in Kitodo's internal 'meta.xml' to the 'metadata.xml' for the SIP.
     * @param sourceNode Node from Kitodo's internal format that should be mapped
     * @param targetParent Node from the SIP metadata document where the new node should be appended
     */
    private void mapNode(Node sourceNode, Node targetParent) {
        // determine element type
        Element e = (Element) sourceNode;
        String nodeType = e.getAttribute("TYPE");
        Node targetNode;
        switch (nodeType) {
            case "deliverable":
                targetNode = createDeliverable(sourceNode, targetParent);
                break;
            case "inventory":
                targetNode = createInventory(sourceNode, targetParent);
                break;
            case "inventory_item":
                targetNode = createDefault(sourceNode, targetParent, "ordnungssystemposition");
                break;
            case "dossier":
                targetNode = createDefault(sourceNode, targetParent, "dossier");
                break;
            case "container":
                targetNode = targetParent;
                break;
            case "document":
            case "document_installment":
            case "papers":
            case "umschlag":
                targetNode = createDefault(sourceNode, targetParent, "dokument");
                break;
            default:
                // log error and return, if element is not mappable
                logger.error("Element + '" + e + "' is not mappable!");
                return;
                // TODO should we do something else?
        }

        // Save the new Node in a HashMap.
        // We need a reference to this Node when adding the file references later.
        logicalNodeMapping.put(e.getAttribute("ID"), targetNode);

        increaseHierarchicalLevel(nodeType, ((Element) targetParent).getTagName());

        // map all children of this node
        NodeList children = sourceNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            // if node is not an element node ignore it
            if (noElementNode(children.item(i))) {
                continue;
            }
            mapNode(children.item(i), targetNode);
        }

        decreaseHierarchicalLevel(nodeType, ((Element) targetParent).getTagName());
        setCountForHierarchicalOrder(((Element) targetNode).getTagName(), ((Element) targetParent).getTagName());
    }

    /**
     * Creates a default node and maps all attributes to the same position.
     * @param sourceNode Node that should be mapped
     * @param targetParent Parent node where the new node should be appended
     * @param name Name of the node node
     * @return New node
     */
    private Node createDefault(Node sourceNode, Node targetParent, String name) {
        Element element = newDoc.createElementNS(XMLNS, name);
        NodeList attributes = getAttributes(sourceNode);
        String id = createId();

        // save id and node separately if it belongs to the root dossier
        if (name.equalsIgnoreCase("dossier") && targetParent.getNodeName().equalsIgnoreCase("ordnungssystemposition")) {
            dossierId = id;
            rootDossier = element;
        }

        if (Objects.nonNull(attributes)) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (noElementNode(attribute)) {
                    continue;
                }

                // create mapping between digitization job and sip
                if (!workingCopyType
                        && ((Element) attribute).getAttribute("name").equalsIgnoreCase("vrzng_enht_id")
                        && !attribute.getTextContent().isEmpty()) {

                    // concat id with dossier id if it belongs to a document
                    if (name.equalsIgnoreCase("dokument")) {
                        importExportMapping.put(attribute.getTextContent(), dossierId + "@" + id);
                    } else {
                        importExportMapping.put(attribute.getTextContent(), id);
                    }
                }

                // map attribute to sip
                mapAttribute(attribute, element, targetParent);
            }
        }

        // set id for node
        element.setAttribute("id", id);

        // add attributes manually which do not exist in meta.xml
        addAttributesIfNecessary(element, name, targetParent);

        appendToSIP(element, targetParent);
        return element;
    }

    /**
     * Creates a deliverable node and maps the attributes.
     * @param sourceNode Node that should be mapped
     * @param targetParent Parent node where the new node should be appended
     * @return New node
     */
    private Node createDeliverable(Node sourceNode, Node targetParent) {
        Element deliverable = newDoc.createElementNS(XMLNS, "ablieferung");
        // TODO is xsi:type attribute in internal format? If yes, we could use createDefault().
        deliverable.setAttribute("xsi:type", "ablieferungFilesSIP");
        targetParent.appendChild(deliverable);

        // TODO is "ablieferungstyp" always "FILES"?
        Element deliverableType = newDoc.createElementNS(XMLNS, "ablieferungstyp");
        deliverableType.appendChild(newDoc.createTextNode("FILES"));
        deliverable.appendChild(deliverableType);

        NodeList attributes = getAttributes(sourceNode);
        if (Objects.nonNull(attributes)) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (noElementNode(attributes.item(i))) {
                    continue;
                }
                mapAttribute(attributes.item(i), deliverable, targetParent);
            }
        }

        return deliverable;
    }

    /**
     * Creates an inventory node and maps the attributes.
     * @param sourceNode Node that should be mapped
     * @param targetParent Parent node where the new node should be appended
     * @return New node
     */
    private Node createInventory(Node sourceNode, Node targetParent) {
        // Inventory could be "Ordnungssystem" or "Ordnungssystemposition" as child of "Ordnungssystem".
        // If "Ordnungssystem" already exists, this element is added as "Ordnungssystemposition".
        Element inventory = newDoc.createElementNS(XMLNS, "ordnungssystem");
        targetParent.appendChild(inventory);

        NodeList attributes = getAttributes(sourceNode);
        if (Objects.nonNull(attributes)) {
            for (int i = 0; i < attributes.getLength(); i++) {
                if (noElementNode(attributes.item(i))) {
                    continue;
                }
                mapAttribute(attributes.item(i), inventory, targetParent);
            }
        }

        return inventory;
    }

    /**
     * Adds attributes to the element if they are needed.
     * @param element The element which will be checked and expanded if necessary
     * @param name The name of the element is used to distinguish which attributes will be added
     */
    private void addAttributesIfNecessary(Element element, String name, Node targetParent) {
        switch (name) {
            case "dokument":
                writeAppearance(element);
                writeHierarchicalOrder(element);
                break;
            case "dossier":
                // add metadata "titel" if not present
                NodeList childrenTitle = element.getElementsByTagName("titel");
                int lengthTitle = childrenTitle.getLength();
                if (lengthTitle == 0) {
                    Element title = newDoc.createElementNS(XMLNS, "vrzng_enht_titel");
                    title.setAttribute("name", "vrzng_enht_titel");
                    title.setTextContent("");
                    mapAttribute(title, element, targetParent);
                }

                writeAppearance(element);
                // add metadata "entstehungszeitraum" if not present
                NodeList childrenDate = element.getElementsByTagName("entstehungszeitraum");
                int lengthDate = childrenDate.getLength();
                if (lengthDate == 0) {
                    Element creationTime = newDoc.createElementNS(XMLNS, "zt_raum_txt");
                    creationTime.setAttribute("name", "zt_raum_txt");
                    creationTime.setTextContent("keine Angabe");
                    mapAttribute(creationTime, element, targetParent);
                }
                // add metadata "ReihenfolgeAnalogesDossier" if element is a "subdossier"
                if (((Element) targetParent).getTagName().equals("dossier")) {
                    writeHierarchicalOrder(element);
                }
                break;
            case "umschlag":
                writeHierarchicalOrder(element);
                break;
            default:
                // do nothing
        }
    }

    /**
     * Returns a list of attributes for the given node.
     * @param node Node from logical structMap
     * @return NodeList of attribute nodes in corresponding dmdSec
     */
    private NodeList getAttributes(Node node) {
        Element element = (Element) node;
        String id = element.getAttribute("DMDID");
        String searchPathToDmdSec = "/mets/dmdSec[@ID='" + id + "']/mdWrap/xmlData/kitodo";

        try {
            Node dmdSec = (Node) xPath.compile(searchPathToDmdSec).evaluate(metaXml, XPathConstants.NODE);
            if (Objects.isNull(dmdSec)) {
                return null;
            }
            return dmdSec.getChildNodes();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Maps the attribute names and structure from internal format to SIP format.
     * @param attribute Attribute node to be mapped
     * @param targetNode New node where the attribute should be appended
     */
    private void mapAttribute(Node attribute, Node targetNode, Node targetParentNode) {
        switch (getAttributeName(attribute)) {
            case "ablfr_nr": {
                Element elem = newDoc.createElementNS(XMLNS, "ablieferungsnummer");
                elem.appendChild(newDoc.createTextNode(attribute.getTextContent()));
                appendToSIP(elem, targetNode);
                break;
            }
            case "ablfr_prtnr_id": {
                Element elem = newDoc.createElementNS(XMLNS, "ablieferndeStelle");
                elem.appendChild(newDoc.createTextNode(limit(attribute.getTextContent(), 200)));
                appendToSIP(elem, targetNode);
                break;
            }
            case "akte_blbnr_prtnr_id": {
                Element elem = newDoc.createElementNS(XMLNS, "provenienz");
                Element childElem = newDoc.createElementNS(XMLNS, "aktenbildnerName");
                childElem.appendChild(newDoc.createTextNode(limit(attribute.getTextContent(), 200)));
                elem.appendChild(childElem);
                appendToSIP(elem, targetNode);
                break;
            }
            case "sgntr_cd": {
                // TODO nur ein Ziel im SIP vermerkt. Andere nicht ins SIP übernehmen?
                if (Objects.equals(targetNode.getNodeName().toLowerCase(), "ordnungssystemposition")) {
                    Element elem = newDoc.createElementNS(XMLNS, "nummer");
                    String textContent = attribute.getTextContent().replaceAll(".*#", "");
                    elem.appendChild(newDoc.createTextNode(limit(textContent, 100)));
                    appendToSIP(elem, targetNode);
                }
                break;
            }
            case "vrzng_enht_id": {
                if (targetNode.getNodeName().equalsIgnoreCase("dossier")
                        && targetParentNode.getNodeName().equals("ordnungssystemposition")) {
                    Node deliveryNumber = newDoc.getElementsByTagName("ablieferungsnummer").item(0);
                    if (!Objects.isNull(deliveryNumber)) {
                        String textContent = deliveryNumber.getTextContent() + "_" + attribute.getTextContent();
                        deliveryNumber.setTextContent(limit(textContent, 100));
                    }
                }
                break;
            }
            case "vrzng_enht_titel": {
                // This inner switch decides how the metadata field should be mapped depending on its position.
                // "Ordnungssystem", "Ordnungssystemposition", "Dossier" and "Dokument" can have this field.
                // Naming is different.
                switch (targetNode.getNodeName().toLowerCase()) {
                    case "ordnungssystem": {
                        Element elem = newDoc.createElementNS(XMLNS, "name");
                        elem.appendChild(newDoc.createTextNode(limit(attribute.getTextContent(), 200)));
                        appendToSIP(elem, targetNode);
                        break;
                    }
                    case "ordnungssystemposition": {
                        Element elem = newDoc.createElementNS(XMLNS, "titel");
                        elem.appendChild(newDoc.createTextNode(limit(attribute.getTextContent(), 200)));
                        appendToSIP(elem, targetNode);
                        break;
                    }
                    // TODO indices müssen formatiert werden bsp 000000n, funktioniert das so?
                    case "dossier": {
                        String title = attribute.getTextContent();
                        if (Objects.equals(title.toLowerCase(), "subdossier") || title.length() == 0) {
                            title = "Subdossier_" + formatNumber(indexDocument++);
                        } else {
                            indexDocument++;
                        }
                        Element elem = newDoc.createElementNS(XMLNS, "titel");
                        elem.appendChild(newDoc.createTextNode(title));
                        appendToSIP(elem, targetNode);
                        break;
                    }
                    case "dokument": {
                        String title = attribute.getTextContent();
                        switch (title.toLowerCase()) {
                            case "umschlag":
                                title = "Umschlag_" + formatNumber(indexDocument++);
                                break;
                            case "dokument":
                                title = "Dokument_" + formatNumber(indexDocument++);
                                break;
                            case "dokument-fortsetzung":
                                title = "Dokument-Fortsetzung_" + formatNumber(indexDocument++);
                                break;
                            case "unterlagen":
                                title = "Unterlagen_" + formatNumber(indexDocument++);
                                break;
                            default:
                                indexDocument++;
                        }

                        Element elem = newDoc.createElementNS(XMLNS, "titel");
                        elem.appendChild(newDoc.createTextNode(title));
                        appendToSIP(elem, targetNode);
                        break;
                    }
                }
                break;
            }
            case "zt_raum_txt": {
                Element elem = mapDate(attribute.getTextContent());
                appendToSIP(elem, targetNode);
                break;
            }
            case "aktnzchn": {
                switch (targetNode.getNodeName().toLowerCase()) {
                    case "dokument":
                        // do nothing - attribute is not allowed at this node
                        break;
                    default:
                        Element elem = newDoc.createElementNS(XMLNS, "aktenzeichen");
                        elem.appendChild(newDoc.createTextNode(limit(attribute.getTextContent(), 200)));
                        appendToSIP(elem, targetNode);
                        break;
                }
                break;
            }
            case "zstz_mrkml": {
                switch (targetNode.getNodeName().toLowerCase()) {
                    case "dossier":
                        Element elem = newDoc.createElementNS(XMLNS, "zusatzmerkmal");
                        elem.appendChild(newDoc.createTextNode(limit(attribute.getTextContent(), 200)));
                        appendToSIP(elem, targetNode);
                        break;
                    default:
                        // Do nothing - should not be mapped
                        break;
                }
                break;
            }
            case "darin_txt": {
                switch (targetNode.getNodeName().toLowerCase()) {
                    case "dokument":
                        // do nothing - attribute is not allowed at this node
                        break;
                    default:
                        Element elem = newDoc.createElementNS(XMLNS, "inhalt");
                        elem.appendChild(newDoc.createTextNode(attribute.getTextContent()));
                        appendToSIP(elem, targetNode);
                        break;
                }
                break;
            }
        }
    }

    /**
     * Appends the given element to the target node. Determines the correct postition of the given element.
     * @param elem Element to be appended
     * @param targetNode parent Node where the element should be appended
     */
    private void appendToSIP(Node elem, Node targetNode) {
        Node nextNode = getNextAttribute(elem, targetNode);
        targetNode.insertBefore(elem, nextNode);
    }

    /**
     * Finds the next attribute that should be after the given attribute.
     * @param attribute Node to find the correct position for
     * @param parentNode Node where attribute should be appended
     * @return Node that should be after the given attribute
     */
    private Node getNextAttribute(Node attribute, Node parentNode) {
        String[] attributeOrder;

        switch (parentNode.getNodeName().toLowerCase()) {
            case "ablieferung":
                attributeOrder = DELIVERABLE_ATTRIBUTE_ORDER;
                break;
            case "dokument":
                attributeOrder = DOCUMENT_ATTRIBUTE_ORDER;
                break;
            case "dossier":
                attributeOrder = DOSSIER_ATTRIBUTE_ORDER;
                break;
            case "ordnungssystemposition":
                attributeOrder = INVENTORY_ATTRIBUTE_ORDER;
                break;
            default:
                // do nothing
                return null;
        }

        int index = getCurrentIndex(attribute.getNodeName(), attributeOrder);
        if (index == -1) {
            return null;
        }
        return getNextExistingAttribute(index, parentNode, attributeOrder);
    }

    /**
     * Finds the index of the given String nodeName in the given array attributeOrder.
     * @param nodeName String to find in array
     * @param attributeOrder Array containing node names
     * @return Index of nodeName or -1 if none matched
     */
    private int getCurrentIndex(String nodeName, String[] attributeOrder) {
        int index = -1;
        for (int i = 0; i < attributeOrder.length; i++) {
            if (attributeOrder[i].toLowerCase().equals(nodeName.toLowerCase())) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * Finds the next attribute in attributeOrder array that exists as childNode of parentNode.
     * @param index Only finds node types from attributeOrder after this index
     * @param parentNode Node that contains the childNodes to be compared to attributeOrder array
     * @param attributeOrder Array of strings containing the nodeNames for possible childNodes of parentNode
     * @return Node object containing the matching childNode or null of none matched
     */
    private Node getNextExistingAttribute(int index, Node parentNode, String[] attributeOrder) {
        NodeList currentAttributes = parentNode.getChildNodes();
        for (int i = index + 1; i < attributeOrder.length; i++) {
            for (int j = 0; j < currentAttributes.getLength(); j++) {
                if (attributeOrder[i].equals(currentAttributes.item(j).getNodeName().toLowerCase())) {
                    return currentAttributes.item(j);
                }
            }
        }
        return null;
    }

    /**
     * Creates a file node for the file at the given file path. Creates a unique id and a checksum of the file.
     * @param filePath Path to file to be referenced
     * @param parentNode Parent node where the new file node should be appended
     * @return New file node
     */
    private Node createFileNode(String filePath, Node parentNode) throws IOException {
        Element elem = newDoc.createElementNS(XMLNS, "datei");
        elem.setAttribute("id", "_" + RandomStringUtils.randomAlphanumeric(22));
        Element namee = newDoc.createElementNS(XMLNS, "name");
        namee.appendChild(newDoc.createTextNode(getFileName(filePath)));
        elem.appendChild(namee);
        Element original = newDoc.createElementNS(XMLNS, "originalName");
        original.appendChild(newDoc.createTextNode(getFileName(filePath)));
        elem.appendChild(original);
        Element algo = newDoc.createElementNS(XMLNS, "pruefalgorithmus");
        algo.appendChild(newDoc.createTextNode("MD5"));
        elem.appendChild(algo);
        Element check = newDoc.createElementNS(XMLNS, "pruefsumme");
        check.appendChild(newDoc.createTextNode(getMD5(filePath)));
        elem.appendChild(check);
        parentNode.appendChild(elem);

        return elem;
    }

    /**
     * Maps the references between physical files and logical structure from internal format to SIP format.
     */
    private void mapFileReferences() {
        for (String physId : sourceImageIds) {
            if (Objects.isNull(targetImages.get(physId)) || Objects.isNull(premisFiles.get(physId))) {
                continue;
            }
            String pathToStructLink = "/mets/structLink/smLink[@to='" + physId + "']/@from";
            try {
                // Find all logical elements this media file is linked to.
                NodeList logIds = (NodeList) xPath.compile(pathToStructLink).evaluate(metaXml, XPathConstants.NODESET);

                /*
                 *  Iterate over all matching logical elements.
                 *  There might be more than one matching element since every media file can be linked to multiple logical elements.
                 *  This is primarily for audio/video files.
                 */
                for (int j = 0; j < logIds.getLength(); j++) {
                    Node logId = logIds.item(j);
                    // get new node mapped to the element from logical structMap with this id
                    Node targetNode = logicalNodeMapping.get(logId.getTextContent());

                    // create reference for image file
                    Element image = newDoc.createElementNS(XMLNS, "dateiRef");
                    image.appendChild(newDoc.createTextNode(((Element) targetImages.get(physId)).getAttribute("id")));
                    targetNode.appendChild(image);

                    // create reference for premis file
                    Element premis = newDoc.createElementNS(XMLNS, "dateiRef");
                    premis.appendChild(newDoc.createTextNode(((Element) premisFiles.get(physId)).getAttribute("id")));
                    targetNode.appendChild(premis);
                }
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }

        // create reference for process premis file
        mapProcessPremisFileReference();
    }

    /**
     * Map file reference for process premis file "Prozess_Digitalisierung_PREMIS.xml" to root dossier node.
     */
    private void mapProcessPremisFileReference() {
        Element premis = newDoc.createElementNS(XMLNS, "dateiRef");
        premis.appendChild(newDoc.createTextNode(((Element) this.processPremisFile).getAttribute("id")));
        rootDossier.appendChild(premis);
    }

    /**
     * Maps a date or period to correct format for SIP.
     *
     * @return Element containing the formatted date or period
     */
    private Element mapDate(String date) {
        if (Objects.isNull(date)) {
            date = "";
        }

        Element creationdate = newDoc.createElementNS(XMLNS, "entstehungszeitraum");

        Element from = newDoc.createElementNS(XMLNS, "von");
        creationdate.appendChild(from);

        Element fromDate = newDoc.createElementNS(XMLNS, "datum");
        from.appendChild(fromDate);

        Element until = newDoc.createElementNS(XMLNS, "bis");
        creationdate.appendChild(until);

        Element untilDate = newDoc.createElementNS(XMLNS, "datum");
        until.appendChild(untilDate);

        String[] parts = date.split("-");

        if (parts[0].contains("ca.")) {
            Element fromApprox = newDoc.createElementNS(XMLNS, "ca");
            fromApprox.setTextContent("true");
            from.insertBefore(fromApprox, fromDate);
        }
        fromDate.setTextContent(convertDate(parts[0]));

        if (parts.length > 1) {
            if (parts[1].contains("ca.")) {
                Element untilApprox = newDoc.createElementNS(XMLNS, "ca");
                untilApprox.setTextContent("true");
                until.insertBefore(untilApprox, untilDate);
            }
            untilDate.setTextContent(convertDate(parts[1]));
        } else {
            untilDate.setTextContent(convertDate(parts[0]));
        }

        return creationdate;
    }

    /**
     * Converts a date like 19.12.2018 to 2018-12-19.
     * "ca." may precede the date indicating that it's an approximate date and should be removed here.
     *
     * @param date String like 19.12.2018, ca.19.12.2018 or 2018
     * @return String like 2018-12-19 or 2018
     */
    private String convertDate(String date) {
        if (date.isEmpty()) {
            return "keine Angabe";
        }
        String[] fromParts = date.replace("ca.", "").split("\\.");
        StringBuilder formattedFrom = new StringBuilder();
        for (int i = fromParts.length - 1; i >= 0; i--) {
            formattedFrom.append(fromParts[i]);
            if (i > 0) {
                formattedFrom.append("-");
            }
        }
        return formattedFrom.toString();
    }

    /**
     * Increase the hierarchical level if the current node type should be counted for the hierarchical order.
     * @param nodeType String containing the type of element, e.g. "inventory", "dossier", "document"
     */
    private void increaseHierarchicalLevel(String nodeType, String parentType) {
        if ((nodeType.equals("dossier") && parentType.equals("dossier")) || nodeType.equals("dokument") || nodeType.equals("umschlag")) {
            this.hierarchicalLevel++;
            this.currentHierarchicalNumber.add(1);
        }
    }

    /**
     * Decrease the hierarchical level if the current node type should be counted for the hierarchical order.
     * @param nodeType String containing the type of element, e.g. "inventory", "dossier", "document"
     */
    private void decreaseHierarchicalLevel(String nodeType, String parentType) {
        if ((nodeType.equals("dossier") && parentType.equals("dossier")) || nodeType.equals("dokument") || nodeType.equals("umschlag")) {
            this.hierarchicalLevel--;
            this.currentHierarchicalNumber.subList(this.hierarchicalLevel + 1, this.currentHierarchicalNumber.size()).clear();
        }
    }

    /**
     * Increase the count on the current level if the current node type should be counted for the hierarchical order.
     * @param nodeType String containing the type of element, e.g. "inventory", "dossier", "document"
     */
    private void setCountForHierarchicalOrder(String nodeType, String parentType) {
        if ((nodeType.equals("dossier") && parentType.equals("dossier")) || nodeType.equals("dokument") || nodeType.equals("umschlag")) {
            this.currentHierarchicalNumber.set(this.hierarchicalLevel, this.currentHierarchicalNumber.get(this.hierarchicalLevel) + 1);
        }
    }

    /**
     * Create and append a node for the "appearance" metadata.
     * @param element The Element where the new node should be appended.
     */
    private void writeAppearance(Element element) {
        Element form = newDoc.createElementNS(XMLNS, "erscheinungsform");
        form.appendChild(newDoc.createTextNode("digital"));
        appendToSIP(form, element);
    }

    /**
     * Create and append a node reflecting the hierarchical order of the element (only used for dossiers, subsdossiers and documents).
     * @param element The Element where the new node should be appended.
     */
    private void writeHierarchicalOrder(Element element) {
        Element additionalData = newDoc.createElementNS(XMLNS, "zusatzDaten");
        Element hierarchicalOrder = newDoc.createElementNS(XMLNS, "merkmal");
        hierarchicalOrder.setAttribute("name", "ReihenfolgeAnalogesDossier");
        StringJoiner joiner = new StringJoiner(".");
        this.currentHierarchicalNumber.stream().map(integer -> String.format("%06d", integer)).forEach(joiner::add);
        hierarchicalOrder.appendChild(newDoc.createTextNode(joiner.toString()));
        additionalData.appendChild(hierarchicalOrder);
        appendToSIP(additionalData, element);
    }

    /**
     * Checks whether the given node is of type 'element'.
     * @param node Node to be checked
     * @return Boolean, true if the node is no element node, false if the node is an element node
     */
    private boolean noElementNode(Node node) {
        return !Objects.equals(node.getNodeType(), Node.ELEMENT_NODE);
    }

    /**
     * Returns the name of an attribute node.
     * @param node Attribute node
     * @return String name of the attribute
     */
    private String getAttributeName(Node node) {
        return ((Element) node).getAttribute("name");
    }

    /**
     * Create checksum for file at given file path.
     * @param filePath String containing path to file
     * @return String containing checksum
     */
    private String getMD5(String filePath) throws IOException {
        URI rootPath = Paths.get(ConfigCore.getParameter(JobService.getMetadataFolder())).toUri();
        String path = rootPath.resolve(filePath).getPath();
        try (FileInputStream fileInputStream = new FileInputStream(path)) {
            // md5Hex calculates the hash sum for the given input stream and returns it as 32 character string.
            return DigestUtils.md5Hex(fileInputStream);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Generates a random id with 22 characters.
     * @return String id
     */
    private String createId() {
        return "_" + RandomStringUtils.randomAlphanumeric(22);
    }

    /**
     * Return the file name for the given path.
     * @param filePath String containing path to file
     * @return String containing file name including suffix
     */
    private String getFileName(String filePath) {
        File f = new File(filePath);
        return f.getName();
    }

    /**
     * Get list of image paths for current process.
     * @return ArrayList of image paths
     */
    Map<String, String> getImages() {
        String structMapXPATH = PHYS_STRUCTMAP + "//div[@TYPE='partialJob']/div[@TYPE='page' or @TYPE='track' or @TYPE='other']";
        Map<String, String> filePaths = new HashMap<>();
        try {
            NodeList files = (NodeList) xPath.compile(structMapXPATH).evaluate(metaXml, XPathConstants.NODESET);
            List<Integer> correctOrder = new ArrayList<>();

            // get file nodes in correct order
            for (int i = 0; i < files.getLength(); i++) {
                Node page = files.item(i);
                int pageOrder = Integer.parseInt(((Element) page).getAttribute("ORDER"));
                correctOrder.add(pageOrder);
            }
            Collections.sort(correctOrder);

            for (int currentIndex : correctOrder) {
                NodeList fileIdList = (NodeList) xPath.compile(PHYS_STRUCTMAP + "//div[@ORDER='" + currentIndex + "']/fptr/@FILEID").evaluate(metaXml, XPathConstants.NODESET);
                Node filePath = null;
                Node parent = null;
                for (int j = 0; j < fileIdList.getLength(); j++) {
                    String fileIdText = fileIdList.item(j).getTextContent();
                    StringBuilder fileGroups = new StringBuilder();
                    for (int i = 0; i < sourceFileUses.size(); i++) {
                        fileGroups.append("@USE='" + sourceFileUses.get(i) + "'");
                        if (i < sourceFileUses.size() - 1) {
                            fileGroups.append(" or ");
                        }
                    }
                    Node possibleFilePath = (Node) xPath.compile("/mets/fileSec/fileGrp[" + fileGroups.toString() + "]/file[@ID='" + fileIdText + "']/FLocat/@href").evaluate(metaXml, XPathConstants.NODE);
                    if (Objects.nonNull(possibleFilePath)) {
                        filePath = possibleFilePath;
                        parent = ((Attr) fileIdList.item(j)).getOwnerElement().getParentNode();
                        break;
                    }
                }


                if (Objects.isNull(filePath) || Objects.isNull(parent)) {
                    throw new IllegalArgumentException("No file for file pointer with order " + currentIndex + " found");
                }

                String id = ((Element) parent).getAttribute("ID");
                filePaths.put(filePath.getTextContent(), id);
                sourceImageIds.add(id);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return filePaths;
    }

    /**
     * Get list of premis file paths for current process.
     * @return List of premis paths
     */
    private Map<String, String> getPremis() {
        Path premisDir = Paths.get(this.processUri.getPath() + File.separator + PREMIS_PATH);
        if (premisDir.toFile().exists() && premisDir.toFile().isDirectory()) {
            try (Stream<Path> premisPathsStream = Files.list(premisDir)) {
                List<String> premisPaths = premisPathsStream.filter(path -> path.toFile().isFile())
                        .filter(path -> path.toFile().canRead())
                        .filter(path -> path.toString().endsWith(PREMIS_SUFFIX))
                        .sorted()
                        .map(Path::toString)
                        .collect(Collectors.toList());
                Map<String, String> premisFileNames = new HashMap<>();
                for (String path : premisPaths) {
                    premisFileNames.put(path.replace(PREMIS_SUFFIX, "").replaceAll(".+/", ""), path);
                }
                return premisFileNames;
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage());
            }
        }
        return new HashMap<>();
    }

    /**
     * Get list of xsd file paths to be copied to every SIP file.
     * @return List of xsd paths
     */
    private List<String> getXsd() {
        Path xsdDir = Paths.get(CONFIGURATION_DIR + "xsd");
        if (xsdDir.toFile().exists() && xsdDir.toFile().isDirectory()) {
            try (Stream<Path> xsdPathsStream = Files.list(xsdDir)) {
                return xsdPathsStream.filter(path -> path.toFile().isFile())
                        .filter(path -> path.toFile().canRead())
                        .filter(path -> path.toString().endsWith(".xsd"))
                        .sorted()
                        .map(Path::toString)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage());
            }
        }

        return new LinkedList<>();
    }

    private String formatNumber(int num) {
        return String.format("%07d", num);
    }

    void createImportExportMapping() {
        if (workingCopyType) {
            return;
        }
        StringBuilder objectIds = new StringBuilder();
        StringBuilder objectValues = new StringBuilder();

        Iterator iterator = importExportMapping.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            objectIds.append(entry.getKey());
            objectValues.append("AIP@" + entry.getValue());
            if (iterator.hasNext()) {
                objectIds.append(",");
                objectValues.append(",");
            }
            iterator.remove();
        }

        String parameters = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<mapping>"
                + "<objectIds>" + objectIds + "</objectIds>\n"
                + "<objectValues>" + objectValues + "</objectValues>\n"
                + "</mapping>";

        try {
            URI filepath = ServiceManager.getFileService().createResource(this.processUri, "mapping.xml");
            PrintWriter writer = new PrintWriter(new File(Paths.get(KitodoConfig.getKitodoDataDirectory()).toUri().resolve(filepath)));
            writer.println(parameters);
            writer.close();
        } catch (IOException e) {
            logger.error("Could not create file 'mapping.xml': " + e);
        }

    }

    /**
     * Retrieve and return the type of working copy "Benutzungskopie" of the digitization job contained in the given document 'job'.
     *
     * @param job Document containing a digitization job
     * @return the type of the working copy of the digitization job
     */
    public static boolean getWorkingCopyType(Document job) {
        try {
            Node node = (Node) xPath.compile(WORKING_COPY).evaluate(job, XPathConstants.NODE);
            if (Objects.isNull(node)) {
                return false;
            }
            return Boolean.parseBoolean(node.getTextContent().trim());
        } catch (XPathExpressionException e) {
            logger.error("Unable to determine working copy: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates the created metadata.xml file.
     * Checks against the SIP schemata and checks whether the file contains documents without files associated to it.
     * @param sipDoc document object of the metadata.xml
     * @throws IOException
     * @throws SAXException
     * @throws IllegalStateException
     */
    private void validateSipMetadata(Document sipDoc) throws IOException, SAXException, IllegalStateException {
        NodeList documentList = sipDoc.getDocumentElement().getElementsByTagName(SIP_DOCUMENT_TAGNAME);
        for (int i = 0; i <= documentList.getLength(); i++) {
            Element documentNode = (Element) documentList.item(i);
            if (Objects.nonNull(documentNode)) {
                NodeList fileRefList = documentNode.getElementsByTagName(SIP_FILEREF_TAGNAME);
                if (fileRefList.getLength() <= 0) {
                    throw new IllegalStateException("Document without associated files found (Title \"" + documentNode.getElementsByTagName("titel").item(0).getTextContent() + "\").");
                }
            }
        }

        String filePath = CONFIGURATION_DIR + "xsd" + File.separator + "arelda.xsd";
        XMLHelper.validateXMLAgainstSchema(XMLHelper.convertDocumentToString(newDoc, false), filePath);
    }

    private void validatePremisFiles(Map<String, String> premisPathList) throws IOException, SAXException {
        for (Map.Entry<String, String> premisPath : premisPathList.entrySet()) {
            String premisFile = new String(Files.readAllBytes(Paths.get(premisPath.getValue())));
            // remove "BOM" (ZWNBSP) at the beginning of the the file
            premisFile = premisFile.replace("\uFEFF", "");
            XMLHelper.validateXMLAgainstSchema(premisFile, CONFIGURATION_DIR + "premis.xsd");
        }
    }

    private String limit(String string, int length) {
        if (length <= string.length()) {
            String substring = string.substring(0, length);
            logger.info("Process " + process.getId() + ": Shortening \"" + string + "\" to \"" + substring + "\" due to maximum length of " + length + " characters.");
            return substring;
        }
        return string;
    }
}
