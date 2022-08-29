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

package org.kitodo.selenium.testframework.pages;

import static org.awaitility.Awaitility.await;
import static org.kitodo.selenium.testframework.Browser.getCellsOfRow;
import static org.kitodo.selenium.testframework.Browser.getRowsOfTable;
import static org.kitodo.selenium.testframework.Browser.getTableDataByColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.kitodo.MockDatabase;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.selenium.testframework.Browser;
import org.kitodo.selenium.testframework.Pages;
import org.kitodo.selenium.testframework.enums.TabIndex;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProjectsPage extends Page<ProjectsPage> {

    private static final String PROJECTS_TAB_VIEW = "projectsTabView";
    private static final String PROJECTS_TABLE = "projectsTable";
    private static final String TEMPLATE_TABLE = "templateTable";
    private static final String WORKFLOW_TABLE = "workflowTable";
    private static final String DOCKET_TABLE = "docketTable";
    private static final String RULESET_TABLE = "rulesetTable";

    @SuppressWarnings("unused")
    @FindBy(id = PROJECTS_TAB_VIEW)
    private WebElement projectsTabView;

    @SuppressWarnings("unused")
    @FindBy(id = PROJECTS_TABLE + DATA)
    private WebElement projectsTable;

    @SuppressWarnings("unused")
    @FindBy(id = TEMPLATE_TABLE + DATA)
    private WebElement templatesTable;

    @SuppressWarnings("unused")
    @FindBy(id = WORKFLOW_TABLE + DATA)
    private WebElement workflowsTable;

    @SuppressWarnings("unused")
    @FindBy(id = DOCKET_TABLE + DATA)
    private WebElement docketsTable;

    @SuppressWarnings("unused")
    @FindBy(id = RULESET_TABLE + DATA)
    private WebElement rulesetsTable;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newElementButton_button")
    private WebElement newElementButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newProjectButton")
    private WebElement newProjectButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newTemplateButton")
    private WebElement newTemplateButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newWorkflowButton")
    private WebElement newWorkflowButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newDocketButton")
    private WebElement newDocketButton;

    @SuppressWarnings("unused")
    @FindBy(id = "projectForm:newRulesetButton")
    private WebElement newRulesetButton;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/projectEdit.jsf?referer=projects&id=1']")
    private WebElement editProjectLink;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/templateEdit.jsf?id=4']")
    private WebElement editTemplateLink;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/workflowEdit.jsf?id=2']")
    private WebElement editWorkflowLink;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/docketEdit.jsf?id=1']")
    private WebElement editDocketLink;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/rulesetEdit.jsf?id=1']")
    private WebElement editRulesetLink;

    @SuppressWarnings("unused")
    @FindBy(xpath = "//a[@href='/kitodo/pages/importConfigurationEdit.jsf?id=1']")
    private WebElement editImportConfigurationLink;

    @SuppressWarnings("unused")
    @FindBy(id = TEMPLATE_TABLE + ":0:templateActionForm:action22")
    private WebElement createProcess;

    @SuppressWarnings("unused")
    @FindBy(id = PROJECTS_TABLE + ":0:projectActionForm:deleteProject")
    private WebElement deleteFirstProjectButton;

    @SuppressWarnings("unused")
    @FindBy(id = DOCKET_TABLE + ":0:actionForm:deleteDocket")
    private WebElement deleteFirstDocketButton;

    @SuppressWarnings("unused")
    @FindBy(id = RULESET_TABLE + ":0:actionForm:deleteRuleset")
    private WebElement deleteFirstRulesetButton;

    public ProjectsPage() {
        super("pages/projects.jsf");
    }

    /**
     * Goes to projects page.
     *
     * @return The projects page.
     */
    public ProjectsPage goTo() throws Exception {
        Pages.getTopNavigation().gotoProjects();
        await("Wait for execution of link click").pollDelay(Browser.getDelayMinAfterLinkClick(), TimeUnit.MILLISECONDS)
                .atMost(Browser.getDelayMaxAfterLinkClick(), TimeUnit.MILLISECONDS).ignoreExceptions()
                .until(this::isAt);
        return this;
    }

    public int countListedProjects() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getRowsOfTable(projectsTable).size();
    }

    public int countListedTemplates() throws Exception {
        switchToTabByIndex(TabIndex.TEMPLATES.getIndex());
        return getRowsOfTable(templatesTable).size();
    }

    public int countListedWorkflows() throws Exception {
        switchToTabByIndex(TabIndex.WORKFLOWS.getIndex());
        return getRowsOfTable(workflowsTable).size();
    }

    public int countListedDockets() throws Exception {
        switchToTabByIndex(TabIndex.DOCKETS.getIndex());
        return getRowsOfTable(docketsTable).size();
    }

    public int countListedRulesets() throws Exception {
        switchToTabByIndex(TabIndex.RULESETS.getIndex());
        return getRowsOfTable(rulesetsTable).size();
    }

    /**
     * Return a list of all project titles which were displayed on clients page.
     *
     * @return list of project titles
     */
    public List<String> getProjectsTitles() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        return getTableDataByColumn(projectsTable, 1);
    }

    public List<String> getProjectsActiveStates() {
        List<WebElement> rowsOfTable = getRowsOfTable(projectsTable);
        List<String> styleClasses = new ArrayList<>();
        for (WebElement row : rowsOfTable) {
            styleClasses.add(getCellsOfRow(row).get(3).findElement(By.tagName("i")).getAttribute("class"));
        }
        return styleClasses;
    }

    /**
     * Returns a list of all template titles which were displayed on workflows
     * page.
     *
     * @return list of template titles
     */
    public List<String> getTemplateTitles() throws Exception {
        switchToTabByIndex(TabIndex.TEMPLATES.getIndex());
        return getTableDataByColumn(templatesTable, 1);
    }

    /**
     * Returns a list of all workflow titles which were displayed on workflows
     * page.
     *
     * @return list of workflow titles
     */
    public List<String> getWorkflowTitles() throws Exception {
        switchToTabByIndex(TabIndex.WORKFLOWS.getIndex());
        return getTableDataByColumn(workflowsTable, 0);
    }

    /**
     * Returns a list of all docket titles which were displayed on dockets page.
     *
     * @return list of docket titles
     */
    public List<String> getDocketTitles() throws Exception {
        switchToTabByIndex(TabIndex.DOCKETS.getIndex());
        return getTableDataByColumn(docketsTable, 0);
    }

    /**
     * Returns a list of all ruleset titles which were displayed on rulesets
     * page.
     *
     * @return list of ruleset titles
     */
    public List<String> getRulesetTitles() throws Exception {
        switchToTabByIndex(TabIndex.RULESETS.getIndex());
        return getTableDataByColumn(rulesetsTable, 0);
    }

    public void createNewProcess() throws Exception {
        switchToTabByIndex(TabIndex.TEMPLATES.getIndex());

        int index = triggerRowToggle(templatesTable, "First template");
        WebElement createProcess = Browser.getDriver()
                .findElement(By.id(TEMPLATE_TABLE + ":" + index + ":createProcessForm:projects:0:createProcess"));
        clickButtonAndWaitForRedirect(createProcess, Pages.getProcessFromTemplatePage().getUrl());
    }

    public List<String> getProjectDetails() {
        int index = triggerRowToggle(projectsTable, "First project");
        WebElement detailsTable = Browser.getDriver()
                .findElement(By.id(PROJECTS_TABLE + ":" + index + ":projectDetailTable"));
        return getTableDataByColumn(detailsTable, 1);
    }

    public List<String> getProjectTemplates() {
        int index = triggerRowToggle(projectsTable, "First project");
        WebElement templatesTable = Browser.getDriver()
                .findElement(By.id(PROJECTS_TABLE + ":" + index + ":projectTemplatesTable"));
        return templatesTable.findElements(By.className("expansion-list-item-title"))
                .stream().map(e -> e.getAttribute("innerText")).collect(Collectors.toList());
    }

    public List<String> getTemplateDetails() {
        int index = triggerRowToggle(templatesTable, "Fourth template");
        WebElement detailsTable = Browser.getDriver()
                .findElement(By.id(TEMPLATE_TABLE + ":" + index + ":templateRowExpansionTable"));
        List<String> details = getTableDataByColumn(detailsTable, 1);
        details.addAll(getTableDataByColumn(detailsTable, 3));
        return details;
    }

    /**
     * Go to edit page for creating a new project.
     *
     * @return project edit page
     */
    public ProjectEditPage createNewProject() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        clickButtonAndWaitForRedirect(newProjectButton, Pages.getProjectEditPage().getUrl());
        return Pages.getProjectEditPage();
    }

    /**
     * Go to edit page for creating a new template.
     *
     * @return template edit page
     */
    public TemplateEditPage createNewTemplate() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();

        clickButtonAndWaitForRedirect(newTemplateButton, Pages.getTemplateEditPage().getUrl());
        return Pages.getTemplateEditPage();
    }

    /**
     * Go to edit page for creating a new workflow.
     *
     * @return workflow edit page
     */
    public WorkflowEditPage createNewWorkflow() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        clickButtonAndWaitForRedirect(newWorkflowButton, Pages.getWorkflowEditPage().getUrl());
        return Pages.getWorkflowEditPage();
    }

    /**
     * Go to edit page for creating a new docket.
     *
     * @return docket edit page
     */
    public DocketEditPage createNewDocket() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        clickButtonAndWaitForRedirect(newDocketButton, Pages.getDocketEditPage().getUrl());
        return Pages.getDocketEditPage();
    }

    /**
     * Go to edit page for creating a new ruleset.
     *
     * @return ruleset edit page
     */
    public RulesetEditPage createNewRuleset() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        newElementButton.click();
        clickButtonAndWaitForRedirect(newRulesetButton, Pages.getRulesetEditPage().getUrl());
        return Pages.getRulesetEditPage();
    }

    /**
     * Go to edit page for creating a new project.
     *
     * @return project edit page
     */
    public ProjectEditPage editProject() throws Exception {
        if (isNotAt()) {
            goTo();
        }
        clickButtonAndWaitForRedirect(editProjectLink, Pages.getProjectEditPage().getUrl());
        return Pages.getProjectEditPage();
    }

    /**
     * Go to edit page for creating a new template.
     *
     * @return template edit page
     */
    public TemplateEditPage editTemplate() throws Exception {
        switchToTabByIndex(TabIndex.TEMPLATES.getIndex());

        clickButtonAndWaitForRedirect(editTemplateLink, Pages.getTemplateEditPage().getUrl());
        return Pages.getTemplateEditPage();
    }

    /**
     * Go to edit page for creating a new workflow.
     *
     * @return workflow edit page
     */
    public WorkflowEditPage editWorkflow() throws Exception {
        switchToTabByIndex(TabIndex.WORKFLOWS.getIndex());

        clickButtonAndWaitForRedirect(editWorkflowLink, Pages.getWorkflowEditPage().getUrl());
        return Pages.getWorkflowEditPage();
    }

    /**
     * Go to edit page for creating a new docket.
     *
     * @return docket edit page
     */
    public DocketEditPage editDocket() throws Exception {
        switchToTabByIndex(TabIndex.DOCKETS.getIndex());

        clickButtonAndWaitForRedirect(editDocketLink, Pages.getDocketEditPage().getUrl());
        return Pages.getDocketEditPage();
    }

    /**
     * Go to edit page for creating a new ruleset.
     *
     * @return ruleset edit page
     */
    public RulesetEditPage editRuleset() throws Exception {
        switchToTabByIndex(TabIndex.RULESETS.getIndex());

        clickButtonAndWaitForRedirect(editRulesetLink, Pages.getRulesetEditPage().getUrl());
        return Pages.getRulesetEditPage();
    }

    /**
     * Go to edit import configuration page.
     */
    public ImportConfigurationEditPage editImportConfiguration() throws Exception {
        switchToTabByIndex(TabIndex.IMPORT_CONFIGURATIONS.getIndex());
        clickButtonAndWaitForRedirect(editImportConfigurationLink, Pages.getImportConfigurationEditPage().getUrl());
        return Pages.getImportConfigurationEditPage();
    }

    /**
     * Remove docket from corresponding list on project page.
     */
    public void deleteDocket() throws Exception {
        deleteElement("Docket", MockDatabase.getRemovableObjectIDs().get(ObjectType.DOCKET.name()),
            TabIndex.DOCKETS.getIndex(), projectsTabView);
    }

    /**
     * Remove ruleset from corresponding list on project page.
     */
    public void deleteRuleset() throws Exception {
        deleteElement("Ruleset", MockDatabase.getRemovableObjectIDs().get(ObjectType.RULESET.name()),
            TabIndex.RULESETS.getIndex(), projectsTabView);
    }

    /**
     * Switch to template Tab.
     */
    public ProjectsPage goToTemplateTab() throws Exception {
        switchToTabByIndex(TabIndex.TEMPLATES.getIndex());
        return this;

    }

    /**
     * Switch to template Tab.
     */
    public ProjectsPage goToWorkflowTab() throws Exception {
        switchToTabByIndex(TabIndex.WORKFLOWS.getIndex());
        return this;

    }

    /**
     * Clicks on the tab indicated by given index (starting with 0 for the first
     * tab).
     *
     * @param index
     *            of tab to be clicked
     */
    private void switchToTabByIndex(int index) throws Exception {
        switchToTabByIndex(index, projectsTabView);
    }

    public String getWorkflowStatusForWorkflow() {
        List<String> tableDataByColumn = getTableDataByColumn(workflowsTable, 1);
        return tableDataByColumn.get(1);
    }
}
