package itawi.url_shortener.builder;

import itawi.url_shortener.dto.Response.UrlStatsResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UrlStatsResponseBuilder {

    public static UrlStatsResponse buildUrlStatsResponse(
            String shortCode,
            String originalUrl,
            String shortUrl,
            Long accessCount,
            String createdAt,
            Long ttl) {

        String expiresAt = ttl > 0 ?
            LocalDateTime.now().plusSeconds(ttl).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) :
            "Never";

        return UrlStatsResponse.builder()
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .shortUrl(shortUrl)
                .accessCount(accessCount)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();
    }
}
