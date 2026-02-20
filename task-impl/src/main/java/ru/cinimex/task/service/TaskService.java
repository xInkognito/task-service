package ru.cinimex.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinimex.task.domain.TaskEntity;
import ru.cinimex.task.exception.TaskBusinessException;
import ru.cinimex.task.exception.TaskNotFoundException;
import ru.cinimex.task.mapper.TaskMapper;
import ru.cinimex.task.repository.TaskRepository;
import ru.cinimex.task.repository.specification.TaskSpecification;
import ru.cinimex.taskapi.dto.TaskCreatedResponse;
import ru.cinimex.taskapi.dto.TaskCreationRequest;
import ru.cinimex.taskapi.dto.TaskResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    @Transactional
    public TaskCreatedResponse createTask(TaskCreationRequest request) {
        TaskEntity entity = taskMapper.toEntity(request);

        TaskEntity savedEntity = taskRepository.save(entity);

        return new TaskCreatedResponse(savedEntity.getId());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(String title, String status, OffsetDateTime start, OffsetDateTime end) {
        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        Specification<TaskEntity> spec = TaskSpecification.filterTasks(title, status, start, end, currentLogin);

        return taskRepository.findAll(spec).stream()
                .map(taskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID id) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача с id " + id + " не найдена"));

        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!task.getAssignee().equals(currentLogin)) {
            throw new TaskNotFoundException("Задача не принадлежит текущему пользователю");
        }

        return taskMapper.toResponse(task);
    }

    @Transactional
    public void updateTask(UUID id, TaskCreationRequest request) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));

        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!task.getAssignee().equals(currentLogin)) {
            throw new TaskNotFoundException("У вас нет прав на редактирование этой задачи");
        }

        if (!"CREATED".equals(task.getStatus())) {
            throw new TaskBusinessException("Редактирование запрещено: задача уже в статусе " + task.getStatus());
        }

        taskMapper.updateEntityFromDto(request, task);

        taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(UUID id) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));

        String currentLogin = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!task.getAssignee().equals(currentLogin)) {
            throw new TaskNotFoundException("Нет прав для удаления данной задачи");
        }

        if (!"CREATED".equals(task.getStatus())) {
            throw new TaskBusinessException("Нельзя удалить задачу в статусе " + task.getStatus());
        }

        taskRepository.delete(task);
    }
}
