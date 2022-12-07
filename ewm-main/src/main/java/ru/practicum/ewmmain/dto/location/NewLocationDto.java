package ru.practicum.ewmmain.dto.location;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class NewLocationDto {
    @NotBlank(message = "Location name cannot be blank")
    private String name;
    @NotNull(message = "Location latitude cannot be null")
    private Double lat;
    @NotNull(message = "Location longitude cannot be null")
    private Double lon;
    @NotNull(message = "City field cannot be null")
    private Long city;
}
