package ru.cinimex.task.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.cinimex.task.domain.TaskEntity;
import ru.cinimex.taskapi.dto.TaskCreationRequest;

import java.time.OffsetDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    public abstract TaskEntity toEntity(TaskCreationRequest request);

    @AfterMapping
    protected void setupEntity(@MappingTarget TaskEntity entity) {
        entity.setId(UUID.randomUUID());
        entity.setStatus("CREATED");
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
    }
}
