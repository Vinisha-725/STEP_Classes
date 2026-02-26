import java.util.*;
import java.util.concurrent.*;

public class RealTimeAnalytics {

    // Total page views per URL
    private final Map<String, Integer> pageViewCount = new ConcurrentHashMap<>();

    // Unique visitors per URL
    private final Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // Traffic source counts
    private final Map<String, Integer> trafficSources = new ConcurrentHashMap<>();

    // Scheduled executor for 5-second dashboard update
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public RealTimeAnalytics() {
        // Run dashboard refresh every 5 seconds
        scheduler.scheduleAtFixedRate(this::printDashboard, 5, 5, TimeUnit.SECONDS);
    }

    // ------------------------------
    // Incoming event processing
    // ------------------------------
    public void processEvent(PageViewEvent event) {

        // Increment total page views
        pageViewCount.merge(event.url, 1, Integer::sum);

        // Track unique visitors
        uniqueVisitors.computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet())
                      .add(event.userId);

        // Track traffic source
        trafficSources.merge(event.source, 1, Integer::sum);
    }

    // ------------------------------
    // Build Top 10 Pages (views)
    // ------------------------------
    private List<Map.Entry<String, Integer>> getTopPages() {
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : pageViewCount.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> b.getValue() - a.getValue()); // descending
        return result;
    }

    // -------------------------------
    // Dashboard print (every 5 sec)
    // -------------------------------
    public void printDashboard() {
        System.out.println("\n========== REAL-TIME DASHBOARD ==========");

        // TOP PAGES
        System.out.println("Top Pages:");
        List<Map.Entry<String, Integer>> topPages = getTopPages();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : topPages) {
            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
            System.out.printf("%d. %s - %d views (%d unique)\n", rank++, url, views, unique);
        }

        // TRAFFIC SOURCES
        System.out.println("\nTraffic Sources:");
        int totalSourceCount = trafficSources.values().stream().mapToInt(i -> i).sum();

        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            double percent = (entry.getValue() * 100.0) / totalSourceCount;
            System.out.printf("%s: %.1f%%\n", entry.getKey(), percent);
        }

        System.out.println("==========================================\n");
    }

    // ----------------------------------
    // Shutdown scheduler when needed
    // ----------------------------------
    public void shutdown() {
        scheduler.shutdown();
    }

    // Event class for incoming page views
    static class PageViewEvent {
        String url;
        String userId;
        String source;

        public PageViewEvent(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    // ----------------------------------
    // Demo Simulation
    // ----------------------------------
    public static void main(String[] args) throws InterruptedException {
        RealTimeAnalytics analytics = new RealTimeAnalytics();

        // Simulate streaming events
        Random rand = new Random();
        String[] urls = {
                "/article/breaking-news",
                "/sports/championship",
                "/tech/new-gadget",
                "/world/politics"
        };

        String[] sources = {"google", "facebook", "direct", "twitter"};

        // Simulate continuous events
        for (int i = 0; i < 5000; i++) {
            String url = urls[rand.nextInt(urls.length)];
            String user = "user_" + rand.nextInt(2000);
            String source = sources[rand.nextInt(sources.length)];

            analytics.processEvent(new PageViewEvent(url, user, source));
            Thread.sleep(1); // simulate stream delay
        }

        analytics.shutdown();
    }
}
