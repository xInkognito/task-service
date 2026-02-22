package ru.cinimex.task.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ru.cinimex.task.domain.TaskEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")}) // -2 - SKIP LOCKED
    @Query("SELECT t FROM TaskEntity t WHERE t.status = 'CREATED' AND t.notificateAt <= :now")
    List<TaskEntity> findTasksToNotify(OffsetDateTime now, Pageable pageable);
}
