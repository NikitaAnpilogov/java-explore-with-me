package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;

/**
 * DTO для {@link Comment}
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@Builder
public class CommentDto {
    private Long id;
    private Long authorId;
    private String text;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;
}