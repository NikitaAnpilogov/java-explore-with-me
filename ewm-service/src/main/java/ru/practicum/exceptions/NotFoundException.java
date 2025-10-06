package ru.practicum.exceptions;

public class NotFoundException extends RuntimeException {
    final String reason;

    public NotFoundException(final String message, String reason) {
        super(message);
        this.reason = reason;
    }
}