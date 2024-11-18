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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.kitodo.api.dataeditor.DataEditorInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.KitodoConfig;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.relayserver.helper.JobHelper;
import org.kitodo.production.services.relayserver.helper.XMLHelper;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class DocketsHandler {
    private static final String PARTIAL_JOBS_NUMBER = "/mets/structMap[@TYPE='PHYSICAL']/div[@TYPE='BoundBook']/div[@TYPE='partialJob']";
    private static final String Strucure_Elements_NUMBER = "/mets/structMap[@TYPE='LOGICAL']//div[@TYPE='dossier'][1]//div";
    private static final String IMAGE_PATH_MAPPING_FILE_PATH = "images/archive/imagePathMapping";
    private static String TASK_PREPARE_DOSSIER = "Dossier vorbereiten";
    private static String TASK_EVALUATE_DOCKETS = "Strukturtrennblatterkennung";

    private DataEditorInterface dataEditorInterface;
    private Process process;
    private List<String> possibleDockets = Arrays.asList("Dokument", "RUECKSEITE", "Subdossier", "Teilauftrag", "Umschlag");
    private List<String> allowedDocumentInstallmentPredecessors = Arrays.asList("document", "document_installment");
    private List<Node> allPartialJobs;
    private Set<String> sourceFileUses;
    private final Map<String, String> imagePathMapping = new HashMap<>();
    public HashMap<String, Integer> partialJobNumberOfDocketsMapping;

    public DocketsHandler(Process process) {
        this.partialJobNumberOfDocketsMapping = new HashMap<>();
        this.process = process;
        this.dataEditorInterface = loadDataEditorModule();
        this.allPartialJobs = getAllPartialJobs();
        sourceFileUses = process.getProject().getFolders().stream()
                .filter(folder -> !folder.equals(process.getProject().getPreview()) && !folder.equals(process.getProject().getMediaView()))
                .map(Folder::getFileGroup)
                .collect(Collectors.toSet());
    }

    private DataEditorInterface loadDataEditorModule() {
        try {
            String fileURI = KitodoConfig.getKitodoDataDirectory() + ServiceManager.getFileService().getMetadataFilePath(this.process);
            KitodoServiceLoader<DataEditorInterface> kitodoServiceLoader = new KitodoServiceLoader<>(DataEditorInterface.class);
            DataEditorInterface dataEditorInterface = kitodoServiceLoader.loadModule();
            dataEditorInterface.readData(Paths.get(fileURI).toUri());
            return dataEditorInterface;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*public String getLogicalID(String dmdID) {
        String fileURI = KitodoConfig.getKitodoDataDirectory() + ServiceManager.getFileService().getMetadataFilePath(this.process);
        Document metaXml = XMLHelper.loadXMLFile(fileURI);
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            Element logicalDivNode = (Element) xPath.compile("/mets/structMap[@TYPE='LOGICAL']//div[@DMDID='" + dmdID + "']")
                        .evaluate(metaXml, XPathConstants.NODE);
            String logicalID = logicalDivNode.getAttribute("ID");
            if (logicalID.equals("")) {
                this.dataEditorInterface.generateLogIds(Paths.get(fileURI).toUri());
                metaXml = XMLHelper.loadXMLFile(fileURI);
                logicalDivNode = (Element) xPath.compile("/mets/structMap[@TYPE='LOGICAL']//div[@DMDID='" + dmdID + "']")
                        .evaluate(metaXml, XPathConstants.NODE);
                logicalID = logicalDivNode.getAttribute("ID");
            }
            return logicalID;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return "";
        }

    }*/

    public void addMedia(List<String> physicalpaths, String physicalDivId, String logicalID, Map<String, String> mimeTypeMapping,
                         Map<String, String> suffixMapping)  throws Exception {
        this.dataEditorInterface.insertMediaFilesAndAddStructLink(physicalpaths, physicalDivId, logicalID, mimeTypeMapping, suffixMapping);
    }

    public void linkMediaFromPrecedingElement(String elementId) {
        this.dataEditorInterface.linkMediaFromPrecedingElement(elementId);
    }

    public String getParentDossierId(String dossierId) {
        return this.dataEditorInterface.getParentDossierId(dossierId);
    }

    /**
     * Returns the file type.
     * @param tiffPath path to file
     * @return String containing "IMAGE" or a docket type of possibleDockets
     * @throws IOException Exception is thrown when file cannot be accessed
     */
    public String getTiffFileType(String tiffPath) throws IOException {
        try (InputStream inputStream = new FileInputStream(tiffPath)) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            bufferedImage = bufferedImage.getSubimage(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight() / 2);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result;
            String code;
            List<BarcodeFormat> possibleFormats = Arrays.asList(BarcodeFormat.CODE_128, BarcodeFormat.QR_CODE);
            Hashtable<DecodeHintType, Object> hint = new Hashtable<DecodeHintType, Object>();
            hint.put(DecodeHintType.POSSIBLE_FORMATS, possibleFormats);
            try {
                result = new MultiFormatReader().decode(bitmap, hint);
                code = result.getText();
            } catch (NotFoundException e) {
                hint.put(DecodeHintType.TRY_HARDER, true);
                try {
                    result = new MultiFormatReader().decode(bitmap, hint);
                    code = result.getText();
                } catch (NotFoundException e1) {
                    return "IMAGE";
                }
            }
            if (!isDocket(code)) {
                code = "IMAGE";
            }
            return code;
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    private boolean isDocket(String code) {
        for (String docketType : possibleDockets) {
            if (code.contains(docketType)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getInputMedia(List<String> mediaFileTypes) {
        List<String> mediaPaths = new LinkedList<>();
        String subdir = ConfigCore.getParameter("scanned_images_subdir_name");
        URI imagesPath = ServiceManager.getFileService().getImagesDirectory(process).resolve(subdir);
        java.nio.file.Path imagesDir = Paths.get(KitodoConfig.getKitodoDataDirectory() + imagesPath);
        if (imagesDir.toFile().exists() && imagesDir.toFile().isDirectory()) {
            mediaPaths = JobHelper.getMediaPaths(imagesDir, mediaFileTypes);
        }
        return mediaPaths;
    }

    //sometimes the Root Element has an "ID=LOG_ROOT" and sometimes an "ID=LOG_0000"
    public String getDossierElementLogicalID() {
        try {
            String fileURI = KitodoConfig.getKitodoDataDirectory() + ServiceManager.getFileService().getMetadataFilePath(this.process);
            Document metaXml = XMLHelper.loadXMLFile(fileURI);
            XPath xPath = XPathFactory.newInstance().newXPath();
            Element logicalDivNode = (Element) xPath.compile("/mets/structMap[@TYPE='LOGICAL']//div[@TYPE='dossier']")
                    .evaluate(metaXml, XPathConstants.NODE);
            return logicalDivNode.getAttribute("ID");
        } catch (XPathExpressionException | IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getBoundBookElementPhysicalID() {
        return this.dataEditorInterface.getBoundBookElementPhysicalID();
    }

    public String addnewElement(String elementType, String dossierId, String physicalDivId) {
        return this.dataEditorInterface.addNewStructElement(elementType, dossierId, physicalDivId);
    }

    public String getUpdatedXmlString() {
        return this.dataEditorInterface.getUpdatedXmlString();
    }

    public void renameAndMoveTiffs(int start, List<String> imagesPaths) throws IOException {

        String imagesDir = KitodoConfig.getKitodoDataDirectory() + process.getId() + File.separator;

        for (Folder folder : process.getProject().getFolders()) {
            if (folder.getFileGroup().equals("LOCAL")) {
                imagesDir = imagesDir + folder.getRelativePath() + File.separator;
                break;
            }
        }

        if (imagesPaths.isEmpty()) {
            return;
        }

        for (String imagePath : imagesPaths) {
            renameAndMoveTiffFile(imagePath, imagesDir, start);
            start++;
        }
    }

    private void renameAndMoveTiffFile(String sourcePath, String targetDir, int index) throws IOException {
        String newImageName = String.format("%08d", index).concat(".").concat(FilenameUtils.getExtension(sourcePath));
        String newPath = targetDir + newImageName;
        moveAndMapFile(Paths.get(URI.create("file://" + sourcePath)), Paths.get(URI.create("file://" + newPath)), false);
        this.dataEditorInterface.editHrefFLocat(sourcePath, "file:" + newPath, sourceFileUses);
    }

    public void renameNextPartialJobTiffs(int start, List<String> imagesPaths) throws IOException {
        if (imagesPaths.isEmpty()) {
            return;
        }

        String processDir = KitodoConfig.getKitodoDataDirectory() + process.getId() + File.separator;
        String firstFileName = new File(imagesPaths.get(0)).getName();
        int firstFileIndex = Integer.parseInt(firstFileName.replace("." + FilenameUtils.getExtension(firstFileName), ""));
        // if first image has already "start" index as name no rename is necessary
        if (start < firstFileIndex) {
            // images should be renamed to smaller indices
            for (String imagePath : imagesPaths) {
                String absolutePath = normalizePath(imagePath, processDir);
                String newImageName = String.format("%08d", start).concat(".").concat(FilenameUtils.getExtension(imagePath));
                String newPath = absolutePath.replace(imagePath.substring(imagePath.lastIndexOf("/") + 1), newImageName);
                moveAndMapFile(Paths.get(absolutePath), Paths.get(newPath), true);
                this.dataEditorInterface.editHrefFLocat(imagePath, newPath, sourceFileUses);
                start++;
            }
        } else if (start > firstFileIndex) {
            // images should be renamed to larger indices
            int reverseStart = start + imagesPaths.size() - 1;
            Collections.reverse(imagesPaths);
            for (String imagePath : imagesPaths) {
                String absolutePath = normalizePath(imagePath, processDir);
                String newImageName = String.format("%08d", reverseStart).concat(".").concat(FilenameUtils.getExtension(imagePath));
                String newPath = absolutePath.replace(imagePath.substring(imagePath.lastIndexOf("/") + 1), newImageName);
                moveAndMapFile(Paths.get(absolutePath), Paths.get(newPath), true);
                this.dataEditorInterface.editHrefFLocat(imagePath, newPath, sourceFileUses);
                reverseStart--;
            }
        }
    }

    public void deletePartialJobImages(String partialJobId) throws IOException, NoSuchElementException {
        Node partialJob = allPartialJobs.get(getCurrentPartialJob(partialJobId));
        NodeList existingImages = partialJob.getChildNodes();

        if (Objects.nonNull(existingImages)) {
            List<String> imagesHref = this.dataEditorInterface.deletePartialJobImages(partialJobId, sourceFileUses);
            for (String imagePath : imagesHref) {
                ServiceManager.getFileService().delete(URI.create(imagePath.replace("file:", "file://")));
            }
        }
    }

    public int getCurrentPartialJob(String id) throws NoSuchElementException {
        for (int i = 0; i < allPartialJobs.size(); i++) {
            Node node = allPartialJobs.get(i);
            if (((Element) node).getAttribute("ID").equals(id)) {
                return i;
            }
        }
        throw new NoSuchElementException("No partial job found for id " + id + ".");
    }

    public int getStartIndex(String id) throws NoSuchElementException {
        List<Node> previousPartialJobList = getPreviousPartialJobs(getCurrentPartialJob(id));
        int numberOfImages = 1;
        for (Node previousPartialJob : previousPartialJobList) {
            numberOfImages += (this.dataEditorInterface.getPartialJobMedia(((Element)previousPartialJob).getAttribute("ID"), sourceFileUses)).size();
        }
        return numberOfImages;
    }

    public List<Node> getPreviousPartialJobs(int index) {
        if (index <= 0) {
            return new ArrayList<>();
        }
        return allPartialJobs.subList(0, index);
    }

    public void renameNextPartialJobImages(String currentPartialJobId, int start) throws IOException, NoSuchElementException {
        List<String> nextImagesPaths = new ArrayList<>();
        List<Node> nextPartialJobsList = getNextPartialJobs(getCurrentPartialJob(currentPartialJobId));
        for (Node nextPartialJob : nextPartialJobsList) {
            nextImagesPaths.addAll(this.dataEditorInterface.getPartialJobMedia(((Element)nextPartialJob).getAttribute("ID"), sourceFileUses));
        }
        if (!nextImagesPaths.isEmpty()) {
            renameNextPartialJobTiffs(start, nextImagesPaths);
        }
    }

    public List<Node> getNextPartialJobs(int index) {
        if (index >= allPartialJobs.size() - 1 ) {
            return new ArrayList<>();
        }
        return allPartialJobs.subList(index + 1, allPartialJobs.size());
    }

    private List<Node> getAllPartialJobs() {
        List<Node> childrenList = new ArrayList<>();
        try {
            String fileURI = KitodoConfig.getKitodoDataDirectory() + ServiceManager.getFileService().getMetadataFilePath(this.process);
            Document metaXml = XMLHelper.loadXMLFile(fileURI);
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList partialJobNodes = (NodeList) xPath.compile("/mets/structMap[@TYPE='PHYSICAL']/div[@TYPE='BoundBook']/div").evaluate(metaXml, XPathConstants.NODESET);
            if (Objects.nonNull(partialJobNodes)) {
                for (int i = 0; i < partialJobNodes.getLength(); i++) {
                    childrenList.add(partialJobNodes.item(i));
                }
                return childrenList;
            }
        } catch (IOException | XPathExpressionException e) {
            JobHelper.jumpToFallbackTask(process, e.getLocalizedMessage(), TASK_EVALUATE_DOCKETS, TASK_PREPARE_DOSSIER);
        }
        return childrenList;
    }

    public void deleteOldImages(List<String> imagePaths) throws IOException {
        for (String tiffPath : imagePaths) {
            ServiceManager.getFileService().delete(URI.create("file://" + tiffPath));
        }
    }

    void removePreviewImages() {
        this.dataEditorInterface.removePreviewImages(this.process.getProject().getPreview().getFileGroup());
        this.dataEditorInterface.removePreviewImages(this.process.getProject().getMediaView().getFileGroup());
    }

    public int getExpectedNumberOfImages(String partialJobId) throws XPathExpressionException, IOException {
        String fileURI = KitodoConfig.getKitodoDataDirectory() + ServiceManager.getFileService().getMetadataFilePath(this.process);
        Document metaXml = XMLHelper.loadXMLFile(fileURI);
        XPath xPath = XPathFactory.newInstance().newXPath();
        Element partialJob = (Element) xPath.compile("/mets/structMap[@TYPE='PHYSICAL']/div[@TYPE='BoundBook']/div[@ID='" + partialJobId + "']").evaluate(metaXml, XPathConstants.NODE);
        Element numberOfPages = (Element) xPath.compile("/mets/dmdSec[@ID='" + partialJob.getAttribute("DMDID") + "']//metadata[@name='numberImages']").evaluate(metaXml, XPathConstants.NODE);
        return Integer.parseInt(numberOfPages.getTextContent());
    }

    public void addInfoComment(String message) {
        Comment comment = new Comment();
        comment.setMessage(message);
        comment.setProcess(this.process);
        comment.setType(CommentType.INFO);
        comment.setCreationDate(new Date());
        try {
            ServiceManager.getCommentService().saveToDatabase(comment);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    public int getNumberOfStrucureElement() throws XPathExpressionException, IOException {
        return getElementsNumberFromMetaXml(Strucure_Elements_NUMBER);
    }

    public int getNumberOfPartialJob() throws XPathExpressionException, IOException {
        return getElementsNumberFromMetaXml(PARTIAL_JOBS_NUMBER);
    }

    private int getElementsNumberFromMetaXml(String xPathAsString) throws XPathExpressionException, IOException {
        String fileURI = KitodoConfig.getKitodoDataDirectory() + ServiceManager.getFileService().getMetadataFilePath(this.process);
        Document metaXml = XMLHelper.loadXMLFile(fileURI);
        XPath xPath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xPath.compile("count(" + xPathAsString + ")");
        Number result = (Number) expr.evaluate(metaXml, XPathConstants.NUMBER);
        return result.intValue();
    }

    public boolean isLogElementPresent(String logId) {
        return this.dataEditorInterface.isLogElementPresent(logId);
    }

    public boolean isPhysElementPresent(String physId) {
        return this.dataEditorInterface.isPhysElementPresent(physId);
    }

    public List<String> checkPartialJobsCompleteness() {
        List<String> comments = new ArrayList<>();
        for (Node partialJob : getAllPartialJobs()) {
            String physicalDivId = ((Element) partialJob).getAttribute("ID");
            try {
                if (Objects.isNull(physicalDivId)) {
                    comments.add("Could not check partial job completeness: Partial job without ID found!");
                    break;
                }

                int expectedNumberOfImages = getExpectedNumberOfImages(physicalDivId);
                int actualNumberOfImages = getActualNumberOfImagesPartialJob(physicalDivId);

                if (!Objects.equals(expectedNumberOfImages, actualNumberOfImages)) {
                    comments.add("Number of scanned images is not correct for partial job with id '" + physicalDivId
                            + "': Found " + actualNumberOfImages
                            + ", expected " + expectedNumberOfImages + ".");
                }
            } catch (XPathExpressionException | IOException e) {
                comments.add("Could not check partial job completeness: Could not get number of partial job images: " + e.getMessage());
            } catch (NumberFormatException | NullPointerException e) {
                comments.add("Could not check partial job completeness: "
                        + "Could not get expected number of pages for partialJob with id \"" + physicalDivId + "\" from meta.xml.");
            }

        }
        return comments;
    }

    public int getActualNumberOfImagesPartialJob(String physicalDivId) {
        return this.dataEditorInterface.getPartialJobMedia(physicalDivId, sourceFileUses).size();
    }

    public int getActualNumberOfImagesLogical(String logicalDivId) {
        return this.dataEditorInterface.getLogicalElementMedia(logicalDivId, sourceFileUses).size();
    }

    /**
     * Check whether the element is preceded by an allowed predecessor for elements of type "document_installment".
     * @param documentId id specifying the element of which the predecessor should be checked
     * @return type of the preceding element as java.lang.String
     */
    public String checkDocumentInstallmentOrder(String documentId) {
        String documentType = this.dataEditorInterface.getPrecedingDocumentType(documentId);
        if (allowedDocumentInstallmentPredecessors.contains(documentType)) {
            return "";
        }
        return "Element of type 'document_installment' not allowed after " + documentType;
    }

    /**
     * Check whether the element of type "umschlag" is preceded by an another element.
     * This type of element should always be the first on its level.
     * @param coverId id specifying the element of which the predecessor should be checked
     * @return type of the preceding element as java.lang.String
     */
    public String checkCoverOrder(String coverId) {
        String documentType = this.dataEditorInterface.getPrecedingDocumentType(coverId);
        if (Objects.equals(documentType, "")) {
            return "";
        }
        return "Element of type 'umschlag' not allowed after " + documentType;
    }

    public Map<String, String> getImagePathMapping() {
        return imagePathMapping;
    }

    /**
     * Move file and add Entry to the imagePathMapping.
     * @param source Current path of the file
     * @param target Path where the file should be moved
     * @param renameExistingFile flag whether an existing file already present in the imagePathMapping is renamed. If true, the has to be updated.
     * @return The target path after successfully moving the file
     * @throws IOException if the file cannot be moved
     */
    public Path moveAndMapFile(Path source, Path target, boolean renameExistingFile) throws IOException {
        Path targetPath = Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        if (renameExistingFile) {
            if (imagePathMapping.containsKey(source.toString())) {
                imagePathMapping.put(target.toString(), imagePathMapping.get(source.toString()));
                imagePathMapping.remove(source.toString());
            }
        } else {
            imagePathMapping.put(target.toString(), source.toString());
        }
        moveMatchingPremisFile(source, target);
        return targetPath;
    }

    /**
     * Move file and remove Entry from imagePathMapping.
     * @param source Current path of the file
     * @param target Path where the file should be moved
     * @return The target path after successfully moving the file
     * @throws IOException if the file cannot be moved
     */
    public Path moveAndRestoreFile(Path source, Path target) throws IOException {
        Path targetPath = Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        imagePathMapping.remove(source.toString().replaceFirst("^file:///", "/"));
        moveMatchingPremisFile(source, target);
        return targetPath;
    }

    /**
     * Check if a matching PREMIS file exists for the specified source path and move it to the specified target path.
     * The matching PREMIS file will be moved to the specified target path where the file extension is replaced with "_PREMIS.xml".
     */
    private void moveMatchingPremisFile(Path source, Path target) throws IOException {
        Path possiblyMatchingPremisPath = Paths.get(source.toString().replaceFirst("(\\.[^.]+)$", "_PREMIS.xml"));
        File possiblyMatchingPremisFile = new File(possiblyMatchingPremisPath.toString());
        if (possiblyMatchingPremisFile.exists() && !possiblyMatchingPremisFile.isDirectory()) {
            Path targetPremisPath = Paths.get(target.toString().replaceFirst("(\\.[^.]+)$", "_PREMIS.xml"));
            Files.move(possiblyMatchingPremisPath, targetPremisPath, StandardCopyOption.ATOMIC_MOVE);
        }
    }

    /**
     * Load the imagePathMapping file for the current process.
     * @return content of the imagePathMapping file as Map<String, String>
     */
    public Map<String, String> loadImagePathMapping() throws IOException {
        String imagePathMappingFile = KitodoConfig.getKitodoDataDirectory() + process.getId() + File.separator + IMAGE_PATH_MAPPING_FILE_PATH;
        List<String> fileContent = Files.readAllLines(Paths.get(imagePathMappingFile));
        Map<String, String> mapping = new HashMap<>();
        for (String line : fileContent) {
            String[] pair = line.split(",");
            if (pair.length == 2) {
                mapping.put(pair[0], pair[1]);
            }
        }
        return mapping;
    }

    /**
     * Write content of the passed Map to the imagePathMapping file of the current process
     * @param mapping Map containing a mapping between original file names (key) and current file names after moving and renaming the files (value)
     * @throws IOException if the file cannot be written
     */
    public void writeImagePathMapping(Map<String, String> mapping) throws IOException {
        String fileContent = mapping.keySet().stream()
                .map(key -> key + "," + mapping.get(key))
                .collect(Collectors.joining("\n"));
        String imagePathMappingFile = KitodoConfig.getKitodoDataDirectory() + process.getId() + File.separator + IMAGE_PATH_MAPPING_FILE_PATH;
        Files.write(Paths.get(imagePathMappingFile), fileContent.getBytes());
    }

    private String normalizePath(String path, String parentDir) {
        path = path.replaceFirst("^file://", "").replaceFirst("^file:", "");
        return path.startsWith("/") ? path : (parentDir.endsWith("/") ? parentDir : parentDir + File.separator) + path;
    }
}
