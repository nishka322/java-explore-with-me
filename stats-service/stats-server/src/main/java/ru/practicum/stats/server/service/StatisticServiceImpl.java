package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.CreateEndpointHitDto;
import ru.practicum.stats.dto.ListViewStats;
import ru.practicum.stats.dto.ResponseEndpointHitDto;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.server.mapper.StatisticMapper;
import ru.practicum.stats.server.repository.StatisticRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatisticServiceImpl implements StatisticService {
    private final StatisticRepository statistics;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ResponseEndpointHitDto addEndpointHit(CreateEndpointHitDto createEndpointHitDto) {
        return StatisticMapper.toDto(
                statistics.save(
                        StatisticMapper.toEntity(createEndpointHitDto)
                )
        );
    }

    @Override
    public ListViewStats getStats(String start, String end, List<String> uris, Boolean unique) {
        LocalDateTime parseStart = LocalDateTime.parse(start, FORMATTER);
        LocalDateTime parseEnd = LocalDateTime.parse(end, FORMATTER);
        List<ViewStats> viewStats;
        ListViewStats response;
        if (unique) {
            viewStats = uris == null
                    ? statistics.getUniqueViewStatsByStartAndEndTime(parseStart, parseEnd)
                    : statistics.getUniqueUrisViewStatsByStartAndEndTime(parseStart, parseEnd, uris);
        } else {
            viewStats = uris == null ? statistics.getViewStatsByStartAndEndTime(parseStart, parseEnd)
                    : statistics.getUrisViewStatsByStartAndEndTime(parseStart, parseEnd, uris);
        }
        response = ListViewStats.builder().viewStats(viewStats).build();
        return response;
    }
}