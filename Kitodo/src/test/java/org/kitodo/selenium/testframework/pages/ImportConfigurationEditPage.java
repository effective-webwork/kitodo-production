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

import java.util.concurrent.TimeUnit;

import org.kitodo.selenium.testframework.Browser;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ImportConfigurationEditPage extends EditPage<ImportConfigurationEditPage> {

    private static final String ERROR_MESSAGES_ID = "editForm:error-messages";

    @SuppressWarnings("unused")
    @FindBy(className = "ui-chkbox-box")
    private WebElement prestructuredImportSwitch;

    public ImportConfigurationEditPage() {
        super("pages/importConfigurationEdit.jsf");
    }

    @Override
    public ImportConfigurationEditPage goTo() throws Exception {
        return null;
    }

    public void save() {
        clickElement(saveButton);
    }

    public void togglePrestructuredImport() throws InterruptedException {
        await("Wait for save button to be displayed").pollDelay(700, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS).ignoreExceptions().until(() -> saveButton.isDisplayed());
        prestructuredImportSwitch.click();
        Thread.sleep(2000);
    }

    public String getErrorMessage() {
        try {
            WebElement errorMessagesElement = Browser.getDriver().findElementById(ERROR_MESSAGES_ID);
            await("Wait for error messages to be displayed").pollDelay(700, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS).ignoreExceptions().until(errorMessagesElement::isDisplayed);
            return errorMessagesElement.getText();
        } catch (StaleElementReferenceException e) {
            WebElement errorMessagesElement = Browser.getDriver().findElementById(ERROR_MESSAGES_ID);
            await("Wait for error messages to be displayed").pollDelay(700, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS).ignoreExceptions().until(errorMessagesElement::isDisplayed);
            return errorMessagesElement.getText();
        }
    }
}
