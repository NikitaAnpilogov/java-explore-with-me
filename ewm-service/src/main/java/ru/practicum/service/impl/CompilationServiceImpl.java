package ru.practicum.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CompilationService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CompilationDto addCompilation(NewCompilationDto compilationDto) {
        log.info("Добавление подборки {}", compilationDto.toString());
        Compilation compilation = CompilationMapper.map(compilationDto);
        compilation.setPinned(Optional.of(compilation.isPinned()).orElse(false));

        Set<Long> compilationEventIds = (compilationDto.getEvents() != null) ? compilationDto.getEvents() : Collections.emptySet();
        List<Long> eventIds = new ArrayList<>(compilationEventIds);
        List<Event> events = eventRepository.findAllByIdIn(eventIds);
        Set<Event> eventsSet = new HashSet<>(events);
        compilation.setEvents(eventsSet);

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Подборка добавлена: {}", savedCompilation);
        return CompilationMapper.map(savedCompilation);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long id, UpdateCompilationRequest compilationDto) {
        Compilation compilation = checkCompilation(id);
        log.info("Обновление подборки c {}, на {}", compilationDto.toString(), compilation.toString());
        Set<Long> eventIds = compilationDto.getEvents();
        if (eventIds != null) {
            List<Event> events = eventRepository.findAllByIdIn(new ArrayList<>(eventIds));
            Set<Event> eventsSet = new HashSet<>(events);
            compilation.setEvents(eventsSet);
        }
        log.trace("SetEvents = {}", compilation.getEvents());
        compilation.setPinned(Optional.of(compilationDto.isPinned()).orElse(false));
        log.trace("SetPinned = {}", compilation.isPinned());
        compilation.setTitle(Optional.ofNullable(compilationDto.getTitle()).orElse(compilation.getTitle()));
        log.info("Подборка обновлена: {}", compilation);
        return CompilationMapper.map(compilation);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long id) {
        log.info("Удаление подборки c {}", id);
        checkCompilation(id);
        compilationRepository.deleteById(id);
        log.info("Подборка удалена");
    }

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение всех подборок с {}, размер {}, pinned = {}", from, size, pinned);
        PageRequest pageRequest = PageRequest.of(from, size);
        List<Compilation> compilations;
        if (pinned != null) {
            log.info("Получение всех подборок с pinned: {}", pinned);
            compilations = compilationRepository.findAllByPinned(pinned, pageRequest);
            log.info("Получены - подборки с pinned: {}", compilations);
        } else {
            log.info("Получение всех подборок без pinned: {}", pinned);
            compilations = compilationRepository.findAll(pageRequest).getContent();
            log.info("Получены - подборки без pinned: {}", compilations);

        }
        return compilations.stream()
                .map(CompilationMapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto findByIdCompilation(Long id) {
        log.info("Получение подборки c {}", id);
        return CompilationMapper.map(checkCompilation(id));
    }

    private Compilation checkCompilation(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Такой подборки нет.", "Подборка с id = " + compId + " не найдена."));
    }
}