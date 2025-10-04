package ru.practicum.controller.publicController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.SearchEventParams;
import ru.practicum.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/events")
public class EventPublicController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllEvents(@Valid SearchEventParams searchEventParams,
                                            HttpServletRequest request) {
        log.info("GET запрос на получения событий с фильтром");
        return eventService.getAllEventFromPublic(searchEventParams, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable(value = "id") @Min(1) Long id,
                                     HttpServletRequest request) {
        log.info("PUBLIC -GET запрос на получения полной информации о событии с  id - {}", id);
        return eventService.getEventById(id, request);
    }
}