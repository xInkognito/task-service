package ru.cinimex.task.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.cinimex.task.domain.TaskEntity;
import ru.cinimex.task.dto.TaskNotification;
import ru.cinimex.task.feign.UserClient;
import ru.cinimex.task.feign.dto.UserResponse;
import ru.cinimex.task.repository.TaskRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserClient userClient;
    @Mock
    private KafkaTemplate<String, TaskNotification> kafkaTemplate;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationScheduler, "topicName", "test-topic");
        ReflectionTestUtils.setField(notificationScheduler, "batchSize", 5);
        ReflectionTestUtils.setField(notificationScheduler, "techToken", "secret-token");
    }

    @Test
    @DisplayName("Успешная обработка уведомлений")
    void shouldProcessNotificationsSuccessfully() {
        UUID taskId = UUID.randomUUID();
        TaskEntity task = new TaskEntity();
        task.setId(taskId);
        task.setTitle("Test Task");
        task.setAssignee("user1");
        task.setStatus("CREATED");

        when(taskRepository.findTasksToNotify(any(), any()))
                .thenReturn(List.of(task));

        var userDto = new UserResponse();
        userDto.setEmail("test@example.com");
        when(userClient.getUserByLogin(eq("user1"), anyString())).thenReturn(userDto);

        notificationScheduler.processNotifications();

        // Проверка отправки в Kafka
        verify(kafkaTemplate).send(eq("test-topic"), eq(taskId.toString()), any(TaskNotification.class));

        // Проверка смены статуса
        assertEquals("DONE", task.getStatus());

        // Проверка сохранения в БД
        verify(taskRepository).saveAll(anyList());
    }
}