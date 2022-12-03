package ru.practicum.ewmmain.dto.category;

import org.springframework.stereotype.Component;
import ru.practicum.ewmmain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

    public Category toCategory(NewCategoryDto dto) {
        return new Category(dto.getName());
    }

    public Category toCategory(CategoryDto dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        return category;
    }

    public CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    public CategoryDto toCategoryDto(Optional<Category> category) {
        if (category.isPresent()) {
            return toCategoryDto(category.get());
        }
        return null;
    }

    public List<CategoryDto> toCategoryDtos(List<Category> categories) {
        return categories.stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
    }
}
