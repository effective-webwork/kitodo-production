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

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped
public class ErrorDialog {

    private String errorMessage;

    private String errorDescription;

    public ErrorDialog() {}

    public ErrorDialog(String errorMessage, String errorDescription) {
        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
    }

    /**
     * Get errorMessage.
     *
     * @return value of errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get errorDescription.
     *
     * @return value of errorDescription
     */
    public String getErrorDescription() {
        return errorDescription;
    }
}
