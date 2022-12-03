package ru.practicum.ewmmain.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewmmain.model.request.Request;
import ru.practicum.ewmmain.model.request.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    boolean existsByRequester_IdAndEvent_Id(Long requesterId, Long eventId);

    boolean existsByIdAndRequester_Id(Long id, Long requesterId);

    List<Request> findByRequester_Id(Long requesterId);

    List<Request> findByEvent_Id(Long eventId);

    @Modifying
    @Query("update Request r set r.status = :status where r.id = :id")
    int updateRequestStatus(Long id, RequestStatus status);

    @Modifying
    @Query("update Request r set r.status = 'CANCELED' where r.event.id = :eventId")
    int cancelAllRequestsByEvent(Long eventId);

}
