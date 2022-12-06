package ru.practicum.ewmmain.dto.city;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class NewCityDto {
    @NotBlank(message = "City name cannot be blank")
    private String name;
}
