package ru.practicum.controller.publicController;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CompilationDto;
import ru.practicum.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(path = "/compilations")
public class CompilationPublicController {

    private final CompilationService compilationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        return compilationService.getAllCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto findByIdCompilation(@PathVariable Long compId) {
        log.info("GET запрос на удаление подборки событий");
        return compilationService.findByIdCompilation(compId);
    }
}