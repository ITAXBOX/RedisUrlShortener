package itawi.url_shortener.service;

import itawi.url_shortener.util.ConversionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdConverterService {

    private final RedisTemplate<String, String> redisTemplate;

    public String createUniqueId() {
        Long id = redisTemplate.opsForValue().increment("id_counter");
        return ConversionUtils.convertToBase62(id);
    }
}
