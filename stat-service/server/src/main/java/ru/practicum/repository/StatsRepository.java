package ru.practicum.repository;

import ru.practicum.StatRequest;
import ru.practicum.ViewStats;
import ru.practicum.ViewsStatsRequest;

import java.util.List;

public interface StatsRepository {
    void saveHit(StatRequest hit);

    List<ViewStats> getStats(ViewsStatsRequest request);

    List<ViewStats> getUniqueStats(ViewsStatsRequest request);
}