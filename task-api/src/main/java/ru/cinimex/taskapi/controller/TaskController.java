package ru.cinimex.taskapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cinimex.taskapi.dto.TaskCreatedResponse;
import ru.cinimex.taskapi.dto.TaskCreationRequest;
import ru.cinimex.taskapi.dto.TaskResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "Task Controller", description = "Управление задачами пользователей")
@RequestMapping(path = "/tasks")
public interface TaskController {

    @Operation(
            summary = "Создание задачи пользователем",
            description = "Доступно пользователям с ролями: USER"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача создана",
                    content = @Content(schema = @Schema(implementation = TaskCreatedResponse.class))),
            @ApiResponse(responseCode = "401", description = "Токен не передан (Unauthorized)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (нужна роль USER)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TaskCreatedResponse> createTask(@Valid @RequestBody TaskCreationRequest taskCreationRequest);

    @Operation(summary = "Получение списка задач с фильтрацией", description = "Возвращает задачи текущего пользователя. Доступ: USER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач получен",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Токен не передан (Unauthorized)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (нужна роль USER)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<TaskResponse>> getTasks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) OffsetDateTime notificateAtStart,
            @RequestParam(required = false) OffsetDateTime notificateAtEnd
    );

    @Operation(summary = "Получение задачи по ID", description = "Доступ только к своим задачам. Доступ: USER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача найдена",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Задача не найдена или не принадлежит пользователю", content = @Content),
            @ApiResponse(responseCode = "401", description = "Токен не передан (Unauthorized)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (нужна роль USER)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TaskResponse> getTaskById(@PathVariable UUID id);

    @Operation(summary = "Обновление задачи", description = "Обновление возможно только в статусе CREATED. Доступ: USER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача обновлена", content = @Content),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или неверный статус", content = @Content),
            @ApiResponse(responseCode = "401", description = "Токен не передан (Unauthorized)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (нужна роль USER)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateTask(@PathVariable UUID id, @Valid @RequestBody TaskCreationRequest taskUpdateRequest);

    @Operation(summary = "Удаление задачи", description = "Удаление возможно только в статусе CREATED. Доступ: USER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача удалена", content = @Content),
            @ApiResponse(responseCode = "400", description = "Ошибка или неверный статус", content = @Content),
            @ApiResponse(responseCode = "401", description = "Токен не передан (Unauthorized)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав (нужна роль USER)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content)
    })
    @DeleteMapping(value = "/{id}")
    ResponseEntity<Void> deleteTask(@PathVariable UUID id);
}
