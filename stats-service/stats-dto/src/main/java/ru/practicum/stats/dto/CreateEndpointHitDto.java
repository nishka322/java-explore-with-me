package ru.practicum.stats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateEndpointHitDto {

    @NotNull(message = "field app can't be null")
    @NotBlank
    @Size(max = 255)
    private String app;
    @NotNull(message = "field uri can't be null")
    @NotBlank
    @Size(max = 255)
    private String uri;
    @NotNull(message = "field ip can't be null")
    @NotBlank
    @Size(max = 255)
    private String ip;
    @NotNull
    private String timestamp;
}