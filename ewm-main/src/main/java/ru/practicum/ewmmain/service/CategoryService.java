package ru.practicum.ewmmain.service;

import ru.practicum.ewmmain.dto.category.CategoryDto;
import ru.practicum.ewmmain.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(NewCategoryDto dto);

    CategoryDto update(CategoryDto dto);

    void delete(long id);

    List<CategoryDto> findAll(int from, int size);

    CategoryDto findById(long id);

}
