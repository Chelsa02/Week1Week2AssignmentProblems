import java.util.*;

public class RealTimeAnalytics {

    // page -> visit count
    private Map<String, Integer> pageViews = new HashMap<>();

    // page -> unique users
    private Map<String, Set<String>> uniqueVisitors = new HashMap<>();

    // traffic source -> count
    private Map<String, Integer> trafficSources = new HashMap<>();

    // Process incoming page view event
    public synchronized void processEvent(String url, String userId, String source) {

        // Update page views
        pageViews.put(url, pageViews.getOrDefault(url, 0) + 1);

        // Track unique visitors
        uniqueVisitors.putIfAbsent(url, new HashSet<>());
        uniqueVisitors.get(url).add(userId);

        // Track traffic sources
        trafficSources.put(source, trafficSources.getOrDefault(source, 0) + 1);
    }

    // Display dashboard
    public synchronized void getDashboard() {

        System.out.println("\n===== REAL-TIME ANALYTICS DASHBOARD =====");

        // Top pages
        List<Map.Entry<String, Integer>> pages =
                new ArrayList<>(pageViews.entrySet());

        pages.sort((a, b) -> b.getValue() - a.getValue());

        System.out.println("\nTop Pages:");

        int limit = Math.min(10, pages.size());

        for (int i = 0; i < limit; i++) {
            String page = pages.get(i).getKey();
            int views = pages.get(i).getValue();
            int unique = uniqueVisitors.get(page).size();

            System.out.println(
                    (i + 1) + ". " + page +
                            " - " + views + " views (" + unique + " unique)"
            );
        }

        // Traffic sources
        System.out.println("\nTraffic Sources:");

        for (String source : trafficSources.keySet()) {
            System.out.println(source + " → " + trafficSources.get(source));
        }

        System.out.println("=========================================");
    }

    public static void main(String[] args) throws Exception {

        RealTimeAnalytics dashboard =
                new RealTimeAnalytics();

        // Simulate incoming events
        dashboard.processEvent("/article/breaking-news", "user_123", "google");
        dashboard.processEvent("/article/breaking-news", "user_456", "facebook");
        dashboard.processEvent("/sports/championship", "user_789", "direct");
        dashboard.processEvent("/sports/championship", "user_101", "google");
        dashboard.processEvent("/sports/championship", "user_102", "google");
        dashboard.processEvent("/tech/ai-future", "user_103", "facebook");

        // Dashboard updates every 5 seconds
        while (true) {

            dashboard.getDashboard();

            Thread.sleep(5000);
        }
    }
}
