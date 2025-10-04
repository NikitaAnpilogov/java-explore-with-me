package ru.practicum;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


public class StatsClient extends BaseClient {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Formatter.DATE_FORMAT);
    private String applicationName;

    public StatsClient(@Value("${stats-server.url}") String serverUrl, @Value("${application.name}") String applicationName, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
        this.applicationName = applicationName;
    }

    public ResponseEntity<Object> saveStats(HttpServletRequest request) {
        final StatRequest hit = StatRequest.builder()
                .app(applicationName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(Timestamp.from(Instant.now()).toLocalDateTime())
                .build();
        return post(hit);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "uris", String.join(",", uris),
                "unique", unique
        );
        return get("/stats?start={start}&end={end}&uris={uris}&unique={unique}", parameters);
    }
}