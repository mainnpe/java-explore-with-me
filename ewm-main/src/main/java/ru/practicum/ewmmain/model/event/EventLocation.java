package ru.practicum.ewmmain.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Embeddable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventLocation {
    @NonNull
    private Double latitude;
    @NonNull
    private Double longitude;
}
