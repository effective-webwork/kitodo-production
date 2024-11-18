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

package org.kitodo.dataeditor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.kitodo.api.dataeditor.DataEditorInterface;
import org.kitodo.dataeditor.entities.DmdSec;
import org.kitodo.dataeditor.enums.FileLocationType;
import org.kitodo.dataformat.metskitodo.*;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.w3c.dom.Document;

/**
 * The main class of this module which is implementing the main interface.
 */
public class DataEditor implements DataEditorInterface {

    private MetsKitodoWrapper metsKitodoWrapper;
    private MetsKitodoObjectFactory metsKitodoObjectFactory = new MetsKitodoObjectFactory();
    private Integer dmdCounter = 1;

    private static final String PARTIAL_JOB = "partialJob";
    private static final String PAGE = "page";
    private static final String TRACK = "track";
    private static final String OTHER = "other";
    private static final String PROFILE = "profile";
    private static final String MANUAL_IMAGE_PROCESSING = "manualImageProcessing";

    @Override
    public void readData(URI xmlFileUri, URI xsltFileUri) throws IOException {
        try {
            this.metsKitodoWrapper = new MetsKitodoWrapper(xmlFileUri, xsltFileUri);
        } catch (JAXBException  | TransformerException | DatatypeConfigurationException e) {
            // TODO add also message for module frontend, when it is ready!
            // For now we wrap exceptions in an IOException so that we don't need to
            // implement JAXB to core
            throw new IOException("Unable to read file", e);
        }
    }

    @Override
    public void readData(URI xmlFileUri) throws IOException {
        try {
            this.metsKitodoWrapper = new MetsKitodoWrapper(xmlFileUri);
        } catch (JAXBException | DatatypeConfigurationException e) {
            // TODO add also message for module frontend, when it is ready!
            // For now we wrap exceptions in an IOException so that we don't need to
            // implement JAXB to core
            throw new IOException("Unable to read file", e);
        }
    }

    @Override
    public void createData(String documentType) throws IOException {
        try {
            this.metsKitodoWrapper = new MetsKitodoWrapper(documentType);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean editData(URI xmlFileUri, URI rulesetFileUri) {
        return false;
    }

    private Mets createMetsTypeFromString(String xml) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Mets.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            StringReader stringReader = new StringReader(xml);
            return (Mets) unmarshaller.unmarshal(stringReader);

        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String convertDocumentToString(Document doc) {
        StringWriter writer = new StringWriter();

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.transform(new DOMSource(doc), new StreamResult(writer));
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }

    private List<DivType> convertMetadataGroupsToDivs(List<MetadataGroupType> metadataGroupTypeList) {
        LinkedList<DivType> divTypes = new LinkedList<>();

        for (MetadataGroupType groupType : metadataGroupTypeList) {
            divTypes.add(walkRecursiveMetadataGroup(groupType));
        }

        return divTypes;
    }

    public DivType walkRecursiveMetadataGroup(MetadataGroupType groupType) {

        // *********** create DMD section
        KitodoType kitodoTypeTemp = new KitodoType();
        kitodoTypeTemp.getMetadata().addAll(groupType.getMetadata());

        // generate dmdid
        String dmdId = "DMDLOG_".concat(String.format("%04d", this.dmdCounter));
        this.dmdCounter++;

        // create dmd section
        MdSecType mdSec = this.metsKitodoObjectFactory.createDmdSecByKitodoMetadata(
                kitodoTypeTemp, dmdId
        );

        this.metsKitodoWrapper.getMets().getDmdSec().add(mdSec);
        // ***********

        String divType = groupType.getName();
        DivType newDiv = new DivType();
        newDiv.getDMDID().add(mdSec);
        newDiv.setTYPE(divType);

        if (groupType.getMetadataGroup().isEmpty()) {
            return newDiv;
        } else {
            LinkedList<DivType> newDivChildren = (LinkedList<DivType>) convertMetadataGroupsToDivs(groupType.getMetadataGroup());
            newDiv.getDiv().addAll(newDivChildren);
            return newDiv;
        }
    }

    DivType generatePhysicalRootDmdSec(String divType) {
        // create Metadata
        KitodoType kitodoMetadata = new KitodoType();
        MetadataType imagePath = this.metsKitodoObjectFactory.createMetadataType();
        imagePath.setAnchorId(false);
        imagePath.setName("pathimagefiles");
        kitodoMetadata.getMetadata().add(imagePath);

        // generate dmdid
        String dmdId = "DMDPHYS_0000";

        // create dmd section
        MdSecType dmdSection = this.metsKitodoObjectFactory.createDmdSecByKitodoMetadata(kitodoMetadata, dmdId);

        // add dmd section
        this.metsKitodoWrapper.getMets().getDmdSec().add(dmdSection);

        DivType newDiv = new DivType();
        newDiv.getDMDID().add(dmdSection);
        newDiv.setTYPE(divType);
        return newDiv;
    }

    /**
     * Create a kitodo document from the Document in the given intermediate format.
     *
     * @param kitodoDocument Document in the intermediate format.
     * @return return the Kitodo document in the new Kitodo internal format as an XML string
     */
    public String createKitodoDocument(Document kitodoDocument) {
        metsKitodoObjectFactory = new MetsKitodoObjectFactory();

        String result = "";

        try {
            Mets mets = this.createMetsTypeFromString(convertDocumentToString(kitodoDocument));

            this.metsKitodoWrapper = new MetsKitodoWrapper("deliverable");

            KitodoType kitodoType = JaxbXmlUtils.getKitodoTypeOfDmdSecElement(this.metsKitodoWrapper.getDmdSecs().get(0));

            KitodoType kitodoTypeTemp = JaxbXmlUtils.getKitodoTypeOfDmdSecElement(mets.getDmdSec().get(0));

            List<MetadataGroupType> topGroups = kitodoTypeTemp.getMetadataGroup();

            assert (topGroups.size() == 1);
            kitodoType.getMetadata().addAll(topGroups.get(0).getMetadata());

            // Skip top most metadata group because it was already transformed into a DMDSec and added to
            // the logical struct by the MetsKitodoWrapper constructor!
            LinkedList<DivType> topDivChildren = (LinkedList<DivType>) convertMetadataGroupsToDivs(topGroups.get(0).getMetadataGroup());
            this.metsKitodoWrapper.getLogicalStructMap().getDiv().getDiv().addAll(topDivChildren);
            this.metsKitodoWrapper.getLogicalStructMap().generateIdsForDivs();

            // Add physical struct map div
            DivType physDiv = generatePhysicalRootDmdSec("BoundBook");
            this.metsKitodoWrapper.getPhysicalStructMap().setDiv(physDiv);

            MetsKitodoWriter metsKitodoWriter = new MetsKitodoWriter();
            if (Objects.isNull(metsKitodoWrapper.getFileSec().getFileGrp())
                    || metsKitodoWrapper.getFileSec().getFileGrp().get(0).getFile().isEmpty()) {
                metsKitodoWrapper.getFileSec().getFileGrp().clear();
            }
            result = metsKitodoWriter.writeSerializedToString(metsKitodoWrapper.getMets());

        } catch (DatatypeConfigurationException | IOException | JAXBException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void insertMediaFilesAndAddStructLink(List<String> mediaFilePaths, String physicalDivId, String logDivId,
                                                 Map<String, String> mimeTypeMapping, Map<String, String> suffixMapping) throws Exception {
        List<DivType> physDivs = insertMediaFiles(mediaFilePaths, physicalDivId, mimeTypeMapping, suffixMapping);
        DivType logDiv = new DivType();
        logDiv.setID(logDivId);
        this.metsKitodoWrapper.getStructLink().addSmLinks(logDiv, physDivs);
    }

    @Override
    public void linkMediaFromPrecedingElement(String elementId) {
        DivType precedingElement = metsKitodoWrapper.getPrecedingElement(elementId);
        if (Objects.nonNull(precedingElement)) {
            List<StructLinkType.SmLink> smLinks = metsKitodoWrapper.getStructLink().getSmLinks();
            List<DivType> precedingMedia = smLinks.stream()
                    .filter(smLink -> Objects.equals(smLink.getFrom(), precedingElement.getID()))
                    .map(smLink -> metsKitodoWrapper.findDivTypeById(metsKitodoWrapper.getPhysicalStructMap().getDiv(), smLink.getTo()))
                    .collect(Collectors.toList());
            DivType element = metsKitodoWrapper.findDivTypeById(metsKitodoWrapper.getLogicalStructMap().getDiv(), elementId);
            metsKitodoWrapper.getStructLink().addSmLinks(element, precedingMedia);
        }
    }

    public String addNewStructElement(String logDivtype, String dossierId, String physicalDivId) {
        return this.metsKitodoWrapper.createLogicalDiv(logDivtype, dossierId, physicalDivId);
    }

    public boolean isLogElementPresent(String elementId) {
        DivType root = this.metsKitodoWrapper.getLogicalStructMap().getDiv();
        return Objects.equals(root.getID(), elementId) || !Objects.equals(root, this.metsKitodoWrapper.findDivTypeById(root, elementId));
    }

    public boolean isPhysElementPresent(String elementId) {
        DivType root = this.metsKitodoWrapper.getPhysicalStructMap().getDiv();
        return Objects.equals(root.getID(), elementId) || !Objects.equals(root, this.metsKitodoWrapper.findDivTypeById(root, elementId));
    }

    @Override
    public String getBoundBookElementPhysicalID() {
        return this.metsKitodoWrapper.getPhysicalStructMap().getDiv().getID();
    }

    @Override
    public String getParentDossierId(String dossierId) {
        return this.metsKitodoWrapper.findParentIdById(this.metsKitodoWrapper.getLogicalStructMap().getDiv(), dossierId);
    }

    @Override
    public String getPrecedingDocumentType(String documentId) {
        return this.metsKitodoWrapper.getPrecedingElementType(documentId);
    }

    private List<DivType> insertMediaFiles(List<String> mediaFilePaths, String physicalDivId, Map<String, String> mimeTypeMapping,
                                           Map<String, String> suffixMapping) throws Exception {
        List<MediaFile> files = new ArrayList<>();
        for (String mediaFilePath : mediaFilePaths) {
            String mimeType = "image/tiff";
            if (Objects.nonNull(suffixMapping) && Objects.nonNull(suffixMapping.get(FilenameUtils.getExtension(mediaFilePath)))) {
                mimeType = suffixMapping.get(FilenameUtils.getExtension(mediaFilePath).toLowerCase());
            }
            files.add(new MediaFile(Paths.get(mediaFilePath).toUri(), FileLocationType.URL, mimeType));
        }
        return this.metsKitodoWrapper.insertMediaFiles(files, physicalDivId, mimeTypeMapping);
    }

    public String getUpdatedXmlString() {
        try {
            MetsKitodoWriter metsKitodoWriter = new MetsKitodoWriter();
            return metsKitodoWriter.writeSerializedToString(this.metsKitodoWrapper.getMets());
        } catch (JAXBException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void editHrefFLocat(String oldHref, String newHref, Set<String> uses) {
        this.metsKitodoWrapper.editHrefFLocat(oldHref,newHref, uses);
    }

    @Override
    public void addPartialJob(Object divType, String selectedProfile, String numberOfPages, String startPage, String manualImageProcessing, URI xmlFile ) {
        if (Objects.isNull(divType)) {
            this.metsKitodoWrapper.addPartialJob(selectedProfile, numberOfPages, startPage, manualImageProcessing);
        } else {
            this.metsKitodoWrapper.editPartialJob(divType, selectedProfile, numberOfPages, startPage, manualImageProcessing);
        }
        saveMets(xmlFile);
    }

    @Override
    public void deletePartialJob(Object divTypeObject, URI fileURI) {
        System.out.println("Deleting partial job..");
        if (Objects.nonNull(divTypeObject) && (divTypeObject instanceof DivType)) {
            DivType divType = (DivType) divTypeObject;
            if (divType.getDMDID().size() > 0) {
                MdSecType mdSecType = (MdSecType) divType.getDMDID().get(0);
                int index = -1;
                for (DmdSec dmdSec : this.metsKitodoWrapper.getDmdSecs()) {
                    if (mdSecType.getID().equals(dmdSec.getID())) {
                        index = this.metsKitodoWrapper.getDmdSecs().indexOf(dmdSec);
                        break;
                    }
                }
                int divTypeIndex = -1;
                for (DivType dt : this.metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv()) {
                    if (dt.getID().equals(divType.getID())) {
                        divTypeIndex = this.metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv().indexOf(dt);
                    }
                }
                if (index != -1 && divTypeIndex != -1) {
                    this.metsKitodoWrapper.getDmdSecs().remove(index);
                    this.metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv().remove(divTypeIndex);
                    saveMets(fileURI);
                } else {
                    System.err.println("Unable to find DivType or DmDSec of given partial job. Aborting removal of partial job!");
                }
            }
        }
    }

    @Override
    public TreeNode getPhysicalStructMap() {
        TreeNode physicalMap = new DefaultTreeNode("root", null);

        DivType boundBook = this.metsKitodoWrapper.getPhysicalStructMap().getDiv();
        TreeNode visibleRoot = new DefaultTreeNode(boundBook, physicalMap);

        visibleRoot.setSelected(true);

        List<DivType> boundBookChildren = boundBook.getDiv();

        TreeNode boundBookAncestorsNode = convertDivTypeToTreeNode(boundBookChildren, visibleRoot);

        if (Objects.nonNull(boundBookAncestorsNode)) {
            visibleRoot.getChildren().add(boundBookAncestorsNode);
        }

        return physicalMap;
    }

    private TreeNode convertDivTypeToTreeNode(List<DivType> divTypes, TreeNode parent) {
        TreeNode treeNode = null;

        for (DivType child : divTypes) {
            treeNode = new DefaultTreeNode(child, parent);
            convertDivTypeToTreeNode(child.getDiv(), treeNode);
        }

        return treeNode;
    }

    @Override
    public LinkedList<HashMap<String, String>> getMetadataOfDivType(Object divTypeObject) throws ClassCastException {
        if (!(divTypeObject instanceof DivType)) {
            throw new ClassCastException("Argument has wrong class '" + divTypeObject.getClass().getName() + "' (DivType expected)");
        }
        DivType divType = (DivType) divTypeObject;

        if (divType.getDMDID().size() > 0) {
            MdSecType mdSecType = (MdSecType) divType.getDMDID().get(0);

            for (DmdSec dmdSec : this.metsKitodoWrapper.getDmdSecs()) {
                if (!divType.getDMDID().isEmpty() && dmdSec.getID().equals(mdSecType.getID())) {
                    return getMetadataOfDmdSec(dmdSec);
                }
            }
        }

        return new LinkedList<>();
    }

    @Override
    public String createImageProcessingParameterFileContent(String previewFolder, String mediaViewFolder) {
        StringBuilder fileContent = new StringBuilder();
        for (DivType divType : this.metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv()) {
            if (Objects.nonNull(divType.getTYPE()) && divType.getTYPE().equals(PARTIAL_JOB)) {
                String profile = determineMetadataValueOfDivType(divType, PROFILE).replaceAll("_\\d+", "");
                String manualImageProcessing = determineMetadataValueOfDivType(divType, MANUAL_IMAGE_PROCESSING);
                for (DivType partialJobChild : divType.getDiv()) {
                    assert PAGE.equals(partialJobChild.getTYPE()) || TRACK.equals(partialJobChild.getTYPE()) || OTHER.equals(partialJobChild.getTYPE());
                    for (DivType.Fptr filePointer : partialJobChild.getFptr()) {
                        FileType fileType = (FileType)filePointer.getFILEID();
                        // skip files in "THUMBS" and "MAX" groups!
                        for (MetsType.FileSec.FileGrp currentFileGroup : this.metsKitodoWrapper.getFileSec().getFileGrp()) {
                            if (!Objects.equals(currentFileGroup.getUSE(), previewFolder)
                                    && !Objects.equals(currentFileGroup.getUSE(), mediaViewFolder)
                                    && currentFileGroup.getFile().contains(fileType)) {
                                for (FileType.FLocat fLocat : fileType.getFLocat()) {
                                    String fileLocation = fLocat.getHref();
                                    String imageFileName = fileLocation.substring(fileLocation.lastIndexOf("/") + 1);
                                    fileContent
                                            .append(imageFileName)
                                            .append(",")
                                            .append(profile)
                                            .append(",")
                                            .append(manualImageProcessing)
                                            .append(System.lineSeparator());
                                }
                            }
                        }
                    }
                }
            }
        }
        return String.valueOf(fileContent);
    }

    private MetsType.FileSec.FileGrp getLocalFileGrop() {
        for (MetsType.FileSec.FileGrp fileGrp : this.metsKitodoWrapper.getFileSec().getFileGrp()) {
            if (fileGrp.getUSE().equals("LOCAL")) {
                return fileGrp;
            }
        }
        return null;
    }

    @Override
    public List<String> deletePartialJobImages(String partialJobId, Set<String> uses) {
        List<String> partialJobImagesHref = new ArrayList<>();
        DivType partialJob = null;

        for (DivType partialJobIterator : this.metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv()) {
            if (partialJobIterator.getID().equals(partialJobId)) {
                partialJob = partialJobIterator;
                break;
            }
        }

        if (Objects.isNull(partialJob)) {
            return null;
        }

        List<DivType> pages = partialJob.getDiv();

        //delete smLink
        List<String> pageIds = new ArrayList<>();
        for (DivType page : pages) {
            pageIds.add(page.getID());
        }
        deleteSmLinks(pageIds);

        //deleteFile Sec
        for (DivType page : pages) {
            for (DivType.Fptr fptr : page.getFptr()) {
                FileType file = (FileType) fptr.getFILEID();
                for (FileType.FLocat flocat : file.getFLocat()) {
                    partialJobImagesHref.add(flocat.getHref());
                }
                for (String use : uses) {
                    this.metsKitodoWrapper.getFileSec().getFileGroup(use).getFile().remove(file);
                }
            }
        }

        // remove fileGrp when it is empty
        for (String use : uses) {
            if (this.metsKitodoWrapper.getFileSec().getFileGroup(use).getFile().isEmpty()) {
                this.metsKitodoWrapper.getFileSec().getFileGrp().remove(this.metsKitodoWrapper.getFileSec().getFileGroup(use));
            }
        }


        //delete partial job children
        partialJob.getDiv().clear();

        return partialJobImagesHref;
    }

    /**
     * Remove preview images from specified fileGroup.
     * This method iterates over all partial jobs, their pages and the existing filePointers.
     * All filePointers and files for the specified fileGroup are removed as well as pages without any files associated.
     *
     * @param fileGroupName Name of the fileGroup as java.lang.String
     */
    @Override
    public void removePreviewImages(String fileGroupName) {
        List<DivType> partialJobs = this.metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv();
        MetsType.FileSec.FileGrp fileGroup = null;

        // find fileGroup
        for (MetsType.FileSec.FileGrp currentFileGroup : this.metsKitodoWrapper.getFileSec().getFileGrp()) {
            if (fileGroupName.equals(currentFileGroup.getUSE())) {
                fileGroup = currentFileGroup;
            }
        }

        // iterate partial jobs, their pages and filePointers
        for (DivType partialJob : partialJobs) {

            List<DivType> pagesToBeRemoved = new ArrayList<>();
            for (DivType page : partialJob.getDiv()) {

                List<DivType.Fptr> filePointersToBeRemoved = new ArrayList<>();
                for (DivType.Fptr filePointer : page.getFptr()) {
                    FileType file = (FileType) filePointer.getFILEID();
                    if ((Objects.nonNull(fileGroup) && fileGroup.getFile().contains(file))) {
                        filePointersToBeRemoved.add(filePointer);
                    }
                }
                page.getFptr().removeAll(filePointersToBeRemoved);

                if (page.getFptr().isEmpty()) {
                    pagesToBeRemoved.add(page);
                }
            }
            partialJob.getDiv().removeAll(pagesToBeRemoved);
        }

        if (Objects.nonNull(fileGroup)) {
            this.metsKitodoWrapper.getFileSec().getFileGrp().remove(fileGroup);
        }
    }

    @Override
    public List<String> getPartialJobMedia(String partialJobId, Set<String> uses) {
        List<String> partialJobImages = new ArrayList<>();
        List<FileType> files = getAllFilesFromFileGroups(uses);
        for (DivType partialJobIterator : this.metsKitodoWrapper.getPhysicalStructMap().getDiv().getDiv()) {
            if (partialJobIterator.getID().equals(partialJobId)) {
                for (DivType page : partialJobIterator.getDiv()) {
                    for (DivType.Fptr fptr : page.getFptr()) {
                        FileType file = (FileType) fptr.getFILEID();
                        if (files.contains(file)) {
                            for (FileType.FLocat flocat : file.getFLocat()) {
                                if (!partialJobImages.contains(flocat.getHref())) {
                                    partialJobImages.add(flocat.getHref());
                                }
                            }
                        }
                    }
                }
                return partialJobImages;
            }
        }
        return partialJobImages;
    }

    @Override
    public List<String> getLogicalElementMedia(String elementId, Set<String> uses) {
        return this.metsKitodoWrapper.getStructLink().getSmLinks().stream()
                .filter(smLink -> Objects.equals(elementId, smLink.getFrom()))
                .map(StructLinkType.SmLink::getTo)
                .collect(Collectors.toList());
    }

    private List<FileType> getAllFilesFromFileGroups(Set<String> uses) {
        List<FileType> files = new ArrayList<>();
        uses.stream().map(use -> this.metsKitodoWrapper.getFileSec().getFileGroup(use).getFile()).forEach(files::addAll);
        // remove file groups if empty to prevent corrupt METS
        for (String use : uses) {
            if (this.metsKitodoWrapper.getFileSec().getFileGroup(use).getFile().isEmpty()) {
                this.metsKitodoWrapper.getFileSec().getFileGrp().remove(this.metsKitodoWrapper.getFileSec().getFileGroup(use));
            }
        }
        return files;
    }

    private void deleteSmLinks(List<String> pageIds) {
        List<StructLinkType.SmLink> smLinks = this.metsKitodoWrapper.getStructLink().getSmLinks();
        List<StructLinkType.SmLink> objectsToRemove = new ArrayList<>();

        for (String pageId : pageIds) {
            for (StructLinkType.SmLink smLink : smLinks) {
                if (smLink.getTo().equals(pageId)) {
                    objectsToRemove.add(smLink);
                }
            }
        }

        smLinks.removeAll(objectsToRemove);
    }

    private String determineMetadataValueOfDivType(DivType partialJob, String metadataName) {
        if (partialJob.getDMDID().size() > 0) {
            MdSecType mdSecType = (MdSecType) partialJob.getDMDID().get(0);

            for (DmdSec dmdSec : this.metsKitodoWrapper.getDmdSecs()) {
                if (!partialJob.getDMDID().isEmpty() && dmdSec.getID().equals(mdSecType.getID())) {
                    for (MetadataType metadataType : dmdSec.getKitodoType().getMetadata()) {
                        if (metadataType.getName().equals(metadataName)) {
                            // checked checkboxes seem to be added as "metadata" nodes without explicit text content!
                            if (MANUAL_IMAGE_PROCESSING.equals(metadataName)) {
                                return "true";
                            }
                            return metadataType.getValue();
                        }
                    }
                }
            }
        }

        // Return default value "false" for manual image processing metadata because value "false" is not explicitly
        // saved to meta.xml for unchecked checkboxes!
        if (MANUAL_IMAGE_PROCESSING.equals(metadataName)) {
            return "false";
        } else {
            return "";
        }
    }

    private LinkedList<HashMap<String, String>> getMetadataOfDmdSec(DmdSec dmdSec) {
        LinkedList<HashMap<String, String>> metadata = new LinkedList<>();
        for (MetadataType metadataType : dmdSec.getKitodoType().getMetadata()) {
            HashMap<String, String> currentMetadata = new HashMap<>();
            currentMetadata.put(metadataType.getName(), metadataType.getValue());
            metadata.add(currentMetadata);
        }
        return metadata;
    }

    private void saveMets(URI xmlFile) {
        try {
            MetsKitodoWriter metsKitodoWriter = new MetsKitodoWriter();
            metsKitodoWriter.writeSerializedToFile(this.metsKitodoWrapper.getMets(), xmlFile);
        } catch (JAXBException | DatatypeConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }
}
