package ru.cinimex.task.mapper;

import org.mapstruct.*;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.cinimex.task.domain.TaskEntity;
import ru.cinimex.taskapi.dto.TaskCreationRequest;
import ru.cinimex.taskapi.dto.TaskResponse;

import java.time.OffsetDateTime;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
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
    protected void postMappingSteps(TaskCreationRequest dto, @MappingTarget TaskEntity entity) {
        entity.setStatus("CREATED");

        OffsetDateTime now = OffsetDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        entity.setAssignee(login);
    }

    @AfterMapping
    protected void updateTimestamp(@MappingTarget TaskEntity entity) {
        entity.setUpdatedAt(OffsetDateTime.now());
    }
}
