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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.relayserver.services.JobService;
import org.primefaces.PrimeFaces;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
public class WorkloadForm implements Serializable {

    private static final Logger logger = LogManager.getLogger(WorkloadForm.class);
    private final JobService jobService = new JobService();

    public int getCapacity() {
        return jobService.getKitodoCapacity();
    }

    public int getWorkload() {
        return jobService.getUsedCapacity();
    }

    public void calculateWorkload() {
        try {
            JobService.calculateUsedCapacity();
            PrimeFaces.current().ajax().update("systemTabView:workloadTab");
        } catch (DAOException e) {
            Helper.setErrorMessage("Unable to calculate workload: " + e.getMessage());
        }
    }
}
