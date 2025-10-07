package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.model.Comment;

/**
 * DTO для {@link Comment}
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UpdateCommentDto {
    @NotNull
    @Size(message = "Допустимый размер комментария от 1 до 2000 символов", min = 1, max = 2000)
    @NotBlank
    private final String text;
}