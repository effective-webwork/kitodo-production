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

package org.kitodo.production.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.production.enums.FilterString;
import org.kitodo.production.forms.process.ProcessListView;
import org.kitodo.production.forms.task.TaskListView;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("SearchForm")
@RequestScoped
public class SearchForm {

    /**
     * Logger instance.
     */
    private static final Logger logger = LogManager.getLogger(SearchForm.class);

    private List<String> projects = new ArrayList<>(); // proj:
    private String project = "";

    private List<String> processMetadataTitles;
    private String processMetadataTitle = "";
    private String processMetadataValue = "";

    private List<String> stepTitles = new ArrayList<>(); // step:
    private List<TaskStatus> stepstatus = new ArrayList<>();
    private String status = "";
    private String stepname = "";

    private List<User> user = new ArrayList<>();
    private String stepdonetitle = "";
    private String stepdoneuser = "";

    private String idin = "";
    private String processParentId = "";
    private String processTitle = ""; // proc:

    private String projectOperand = "";
    private String processOperand = "";
    private String processMetadataOperand = "";
    private String stepOperand = "";

    private Boolean showInactiveProjects = false;
    private Boolean showClosedProcesses = false;

    /**
     * Initializes SearchForm.
     */
    public SearchForm() {
        this.stepstatus.addAll(ServiceManager.getFilterService().initStepStatus());
        this.projects = ServiceManager.getFilterService().initProjects();
        this.stepTitles = ServiceManager.getFilterService().initStepTitles();
        this.processMetadataTitles = ServiceManager.getFilterService().initProcessPropertyTitles();
        this.user.addAll(ServiceManager.getFilterService().initUserList());
    }

    public List<String> getProjects() {
        return this.projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }

    /**
     * Returns the list of process metadata titles.
     * @return the list of process metadata titles
     */
    public List<String> getProcessMetadataTitles() {
        return this.processMetadataTitles;
    }

    /**
     * Sets the list of process metadata titles.
     * @param processMetadataTitles the list of process metadata titles to set
     */
    public void setProcessMetadataTitles(List<String> processMetadataTitles) {
        this.processMetadataTitles = processMetadataTitles;
    }

    public List<String> getStepTitles() {
        return this.stepTitles;
    }

    public void setStepTitles(List<String> stepTitles) {
        this.stepTitles = stepTitles;
    }

    public List<TaskStatus> getStepstatus() {
        return this.stepstatus;
    }

    public void setStepstatus(List<TaskStatus> stepstatus) {
        this.stepstatus = stepstatus;
    }

    public String getStepdonetitle() {
        return this.stepdonetitle;
    }

    public void setStepdonetitle(String stepdonetitle) {
        this.stepdonetitle = stepdonetitle;
    }

    public String getStepdoneuser() {
        return this.stepdoneuser;
    }

    public void setStepdoneuser(String stepdoneuser) {
        this.stepdoneuser = stepdoneuser;
    }

    public String getIdin() {
        return this.idin;
    }

    public void setIdin(String idin) {
        this.idin = idin;
    }

    public String getProcessParentId() {
        return processParentId;
    }

    public void setProcessParentId(String processParentId) {
        this.processParentId = processParentId;
    }

    public String getProject() {
        return this.project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getProcessTitle() {
        return this.processTitle;
    }

    public void setProcessTitle(String processTitle) {
        this.processTitle = processTitle;
    }

    /**
     * Returns the title of the process metadata to search for.
     * @return the title of the process metadata to search for
     */
    public String getProcessMetadataTitle() {
        return this.processMetadataTitle;
    }

    /**
     * Sets the title of the process metadata to search for.
     * @param processPropertyTitle the title of the process metadata to search for
     */
    public void setProcessMetadataTitle(String processPropertyTitle) {
        this.processMetadataTitle = processPropertyTitle;
    }

    /**
     * Returns the value of the process metadata to search for.
     * @return the value of the process metadata to search for
     */
    public String getProcessMetadataValue() {
        return this.processMetadataValue;
    }

    /**
     * Sets the value of the process metadata to search for.
     * @param processMetadataValue the value of the process metadata to search for
     */
    public void setProcessMetadataValue(String processMetadataValue) {
        this.processMetadataValue = processMetadataValue;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStepname() {
        return this.stepname;
    }

    public void setStepname(String stepname) {
        this.stepname = stepname;
    }

    public List<User> getUser() {
        return this.user;
    }

    public void setUser(List<User> user) {
        this.user = user;
    }

    /**
     * Return whether to show matching processes for inactive projects.
     * 
     * @return whether to show matching processes for inactive projects
     */
    public boolean getShowInactiveProjects() {
        return this.showInactiveProjects;
    }

    /**
     * Set whether to show matching processes for inactive projects.
     * 
     * @param showInactiveProjects whether to show matching processes for inactive projects
     */
    public void setShowInactiveProjects(boolean showInactiveProjects) {
        this.showInactiveProjects = showInactiveProjects;
    }

    /**
     * Return whether to show matching processes that are already closed.
     * 
     * @return whether to show matching processes that are already closed
     */
    public boolean getShowClosedProcesses() {
        return this.showClosedProcesses;
    }

    /**
     * Set whether to show matching processes that are already closed.
     * 
     * @param showClosedProcesses whether to show matching processes that are already closed
     */
    public void setShowClosedProcesses(boolean showClosedProcesses) {
        this.showClosedProcesses = showClosedProcesses;
    }

    /**
     * Filter processes.
     *
     * @return filter as java.lang.String
     */
    public String filterProcesses() {
        return ProcessListView.getViewPath(createFilter(), this.showInactiveProjects, this.showClosedProcesses);
    }

    /**
     * Filter tasks.
     *
     * @return filter as java.lang.String
     */
    public String filterTasks() {
        return TaskListView.getViewPath(createFilter());
    }

    private String createFilter() {
        String search = "";
        if (!this.processTitle.isEmpty()) {
            search += "\"" + this.processOperand + "process:" + this.processTitle + "\" ";
        }
        if (!this.idin.isEmpty()) {
            search += "\"" + FilterString.ID.getFilterEnglish() + this.idin + "\" ";
        }
        if (!this.processParentId.isEmpty()) {
            search += "\"" + FilterString.PARENTPROCESSID.getFilterEnglish() + this.processParentId + "\" ";
        }
        if (!this.project.isEmpty()) {
            search += "\"" + this.projectOperand + FilterString.PROJECT.getFilterEnglish() + this.project + "\" ";
        }
        if (!this.stepname.isEmpty()) {
            search += "\"" + this.stepOperand + this.status + ":" + this.stepname + "\" ";
        }
        if (!this.stepdonetitle.isEmpty() && !this.stepdoneuser.isEmpty()
                && ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.WITH_USER_STEP_DONE_SEARCH)) {
            search += "\"" + FilterString.TASKDONEUSER.getFilterEnglish() + this.stepdoneuser + "\" \""
                    + FilterString.TASKDONETITLE.getFilterEnglish() + this.stepdonetitle + "\" ";
        }
        if (StringUtils.isNotBlank(this.processMetadataValue)) {
            if (StringUtils.isNotBlank(this.processMetadataTitle)) {
                search += "\"" + this.processMetadataOperand + FilterString.PROPERTY.getFilterEnglish()
                        + this.processMetadataTitle + ":" + this.processMetadataValue + "\" ";
            } else {
                search += "\"" + this.processMetadataOperand + FilterString.PROPERTY.getFilterEnglish()
                        + "*:" + this.processMetadataValue + "\" ";
            }
        } else {
            if (StringUtils.isNotBlank(this.processMetadataTitle)) {
                search += "\"" + this.processMetadataOperand + FilterString.PROPERTY.getFilterEnglish()
                        + this.processMetadataTitle + ":*\" ";
            }
        }
        return search;
    }

    private String createSearchProperty(String title, String value, String operand, FilterString filterString) {
        if (Objects.nonNull(value) && !value.isEmpty()) {
            if (Objects.nonNull(title) && !title.isEmpty()) {
                return "\"" + operand + filterString.getFilterEnglish() + title + ":" + value + "\" ";
            } else {
                return "\"" + operand + filterString.getFilterEnglish() + value + "\" ";
            }
        }
        return "";
    }

    /**
     * Get operands.
     *
     * @return list of SelectItem objects
     */
    public List<SelectItem> getOperands() {
        List<SelectItem> answer = new ArrayList<>();
        SelectItem and = new SelectItem("", Helper.getTranslation("AND"));
        SelectItem not = new SelectItem("-", Helper.getTranslation("NOT"));
        answer.add(and);
        answer.add(not);
        return answer;
    }

    public String getProjectOperand() {
        return this.projectOperand;
    }

    public void setProjectOperand(String projectOperand) {
        this.projectOperand = projectOperand;
    }

    /**
     * Returns the operand for the process metadata filter.
     * @return the operand for the process metadata filter
     */
    public String getProcessMetadataOperand() {
        return this.processMetadataOperand;
    }

    /**
     * Sets the operand for the process metadata filter.
     * @param processMetadataOperand the operand for the process metadata filter to set
     */
    public void setProcessMetadataOperand(String processMetadataOperand) {
        this.processMetadataOperand = processMetadataOperand;
    }

    public String getStepOperand() {
        return this.stepOperand;
    }

    public void setStepOperand(String stepOperand) {
        this.stepOperand = stepOperand;
    }

    public String getProcessOperand() {
        return this.processOperand;
    }

    public void setProcessOperand(String processOperand) {
        this.processOperand = processOperand;
    }

}
