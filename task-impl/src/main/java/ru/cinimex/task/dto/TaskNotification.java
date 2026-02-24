package ru.cinimex.task.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskNotification {
    private String email;
    private String header;
    private String body;
}
