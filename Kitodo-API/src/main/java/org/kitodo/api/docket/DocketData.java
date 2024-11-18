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

package org.kitodo.api.docket;

import java.net.URI;
import org.kitodo.api.dataeditor.ContainerStructureElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DocketData {

    /** The metadata file. */
    private URI metadataFile;

    /** The docket data of the parent. */
    private DocketData parent;

    /** The name of the process. */
    private String processName;

    /** The id of the process. */
    private String processId;

    /** The name of the project. */
    private String projectName;

    /** The name of the used ruleset. */
    private String rulesetName;

    /** The creation Date of the process. */
    private String creationDate;

    /** The comments. */
    private List<String> comments = new ArrayList<>();

    /** The template properties. */
    private List<Property> templateProperties;

    /** The workpiece properties. */
    private List<Property> workpieceProperties;

    /** The process properties. */
    private List<Property> processProperties;

    /** The process viaduc id. */
    private String viaducId;

    /** The titel of the process. */
    private String processTitel;

    private String dateOfRequestDossierFromViaduc;

    /** The Dossier Signatur. */
    private String dossierSignatur;

    /** in schutzfrist. */
    private String schutzfrist;

    private List<String> behaeltnisse;

    /** The list of DmdSections. */
    private List<DmdSec> dmdSecs = new ArrayList<>();

    /* The bar remark for the process. */
    private String barRemark;

    /* The client remark for the process.*/
    private String clientRemark;

    /* The "Aktenzeichen" of the root dossier. */
    private String reference;

    /* The "Zeitraum" of the root dossier. */
    private String period;

    /* The "Früheres Aktenzeichen" of the root dossier. */
    private String previousReference;

    /* The "Zusatzkomponente" of the root dossier. */
    private String additionalComponent;

    private List<ContainerStructureElement> containerStructure = new ArrayList<>();


    /**
     * Gets the metadataFile.
     *
     * @return The metadataFile.
     */
    public URI metadataFile() {
        return metadataFile;
    }

    /**
     * Sets the metadataFile.
     *
     * @param metadataFile
     *            The metadata file.
     */
    public void setMetadataFile(URI metadataFile) {
        this.metadataFile = metadataFile;
    }

    /**
     * Gets the processName.
     *
     * @return The processName.
     */
    public DocketData getParent() {
        return parent;
    }

    /**
     * Sets the parent.
     *
     * @param parent
     *            The docket data of the parent.
     */
    public void setParent(DocketData parent) {
        this.parent = parent;
    }

    /**
     * Gets the parent.
     *
     * @return The parent.
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * Sets the processName.
     * 
     * @param processName
     *            The query to execute.
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    /**
     * Gets the processId.
     * 
     * @return The processId.
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * Sets the processId.
     * 
     * @param processId
     *            The processId.
     */
    public void setProcessId(String processId) {
        this.processId = processId;
    }

    /**
     * Gets the projectName.
     * 
     * @return The projectName.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the projectName.
     * 
     * @param projectName
     *            The projectName.
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Gets the rulesetName.
     * 
     * @return The rulesetName.
     */
    public String getRulesetName() {
        return rulesetName;
    }

    /**
     * Sets the rulesetName.
     * 
     * @param rulesetName
     *            The rulesetName.
     */
    public void setRulesetName(String rulesetName) {
        this.rulesetName = rulesetName;
    }

    /**
     * Gets the creationDate.
     * 
     * @return The creationDate.
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creationDate.
     * 
     * @param creationDate
     *            The creationDate.
     */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets the comments.
     *
     * @return The comments.
     */
    public List<String> getComments() {
        return comments;
    }

    /**
     * Sets the comments.
     *
     * @param comments
     *            The comments.
     */
    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    /**
     * Gets the templateProperties.
     * 
     * @return The templateProperties.
     */
    public List<Property> getTemplateProperties() {
        return templateProperties;
    }

    /**
     * Sets the templateProperties.
     * 
     * @param templateProperties
     *            The templateProperties.
     */
    public void setTemplateProperties(List<Property> templateProperties) {
        this.templateProperties = templateProperties;
    }

    /**
     * Gets the workpieceProperties.
     * 
     * @return The workpieceProperties.
     */
    public List<Property> getWorkpieceProperties() {
        if (Objects.isNull(workpieceProperties)) {
            workpieceProperties = new ArrayList<>();
        }
        return workpieceProperties;
    }

    /**
     * Sets the workpieceProperties.
     * 
     * @param workpieceProperties
     *            The workpieceProperties.
     */
    public void setWorkpieceProperties(List<Property> workpieceProperties) {
        this.workpieceProperties = workpieceProperties;
    }

    /**
     * Gets the processProperties.
     * 
     * @return The processProperties.
     */
    public List<Property> getProcessProperties() {
        if (Objects.isNull(processProperties)) {
            processProperties = new ArrayList<>();
        }
        return processProperties;
    }

    /**
     * Sets the processProperties.
     * 
     * @param processProperties
     *            The processProperties.
     */
    public void setProcessProperties(List<Property> processProperties) {
        this.processProperties = processProperties;
    }

    /**
     * Gets the Dossier Signatur.
     *
     * @return the Dossier Signatur.
     */
    public String getDossierSignatur() {
        return dossierSignatur;
    }

    /**
     * Sets The Dossier Signatur.
     *
     * @param dossierSignatur
     *            The Dossier Signatur.
     */
    public void setDossierSignatur(String dossierSignatur) {
        this.dossierSignatur = dossierSignatur;
    }

    /**
     * Gets the DmdSections.
     *
     * @return The DmdSections.
     */
    public List<DmdSec> getDmdSecs() {
        return dmdSecs;
    }

    /**
     * Sets the DmdSections.
     *
     * @param dmdSecs
     *            The DmdSections.
     */
    public void setDmdSecs(List<DmdSec> dmdSecs) {
        this.dmdSecs = dmdSecs;
    }

    /**
     * Get schutzfrist.
     *
     * @return value of schutzfrist
     */
    public String getSchutzfrist() {
        return schutzfrist;
    }

    /**
     * Set schutzfrist.
     *
     * @param schutzfrist as java.lang.String
     */
    public void setSchutzfrist(String schutzfrist) {
        if (schutzfrist.equals("true")) {
            this.schutzfrist = "In Schutzfrist";
        } else {
            this.schutzfrist = "-";
        }
    }

    /**
     * Get processTitel.
     *
     * @return value of processTitel
     */
    public String getProcessTitel() {
        return processTitel;
    }

    /**
     * Set processTitel.
     *
     * @param processTitel as java.lang.String
     */
    public void setProcessTitel(String processTitel) {
        this.processTitel = processTitel;
    }

    /**
     * Get dateOfRequestDossierFromViaduc.
     *
     * @return value of dateOfRequestDossierFromViaduc
     */
    public String getDateOfRequestDossierFromViaduc() {
        return dateOfRequestDossierFromViaduc;
    }

    /**
     * Set dateOfRequestDossierFromViaduc.
     *
     * @param dateOfRequestDossierFromViaduc as java.lang.String
     */
    public void setDateOfRequestDossierFromViaduc(String dateOfRequestDossierFromViaduc) {
        this.dateOfRequestDossierFromViaduc = dateOfRequestDossierFromViaduc;
    }

    /**
     * Get behaeltnisse.
     *
     * @return value of behaeltnisse
     */
    public List<String> getBehaeltnisse() {
        return Objects.nonNull(behaeltnisse) ? behaeltnisse : Collections.emptyList();
    }

    /**
     * Set behaeltnisse.
     *
     * @param behaeltnisse as java.util.List<java.lang.String>
     */
    public void setBehaeltnisse(List<String> behaeltnisse) {
        this.behaeltnisse = behaeltnisse;
    }

    /**
     * Get barRemark.
     *
     * @return value of barRemark
     */
    public String getBarRemark() {
        return barRemark;
    }

    /**
     * Set barRemark.
     *
     * @param barRemark as java.lang.String
     */
    public void setBarRemark(String barRemark) {
        this.barRemark = barRemark;
    }

    /**
     * Get clientRemark.
     *
     * @return value of clientRemark
     */
    public String getClientRemark() {
        return clientRemark;
    }

    /**
     * Set clientRemark.
     *
     * @param clientRemark as java.lang.String
     */
    public void setClientRemark(String clientRemark) {
        this.clientRemark = clientRemark;
    }

    /**
     * Get viaducId.
     *
     * @return value of viaducId
     */
    public String getViaducId() {
        return viaducId;
    }

    /**
     * Set viaducId.
     *
     * @param viaducId as java.lang.String
     */
    public void setViaducId(String viaducId) {
        this.viaducId = viaducId;
    }

    /**
     * Get reference.
     *
     * @return value of reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Set reference.
     *
     * @param reference as java.lang.String
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Get period.
     *
     * @return value of period
     */
    public String getPeriod() {
        return period;
    }

    /**
     * Set period.
     *
     * @param period as java.lang.String
     */
    public void setPeriod(String period) {
        this.period = period;
    }

    /**
     * Get previousReference.
     *
     * @return value of previousReference
     */
    public String getPreviousReference() {
        return previousReference;
    }

    /**
     * Set previousReference.
     *
     * @param previousReference as java.lang.String
     */
    public void setPreviousReference(String previousReference) {
        this.previousReference = previousReference;
    }

    /**
     * Get additionalComponent.
     *
     * @return value of additionalComponent
     */
    public String getAdditionalComponent() {
        return additionalComponent;
    }

    /**
     * Set additionalComponent.
     *
     * @param additionalComponent as java.lang.String
     */
    public void setAdditionalComponent(String additionalComponent) {
        this.additionalComponent = additionalComponent;
    }

    /**
     * Get containerStructure.
     *
     * @return value of containerStructure
     */
    public List<ContainerStructureElement> getContainerStructure() {
        return containerStructure;
    }

    /**
     * Set containerStructure.
     *
     * @param containerStructure as java.util.List<org.kitodo.api.dataeditor.ContainerStructureElement>
     */
    public void setContainerStructure(List<ContainerStructureElement> containerStructure) {
        this.containerStructure = containerStructure;
    }
}
