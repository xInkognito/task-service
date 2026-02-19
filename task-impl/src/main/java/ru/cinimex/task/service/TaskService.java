package ru.cinimex.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.cinimex.task.repository.TaskRepository;
import ru.cinimex.taskapi.dto.TaskCreatedResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public UUID createTask(TaskCreatedResponse response, Authentication authentication) {

    }
}
