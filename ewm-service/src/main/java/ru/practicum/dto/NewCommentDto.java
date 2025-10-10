package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.model.Comment;

/**
 * DTO для {@link Comment}
 */
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor(force = true)
@Builder
public class NewCommentDto {
    @NotBlank(message = "Коментарий не может быть пустой")
    @Size(message = "Допустимый размер коментария от 1 до 2000 символов", min = 1, max = 2000)
    private final String text;
}