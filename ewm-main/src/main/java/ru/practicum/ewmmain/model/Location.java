package ru.practicum.ewmmain.model;

import lombok.*;

import javax.persistence.*;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @NonNull
    private String name;
    @NonNull
    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
    @NonNull
    private Double latitude;
    @NonNull
    private Double longitude;
}


