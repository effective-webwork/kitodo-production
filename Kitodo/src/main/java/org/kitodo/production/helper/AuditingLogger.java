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

package org.kitodo.production.helper;

import java.util.Objects;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.enums.LogEntryType;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.relayserver.services.JobService;

public class AuditingLogger {

    /**
     * Create and return a log entry String for adding or removing a property from an object.
     *
     * @param type LogEntryType to be created
     * @param propertyType property type that is added or removed (i.e. "Client", "Template" etc.)
     * @param propertyName name of property that is added or removed
     * @param propertyId id of property that is added or removed
     * @param objectType object type that to which property is added or removed (i.e. "User", "Project" etc.)
     * @param objectName name of object to which property is added or removed
     * @param objectId id of object to which property is added or removed
     * @return log entry String
     */
    public static String createPropertyAddOrRemoveLogEntry(LogEntryType type, String propertyType, String propertyName,
                                                           Integer propertyId, String objectType, String objectName,
                                                           Integer objectId) {
        User currentUser = ServiceManager.getUserService().getAuthenticatedUser();
        return String.format(type.getTemplateString(), currentUser.getFullName(), currentUser.getId(), propertyType,
                propertyName, propertyId, objectType, objectName, objectId);
    }

    /**
     * Create and return a log entry String for updating a field value of an object.
     * @param field name of field whose value is updated
     * @param oldValue old field value
     * @param newValue new field value
     * @param objectType type of object whose field is updated
     * @param objectName name of object whose field is updated
     * @param objectId id of object whose field is updated
     * @return log entry String
     */
    public static String createFieldUpdateLogEntry(String field, String oldValue, String newValue, String objectType,
                                                   String objectName, Integer objectId) {
        User currentUser = ServiceManager.getUserService().getAuthenticatedUser();
        return String.format(LogEntryType.UPDATE.getTemplateString(), currentUser.getFullName(), currentUser.getId(),
                field, objectType, objectName, objectId, oldValue, newValue);
    }

    /**
     * Create and return a log entry String for saving or deleting an object.
     * @param type LogEntryType to be created
     * @param objectType type of object that is saved or deleted
     * @param objectName name of object that is saved or deleted
     * @param objectId id of object that is saved or deleted
     * @return log entry String
     */
    public static String createObjectSaveOrDeleteLogEntry(LogEntryType type, String objectType, String objectName,
                                                          Integer objectId) {
        User currentUser = ServiceManager.getUserService().getAuthenticatedUser();
        return String.format(type.getTemplateString(), currentUser.getFullName(), currentUser.getId(), objectType,
                objectName, objectId);
    }

    /**
     * Create and return a log entry String for manipulating a process.
     * @param type LogEntryType to be created
     * @param process Process to be manipulated
     * @return log entry String
     */
    public static String createProcessManipulationLogEntry(LogEntryType type, Process process) {
        User currentUser = ServiceManager.getUserService().getAuthenticatedUser();
        return String.format(type.getTemplateString(), currentUser.getFullName(),
                currentUser.getId(), process.getTitle(), JobService.getViaducId(process), process.getId());
    }

    /**
     * Create and return a log entry String for closing the given task.
     * @param task Task to be closed
     * @return log entry String
     */
    public static String createProcessTaskClosedLogEntry(Task task) {
        User currentUser = ServiceManager.getUserService().getAuthenticatedUser();
        if (Objects.nonNull(currentUser)) {
            return String.format(LogEntryType.TASK_CLOSED.getTemplateString(), currentUser.getFullName(),
                    currentUser.getId(), task.getTitle(), task.getId(), task.getProcess().getTitle(),
                    JobService.getViaducId(task.getProcess()), task.getProcess().getId());
        }
        else {
            return "ERROR: unable to create 'close task' log entry: current user is 'null'!";
        }
    }

    /**
     * Create and return a log entry String for assigning or unassigning the given task to the current user.
     * @param task Task to be assigned or unassigned
     * @return log entry String
     */
    public static String createProcessTaskAssignedLogEntry(LogEntryType type, Task task) {
        User currentUser = ServiceManager.getUserService().getAuthenticatedUser();
        if (Objects.nonNull(currentUser)) {
            return String.format(type.getTemplateString(), task.getTitle(), task.getId(),
                    task.getProcess().getTitle(), JobService.getViaducId(task.getProcess()), task.getProcess().getId(),
                    currentUser.getFullName(), currentUser.getId());
        } else {
            return "ERROR: unable to create 'task assigned' log entry: current user is 'null'!";
        }
    }

    /**
     * Create and return a log entry String for changing the Viaduc state of a digitization job.
     * @param process Process to log
     * @param oldState old Viaduc state
     * @param newState new Viaduc state
     * @return log entry String
     */
    public static String createProcessStateChangedLogEntry(Process process, String oldState, String newState) {
        return String.format(LogEntryType.STATE_CHANGED.getTemplateString(), process.getTitle(),
                JobService.getViaducId(process), process.getId(), oldState, newState);
    }

    /**
     * Create and return a log entry String for manually changing the task state of the given Task 'task'.
     * @param task Task to log
     * @param previousState previous task state
     * @return log entry String
     */
    public static String createTaskStateChangedLogEntry(Task task, String previousState) {
        User currentUser = ServiceManager.getUserService().getAuthenticatedUser();
        if (Objects.nonNull(currentUser)) {
            return String.format(LogEntryType.TASK_STATE_CHANGED.getTemplateString(), task.getTitle(), task.getId(),
                    task.getProcess().getTitle(), JobService.getViaducId(task.getProcess()), task.getProcess().getId(),
                    task.getProcessingStatus().getTitle(), previousState, currentUser.getFullName(), currentUser.getId());
        } else {
            return "ERROR: unable to create 'task state changed' log entry: current user is 'null'!";
        }
    }

    /**
     * Create and return a log entry String for adding or removing a structure element from to or from a given process.
     * @param type LogEntryType to create
     * @param elementType type of the structural element
     * @param label label of created structure element
     * @param process Process to which partial job is added
     * @return log entry String
     */
    public static String createStructuralElementAddedOrRemovedLogEntry(LogEntryType type, String elementType,
                                                                       String label, Process process) {
        User currentUser = ServiceManager.getUserService().getAuthenticatedUser();
        if (Objects.nonNull(currentUser)) {
            return String.format(type.getTemplateString(), currentUser.getFullName(), currentUser.getId(),
                    elementType, label, process.getTitle(), JobService.getViaducId(process), process.getId());
        } else {
            return "ERROR: unable to create 'structural element added' log entry: current user is 'null'!";
        }
    }

    /**
     * Create and return a log entry String for saving the metadata of the given process.
     * @param process process whose metadata is saved
     * @return log entry String
     */
    public static String createMetadataSaveLogEntry(Process process) {
        User currentUser = ServiceManager.getUserService().getAuthenticatedUser();
        if (Objects.nonNull(currentUser)) {
            return String.format(LogEntryType.METADATA_EDITOR_SAVED.getTemplateString(), currentUser.getFullName(),
                    currentUser.getId(), process.getTitle(), JobService.getViaducId(process), process.getId());
        } else {
            return "ERROR: unable to create 'metadata saved' log entry: current user is 'null'!";
        }
    }

}
