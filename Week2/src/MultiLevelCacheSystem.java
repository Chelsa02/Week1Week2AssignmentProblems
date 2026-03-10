import java.util.*;

public class MultiLevelCacheSystem {

    static class Video {
        String videoId;
        String content;

        Video(String videoId, String content) {
            this.videoId = videoId;
            this.content = content;
        }
    }

    // L1 cache: LinkedHashMap with access-order for LRU
    private final int L1_CAPACITY = 10000;
    private LinkedHashMap<String, Video> L1Cache = new LinkedHashMap<>(L1_CAPACITY, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<String, Video> eldest) {
            return size() > L1_CAPACITY;
        }
    };

    // L2 cache: videoId -> simulated SSD file path
    private final int L2_CAPACITY = 100000;
    private HashMap<String, String> L2Cache = new HashMap<>();
    private HashMap<String, Integer> L2AccessCount = new HashMap<>();

    // L3 database: videoId -> Video object
    private HashMap<String, Video> L3Database = new HashMap<>();

    // Hit counters
    private int L1Hits = 0, L2Hits = 0, L3Hits = 0;
    private int L1Requests = 0, L2Requests = 0, L3Requests = 0;

    // Access threshold to promote from L2→L1
    private final int PROMOTION_THRESHOLD = 3;

    public MultiLevelCacheSystem() {
        // Simulate database with 1M videos
        for (int i = 1; i <= 1000; i++) { // smaller for demo
            String vid = "video_" + i;
            L3Database.put(vid, new Video(vid, "Content of " + vid));
        }
    }

    public Video getVideo(String videoId) {

        L1Requests++;
        if (L1Cache.containsKey(videoId)) {
            L1Hits++;
            System.out.println(videoId + " → L1 Cache HIT (~0.5ms)");
            return L1Cache.get(videoId);
        }

        L2Requests++;
        if (L2Cache.containsKey(videoId)) {
            L2Hits++;
            System.out.println(videoId + " → L2 Cache HIT (~5ms)");
            int count = L2AccessCount.getOrDefault(videoId, 0) + 1;
            L2AccessCount.put(videoId, count);

            // Promote to L1 if access count exceeds threshold
            if (count >= PROMOTION_THRESHOLD && L3Database.containsKey(videoId)) {
                L1Cache.put(videoId, L3Database.get(videoId));
                System.out.println("→ Promoted " + videoId + " to L1");
            }

            return L3Database.get(videoId); // simulate reading from SSD
        }

        L3Requests++;
        if (L3Database.containsKey(videoId)) {
            L3Hits++;
            System.out.println(videoId + " → L3 Database HIT (~150ms)");
            // Add to L2
            if (L2Cache.size() >= L2_CAPACITY) {
                // Simple eviction: remove random
                String firstKey = L2Cache.keySet().iterator().next();
                L2Cache.remove(firstKey);
                L2AccessCount.remove(firstKey);
            }
            L2Cache.put(videoId, "SSD_PATH/" + videoId);
            L2AccessCount.put(videoId, 1);
            return L3Database.get(videoId);
        }

        System.out.println(videoId + " → Not found!");
        return null;
    }

    public void getStatistics() {
        double L1HitRate = L1Requests == 0 ? 0 : 100.0 * L1Hits / L1Requests;
        double L2HitRate = L2Requests == 0 ? 0 : 100.0 * L2Hits / L2Requests;
        double L3HitRate = L3Requests == 0 ? 0 : 100.0 * L3Hits / L3Requests;

        double overallRequests = L1Requests + L2Requests + L3Requests;
        double overallHits = L1Hits + L2Hits + L3Hits;
        double overallHitRate = overallRequests == 0 ? 0 : 100.0 * overallHits / overallRequests;

        System.out.println("\nCache Statistics:");
        System.out.printf("L1: Hit Rate %.2f%%\n", L1HitRate);
        System.out.printf("L2: Hit Rate %.2f%%\n", L2HitRate);
        System.out.printf("L3: Hit Rate %.2f%%\n", L3HitRate);
        System.out.printf("Overall: Hit Rate %.2f%%\n", overallHitRate);
    }

    public static void main(String[] args) {

        MultiLevelCacheSystem cacheSystem = new MultiLevelCacheSystem();

        // Demo requests
        cacheSystem.getVideo("video_123");
        cacheSystem.getVideo("video_123"); // L1 should hit second time
        cacheSystem.getVideo("video_999");
        cacheSystem.getVideo("video_500");

        cacheSystem.getStatistics();
    }
}