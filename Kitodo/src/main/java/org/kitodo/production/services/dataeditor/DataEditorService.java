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

package org.kitodo.production.services.dataeditor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataeditor.DataEditorInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.ComplexMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.api.dataformat.LogicalDivision;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.View;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.KitodoConfig;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.forms.dataeditor.DataEditorForm;
import org.kitodo.production.forms.dataeditor.StructurePanel;
import org.kitodo.production.forms.dataeditor.StructureTreeNode;
import org.kitodo.production.helper.Helper;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.TreeNode;
import org.w3c.dom.Document;


public class DataEditorService {

    private static final Logger logger = LogManager.getLogger(DataEditorService.class);
    private static final String IMAGE_PROCESSING_PARAMETERS_FILENAME = "imageProcessingParameters";
    private DataEditorInterface dataEditor;

    /**
     * Reads the data of a given file in xml format. The format of that file
     * needs to be the corresponding to the one which is referenced by the data
     * editor module as data format module.
     *
     * @param xmlFileUri
     *            The path to the metadata file as URI.
     */
    public void readData(URI xmlFileUri) throws IOException {
        DataEditorInterface dataEditor = loadDataEditorModule();
        URI xsltFile = getXsltFileFromConfig();
        dataEditor.readData(xmlFileUri, xsltFile);
    }

    private DataEditorInterface loadDataEditorModule() {
        KitodoServiceLoader<DataEditorInterface> serviceLoader = new KitodoServiceLoader<>(DataEditorInterface.class);
        return serviceLoader.loadModule();
    }

    private URI getXsltFileFromConfig() {
        String path = getXsltFolder();
        String file = ConfigCore.getParameter(ParameterCore.XSLT_FILENAME_METADATA_TRANSFORMATION);
        return Paths.get(path + file).toUri();
    }

    private String getXsltFolder() {
        return ConfigCore.getParameter(ParameterCore.DIR_XSLT);
    }

    public void createData(String documentType) {
        DataEditorInterface dataEditor = loadDataEditorModule();
        try {
            dataEditor.createData(documentType);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String createMetaXml(Document kitodoMetadata) {
        DataEditorInterface dataEditor = loadDataEditorModule();
        return dataEditor.createKitodoDocument(kitodoMetadata);

    }

    public void createImageProcessingParameterFile(Process process) throws IOException {
        initializeDataEditor(process);
        String previewFolder = process.getProject().getPreview().getFileGroup();
        String mediaViewFolder = process.getProject().getMediaView().getFileGroup();
        String fileContent = dataEditor.createImageProcessingParameterFileContent(previewFolder, mediaViewFolder);
        String imageProcessingParameterFilepath = getImageProcessingParameterFilepath(process);
        Files.write(Paths.get(imageProcessingParameterFilepath), fileContent.getBytes());
    }

    private void initializeDataEditor(Process process) throws IOException {
        if (Objects.isNull(dataEditor)) {
            dataEditor = loadDataEditorModule();
        }
        if (Objects.nonNull(process)) {
            String fileURI = KitodoConfig.getKitodoDataDirectory() + ServiceManager.getFileService().getMetadataFilePath(process);
            dataEditor.readData(Paths.get(fileURI).toUri());
        }
    }

    /**
     * Retrieve and return list of metadata keys that are used for displaying title information in the metadata editors
     * structure and gallery panels from the Kitodo configuration file.
     *
     * @return list of title metadata keys
     */
    public static List<String> getTitleKeys() {
        return Arrays.stream(ConfigCore.getParameter(ParameterCore.TITLE_KEYS, "").split(","))
                .map(String::trim).collect(Collectors.toList());
    }

    /**
     * Retrieve and return filepath of image processing parameter file for given process.
     *
     * @param process Process for which the image processing parameter file path is returned
     * @return image processing parameter file path
     * @throws IOException thrown when process directory cannot be read
     */
    public static String getImageProcessingParameterFilepath(Process process) throws IOException {
        // TODO: there should be a better way to get the absolute path of the process directory!
        File metadataFile = new File(KitodoConfig.getKitodoDataDirectory() + ServiceManager.getFileService().getMetadataFilePath(process));
        return metadataFile.getParent() + File.separator + IMAGE_PROCESSING_PARAMETERS_FILENAME;
    }

    /**
     * Retrieve and return title value from given LogicalDivision.
     *
     * @param element LogicalDivision for which the title value is returned.
     * @param metadataTitleKey as a String that its value will be displayed.
     * @return title value of given element
     */
    public static String getTitleValue(LogicalDivision element, String metadataTitleKey) {
        String[] metadataPath = metadataTitleKey.split("@");
        int lastIndex = metadataPath.length - 1;
        Collection<Metadata> metadata = element.getMetadata();
        for (int i = 0; i < lastIndex; i++) {
            final String metadataKey = metadataPath[i];
            metadata = metadata.stream()
                    .filter(currentMetadata -> Objects.equals(currentMetadata.getKey(), metadataKey))
                    .filter(MetadataGroup.class::isInstance).map(MetadataGroup.class::cast)
                    .flatMap(metadataGroup -> metadataGroup.getMetadata().stream())
                    .collect(Collectors.toList());
        }
        Optional<String> metadataTitle = metadata.stream()
                .filter(currentMetadata -> Objects.equals(currentMetadata.getKey(), metadataPath[lastIndex]))
                .filter(MetadataEntry.class::isInstance).map(MetadataEntry.class::cast)
                .map(MetadataEntry::getValue)
                .filter(value -> !value.isEmpty())
                .findFirst();
        if (metadataTitle.isPresent()) {
            return metadataTitle.get();
        }
        return " - ";
    }

    /**
     * Determine and return which metadata can be added to a specific MetadataGroup.
     *
     * @param ruleset ruleset of the process
     * @param metadataNode TreeNode containing MetadataGroup to check
     * @return List of select items representing addable metadata types
     */
    public static List<SelectItem> getAddableMetadataForGroup(Ruleset ruleset, TreeNode metadataNode) {
        ProcessFieldedMetadata fieldedMetadata = ((ProcessFieldedMetadata) metadataNode.getData());
        ComplexMetadataViewInterface metadataView = fieldedMetadata.getMetadataView();
        List<SelectItem> addableMetadata = new ArrayList<>();
        for (MetadataViewInterface keyView : metadataView.getAddableMetadata(fieldedMetadata.getChildMetadata(),
                fieldedMetadata.getAdditionallySelectedFields())) {
            addableMetadata.add(
                    new SelectItem(keyView.getId(), keyView.getLabel(),
                            keyView instanceof SimpleMetadataViewInterface
                                    ? ((SimpleMetadataViewInterface) keyView).getInputType().toString()
                                    : "dataTable"));
        }
        return sortMetadataList(addableMetadata, ruleset);
    }

    /**
     * Determine and return which metadata can be added to the currently selected LogicalDivision.
     * @param dataEditor DataEditorForm instance used to determine addable metadata types
     * @param currentElement whether addable metadata should be determined for currently selected
     *                       LogicalDivision or not (if "false", it is determined for a new
     *                       LogicalDivision added by "AddDocStrucElementDialog"!)
     * @param metadataNodes TreeNodes containing the metadata already assigned to the current structure element
     * @param structureType type of the new structure to be added and for which addable metadata is to be determined
     * @return List of select items representing addable metadata types
     */
    public static List<SelectItem> getAddableMetadataForStructureElement(DataEditorForm dataEditor,
                                                                         boolean currentElement,
                                                                         List<TreeNode> metadataNodes,
                                                                         String structureType,
                                                                         boolean isLogicalStructure) {
        List<SelectItem> addableMetadata = new ArrayList<>();
        Collection<Metadata> existingMetadata = Collections.emptyList();
        StructuralElementViewInterface structureView;
        try {
            if (currentElement) {
                structureView = getStructuralElementView(dataEditor);
                existingMetadata = getExistingMetadataRows(metadataNodes);
            } else {
                structureView = dataEditor.getRulesetManagement()
                        .getStructuralElementView(structureType,
                                dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
            }
            Collection<String> additionalFields = isLogicalStructure ? dataEditor.getMetadataPanel()
                    .getLogicalMetadataTable().getAdditionallySelectedFields() : dataEditor.getMetadataPanel()
                    .getPhysicalMetadataTable().getAdditionallySelectedFields();
            if (Objects.nonNull(structureView)) {
                addableMetadata = getAddableMetadataForStructureElement(structureView, existingMetadata,
                        additionalFields, dataEditor.getProcess().getRuleset());
            }
        } catch (InvalidMetadataValueException e) {
            Helper.setErrorMessage(e);
        }
        return addableMetadata;
    }

    /**
     * Determine and return list of metadata that can be added to currently selected structure element.
     *
     * @param dataEditor DataEditorForm instance used to determine list of addable metadata types
     * @return List of select items representing addable metadata types
     */
    public static List<SelectItem> getAddableMetadataForStructureElement(DataEditorForm dataEditor) {
        return getAddableMetadataForStructureElement(dataEditor,
                true, dataEditor.getMetadataPanel().getLogicalMetadataRows().getChildren(), null, true);
    }

    /**
     * Determine and return which metadata can be added to the currently selected LogicalDivision.
     * @param structureView StructureElementViewInterface corresponding to structure element currently selected
     * @param existingMetadata existing Metadata of the structureView
     * @param additionalFields additionally added Metadata of the structureView
     * @param ruleset ruleset
     * @return List of select items representing addable metadata types
     */
    public static List<SelectItem> getAddableMetadataForStructureElement(StructuralElementViewInterface structureView,
                                                                         Collection<Metadata> existingMetadata,
                                                                         Collection<String> additionalFields, Ruleset ruleset) {
        List<SelectItem> addableMetadata = new ArrayList<>();
        Collection<MetadataViewInterface> viewInterfaces = structureView
                .getAddableMetadata(existingMetadata, additionalFields);
        for (MetadataViewInterface keyView : viewInterfaces) {
            addableMetadata.add(
                    new SelectItem(keyView.getId(), keyView.getLabel(),
                            keyView instanceof SimpleMetadataViewInterface
                                    ? ((SimpleMetadataViewInterface) keyView).getInputType().toString()
                                    : "dataTable"));
        }
        return sortMetadataList(addableMetadata, ruleset);
    }

    /**
     * Determine and return list of metadata that can be added to currently selected media unit.
     *
     * @param dataEditor DataEditorForm instance used to determine list of addable metadata types
     * @return List of select items representing addable metadata types
     */
    public static List<SelectItem> getAddableMetadataForMediaUnit(DataEditorForm dataEditor) {
        return getAddableMetadataForStructureElement(dataEditor,
                true, dataEditor.getMetadataPanel().getPhysicalMetadataRows().getChildren(), null, false);
    }

    /**
     * Determine and return StructureElementViewInterface corresponding to structure element currently selected or to be
     * added via AddDocStructTypeDialog.
     *
     * @param dataEditor DataEditorForm instance used to determine StructureElementViewInterface
     * @return StructureElementViewInterface corresponding to structure element currently selected or to be added
     */
    public static StructuralElementViewInterface getStructuralElementView(DataEditorForm dataEditor) {
        Optional<LogicalDivision> selectedStructure = dataEditor.getSelectedStructure();
        if (selectedStructure.isPresent()) {
            return dataEditor.getRulesetManagement()
                    .getStructuralElementView(
                            selectedStructure.get().getType(),
                            dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        }

        TreeNode selectedLogicalNode = dataEditor.getStructurePanel().getSelectedLogicalNode();

        if (Objects.isNull(selectedLogicalNode)) {
            throw new IllegalStateException("No logical node selected!");
        }

        if (!(selectedLogicalNode.getData() instanceof StructureTreeNode)) {
            String nodeClass = "unknown";
            if (Objects.nonNull(selectedLogicalNode.getData())) {
                nodeClass = selectedLogicalNode.getData().getClass().getName();
            }
            throw new IllegalStateException("Selected logical node data has wrong type '" + nodeClass
                    + "'! ('StructureTreeNode' expected)");
        }
        StructureTreeNode structureTreeNode = (StructureTreeNode) selectedLogicalNode.getData();

        Object dataObject = structureTreeNode.getDataObject();

        // data object is null for structures inside parent processes
        if (Objects.isNull(dataObject)) {
            return null;
        }

        if (dataObject instanceof View) {
            View view = (View) dataObject;
            if (Objects.isNull(view.getPhysicalDivision())) {
                throw new IllegalStateException("View has no physical division assigned!");
            }
            return dataEditor.getRulesetManagement().getStructuralElementView(
                view.getPhysicalDivision().getType(),
                    dataEditor.getAcquisitionStage(), dataEditor.getPriorityList());
        }

        // data object is a sibling process
        if (dataObject instanceof Process) {
            return null;
        }
        throw new IllegalStateException("Data object has unknown type '" + dataObject.getClass().getName() + "'!");
    }

    /**
     * Get existing Metadata in metadataTreeNodes.
     * @param metadataTreeNodes as a List of TreeNode
     * @return the existing metadata
     */
    public static Collection<Metadata> getExistingMetadataRows(List<TreeNode> metadataTreeNodes) throws InvalidMetadataValueException {
        Collection<Metadata> existingMetadataRows = new ArrayList<>();

        for (TreeNode metadataNode : metadataTreeNodes) {
            if (metadataNode.getData() instanceof ProcessDetail) {
                try {
                    existingMetadataRows.addAll(((ProcessDetail) metadataNode.getData()).getMetadata(false));
                } catch (NullPointerException e) {
                    logger.error(e);
                }
            }
        }
        return existingMetadataRows;
    }

    /**
     * Get allowed substructural elements as sorted list of select items.
     *
     * @param divisionView
     *         The division View
     * @param ruleset
     *         The ruleset
     * @return Sorted list of select items
     */
    public static List<SelectItem> getSortedAllowedSubstructuralElements(
            StructuralElementViewInterface divisionView, Ruleset ruleset) {
        List<SelectItem> selectItems = new ArrayList<>();
        for (Map.Entry<String, String> entry : divisionView.getAllowedSubstructuralElements().entrySet()) {
            selectItems.add(new SelectItem(entry.getKey(), entry.getValue()));
        }
        sortMetadataList(selectItems, ruleset);
        return selectItems;
    }

    /**
     * Sort a metadata list alphabetically if the 'orderMetadataByRuleset' parameter of the ruleset not set as true.
     * @param itemList as a List of SelectItem
     * @param ruleset as a Ruleset
     * @return itemList
     */
    public static List<SelectItem> sortMetadataList(List<SelectItem> itemList, Ruleset ruleset) {
        if (!(itemList.isEmpty() || ruleset.isOrderMetadataByRuleset())) {
            itemList.sort(Comparator.comparing(SelectItem::getLabel));
        }
        return itemList;
    }

    /**
     * Get the view of base media by comparing media files of tree nodes.
     *
     * @param treeNodes
     *         The tree nodes
     * @param mediaFiles
     *         The media files to compare too
     * @return View or null
     */
    public static View getViewOfBaseMediaByMediaFiles(List<TreeNode> treeNodes, Map<MediaVariant, URI> mediaFiles) {
        for (TreeNode treeNode : treeNodes) {
            if (StructurePanel.VIEW_NODE_TYPE.equals(
                    treeNode.getType()) && treeNode.getData() instanceof StructureTreeNode) {
                StructureTreeNode structureMediaTreeNode = (StructureTreeNode) treeNode.getData();
                if (structureMediaTreeNode.getDataObject() instanceof View) {
                    View view = (View) structureMediaTreeNode.getDataObject();
                    if (view.getPhysicalDivision().getMediaFiles().equals(mediaFiles)) {
                        return view;
                    }
                }
            }
            if (treeNode.getChildCount() > 0) {
                View view = getViewOfBaseMediaByMediaFiles(treeNode.getChildren(), mediaFiles);
                if (Objects.nonNull(view)) {
                    return view;
                }
            }
        }
        return null;
    }

}
