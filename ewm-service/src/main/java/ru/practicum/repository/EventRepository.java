package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.model.Category;
import ru.practicum.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Optional<Event> findByInitiatorIdAndId(Long userId, Long eventId);

    List<Event> findAllByIdIn(List<Long> ids);

    List<Event> findByCategory(Category category);

    Page<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);
}