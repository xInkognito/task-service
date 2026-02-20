package ru.cinimex.task.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.cinimex.task.domain.TaskEntity;
import ru.cinimex.taskapi.dto.TaskCreationRequest;
import ru.cinimex.taskapi.dto.TaskResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    public abstract TaskEntity toEntity(TaskCreationRequest request);

    public abstract TaskResponse toResponse(TaskEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    public abstract void updateEntityFromDto(TaskCreationRequest dto, @MappingTarget TaskEntity entity);

    @AfterMapping
    protected void fillPersistentFields(@MappingTarget TaskEntity entity) {
        entity.setId(UUID.randomUUID());
        entity.setStatus("CREATED");
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());

        // Извлекаем логин пользователя из SecurityContext (JWT)
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        entity.setAssignee(login);
    }

    @AfterMapping
    protected void updateTimestamp(@MappingTarget TaskEntity entity) {
        entity.setUpdatedAt(OffsetDateTime.now());
    }
}
