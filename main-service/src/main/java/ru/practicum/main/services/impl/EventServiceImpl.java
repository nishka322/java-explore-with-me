package ru.practicum.main.services.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.main.constants.Pattern;
import ru.practicum.main.dto.event.*;
import ru.practicum.main.enums.*;
import ru.practicum.main.exceptions.*;
import ru.practicum.main.mappers.EventMapper;
import ru.practicum.main.models.Category;
import ru.practicum.main.models.Event;
import ru.practicum.main.models.User;
import ru.practicum.main.repositories.CategoryRepository;
import ru.practicum.main.repositories.EventRepository;
import ru.practicum.main.repositories.RequestRepository;
import ru.practicum.main.repositories.UserRepository;
import ru.practicum.main.services.EventService;

import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final EntityManager entityManager;
    private final String datePattern = Pattern.DATE;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern);

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new CategoryNotExistException(""));
        LocalDateTime eventDate = newEventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new WrongTimeException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value:" + eventDate);
        }
        Event event = eventMapper.toEventModel(newEventDto);
        event.setCategory(category);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException(String.format("Can't create event, the user with id = %s doesn't exist", userId)));
        event.setInitiator(user);
        return setConfirmedRequest(event);
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return eventMapper.toEventShortDtoList(eventRepository.findAllByInitiatorId(userId, page).toList());
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminDto updateEventAdminDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotExistException(String.format("Can't update event with id = %s", eventId)));
        if (updateEventAdminDto == null) {
            return eventMapper.toEventFullDto(event);
        }

        if (updateEventAdminDto.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminDto.getAnnotation());
        }
        if (updateEventAdminDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminDto.getCategory()).orElseThrow(() -> new CategoryNotExistException(""));
            event.setCategory(category);
        }
        if (updateEventAdminDto.getDescription() != null) {
            event.setDescription(updateEventAdminDto.getDescription());
        }
        if (updateEventAdminDto.getLocation() != null) {
            event.setLocation(updateEventAdminDto.getLocation());
        }
        if (updateEventAdminDto.getPaid() != null) {
            event.setPaid(updateEventAdminDto.getPaid());
        }
        if (updateEventAdminDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminDto.getParticipantLimit().intValue());
        }
        if (updateEventAdminDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminDto.getRequestModeration());
        }
        if (updateEventAdminDto.getTitle() != null) {
            event.setTitle(updateEventAdminDto.getTitle());
        }
        if (updateEventAdminDto.getStateAction() != null) {
            if (updateEventAdminDto.getStateAction().equals(StateActionForAdmin.PUBLISH_EVENT)) {
                if (event.getPublishedOn() != null) {
                    throw new AlreadyPublishedException("Event already published");
                }
                if (event.getState() == null || event.getState().equals(EventState.CANCELED)) {
                    throw new EventAlreadyCanceledException("Event already canceled");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateEventAdminDto.getStateAction().equals(StateActionForAdmin.REJECT_EVENT)) {
                if (event.getPublishedOn() != null) {
                    throw new AlreadyPublishedException("Event already published");
                }
                event.setState(EventState.CANCELED);
            }
        }
        if (updateEventAdminDto.getEventDate() != null) {
            LocalDateTime eventDateTime = updateEventAdminDto.getEventDate();
            if (eventDateTime.isBefore(LocalDateTime.now())) {
                throw new WrongTimeException("The start date of the event to be modified is less than one hour from the publication date.");
            }

            event.setEventDate(updateEventAdminDto.getEventDate());
        }
        return setConfirmedRequest(event);
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserDto updateEventUserDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotExistException(""));

        if (event.getPublishedOn() != null) {
            throw new AlreadyPublishedException("Event already published");
        }

        if (updateEventUserDto == null) {
            return eventMapper.toEventFullDto(event);
        }

        if (updateEventUserDto.getAnnotation() != null) {
            event.setAnnotation(updateEventUserDto.getAnnotation());
        }
        if (updateEventUserDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserDto.getCategory()).orElseThrow(() -> new CategoryNotExistException(""));
            event.setCategory(category);
        }
        if (updateEventUserDto.getDescription() != null) {
            event.setDescription(updateEventUserDto.getDescription());
        }
        if (updateEventUserDto.getEventDate() != null) {
            LocalDateTime eventDateTime = updateEventUserDto.getEventDate();
            if (eventDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new WrongTimeException("The start date of the event to be modified is less than one hour from the publication date.");
            }
            event.setEventDate(updateEventUserDto.getEventDate());
        }
        if (updateEventUserDto.getLocation() != null) {
            event.setLocation(updateEventUserDto.getLocation());
        }
        if (updateEventUserDto.getPaid() != null) {
            event.setPaid(updateEventUserDto.getPaid());
        }
        if (updateEventUserDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserDto.getParticipantLimit().intValue());
        }
        if (updateEventUserDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserDto.getRequestModeration());
        }
        if (updateEventUserDto.getTitle() != null) {
            event.setTitle(updateEventUserDto.getTitle());
        }

        if (updateEventUserDto.getStateAction() != null) {
            if (updateEventUserDto.getStateAction().equals(StateActionForUser.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }
        return setConfirmedRequest(event);
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() -> new EventNotExistException(""));
        return setConfirmedRequest(event);
    }

    @Override
    public List<EventFullDto> getEventsWithParamsByAdmin(List<Long> users, EventState states, List<Long> categoriesId,
                                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                         Integer from, Integer size) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (categoriesId != null && !categoriesId.isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(categoriesId);
            if (!categories.isEmpty()) {
                criteria = builder.and(criteria, root.get("category").in(categories));
            }
        }

        if (users != null && !users.isEmpty()) {
            List<User> userEntities = userRepository.findAllById(users);
            if (!userEntities.isEmpty()) {
                criteria = builder.and(criteria, root.get("initiator").in(userEntities));
            }
        }

        if (states != null) {
            criteria = builder.and(criteria, root.get("state").in(states));
        }

        if (rangeStart != null) {
            criteria = builder.and(criteria, builder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        if (rangeEnd != null) {
            criteria = builder.and(criteria, builder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        query.select(root)
                .where(criteria)
                .orderBy(builder.desc(root.get("createdOn")));

        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        if (events.isEmpty()) return List.of();

        List<EventFullDto> dtos = eventMapper.toEventFullDtoList(events);
        Map<Long, Long> confirmedRequests = getConfirmedRequestsList(events);
        for (EventFullDto dto : dtos) {
            dto.setConfirmedRequests(confirmedRequests.getOrDefault(dto.getId(), 0L));
        }
        setView(dtos);
        return dtos;
    }


    @Override
    public List<EventFullDto> getEventsWithParamsByUser(
            String text,
            List<Long> users,
            List<Long> categories,
            Boolean paid,
            String rangeStart,
            String rangeEnd,
            Boolean onlyAvailable,
            SortValue sort,
            Integer from,
            Integer size,
            String ip,
            String uri,
            List<String> states) {

        LocalDateTime start = null;
        LocalDateTime end = null;
        try {
            if (rangeStart != null) start = LocalDateTime.parse(rangeStart, dateFormatter);
            if (rangeEnd != null) end = LocalDateTime.parse(rangeEnd, dateFormatter);
        } catch (DateTimeParseException e) {
            log.debug("Неверный формат даты: {}", e.getMessage());
        }

        checkDateTime(start, end);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> root = cq.from(Event.class);

        Predicate predicate = cb.conjunction();

        if (text != null && !text.isBlank()) {
            Predicate annotationContain = cb.like(cb.lower(root.get("annotation")), "%" + text.toLowerCase() + "%");
            Predicate descriptionContain = cb.like(cb.lower(root.get("description")), "%" + text.toLowerCase() + "%");
            predicate = cb.and(predicate, cb.or(annotationContain, descriptionContain));
        }

        if (users != null && !users.isEmpty()) {
            predicate = cb.and(predicate, root.get("initiator").get("id").in(users));
        }

        if (categories != null && !categories.isEmpty()) {
            List<Category> categoryEntities = categoryRepository.findAllById(categories);
            if (!categoryEntities.isEmpty()) {
                predicate = cb.and(predicate, root.get("category").in(categoryEntities));
            }
        }

        if (paid != null) {
            predicate = cb.and(predicate, paid ? cb.isTrue(root.get("paid")) : cb.isFalse(root.get("paid")));
        }

        if (start != null) predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("eventDate"), start));
        if (end != null) predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("eventDate"), end));

        if (states != null && !states.isEmpty()) {
            CriteriaBuilder.In<String> inStates = cb.in(root.get("state"));
            for (String s : states) inStates.value(s);
            predicate = cb.and(predicate, inStates);
        }

        cq.select(root).where(predicate);

        if (sort != null) {
            if (sort == SortValue.EVENT_DATE) cq.orderBy(cb.asc(root.get("eventDate")));
            else cq.orderBy(cb.desc(root.get("views")));
        } else {
            cq.orderBy(cb.asc(root.get("eventDate")));
        }

        List<Event> events = entityManager.createQuery(cq)
                .setFirstResult(from != null ? from : 0)
                .setMaxResults(size != null ? size : 10)
                .getResultList();

        if (events.isEmpty()) return new ArrayList<>();

        List<EventFullDto> dtos = eventMapper.toEventFullDtoList(events);

        Map<Long, Long> confirmedRequests = getConfirmedRequestsList(events);
        for (EventFullDto dto : dtos) {
            dto.setConfirmedRequests(confirmedRequests.getOrDefault(dto.getId(), 0L));
        }

        setView(dtos);

        if (Boolean.TRUE.equals(onlyAvailable)) {
            dtos = dtos.stream()
                    .filter(dto -> dto.getConfirmedRequests() < dto.getParticipantLimit())
                    .collect(Collectors.toList());
        }

        sendStat(events, ip, uri);

        return dtos;
    }

    @Override
    public EventFullDto getEvent(Long id, String ip, String uri) {
        Event event = eventRepository.findByIdAndPublishedOnIsNotNull(id)
                .orElseThrow(() -> new EventNotExistException(
                        String.format("Can't find event with id = %s event doesn't exist", id)));

        EventFullDto eventFullDto = setConfirmedRequest(event);

        sendStat(eventFullDto, ip, uri);

        Long views = setView(event);
        eventFullDto.setViews(views != null ? views + 1 : 1L);

        return eventFullDto;
    }

    public void sendStat(EventFullDto event, String ip, String uri) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = ip;
        String nameService = "main-service";

        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(now.format(dateFormatter));
        requestDto.setUri("/events");
        requestDto.setApp(nameService);
        requestDto.setIp(remoteAddr);
        statsClient.addStats(requestDto);
        sendStatForTheEvent(event.getId(), remoteAddr, now, nameService);
    }

    public void sendStat(List<Event> events, String ip, String uri) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = ip;
        String nameService = "main-service";

        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(now.format(dateFormatter));
        requestDto.setUri("/events");
        requestDto.setApp(nameService);
        requestDto.setIp(ip);
        statsClient.addStats(requestDto);
    }

    public void setView(List<EventFullDto> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        LocalDateTime start;
        try {
            start = LocalDateTime.parse(events.getFirst().getCreatedOn());
        } catch (Exception e) {
            start = LocalDateTime.now().minusYears(1);
        }

        List<String> uris = new ArrayList<>();
        Map<String, EventFullDto> eventsUri = new HashMap<>();

        for (EventFullDto event : events) {
            try {
                LocalDateTime createdOn = LocalDateTime.parse(event.getCreatedOn());
                if (createdOn.isBefore(start)) {
                    start = createdOn;
                }
            } catch (Exception e) {
                log.debug("Ошибка парсинга createdOn для события id={}: {}", event.getId(), e.getMessage());
            }

            String uri = "/events/" + event.getId();
            uris.add(uri);
            eventsUri.put(uri, event);
            event.setViews(0L);
        }

        String startTime = start.format(DateTimeFormatter.ofPattern(Pattern.DATE));
        String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(Pattern.DATE));
        List<ViewStatsDto> stats = getStats(startTime, endTime, uris);
        stats.forEach((stat) -> {
            EventFullDto dto = eventsUri.get(stat.getUri());
            if (dto != null) {
                dto.setViews(stat.getHits());
            }
        });
    }

    public Long setView(Event event) {
        if (event == null || event.getCreatedOn() == null) {
            return 0L;
        }

        String startTime = event.getCreatedOn().format(dateFormatter);
        String endTime = LocalDateTime.now().format(dateFormatter);
        List<String> uris = List.of("/events/" + event.getId());
        List<ViewStatsDto> stats = getStats(startTime, endTime, uris);
        if (stats.size() == 1) {
            return stats.getFirst().getHits();
        } else {
            return 0L;
        }
    }

    private void checkDateTime(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            start = LocalDateTime.now().minusYears(100);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }
        if (start.isAfter(end)) {
            throw new WrongTimeException("Некорректный запрос. Дата окончания события задана позже даты старта");
        }
    }

    private List<ViewStatsDto> getStats(String startTime, String endTime, List<String> uris) {
        return statsClient.getStats(startTime, endTime, uris, false);
    }

    private void sendStatForTheEvent(Long eventId, String remoteAddr, LocalDateTime now,
                                     String nameService) {
        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(now.format(dateFormatter));
        requestDto.setUri("/events/" + eventId);
        requestDto.setApp(nameService);
        requestDto.setIp(remoteAddr);
        statsClient.addStats(requestDto);
    }

    private EventFullDto setConfirmedRequest(Event event) {
        Integer confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(eventRepository.save(event));
        eventFullDto.setConfirmedRequests((long) confirmed);
        return eventFullDto;
    }

    private Map<Long, Long> getConfirmedRequestsList(List<Event> events) {
        List<Event> publishedEvents = getPublished(events);

        return requestRepository.findAllByEventInAndStatus(publishedEvents, RequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.groupingBy(eventRequest -> eventRequest.getEvent().getId(), Collectors.counting()));
    }

    private List<Event> getPublished(List<Event> events) {
        return events.stream()
                .filter(event -> event.getPublishedOn() != null)
                .collect(Collectors.toList());
    }
}