package ru.cinimex.task.repository.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import ru.cinimex.task.domain.TaskEntity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TaskSpecification {

    public static Specification<TaskEntity> filterTasks(
            String title, String status, OffsetDateTime start, OffsetDateTime end, String assignee) {

        return (root, query, cb) -> {
            log.info("Filtering tasks: title={}, status={}, start={}, end={}, assignee={}",
                    title, status, start, end, assignee);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("assignee"), assignee));

            if (title != null && !title.isBlank()) {
                log.debug("Applying title filter: {}", title);
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }

            if (status != null && !status.isBlank()) {
                log.debug("Applying status filter: {}", status);
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (start != null) {
                log.debug("Applying start date filter: {}", start);
                predicates.add(cb.greaterThanOrEqualTo(root.get("notificateAt"), start));
            }
            if (end != null) {
                log.debug("Applying end date filter: {}", end);
                predicates.add(cb.lessThanOrEqualTo(root.get("notificateAt"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
