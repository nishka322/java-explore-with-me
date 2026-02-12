package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.model.Comment;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreated(comment.getCreated());

        if (comment.getAuthor() != null) {
            dto.setAuthorName(comment.getAuthor().getName());
        }

        if (comment.getEvent() != null) {
            dto.setEventId(comment.getEvent().getId());
        }

        return dto;
    }

    public List<CommentDto> toCommentDtos(List<Comment> comments) {
        if (comments == null) {
            return null;
        }
        return comments.stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
    }
}