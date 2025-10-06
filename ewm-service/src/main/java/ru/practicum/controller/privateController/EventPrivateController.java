package ru.practicum.controller.privateController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.*;
import ru.practicum.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/users/{userId}/events")
public class EventPrivateController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllEventsByUserId(@PathVariable(value = "userId") @Min(1) Long userId,
                                                    @RequestParam(value = "from", defaultValue = "0")
                                                    @PositiveOrZero Integer from,
                                                    @RequestParam(value = "size", defaultValue = "10")
                                                    @Positive Integer size) {
        log.info("PRIVATE - GET запрос на получения событий пользователя с id - {}", userId);
        return eventService.getEventsByUserId(userId, from, size);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable(value = "userId") @Min(1) Long userId,
                                 @RequestBody @Valid NewEventDto input) {
        log.info("POST запрос на создание события от пользователя с id= {}", userId);
        return eventService.addNewEvent(userId, input);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getFullEventByOwner(@PathVariable(value = "userId") @Min(1) Long userId,
                                            @PathVariable(value = "eventId") @Min(1) Long eventId) {
        log.info("GET запрос на получения полной информации о событии для пользователя с id= {}", userId);
        return eventService.getEventByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByOwner(@PathVariable(value = "userId") @Min(0) Long userId,
                                           @PathVariable(value = "eventId") @Min(0) Long eventId,
                                           @RequestBody @Valid UpdateEventUserRequest requestUpdate) {
        log.info("PATCH запрос на обновление события от пользователя с id= {}", userId);
        return eventService.updateEventByUserIdAndEventId(userId, eventId, requestUpdate);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getAllRequestByEventFromOwner(@PathVariable(value = "userId") @Min(1) Long userId,
                                                                       @PathVariable(value = "eventId") @Min(1) Long eventId) {
        log.info("GET запрос на получение информации о всех запросах об участии в событии для пользователя с id= {}", userId);
        return eventService.getAllParticipationRequestsFromEventByOwner(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResponse updateStatusRequestFromOwner(@PathVariable(value = "userId") @Min(1) Long userId,
                                                                         @PathVariable(value = "eventId") @Min(1) Long eventId,
                                                                         @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("PATCH запрос на обновление статуса события от пользователя с id= {}", userId);
        return eventService.updateStatusRequest(userId, eventId, request);
    }

}