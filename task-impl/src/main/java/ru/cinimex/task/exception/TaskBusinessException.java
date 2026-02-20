package ru.cinimex.task.exception;

public class TaskBusinessException extends RuntimeException {
    public TaskBusinessException(String message) {
        super(message);
    }
}
