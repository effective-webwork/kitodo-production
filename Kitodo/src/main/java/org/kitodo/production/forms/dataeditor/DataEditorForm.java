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

package org.kitodo.production.forms.dataeditor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.IncludedStructuralElement;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.View;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.NoSuchMetadataFieldException;
import org.kitodo.exceptions.StructureNotEmptyException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.interfaces.RulesetSetupInterface;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;

@Named("DataEditorForm")
@SessionScoped
public class DataEditorForm implements RulesetSetupInterface, Serializable {

    private static final Logger logger = LogManager.getLogger(DataEditorForm.class);

    /**
     * A filter on the rule set depending on the workflow step. So far this is
     * not configurable anywhere and is therefore on “edit”.
     */
    private String acquisitionStage;

    /**
     * Backing bean for the add doc struc type dialog.
     */
    private final AddDocStrucTypeDialog addDocStrucTypeDialog;

    /**
     * Backing bean for the add MediaUnit dialog.
     */
    private final AddMediaUnitDialog addMediaUnitDialog;

    /**
     * Backing bean for the edit pages dialog.
     */
    private final EditPagesDialog editPagesDialog;

    /**
     * Backing bean for the gallery panel.
     */
    private final GalleryPanel galleryPanel;

    /**
     * The current process children.
     */
    private Set<Process> currentChildren = new HashSet<>();

    /**
     * The path to the main file, to save it later.
     */
    private URI mainFileUri;

    /**
     * Backing bean for the metadata panel.
     */
    private final MetadataPanel metadataPanel;

    /**
     * Currently open processes of all users.
     */
    private static Map<Integer, User> openProcesses = new HashMap<>();

    /**
     * Backing bean for the pagination panel.
     */
    private final PaginationPanel paginationPanel;

    /**
     * The language preference list of the editing user for displaying the
     * metadata labels. We cache this because it’s used thousands of times and
     * otherwise the access would always go through the search engine, which
     * would delay page creation.
     */
    private List<LanguageRange> priorityList;

    /**
     * Process whose workpiece is under edit.
     */
    private Process process;

    private String referringView = "desktop";

    /**
     * The ruleset that the file is based on.
     */
    private RulesetManagementInterface ruleset;

    /**
     * Backing bean for the structure panel.
     */
    private final StructurePanel structurePanel;

    /**
     * User sitting in front of the editor.
     */
    private User user;

    /**
     * The file content.
     */
    private Workpiece workpiece;

    /**
     * The task the user is currently working on when opening the Metadata Editor.
     */
    private Task currentTask;

    /**
     * This List of Pairs stores all selected physical elements and the logical elements in which the physical element was selected.
     * It is necessary to store the logical elements as well, because a physical element can be assigned to multiple logical elements.
     */
    private List<Pair<MediaUnit, IncludedStructuralElement>> selectedMedia;

    /**
     * Public constructor.
     */
    public DataEditorForm() {
        this.structurePanel = new StructurePanel(this);
        this.metadataPanel = new MetadataPanel(this);
        this.galleryPanel = new GalleryPanel(this);
        this.paginationPanel = new PaginationPanel(this);
        this.addDocStrucTypeDialog = new AddDocStrucTypeDialog(this);
        this.addMediaUnitDialog = new AddMediaUnitDialog(this);
        this.editPagesDialog = new EditPagesDialog(this);
        acquisitionStage = "edit";
    }

    /**
     * Open the metadata file of the process of which the task with the given ID in the metadata editor.
     *
     * @param taskID
     *            ID of the task whose process is opened
     * @param referringView
     *            JSF page the user came from
     */
    public void open(String taskID, String referringView) {
        try {
            this.currentTask = ServiceManager.getTaskService().getById(Integer.parseInt(taskID));
            this.referringView = referringView;
            this.process = this.currentTask.getProcess();
            this.currentChildren.addAll(process.getChildren());
            this.user = ServiceManager.getUserService().getCurrentUser();

            ruleset = openRuleset(process.getRuleset());
            openMetsFile();
            selectedMedia = new LinkedList<>();
            init();
            openProcesses.put(process.getId(), user);
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect(referringView);
            } catch (IOException ex) {
                logger.error("Unable to redirect to referrer '" + referringView + "'. (" + ex.getLocalizedMessage()
                        + ")");
            }
        }
    }

    /**
     * Opens the METS file.
     *
     * @throws IOException
     *             if filesystem I/O fails
     */
    private void openMetsFile() throws IOException {
        mainFileUri = ServiceManager.getProcessService().getMetadataFileUri(process);
        workpiece = ServiceManager.getMetsService().loadWorkpiece(mainFileUri);
        ServiceManager.getFileService().searchForMedia(process, workpiece);
    }

    private RulesetManagementInterface openRuleset(Ruleset ruleset) throws IOException {
        final long begin = System.nanoTime();
        String metadataLanguage = user.getMetadataLanguage();
        priorityList = LanguageRange.parse(metadataLanguage.isEmpty() ? "en" : metadataLanguage);
        RulesetManagementInterface openRuleset = ServiceManager.getRulesetService().openRuleset(ruleset);
        if (logger.isTraceEnabled()) {
            logger.trace("Reading ruleset took {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
        return openRuleset;
    }

    private void init() {
        final long begin = System.nanoTime();

        List<MediaUnit> severalAssignments = new LinkedList<>();
        initSeveralAssignments(workpiece.getMediaUnit(), severalAssignments);
        structurePanel.getSeveralAssignments().addAll(severalAssignments);

        structurePanel.show();
        structurePanel.getSelectedLogicalNode().setSelected(true);
        structurePanel.getSelectedPhysicalNode().setSelected(true);
        metadataPanel.showLogical(getSelectedStructure());
        metadataPanel.showPhysical(getSelectedMediaUnit());
        galleryPanel.show();
        paginationPanel.show();

        editPagesDialog.prepare();

        if (logger.isTraceEnabled()) {
            logger.trace("Initializing editor beans took {} ms",
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
        }
    }

    /**
     * Clears all remaining content from the data editor form.
     *
     * @return the referring view, to return there
     */
    public String close() {
        metadataPanel.clear();
        structurePanel.clear();
        galleryPanel.clear();
        workpiece = null;
        mainFileUri = null;
        ruleset = null;
        currentChildren.clear();
        selectedMedia.clear();
        openProcesses.remove(process.getId());
        process = null;
        user = null;
        this.setCurrentTask(null);
        if (referringView.contains("?")) {
            return referringView + "&faces-redirect=true";
        } else {
            return referringView + "?faces-redirect=true";
        }
    }

    /**
     * Validate the structure and metadata.
     *
     * @return whether the validation was successful or not
     */
    public boolean validate() {
        try {
            ValidationResult validationResult = ServiceManager.getMetadataValidationService().validate(workpiece,
                ruleset);
            State state = validationResult.getState();
            if (State.ERROR.equals(state)) {
                Helper.setErrorMessage(Helper.getTranslation("dataEditor.validation.state.error"));
                for (String message : validationResult.getResultMessages()) {
                    Helper.setErrorMessage(message);
                }
                return false;
            } else {
                Helper.setMessage(Helper.getTranslation("dataEditor.validation.state.".concat(state.toString()
                        .toLowerCase())));
                for (String message : validationResult.getResultMessages()) {
                    Helper.setMessage(message);
                }
                return true;
            }
        } catch (DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return false;
        }
    }

    /**
     * Save the structure and metadata.
     *
     * @return navigation target
     */
    public String save() {
        metadataPanel.preserve();
        structurePanel.preserve();
        try (OutputStream out = ServiceManager.getFileService().write(mainFileUri)) {
            ServiceManager.getMetsService().save(workpiece, out);
            return close();
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        PrimeFaces.current().executeScript("PF('sticky-notifications').removeAll();");
        PrimeFaces.current().ajax().update("notifications");
        return null;
    }

    private void initSeveralAssignments(MediaUnit mediaUnit, List<MediaUnit> severalAssignments) {
        if (mediaUnit.getIncludedStructuralElements().size() > 1) {
            severalAssignments.add(mediaUnit);
        }
        for (MediaUnit child : mediaUnit.getChildren()) {
            initSeveralAssignments(child, severalAssignments);
        }
    }

    /**
     * Deletes the selected outline point from the logical outline. This method
     * is called by PrimeFaces to inform the application that the user has
     * clicked on the shortcut menu entry to clear the outline point.
     */
    public void deleteStructure() {
        try {
            structurePanel.deleteSelectedStructure();
        } catch (StructureNotEmptyException e) {
            Helper.setErrorMessage(Helper.getTranslation("dataEditor.structureDeletionError")
                    + " (" + e.getLocalizedMessage() + ")");
        }
    }

    /**
     * Deletes the selected media unit from the media list. The associated files
     * on the drive are not deleted. The next time the editor is started, files
     * that are not yet in the media list will be inserted there again. This
     * method is called by PrimeFaces to inform the application that the user
     * clicked on the context menu entry to delete the media unit.
     */
    public void deleteMediaUnit() {
        structurePanel.deleteSelectedMediaUnit();
    }

    @Override
    public String getAcquisitionStage() {
        return acquisitionStage;
    }

    /**
     * Returns the backing bean for the add doc struc type dialog. This function
     * is used by PrimeFaces to access the elements of the add doc struc type
     * dialog.
     *
     * @return the backing bean for the add doc struc type dialog
     */
    public AddDocStrucTypeDialog getAddDocStrucTypeDialog() {
        return addDocStrucTypeDialog;
    }

    /**
     * Returns the backing bean for the add media dialog. This function is used
     * by PrimeFaces to access the elements of the add media dialog.
     *
     * @return the backing bean for the add media dialog
     */
    public AddMediaUnitDialog getAddMediaUnitDialog() {
        return addMediaUnitDialog;
    }

    /**
     * Returns the backing bean for the edit pages dialog. This function is used
     * by PrimeFaces to access the elements of the edit pages dialog.
     *
     * @return the backing bean for the edit pages dialog
     */
    public EditPagesDialog getEditPagesDialog() {
        return editPagesDialog;
    }

    /**
     * Returns the backing bean for the gallery panel. This function is used by
     * PrimeFaces to access the elements of the gallery panel.
     *
     * @return the backing bean for the gallery panel
     */
    public GalleryPanel getGalleryPanel() {
        return galleryPanel;
    }

    Set<Process> getCurrentChildren() {
        return currentChildren;
    }

    /**
     * Returns the backing bean for the metadata panel. This function is used
     * by PrimeFaces to access the elements of the metadata panel.
     *
     * @return the backing bean for the metadata panel
     */
    public MetadataPanel getMetadataPanel() {
        return metadataPanel;
    }

    /**
     * Returns the backing bean for the pagination panel. This function is used
     * by PrimeFaces to access the elements of the pagination panel.
     *
     * @return the backing bean for the pagination panel
     */
    public PaginationPanel getPaginationPanel() {
        return paginationPanel;
    }

    @Override
    public List<LanguageRange> getPriorityList() {
        return priorityList;
    }

    /**
     * Get process.
     *
     * @return value of process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Get process title.
     *
     * @return value of process title
     */
    public String getProcessTitle() {
        return process.getTitle();
    }

    /**
     * Get the current task.
     *
     * @return the current task
     */
    public Task getCurrentTask() {
        return this.currentTask;
    }

    /**
     * Return a list of tasks that are "INWORK", assigned to the current user and have type "metadata".
     *
     * @param processID
     *          ID of the process for which the current task options will be determined
     *
     * @return list of tasks with type metadata that are "INWORK" assigned to the current user
     */
    public List<Task> getCurrentTaskOptions(int processID) {
        try {
            Process process = ServiceManager.getProcessService().getById(processID);
            return process.getTasks().stream().filter(t -> t.isTypeMetadata()
                    && Objects.nonNull(t.getProcessingUser())
                    && ServiceManager.getUserService().getAuthenticatedUser().getId().equals(t.getProcessingUser().getId())
                    && TaskStatus.INWORK.equals(t.getProcessingStatus())).collect(Collectors.toList());
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne",
                    new Object[] {ObjectType.PROCESS.getTranslationSingular(), processID} , logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Set the current task.
     *
     * @param task the new value for the current task
     */
    public void setCurrentTask(Task task) {
        this.currentTask = task;
    }

    public static User getUserOpened(Integer identificationNumber) {
        return openProcesses.get(identificationNumber);
    }

    @Override
    public RulesetManagementInterface getRuleset() {
        return ruleset;
    }

    Optional<IncludedStructuralElement> getSelectedStructure() {
        return structurePanel.getSelectedStructure();
    }

    Optional<MediaUnit> getSelectedMediaUnit() {
        return structurePanel.getSelectedMediaUnit();
    }

    /**
     * Return structurePanel.
     *
     * @return structurePanel
     */
    public StructurePanel getStructurePanel() {
        return structurePanel;
    }

    Workpiece getWorkpiece() {
        return workpiece;
    }

    void refreshStructurePanel() {
        structurePanel.show();
        galleryPanel.updateStripes();
    }

    void setProcess(Process process) {
        this.process = process;
    }

    /**
     * Set the current process of the DataEditorForm by ID.
     *
     * @param processID
     *          ID of the process to set
     */
    public void setProcessByID(int processID) {
        try {
            setProcess(ServiceManager.getProcessService().getById(processID));
        } catch (DAOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Get selectedMedia.
     *
     * @return value of selectedMedia
     */
    List<Pair<MediaUnit, IncludedStructuralElement>> getSelectedMedia() {
        return selectedMedia;
    }

    /**
     * Check if the passed MediaUnit is selected.
     * @param mediaUnit MediaUnit object to check for selection
     * @param includedStructuralElement object to check whether the MediaUnit is selected as a child of this IncludedStructuralElement.
     *                                  A MediaUnit can be assigned to multiple IncludedStructuralElements but can be selected
     *                                  in one of these IncludedStructuralElements.
     * @return boolean whether the MediaUnit is selected at the specified position
     */
    public boolean isSelected(MediaUnit mediaUnit, IncludedStructuralElement includedStructuralElement) {
        if (Objects.nonNull(mediaUnit) && Objects.nonNull(includedStructuralElement)) {
            return selectedMedia.contains(new ImmutablePair<>(mediaUnit, includedStructuralElement));
        }
        return false;
    }

    void switchStructure(Object treeNodeData, boolean updateGallery) throws NoSuchMetadataFieldException {
        try {
            metadataPanel.preserveLogical();
        } catch (InvalidMetadataValueException e) {
            logger.info(e.getLocalizedMessage(), e);
        }

        Optional<IncludedStructuralElement> selectedStructure = structurePanel.getSelectedStructure();

        metadataPanel.showLogical(selectedStructure);
        if (treeNodeData instanceof StructureTreeNode) {
            StructureTreeNode structureTreeNode = (StructureTreeNode) treeNodeData;
            if (Objects.nonNull(structureTreeNode.getDataObject())) {
                if (structureTreeNode.getDataObject() instanceof IncludedStructuralElement
                        && selectedStructure.isPresent()) {
                    // Logical structure element selected
                    IncludedStructuralElement structuralElement = selectedStructure.get();
                    if (!structuralElement.getViews().isEmpty()) {
                        ArrayList<View> views = new ArrayList<>(structuralElement.getViews());
                        if (Objects.nonNull(views.get(0))) {
                            View firstView = views.get(0);
                            updatePhysicalStructureTree(firstView);
                            if (updateGallery) {
                                updateGallery(firstView);
                            }
                        }
                    }
                } else if (structureTreeNode.getDataObject() instanceof View) {
                    // Page selected in logical tree
                    View view = (View) structureTreeNode.getDataObject();
                    metadataPanel.showPageInLogical(view.getMediaUnit());
                    if (updateGallery) {
                        updateGallery(view);
                    }
                    // no need to update physical tree because pages can only be clicked in logical tree if physical tree is hidden!
                }
            }
        }
    }

    void switchMediaUnit() throws NoSuchMetadataFieldException {
        try {
            metadataPanel.preservePhysical();
        } catch (InvalidMetadataValueException e) {
            logger.info(e.getLocalizedMessage(), e);
        }

        Optional<MediaUnit> selectedMediaUnit = structurePanel.getSelectedMediaUnit();

        metadataPanel.showPhysical(selectedMediaUnit);
        if (selectedMediaUnit.isPresent()) {
            // update gallery
            galleryPanel.updateSelection(selectedMediaUnit.get());
            // update logical tree
            for (GalleryMediaContent galleryMediaContent : galleryPanel.getMedias()) {
                if (Objects.nonNull(galleryMediaContent.getView())
                        && Objects.equals(selectedMediaUnit.get(), galleryMediaContent.getView().getMediaUnit())) {
                    structurePanel.updateLogicalNodeSelection(galleryMediaContent);
                    break;
                }
            }
        }
    }

    private void updatePhysicalStructureTree(View view) {
        GalleryMediaContent galleryMediaContent = this.galleryPanel.getGalleryMediaContent(view);
        if (Objects.nonNull(galleryMediaContent)) {
            structurePanel.updatePhysicalNodeSelection(galleryMediaContent);
        }
    }

    private void updateGallery(View view) {
        MediaUnit mediaUnit = view.getMediaUnit();
        if (Objects.nonNull(mediaUnit)) {
            galleryPanel.updateSelection(mediaUnit);
        }
    }

    /**
     * Determine fallback task of process with given ID 'processID' and return the tasks ID.
     *
     * @param processID
     *          ID of process for which the fallback task is determined
     * @return ID of the fallback task for process with given ID 'processID'
     */
    public int getFallbackTaskID(int processID) {
        try {
            Process process = ServiceManager.getProcessService().getById(processID);
            if (!process.getTasks().isEmpty()) {
                return process.getTasks().get(0).getId();
            }
            return -1;
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {ObjectType.PROCESS.getTranslationSingular(),
                processID}, logger, e);
            return -1;
        }
    }

    /**
     * Create and return the navigation path to the metadata editor, containing the currentTask ID and the given
     * referrer as view/URL parameters.
     *
     * @param referrer
     *          path of referring view
     * @return navigation path to metadata editor page including currentTasks ID and referrer as view parameters
     */
    public String selectCurrentTask(String referrer) {
        return "/pages/metadataEditor?faces-redirect=true&taskId=" + this.getCurrentTask().getId()
                + "&referrer=" + referrer;
    }

    void assignView(IncludedStructuralElement includedStructuralElement, View view) {
        includedStructuralElement.getViews().add(view);
        view.getMediaUnit().getIncludedStructuralElements().add(includedStructuralElement);
    }

    void unassignView(IncludedStructuralElement includedStructuralElement, View view, boolean removeLast) {
        // if View was moved within one element, we need to distinguish two possible directions it could have been moved
        if (removeLast) {
            includedStructuralElement.getViews().removeLastOccurrence(view);
        } else {
            includedStructuralElement.getViews().removeFirstOccurrence(view);
        }
        view.getMediaUnit().getIncludedStructuralElements().remove(includedStructuralElement);
    }
}
