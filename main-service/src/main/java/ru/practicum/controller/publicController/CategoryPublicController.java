package ru.practicum.controller.publicController;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.service.CategoryService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/categories")
public class CategoryPublicController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                           @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("PUBLIC - GET запрос на получение категорий списком с размерами {}, {}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{id}")
    public CategoryDto getCategory(@PathVariable Long id) {
        log.info("PUBLIC - GET запрос на получение категории с id={}", id);
        return categoryService.getCategoryById(id);
    }
}