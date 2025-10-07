package ru.practicum.service.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.exceptions.IncorrectParametersException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.model.enums.EventStatus;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.CommentService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getEventStatus().equals(EventStatus.PUBLISHED)) {
            throw new IncorrectParametersException("Добавление комментария невозможно.",
                    "Нельзя комментировать не опубликованное событие.");
        }
        return commentMapper.map(commentRepository.save(commentMapper.map(newCommentDto, event, user)));
    }

    @Transactional
    @Override
    public CommentDto updateByUser(Long userId, Long commentId, @Valid NewCommentDto newCommentDto) {
        User user = checkUser(userId);
        Comment comment = checkComment(commentId);
        checkAuthorComment(user, comment);
        LocalDateTime now = LocalDateTime.now();
        comment.setText(newCommentDto.getText());
        comment.setUpdated(now);
        return commentMapper.map(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentDto> getCommentsByUser(Long userId) {
        User user = checkUser(userId);
        List<Comment> comments = commentRepository.findByAuthor_Id(user.getId());
        return comments.stream().map(commentMapper::map).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Comment getCommentByUserIdAndCommentId(Long userId, Long commentId) {
        checkUser(userId);
        return commentRepository.findAllByAuthor_IdAndId(userId, commentId).orElseThrow(
                () -> new NotFoundException("Комментарий не найден.", String.format("Комментарий c id=%d  не найден.", commentId)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Comment> getCommentsInEvent(Long eventId, Integer from, Integer size) {
        Event event = checkEvent(eventId);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return commentRepository.findAllByEvent_id(event.getId(), pageRequest);
    }

    @Transactional
    @Override
    public void deleteComment(Long userId, Long commentId) {
        User user = checkUser(userId);
        Comment comment = checkComment(commentId);
        checkAuthorComment(user, comment);
        commentRepository.delete(comment);
    }

    @Transactional
    @Override
    public void deleteCommentByAdmin(Long commentId) {
        commentRepository.delete(checkComment(commentId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Comment> getCommentsByText(String text, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (text.isEmpty() || text.isBlank()) {
            return Collections.emptyList();
        }
        //return commentRepository.findByText(text, pageable);
        return null;
    }

    private Comment checkComment(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new NotFoundException("Комментарий не найден.",
                String.format("Комментарий c id=%d  не найден.", id)));
    }

    private void checkAuthorComment(User user, Comment comment) {
        if (!comment.getAuthor().equals(user)) {
            throw new IncorrectParametersException("Доступ запрещен.", "Пользователь не является автором комментария");
        }
    }

    private Event checkEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Событие не найдено.",
                String.format("Событие с id=%d  не найдено", id)));
    }

    private User checkUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден.",
                String.format("Пользователь c id=%d  не найден", id)));
    }
}