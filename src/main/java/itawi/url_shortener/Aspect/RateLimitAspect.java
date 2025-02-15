package itawi.url_shortener.Aspect;

import itawi.url_shortener.Exception.RateLimitExceededException;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@AllArgsConstructor
public class RateLimitAspect {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RedisTemplate<String, String> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) {
            logger.warn("Request object is null. Proceeding without rate limiting.");
            return joinPoint.proceed();
        }

        String ip = request.getRemoteAddr();
        if (ip == null || ip.isEmpty()) {
            logger.warn("IP address is null or empty. Proceeding without rate limiting.");
            return joinPoint.proceed();
        }

        String rateLimitKey = "rate_limit:" + ip;
        Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey, 1);

        if (currentCount == null) {
            redisTemplate.opsForValue().set(rateLimitKey, "1", 1, TimeUnit.MINUTES);
            currentCount = 1L;
        }

        if (currentCount > rateLimit.value()) {
            logger.warn("Rate limit exceeded for IP: {}", ip);
            throw new RateLimitExceededException("Rate limit exceeded. Try again later.");
        }

        return joinPoint.proceed();
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}