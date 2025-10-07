package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.NewCommentDto;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(source = "author.id", target = "authorId")
    CommentDto map(Comment comment);

    @Mapping(target = "author", source = "user")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "id", ignore = true)
    Comment map(NewCommentDto newCommentDto, Event event, User user);

}