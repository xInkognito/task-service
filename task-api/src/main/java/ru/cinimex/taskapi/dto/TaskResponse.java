package ru.cinimex.taskapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Полная информация о задаче")
public class TaskResponse {

    @Schema(description = "Идентификатор задачи", example = "7245f92e-6cc3-4220-bf80-7369ee2e1011")
    private UUID id;

    @Schema(description = "Заголовок", example = "Тестирование сервиса")
    private String title;

    @Schema(description = "Описание", example = "Написать тесты")
    private String description;

    @Schema(description = "Статус задачи", example = "CREATED")
    private String status;

    @Schema(description = "Дата уведомления")
    private OffsetDateTime notificateAt;

    @Schema(description = "Дата создания")
    private OffsetDateTime createdAt;

    @Schema(description = "Дата обновления")
    private OffsetDateTime updatedAt;

    @Schema(description = "Логин исполнителя", example = "ivan_user")
    private String assignee;
}
