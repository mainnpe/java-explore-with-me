package ru.practicum.ewmmain.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewmmain.dto.event.EventShortDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CompilationDto {
    private Long id;

    private String title;

    private Boolean pinned;

    private List<EventShortDto> events;
}
