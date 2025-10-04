package ru.practicum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * Широта и долгота места проведения события
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "location")
@Builder
@EqualsAndHashCode(exclude = "id")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Max(value = 90)
    @Min(value = -90)
    @Column(name = "lat")
    private float lat;

    @Min(value = -180)
    @Max(value = 180)
    @Column(name = "lon")
    private float lon;
}