package ru.practicum;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ViewsStatsRequest {
    private List<String> uri;
    private LocalDateTime start;
    private LocalDateTime end;
    private Boolean unique;
    private String application;
}