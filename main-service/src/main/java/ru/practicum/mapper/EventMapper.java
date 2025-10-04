package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.model.Event;

@UtilityClass
public class EventMapper {
    public Event map(NewEventDto newEventDto) {
        if (newEventDto.getParticipantLimit() == null) {
            newEventDto.setParticipantLimit(0);
        }
        return Event.builder()
                .id(null)
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(LocationMapper.map(newEventDto.getLocation()))
                .paid(newEventDto.isPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.isRequestModeration())
                .title(newEventDto.getTitle())
                .build();
    }

    public EventFullDto mapToFull(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.map(event.getCategory()))
                .createdOn(event.getCreatedDate())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserMapper.mapToShort(event.getInitiator()))
                .location(LocationMapper.map(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublisherDate())
                .requestModeration(event.isRequestModeration())
                .state(event.getEventStatus())
                .title(event.getTitle())
                .build();
    }

    public EventShortDto mapToShort(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.map(event.getCategory()))
                .eventDate(event.getEventDate())
                .initiator(UserMapper.mapToShort(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .build();
    }
}