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

package org.kitodo.production.enums;

public enum LogEntryType {

    UPDATE("User '%s' (ID: %s) updated field '%s' of %s '%s' (ID: %s) from '%s' to '%s'"),
    ADD("User '%s' (ID: %s) added %s '%s' (ID: %s) to %s '%s' (ID: %s)"),
    REMOVE("User '%s' (ID: %s) removed %s '%s' (ID: %s) from %s '%s' (ID: %s)"),
    SAVE("User '%s' (ID: %s) saved %s '%s' (ID: %s)"),
    DELETE("User '%s' (ID: %s) deleted %s '%s' (ID: %s)"),
    PROCESS_SAVE("User '%s' (ID: %s) saved process '%s' (Viaduc ID: %s, Kitodo ID: %s)"),
    PROCESS_DELETE("User '%s' (ID: %s) deleted process '%s' (Viaduc ID: %s, Kitodo ID: %s)"),
    PROCESS_CANCEL("User '%s' (ID: %s) canceled process '%s' (Viaduc ID: %s, Kitodo ID: %s)"),
    TASK_CLOSED("User '%s' (ID: %s) closed task '%s' (ID: %s) of process '%s' (Viaduc ID: %s, Kitodo ID: %s)"),
    TASK_ASSIGNED("Task '%s' (ID: %s) of process '%s' (Viaduc ID: %s, Kitodo ID: %s) assigned to user '%s' (ID: %s)"),
    TASK_UNASSIGNED("Task '%s' (ID: %s) of process '%s' (Viaduc ID: %s, Kitodo ID: %s) unassigned from user '%s' (ID: %s)"),
    TASK_STATE_CHANGED("State of task '%s' (ID: %s) of process '%s' (Viaduc ID: %s, Kitodo ID: %s) manually changed from '%s' to '%s' by user '%s' (ID: %s)"),
    STATE_CHANGED("Job state of process '%s' (Viaduc ID: %s, Kitodo ID: %s) changed from '%s' to '%s'"),
    METADATA_EDITOR_STRUCTURAL_ELEMENT_ADDED("User '%s' (ID: %s) added %s '%s' to metadata of process '%s' (Viaduc ID: %s, Kitodo ID: %s)"),
    METADATA_EDITOR_STRUCTURAL_ELEMENT_REMOVED("User '%s' (ID: %s) removed %s '%s' from metadata of process '%s' (Viaduc ID: %s, Kitodo ID: %s)"),
    METADATA_EDITOR_SAVED("User '%s' (ID: %s) saved metadata of process '%s' (Viaduc ID: %s, Kitodo ID: %s)");

    String templateString;

    LogEntryType(String logString) {
        this.templateString = logString;
    }

    /**
     * Return template string.
     *
     * @return template string
     */
    public String getTemplateString() {
        return this.templateString;
    }
}
