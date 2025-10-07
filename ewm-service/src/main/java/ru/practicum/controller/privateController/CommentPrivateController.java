package ru.practicum.controller.privateController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.model.Comment;
import ru.practicum.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentPrivateController {
    private final CommentService commentService;

    @GetMapping("/users/{userId}/comments")
    public List<CommentDto> getCommentsByUserId(@PathVariable Long userId) {
        log.info("PRIVATE - GET запрос на получение комментариев пользователя с id = {}", userId);
        return commentService.getCommentsByUser(userId);
    }

    @GetMapping("/users/{userId}/{commentId}")
    public Comment getComment(@PathVariable Long userId, @PathVariable Long commentId) {
        log.info("PRIVATE - GET запрос на получения комментария id = {} пользователем id = {} ", commentId, userId);
        return commentService.getCommentByUserIdAndCommentId(userId, commentId);
    }

    @PostMapping("/users/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@PathVariable Long userId,
                                 @PathVariable Long eventId,
                                 @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("POST запрос на добавление комментария: {}", newCommentDto);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("/users/{userId}/{commentId}")
    public CommentDto patchRequestByUser(@PathVariable Long userId, @PathVariable Long commentId,
                                         @Valid @RequestBody NewCommentDto updateCommentDto) {

        log.info("PATCH запрос на обновление пользователем с userId = {}  комментария с commentId = {} ", userId, commentId);
        return commentService.updateByUser(userId, commentId, updateCommentDto);
    }

    @DeleteMapping("/users/{userId}/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        log.info("PRIVATE - DELETE запрос на удаление комментария id = {} пользователем id = {} ", userId, commentId);
        commentService.deleteComment(userId, commentId);
    }
}