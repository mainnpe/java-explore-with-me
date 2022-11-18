package ru.practicum.ewmmain.dto.compilation;

import org.springframework.stereotype.Component;
import ru.practicum.ewmmain.model.Compilation;

import java.util.HashSet;

@Component
public class CompilationMapper {
    //eventRepository

    //eventMapper
    public Compilation toCompilation(NewCompilationDto dto) {
        return new Compilation(
                dto.getTitle(),
                dto.getPinned(),
                //TODO
                //add events instead of event_ids
                new HashSet<>()
        );
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        return new CompilationDto(
                compilation.getId(),
                compilation.getTitle(),
                compilation.getPinned(),
                //TODO
                //add eventShortDto
                new HashSet<>()
        );
    }
}
