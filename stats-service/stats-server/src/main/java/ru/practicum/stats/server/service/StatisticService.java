package ru.practicum.stats.server.service;


import ru.practicum.stats.dto.CreateEndpointHitDto;
import ru.practicum.stats.dto.ListViewStats;
import ru.practicum.stats.dto.ResponseEndpointHitDto;

import java.util.List;

public interface StatisticService {
    ResponseEndpointHitDto addEndpointHit(CreateEndpointHitDto createEndpointHitDto);

    ListViewStats getStats(String start, String end, List<String> uris, Boolean unique);
}