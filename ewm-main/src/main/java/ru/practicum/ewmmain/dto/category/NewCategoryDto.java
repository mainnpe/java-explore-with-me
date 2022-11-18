package ru.practicum.ewmmain.dto.category;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class NewCategoryDto {
    @NotBlank(message = "Category name cannot be blank")
    private String name;
}
