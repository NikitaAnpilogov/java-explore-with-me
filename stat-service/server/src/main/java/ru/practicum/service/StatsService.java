package ru.practicum.service;

import ru.practicum.StatRequest;
import ru.practicum.ViewStats;
import ru.practicum.ViewsStatsRequest;

import java.util.List;

public interface StatsService {
    void saveHit(StatRequest hit);

    List<ViewStats> getViewStatsList(ViewsStatsRequest request);
}