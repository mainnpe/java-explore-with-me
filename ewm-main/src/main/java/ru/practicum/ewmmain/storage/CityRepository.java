package ru.practicum.ewmmain.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewmmain.model.City;

public interface CityRepository extends JpaRepository<City, Long> {

}
