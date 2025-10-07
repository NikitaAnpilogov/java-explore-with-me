package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEvent_id(Long eventId, Pageable pageable);

    List<Comment> findByAuthor_Id(Long authorId);

    Optional<Comment> findAllByAuthor_IdAndId(Long authorId, Long eventId);

    @Query("SELECT c.event.id, COUNT(c) " +
            "FROM Comment c WHERE c.event.id IN :eventIds GROUP BY c.event.id")
    List<Object[]> countCommentsByEventId(@Param("eventIds") List<Long> eventIds);

    @Query("select c from Comment c where lower(c.text) like lower(concat('%',?1,'%'))")
    List<Comment> findByText(String text, Pageable pageable);
}