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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.relayserver.services.JobService;

@Named("StatisticBean")
@ViewScoped
public class StatisticBean implements Serializable {

    private static final Logger logger = LogManager.getLogger(StatisticBean.class);
    private static List<String> workflowTasks;

    /**
     * Retrieve and return list of task titles of Vecteur workflow.
     *
     * @return list of task titles of Vecteur workflow
     */
    public static List<String> getWorkflowTasks() {
        try {
            if (Objects.isNull(workflowTasks)) {
                workflowTasks = JobService.getWorkflowTasks();
            }
        } catch (DAOException e) {
            logger.warn("Unable to load workflow tasks!");
            return Collections.emptyList();
        } catch (ProcessGenerationException e) {
            logger.error(e.getMessage());
            return Collections.emptyList();
        }
        return workflowTasks;
    }

    /**
     * Compute and return runtime of task with given name 'taskTitle' in process 'processDTO'.
     *
     * @param processDTO
     *          process for which the runtime of task with given name 'taskTitle' is computed
     * @param taskTitle
     *          name of task for which the runtime in given process 'processDTO' is computed
     * @return String containing the task runtime
     */
    public static String getTaskRuntime(ProcessDTO processDTO, String taskTitle) {
        try {
            return JobService.computeTaskRuntime(processDTO, taskTitle);
        } catch (DAOException e) {
            Helper.setErrorMessage("Unable to compute task '" + taskTitle + "' for process " + processDTO.getId());
            return "";
        }
    }

    public static String getViaducId(ProcessDTO processDTO) {
        try {
            return JobService.getViaducId(ServiceManager.getProcessService().getById(processDTO.getId()));
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
