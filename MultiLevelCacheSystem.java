import java.util.*;

public class MultiLevelCacheSystem {

    // -------------------------------
    // Video object
    // -------------------------------
    static class VideoData {
        String videoId;
        String content;  // Simplified video payload
        long lastUpdated;

        VideoData(String videoId, String content) {
            this.videoId = videoId;
            this.content = content;
            this.lastUpdated = System.currentTimeMillis();
        }
    }

    // --------------------------------------------
    // L1 Cache (In-memory, max 10,000 videos, LRU)
    // --------------------------------------------
    private final int L1_CAPACITY = 10_000;

    private final LinkedHashMap<String, VideoData> L1Cache =
            new LinkedHashMap<String, VideoData>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                    return size() > L1_CAPACITY;
                }
            };

    // ------------------------------------------------------
    // L2 Cache (SSD-backed, represented as file paths here)
    // LinkedHashMap for LRU + HashMap for fast lookup
    // ------------------------------------------------------
    private final int L2_CAPACITY = 100_000;

    private final LinkedHashMap<String, String> L2Cache =
            new LinkedHashMap<String, String>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > L2_CAPACITY;
                }
            };

    // Simulated SSD storage mapping videoId → VideoData
    private final Map<String, VideoData> SSDStorage = new HashMap<>();

    // --------------------------------------------
    // L3 Database simulation (slow)
    // --------------------------------------------
    private final Map<String, VideoData> L3Database = new HashMap<>();

    // ---------------------------------------------
    // Access Count (for promotion L2 → L1)
    // ---------------------------------------------
    private final Map<String, Integer> accessCount = new HashMap<>();

    private final int PROMOTION_THRESHOLD = 5; // Move to L1 after N accesses

    // ---------------------------------------------
    // Statistics
    // ---------------------------------------------
    private long L1Hits = 0, L1Miss = 0;
    private long L2Hits = 0, L2Miss = 0;
    private long L3Hits = 0;
    private long totalRequests = 0;

    private double L1Time = 0.5;  // milliseconds
    private double L2Time = 5.0;
    private double L3Time = 150.0;

    // ---------------------------------------------
    // Put initial data into L3 database
    // ---------------------------------------------
    public void addToDatabase(String videoId, String content) {
        L3Database.put(videoId, new VideoData(videoId, content));
    }

    // ---------------------------------------------
    // Get video (L1 → L2 → L3)
    // ---------------------------------------------
    public VideoData getVideo(String videoId) {
        totalRequests++;

        // L1 check
        if (L1Cache.containsKey(videoId)) {
            L1Hits++;
            incrementAccess(videoId);
            return L1Cache.get(videoId);
        }
        L1Miss++;

        // L2 check
        if (L2Cache.containsKey(videoId)) {
            L2Hits++;

            // Read from SSD storage
            VideoData video = SSDStorage.get(videoId);

            // Promotion logic: promote to L1
            incrementAccess(videoId);
            if (accessCount.get(videoId) >= PROMOTION_THRESHOLD) {
                promoteToL1(videoId, video);
            }
            return video;
        }
        L2Miss++;

        // L3 database check (slow)
        if (L3Database.containsKey(videoId)) {
            L3Hits++;
            VideoData video = L3Database.get(videoId);

            // Add to L2
            addToL2(video);

            incrementAccess(videoId);
            return video;
        }

        return null; // Not found anywhere
    }

    // ---------------------------------------------
    // Track access count
    // ---------------------------------------------
    private void incrementAccess(String videoId) {
        accessCount.merge(videoId, 1, Integer::sum);
    }

    // ---------------------------------------------
    // Promote to L1 (remove from L2)
    // ---------------------------------------------
    private void promoteToL1(String videoId, VideoData video) {
        L2Cache.remove(videoId);
        SSDStorage.remove(videoId);

        L1Cache.put(videoId, video);
    }

    // ---------------------------------------------
    // Add item to L2 cache
    // ---------------------------------------------
    private void addToL2(VideoData video) {
        L2Cache.put(video.videoId, "ssd://" + video.videoId);
        SSDStorage.put(video.videoId, video);
    }

    // ---------------------------------------------
    // Invalidate video across all caches
    // ---------------------------------------------
    public void invalidate(String videoId) {
        L1Cache.remove(videoId);
        L2Cache.remove(videoId);
        SSDStorage.remove(videoId);
        L3Database.remove(videoId);
        accessCount.remove(videoId);
    }

    // ---------------------------------------------
    // Dashboard statistics
    // ---------------------------------------------
    public void getStatistics() {
        System.out.println("\n========== CACHE STATISTICS ==========");

        double L1HitRate = (L1Hits * 100.0) / Math.max(1, totalRequests);
        double L2HitRate = (L2Hits * 100.0) / Math.max(1, totalRequests);
        double L3HitRate = (L3Hits * 100.0) / Math.max(1, totalRequests);

        double avgTime = (L1Hits * L1Time + L2Hits * L2Time + L3Hits * L3Time)
                / Math.max(1, totalRequests);

        System.out.printf("L1: Hit Rate %.1f%%, Avg Time: %.2f ms\n", L1HitRate, L1Time);
        System.out.printf("L2: Hit Rate %.1f%%, Avg Time: %.2f ms\n", L2HitRate, L2Time);
        System.out.printf("L3: Hit Rate %.1f%%, Avg Time: %.2f ms\n", L3HitRate, L3Time);
        System.out.printf("Overall: Hit Rate %.1f%%, Average Response Time: %.2f ms\n",
                (L1HitRate + L2HitRate + L3HitRate), avgTime);

        System.out.println("======================================\n");
    }

    // ---------------------------------------------
    // DEMO
    // ---------------------------------------------
    public static void main(String[] args) {
        MultiLevelCacheSystem cache = new MultiLevelCacheSystem();

        // Populate L3 (database)
        cache.addToDatabase("video_123", "Video content A");
        cache.addToDatabase("video_999", "Video content B");

        // Requests
        System.out.println("Request #1: video_123");
        cache.getVideo("video_123");  // L1 miss → L2 miss → L3 hit → add to L2

        System.out.println("\nRequest #2: video_123");
        cache.getVideo("video_123");  // L2 hit → maybe promote to L1

        // Cause more accesses to promote to L1
        for (int i = 0; i < 5; i++) cache.getVideo("video_123");

        System.out.println("\nRequest: video_999");
        cache.getVideo("video_999");  // L1 miss → L2 miss → L3 hit

        cache.getStatistics();
    }
}
