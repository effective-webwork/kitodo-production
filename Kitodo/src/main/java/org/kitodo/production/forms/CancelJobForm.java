package org.kitodo.production.forms;

import java.io.IOException;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.controller.SecurityAccessController;
import org.kitodo.production.enums.LogEntryType;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.AuditingLogger;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.relayserver.services.JobService;

@Named("CancelJobForm")
@ViewScoped
public class CancelJobForm extends BaseForm {

    private static final int CANCELLATION_MESSAGE_MAX_LENGTH = 512;
    private static final Logger logger = LogManager.getLogger(CancelJobForm.class);
    private Process process;
    private String cancellationMessage;
    private String cancelCode = "";
    protected static final String ERROR_CANCELLING_JOB = "errorCancellingJob";


    public String getCancellationMessage() {
        return cancellationMessage;
    }

    public void setCancellationMessage(String cancellationMessage) {
        this.cancellationMessage = cancellationMessage;
    }

    public String getCancelCode() {
        return cancelCode;
    }

    public void setCancelCode(String newCancelCode) {
        cancelCode = newCancelCode;
    }

    public static List<String> getCancelCodes() {
        return JobService.getCancelCodes();
    }

    /**
     * Get the maximum length allowed for the cancellation message. Since this will be saved together with the
     * cancellation code, the cancellation code maximum length is subtracted.
     *
     * @return maximum length allowed for cancellation message field
     */
    public static int getCancellationMessageMaxLength() {
        int codeMaximumLength = JobService.getCancelCodes().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        return CANCELLATION_MESSAGE_MAX_LENGTH - codeMaximumLength;
    }

    /**
     * Load process by ID.
     * @param id ID of process to load
     */
    public void load(int id) {
        SecurityAccessController securityAccessController = new SecurityAccessController();
        try {
            if (securityAccessController.hasAuthorityToCancelJobs()) {
                if (id > 0) {
                    this.process = ServiceManager.getProcessService().getById(id);
                }
            } else {
                ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                context.redirect(DEFAULT_LINK);
            }
        } catch (DAOException | IOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.PROCESS.getTranslationSingular(), id },
                    logger, e);
        }
    }

    /**
     * Cancel Viaduc digitization job.
     *
     * @return navigation path to process list
     */
    public String cancelJob() {
        String cancelString = (this.cancelCode + "_" + this.cancellationMessage).replace(" ", "_");
        try {
            JobService.cancelJob(this.process, cancelString);
            logger.info(AuditingLogger.createProcessManipulationLogEntry(LogEntryType.PROCESS_CANCEL, process));
            return processesPage;
        } catch (DataException | DAOException | IOException e) {
            Helper.setErrorMessage("Unable to cancel job", logger, e);
        }
        return null;
    }

    /**
     * Get process.
     *
     * @return value of process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Set process.
     *
     * @param process as org.kitodo.data.database.beans.Process
     */
    public void setProcess(Process process) {
        this.process = process;
    }
}
