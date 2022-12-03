package ru.practicum.ewmmain.model.request;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.ewmmain.model.User;
import ru.practicum.ewmmain.model.event.Event;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "requests")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    @NonNull
    private User requester;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @NonNull
    private Event event;

    @Enumerated(value = EnumType.STRING)
    @NonNull
    private RequestStatus status;

    @CreationTimestamp
    private LocalDateTime created;
}
