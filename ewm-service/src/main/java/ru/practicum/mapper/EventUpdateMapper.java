package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.UpdateEventRequest;
import ru.practicum.model.Event;

import java.util.function.Consumer;

@UtilityClass
public class EventUpdateMapper {

    public static void updateEventFromRequest(Event target, UpdateEventRequest source) {
        if (source == null || target == null) return;

        updateIfNotNullAndNotBlank(source.getAnnotation(), target::setAnnotation);
        updateIfNotNullAndNotBlank(source.getDescription(), target::setDescription);
        updateIfNotNullAndNotBlank(source.getTitle(), target::setTitle);
        updateIfNotNull(source.getLocation(), target::setLocation);
        updateIfNotNull(source.getParticipantLimit(), target::setParticipantLimit);
        updateIfNotNull(source.getPaid(), target::setPaid);
        updateIfNotNull(source.getRequestModeration(), target::setRequestModeration);
    }

    private static <T> void updateIfNotNull(T newValue, Consumer<T> setter) {
        if (newValue != null) setter.accept(newValue);
    }

    private static void updateIfNotNullAndNotBlank(String newValue, Consumer<String> setter) {
        if (newValue != null && !newValue.isBlank()) setter.accept(newValue);
    }
}
