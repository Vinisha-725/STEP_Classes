import java.util.*;

public class PlagiarismDetector {

    private final int N_GRAM_SIZE = 5; // 5-grams recommended
    private final Map<String, Set<String>> ngramIndex = new HashMap<>();

    // --------------------------
    // Extract n-grams from text
    // --------------------------
    public List<String> extractNGrams(String text) {
        String[] words = text.toLowerCase().split("\\s+");
        List<String> ngrams = new ArrayList<>();

        for (int i = 0; i <= words.length - N_GRAM_SIZE; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < N_GRAM_SIZE; j++) {
                sb.append(words[i + j]).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }

        return ngrams;
    }

    // ---------------------------------------------
    // Add a document to the n-gram → document index
    // ---------------------------------------------
    public void indexDocument(String docId, String text) {
        List<String> ngrams = extractNGrams(text);
        for (String ng : ngrams) {
            ngramIndex.computeIfAbsent(ng, k -> new HashSet<>()).add(docId);
        }
    }

    // --------------------------------------------------------
    // Analyze a new document and compute similarity with others
    // --------------------------------------------------------
    public Map<String, Double> analyzeDocument(String docId, String text) {
        List<String> ngrams = extractNGrams(text);
        Map<String, Integer> matchCount = new HashMap<>();

        for (String ng : ngrams) {
            if (ngramIndex.containsKey(ng)) {
                for (String matchedDoc : ngramIndex.get(ng)) {
                    matchCount.put(matchedDoc, matchCount.getOrDefault(matchedDoc, 0) + 1);
                }
            }
        }

        // similarity % = matching_ngrams / total_ngrams_in_query_doc
        Map<String, Double> similarityScores = new HashMap<>();
        for (String otherDoc : matchCount.keySet()) {
            double similarity = (matchCount.get(otherDoc) * 100.0) / ngrams.size();
            similarityScores.put(otherDoc, similarity);
        }

        return similarityScores;
    }

    // ----------------------------
    // Pretty print similarity data
    // ----------------------------
    public void printSimilarityReport(String docId, String text) {
        List<String> ngrams = extractNGrams(text);
        System.out.println("→ Extracted " + ngrams.size() + " n-grams");

        Map<String, Double> scores = analyzeDocument(docId, text);

        scores.entrySet()
                .stream()
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    String target = entry.getKey();
                    double percent = entry.getValue();
                    System.out.printf("→ Similarity with %s: %.2f%%\n", target, percent);

                    if (percent > 60) {
                        System.out.println("   ⚠ PLAGIARISM DETECTED");
                    } else if (percent > 15) {
                        System.out.println("   ⚠ Suspicious similarity");
                    }
                });
    }

    // --------------------------------
    // Demo
    // --------------------------------
    public static void main(String[] args) {
        PlagiarismDetector pd = new PlagiarismDetector();

        // Existing documents in database
        pd.indexDocument("essay_089.txt",
                "Machine learning is a field of computer science that focuses on algorithms that learn from data.");

        pd.indexDocument("essay_092.txt",
                "Machine learning is a core subfield of artificial intelligence. It focuses on algorithms that can learn patterns from data.");

        // New document to test
        String newEssay =
                "Machine learning is a field of AI. It focuses heavily on algorithms that can learn patterns from data effectively.";

        pd.printSimilarityReport("essay_123.txt", newEssay);
    }
}
