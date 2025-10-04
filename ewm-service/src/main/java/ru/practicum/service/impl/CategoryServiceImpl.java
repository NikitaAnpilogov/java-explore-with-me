package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        log.info("Обработка запроса на получение категорий с фильтром c {} и {} по страницы)", from, size);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageRequest)
                .stream()
                .map(CategoryMapper::map)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = checkIsCategoryExist(id);
        log.info("Найдена категория - {}", category);
        return CategoryMapper.map(category);
    }

    @Override
    @Transactional
    public CategoryDto addNewCategory(NewCategoryDto newCategoryDto) {
        log.info("Добавление новой категории");
        Category category = CategoryMapper.map(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);
        log.info("Сохранена новая категория - {}", savedCategory);
        return CategoryMapper.map(savedCategory);
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long id) {
        log.info("Удаление категории с id = {}", id);
        Category category = checkIsCategoryExist(id);
        List<Event> events = eventRepository.findByCategory(category);
        if (!events.isEmpty()) {
            throw new ConflictException("Нельзя удалить категорию!", "Существуют события с такой категорией.");
        }
        categoryRepository.deleteById(id);
        log.info("Удалена категория с id = {}", id);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto category) {
        log.info("Обновление категории с id = {}, новая категория = {}", id, category);
        Category oldCategory = checkIsCategoryExist(id);

        String newName = category.getName();
        if (newName != null && !newName.equals(oldCategory.getName())) {
            checkIsUniqueNameCategory(newName);
        }
        oldCategory.setName(newName);
        Category updatedCategory = categoryRepository.save(oldCategory);
        log.info("Категория обновлена {}", updatedCategory);
        return CategoryMapper.map(updatedCategory);
    }

    private void checkIsUniqueNameCategory(String categoryName) {
        log.info("Проверка на уникальность имени категории {}", categoryName);
        if (categoryRepository.existsByNameIgnoreCase(categoryName)) {
            throw new ConflictException("Категория с таким именем уже существует.", "Категория с именем " + categoryName + " уже существует.");
        }
    }

    private Category checkIsCategoryExist(Long id) {
        log.info("Проверка на существование категории с id = {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundException("Категория не найдена.", "Категории с id = " + id + " не существует"));
    }
}