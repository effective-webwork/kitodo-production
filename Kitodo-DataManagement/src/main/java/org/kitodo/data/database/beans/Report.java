package org.kitodo.data.database.beans;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.kitodo.data.database.converter.TaskDataConverter;

@Entity
@Table(name = "report")
public class Report extends BaseBean {

    @Column
    @CsvBindByPosition(position = 0)
    @CsvBindByName
    private Integer kitodoId;

    @Column
    @CsvBindByPosition(position = 1)
    @CsvBindByName
    private Integer viaducId;

    @Column
    @CsvBindByPosition(position = 2)
    @CsvBindByName
    private Integer numberOfScans;

    @Column
    @CsvBindByPosition(position = 3)
    @CsvBindByName
    private String complexity;

    @Column
    @CsvBindByName
    @CsvBindByPosition(position = 4)
    private Boolean correctionWorkflowActivated;

    @Column
    @CsvBindByName
    @CsvBindByPosition(position = 5)
    private Boolean structuralError;

    @Column
    @CsvBindByName
    @CsvBindByPosition(position = 6)
    private Boolean missingInformation;

    @Column
    @CsvBindByName
    @CsvBindByPosition(position = 7)
    private Boolean reimport;

    @Column
    @CsvBindByName
    @CsvBindByPosition(position = 8)
    private String uploadDestination;

    @Column
    @CsvBindByName
    @CsvBindByPosition(position = 9)
    private Boolean canceled;

    @Column
    @CsvBindByName
    @CsvBindByPosition(position = 10)
    private LocalDateTime canceledTimestamp;

    @Column
    @CsvBindByName
    @CsvBindByPosition(position = 11)
    private String canceledMessage;

    @Column
    @CsvBindByName
    @CsvBindByPosition(position = 12)
    private Boolean workingCopy;

    @Column
    @CsvBindByName
    @CsvBindByPosition(position = 13)
    private LocalDateTime reportCreatedTimestamp;

    @Column(columnDefinition = "LONGTEXT")
    @Convert(converter = TaskDataConverter.class)
    private HashMap<String, List<HashMap<String, Object>>> taskData;

    /**
     * Empty default constructor.
     */
    public Report() {
    }

    /**
     * Get kitodoId.
     *
     * @return value of kitodoId
     */
    public Integer getKitodoId() {
        return kitodoId;
    }

    /**
     * Set kitodoId.
     *
     * @param kitodoId as java.lang.Integer
     */
    public void setKitodoId(Integer kitodoId) {
        this.kitodoId = kitodoId;
    }

    /**
     * Get viaducId.
     *
     * @return value of viaducId
     */
    public Integer getViaducId() {
        return viaducId;
    }

    /**
     * Set viaducId.
     *
     * @param viaducId as java.lang.Integer
     */
    public void setViaducId(Integer viaducId) {
        this.viaducId = viaducId;
    }

    /**
     * Get numberOfScans.
     *
     * @return value of numberOfScans
     */
    public Integer getNumberOfScans() {
        return numberOfScans;
    }

    /**
     * Set numberOfScans.
     *
     * @param numberOfScans as java.lang.Integer
     */
    public void setNumberOfScans(Integer numberOfScans) {
        this.numberOfScans = numberOfScans;
    }

    /**
     * Get complexity.
     *
     * @return value of complexity
     */
    public String getComplexity() {
        return complexity;
    }

    /**
     * Set complexity.
     *
     * @param complexity as java.lang.String
     */
    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    /**
     * Get correctionWorkflowActivated.
     *
     * @return value of correctionWorkflowActivated
     */
    public Boolean getCorrectionWorkflowActivated() {
        return correctionWorkflowActivated;
    }

    /**
     * Set correctionWorkflowActivated.
     *
     * @param correctionWorkflowActivated as java.lang.Boolean
     */
    public void setCorrectionWorkflowActivated(Boolean correctionWorkflowActivated) {
        this.correctionWorkflowActivated = correctionWorkflowActivated;
    }

    /**
     * Get structuralError.
     *
     * @return value of structuralError
     */
    public Boolean getStructuralError() {
        return structuralError;
    }

    /**
     * Set structuralError.
     *
     * @param structuralError as java.lang.Boolean
     */
    public void setStructuralError(Boolean structuralError) {
        this.structuralError = structuralError;
    }

    /**
     * Get missingInformation.
     *
     * @return value of missingInformation
     */
    public Boolean getMissingInformation() {
        return missingInformation;
    }

    /**
     * Set missingInformation.
     *
     * @param missingInformation as java.lang.Boolean
     */
    public void setMissingInformation(Boolean missingInformation) {
        this.missingInformation = missingInformation;
    }

    /**
     * Get reimport.
     *
     * @return value of reimport
     */
    public Boolean getReimport() {
        return reimport;
    }

    /**
     * Set reimport.
     *
     * @param reimport as java.lang.Boolean
     */
    public void setReimport(Boolean reimport) {
        this.reimport = reimport;
    }

    /**
     * Get uploadDestination.
     *
     * @return value of uploadDestination
     */
    public String getUploadDestination() {
        return uploadDestination;
    }

    /**
     * Set uploadDestination.
     *
     * @param uploadDestination as java.lang.String
     */
    public void setUploadDestination(String uploadDestination) {
        this.uploadDestination = uploadDestination;
    }

    /**
     * Get canceled.
     *
     * @return value of canceled
     */
    public Boolean getCanceled() {
        return canceled;
    }

    /**
     * Set canceled.
     *
     * @param canceled as java.lang.Boolean
     */
    public void setCanceled(Boolean canceled) {
        this.canceled = canceled;
    }

    /**
     * Get canceledTimestamp.
     *
     * @return value of canceledTimestamp
     */
    public LocalDateTime getCanceledTimestamp() {
        return canceledTimestamp;
    }

    /**
     * Set canceledTimestamp.
     *
     * @param canceledTimestamp as java.time.LocalDateTime
     */
    public void setCanceledTimestamp(LocalDateTime canceledTimestamp) {
        this.canceledTimestamp = canceledTimestamp;
    }

    /**
     * Get canceledMessage.
     *
     * @return value of canceledMessage
     */
    public String getCanceledMessage() {
        return canceledMessage;
    }

    /**
     * Set canceledMessage.
     *
     * @param canceledMessage as java.lang.String
     */
    public void setCanceledMessage(String canceledMessage) {
        this.canceledMessage = canceledMessage;
    }

    /**
     * Get workingCopy.
     *
     * @return value of workingCopy
     */
    public Boolean getWorkingCopy() {
        return workingCopy;
    }

    /**
     * Set workingCopy.
     *
     * @param workingCopy as java.lang.Boolean
     */
    public void setWorkingCopy(Boolean workingCopy) {
        this.workingCopy = workingCopy;
    }

    /**
     * Get reportCreatedTimestamp.
     *
     * @return value of reportCreatedTimestamp
     */
    public LocalDateTime getReportCreatedTimestamp() {
        return reportCreatedTimestamp;
    }

    /**
     * Set reportCreatedTimestamp.
     *
     * @param reportCreatedTimestamp as java.time.LocalDateTime
     */
    public void setReportCreatedTimestamp(LocalDateTime reportCreatedTimestamp) {
        this.reportCreatedTimestamp = reportCreatedTimestamp;
    }

    /**
     * Get taskData.
     *
     * @return value of taskData
     */
    @Convert(converter = TaskDataConverter.class)
    public HashMap<String, List<HashMap<String, Object>>> getTaskData() {
        return taskData;
    }

    /**
     * Set taskData.
     *
     * @param taskData as java.util.Map<java.lang.String,java.lang.Object>
     */
    @Convert(converter = TaskDataConverter.class)
    public void setTaskData(HashMap<String, List<HashMap<String, Object>>> taskData) {
        this.taskData = taskData;
    }
}
