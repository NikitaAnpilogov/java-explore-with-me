package ru.practicum.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.practicum.ViewStats;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ViewStatsMapper implements RowMapper<ViewStats> {

    @Override
    public ViewStats mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ViewStats.builder()
                .app(rs.getString("app"))
                .uri(rs.getString("uri"))
                .hits(rs.getLong("hits"))
                .build();
    }
}