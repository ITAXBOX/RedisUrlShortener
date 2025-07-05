package itawi.url_shortener.dto.Response;

import lombok.Builder;

@Builder
public record ShortenResponse(
        String shortUrl,
        String originalUrl,
        Long accessCount
) {
}