package ru.practicum.exceptions;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {
    final String reason;

    public ConflictException(final String message, String reason) {
        super(message);
        this.reason = reason;
    }
}