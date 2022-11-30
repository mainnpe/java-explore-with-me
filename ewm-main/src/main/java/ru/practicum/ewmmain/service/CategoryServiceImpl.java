package ru.practicum.ewmmain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewmmain.dto.category.CategoryDto;
import ru.practicum.ewmmain.dto.category.CategoryMapper;
import ru.practicum.ewmmain.dto.category.NewCategoryDto;
import ru.practicum.ewmmain.exception.EntityNotFoundException;
import ru.practicum.ewmmain.model.Category;
import ru.practicum.ewmmain.storage.CategoryRepository;
import ru.practicum.ewmmain.storage.EventRepository;
import ru.practicum.ewmmain.utils.CustomPageRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto create(NewCategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            log.warn("Category with name '{}' already exists", dto.getName());
        }
        Category category = categoryRepository.save(categoryMapper.toCategory(dto));
        log.info("Category with id = {} has been created", category.getId());

        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto update(CategoryDto dto) {
        if (categoryRepository.findById(dto.getId()).isEmpty()) {
            log.warn("Category with id '{}' not found", dto.getId());
            throw new EntityNotFoundException("Unable to find category for update");
        }
        if (categoryRepository.existsByName(dto.getName())) {
            log.warn("Category with name '{}' already exists", dto.getName());
        }
        Category category = categoryRepository.save(categoryMapper.toCategory(dto));
        log.info("Category with id = {} has been updated", category.getId());

        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public void delete(long id) {
        if (eventRepository.existsByCategory_Id(id)) {
            log.warn("Delete category operation is forbidden. " +
                    "At least one event with category_id = {} exist", id);
        }
        categoryRepository.deleteById(id);
        log.info("Category with id = {} has been deleted", id);
    }

    @Override
    public List<CategoryDto> findAll(int from, int size) {
        Pageable page = CustomPageRequest.of(from, size);
        List<Category> categories = categoryRepository.findAll(page).getContent();
        log.info("For conditions from = {}, size = {}: {} users has been founded",
                from, size, categories.size());
        return categoryMapper.toCategoryDtos(categories);
    }

    @Override
    public CategoryDto findById(long id) {
        if (!categoryRepository.existsById(id)) {
            log.warn("Category with id = {} does not exist", id);
            throw new EntityNotFoundException("Category not found");
        }
        return categoryMapper.toCategoryDto(
                categoryRepository.findById(id));
    }

}
