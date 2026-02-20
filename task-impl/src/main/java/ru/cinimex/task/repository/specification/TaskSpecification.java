package ru.cinimex.task.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import ru.cinimex.task.domain.TaskEntity;

import java.time.OffsetDateTime;

public class TaskSpecification {

    public static Specification<TaskEntity> filterTasks(
            String title,
            String status,
            OffsetDateTime start,
            OffsetDateTime end,
            String assignee) {

        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            // Фильтр по исполнителю
            predicates.getExpressions().add(cb.equal(root.get("assignee"), assignee));

            // Фильтр по названию (поиск подстроки, без учета регистра)
            if (title != null && !title.isBlank()) {
                predicates.getExpressions().add(
                        cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%")
                );
            }

            // Фильтр по статусу
            if (status != null && !status.isBlank()) {
                predicates.getExpressions().add(cb.equal(root.get("status"), status));
            }

            // Фильтр по диапазону дат
            if (start != null) {
                predicates.getExpressions().add(cb.greaterThanOrEqualTo(root.get("notificateAt"), start));
            }
            if (end != null) {
                predicates.getExpressions().add(cb.lessThanOrEqualTo(root.get("notificateAt"), end));
            }

            return predicates;
        };
    }
}
