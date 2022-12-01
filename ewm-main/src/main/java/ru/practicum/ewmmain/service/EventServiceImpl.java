package ru.practicum.ewmmain.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewmmain.client.StatsServiceClient;
import ru.practicum.ewmmain.dto.event.*;
import ru.practicum.ewmmain.dto.stats.EndpointHit;
import ru.practicum.ewmmain.dto.stats.ViewStatsDto;
import ru.practicum.ewmmain.exception.EntityNotFoundException;
import ru.practicum.ewmmain.exception.ForbiddenOperationException;
import ru.practicum.ewmmain.model.Category;
import ru.practicum.ewmmain.model.event.Event;
import ru.practicum.ewmmain.model.event.EventStatus;
import ru.practicum.ewmmain.model.event.QEvent;
import ru.practicum.ewmmain.storage.CategoryRepository;
import ru.practicum.ewmmain.storage.EventRepository;
import ru.practicum.ewmmain.storage.UserRepository;
import ru.practicum.ewmmain.utils.CustomPageRequest;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatsServiceClient statsServiceClient;

    public static final String APP_NAME = "ewm-main-service";

    @Override
    public EventFullDto create(NewEventDto newEventDto) {
        if (!userRepository.existsById(newEventDto.getUserId())) {
            log.warn("User with id = {} does not exists", newEventDto.getUserId());
            throw new EntityNotFoundException("User does not exists");
        }

        if (!categoryRepository.existsById(newEventDto.getCategory())) {
            log.warn("Category with id = {} does not exists", newEventDto.getCategory());
            throw new EntityNotFoundException("Category does not exists");
        }
        Event event = eventRepository.save(eventMapper.toEvent(newEventDto));
        log.info("Event with id = {} has been created", event.getId());

        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto publish(Long eventId) {
        LocalDateTime publishDate = LocalDateTime.now();
        LocalDateTime publishDateLimit = publishDate.plusHours(1L);

        if (!eventRepository.existsByIdAndStateAndEventDateGreaterThanEqual(eventId,
                EventStatus.PENDING, publishDateLimit)) {
            log.warn("Unable to publish event");
            throw new ForbiddenOperationException("Only pending events can be published");
        }

        int rows = eventRepository.updateEventStateAndPublishedDate(eventId,
                EventStatus.PUBLISHED, publishDate);
        log.info("{} event with id = {} was published", rows, eventId);
        Optional<Event> eventOpt = eventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            log.warn("Unable to find event with id = {} after update (status -> published)", eventId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable extract event data after update");
        }
        return eventMapper.toEventFullDto(eventOpt.get());
    }

    @Override
    @Transactional
    public EventFullDto cancel(Long eventId) {
        if (eventRepository.existsByIdAndState(eventId,
                EventStatus.PUBLISHED)) {
            log.warn("Unable to cancel event {}. The event has been already published", eventId);
            throw new ForbiddenOperationException("Published events cannot be rejected");
        }

        int rows = eventRepository.updateEventState(eventId,
                EventStatus.CANCELED);
        log.info("{} event with id = {} was rejected", rows, eventId);
        Optional<Event> eventOpt = eventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            log.warn("Unable to find event with id = {} after update (status -> canceled)", eventId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable extract event data after update");
        }
        return eventMapper.toEventFullDto(eventOpt.get());
    }

    @Override
    @Transactional
    public EventFullDto cancel(Long eventId, Long userId) {
        if (!eventRepository.existsByIdAndUser_Id(eventId, userId)) {
            log.warn("Unable to cancel event {}. User {} can cancel only his events",
                    eventId, userId);
            throw new ForbiddenOperationException("User can cancel only his events");
        }
        if (!eventRepository.existsByIdAndState(eventId, EventStatus.PENDING)) {
            log.warn("Unable to cancel event {}. Only pending events can be cancelled", eventId);
            throw new ForbiddenOperationException("Only pending events can be cancelled");
        }

        int rows = eventRepository.updateEventState(eventId,
                EventStatus.CANCELED);
        log.info("{} event with id = {} was canceled", rows, eventId);
        Optional<Event> eventOpt = eventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            log.warn("Unable to find event with id = {} after update (status -> canceled)", eventId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable extract event data after update");
        }
        return eventMapper.toEventFullDto(eventOpt.get());
    }

    @Override
    public EventFullDto find(Long eventId, Long userId) {
        if (!eventRepository.existsByIdAndUser_Id(eventId, userId)) {
            log.warn("Event with id = {} and user id = {} does not exists", eventId, userId);
            throw new EntityNotFoundException("Requested event created buy " +
                    "current user does not exists");
        }
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            log.warn("Unable to find event with id = {} during get request", eventId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to find event");
        }
        return eventMapper.toEventFullDto(eventOpt.get());
    }

    @Override
    public EventFullDto find(Long eventId, String ip, String uri) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty() ||
                !Objects.equals(eventOpt.get().getState(), EventStatus.PUBLISHED)) {
            log.warn("Event with id = {} does not exists or not published", eventId);
            throw new EntityNotFoundException("Event not found");
        }
        saveEndpointHit(ip, uri);
        List<ViewStatsDto> views = getStats(uri);
        return eventMapper.toEventFullDto(eventOpt.get(), views);
    }

    @Override
    public List<EventShortDto> findAll(Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            log.warn("User with id = {} does not exists", userId);
            throw new EntityNotFoundException("User does not exists");
        }
        Pageable page = CustomPageRequest.of(from, size);
        List<Event> events = eventRepository.findAll(page).getContent();
        log.info("For conditions from = {}, size = {}: {} events has been founded",
                from, size, events.size());
        return eventMapper.toEventShortDtos(events);
    }

    @Override
    public EventFullDto modify(AdminUpdateEventDto updateEventDto) {
        Long eventId = updateEventDto.getId();
        Optional<Category> categoryOpt = categoryRepository.findById(
                updateEventDto.getCategory());
        if (categoryOpt.isEmpty()) {
            log.warn("Category with id = {} does not exists", updateEventDto.getCategory());
            throw new EntityNotFoundException("Category does not exists");
        }
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            log.warn("Event with id = {} does not exists", eventId);
            throw new EntityNotFoundException("Event does not exists");
        }

        Event event = eventRepository.save(eventMapper.updateEvent(
                updateEventDto, eventOpt.get(), categoryOpt.get()));
        log.info("Event with id = {} has been updated", eventId);

        return eventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto modify(UpdateEventDto updateEventDto, Long userId) {
        Long eventId = updateEventDto.getId();
        Optional<Event> eventOpt = eventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            log.warn("Event with id = {} does not exists", eventId);
            throw new EntityNotFoundException("Event does not exists");
        }

        if (!eventOpt.get().getUser().getId().equals(userId)) {
            log.warn("Unable to modify. User can modify only his events");
            throw new ForbiddenOperationException("User can modify only his events");
        }

        if (!(eventRepository.existsByIdAndState(eventId, EventStatus.CANCELED) ||
                eventRepository.existsByIdAndState(eventId, EventStatus.PENDING))) {
            log.warn("Unable to modify. The event has been already published");
            throw new ForbiddenOperationException("Published events cannot be modified");
        }

        Optional<Category> categoryOpt = categoryRepository.findById(
                updateEventDto.getCategory());
        if (categoryOpt.isEmpty()) {
            log.warn("Category with id = {} does not exists", updateEventDto.getCategory());
            throw new EntityNotFoundException("Category does not exists");
        }

        Event event = eventRepository.save(eventMapper.updateEvent(
                updateEventDto, eventOpt.get(), categoryOpt.get()));
        log.info("Event with id = {} has been updated", eventId);

        return eventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> findAll(Optional<String> text, List<Long> categories, Optional<Boolean> paid, boolean onlyAvailable,
                                       Optional<LocalDateTime> rangeStart, Optional<LocalDateTime> rangeEnd,
                                       EventSortState sort, int from, int size, String ip, String uri) {

        Pageable page = (EventSortState.EVENT_DATE.equals(sort)) ?
                CustomPageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "eventDate")) :
                CustomPageRequest.of(from, size);
        BooleanExpression resultPredicate = preparePredicateForFindAllPublic(text, categories,
                paid, onlyAvailable, rangeStart, rangeEnd);

        List<Event> events = eventRepository.findAll(resultPredicate, page).getContent();
        log.info("For conditions search text = {}, categories = {}, paid = {}, onlyAvailable = {}, " +
                        "rangeStart = {}, rangeEnd = {}, sort = {}, from = {}, size = {}: " +
                        "{} events has been founded", text, categories, paid, onlyAvailable,
                rangeStart, rangeEnd, sort, from, size, events.size());
        saveEndpointHit(ip, uri);
        List<ViewStatsDto> views = getStats();
        return eventMapper.toEventShortDtos(events, views);
    }

    @Override
    public List<EventFullDto> findAll(List<Long> users, List<EventStatus> states,
                                      List<Long> categories, Optional<LocalDateTime> rangeStart,
                                      Optional<LocalDateTime> rangeEnd, int from, int size) {
        Pageable page = CustomPageRequest.of(from, size);
        BooleanExpression resultPredicate = preparePredicateForFindAllAdmin(users, states, categories,
                rangeStart, rangeEnd);

        List<Event> events = eventRepository.findAll(resultPredicate, page).getContent();
        log.info("For conditions users = {}, states = {}, categories = {}, " +
                        "rangeStart = {}, rangeEnd = {}, from = {}, size = {}: " +
                        "{} events has been founded", users, states, categories,
                rangeStart, rangeEnd, from, size, events.size());
        return eventMapper.toEventFullDtos(events);
    }

    public BooleanExpression preparePredicateForFindAllPublic(Optional<String> text, List<Long> categories,
                                                              Optional<Boolean> paid, boolean onlyAvailable,
                                                              Optional<LocalDateTime> rangeStart,
                                                              Optional<LocalDateTime> rangeEnd) {
        QEvent event = QEvent.event;
        LocalDateTime now = LocalDateTime.now();

        BooleanExpression onlyPublished = event.state.eq(EventStatus.PUBLISHED);


        BooleanExpression byAvailability = event.confirmedRequests.lt(event.participantLimit);

        BooleanExpression resultPredicate = onlyPublished;


        if (!CollectionUtils.isEmpty(categories)) {
            BooleanExpression inCategories = event.category.id.in(categories);
            resultPredicate.and(inCategories);
        }

        if (paid.isPresent()) {
            resultPredicate.and(event.paid.eq(paid.get()));
        }

        if (rangeStart.isEmpty() && rangeEnd.isEmpty()) {
            resultPredicate.and(event.eventDate.after(now));
        } else {
            resultPredicate.and(event.eventDate
                    .between(rangeStart.get(), rangeEnd.get()));
        }

        if (onlyAvailable) {
            resultPredicate.and(byAvailability);
        }

        if (text.isPresent()) {
            resultPredicate.and(event.annotation.likeIgnoreCase(text.get())
                    .or(event.description.likeIgnoreCase(text.get())));
        }
        return resultPredicate;
    }


    public BooleanExpression preparePredicateForFindAllAdmin(List<Long> users, List<EventStatus> states,
                                                             List<Long> categories,
                                                             Optional<LocalDateTime> rangeStart,
                                                             Optional<LocalDateTime> rangeEnd) {
        QEvent event = QEvent.event;
        LocalDateTime now = LocalDateTime.now();
        BooleanExpression resultPredicate = null;
        BooleanExpression byEventDate;

        if (!CollectionUtils.isEmpty(users)) {
            resultPredicate = event.user.id.in(users);
        }

        if (!CollectionUtils.isEmpty(categories)) {
            BooleanExpression inCategories = event.category.id.in(categories);
            resultPredicate = (resultPredicate == null) ?
                    inCategories : resultPredicate.and(inCategories);
        }

        if (!CollectionUtils.isEmpty(states)) {
            BooleanExpression inStates = event.state.in(states);
            resultPredicate = (resultPredicate == null) ?
                    inStates : resultPredicate.and(inStates);
        }

        if (rangeStart.isEmpty() && rangeEnd.isEmpty()) {
            byEventDate = event.eventDate.after(now);
        } else {
            byEventDate = event.eventDate
                    .between(rangeStart.get(), rangeEnd.get());
        }
        resultPredicate = (resultPredicate == null) ?
                byEventDate : resultPredicate.and(byEventDate);

        return resultPredicate;
    }

    @Override
    public void saveEndpointHit(String ip, String uri) {
        ResponseEntity<Object> response = statsServiceClient.saveHit(
                new EndpointHit(APP_NAME, uri, ip, LocalDateTime.now()));

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error during saving stats");
        }
    }

    @Override
    public List<ViewStatsDto> getStats(String uri) {
        List<ViewStatsDto> dtos = statsServiceClient.getStats(uri);
        if (uri == null) {
            log.info("Amount of founded results: {}", dtos.size());
        } else {
            log.info("For uri - {} amount of views: {}", uri, dtos.size());
        }
        return dtos;
    }

    @Override
    public List<ViewStatsDto> getStats() {
        return getStats(null);
    }
}
