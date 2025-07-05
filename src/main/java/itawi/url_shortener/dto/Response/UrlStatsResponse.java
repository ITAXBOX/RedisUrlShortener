package itawi.url_shortener.dto.Response;

import lombok.Builder;

@Builder
public record UrlStatsResponse(
        String shortCode,
        String originalUrl,
        String shortUrl,
        Long accessCount,
        String createdAt,
        String expiresAt
) {
}
