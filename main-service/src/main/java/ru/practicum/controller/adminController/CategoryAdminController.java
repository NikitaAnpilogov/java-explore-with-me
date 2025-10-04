package ru.practicum.controller.adminController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.service.CategoryService;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/categories")
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("ADMIN - POST - создание новой категории: {}", newCategoryDto);
        return categoryService.addNewCategory(newCategoryDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        log.info("ADMIN - DELETE - удаление категории: {}", id);
        categoryService.deleteCategoryById(id);
    }

    @PatchMapping("/{id}")
    public CategoryDto updateCategory(@PathVariable(value = "id") @Min(1) Long id,
                                      @RequestBody @Valid CategoryDto categoryDto) {
        log.info("ADMIN - PATCH - обновление категории: {}", categoryDto);
        return categoryService.updateCategory(id, categoryDto);
    }
}