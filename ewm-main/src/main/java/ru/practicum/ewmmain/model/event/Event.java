package ru.practicum.ewmmain.model.event;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.ewmmain.model.Category;
import ru.practicum.ewmmain.model.Compilation;
import ru.practicum.ewmmain.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;


@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String title;
    private String annotation;
    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate; //event_date

    @Column(name = "created")
    @CreationTimestamp
    private LocalDateTime createdOn; //created

    @Column(name = "published")
    private LocalDateTime publishedOn; //published

    @Enumerated(value = EnumType.STRING)
    private EventStatus state;
    private Boolean paid;

    @Column(name = "participant_limit")
    private Integer participantLimit;  // participant_limit INT default 0,

    @Column(name = "request_moderation")
    private Boolean requestModeration;  //request_moderation BOOLEAN DEFAULT TRUE,

    @Column(name = "confirmed_requests")
    private Integer confirmedRequests;  //confirmed_requests INT,

    @Embedded
    private EventLocation location;

    @ManyToMany(mappedBy = "events")
    Set<Compilation> compilations;

    public Event() {
    }
}
