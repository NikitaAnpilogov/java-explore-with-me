package ru.practicum;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class ViewStats {
    private String app;
    private String uri;
    private int hits;
}