package ru.practicum.ewmmain.storage;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmmain.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(@NonNull String name);

}
