package ru.practicum.ewmmain.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class CategoryDto {
    @NotNull(message = "Category name cannot be null")
    @Min(value = 1, message = "Category name cannot be less than 1")
    private Long id;

    @NotBlank(message = "Category name cannot be blank")
    private String name;
}
