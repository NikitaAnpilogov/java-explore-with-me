package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.constants.PaginationConstants;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchEventParamsAdmin {
    private List<Long> users;

    private List<String> states;

    private List<Long> categories;

    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;
    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    @PositiveOrZero
    @Builder.Default
    private Integer from = PaginationConstants.DEFAULT_FROM;
    @Positive
    @Builder.Default
    private Integer size = PaginationConstants.DEFAULT_SIZE;
}