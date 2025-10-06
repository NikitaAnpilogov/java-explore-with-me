package ru.practicum.exceptions;

import lombok.Getter;

@Getter
public class CorrelationException extends RuntimeException {
    final String reason;

    public CorrelationException(final String message, String reason) {
        super(message);
        this.reason = reason;
    }
}