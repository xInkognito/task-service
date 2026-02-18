package ru.cinimex.taskapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ответ с UUID созданной задачи")
public class TaskCreatedResponse {

    @Schema(
            description = "Идентификатор задачи",
            example = "7245f92e-6cc3-4220-bf80-7369ee2e1011"
    )
    private UUID generatedId;
}
