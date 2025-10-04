package ru.practicum.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException ex) {
        log.error("Not found exception: {}, reason - {}, stacktrace: {}", ex.getMessage(), ex.reason, ex.getStackTrace());
        return ApiError.builder()
                .errors(Collections.singletonList(ex.getMessage()))
                .status(HttpStatus.NOT_FOUND.toString())
                .message(ex.getMessage())
                .reason(ex.reason)
                .build();
    }

    @ExceptionHandler(IncorrectParametersException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerIncorrectParametersException(IncorrectParametersException e) {
        log.debug("Получен статус 400 BAD_REQUEST {}", e.getMessage(), e);
        return ApiError.builder()
                .errors(Collections.singletonList(e.getMessage()))
                .status(HttpStatus.BAD_REQUEST.toString())
                .message(e.getMessage())
                .reason(e.reason)
                .build();
    }

    @ExceptionHandler(CorrelationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCorrelationException(CorrelationException e) {
        log.debug("Получен статус 400 BAD_REQUEST {}", e.getMessage(), e);
        return ApiError.builder()
                .errors(Collections.singletonList(e.getMessage()))
                .status(HttpStatus.BAD_REQUEST.toString())
                .message(e.getMessage())
                .reason(e.reason)
                .build();
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handlerIncorrectParametersException(Exception e) {
        log.debug("Получен статус 400 BAD_REQUEST {}", e.getMessage(), e);
        return ApiError.builder()
                .errors(Collections.singletonList(e.getMessage()))
                .status(HttpStatus.BAD_REQUEST.toString())
                .message(e.getMessage())
                .reason(e.getCause().getLocalizedMessage())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerConstraintViolationException(ConstraintViolationException e) {
        log.debug("Получен статус 409 CONFLICT {}", e.getMessage(), e);
        return ApiError.builder()
                .errors(Collections.singletonList(e.getLocalizedMessage()))
                .status(HttpStatus.CONFLICT.toString())
                .message("Попытка добавления данных которые уже есть.")
                .reason(e.getCause().getLocalizedMessage())
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerConflictException(ConflictException e) {
        log.debug("Получен статус 409 CONFLICT {}", e.getMessage(), e);
        return ApiError.builder()
                .errors(Collections.singletonList(e.getMessage()))
                .status(HttpStatus.CONFLICT.toString())
                .message(e.getMessage())
                .reason(e.reason)
                .build();
    }

    @ExceptionHandler({PSQLException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerPSQLException(Exception e) {
        log.debug("Получен статус 409 CONFLICT {}", e.getMessage(), e);
        return ApiError.builder()
                .errors(Collections.singletonList(e.getMessage()))
                .status(HttpStatus.CONFLICT.toString())
                .message(e.getMessage())
                .reason(e.getCause().getLocalizedMessage())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handlerOtherException(Throwable e) {
        log.warn("Получен статус 500 SERVER_ERROR {}", e.getMessage(), e);
        return ApiError.builder()
                .errors(Collections.singletonList(e.getMessage()))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .message(e.getMessage())
                .reason("INTERNAL_SERVER_ERROR")
                .build();
    }

}