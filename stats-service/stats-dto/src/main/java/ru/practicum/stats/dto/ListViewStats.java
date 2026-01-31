package ru.practicum.stats.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ListViewStats {
    @JsonValue
    private List<ru.practicum.stats.dto.ViewStats> viewStats;
}