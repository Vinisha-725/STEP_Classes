import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UsernameC {

    // Thread-safe set of taken usernames
    private final Set<String> usernameSet = ConcurrentHashMap.newKeySet();

    // Tracks how many times a username was checked
    private final ConcurrentHashMap<String, AtomicInteger> attemptCount = new ConcurrentHashMap<>();

    // Keep most attempted username and its count
    private volatile String mostAttemptedUsername = null;
    private volatile int maxAttempts = 0;

    public UsernameChecker(Collection<String> existingUsernames) {
        usernameSet.addAll(existingUsernames);
    }

    /**
     * Checks if a username is available.
     * @param username Username to check
     * @return true if available, false if taken
     */
    public boolean checkAvailability(String username) {
        // Record attempt
        int currentCount = attemptCount
                .computeIfAbsent(username, k -> new AtomicInteger(0))
                .incrementAndGet();

        // Update most attempted
        updateMostAttempted(username, currentCount);

        return !usernameSet.contains(username);
    }

    private synchronized void updateMostAttempted(String username, int count) {
        if (count > maxAttempts) {
            maxAttempts = count;
            mostAttemptedUsername = username;
        }
    }

    /**
     * Suggest alternative usernames if original is taken.
     * @param username The taken username
     * @param limit Number of suggestions to return
     * @return List of suggested available usernames
     */
    public List<String> suggestAlternatives(String username, int limit) {
        List<String> suggestions = new ArrayList<>();
        int counter = 1;

        // Try appending numbers
        while (suggestions.size() < limit) {
            String candidate = username + counter++;
            if (!usernameSet.contains(candidate)) {
                suggestions.add(candidate);
            }
        }
        return suggestions;
    }

    /**
     * Returns the most attempted username.
     */
    public String getMostAttempted() {
        return mostAttemptedUsername + " (" + maxAttempts + " attempts)";
    }

    /**
     * Simulates registering a username (marks it as taken).
     */
    public void registerUsername(String username) {
        usernameSet.add(username.toLowerCase());
    }

    public static void main(String[] args) {
        // Example existing usernames
        List<String> initialUsers = Arrays.asList("john_doe", "admin", "alice123");
        UsernameChecker checker = new UsernameChecker(initialUsers);

        // Simulate checks
        System.out.println("john_doe available? " + checker.checkAvailability("john_doe"));
        System.out.println("jane_smith available? " + checker.checkAvailability("jane_smith"));

        // Suggest alternatives
        System.out.println("Suggestions for john_doe: " +
                checker.suggestAlternatives("john_doe", 5));

        // Simulate multiple attempts on "admin"
        for (int i = 0; i < 1500; i++) {
            checker.checkAvailability("admin");
        }

        System.out.println("Most attempted: " + checker.getMostAttempted());
    }
}
