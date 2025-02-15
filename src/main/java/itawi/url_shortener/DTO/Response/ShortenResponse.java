package itawi.url_shortener.DTO.Response;

public record ShortenResponse(
        String shortUrl,
        String originalUrl,
        Long accessCount
) {
}