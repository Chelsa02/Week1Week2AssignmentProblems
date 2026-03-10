import java.util.*;

public class DistributedRateLimiter {

    // Token Bucket class
    static class TokenBucket {
        int tokens;
        int maxTokens;
        long lastRefillTime;

        TokenBucket(int maxTokens) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }
    }

    // clientId -> TokenBucket
    private Map<String, TokenBucket> clientBuckets = new HashMap<>();

    private static final int MAX_REQUESTS = 1000;
    private static final long REFILL_INTERVAL = 60 * 60 * 1000; // 1 hour

    // Check rate limit
    public synchronized String checkRateLimit(String clientId) {

        TokenBucket bucket = clientBuckets.get(clientId);

        if (bucket == null) {
            bucket = new TokenBucket(MAX_REQUESTS);
            clientBuckets.put(clientId, bucket);
        }

        refillTokens(bucket);

        if (bucket.tokens > 0) {
            bucket.tokens--;
            return "Allowed (" + bucket.tokens + " requests remaining)";
        }

        long retryAfter = (REFILL_INTERVAL -
                (System.currentTimeMillis() - bucket.lastRefillTime)) / 1000;

        return "Denied (0 requests remaining, retry after " + retryAfter + "s)";
    }

    // Refill tokens every hour
    private void refillTokens(TokenBucket bucket) {

        long now = System.currentTimeMillis();

        if (now - bucket.lastRefillTime >= REFILL_INTERVAL) {
            bucket.tokens = bucket.maxTokens;
            bucket.lastRefillTime = now;
        }
    }

    // Show rate limit status
    public void getRateLimitStatus(String clientId) {

        TokenBucket bucket = clientBuckets.get(clientId);

        if (bucket == null) {
            System.out.println("Client not found.");
            return;
        }

        int used = bucket.maxTokens - bucket.tokens;

        long resetTime = bucket.lastRefillTime + REFILL_INTERVAL;

        System.out.println("{used: " + used +
                ", limit: " + bucket.maxTokens +
                ", reset: " + resetTime + "}");
    }

    public static void main(String[] args) {

        DistributedRateLimiter limiter = new DistributedRateLimiter();

        String client = "abc123";

        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit(client));
        }

        limiter.getRateLimitStatus(client);
    }
}
