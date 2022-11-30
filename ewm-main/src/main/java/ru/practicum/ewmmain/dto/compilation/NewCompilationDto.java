package ru.practicum.ewmmain.dto.compilation;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Getter
@Setter
public class NewCompilationDto {

    @NotBlank(message = "Compilation name cannot be blank")
    private String title;

    @NotNull(message = "Pinned property cannot be null")
    private Boolean pinned;

    private Collection<Long> events;
}
