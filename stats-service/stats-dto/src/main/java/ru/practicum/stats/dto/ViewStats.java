package ru.practicum.stats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ViewStats {
    @JsonProperty("app")
    private String app;
    @JsonProperty("uri")
    private String uri;
    @JsonProperty("hits")
    private Long hits;
}