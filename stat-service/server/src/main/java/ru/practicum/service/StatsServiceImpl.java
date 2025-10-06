package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.StatRequest;
import ru.practicum.ViewStats;
import ru.practicum.ViewsStatsRequest;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.repository.StatsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statRepository;

    @Override
    public void saveHit(StatRequest hit) {
        statRepository.saveHit(hit);
    }

    @Override
    public List<ViewStats> getViewStatsList(ViewsStatsRequest request) {
        if (request.getStart() != null && request.getEnd() != null) {
            if (request.getStart().isAfter(request.getEnd())) {
                throw new BadRequestException("Дата старта поиска не может быть больше даты окончания");
            }
        }
        if (request.getUnique() != null) {
            if (request.getUnique()) {
                return statRepository.getUniqueStats(request);
            }
        }
        return statRepository.getStats(request);
    }
}