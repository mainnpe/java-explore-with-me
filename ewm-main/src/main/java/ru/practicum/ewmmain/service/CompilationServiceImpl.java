package ru.practicum.ewmmain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewmmain.dto.compilation.CompilationDto;
import ru.practicum.ewmmain.dto.compilation.CompilationMapper;
import ru.practicum.ewmmain.dto.compilation.NewCompilationDto;
import ru.practicum.ewmmain.exception.EntityNotFoundException;
import ru.practicum.ewmmain.exception.ForbiddenOperationException;
import ru.practicum.ewmmain.model.Compilation;
import ru.practicum.ewmmain.model.event.Event;
import ru.practicum.ewmmain.storage.CompilationRepository;
import ru.practicum.ewmmain.storage.EventRepository;
import ru.practicum.ewmmain.utils.CustomPageRequest;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        List<Event> events = newCompilationDto.getEvents().stream()
                .map(eventRepository::findById)
                .map(e -> e.orElseThrow(
                        () -> new EntityNotFoundException(String.format("Event %s does not exist", e)))
                ).collect(Collectors.toList());
        Compilation compilation = compilationRepository.save(
                compilationMapper.toCompilation(newCompilationDto, events));
        log.info("Compilation {} has been created", compilation.getId());
        return compilationMapper.toCompilationDto(compilation);
    }

    @Override
    public void delete(long id) {
        compilationRepository.deleteById(id);
        log.info("Compilation {} has been deleted", id);
    }


    @Override
    public void deleteEvent(long compId, long eventId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.warn("Compilation with id = {} does not exists", compId);
                    throw new EntityNotFoundException("Compilation does not exists");
                });
        Set<Event> events = compilation.getEvents();
        events.stream()
                .map(Event::getId)
                .filter(x -> x.equals(eventId))
                .findAny()
                .orElseThrow(() -> {
                    log.warn("Compilation {} does not contain event {}", compId, eventId);
                    throw new ForbiddenOperationException("Compilation does not contain this event");
                });
        Event eventToDelete = Event.builder().id(eventId).build();
        events.remove(eventToDelete);
        compilationRepository.save(compilation);
        log.info("Event {} has been removed from compilation {}", eventId, compId);
    }



    @Override
    public void addEvent(long compId, long eventId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.warn("Compilation with id = {} does not exists", compId);
                    throw new EntityNotFoundException("Compilation does not exists");
                });
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            log.warn("Event with id = {} does not exists", eventId);
            throw new EntityNotFoundException("Event does not exists");
        });
        compilation.getEvents().add(event);
        compilationRepository.save(compilation);
        log.info("Event {} has been added to compilation {}", eventId, compId);
    }

    @Override
    public void unpin(long compId) {
        if (!compilationRepository.existsById(compId)) {
            log.warn("Compilation with id = {} does not exists", compId);
            throw new EntityNotFoundException("Compilation does not exists");
        }

        int rows = compilationRepository.updatePinnedState(compId, false);

        if (rows != 1) {
            log.warn("Error during update pinned (-> false) state for compilation {}. " +
                    "Updated rows - {} ", compId, rows);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error during update pinned state");
        }
    }

    @Override
    public void pin(long compId) {
        if (!compilationRepository.existsById(compId)) {
            log.warn("Compilation with id = {} does not exists", compId);
            throw new EntityNotFoundException("Compilation does not exists");
        }

        int rows = compilationRepository.updatePinnedState(compId, true);

        if (rows != 1) {
            log.warn("Error during update pinned (-> true) state for compilation {}. " +
                    "Updated rows - {} ", compId, rows);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error during update pinned state");
        }
    }


    @Override
    public CompilationDto find(long id) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Compilation with id = {} does not exists", id);
                    throw new EntityNotFoundException("Compilation not found");
                });
        return compilationMapper.toCompilationDto(compilation);
    }
    @Override
    public List<CompilationDto> findAll(boolean pinned, int from, int size) {
        log.warn("Beginning of findAll method");
        Pageable page = CustomPageRequest.of(from, size);

        List<Compilation> compilations = compilationRepository.findByPinned(pinned, page);
        log.info("For conditions pinned = {}, from = {}, size = {}: {} compilations has been found",
                pinned, from, size, compilations.size());
        log.warn("Calling mapper");
        return compilationMapper.toCompilationDtos(compilations);
    }

    @Override
    public List<CompilationDto> findAll(int from, int size) {
        log.warn("Beginning of findAll method");
        Pageable page = CustomPageRequest.of(from, size);
        List<Compilation> compilations = compilationRepository.findAll(page).getContent();
        log.info("For conditions from = {}, size = {}: {} compilations has been found",
                from, size, compilations.size());
        log.warn("Calling mapper");
        return compilationMapper.toCompilationDtos(compilations);
    }
}
