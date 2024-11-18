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

package org.kitodo.production.services.relayserver.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.PremisException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.workflow.WorkflowControllerService;

public class JobHelper {

    private static final Logger logger = LogManager.getLogger(JobHelper.class);
    private static final String DEFAULT_FALLBACK_TASK = "Qualitätsprüfung";

    /**
     * After a task failed to complete, return given Process 'process' to fallback task using the correction comment
     * feature of Kitodo.Production.
     * @param process the Process that is returned to the fallback task
     * @param errorMessage the error message that will be used for the comment
     * @param failedTask the task that failed to complete and raised the error
     * @return the updated Process object
     */
    public static Process jumpToFallbackTask(Process process, String errorMessage, String failedTask) {
        return jumpToFallbackTask(process, errorMessage, failedTask, DEFAULT_FALLBACK_TASK);
    }

    /**
     * After a task failed to complete, return given Process 'process' to fallback task using the correction comment
     * feature of Kitodo.Production.
     * @param process the Process that is returned to the fallback task
     * @param errorMessage the error message that will be used for the comment
     * @param failedTaskTitle the title of the task that failed to complete and raised the error
     * @param fallbackTaskTitle title of the task where the process should be reset to
     * @return the updated Process object
     */
    public static Process jumpToFallbackTask(Process process, String errorMessage, String failedTaskTitle, String fallbackTaskTitle) {
        Task fallbackTask = null;
        Task failedTask = null;

        // get fallback task by title
        if (Objects.nonNull(fallbackTaskTitle)) {
            for (Task task : process.getTasks()) {
                if (Objects.equals(task.getTitle(), fallbackTaskTitle)) {
                    fallbackTask = task;
                } else if (Objects.equals(task.getTitle(), failedTaskTitle)) {
                    failedTask = task;
                }
            }
        }
        if (Objects.nonNull(fallbackTask)) {
            // create comment and report a problem
            Comment comment = new Comment();
            comment.setProcess(process);
            comment.setCreationDate(new Date());
            comment.setType(CommentType.ERROR);
            comment.setCorrected(Boolean.FALSE);
            comment.setCorrectionTask(fallbackTask);
            comment.setCurrentTask(failedTask);
            comment.setMessage(Objects.nonNull(errorMessage) ? errorMessage : "");
            try {
                ServiceManager.getCommentService().saveToDatabase(comment);
                process = ServiceManager.getProcessService().getById(process.getId());
                saveProcessAndTasksToIndex(process);
                new WorkflowControllerService().reportProblem(comment, TaskEditType.AUTOMATIC);
                logger.error("Handling error by setting process " + process.getId() + " to task \""
                        + fallbackTaskTitle + "\": " + errorMessage);
                return ServiceManager.getProcessService().getById(process.getId());
            } catch (DataException | DAOException | CustomResponseException | IOException err) {
                logger.error("Error handling for " + failedTask + " failed: " + err.getLocalizedMessage());
            }
        } else {
            logger.error("Fallback for failed task "
                    + failedTask + " (ID " + process.getId() + ") not found or specified: " + errorMessage);
        }
        return null;
    }

    private static void saveProcessAndTasksToIndex(Process process) throws CustomResponseException, DataException, IOException {
        ServiceManager.getProcessService().saveToIndex(process, true);
        for (Task task : process.getTasks()) {
            // update tasks in elastic search index, which includes correction comment status
            ServiceManager.getTaskService().saveToIndex(task, true);
        }
    }

    /**
     * Create an info comment.
     * @param process the Process that is commented
     * @param message the info message of the comment
     */
    public static void createComment(Process process, String message) {
        Comment comment = new Comment();
        comment.setProcess(process);
        comment.setCreationDate(new Date());
        comment.setType(CommentType.INFO);
        comment.setMessage(message);
        try {
            ServiceManager.getCommentService().saveToDatabase(comment);
        } catch (DAOException e) {
            logger.error("Could not write comment \"" + message + "\" because of error: " + e.getLocalizedMessage());
        }
    }

    /**
     * Return filenames of media in folder with given Path 'directory' as list of Strings.
     *
     * @param directory
     *          Path of folder
     * @param mediaFileTypes
     *          List of allowed suffixes
     * @return
     *          List of Strings containing the filenames of the media
     */
    public static List<String> getMediaPaths(Path directory, List<String> mediaFileTypes) {
        try (Stream<Path> allPaths = Files.list(directory)) {
            return allPaths.filter(path -> path.toFile().isFile())
                    .filter(path -> path.toFile().canRead())
                    .filter(path -> endsWithAny(path.toString(), mediaFileTypes))
                    .sorted()
                    .map(java.nio.file.Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage());
        }
        return new LinkedList<>();
    }

    private static boolean endsWithAny(String string, Collection<String> suffixes) {
        if (Objects.isNull(string) || Objects.isNull(suffixes)) {
            return false;
        }
        return suffixes.stream().anyMatch(string::endsWith);
    }
}
