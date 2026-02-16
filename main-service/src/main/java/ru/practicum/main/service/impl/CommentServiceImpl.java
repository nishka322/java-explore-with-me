package ru.practicum.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.NewCommentDto;
import ru.practicum.main.exception.*;
import ru.practicum.main.mapper.CommentMapper;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.CommentsRepository;
import ru.practicum.main.service.CommentService;
import ru.practicum.main.service.EventService;
import ru.practicum.main.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final UserService userService;
    private final EventService eventService;
    private final CommentsRepository commentsRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(NewCommentDto newCommentDto, Long userId, Long eventId) {
        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        comment.setText(newCommentDto.getText());

        Comment saved = commentsRepository.save(comment);
        log.info("Created comment {} for event {} by user {}", saved.getId(), eventId, userId);
        return commentMapper.toCommentDto(saved);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAdmin(NewCommentDto newCommentDto, Long commentId) {
        Comment comment = findCommentOrThrow(commentId);
        comment.setText(newCommentDto.getText());
        Comment updated = commentsRepository.save(comment);
        log.info("Admin updated comment {}", commentId);
        return commentMapper.toCommentDto(updated);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByUser(NewCommentDto newCommentDto, Long userId, Long commentId) {
        userService.getUserById(userId);

        Comment comment = findCommentOrThrow(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new CommentConflictException("Can't update comment created by another user");
        }

        comment.setText(newCommentDto.getText());
        Comment updated = commentsRepository.save(comment);
        log.info("User {} updated comment {}", userId, commentId);
        return commentMapper.toCommentDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentsByIdByUser(Long userId, Long commentId) {
        userService.getUserById(userId);

        Comment comment = findCommentOrThrow(commentId);
        if (!userId.equals(comment.getAuthor().getId())) {
            throw new CommentConflictException("Can't get comment created by another user");
        }

        log.info("User {} fetched comment {}", userId, commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserCommentsByCreateTime(Long userId,
                                                        LocalDateTime createStart,
                                                        LocalDateTime createEnd,
                                                        Integer from,
                                                        Integer size) {
        userService.getUserById(userId);

        if (createStart != null && createEnd != null && createEnd.isBefore(createStart)) {
            throw new WrongTimeException("createEnd must be after createStart");
        }

        Specification<Comment> spec = Specification
                .where(authorIdEquals(userId))
                .and(createdBetween(createStart, createEnd));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").ascending());
        List<Comment> comments = commentsRepository.findAll(spec, pageable).getContent();

        log.info("Fetched {} comments for user {}", comments.size(), userId);
        return commentMapper.toCommentDtos(comments);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        userService.getUserById(userId);

        Comment comment = findCommentOrThrow(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new CommentConflictException("Can't delete comment created by another user");
        }

        commentsRepository.delete(comment);
        log.info("User {} deleted comment {}", userId, commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEventIdByAdmin(Long eventId, Integer from, Integer size) {
        eventService.getEventById(eventId);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentsRepository.findAllByEventId(eventId, pageable);
        log.info("Admin fetched {} comments for event {}", comments.size(), eventId);
        return commentMapper.toCommentDtos(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentsByIdByAdmin(Long commentId) {
        Comment comment = findCommentOrThrow(commentId);
        log.info("Admin fetched comment {}", commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = findCommentOrThrow(commentId);
        commentsRepository.delete(comment);
        log.info("Admin deleted comment {}", commentId);
    }

    private Comment findCommentOrThrow(Long commentId) {
        return commentsRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotExistException(
                        String.format("Comment with id=%d doesn't exist", commentId)));
    }

    private static Specification<Comment> authorIdEquals(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), userId);
    }

    private static Specification<Comment> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start != null && end != null) {
                return cb.between(root.get("created"), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("created"), start);
            } else if (end != null) {
                return cb.lessThanOrEqualTo(root.get("created"), end);
            }
            return null;
        };
    }
}