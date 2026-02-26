import java.util.*;
import java.util.concurrent.*;
import java.net.InetAddress;

public class DNSCache {

    // DNS entry with TTL
    static class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime;   // epoch ms

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    // LRU cache using LinkedHashMap
    private final int capacity;
    private final Map<String, DNSEntry> cache;

    // Stats
    private long hits = 0;
    private long misses = 0;

    // Auto cleanup service
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public DNSCache(int capacity) {
        this.capacity = capacity;

        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };

        // Run cleanup every second
        cleaner.scheduleAtFixedRate(this::removeExpiredEntries, 1, 1, TimeUnit.SECONDS);
    }

    // Main resolve method
    public synchronized String resolve(String domain) {

        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            return "Cache HIT → " + entry.ipAddress;
        }

        // Cache miss or expired
        misses++;

        if (entry != null && entry.isExpired()) {
            cache.remove(domain);
            System.out.println("Cache EXPIRED for: " + domain);
        }

        // Query upstream DNS
        String ip = queryUpstreamDNS(domain);

        // TTL = 5 minutes for demo (300 seconds)
        cache.put(domain, new DNSEntry(domain, ip, 300));

        return "Cache MISS → Query Upstream → " + ip;
    }

    // Simulated upstream DNS query
    private String queryUpstreamDNS(String domain) {
        try {
            // Real DNS lookup
            InetAddress address = InetAddress.getByName(domain);
            return address.getHostAddress();
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }

    // Remove expired entries
    private synchronized void removeExpiredEntries() {
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, DNSEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                toRemove.add(entry.getKey());
            }
        }

        for (String key : toRemove) {
            cache.remove(key);
        }
    }

    // Stats
    public synchronized String getCacheStats() {
        long total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0 / total);

        return String.format(
            "Hit Rate: %.2f%%, Hits: %d, Misses: %d, Cache Size: %d",
            hitRate, hits, misses, cache.size()
        );
    }

    // Graceful shutdown
    public void shutdown() {
        cleaner.shutdown();
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        DNSCache dnsCache = new DNSCache(5);

        System.out.println(dnsCache.resolve("google.com"));
        System.out.println(dnsCache.resolve("google.com"));

        Thread.sleep(310 * 1000);   // Wait for TTL expiration

        System.out.println(dnsCache.resolve("google.com"));

        System.out.println(dnsCache.getCacheStats());

        dnsCache.shutdown();
    }
}
