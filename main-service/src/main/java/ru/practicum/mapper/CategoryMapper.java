package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.model.Category;

@UtilityClass
public class CategoryMapper {
    public Category map(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    public CategoryDto map(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}