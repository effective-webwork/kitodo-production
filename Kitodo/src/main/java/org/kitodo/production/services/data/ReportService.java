package org.kitodo.production.services.data;

import com.opencsv.CSVWriter;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Report;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ReportDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.PremisException;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.kitodo.production.services.relayserver.helper.PremisHelper;
import org.kitodo.production.services.relayserver.helper.XMLHelper;
import org.kitodo.production.services.relayserver.services.JobService;
import org.kitodo.production.services.relayserver.services.SIPMetadata;
import org.primefaces.model.SortOrder;
import org.w3c.dom.Document;

public class ReportService extends SearchDatabaseService<Report, ReportDAO> {

    private static volatile ReportService instance = null;
    private static final Logger logger = LogManager.getLogger(ReportService.class);
    // TODO: move to enum and use in JobService and here!
    private static final String CANCELED = "CANCELED";
    private static final String CLEANUP_TASK_TITLE = "Auftrag abschliessen";
    private static final String USER_EVENT = "user";
    private static final String STARTED_EVENT = "Started";
    private static final String FINISHED_EVENT = "Finished";
    private static final String HANDOVER_EVENT = "Hand over";
    private static final String NUMBER_OF_TASK_SCANS = "numberOfTaskScans";
    private static final String NUMBER_OF_TASK_DOCKETS = "numberOfTaskDockets";
    private static final String CORRECTION_WORKFLOW = "correctionWorkflow";
    private static final String CORRECTION_COMMENT = "correctionComment";
    private static final String ORIGINAL_FORMAT = "originalFormat";
    private static final String DELIVERY_FORMAT = "deliveryFormat";
    private static final String EXTERN = "extern";
    private static final String NOT_APPLICABLE = "N/A";
    private static final String SYSTEM = "System";

    private static final List<String> COMMENT_CSV_HEADERS = Arrays.asList(
            "Kitodo-ID",
            "Timestamp",
            "User",
            "Text"
    );

    private static final List<String> TASKEVENT_CSV_HEADERS = Arrays.asList(
            "Kitodo-ID",
            "Prozessschritt",
            "Started",
            "Hand over",
            "Finished",
            "User",
            "Korrekturkommentar",
            "Number of scanned pages",
            "Number of task scans",
            "Number of task dockets",
            "Originalformat",
            "Lieferformat",
            "Korrekturlauf"
    );

    /**
     * Default constructor.
     */
    private ReportService() {
        super(new ReportDAO());
    }

    /**
     * Return singleton variable of type ReportService.
     *
     * @return unique instance of ReportService
     */
    public static ReportService getInstance() {
        ReportService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (ReportService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new ReportService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Load data for frontend lists. Data can be loaded from database or index.
     *
     * @param first     searched objects
     * @param pageSize  size of page
     * @param sortField field by which data should be sorted
     * @param sortOrder order ascending or descending
     * @param filters   for search query
     * @return loaded data
     */
    @Override
    public List<?> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) throws DataException {
        return null;
    }

    /**
     * Count all rows in database.
     *
     * @return amount of all rows
     */
    @Override
    public Long countDatabaseRows() throws DAOException {
        return null;
    }

    /**
     * This function is used for count amount of results for frontend lists.
     *
     * @param filters Map of parameters used for filtering
     * @return amount of results
     * @throws DAOException  that can be caused by Hibernate
     * @throws DataException that can be caused by ElasticSearch
     */
    @Override
    public Long countResults(Map filters) throws DAOException, DataException {
        return null;
    }

    public Report storeJobReportToDatabase(Process job) throws DAOException {
        Report report = new Report();
        report.setKitodoId(job.getId());

        String viaducId = JobService.getViaducId(job);
        if (viaducId.isEmpty()) {
            viaducId = job.getTitle().replace("Auftrag", "").trim();
        }
        if (StringUtils.isNumeric(viaducId)) {
            report.setViaducId(Integer.parseInt(viaducId));
        }

        report.setStructuralError(job.isStructuralError());
        report.setMissingInformation(job.isMissingInformation());
        report.setReimport(job.isReimport());

        report.setNumberOfScans(JobService.countProcessScans(job));
        // total number of pages incl. dockets can be determined by adding corresponding data from that processes scanning tasks!
        // TODO: check if in that case this total number is still required as a separate field!
        // report.setNumberOfScansAndDockets();

        Document metadataDocument = XMLHelper.getMetadataDocument(job);
        Boolean isWorkingCopy = SIPMetadata.getWorkingCopyType(metadataDocument);
        report.setWorkingCopy(isWorkingCopy);
        report.setUploadDestination(isWorkingCopy ? "SFTP" : "DIR");
        report.setComplexity(XMLHelper.getComplexity(metadataDocument));

        try {
            report.setTaskData(aggregateTaskData(job, metadataDocument));
        } catch (IOException | ParserConfigurationException | PremisException | DateTimeParseException
                | XPathExpressionException e) {
            logger.error("Unable to load task data from PREMIS file for job with Kitodo-ID " + job.getId() + ": "
                    + e.getMessage());
            e.printStackTrace();
        }

        report.setCorrectionWorkflowActivated(job.getTasks().stream().anyMatch(Task::isCorrection));

        // current date for jobs for which report is created during cleanup (and corresponding cleanup timestamp is still "null"!)
        LocalDateTime timestamp = LocalDateTime.now();

        for (Task task : job.getTasks()) {
            if (CLEANUP_TASK_TITLE.equals(task.getTitle())) {
                if (Objects.nonNull(task.getProcessingTime())) {
                    // "processingTime" timestamp for jobs that have already been successfully cleaned up when report is created
                    timestamp = task.getProcessingTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                }
                break;
            }
        }

        // add "taskEvent" entry for "cleanup" task! (independent of whether job was canceled or completed!)
        HashMap<String, Object> cleanupTaskData = new HashMap<>();
        cleanupTaskData.put(STARTED_EVENT, Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant()));
        cleanupTaskData.put(FINISHED_EVENT, Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant()));
        cleanupTaskData.put(CORRECTION_WORKFLOW, false);
        cleanupTaskData.put(CORRECTION_COMMENT, NOT_APPLICABLE);
        cleanupTaskData.put(USER_EVENT, SYSTEM);
        // "taskData" will be "null" if, for example, the metadata file is missing!
        if (Objects.nonNull(report.getTaskData())) {
            report.getTaskData().put(CLEANUP_TASK_TITLE, Collections.singletonList(cleanupTaskData));
        } else {
            logger.error("Task data is 'null', unable to create valid report for job with Kitodo-ID " + job.getId());
        }

        if (CANCELED.equals(JobService.getViaducState(job))) {
            report.setCanceled(true);
            report.setCanceledMessage(JobService.getViaducMessage(job));
            if (Objects.isNull(report.getCanceledTimestamp())) {
                report.setCanceledTimestamp(timestamp);
            }
        } else {
            report.setCanceled(false);
            report.setCanceledMessage(NOT_APPLICABLE);
        }

        report.setReportCreatedTimestamp(timestamp);

        saveToDatabase(report);
        return dao.getByKitodoId(job.getId());
    }

    private static HashMap<String, List<HashMap<String, Object>>> aggregateTaskData(Process job, Document document) throws IOException, ParserConfigurationException,
            PremisException, DateTimeParseException, XPathExpressionException {
        // for each task: start time, end time, user (multiple times if task was performed multiple times) => PREMIS
        HashMap<String, List<HashMap<String, Object>>> taskEventData = PremisHelper.getTaskEventData(job);

        // augment task data from PREMIS file with additional data for reports
        for (Task task : job.getTasks()) {
            List<HashMap<String, Object>> currentTaskEvents = taskEventData.get(task.getTitle());
            boolean isScanningTask = task.isTypeImagesWrite() && Objects.nonNull(task.getWorkflowCondition()) && Objects.nonNull(task.getWorkflowCondition().getValue());

            for (int i = 0 ; i < currentTaskEvents.size(); i++) {
                // collect scanning data for scanning tasks
                if (isScanningTask) {
                    String profileXpath = task.getWorkflowCondition().getValue();
                    // _total_ number of scans and dockets of whole job can be determined by adding corresponding values for all tasks in job!
                    int numberOfPartialJobScans = 0;
                    // _total_ number of dockets of whole job can be determined by adding corresponding values for all tasks in job!
                    int numberOfPartialJobDockets = 0;
                    // number of partial job files is only saved for _last_ occurrence of scanning task!
                    if (i == currentTaskEvents.size() -1) {
                        numberOfPartialJobScans = XMLHelper.getNumberOfPartialJobFiles(document, profileXpath, "numberActualImages");
                        numberOfPartialJobDockets = XMLHelper.getNumberOfPartialJobFiles(document, profileXpath, "numberDockets");
                    }
                    currentTaskEvents.get(i).put(NUMBER_OF_TASK_SCANS, String.valueOf(numberOfPartialJobScans));
                    currentTaskEvents.get(i).put(NUMBER_OF_TASK_DOCKETS, String.valueOf(numberOfPartialJobDockets));
                    // add "originalFormat" and "deliveryFormat" for external partial jobs
                    if (profileXpath.contains(EXTERN)) {
                        currentTaskEvents.get(i).put(ORIGINAL_FORMAT, XMLHelper.getOriginalFormat(document, profileXpath));
                        currentTaskEvents.get(i).put(DELIVERY_FORMAT, XMLHelper.getDeliveryFormat(document, profileXpath));
                    }
                }
            }
        }
        return taskEventData;
    }

    /**
     * Retrieve and return reports for all process in date range defined by given 'fromDate' and 'toDate'.
     *
     * @param fromDate String containing the start date of the date range
     * @param toDate String containing the end date of the date range
     * @return byte array of reports for processes in given date range in CSV format
     * @throws DAOException thrown if loading reports from databases results in an error
     */
    public byte[] getReportsInDateRange(Date fromDate, Date toDate) throws DAOException,
            CsvRequiredFieldEmptyException, IOException, CsvDataTypeMismatchException {
        List<Report> reports = new ArrayList<>();
        for (Process process : ServiceManager.getProcessService().getProcessesCleanedUpInDateRange(fromDate, toDate)) {
            Report report = dao.getByKitodoId(process.getId());
            if (Objects.isNull(report)) {
                report = storeJobReportToDatabase(process);
            }
            reports.add(report);
        }
        return createCSVData(reports);
    }

    public String getCommentsReport(Date fromDate, Date toDate) throws DAOException {
        List<String> commentStrings = new LinkedList<>();
        commentStrings.add(String.join(String.valueOf(CSVWriter.DEFAULT_SEPARATOR), COMMENT_CSV_HEADERS));
        for (Process process : ServiceManager.getProcessService().getProcessesCleanedUpInDateRange(fromDate, toDate)) {
            for (Comment comment : process.getComments()) {
                if (CommentType.ERROR.equals(comment.getType())) {
                    // correction comments are included in task events report!
                    continue;
                }
                List<String> reportParts = new LinkedList<>();
                reportParts.add(String.valueOf(process.getId()));
                if (Objects.nonNull(comment.getCreationDate())) {
                    reportParts.add(comment.getCreationDate().toString());
                } else {
                    reportParts.add(NOT_APPLICABLE);
                }
                if (Objects.nonNull(comment.getAuthor()) && Objects.nonNull(comment.getAuthor().getName()) && Objects.nonNull(comment.getAuthor().getSurname())) {
                    reportParts.add(normalizeUsername(comment.getAuthor().getFullName()));
                } else {
                    reportParts.add(NOT_APPLICABLE);
                }
                reportParts.add(StringEscapeUtils.escapeCsv(comment.getMessage()));
                commentStrings.add(String.join(String.valueOf(CSVWriter.DEFAULT_SEPARATOR), reportParts));
            }
        }
        return String.join(CSVWriter.DEFAULT_LINE_END, commentStrings);
    }

    private byte[] createCSVData(List<Report> reports) throws IOException, CsvDataTypeMismatchException,
            CsvRequiredFieldEmptyException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = new OutputStreamWriter(stream);
        HeaderColumnNameMappingStrategy<Report> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(Report.class);
        StatefulBeanToCsv<Report> statefulBeanToCsv = new StatefulBeanToCsvBuilder<Report>(streamWriter)
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withMappingStrategy(strategy)
                .build();
        statefulBeanToCsv.write(reports);
        streamWriter.flush();
        return stream.toByteArray();
    }

    public String getTaskEventReportsInDateRange(Date fromDate, Date toDate) throws DAOException, IOException {
        LinkedList<LinkedList<Object>> taskEventReports = new LinkedList<>();
        for (Process process : ServiceManager.getProcessService().getProcessesCleanedUpInDateRange(fromDate, toDate)) {
            Report report = dao.getByKitodoId(process.getId());
            if (Objects.isNull(report)) {
                report = storeJobReportToDatabase(process);
            }
            if (Objects.nonNull(report.getTaskData())) {
                taskEventReports.addAll(mapTaskEventsToCSV(report));
            }
        }

        // sort by "STARTED" column (index 2)
        taskEventReports = taskEventReports.stream()
                .sorted(Comparator.comparingLong(e -> (Long) e.get(2)))
                .collect(Collectors.toCollection(LinkedList::new));

        LinkedList<String> eventStrings = new LinkedList<>();
        eventStrings.add(String.join(String.valueOf(CSVWriter.DEFAULT_SEPARATOR), TASKEVENT_CSV_HEADERS));

        // map "STARTED" milliseconds to Date String
        for (LinkedList<Object> eventList : taskEventReports) {

            LinkedList<String> eventValues = new LinkedList<>();
            for (Object event : eventList) {
                if (event instanceof Long) {
                    eventValues.add(XMLHelper.formatLocalDateTime(new Date((Long) event)));
                } else if (Objects.isNull(event)) {
                    eventValues.add(NOT_APPLICABLE);
                } else {
                    eventValues.add(StringEscapeUtils.escapeCsv(String.valueOf(event)));
                }
            }
            // create comma separated string from event values and add to list of CSV entries
            eventStrings.add(String.join(String.valueOf(CSVWriter.DEFAULT_SEPARATOR), eventValues));
        }

        // join resulting Strings using DEFAULT_LINE_END and return String
        return String.join(CSVWriter.DEFAULT_LINE_END, eventStrings);
    }

    private LinkedList<LinkedList<Object>> mapTaskEventsToCSV(Report report) {
        HashMap<String, List<HashMap<String, Object>>> taskEvents = report.getTaskData();
        LinkedList<LinkedList<Object>> eventValueLists = new LinkedList<>();
        for (String taskTitle : taskEvents.keySet()) {
            if (taskEvents.get(taskTitle) != null) {
                List<HashMap<String, Object>> taskEvent = taskEvents.get(taskTitle);
                if (!taskEvent.isEmpty()) {
                    //  if eventDataList has more than one entry it means the task was processed multiple times (=> correction workflow)
                    //  --> create one entry per line!
                    for (Object eventDataObject : taskEvent) {
                        if (!(eventDataObject instanceof LinkedHashMap)) {
                            continue;
                        }
                        LinkedHashMap<String, Object> eventData = (LinkedHashMap<String, Object>) eventDataObject;
                        LinkedList<Object> events = new LinkedList<>();
                        events.add(report.getKitodoId());
                        events.add(taskTitle);
                        events.add(eventData.get(STARTED_EVENT));
                        events.add(eventData.get(HANDOVER_EVENT));
                        events.add(eventData.get(FINISHED_EVENT));
                        if (eventData.get(USER_EVENT) instanceof String) {
                            events.add(normalizeUsername((String) eventData.get(USER_EVENT)));
                        } else {
                            events.add(NOT_APPLICABLE);
                        }
                        events.add(eventData.getOrDefault(CORRECTION_COMMENT, NOT_APPLICABLE));
                        events.add(eventData.getOrDefault(XMLHelper.NUMBER_OF_SCANNED_PAGES, 0));
                        events.add(eventData.getOrDefault(NUMBER_OF_TASK_SCANS, 0));
                        events.add(eventData.getOrDefault(NUMBER_OF_TASK_DOCKETS, 0));
                        events.add(eventData.getOrDefault(ORIGINAL_FORMAT, NOT_APPLICABLE));
                        events.add(eventData.getOrDefault(DELIVERY_FORMAT, NOT_APPLICABLE));
                        events.add(eventData.getOrDefault(CORRECTION_WORKFLOW, false));
                        eventValueLists.add(events);
                    }
                }
            }
        }
        return eventValueLists;
    }

    private static String normalizeUsername(String username) {
        if (username.contains(",")) {
            List<String> nameParts = Arrays.asList(username.split(","));
            Collections.reverse(nameParts);
            return nameParts.stream().map(String::trim).collect(Collectors.joining(" "));
        } else {
            return username;
        }
    }
}
