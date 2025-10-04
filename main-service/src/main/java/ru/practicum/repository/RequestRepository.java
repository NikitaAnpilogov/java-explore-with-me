package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Request;
import ru.practicum.model.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByEventIdInAndStatus(List<Long> eventIds, RequestStatus status);

    Optional<Request> findByIdAndRequesterId(Long id, Long requesterId);

    List<Request> findAllByRequesterId(Long requesterId);

    Optional<List<Request>> findByEventIdAndIdIn(Long eventId, List<Long> id);

    Boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    int countByEventIdAndStatus(Long eventId, RequestStatus status);
}