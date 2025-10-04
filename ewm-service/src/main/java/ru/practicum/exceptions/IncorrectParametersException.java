package ru.practicum.exceptions;

import lombok.Getter;

@Getter
public class IncorrectParametersException extends RuntimeException {
    final String reason;

    public IncorrectParametersException(final String message, String reason) {
        super(message);
        this.reason = reason;
    }
}