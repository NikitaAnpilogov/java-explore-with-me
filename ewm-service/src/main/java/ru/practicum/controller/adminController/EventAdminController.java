package ru.practicum.controller.adminController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.SearchEventParamsAdmin;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.service.EventService;

import java.util.List;

@Slf4j
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
@RestController
public class EventAdminController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> searchEvents(@Valid SearchEventParamsAdmin paramsAdmin) {
        log.info("ADMIN - GET запрос на получение списка событий с параметрами: {}", paramsAdmin);
        return eventService.getAllEventFromAdmin(paramsAdmin);
    }

    @PatchMapping("/{id}")
    public EventFullDto updateEvent(@PathVariable(value = "id") @Min(1) Long id,
                                    @Valid @RequestBody UpdateEventAdminRequest request) {
        log.info("ADMIN - PATCH запрос на обновление события с id: {} и параметрами: {}", id, request);
        return eventService.updateEventFromAdmin(id, request);
    }


}