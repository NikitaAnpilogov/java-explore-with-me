package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatRequest;
import ru.practicum.ViewStats;
import ru.practicum.ViewsStatsRequest;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class StatsController {
    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void hit(@RequestBody StatRequest hit) {
        log.info("POST - сохранения вызова hit {}", hit);
        service.saveHit(hit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(defaultValue = "false") boolean unique) {
        log.info("GET - получение статистики с параметрами start={}, end={}, uris={}, unique={}",
                start,
                end,
                uris,
                unique);
        if (uris == null) {
            uris = Collections.emptyList();
        }

        List<ViewStats> results = service.getViewStatsList(
                ViewsStatsRequest.builder()
                        .start(start)
                        .end(end)
                        .uri(uris)
                        .unique(unique)
                        .build()
        );
        return results;
    }
}