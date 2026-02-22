package ru.cinimex.task.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cinimex.task.domain.TaskEntity;
import ru.cinimex.task.event.NotificationEvent;
import ru.cinimex.task.feign.UserClient;
import ru.cinimex.task.repository.TaskRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationScheduler {

    private final TaskRepository taskRepository;
    private final UserClient userClient;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${app.kafka.notification-topic}")
    private String topicName;

    @Value("${app.scheduling.batch-size:5}")
    private int batchSize;

    @Value("${app.security.technical-token}")
    private String techToken;

    @Scheduled(cron = "${app.scheduling.notification-cron}")
    @Transactional
    public void processNotifications() {
        log.info("Starting notification processing job...");

        // Получаем ограниченный список задач
        List<TaskEntity> tasks = taskRepository.findTasksToNotify(
                OffsetDateTime.now(),
                PageRequest.of(0, batchSize)
        );

        if (tasks.isEmpty()) {
            return;
        }

        for (TaskEntity task : tasks) {
            try {
                // Получаем email через Feign
                String userEmail = fetchUserEmail(task.getAssignee());

                // Отправляем в Kafka
                sendToKafka(userEmail, task);

                // Успех
                task.setStatus("DONE");
            } catch (Exception e) {
                log.error("Failed to process task {}: {}", task.getId(), e.getMessage());
                task.setStatus("ERROR");
            }
        }

        // Сохраняем всё пачкой в конце транзакции
        taskRepository.saveAll(tasks);
    }

    private String fetchUserEmail(String login) {
        String authHeader = techToken.startsWith("Bearer ") ? techToken : "Bearer " + techToken;
        return userClient.getUserByLogin(login, authHeader).getEmail();
    }

    private void sendToKafka(String email, TaskEntity task) {
        // Формируем объект сообщения
        var message = new NotificationEvent(email, task.getTitle(), task.getDescription());
        kafkaTemplate.send(topicName, task.getId().toString(), message);
    }
}