package com.fde.backlogassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Ai ai = new Ai();
    private final RateLimit rateLimit = new RateLimit();
    private final Pdf pdf = new Pdf();

    public Ai getAi() { return ai; }
    public RateLimit getRateLimit() { return rateLimit; }
    public Pdf getPdf() { return pdf; }

    public static class Ai {
        private final Retry retry = new Retry();
        public Retry getRetry() { return retry; }
    }
    public static class Retry {
        private int maxAttempts = 3;
        private long initialDelayMs = 500;
        private double multiplier = 2.0;
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public long getInitialDelayMs() { return initialDelayMs; }
        public void setInitialDelayMs(long initialDelayMs) { this.initialDelayMs = initialDelayMs; }
        public double getMultiplier() { return multiplier; }
        public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
    }
    public static class RateLimit {
        private int capacity = 10;
        private int refillTokens = 10;
        private long refillPeriodSeconds = 60;
        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
        public int getRefillTokens() { return refillTokens; }
        public void setRefillTokens(int refillTokens) { this.refillTokens = refillTokens; }
        public long getRefillPeriodSeconds() { return refillPeriodSeconds; }
        public void setRefillPeriodSeconds(long refillPeriodSeconds) { this.refillPeriodSeconds = refillPeriodSeconds; }
    }
    public static class Pdf {
        private long maxFileSizeBytes = 5_242_880;
        private int maxPages = 100;
        private int maxExtractedCharacters = 100_000;
        private int chunkSize = 12_000;
        private int chunkOverlap = 500;
        public long getMaxFileSizeBytes() { return maxFileSizeBytes; }
        public void setMaxFileSizeBytes(long maxFileSizeBytes) { this.maxFileSizeBytes = maxFileSizeBytes; }
        public int getMaxPages() { return maxPages; }
        public void setMaxPages(int maxPages) { this.maxPages = maxPages; }
        public int getMaxExtractedCharacters() { return maxExtractedCharacters; }
        public void setMaxExtractedCharacters(int maxExtractedCharacters) { this.maxExtractedCharacters = maxExtractedCharacters; }
        public int getChunkSize() { return chunkSize; }
        public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
        public int getChunkOverlap() { return chunkOverlap; }
        public void setChunkOverlap(int chunkOverlap) { this.chunkOverlap = chunkOverlap; }
    }
}
