package itawi.url_shortener.builder;

import itawi.url_shortener.dto.Response.ShortenResponse;

public class ShortenResponseBuilder {

    public static ShortenResponse buildShortenResponse(String shortUrl, String originalUrl, Long accessCount) {
        return ShortenResponse.builder()
                .shortUrl(shortUrl)
                .originalUrl(originalUrl)
                .accessCount(accessCount)
                .build();
    }
}
