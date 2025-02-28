package itawi.url_shortener.service;

import itawi.url_shortener.dto.Request.ShortenRequest;
import itawi.url_shortener.dto.Response.ShortenResponse;
import itawi.url_shortener.exception.UrlNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

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

    public ShortenResponse shortenUrl(ShortenRequest request) {
        urlValidator.validate(request.url());

        String shortCode = generateShortCode(request);
        storeUrl(shortCode, request);
        return new ShortenResponse(buildShortUrl(shortCode), request.url(), 0L);
    }

    private String generateShortCode(ShortenRequest request) {
        return request.customAlias() != null ?
                request.customAlias() :
                idConverter.createUniqueId();
    }

    private void storeUrl(String shortCode, ShortenRequest request) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(URL_KEY_PREFIX + shortCode, request.url());

        if (request.expirationSeconds() != null) {
            redisTemplate.expire(URL_KEY_PREFIX + shortCode,
                    request.expirationSeconds(),
                    TimeUnit.SECONDS);
        }
    }

    public String resolveUrl(String shortCode) {
        String url = redisTemplate.opsForValue().get(URL_KEY_PREFIX + shortCode);
        if (url == null) throw new UrlNotFoundException();

        redisTemplate.opsForValue().increment(ACCESS_COUNT_KEY_PREFIX + shortCode);
        return url;
    }

    public Long getAccessCount(String shortCode) {
        String count = redisTemplate.opsForValue().get(ACCESS_COUNT_KEY_PREFIX + shortCode);
        return count != null ? Long.parseLong(count) : 0L;
    }

    public String buildShortUrl(String shortCode) {
        return baseUrl + "/" + shortCode;
    }
}