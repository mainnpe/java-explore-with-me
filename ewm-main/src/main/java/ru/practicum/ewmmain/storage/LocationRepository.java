package ru.practicum.ewmmain.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewmmain.model.Location;

import java.util.Collection;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    @Query(value = "select l.* " +
            "from locations as l " +
            "where distance(l.latitude, l.longitude, :other_lat, :other_lon) <= :limit ;", nativeQuery = true)
    List<Location> findNearby(@Param("other_lat") double lat, @Param("other_lon") double lon,
                              @Param("limit") double r);

    boolean existsByCity_Id(Long cityId);

    List<Location> findByCity_IdIn(Collection<Long> cityIds, Pageable pageable);

}
