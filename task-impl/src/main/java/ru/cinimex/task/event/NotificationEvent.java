package ru.cinimex.task.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationEvent {
    private String email;
    private String title;
    private String description;
}
