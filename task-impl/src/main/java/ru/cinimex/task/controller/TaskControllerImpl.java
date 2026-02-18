package ru.cinimex.task.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import ru.cinimex.task.service.TaskService;
import ru.cinimex.taskapi.controller.TaskController;
import ru.cinimex.taskapi.dto.TaskCreatedResponse;
import ru.cinimex.taskapi.dto.TaskCreationRequest;
import ru.cinimex.taskapi.dto.TaskResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class TaskControllerImpl implements TaskController {

    private final TaskService taskService;

    @Override
    public ResponseEntity<TaskCreatedResponse> createTask(TaskCreationRequest taskCreationRequest) {
        return null;
    }

    @Override
    public ResponseEntity<List<TaskResponse>> getTasks(String title, String status, OffsetDateTime notificateAtStart, OffsetDateTime notificateAtEnd) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> getTaskById(UUID id) {
        return null;
    }

    @Override
    public ResponseEntity<Void> updateTask(UUID id, TaskCreationRequest taskUpdateRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteTask(UUID id) {
        return null;
    }
}
