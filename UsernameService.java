import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UsernameService {

    // username -> userId (dummy userId for demo)
    private final ConcurrentHashMap<String, Integer> userTable = new ConcurrentHashMap<>();

    // username -> number of attempts
    private final ConcurrentHashMap<String, AtomicInteger> attemptFrequency = new ConcurrentHashMap<>();

    // --- Check if username exists ---
    public boolean checkAvailability(String username) {
        trackAttempt(username);
        return !userTable.containsKey(username);
    }

    // --- Register a new username ---
    public void register(String username, int userId) {
        userTable.put(username, userId);
    }

    // --- Suggest similar alternatives ---
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        // Append numbers
        for (int i = 1; i <= 3; i++) {
            String suggestion = username + i;
            if (!userTable.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        // Replace "_" with "." if applicable
        if (username.contains("_")) {
            String suggestion = username.replace("_", ".");
            if (!userTable.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        return suggestions;
    }

    // --- Track popularity of searched usernames ---
    private void trackAttempt(String username) {
        attemptFrequency
                .computeIfAbsent(username, k -> new AtomicInteger(0))
                .incrementAndGet();
    }

    // --- Return most attempted username ---
    public String getMostAttempted() {
        return attemptFrequency.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().get()))
                .map(Map.Entry::getKey)
                .orElse("No attempts yet");
    }

    // --- Demo / main ---
    public static void main(String[] args) {
        UsernameService service = new UsernameService();

        // Simulate existing users
        service.register("john_doe", 101);
        service.register("admin", 999);

        System.out.println(service.checkAvailability("john_doe")); // false
        System.out.println(service.checkAvailability("jane_smith")); // true
        System.out.println(service.suggestAlternatives("john_doe"));
        
        // simulate admin being queried many times
        for (int i = 0; i < 10543; i++) {
            service.checkAvailability("admin");
        }

        System.out.println("Most attempted: " + service.getMostAttempted()); // admin
    }
}
