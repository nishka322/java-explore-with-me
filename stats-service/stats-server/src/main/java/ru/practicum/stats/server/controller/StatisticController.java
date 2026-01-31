package ru.practicum.stats.server.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.CreateEndpointHitDto;
import ru.practicum.stats.dto.ListViewStats;
import ru.practicum.stats.dto.ResponseEndpointHitDto;
import ru.practicum.stats.dto.ViewStats;
import ru.practicum.stats.server.service.StatisticServiceImpl;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping
public class StatisticController {

    private final StatisticServiceImpl statsService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatisticController(StatisticServiceImpl StatisticServiceImpl) {
        this.statsService = StatisticServiceImpl;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEndpointHitDto hit(@Valid @RequestBody CreateEndpointHitDto hit) {
        return statsService.addEndpointHit(hit);
    }

    @GetMapping("/stats")
    public List<ViewStats> stats(@RequestParam String start,
                                 @RequestParam String end,
                                 @RequestParam(required = false) List<String> uris,
                                 @RequestParam(required = false, defaultValue = "false") boolean unique) {

        ListViewStats result = statsService.getStats(start, end, uris, unique);

        return result.getViewStats();
    }
}
