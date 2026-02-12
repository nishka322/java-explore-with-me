package ru.practicum.main.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main.config.ConfigUtil;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CommentDto {
    private Long id;

    @NotBlank
    @Size(max = 250)
    private String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConfigUtil.DATE)
    private LocalDateTime created;
    private String authorName;
    private Long eventId;
}
