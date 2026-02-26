import java.util.*;

public class AutocompleteSystem {

    private static final int TOP_K = 10;
    private static final int MAX_CACHE_SIZE = 5000;

    // Global frequency map
    private Map<String, Integer> frequencyMap = new HashMap<>();

    // Root of Trie
    private TrieNode root = new TrieNode();

    // LRU cache for popular prefixes
    private Map<String, List<String>> prefixCache =
            new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, List<String>> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            };

    // Trie node class
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();

        // Top K suggestions (min-heap)
        PriorityQueue<Suggestion> topSuggestions =
                new PriorityQueue<>(Comparator.comparingInt(s -> s.frequency));

        boolean isEndOfQuery;
    }

    private static class Suggestion {
        String query;
        int frequency;

        Suggestion(String q, int f) {
            this.query = q;
            this.frequency = f;
        }
    }

    // Insert query into trie and update top suggestions
    private void insertIntoTrie(String query, int frequency) {
        TrieNode current = root;
        for (char c : query.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
            current.topSuggestions.add(new Suggestion(query, frequency));
            if (current.topSuggestions.size() > TOP_K) {
                current.topSuggestions.poll(); // remove lowest frequency
            }
        }
        current.isEndOfQuery = true;
    }

    /** Add or update frequency for a search query */
    public void updateFrequency(String query) {
        int newFreq = frequencyMap.getOrDefault(query, 0) + 1;
        frequencyMap.put(query, newFreq);

        insertIntoTrie(query, newFreq);
        prefixCache.clear(); // invalidate cache
    }

    /** Return top 10 suggestions for prefix */
    public List<String> search(String prefix) {

        // 1. Cache hit
        if (prefixCache.containsKey(prefix)) {
            return prefixCache.get(prefix);
        }

        // 2. Trie lookup
        TrieNode current = root;
        for (char c : prefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                // Perform typo correction
                return prefixCache.computeIfAbsent(prefix,
                        k -> handleTypos(prefix));
            }
            current = current.children.get(c);
        }

        // 3. Extract from heap
        List<Suggestion> list = new ArrayList<>(current.topSuggestions);
        list.sort((a, b) -> Integer.compare(b.frequency, a.frequency));

        List<String> result = new ArrayList<>();
        for (Suggestion s : list) result.add(s.query);

        // 4. Cache result
        prefixCache.put(prefix, result);
        return result;
    }

    /** Handle Levenshtein ≤ 1 typo correction */
    private List<String> handleTypos(String input) {
        List<String> candidates = new ArrayList<>();

        for (String query : frequencyMap.keySet()) {
            if (isLevenshteinOne(input, query)) {
                candidates.add(query);
            }
        }

        candidates.sort((a, b) ->
                Integer.compare(frequencyMap.get(b), frequencyMap.get(a)));

        return candidates.subList(0, Math.min(TOP_K, candidates.size()));
    }

    private boolean isLevenshteinOne(String a, String b) {
        if (Math.abs(a.length() - b.length()) > 1) return false;

        int mismatches = 0;
        int i = 0, j = 0;

        while (i < a.length() && j < b.length()) {
            if (a.charAt(i) != b.charAt(j)) {
                mismatches++;
                if (mismatches > 1) return false;

                if (a.length() > b.length()) i++;
                else if (a.length() < b.length()) j++;
                else { i++; j++; }

            } else {
                i++; j++;
            }
        }

        return true;
    }

    /** For debugging / API exposure */
    public int getFrequency(String query) {
        return frequencyMap.getOrDefault(query, 0);
    }
}
