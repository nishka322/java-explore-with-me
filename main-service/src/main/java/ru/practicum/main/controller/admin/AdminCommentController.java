package ru.practicum.main.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.config.ConfigUtil;
import ru.practicum.main.dto.comment.CommentDto;
import ru.practicum.main.dto.comment.NewCommentDto;
import ru.practicum.main.service.CommentService;
import ru.practicum.main.utils.PaginationValidator;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;
    private final PaginationValidator paginationValidator;

    @GetMapping
    public List<CommentDto> getCommentsByEventId(@Positive @RequestParam Long eventId,
                                                 @PositiveOrZero @RequestParam(defaultValue = ConfigUtil.DEFAULT_PAGE_FROM) Integer from,
                                                 @Positive @RequestParam(defaultValue = ConfigUtil.DEFAULT_PAGE_SIZE) Integer size) {

        paginationValidator.validatePaginationParams(from, size);

        log.debug("Admin request: get comments for event ID = {}, from = {}, size = {}", eventId, from, size);
        return commentService.getCommentsByEventIdByAdmin(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable Long commentId) {
        log.debug("Admin request: get comment by ID = {}", commentId);
        return commentService.getCommentsByIdByAdmin(commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.debug("Admin request: delete comment ID = {}", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(
            @Valid @RequestBody NewCommentDto newCommentDto,
            @PathVariable Long commentId) {

        log.debug("Admin request: update comment ID = {}", commentId);
        return commentService.updateCommentByAdmin(newCommentDto, commentId);
    }
}