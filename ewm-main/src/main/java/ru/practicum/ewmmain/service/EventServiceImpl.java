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
import ru.practicum.ewmmain.model.Location;
import ru.practicum.ewmmain.model.event.Event;
import ru.practicum.ewmmain.model.event.EventStatus;
import ru.practicum.ewmmain.model.event.QEvent;
import ru.practicum.ewmmain.storage.CategoryRepository;
import ru.practicum.ewmmain.storage.EventRepository;
import ru.practicum.ewmmain.storage.LocationRepository;
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
    private final LocationRepository locationRepository;
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
        Location location = locationRepository.findById(newEventDto.getLocation())
                .orElseThrow(() -> {
                    log.warn("Location with id = {} does not exist", newEventDto.getLocation());
                    throw new EntityNotFoundException("Location does not exists");
                });
        Event event = eventRepository.save(eventMapper.toEvent(newEventDto, location));
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

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.warn("Unable to find event with id = {} after update (status -> published)", eventId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable extract event data after update");
        });

        return eventMapper.toEventFullDto(event);
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

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.warn("Unable to find event with id = {} after update (status -> canceled)", eventId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable extract event data after update");
        });
        return eventMapper.toEventFullDto(event);
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

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.warn("Unable to find event with id = {} after update (status -> canceled)", eventId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable extract event data after update");
        });
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto find(Long eventId, Long userId) {
        if (!eventRepository.existsByIdAndUser_Id(eventId, userId)) {
            log.warn("Event with id = {} and user id = {} does not exists", eventId, userId);
            throw new EntityNotFoundException("Requested event created buy " +
                    "current user does not exists");
        }

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.warn("Unable to find event with id = {} during get request", eventId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to find event");
        });
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto find(Long eventId, String ip, String uri) {

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.warn("Event with id = {} does not exist", eventId);
            throw new EntityNotFoundException("Event not found");
        });
        if (!Objects.equals(event.getState(), EventStatus.PUBLISHED)) {
            log.warn("Event with id = {} not published", eventId);
            throw new EntityNotFoundException("Event not found");
        }
        saveEndpointHit(ip, uri);
        List<ViewStatsDto> views = getStats(uri);
        return eventMapper.toEventFullDto(event, views);
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
        Category category = categoryRepository.findById(updateEventDto.getCategory())
                .orElseThrow(() -> {
                    log.warn("Category with id = {} does not exists", updateEventDto.getCategory());
                    throw new EntityNotFoundException("Category does not exists");
                });

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.warn("Event with id = {} does not exists", eventId);
            throw new EntityNotFoundException("Event does not exists");
        });

        Location location = locationRepository.findById(updateEventDto.getLocation())
                .orElseThrow(() -> {
                    log.warn("Location with id = {} does not exist", updateEventDto.getLocation());
                    throw new EntityNotFoundException("Location does not exist");
                });

        Event updatedEvent = eventRepository.save(eventMapper.updateEvent(
                updateEventDto, event, category, location));
        log.info("Event with id = {} has been updated", eventId);

        return eventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    public EventFullDto modify(UpdateEventDto updateEventDto, Long userId) {
        Long eventId = updateEventDto.getId();

        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.warn("Event with id = {} does not exists", eventId);
            throw new EntityNotFoundException("Event does not exists");
        });

        if (!event.getUser().getId().equals(userId)) {
            log.warn("Unable to modify. User can modify only his events");
            throw new ForbiddenOperationException("User can modify only his events");
        }

        if (!(eventRepository.existsByIdAndState(eventId, EventStatus.CANCELED) ||
                eventRepository.existsByIdAndState(eventId, EventStatus.PENDING))) {
            log.warn("Unable to modify. The event has been already published");
            throw new ForbiddenOperationException("Published events cannot be modified");
        }

        Category category = categoryRepository.findById(updateEventDto.getCategory())
                .orElseThrow(() -> {
                    log.warn("Category with id = {} does not exists", updateEventDto.getCategory());
                    throw new EntityNotFoundException("Category does not exists");
                });

        Event updatedEvent = eventRepository.save(eventMapper.updateEvent(
                updateEventDto, event, category));
        log.info("Event with id = {} has been updated", eventId);

        return eventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    public List<EventShortDto> findAll(Optional<String> text, List<Long> categories,
                                       Optional<Boolean> paid, boolean onlyAvailable,
                                       Optional<LocalDateTime> rangeStart, Optional<LocalDateTime> rangeEnd,
                                       EventSortState sort, List<Long> cities, List<Long> locations,
                                       Double lat, Double lon, Double r, int from, int size, String ip, String uri) {

        Pageable page = (EventSortState.EVENT_DATE.equals(sort)) ?
                CustomPageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "eventDate")) :
                CustomPageRequest.of(from, size);
        BooleanExpression resultPredicate = preparePredicateForFindAllPublic(text, categories,
                paid, onlyAvailable, rangeStart, rangeEnd, cities, locations, lat, lon, r);

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
                                                              Optional<LocalDateTime> rangeEnd, List<Long> cities,
                                                              List<Long> locations, Double lat, Double lon, Double r) {
        QEvent event = QEvent.event;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = rangeStart.orElse(now);
        LocalDateTime endDate = rangeEnd.orElse(null);

        BooleanExpression resultPredicate = event.state.eq(EventStatus.PUBLISHED);


        if (!CollectionUtils.isEmpty(categories)) {
            BooleanExpression inCategories = event.category.id.in(categories);
            resultPredicate = resultPredicate.and(inCategories);
        }

        if (endDate == null) {
            resultPredicate = resultPredicate.and(event.eventDate.after(now));
        } else {
            resultPredicate = resultPredicate.and(event.eventDate
                    .between(startDate, endDate));
        }

        if (onlyAvailable) {
            BooleanExpression byAvailability = event.confirmedRequests.lt(event.participantLimit)
                    .or(event.participantLimit.eq(0));
            resultPredicate = resultPredicate.and(byAvailability);
        }

        if (paid.isPresent()) {
            resultPredicate = resultPredicate.and(event.paid.eq(paid.get()));
        }

        if (text.isPresent()) {
            String textToFind = "%" + text.get() + "%";
            resultPredicate = resultPredicate.and(event.annotation.likeIgnoreCase(textToFind)
                    .or(event.description.likeIgnoreCase(textToFind)));
        }

        if (!CollectionUtils.isEmpty(cities)) {
            BooleanExpression inCities = event.location.city.id.in(cities);
            resultPredicate = resultPredicate.and(inCities);
        }

        if (!CollectionUtils.isEmpty(locations)) {
            BooleanExpression inLocations = event.location.id.in(locations);
            resultPredicate = resultPredicate.and(inLocations);
        }

        if (lat != null && lon != null && r != null) {
            List<Location> locationsNearBy = locationRepository.findNearby(lat, lon, r);
            resultPredicate = resultPredicate.and(event.location.in(locationsNearBy));
        }

        return resultPredicate;
    }


    public BooleanExpression preparePredicateForFindAllAdmin(List<Long> users, List<EventStatus> states,
                                                             List<Long> categories,
                                                             Optional<LocalDateTime> rangeStart,
                                                             Optional<LocalDateTime> rangeEnd) {
        QEvent event = QEvent.event;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = rangeStart.orElse(now);
        LocalDateTime endDate = rangeEnd.orElse(null);
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


        if (endDate == null) {
            byEventDate = event.eventDate.after(now);
        } else {
            byEventDate = event.eventDate
                    .between(startDate, endDate);
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
