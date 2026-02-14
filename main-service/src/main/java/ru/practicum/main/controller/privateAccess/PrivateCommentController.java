package ru.practicum.main.controller.privateAccess;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.config.ConfigUtil;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.NewCommentDto;
import ru.practicum.main.exception.PaginationException;
import ru.practicum.main.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@Valid @RequestBody NewCommentDto newCommentDto,
                                    @PathVariable Long userId,
                                    @PathVariable Long eventId) {
        log.debug("User {} creates comment for event {}", userId, eventId);
        return commentService.createComment(newCommentDto, userId, eventId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@Valid @RequestBody NewCommentDto newCommentDto,
                                    @PathVariable Long userId,
                                    @PathVariable Long commentId) {
        log.debug("User {} updates comment {}", userId, commentId);
        return commentService.updateCommentByUser(newCommentDto, userId, commentId);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable Long userId,
                                     @PathVariable Long commentId) {
        log.debug("User {} fetches comment {}", userId, commentId);
        return commentService.getCommentsByIdByUser(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getUserCommentsByCreateTime(
            @PathVariable Long userId,
            @PositiveOrZero @RequestParam(defaultValue = ConfigUtil.DEFAULT_PAGE_FROM) Integer from,
            @Positive @RequestParam(defaultValue = ConfigUtil.DEFAULT_PAGE_SIZE) Integer size,
            @RequestParam(required = false) @DateTimeFormat(pattern = ConfigUtil.DATE) LocalDateTime createStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = ConfigUtil.DATE) LocalDateTime createEnd) {

        if (from % size != 0) {
            throw new PaginationException("Parameter 'from' must be a multiple of 'size'");
        }
        if (size <= 0) {
            throw new PaginationException("Parameter 'size' must be positive");
        }
        if (from < 0) {
            throw new PaginationException("Parameter 'from' must be zero or positive");
        }
        log.debug("User {} fetches own comments: from={}, size={}, start={}, end={}",
                userId, from, size, createStart, createEnd);
        return commentService.getUserCommentsByCreateTime(userId, createStart, createEnd, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByUser(@PathVariable Long userId,
                                    @PathVariable Long commentId) {
        log.debug("User {} deletes comment {}", userId, commentId);
        commentService.deleteCommentByUser(userId, commentId);
    }
}