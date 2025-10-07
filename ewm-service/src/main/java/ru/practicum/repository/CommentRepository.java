package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.TotalCommentsByEventDto;
import ru.practicum.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEvent_id(Long eventId, Pageable pageable);

    List<Comment> findByAuthor_Id(Long authorId);

    Optional<Comment> findAllByAuthor_IdAndId(Long authorId, Long eventId);

    @Query("select new ru.practicum.dto.comment.TotalCommentsByEventDto(c.event.id, COUNT(c)) " +
            "from comments as c where c.event.id = ?1 GROUP BY c.event.id")
    List<TotalCommentsByEventDto> countCommentsByEventId(List<Long> eventIds);

    @Query("select c from comments as c where lower(c.text) like lower(concat('%',?1,'%'))")
    List<Comment> findByText(String text, Pageable pageable);
}