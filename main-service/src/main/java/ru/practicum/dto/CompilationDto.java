package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationDto {
    @UniqueElements
    private Set<EventShortDto> events;

    private Long id;

    private boolean pinned;

    private String title;
}