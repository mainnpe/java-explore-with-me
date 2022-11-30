package ru.practicum.ewmmain.storage;

import lombok.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmmain.model.User;

import java.util.Collection;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByIdIn(Collection<Long> id, Pageable pageable);

    boolean existsByEmail(@NonNull String email);

}
