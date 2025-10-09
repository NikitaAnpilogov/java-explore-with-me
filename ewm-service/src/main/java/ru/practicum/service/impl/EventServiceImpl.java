package ru.practicum.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.ViewStats;
import ru.practicum.dto.*;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.CorrelationException;
import ru.practicum.exceptions.IncorrectParametersException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.EventUpdateMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.model.enums.EventAdminState;
import ru.practicum.model.enums.EventStatus;
import ru.practicum.model.enums.EventUserState;
import ru.practicum.model.enums.RequestStatus;
import ru.practicum.repository.*;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StatsClient statsClient;
    private final RequestRepository requestRepository;
    private final LocationRepository locationRepository;
    private final ObjectMapper viewStatsMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAllEventFromAdmin(SearchEventParamsAdmin searchEventParamsAdmin) {
        PageRequest pageable = PageRequest.of(searchEventParamsAdmin.getFrom() / searchEventParamsAdmin.getSize(),
                searchEventParamsAdmin.getSize());
        Specification<Event> specification = Specification.where(null);

        List<Long> users = searchEventParamsAdmin.getUsers();
        List<String> states = searchEventParamsAdmin.getStates();
        List<Long> categories = searchEventParamsAdmin.getCategories();
        LocalDateTime rangeEnd = searchEventParamsAdmin.getEnd();
        LocalDateTime rangeStart = searchEventParamsAdmin.getStart();

        if (users != null && !users.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("initiator").get("id").in(users));
        }
        if (states != null && !states.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("eventStatus").as(String.class).in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories));
        }
        if (rangeEnd != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }
        if (rangeStart != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }
        Page<Event> events = eventRepository.findAll(specification, pageable);

        List<EventFullDto> result = events.getContent()
                .stream().map(EventMapper::mapToFull).collect(Collectors.toList());

        Map<Long, List<Request>> confirmedRequestsCountMap = getConfirmedRequestsCount(events.toList());
        for (EventFullDto event : result) {
            List<Request> requests = confirmedRequestsCountMap.getOrDefault(event.getId(), List.of());
            event.setConfirmedRequests(requests.size());
        }
        return result;
    }

    @Override
    @Transactional
    public EventFullDto updateEventFromAdmin(Long eventId, UpdateEventAdminRequest requestUpdate) {
        Event oldEvent = checkEvent(eventId);
        if (oldEvent.getEventStatus().equals(EventStatus.PUBLISHED) || oldEvent.getEventStatus().equals(EventStatus.CANCELED)) {
            throw new ConflictException("Действие не возможно.", "Можно изменить только неподтвержденное событие");
        }
        boolean hasChanges = false;
        Event eventForUpdate = universalUpdate(oldEvent, requestUpdate);
        if (eventForUpdate == null) {
            eventForUpdate = oldEvent;
        } else {
            hasChanges = true;
        }
        LocalDateTime gotEventDate = requestUpdate.getEventDate();
        if (gotEventDate != null) {
            if (gotEventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new IncorrectParametersException("Изменение не возможно.", "Некорректные параметры" +
                        ". Дата начала изменяемого события должна быть не ранее чем за час от даты публикации.");
            }
            eventForUpdate.setEventDate(requestUpdate.getEventDate());
            hasChanges = true;
        }

        EventAdminState gotAction = requestUpdate.getStateAction();
        if (gotAction != null) {
            if (EventAdminState.PUBLISH_EVENT.equals(gotAction)) {
                eventForUpdate.setEventStatus(EventStatus.PUBLISHED);
                hasChanges = true;
            } else if (EventAdminState.REJECT_EVENT.equals(gotAction)) {
                eventForUpdate.setEventStatus(EventStatus.CANCELED);
                hasChanges = true;
            }
        }
        Event eventAfterUpdate = null;
        if (hasChanges) {
            eventAfterUpdate = eventRepository.save(eventForUpdate);
        }
        return eventAfterUpdate != null ? EventMapper.mapToFull(eventAfterUpdate) : null;

    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найдее.", "Пользователь с id= " + userId + " не найден");
        }

        PageRequest pageRequest = PageRequest.of(from / size, size, org.springframework.data.domain.Sort.by(Sort.Direction.ASC, "id"));

        return eventRepository.findAllByInitiatorId(userId, pageRequest).getContent()
                .stream().map(EventMapper::mapToShort).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto addNewEvent(Long userId, NewEventDto newEventDto) {
        log.info("Добавление нового события {}", newEventDto);
        LocalDateTime createdTime = LocalDateTime.now();
        User user = checkUser(userId);
        checkDateAndTime(LocalDateTime.now(), newEventDto.getEventDate());
        Category category = checkCategory(newEventDto.getCategory());
        Event event = EventMapper.map(newEventDto);
        event.setCategory(category);
        event.setInitiator(user);
        event.setEventStatus(EventStatus.PENDING);
        event.setCreatedDate(createdTime);
        if (newEventDto.getLocation() != null) {
            Location location = locationRepository.save(LocationMapper.map(newEventDto.getLocation()));
            event.setLocation(location);
        }
        Event eventSaved = eventRepository.save(event);

        EventFullDto eventFullDto = EventMapper.mapToFull(eventSaved);
        eventFullDto.setViews(0L);
        eventFullDto.setConfirmedRequests(0);
        log.info("Событие сохранено {} ", eventFullDto);
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        checkUser(userId);
        Event event = checkEvenByInitiatorAndEventId(userId, eventId);
        return EventMapper.mapToFull(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUserIdAndEventId(Long userId, Long eventId, UpdateEventUserRequest requestUpdate) {
        log.info("Обновление события с id= {} ", eventId);
        checkUser(userId);
        Event oldEvent = checkEvenByInitiatorAndEventId(userId, eventId);
        if (oldEvent.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new ConflictException("Событие не обновлено.", "Лимит участников исчерпан.");
        }
        if (!oldEvent.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Событие не обновлено.", "Пользователь с id= " + userId + " не автор события");
        }
        Event eventForUpdate = universalUpdate(oldEvent, requestUpdate);
        boolean hasChanges = false;
        if (eventForUpdate == null) {
            eventForUpdate = oldEvent;
        } else {
            hasChanges = true;
        }
        LocalDateTime newDate = requestUpdate.getEventDate();
        if (newDate != null) {
            checkDateAndTime(LocalDateTime.now(), newDate);
            eventForUpdate.setEventDate(newDate);
            hasChanges = true;
        }
        EventUserState stateAction = requestUpdate.getStateAction();
        if (stateAction != null) {
            switch (stateAction) {
                case SEND_TO_REVIEW:
                    eventForUpdate.setEventStatus(EventStatus.PENDING);
                    hasChanges = true;
                    break;
                case CANCEL_REVIEW:
                    eventForUpdate.setEventStatus(EventStatus.CANCELED);
                    hasChanges = true;
                    break;
            }
        }
        Event eventAfterUpdate = null;
        if (hasChanges) {
            eventAfterUpdate = eventRepository.save(eventForUpdate);
        }

        return eventAfterUpdate != null ? EventMapper.mapToFull(eventAfterUpdate) : null;
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> getAllParticipationRequestsFromEventByOwner(Long userId, Long eventId) {
        checkUser(userId);
        checkEvenByInitiatorAndEventId(userId, eventId);
        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requests.stream().map(RequestMapper::map).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResponse updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest requestUpdate) {
        checkUser(userId);
        Event event = checkEvenByInitiatorAndEventId(userId, eventId);

        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Событие не обновлено", "Это событие не требует подтверждения запросов");
        }
        RequestStatus status = requestUpdate.getStatus();

        int confirmedRequestsCount = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        switch (status) {
            case CONFIRMED:
                if (event.getParticipantLimit() == confirmedRequestsCount) {
                    throw new ConflictException("Событие не обновлено.", "Лимит участников исчерпан.");
                }
                UpdatedStatusDto updatedStatusConfirmed = updatedStatusConfirmed(event,
                        UpdatedStatusDto.builder()
                                .idsFromUpdateStatus(new ArrayList<>(requestUpdate.getRequestIds())).build(),
                        RequestStatus.CONFIRMED, confirmedRequestsCount);

                List<Request> confirmedRequests = requestRepository.findAllById(updatedStatusConfirmed.getProcessedIds());
                List<Request> rejectedRequests = new ArrayList<>();
                if (!updatedStatusConfirmed.getIdsFromUpdateStatus().isEmpty()) {
                    List<Long> ids = updatedStatusConfirmed.getIdsFromUpdateStatus();
                    rejectedRequests = rejectRequest(ids, eventId);
                }

                return EventRequestStatusUpdateResponse.builder()
                        .confirmedRequests(confirmedRequests
                                .stream()
                                .map(RequestMapper::map).collect(Collectors.toList()))
                        .rejectedRequests(rejectedRequests
                                .stream()
                                .map(RequestMapper::map).collect(Collectors.toList()))
                        .build();
            case REJECTED:
                if (event.getParticipantLimit() == confirmedRequestsCount) {
                    throw new ConflictException("Событие не обновлено.", "Лимит участников исчерпан.");
                }

                final UpdatedStatusDto updatedStatusReject = updatedStatusConfirmed(event,
                        UpdatedStatusDto.builder()
                                .idsFromUpdateStatus(new ArrayList<>(requestUpdate.getRequestIds())).build(),
                        RequestStatus.REJECTED, confirmedRequestsCount);
                List<Request> rejectRequest = requestRepository.findAllById(updatedStatusReject.getProcessedIds());

                return EventRequestStatusUpdateResponse.builder()
                        .rejectedRequests(rejectRequest
                                .stream()
                                .map(RequestMapper::map).collect(Collectors.toList()))
                        .build();
            default:
                throw new IncorrectParametersException("Статут не обновлен", "Некорректный статус - " + status);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getAllEventFromPublic(SearchEventParams searchEventParams, HttpServletRequest request) {
        if (searchEventParams.getRangeEnd() != null && searchEventParams.getRangeStart() != null) {
            if (searchEventParams.getRangeEnd().isBefore(searchEventParams.getRangeStart())) {
                throw new IncorrectParametersException("Ошибка параметров запроса.", "Дата окончания не может быть раньше даты начала");
            }
        }

        addStatsClient(request);

        Pageable pageable = createPageableWithSort(searchEventParams);

        Specification<Event> specification = buildEventSpecification(searchEventParams);
        List<Event> resultEvents = eventRepository.findAll(specification, pageable).getContent();

        Map<Long, Long> viewStatsMap = getViewsAllEvents(resultEvents);

        Map<Long, List<Request>> confirmedRequestsMap = getConfirmedRequestsCount(resultEvents);

        List<EventShortDto> result = resultEvents
                .stream().map(event -> {
                    EventShortDto dto = EventMapper.mapToShort(event);
                    dto.setViews(viewStatsMap.getOrDefault(event.getId(), 0L));
                    List<Request> eventRequests = confirmedRequestsMap.get(event.getId());
                    dto.setConfirmedRequests(eventRequests != null ? (long) eventRequests.size() : 0L);
                    return dto;
                }).collect(Collectors.toList());

        if ("VIEWS".equalsIgnoreCase(searchEventParams.getSort())) {
            result.sort((e1, e2) -> Long.compare(e2.getViews(), e1.getViews())); // DESC порядок
        }

        return result;
    }

    private Pageable createPageableWithSort(SearchEventParams searchEventParams) {
        int from = searchEventParams.getFrom() != null ? searchEventParams.getFrom() : 0;
        int size = searchEventParams.getSize() != null ? searchEventParams.getSize() : 10;

        if (searchEventParams.getSort() != null && "EVENT_DATE".equalsIgnoreCase(searchEventParams.getSort())) {
            Sort sort = Sort.by(Sort.Direction.ASC, "eventDate");
            return PageRequest.of(from / size, size, sort);
        }

        return PageRequest.of(from / size, size);
    }

    private Specification<Event> buildEventSpecification(SearchEventParams searchEventParams) {
        Specification<Event> specification = Specification.where(null);
        LocalDateTime now = LocalDateTime.now();

        if (searchEventParams.getText() != null && !searchEventParams.getText().isBlank()) {
            String searchText = searchEventParams.getText().toLowerCase();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + searchText + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + searchText + "%")
                    ));
        }

        if (searchEventParams.getCategories() != null && !searchEventParams.getCategories().isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(searchEventParams.getCategories()));
        }

        if (searchEventParams.getPaid() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("paid"), searchEventParams.getPaid()));
        }

        LocalDateTime startDateTime = Objects.requireNonNullElse(searchEventParams.getRangeStart(), now);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));

        if (searchEventParams.getRangeEnd() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("eventDate"), searchEventParams.getRangeEnd()));
        } else {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThan(root.get("eventDate"), now));
        }

        if (searchEventParams.getOnlyAvailable() != null && searchEventParams.getOnlyAvailable()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.equal(root.get("participantLimit"), 0),
                            criteriaBuilder.greaterThan(
                                    root.get("participantLimit"),
                                    root.get("confirmedRequests")
                            )
                    ));
        }

        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("eventStatus"), EventStatus.PUBLISHED));

        return specification;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = checkEvent(eventId);
        if (!event.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new NotFoundException("Событие не опубликовано.", "Событие с id = " + eventId + " не опубликовано");
        }
        addStatsClient(request);
        EventFullDto eventFullDto = EventMapper.mapToFull(event);
        Map<Long, Long> viewStatsMap = getViewsAllEvents(List.of(event));
        Long views = viewStatsMap.getOrDefault(event.getId(), 0L);
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    private void addStatsClient(HttpServletRequest request) {
        statsClient.saveStats(request);
    }

    private Map<Long, Long> getViewsAllEvents(List<Event> events) {
        List<String> uris = events.stream()
                .map(event -> String.format("/events/%s", event.getId()))
                .collect(Collectors.toList());

        List<LocalDateTime> startDates = events.stream()
                .map(Event::getCreatedDate)
                .toList();
        LocalDateTime earliestDate = startDates.stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);
        Map<Long, Long> viewStatsMap = new HashMap<>();
        if (earliestDate != null) {
            ResponseEntity<Object> response = statsClient.getStats(earliestDate, LocalDateTime.now().plusSeconds(1),
                    uris, true);
            List<ViewStats> viewStatsList;
            try {
                viewStatsList = viewStatsMapper.convertValue(response.getBody(), new TypeReference<>() {
                });
            } catch (IllegalArgumentException e) {
                if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    throw new IncorrectParametersException("Ошибка параметров запроса.", "Некорректный статус - " + response.getStatusCode());
                }
                return Collections.emptyMap();
            }

            viewStatsMap = viewStatsList.stream()
                    .filter(statsDto -> statsDto.getUri().startsWith("/events/"))
                    .collect(Collectors.toMap(
                            statsDto -> Long.parseLong(statsDto.getUri().substring("/events/".length())),
                            ViewStats::getHits
                    ));
        }
        return viewStatsMap;
    }

    private UpdatedStatusDto updatedStatusConfirmed(Event event, UpdatedStatusDto updatedStatus,
                                                    RequestStatus status, int confirmedRequestsCount) {
        int freeRequest = event.getParticipantLimit() - confirmedRequestsCount;
        List<Long> ids = updatedStatus.getIdsFromUpdateStatus();
        List<Long> processedIds = new ArrayList<>();
        List<Request> requestListLoaded = checkRequestOrEventList(event.getId(), ids);
        List<Request> requestList = new ArrayList<>();

        for (Request request : requestListLoaded) {
            if (freeRequest == 0) {
                break;
            }

            request.setStatus(status);
            requestList.add(request);

            processedIds.add(request.getId());
            freeRequest--;
        }

        requestRepository.saveAll(requestList);
        updatedStatus.setProcessedIds(processedIds);
        return updatedStatus;
    }

    private Event universalUpdate(Event oldEvent, UpdateEventRequest updateEvent) {
        if (updateEvent == null) return oldEvent;

        EventUpdateMapper.updateEventFromRequest(oldEvent, updateEvent);

        if (updateEvent.getCategory() != null) {
            Category category = checkCategory(updateEvent.getCategory());
            oldEvent.setCategory(category);
        }

        return oldEvent;
    }

    private List<Request> rejectRequest(List<Long> ids, Long eventId) {
        List<Request> rejectedRequests = new ArrayList<>();
        List<Request> requestList = new ArrayList<>();
        List<Request> requestListLoaded = checkRequestOrEventList(eventId, ids);

        for (Request request : requestListLoaded) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                break;
            }
            request.setStatus(RequestStatus.REJECTED);
            requestList.add(request);
            rejectedRequests.add(request);
        }
        requestRepository.saveAll(requestList);
        return rejectedRequests;
    }

    private Map<Long, List<Request>> getConfirmedRequestsCount(List<Event> events) {
        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(events
                .stream().map(Event::getId).collect(Collectors.toList()), RequestStatus.CONFIRMED);
        return requests.stream().collect(Collectors.groupingBy(r -> r.getEvent().getId()));
    }

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Евент не найден", "События с id = " + eventId + " не найдены."));
    }

    private User checkUser(Long userId) {
        log.info("Проверяем - user {}", userId);
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь не найден.", "Пользователя с id = " + userId + " не существует."));
    }

    private List<Request> checkRequestOrEventList(Long eventId, List<Long> requestId) {
        log.info("Проверяем - request {} и евент {}", requestId, eventId);
        return requestRepository.findByEventIdAndIdIn(eventId, requestId).orElseThrow(
                () -> new NotFoundException(" Запрос или евент не существуют.", "Запроса с id = " + requestId + " или события с id = "
                        + eventId + "не существуют"));
    }

    private Category checkCategory(Long catId) {
        log.info("Проверяем - category {} ", catId);
        return categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundException("Категория не найдена", "Категории с id = " + catId + " не существует"));
    }

    private Event checkEvenByInitiatorAndEventId(Long userId, Long eventId) {
        log.info("Проверяем - event {} пользователя user {}", eventId, userId);
        return eventRepository.findByInitiatorIdAndId(userId, eventId).orElseThrow(
                () -> new NotFoundException("Евент с таким пользователем не найден", "События с id = " + eventId + "и с пользователем с id = " + userId +
                        " не существует"));
    }

    private void checkDateAndTime(LocalDateTime now, LocalDateTime dateTime) {
        log.info("Проверяем - time-now {} dateTime {}", now, dateTime);
        if (dateTime.isBefore(now.plusHours(2))) {
            throw new CorrelationException("Ошибка данных полей времени.", "Поле должно содержать дату, " +
                    "которая еще не наступила и не ранее двух часов вперед.");
        }
    }
}