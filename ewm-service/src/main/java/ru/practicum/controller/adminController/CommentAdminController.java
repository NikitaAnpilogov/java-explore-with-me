package ru.practicum.controller.adminController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.Comment;
import ru.practicum.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class CommentAdminController {
    private final CommentService commentService;

    @GetMapping("/search")
    public List<Comment> getCommentsByText(@RequestParam String text,
                                           @RequestParam(value = "from", defaultValue = "0") Integer from,
                                           @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("ADMIN - GET запрос на получение комментариев по тексту");
        return commentService.getCommentsByText(text, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("ADMIN - GET запрос на просмотр комментария с id = {}", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }
}