import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiter {

    // Tracks each client's token bucket
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    // Configuration: 1000 requests/hour
    private final long MAX_TOKENS = 1000;
    private final long REFILL_INTERVAL_MS = 3600_000;               // 1 hour
    private final double REFILL_RATE_PER_MS = MAX_TOKENS / (double) REFILL_INTERVAL_MS;

    // Token Bucket class
    static class TokenBucket {
        AtomicLong tokens;
        AtomicLong lastRefillTime;

        TokenBucket(long maxTokens) {
            this.tokens = new AtomicLong(maxTokens);
            this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
        }
    }

    // -----------------------------------------
    // Main API: Check if request is allowed
    // -----------------------------------------
    public synchronized boolean checkRateLimit(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, id -> new TokenBucket(MAX_TOKENS));
        refill(bucket);

        long currentTokens = bucket.tokens.get();
        if (currentTokens > 0) {
            bucket.tokens.decrementAndGet();
            return true;
        }
        return false;
    }

    // -----------------------------------------
    // Refill tokens based on elapsed time
    // -----------------------------------------
    private void refill(TokenBucket bucket) {
        long now = System.currentTimeMillis();
        long lastRefill = bucket.lastRefillTime.get();

        long elapsed = now - lastRefill;
        if (elapsed <= 0) return;

        long newTokens = (long) (elapsed * REFILL_RATE_PER_MS);

        if (newTokens > 0) {
            long updatedTokens = Math.min(MAX_TOKENS, bucket.tokens.get() + newTokens);
            bucket.tokens.set(updatedTokens);
            bucket.lastRefillTime.set(now);
        }
    }

    // -------------------------------------------------------
    // Get detailed rate limit status for dashboard/debugging
    // -------------------------------------------------------
    public synchronized String getRateLimitStatus(String clientId) {
        TokenBucket bucket = buckets.get(clientId);
        if (bucket == null) {
            return "{used: 0, limit: " + MAX_TOKENS + ", reset: now}";
        }

        refill(bucket);

        long remaining = bucket.tokens.get();
        long used = MAX_TOKENS - remaining;
        long resetSeconds =
                (bucket.lastRefillTime.get() + REFILL_INTERVAL_MS - System.currentTimeMillis()) / 1000;

        return "{used: " + used +
               ", limit: " + MAX_TOKENS +
               ", reset: " + resetSeconds + "s}";
    }

    // ---------------------------------------------------
    // Helper: returns error when limit exceeded
    // ---------------------------------------------------
    public String handleRequest(String clientId) {
        if (checkRateLimit(clientId)) {
            long remaining = buckets.get(clientId).tokens.get();
            return "Allowed (" + remaining + " requests remaining)";
        } else {
            long retryAfter =
                (buckets.get(clientId).lastRefillTime.get() + REFILL_INTERVAL_MS - System.currentTimeMillis()) / 1000;

            return "Denied (0 requests remaining, retry after " + retryAfter + "s)";
        }
    }

    // ---------------------------------------------------
    // Demo
    // ---------------------------------------------------
    public static void main(String[] args) throws InterruptedException {
        RateLimiter limiter = new RateLimiter();
        String client = "abc123";

        for (int i = 0; i < 1005; i++) {
            System.out.println(limiter.handleRequest(client));
            Thread.sleep(5); // simulate traffic
        }

        System.out.println("\nStatus:");
        System.out.println(limiter.getRateLimitStatus(client));
    }
}
