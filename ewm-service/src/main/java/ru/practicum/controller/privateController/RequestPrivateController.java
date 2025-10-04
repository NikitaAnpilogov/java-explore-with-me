package ru.practicum.controller.privateController;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "users/{userId}/requests")
public class RequestPrivateController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable(value = "userId") @Positive Long userId,
                                              @RequestParam(name = "eventId") @Positive Long eventId) {
        log.info("PRIVATE - POST запрос на создание запроса на участие в событии с id - {}  пользователя с id - {}",
                eventId, userId);
        return requestService.addNewRequest(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getAllRequests(@PathVariable(value = "userId") @Positive Long userId) {
        log.info("PRIVATE - GET запрос на получение всех запросов на участие в событиях пользователя с id - {}", userId);
        return requestService.getRequestsByUserId(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto canceledRequest(@PathVariable(value = "userId") @Positive Long userId,
                                                   @PathVariable(value = "requestId") @Positive Long requestId) {
        log.info("PRIVATE - PATCH запрос на отмену запроса пользователем с id - {}", userId);
        return requestService.cancelRequest(userId, requestId);
    }
}