package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.StatRequest;
import ru.practicum.ViewStats;
import ru.practicum.ViewsStatsRequest;
import ru.practicum.exceptions.BadRequestException;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatsServiceImplTest {

    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    private StatRequest statRequest;
    private ViewsStatsRequest viewsStatsRequest;


    @BeforeEach
    public void setUp() {
        statRequest = new StatRequest().builder()
                .app("ewm-main-service")
                .uri("test-uri/1")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        viewsStatsRequest = new ViewsStatsRequest().toBuilder()
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusHours(1))
                .uri(List.of("/uri"))
                .unique(false)
                .build();

    }

    @Test
    public void testSaveHit() {
        statsService.saveHit(statRequest);

        verify(statsRepository, times(1)).saveHit(statRequest);
    }

    @Test
    public void testGetViewStatsList() {
        List<ViewStats> viewStatsList = Collections.emptyList();
        when(statsRepository.getStats(viewsStatsRequest)).thenReturn(viewStatsList);

        List<ViewStats> result = statsService.getViewStatsList(viewsStatsRequest);

        assertEquals(viewStatsList, result);
        verify(statsRepository, times(1)).getStats(viewsStatsRequest);
    }

    @Test
    public void testGetViewStatsListWithUnique() {
        viewsStatsRequest = new ViewsStatsRequest().toBuilder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now())
                .uri(List.of("/uri"))
                .unique(true)
                .build();
        List<ViewStats> viewStatsList = Collections.emptyList();
        when(statsRepository.getUniqueStats(viewsStatsRequest)).thenReturn(viewStatsList);
        List<ViewStats> result = statsService.getViewStatsList(viewsStatsRequest);

        assertEquals(viewStatsList, result);
        verify(statsRepository, times(1)).getUniqueStats(viewsStatsRequest);
    }

    @Test
    public void testGetViewStatsListWithInvalidDates() {
        viewsStatsRequest = new ViewsStatsRequest().toBuilder()
                .start(LocalDateTime.now().plusHours(10))
                .end(LocalDateTime.now())
                .uri(List.of("/uri"))
                .unique(false)
                .build();

        assertThrows(BadRequestException.class, () -> statsService.getViewStatsList(viewsStatsRequest));
    }
}