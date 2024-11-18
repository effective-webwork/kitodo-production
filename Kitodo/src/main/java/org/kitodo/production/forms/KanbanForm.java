package org.kitodo.production.forms;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.KanbanService;

@Named
@ViewScoped
public class KanbanForm implements Serializable {

    private static final String CLEANUP = "Auftrag abschliessen";
    private List<ProcessDTO> processes = null;
    private LocalDateTime lastUpdate = LocalDateTime.now();

    public List<ProcessDTO> getProcesses() {
        if (Objects.isNull(processes) || lastUpdate.plusMinutes(1).isBefore(LocalDateTime.now())) {
            processes = KanbanService.getActiveProcesses();
            lastUpdate = LocalDateTime.now();
        }
        return processes;
    }

    public List<ProcessDTO> getProcessesWithOpenTask(String taskName) {
        return getProcesses().stream()
                .filter(processDTO -> processDTO.getTasks().stream().anyMatch(
                    task -> TaskStatus.OPEN.equals(task.getProcessingStatus()) && task.getTitle().startsWith(taskName)))
                .sorted(Comparator.comparing(ProcessDTO::getId))
                .collect(Collectors.toList());
    }

    public List<ProcessDTO> getProcessesWithInWorkTask(String taskName) {
        return getProcesses().stream()
                .filter(processDTO -> processDTO.getTasks().stream().anyMatch(
                    task -> TaskStatus.INWORK.equals(task.getProcessingStatus()) && task.getTitle().startsWith(taskName)))
                .sorted(Comparator.comparing(ProcessDTO::getId))
                .collect(Collectors.toList());
    }

    public List<ProcessDTO> getProcessesWithOpenOrInWorkTask(String taskName) {
        return getProcesses().stream()
                .filter(processDTO -> processDTO.getTasks().stream().anyMatch(
                    task -> (TaskStatus.INWORK.equals(task.getProcessingStatus()) || TaskStatus.OPEN.equals(task.getProcessingStatus()))
                            && task.getTitle().startsWith(taskName)
                        ))
                .sorted(Comparator.comparing(ProcessDTO::getId))
                .collect(Collectors.toList());
    }

    public List<ProcessDTO> getProcessesWithoutOpenTasks() {
        List<ProcessDTO> filteredList = new ArrayList<>();
        for (ProcessDTO processDTO : getProcesses()) {
            if (processDTO.getTasks().stream().noneMatch(task -> TaskStatus.OPEN.equals(task.getProcessingStatus())
                    || TaskStatus.INWORK.equals(task.getProcessingStatus())
                    || (CLEANUP.equals(task.getTitle())) && TaskStatus.DONE.equals(task.getProcessingStatus()))) {
                filteredList.add(processDTO);
            }
        }
        filteredList.sort(Comparator.comparing(ProcessDTO::getId));
        return filteredList;
    }

    public List<ProcessDTO> getProcessesInCorrection(boolean taskInWork) {
        List<ProcessDTO> processesInCorrection = getProcesses().stream()
                .filter(processDTO -> processDTO.getTasks().stream()
                        .anyMatch(taskDTO -> taskDTO.isCorrection() && (TaskStatus.OPEN.equals(taskDTO.getProcessingStatus()) || TaskStatus.INWORK.equals(taskDTO.getProcessingStatus()))))
                .collect(Collectors.toList());
        List<ProcessDTO> filteredProcessDTOS = new ArrayList<>();
        for (ProcessDTO processDTO : processesInCorrection) {
            Process process;
            try {
                process = ServiceManager.getProcessService().getById(processDTO.getId());
            } catch (DAOException e) {
                e.printStackTrace();
                continue;
            }
            Optional<Comment> lastCorrectionComment = process.getComments().stream().filter(comment -> CommentType.ERROR.equals(comment.getType())).reduce((first, second) -> second);
            if (!lastCorrectionComment.isPresent()) {
                continue;
            }
            if (process.getTasks().stream().anyMatch(task -> !taskInWork && TaskStatus.OPEN.equals(task.getProcessingStatus()) && task.getOrdering() < lastCorrectionComment.get().getCurrentTask().getOrdering())) {
                filteredProcessDTOS.add(processDTO);
            } else if (process.getTasks().stream().anyMatch(task -> taskInWork && TaskStatus.INWORK.equals(task.getProcessingStatus()) && task.getOrdering() < lastCorrectionComment.get().getCurrentTask().getOrdering())) {
                filteredProcessDTOS.add(processDTO);
            }
        }
        filteredProcessDTOS.sort(Comparator.comparing(ProcessDTO::getId));
        return filteredProcessDTOS;
    }

    public List<ProcessDTO> getCancelledProcesses() {
        return getProcesses().stream()
                .filter(processDTO -> processDTO.getTasks().stream()
                        .allMatch(taskDTO -> CLEANUP.equals(taskDTO.getTitle()) || TaskStatus.LOCKED.equals(taskDTO.getProcessingStatus())))
                .sorted(Comparator.comparing(ProcessDTO::getId))
                .collect(Collectors.toList());
    }

    public List<ProcessDTO> getClosedProcesses() {
        return getProcesses().stream()
                .filter(processDTO -> processDTO.getTasks().stream()
                        .allMatch(taskDTO -> TaskStatus.DONE.equals(taskDTO.getProcessingStatus())))
                .sorted(Comparator.comparing(ProcessDTO::getId))
                .collect(Collectors.toList());
    }

    public String getStringForProcess(ProcessDTO processDTO, String task) {
        return KanbanService.getStringForProcess(processDTO, task, false);
    }

    public String getStringForProcessWithUser(ProcessDTO processDTO, String task) {
        return KanbanService.getStringForProcess(processDTO, task, true);
    }

    public String getStylingClasses(ProcessDTO processDTO) {
        StringBuilder stylingClasses = new StringBuilder();
        stylingClasses.append(KanbanService.getPriorityClass(processDTO));
        stylingClasses.append(KanbanService.getExtentClass(processDTO));
        return stylingClasses.toString();
    }
}
