package ru.cinimex.task.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.cinimex.task.domain.TaskEntity;
import ru.cinimex.task.exception.TaskBusinessException;
import ru.cinimex.task.exception.TaskNotFoundException;
import ru.cinimex.task.mapper.TaskMapper;
import ru.cinimex.task.repository.TaskRepository;
import ru.cinimex.taskapi.dto.TaskCreatedResponse;
import ru.cinimex.taskapi.dto.TaskCreationRequest;
import ru.cinimex.taskapi.dto.TaskResponse;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private final String CURRENT_USER = "test_user";
    private final UUID TASK_ID = UUID.randomUUID();

    @BeforeEach
    void setupSecurity() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(CURRENT_USER);

        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("Метод createTask")
    class CreateTask {
        @Test
        void shouldCreateTaskSuccessfully() {
            TaskCreationRequest request = new TaskCreationRequest("Title", "Desc", OffsetDateTime.now());
            TaskEntity entity = new TaskEntity();
            entity.setId(TASK_ID);

            when(taskMapper.toEntity(request)).thenReturn(entity);
            when(taskRepository.save(entity)).thenReturn(entity);

            TaskCreatedResponse response = taskService.createTask(request);

            assertNotNull(response);
            assertEquals(TASK_ID, response.getGeneratedId());
            verify(taskRepository).save(entity);
        }
    }

    @Nested
    @DisplayName("Метод getTaskById")
    class GetTaskById {
        @Test
        @DisplayName("Успешное получение задачи")
        void shouldReturnTaskResponse() {
            TaskEntity task = new TaskEntity();
            task.setAssignee(CURRENT_USER);

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
            when(taskMapper.toResponse(task)).thenReturn(new TaskResponse());

            assertNotNull(taskService.getTaskById(TASK_ID));
        }

        @Test
        @DisplayName("Ошибка: Задача не найдена")
        void shouldThrowNotFoundWhenTaskMissing() {
            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

            assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(TASK_ID));
        }

        @Test
        @DisplayName("Ошибка: Задача чужого пользователя")
        void shouldThrowNotFoundWhenWrongUser() {
            TaskEntity task = new TaskEntity();
            task.setAssignee("another_user");

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(TASK_ID));
        }
    }

    @Nested
    @DisplayName("Метод updateTask")
    class UpdateTask {
        @Test
        @DisplayName("Успешное обновление")
        void shouldUpdateSuccessfully() {
            TaskEntity task = new TaskEntity();
            task.setAssignee(CURRENT_USER);
            task.setStatus("CREATED");
            TaskCreationRequest request = new TaskCreationRequest("New", "Desc", null);

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            taskService.updateTask(TASK_ID, request);

            verify(taskMapper).updateEntityFromDto(request, task);
            verify(taskRepository).save(task);
        }

        @Test
        @DisplayName("Ошибка: Статус не CREATED")
        void shouldThrowBusinessExceptionWhenStatusIsNotCreated() {
            TaskEntity task = new TaskEntity();
            task.setAssignee(CURRENT_USER);
            task.setStatus("IN_PROGRESS");

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            assertThrows(TaskBusinessException.class, () -> taskService.updateTask(TASK_ID, new TaskCreationRequest(null, null, null)));
        }
    }

    @Nested
    @DisplayName("Метод deleteTask")
    class DeleteTask {
        @Test
        @DisplayName("Успешное удаление")
        void shouldDeleteSuccessfully() {
            TaskEntity task = new TaskEntity();
            task.setAssignee(CURRENT_USER);
            task.setStatus("CREATED");

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            taskService.deleteTask(TASK_ID);

            verify(taskRepository).delete(task);
        }

        @Test
        @DisplayName("Ошибка: Попытка удалить не свою задачу")
        void shouldThrowNotFoundOnDeleteOthersTask() {
            TaskEntity task = new TaskEntity();
            task.setAssignee("hacker");

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

            assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(TASK_ID));
            verify(taskRepository, never()).delete(any(TaskEntity.class));
        }
    }
}
