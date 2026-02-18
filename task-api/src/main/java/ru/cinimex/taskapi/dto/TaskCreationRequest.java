package ru.cinimex.taskapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запрос на создание задачи")
public class TaskCreationRequest {

    @NotBlank
    @Schema(description = "Наименование задачи", example = "Тестирование сервиса")
    private String title;

    @Schema(description = "Описание задачи, может быть null", example = "Написать юнит-тесты для сервиса оркестрации задач")
    private String description;

    @NotBlank
    @Future(message = "Дата уведомления должна быть в будущем")
    @Schema(description = "Время отправки уведомления", example = "2024-08-14T13:52:22")
    private OffsetDateTime notificateAt;
}
