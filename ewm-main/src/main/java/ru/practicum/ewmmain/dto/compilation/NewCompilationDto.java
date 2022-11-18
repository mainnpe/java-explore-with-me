package ru.practicum.ewmmain.dto.compilation;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Getter
@Setter
public class NewCompilationDto {

    @NotBlank(message = "Compilation name cannot be blank")
    private String title;

    @NotNull(message = "Pinned property cannot be null")
    private Boolean pinned;

    @UniqueElements(message = "Event ids in compilation must be unique")
    @NotEmpty(message = "Event list cannot be empty")
    private Collection<Integer> events;
}
