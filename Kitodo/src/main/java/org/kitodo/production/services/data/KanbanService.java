package org.kitodo.production.services.data;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.dto.PropertyDTO;
import org.kitodo.production.services.ServiceManager;

public class KanbanService {

    private static final Logger logger = LogManager.getLogger(KanbanService.class);
    private static final String JOB_ID = "AuftragsId";
    private static final String COMPLEXITY = "complexity";
    private static final String PRIORITY = "priority";
    private static final String EXTENT = "extent";
    private static final String SEPARATOR = "-";
    private static final String PROCESS_NOTE_MULTIPLE_USERS = " !";
    private static final String CLEANED_UP_TITLE_PREFIX = "Auftrag ";
    private static final Map<String, Integer> EXTENT_MAPPING = new HashMap<>();
    private static final int TIME_SPAN_ACTIVE_PROCESSES = 24;
    private static final List<DayOfWeek> WEEKEND = new ArrayList<>();

    static {
        EXTENT_MAPPING.put("klein", 1);
        EXTENT_MAPPING.put("mittel", 2);
        EXTENT_MAPPING.put("gross", 3);
        EXTENT_MAPPING.put("sehr gross", 4);
        WEEKEND.add(DayOfWeek.SATURDAY);
        WEEKEND.add(DayOfWeek.SUNDAY);
    }

public static List<ProcessDTO> getActiveProcesses() {
        List<ProcessDTO> processes = ServiceManager.getProcessService().findActiveProcesses();
        processes.addAll(ServiceManager.getProcessService().findRecentlyCanceledOrClosedProcesses(getLastActivityTime(TIME_SPAN_ACTIVE_PROCESSES)));
        return processes;
    }

    private static LocalDateTime getLastActivityTime(int timeSpanHours) {
        LocalDateTime workingDate = LocalDateTime.now();
        long nanosLeft = Duration.ofHours(timeSpanHours).toNanos();
        do {
            long nanosOfDay = workingDate.toLocalTime().toNanoOfDay();
            if (WEEKEND.contains(workingDate.getDayOfWeek())) {
                workingDate = workingDate.minusNanos(nanosOfDay + 1);
            } else {
                if (nanosOfDay > nanosLeft) {
                    workingDate = workingDate.minusNanos(nanosLeft);
                    break;
                } else {
                    workingDate = workingDate.minusNanos(nanosOfDay + 1);
                    nanosLeft -= nanosOfDay;
                }
            }
        } while (nanosLeft > 0);
        return workingDate.plusNanos(1);
    }

    private static MetadataEntry getMetadata(ProcessDTO processDTO, String key) {
        Optional<Metadata> metadata = processDTO.getMetadata().stream()
                .filter(m -> key.equals(m.getKey()))
                .findFirst();
        if (metadata.isPresent() && metadata.get() instanceof MetadataEntry) {
            return (MetadataEntry) metadata.get();
        }
        return null;
    }


    public static String getStringForProcess(ProcessDTO processDTO, String task, boolean showUser) {
        if (Objects.isNull(processDTO)) {
            logger.error("ProcessDTO must not be null!");
            return "N/A";
        }
        StringBuilder processString = new StringBuilder();
        Optional<PropertyDTO> jobId = processDTO.getProperties().stream().filter(propertyDTO -> JOB_ID.equals(propertyDTO.getTitle())).findFirst();
        if (jobId.isPresent() && StringUtils.isNotEmpty(jobId.get().getValue())) {
            processString.append(jobId.get().getValue());
        } else if (processDTO.getTitle().startsWith(CLEANED_UP_TITLE_PREFIX)) {
            processString.append(processDTO.getTitle().replace(CLEANED_UP_TITLE_PREFIX, ""));
        } else {
            // use process id as fallback when "AuftragsId" could not be found
            processString.append("(" + processDTO.getId() + ")");
        }
        if (showUser) {
            processString.append(getUserForTask(processDTO, task));
        }
        processString.append(getComplexity(processDTO));
        if (hasMultipleActiveTasks(processDTO)) {
            processString.append(PROCESS_NOTE_MULTIPLE_USERS);
        }
        return processString.toString();
    }

    private static boolean hasMultipleActiveTasks(ProcessDTO processDTO) {
        long numberOfOpenTasks = processDTO.getTasks().stream()
                .filter(task -> TaskStatus.OPEN.equals(task.getProcessingStatus()) || TaskStatus.INWORK.equals(task.getProcessingStatus()))
                .count();
        return numberOfOpenTasks > 1;
    }

    private static String getUserForTask(ProcessDTO processDTO, String taskName) {
        if (Objects.nonNull(taskName) && StringUtils.isNotEmpty(taskName)) {
            List<String> processingUsers = processDTO.getTasks().stream()
                    .filter(task -> task.getTitle().startsWith(taskName)
                            && TaskStatus.INWORK.equals(task.getProcessingStatus())
                            && Objects.nonNull(task.getProcessingUser()))
                    .map(task -> task.getProcessingUser().getLogin())
                    .collect(Collectors.toList());
            StringBuilder userString = new StringBuilder();
            for (String processingUser : processingUsers) {
                if (userString.length() == 0) {
                    userString.append(" ");
                }
                userString.append("(").append(processingUser).append(")");
            }
            return userString.toString();
        }
        return "";
    }

    private static String getComplexity(ProcessDTO processDTO) {
        MetadataEntry complexity = getMetadata(processDTO, COMPLEXITY);
        if (Objects.nonNull(complexity)) {
            switch (complexity.getValue()) {
                case "3":
                    return " +";
                case "1":
                    return " -";
                default:
                    return "";
            }
        }
        return "";
    }

    public static String getPriorityClass(ProcessDTO processDTO) {
        return " " + PRIORITY + SEPARATOR + processDTO.getPriority().getValue();
    }

    public static String getExtentClass(ProcessDTO processDTO) {
        MetadataEntry extent = getMetadata(processDTO, EXTENT);
        if (Objects.nonNull(extent)) {
            String extentKey = extent.getValue();
            if (EXTENT_MAPPING.containsKey(extentKey)) {
                return " " + EXTENT + SEPARATOR + EXTENT_MAPPING.get(extentKey);
            }
        }
        return "";
    }




}
