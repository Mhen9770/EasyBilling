package com.easybilling.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/**
 * Gateway filter for rate limiting.
 */
@Slf4j
@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final Duration WINDOW_SIZE = Duration.ofMinutes(1);
    
    public RateLimitFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
            String key = "rate_limit:" + clientIp;
            
            return redisTemplate.opsForValue()
                    .increment(key)
                    .flatMap(count -> {
                        if (count == 1) {
                            // Set expiration on first request
                            return redisTemplate.expire(key, WINDOW_SIZE)
                                    .then(chain.filter(exchange));
                        } else if (count > MAX_REQUESTS_PER_MINUTE) {
                            log.warn("Rate limit exceeded for IP: {}", clientIp);
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            return exchange.getResponse().setComplete();
                        } else {
                            return chain.filter(exchange);
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("Rate limit check error: {}", e.getMessage());
                        // Continue on error to not block requests
                        return chain.filter(exchange);
                    });
        };
    }
    
    public static class Config {
        // Configuration properties if needed
    }
}
