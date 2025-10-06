package ru.practicum.controller.adminController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.service.CompilationService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/compilations")
public class CompilationAdminController {

    private final CompilationService compilationService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto compilationDto) {
        log.info("ADMIN - POST запрос на создание подборки событий");
        return compilationService.addCompilation(compilationDto);
    }

    @PatchMapping("/{id}")
    public CompilationDto updateCompilation(@RequestBody @Valid UpdateCompilationRequest update,
                                            @PathVariable Long id) {
        log.info("ADMIN - PATCH запрос на обнавление подборки событий");
        return compilationService.updateCompilation(id, update);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long id) {
        log.info("ADMIN - DELETE запрос на удаление подборки событий");
        compilationService.deleteCompilation(id);
    }

}