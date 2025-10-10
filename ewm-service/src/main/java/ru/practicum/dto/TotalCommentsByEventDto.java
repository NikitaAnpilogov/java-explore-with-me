package ru.practicum.dto;

import lombok.*;

@Data
@NoArgsConstructor
public class TotalCommentsByEventDto {

    public TotalCommentsByEventDto(Long eventId, Long count) {
        this.eventId = eventId;
        this.totalComments = count;
    }

    private Long eventId;
    private Long totalComments;
}