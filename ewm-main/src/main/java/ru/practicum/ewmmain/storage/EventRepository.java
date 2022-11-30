package ru.practicum.ewmmain.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewmmain.model.event.Event;
import ru.practicum.ewmmain.model.event.EventStatus;

import java.time.LocalDateTime;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    boolean existsByCategory_Id(Long category_id);

    @Modifying
    @Query("update Event e set e.state = :state where e.id = :id")
    int updateEventState(Long id, EventStatus state);

    @Modifying
    @Query("update Event e set e.state = :state, e.publishedOn = :publishedDate where e.id = :id")
    int updateEventStateAndPublishedDate(Long id, EventStatus state, LocalDateTime publishedDate);

    boolean existsByIdAndStateAndEventDateGreaterThanEqual(Long id, EventStatus state,
                                                           LocalDateTime publishTimeLimit);

    boolean existsByIdAndState(Long id, EventStatus state);

    boolean existsByIdAndUser_Id(Long id, Long user_id);

    @Modifying
    @Query("update Event e set e.confirmedRequests = e.confirmedRequests + 1 where e.id = :id")
    int increaseConfirmedRequestsByOne(Long id);

    @Modifying
    @Query("update Event e set e.confirmedRequests = e.confirmedRequests - 1 where e.id = :id")
    int decreaseConfirmedRequestsByOne(Long id);

}
