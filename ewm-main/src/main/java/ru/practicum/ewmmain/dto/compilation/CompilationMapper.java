package ru.practicum.ewmmain.dto.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewmmain.dto.event.EventMapper;
import ru.practicum.ewmmain.model.Compilation;
import ru.practicum.ewmmain.model.event.Event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {
    private final EventMapper eventMapper;

    public Compilation toCompilation(NewCompilationDto dto, Collection<Event> events) {
        Boolean pinned = dto.getPinned() != null && dto.getPinned();
        return new Compilation(
                dto.getTitle(),
                pinned,
                new HashSet<>(events)
        );
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        List<Event> events = new ArrayList<>(compilation.getEvents());
        return new CompilationDto(
                compilation.getId(),
                compilation.getTitle(),
                compilation.getPinned(),
                eventMapper.toEventShortDtos(events)
        );
    }

    public List<CompilationDto> toCompilationDtos(List<Compilation> compilations) {
        return compilations.stream()
                .map(this::toCompilationDto)
                .collect(Collectors.toList());
    }
}
