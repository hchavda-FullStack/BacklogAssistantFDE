package com.fde.backlogassistant.service;

import com.fde.backlogassistant.config.AppProperties;
import com.fde.backlogassistant.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {
    @Test void shouldRejectWhenCapacityExceeded() {
        AppProperties p = new AppProperties(); p.getRateLimit().setCapacity(1); p.getRateLimit().setRefillTokens(1); p.getRateLimit().setRefillPeriodSeconds(3600);
        RateLimitService service = new RateLimitService(p);
        service.checkAllowed("client-a");
        assertThrows(RateLimitExceededException.class, () -> service.checkAllowed("client-a"));
        assertDoesNotThrow(() -> service.checkAllowed("client-b"));
    }
}
