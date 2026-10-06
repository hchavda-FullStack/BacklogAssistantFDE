package com.fde.backlogassistant.service;

import com.fde.backlogassistant.config.AppProperties;
import com.fde.backlogassistant.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final AppProperties.RateLimit properties;
    private final Clock clock;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Autowired
    public RateLimitService(AppProperties appProperties) {
        this(appProperties, Clock.systemUTC());
    }

    RateLimitService(AppProperties appProperties, Clock clock) {
        this.properties = appProperties.getRateLimit();
        this.clock = clock;
    }

    public void checkAllowed(String clientId) {
        String key = (clientId == null || clientId.isBlank()) ? "unknown" : clientId;
        TokenBucket bucket = buckets.computeIfAbsent(key,
                ignored -> new TokenBucket(properties.getCapacity(), clock.millis()));
        if (!bucket.tryConsume(properties, clock.millis())) {
            throw new RateLimitExceededException("AI request rate limit exceeded. Please retry later.");
        }
    }

    private static final class TokenBucket {
        private double tokens;
        private long lastRefillMillis;
        private TokenBucket(int capacity, long now) {
            this.tokens = capacity;
            this.lastRefillMillis = now;
        }
        private synchronized boolean tryConsume(AppProperties.RateLimit p, long now) {
            long elapsed = Math.max(0, now - lastRefillMillis);
            double periods = elapsed / (p.getRefillPeriodSeconds() * 1000.0);
            if (periods > 0) {
                tokens = Math.min(p.getCapacity(), tokens + periods * p.getRefillTokens());
                lastRefillMillis = now;
            }
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }
}
