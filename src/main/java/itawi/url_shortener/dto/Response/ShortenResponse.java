package itawi.url_shortener.dto.Response;

public record ShortenResponse(
        String shortUrl,
        String originalUrl,
        Long accessCount
) {
}