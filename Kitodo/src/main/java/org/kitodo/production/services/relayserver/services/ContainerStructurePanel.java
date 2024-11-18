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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Task;
import org.kitodo.production.forms.dataeditor.DataEditorForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.api.dataeditor.ContainerStructureElement;
import org.kitodo.api.dataeditor.ContainerStructureMetadata;
import org.kitodo.production.services.relayserver.helper.XMLHelper;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ContainerStructurePanel {

    private final DataEditorForm dataEditorForm;
    private static final String VISIBLE_IN_TASK = "Dossier prüfen";
    private TreeNode containerStructure;
    private TreeNode selectedTreeNode;
    private ContainerStructureElement selectedElement;
    private static final List<String> possibleStructures = Arrays.asList(
            "Behaeltnisse",
            "Behaeltnis",
            "EnthalteneVerzEinheiten",
            "VerzEinheit"
    );


    public ContainerStructurePanel(DataEditorForm form) {
        this.dataEditorForm = form;
    }

    /**
     * Prepare the ContainerStructurePanel for displaying content.
     */
    public void show() {
        containerStructure = null;
        selectedTreeNode = null;
        selectedElement = null;
    }

    public TreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    public ContainerStructureElement getSelectedElement() {
        return selectedElement;
    }

    public void setSelectedElement(ContainerStructureElement selectedElement) {
        this.selectedElement = selectedElement;
    }

    /**
     * Sets ContainerStructureElement on selection of TreeNode.
     *
     * @param event The NoteSelectEvent.
     */
    public void onNodeSelect(NodeSelectEvent event) {
        setSelectedElement((ContainerStructureElement) event.getTreeNode().getData());
    }


    /**
     * Retrieve the container structure of the digitization job needed for identification.
     *
     * @return TreeNode containing the full container structure
     */
    public TreeNode getContainerStructure() {
        if (Objects.isNull(this.containerStructure)) {
            setContainerStructure(createContainerStructure());
        }
        return this.containerStructure;
    }

    private void setContainerStructure(TreeNode containerStructure) {
        this.containerStructure = containerStructure;
    }

    private TreeNode createContainerStructure() {
        TreeNode root = new DefaultTreeNode("root", null);

        String xml = getContainerStructureXML();
        if (Objects.isNull(xml) || xml.isEmpty()) {
            return root;
        }

        Document document = XMLHelper.parseXML(xml);
        convertNodeToPrimeFacesTreeNode(document.getFirstChild(), root);

        return setExpandingAll(root, true);
    }

    private String getContainerStructureXML() {
        String xml = null;

        // get xml containing container structure
        List<Property> properties;
        properties = dataEditorForm.getProcess().getProperties();

        for (Property property : properties) {
            if (property.getTitle().equalsIgnoreCase("Behältnisse")) {
                xml = property.getValue();
                break;
            }
        }

        return xml;
    }

    private void convertNodeToPrimeFacesTreeNode(Node node, TreeNode parentNode) {
        if (!Objects.equals(node.getNodeType(), Node.ELEMENT_NODE)) {
            return;
        }

        ContainerStructureElement containerStructureElement = new ContainerStructureElement(node.getNodeName());
        TreeNode treeNode = new DefaultTreeNode(containerStructureElement);
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node currentChild = children.item(i);
            if (!Objects.equals(currentChild.getNodeType(), Node.ELEMENT_NODE)) {
                continue;
            }
            if (isMetadata(currentChild)) {
                ContainerStructureMetadata metadata = new ContainerStructureMetadata(currentChild.getNodeName(),
                        currentChild.getTextContent());
                containerStructureElement.addMetadata(metadata);
            } else {
                // Kind ist Strukturelement und muss weiter ausgewertet werden
                convertNodeToPrimeFacesTreeNode(currentChild, treeNode);
            }
        }

        int index = -1;
        for (TreeNode child : parentNode.getChildren()) {
            if (child.getData() instanceof ContainerStructureElement) {
                ContainerStructureElement childElement = (ContainerStructureElement) child.getData();
                if (containerStructureElement.compareTo(childElement) < 0) {
                    index = parentNode.getChildren().indexOf(child);
                    break;
                }
            }
        }
        if (index > -1) {
            parentNode.getChildren().add(index, treeNode);
        } else {
            parentNode.getChildren().add(treeNode);
        }
    }

    private boolean isMetadata(Node node) {
        return !possibleStructures.contains(node.getNodeName());
    }

    private TreeNode setExpandingAll(TreeNode node, boolean expanded) {
        for (TreeNode child : node.getChildren()) {
            setExpandingAll(child, expanded);
        }
        node.setExpanded(expanded);

        return node;
    }

    public boolean isDisplayContainerStructure() {
        if (Objects.isNull(this.dataEditorForm.getProcess())) {
            return false;
        }
        Task currentTask = ServiceManager.getProcessService().getCurrentTask(this.dataEditorForm.getProcess());
        if (Objects.nonNull(currentTask)) {
            return currentTask.getTitle().equalsIgnoreCase(VISIBLE_IN_TASK);
        } else {
            Helper.setErrorMessage("Unable to determine current task => container structure is not displayed!");
            return false;
        }
    }

    /**
     * Get Signature.
     *
     * @param containerStructureElement containerStructureElement
     * @return signature as java.lang.String
     */
    public String getSignature(ContainerStructureElement containerStructureElement) {
        for (ContainerStructureMetadata containerStructureMetadata : containerStructureElement.getMetadata()) {
            if (containerStructureMetadata.getMetadata().equals("Signatur")) {
                return containerStructureMetadata.getValue();
            }
        }
        return "Keine Signatur";
    }

    /**
     * Get All Metadata.
     *
     * @param containerStructureElement containerStructureElement
     * @return all Metadata as java.lang.String
     */
    public String getAllMetadata(ContainerStructureElement containerStructureElement) {
        StringBuilder all = new StringBuilder(containerStructureElement.getType());
        for (ContainerStructureMetadata containerStructureMetadata : containerStructureElement.getMetadata()) {
            all.append("_").append(containerStructureMetadata.getValue());
        }
        return all.toString();
    }

}
