package ru.practicum.ewmmain.storage;

import lombok.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewmmain.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Modifying
    @Query("update Compilation c set c.pinned = :isPin where c.id = :comId")
    int updatePinnedState(Long comId, Boolean isPin);

    List<Compilation> findByPinned(@NonNull Boolean pinned, Pageable page);
}
