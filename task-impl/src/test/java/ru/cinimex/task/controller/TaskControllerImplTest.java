package ru.cinimex.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.cinimex.task.exception.TaskBusinessException;
import ru.cinimex.task.exception.TaskNotFoundException;
import ru.cinimex.task.repository.TaskRepository;
import ru.cinimex.task.service.JwtService;
import ru.cinimex.task.service.TaskService;
import ru.cinimex.taskapi.dto.TaskCreatedResponse;
import ru.cinimex.taskapi.dto.TaskCreationRequest;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
class TaskControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private JwtService jwtService;

    @Nested
    @DisplayName("POST /tasks - Создание задачи")
    class CreateTaskTests {

        @Test
        @DisplayName("200 OK: Успешное создание")
        @WithMockUser(roles = "USER")
        void successCreate() throws Exception {
            UUID generatedId = UUID.randomUUID();
            TaskCreationRequest request = new TaskCreationRequest(
                    "Тест", "Описание", OffsetDateTime.now().plusDays(1));

            when(taskService.createTask(any())).thenReturn(new TaskCreatedResponse(generatedId));

            mockMvc.perform(post("/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.generatedId").value(generatedId.toString()));
        }

        @Test
        @DisplayName("400 Bad Request: Дата в прошлом")
        @WithMockUser(roles = "USER")
        void failPastDate() throws Exception {
            TaskCreationRequest request = new TaskCreationRequest(
                    "Тест", "Описание", OffsetDateTime.parse("2020-01-01T10:00:00Z"));

            mockMvc.perform(post("/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ошибка валидации")));
        }

        @Test
        @DisplayName("401 Unauthorized: JWT токен отсутствует")
        void failUnauthorized() throws Exception {
            TaskCreationRequest request = new TaskCreationRequest(
                    "Тест", "Описание", OffsetDateTime.now().plusDays(1));

            mockMvc.perform(post("/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("403 Forbidden: У пользователя нет роли USER (роль ADMIN)")
        @WithMockUser(roles = "ADMIN")
        void failForbidden() throws Exception {
            TaskCreationRequest request = new TaskCreationRequest(
                    "Тест", "Описание", OffsetDateTime.now().plusDays(1));

            mockMvc.perform(post("/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /tasks - Получение списка задач с фильтрацией")
    class GetAllTasksTests {

        @Test
        @DisplayName("200 OK: Успешное получение списка без фильтров")
        @WithMockUser(roles = "USER")
        void successGetAll() throws Exception {
            // Подготавливаем список из двух задач
            ru.cinimex.taskapi.dto.TaskResponse task1 = new ru.cinimex.taskapi.dto.TaskResponse();
            task1.setTitle("Задача 1");
            ru.cinimex.taskapi.dto.TaskResponse task2 = new ru.cinimex.taskapi.dto.TaskResponse();
            task2.setTitle("Задача 2");

            when(taskService.getTasks(any(), any(), any(), any()))
                    .thenReturn(java.util.List.of(task1, task2));

            mockMvc.perform(get("/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2)) // Проверяем размер списка
                    .andExpect(jsonPath("$[0].title").value("Задача 1"))
                    .andExpect(jsonPath("$[1].title").value("Задача 2"));
        }

        @Test
        @DisplayName("200 OK: Получение списка с применением фильтров")
        @WithMockUser(roles = "USER")
        void successGetWithFilters() throws Exception {
            when(taskService.getTasks(eq("Тест"), eq("CREATED"), any(), any()))
                    .thenReturn(java.util.List.of());

            mockMvc.perform(get("/tasks")
                            .param("title", "Тест")
                            .param("status", "CREATED")
                            .param("notificate_at_start", "2024-01-01T00:00:00Z")
                            .param("notificate_at_end", "2024-12-31T23:59:59Z"))
                    .andExpect(status().isOk());

            verify(taskService).getTasks(eq("Тест"), eq("CREATED"), any(), any());
        }

        @Test
        @DisplayName("401 Unauthorized: Токен не передан")
        void failUnauthorized() throws Exception {
            mockMvc.perform(get("/tasks"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("403 Forbidden: Неверная роль")
        @WithMockUser(roles = "ADMIN")
        void failForbidden() throws Exception {
            mockMvc.perform(get("/tasks"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /tasks/{id} - Получение задачи")
    class GetTaskTests {

        private final UUID taskId = UUID.randomUUID();

        @Test
        @DisplayName("200 OK: Успешное получение задачи")
        @WithMockUser(roles = "USER")
        void successGet() throws Exception {
            ru.cinimex.taskapi.dto.TaskResponse response = new ru.cinimex.taskapi.dto.TaskResponse();
            response.setId(taskId);
            response.setTitle("Тестовая задача");
            response.setDescription("Описание");
            response.setStatus("CREATED");

            when(taskService.getTaskById(taskId)).thenReturn(response);

            mockMvc.perform(get("/tasks/" + taskId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(taskId.toString()))
                    .andExpect(jsonPath("$.title").value("Тестовая задача"))
                    .andExpect(jsonPath("$.status").value("CREATED"));
        }

        @Test
        @DisplayName("400 Bad Request: Задача не найдена")
        @WithMockUser(roles = "USER")
        void failNotFound() throws Exception {
            doThrow(new TaskNotFoundException("Задача с таким ID не найдена"))
                    .when(taskService).getTaskById(taskId);

            mockMvc.perform(get("/tasks/" + taskId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Задача с таким ID не найдена"));
        }

        @Test
        @DisplayName("400 Bad Request: Попытка просмотра чужой задачи")
        @WithMockUser(roles = "USER")
        void failWrongAssignee() throws Exception {
            String errorMsg = "У вас нет прав для просмотра этой задачи";
            doThrow(new TaskBusinessException(errorMsg))
                    .when(taskService).getTaskById(taskId);

            mockMvc.perform(get("/tasks/" + taskId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(errorMsg));
        }

        @Test
        @DisplayName("401 Unauthorized: Токен отсутствует")
        void failUnauthorized() throws Exception {
            mockMvc.perform(get("/tasks/" + taskId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("403 Forbidden: Ошибка роли (например, ADMIN вместо USER)")
        @WithMockUser(roles = "ADMIN")
        void failForbidden() throws Exception {
            mockMvc.perform(get("/tasks/" + taskId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /tasks/{id} - Обновление задачи")
    class UpdateTaskTests {

        private final UUID taskId = UUID.randomUUID();
        private final TaskCreationRequest validRequest = new TaskCreationRequest(
                "Обновленное название",
                "Обновленное описание",
                OffsetDateTime.now().plusDays(2)
        );

        @Test
        @DisplayName("200 OK: Успешное обновление")
        @WithMockUser(roles = "USER")
        void successUpdate() throws Exception {
            mockMvc.perform(put("/tasks/" + taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("400 Bad Request: Задача не найдена")
        @WithMockUser(roles = "USER")
        void failNotFound() throws Exception {
            doThrow(new TaskNotFoundException("Задача не найдена"))
                    .when(taskService).updateTask(any(UUID.class), any(TaskCreationRequest.class));

            mockMvc.perform(put("/tasks/" + taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Задача не найдена"));
        }

        @Test
        @DisplayName("400 Bad Request: Неверный статус (не CREATED)")
        @WithMockUser(roles = "USER")
        void failWrongStatus() throws Exception {
            String errorMsg = "Редактирование запрещено: задача в статусе IN_PROGRESS";
            doThrow(new TaskBusinessException(errorMsg))
                    .when(taskService).updateTask(any(UUID.class), any(TaskCreationRequest.class));

            mockMvc.perform(put("/tasks/" + taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(errorMsg));
        }

        @Test
        @DisplayName("400 Bad Request: Попытка обновить чужую задачу")
        @WithMockUser(roles = "USER")
        void failWrongAssignee() throws Exception {
            String errorMsg = "Нет прав для изменения данной задачи";
            doThrow(new TaskNotFoundException(errorMsg))
                    .when(taskService).updateTask(any(UUID.class), any(TaskCreationRequest.class));

            mockMvc.perform(put("/tasks/" + taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(errorMsg));
        }

        @Test
        @DisplayName("401 Unauthorized: Отсутствует токен")
        void failUnauthorized() throws Exception {
            mockMvc.perform(put("/tasks/" + taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("403 Forbidden: Недостаточно прав (не USER)")
        @WithMockUser(roles = "ADMIN")
        void failForbidden() throws Exception {
            mockMvc.perform(put("/tasks/" + taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("400 Bad Request: Ошибка валидации (дата в прошлом)")
        @WithMockUser(roles = "USER")
        void failValidation() throws Exception {
            TaskCreationRequest invalidRequest = new TaskCreationRequest(
                    "Title", "Desc", OffsetDateTime.now().minusDays(1));

            mockMvc.perform(put("/tasks/" + taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ошибка валидации")));
        }
    }

    @Nested
    @DisplayName("DELETE /tasks/{id} - Удаление задачи")
    class DeleteTaskTests {

        private final UUID taskId = UUID.randomUUID();

        @Test
        @DisplayName("200 OK: Успешное удаление")
        @WithMockUser(roles = "USER")
        void successDelete() throws Exception {
            mockMvc.perform(delete("/tasks/" + taskId))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("400 Bad Request: Задача не существует")
        @WithMockUser(roles = "USER")
        void failNotFound() throws Exception {
            String errorMessage = "Задача не найдена";

            doThrow(new TaskNotFoundException(errorMessage))
                    .when(taskService).deleteTask(taskId);

            mockMvc.perform(delete("/tasks/" + taskId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("400 Bad Request: Статус задачи не CREATED")
        @WithMockUser(roles = "USER")
        void failInvalidStatus() throws Exception {
            String errorMessage = "Нельзя удалить задачу в статусе IN_PROGRESS";

            doThrow(new TaskBusinessException(errorMessage))
                    .when(taskService).deleteTask(taskId);

            mockMvc.perform(delete("/tasks/" + taskId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("400 Bad Request: Задача принадлежит другому пользователю")
        @WithMockUser(roles = "USER")
        void failWrongAssignee() throws Exception {
            String errorMessage = "Нет прав для удаления данной задачи";

            doThrow(new TaskNotFoundException(errorMessage))
                    .when(taskService).deleteTask(taskId);

            mockMvc.perform(delete("/tasks/" + taskId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("401 Unauthorized: JWT токен не передан")
        void failUnauthorized() throws Exception {
            mockMvc.perform(delete("/tasks/" + taskId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("403 Forbidden: Недостаточно прав (нет роли USER)")
        @WithMockUser(roles = "ADMIN")
        void failForbidden() throws Exception {
            mockMvc.perform(delete("/tasks/" + taskId))
                    .andExpect(status().isForbidden());
        }
    }
}