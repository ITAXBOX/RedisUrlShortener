package itawi.url_shortener.service;

import itawi.url_shortener.dto.Request.ShortenRequest;
import itawi.url_shortener.dto.Response.ShortenResponse;
import itawi.url_shortener.dto.Response.UrlStatsResponse;
import itawi.url_shortener.exception.UrlNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final RedisTemplate<String, String> redisTemplate;
    private final IdConverterService idConverter;
    private final UrlValidator urlValidator;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final String URL_KEY_PREFIX = "url:";
    private static final String ACCESS_COUNT_KEY_PREFIX = "access:";
    private static final String CREATED_AT_KEY_PREFIX = "created:";

    public ShortenResponse shortenUrl(ShortenRequest request) {
        urlValidator.validate(request.url());

        String shortCode = generateShortCode(request);
        storeUrl(shortCode, request);
        return new ShortenResponse(buildShortUrl(shortCode), request.url(), 0L);
    }

    public String resolveUrl(String shortCode) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String originalUrl = ops.get(URL_KEY_PREFIX + shortCode);

        if (originalUrl == null) {
            throw new UrlNotFoundException("Short code not found: " + shortCode);
        }

        // Increment access count
        ops.increment(ACCESS_COUNT_KEY_PREFIX + shortCode);

        return originalUrl;
    }

    public UrlStatsResponse getUrlStats(String shortCode) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String originalUrl = ops.get(URL_KEY_PREFIX + shortCode);

        if (originalUrl == null) {
            throw new UrlNotFoundException("Short code not found: " + shortCode);
        }

        String accessCountStr = ops.get(ACCESS_COUNT_KEY_PREFIX + shortCode);
        String createdAt = ops.get(CREATED_AT_KEY_PREFIX + shortCode);
        Long accessCount = accessCountStr != null ? Long.parseLong(accessCountStr) : 0L;

        long ttl = redisTemplate.getExpire(URL_KEY_PREFIX + shortCode);
        String expiresAt = ttl > 0 ?
                LocalDateTime.now().plusSeconds(ttl).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) :
                "Never";

        return UrlStatsResponse.builder()
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .shortUrl(buildShortUrl(shortCode))
                .accessCount(accessCount)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();
    }

    private String generateShortCode(ShortenRequest request) {
        return request.customAlias() != null ?
                request.customAlias() :
                idConverter.createUniqueId();
    }

    private void storeUrl(String shortCode, ShortenRequest request) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(URL_KEY_PREFIX + shortCode, request.url());
        ops.set(CREATED_AT_KEY_PREFIX + shortCode, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        ops.set(ACCESS_COUNT_KEY_PREFIX + shortCode, "0");

        if (request.expirationSeconds() != null) {
            redisTemplate.expire(URL_KEY_PREFIX + shortCode, request.expirationSeconds(), TimeUnit.SECONDS);
            redisTemplate.expire(CREATED_AT_KEY_PREFIX + shortCode, request.expirationSeconds(), TimeUnit.SECONDS);
            redisTemplate.expire(ACCESS_COUNT_KEY_PREFIX + shortCode, request.expirationSeconds(), TimeUnit.SECONDS);
        }
    }

    private String buildShortUrl(String shortCode) {
        return baseUrl + "/" + shortCode;
    }
}