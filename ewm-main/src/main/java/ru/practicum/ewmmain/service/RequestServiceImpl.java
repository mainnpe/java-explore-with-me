package ru.practicum.ewmmain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewmmain.dto.request.ParticipationRequestDto;
import ru.practicum.ewmmain.dto.request.RequestMapper;
import ru.practicum.ewmmain.exception.EntityNotFoundException;
import ru.practicum.ewmmain.exception.ForbiddenOperationException;
import ru.practicum.ewmmain.model.User;
import ru.practicum.ewmmain.model.event.Event;
import ru.practicum.ewmmain.model.event.EventStatus;
import ru.practicum.ewmmain.model.request.Request;
import ru.practicum.ewmmain.model.request.RequestStatus;
import ru.practicum.ewmmain.storage.EventRepository;
import ru.practicum.ewmmain.storage.RequestRepository;
import ru.practicum.ewmmain.storage.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestMapper requestMapper;
    @PersistenceContext
    private final EntityManager em;

    @Override
    public ParticipationRequestDto create(long userId, long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User with id = {} does not exist", userId);
            throw new EntityNotFoundException("User does not exist");
        });
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.warn("Event with id = {} does not exist", userId);
            throw new EntityNotFoundException("Event does not exist");
        });

        if (eventRepository.existsByIdAndUser_Id(eventId, userId)) {
            log.warn("Unable to add request to event {} by user {}. " +
                            "Users can add requests only for other's user events",
                    eventId, userId);
            throw new ForbiddenOperationException("Users can add requests only for other's user events");
        }
        if (!EventStatus.PUBLISHED.equals(event.getState())) {
            log.warn("Event {} is not published. User cannot participate in events " +
                    "that not published yet", eventId);
            throw new ForbiddenOperationException("User cannot participate in events that not published yet");
        }
        if (!event.getParticipantLimit().equals(0)
                && Objects.equals(event.getParticipantLimit(), event.getConfirmedRequests())) {
            log.warn("Unable to participate. Participant limit for event {} has been reached", eventId);
            throw new ForbiddenOperationException("Participant limit for this event has been reached");
        }
        if (requestRepository.existsByRequester_IdAndEvent_Id(userId, eventId)) {
            log.warn("User {} already sent his request for event {}", userId, eventId);
        }
        RequestStatus requestStatus = (event.getRequestModeration()) ?
                RequestStatus.PENDING : RequestStatus.CONFIRMED;

        return requestMapper.toRequestDto(
                requestRepository.save(new Request(user, event, requestStatus))
        );
    }


    @Override
    public List<ParticipationRequestDto> findAll(long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("User with id = {} does not exist", userId);
            throw new EntityNotFoundException("User does not exist");
        }
        return requestMapper.toRequestDtos(
                requestRepository.findByRequester_Id(userId)
        );
    }

    @Override
    public List<ParticipationRequestDto> findAll(long userId, long eventId) {
        if (!eventRepository.existsByIdAndUser_Id(eventId, userId)) {
            log.warn("Event with id = {} and user id = {} does not exists", eventId, userId);
            throw new EntityNotFoundException("Event created buy current user " +
                    "does not exists");
        }
        return requestMapper.toRequestDtos(
                requestRepository.findByEvent_Id(eventId)
        );
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(long userId, long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> {
            log.warn("Request with id = {} does not exist", requestId);
            throw new EntityNotFoundException("Request does not exist");
        });

        if (!requestRepository.existsByIdAndRequester_Id(requestId, userId)) {
            log.warn("Unable to cancel request {}. User {} can cancel only its requests",
                    requestId, userId);
            throw new ForbiddenOperationException("User can cancel only its requests");
        }

        updateRequestStatusAndNConfirmed(requestId, request.getEvent().getId(),
                RequestStatus.CANCELED);

        Request updatedRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Unable to find request with id = {} after update (status -> canceled)", requestId);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error during update of request status to cancel");
                });

        em.refresh(updatedRequest);
        return requestMapper.toRequestDto(updatedRequest);
    }


    @Override
    @Transactional
    public ParticipationRequestDto confirm(long userId, long eventId, long requestId) {
        checkConditionsForConfirmRequest(userId, eventId, requestId);
        updateRequestStatusAndNConfirmed(requestId, eventId, RequestStatus.CONFIRMED);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Unable to find request {} after update (status -> confirmed)",
                            requestId);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error during update of request status to confirmed");
                });

        em.refresh(request);
        return requestMapper.toRequestDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto reject(long userId, long eventId, long requestId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.warn("Unable to reject request {}. Event {} does not exist", requestId, eventId);
            throw new ForbiddenOperationException("Unable to reject. Event does not exist");
        });

        if (!event.getUser().getId().equals(userId)) {
            log.warn("Unable to reject request {}. User {} did not create this event", requestId, userId);
            throw new ForbiddenOperationException("Unable to reject. User did not create this event");
        }
        if (!requestRepository.existsById(requestId)) {
            log.warn("Request with id = {} does not exist", requestId);
            throw new EntityNotFoundException("Request does not exist");
        }

        updateRequestStatusAndNConfirmed(requestId, eventId, RequestStatus.REJECTED);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Unable to find request {} after update (status -> rejected)",
                            requestId);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error during update of request status to rejected");
                });

        em.refresh(request);
        return requestMapper.toRequestDto(request);
    }


    private void checkConditionsForConfirmRequest(long userId, long eventId, long requestId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.warn("Unable to confirm request {}. Event {} does not exist",
                    requestId, eventId);
            throw new ForbiddenOperationException("Unable to confirm. Event does not exist");
        });

        if (!event.getUser().getId().equals(userId)) {
            log.warn("Unable to confirm request {}. User {} did not create this event", requestId, eventId);
            throw new ForbiddenOperationException("Unable to confirm. User did not create this event");
        }
        if (!requestRepository.existsById(requestId)) {
            log.warn("Request with id = {} does not exist", requestId);
            throw new EntityNotFoundException("Request does not exist");
        }
        Integer participantLimit = event.getParticipantLimit();

        if (participantLimit.equals(0) || event.getRequestModeration().equals(false)) {
            log.warn("Unable to approve request {}. Event {} do not require moderation",
                    requestId, eventId);
            throw new ForbiddenOperationException("Unable to approve request. Event do not require moderation");
        }
        if (participantLimit.equals(event.getConfirmedRequests())) {
            log.warn("Unable to approve request {}. Participant limit of {} has been reached",
                    requestId, participantLimit);
            throw new ForbiddenOperationException("Unable to approve request. " +
                    "Participant limit has been reached");
        }
    }


    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateRequestStatusAndNConfirmed(long requestId, long eventId, RequestStatus status) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> {
            log.warn("Request with id = {} does not exist", requestId);
            throw new EntityNotFoundException("Request does not exist");
        });
        em.refresh(request);
        RequestStatus initialStatus = request.getStatus();
        int requestRows;
        int eventRows = 0;

        switch (status) {
            case CONFIRMED:
                requestRows = requestRepository.updateRequestStatus(requestId, status);
                eventRows = eventRepository.increaseConfirmedRequestsByOne(eventId);
                break;
            case REJECTED:
            case CANCELED:
                requestRows = requestRepository.updateRequestStatus(requestId, status);
                if (RequestStatus.CONFIRMED.equals(initialStatus)) {
                    eventRows = eventRepository.decreaseConfirmedRequestsByOne(eventId);
                }
                break;
            default:
                log.trace("No actions. Method updateRequestStatusAndNConfirmed " +
                        "was called with status = {}", status);
                return;
        }

        if (requestRows != 1 || (eventRows != 1 && RequestStatus.CONFIRMED.equals(initialStatus))) {
            log.warn("Error during update request status on {} or events. Updated rows: " +
                    "requestRows = {}, eventRows = {}", status, requestRows, eventRows);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error during update request status");
        }
        log.info("Request {} status was updated to {}", requestId, status);
        log.info("Number of confirmed requests for event {} was {} by 1",
                eventId, (RequestStatus.CONFIRMED.equals(status)) ? "increased" : "decreased");
    }


    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void cancelRedundantRequests(long eventId) {
        eventRepository.findById(eventId).ifPresent(event -> {
            log.trace("For event {} confirmed requests - {}, participant limit - {}",
                    event.getId(), event.getConfirmedRequests(), event.getParticipantLimit());
            if (event.getParticipantLimit().equals(event.getConfirmedRequests())) {
                int canceledRows = requestRepository.cancelAllRequestsByEvent(eventId);
                log.info("{} requests was canceled due to reaching limit of participants {} " +
                        "for event {}", canceledRows, event.getParticipantLimit(), eventId);
            }
        });
    }

}
