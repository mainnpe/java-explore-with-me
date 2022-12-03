package ru.practicum.ewmmain.service;

import ru.practicum.ewmmain.dto.compilation.CompilationDto;
import ru.practicum.ewmmain.dto.compilation.NewCompilationDto;

import java.util.List;

public interface CompilationService {

    CompilationDto create(NewCompilationDto newCompilationDto);

    void delete(long id);

    void deleteEvent(long compId, long eventId);

    void addEvent(long compId, long eventId);

    void unpin(long compId);

    void pin(long compId);

    CompilationDto find(long id);

    List<CompilationDto> findAll(boolean pinned, int from, int size);

    List<CompilationDto> findAll(int from, int size);

}
