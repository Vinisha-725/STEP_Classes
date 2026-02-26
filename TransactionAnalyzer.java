import java.util.*;

public class TransactionAnalyzer {

    // -----------------------------
    // Transaction Model
    // -----------------------------
    static class Transaction {
        int id;
        int amount;
        String merchant;
        String account;
        long timestamp; // epoch millis

        Transaction(int id, int amount, String merchant, String account, long timestamp) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.timestamp = timestamp;
        }
    }

    private List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    // --------------------------------------------------
    // 1. CLASSIC TWO SUM (O(n))
    // --------------------------------------------------
    public List<int[]> findTwoSum(int target) {
        List<int[]> result = new ArrayList<>();
        Map<Integer, Transaction> seen = new HashMap<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;

            if (seen.containsKey(t.amount)) { 
                result.add(new int[]{seen.get(t.amount).id, t.id});
            }
            seen.put(complement, t);
        }

        return result;
    }

    // --------------------------------------------------
    // 2. TWO SUM WITH 1-HOUR TIME WINDOW
    // --------------------------------------------------
    public List<int[]> findTwoSumWithin1Hour(int target) {
        long ONE_HOUR = 3600_000L;
        List<int[]> result = new ArrayList<>();

        // Sort by timestamp for efficient window filtering
        transactions.sort(Comparator.comparingLong(t -> t.timestamp));

        Map<Integer, List<Transaction>> windowMap = new HashMap<>();
        int left = 0;

        for (int right = 0; right < transactions.size(); right++) {
            Transaction curr = transactions.get(right);

            // Shrink the sliding window
            while (curr.timestamp - transactions.get(left).timestamp > ONE_HOUR) {
                Transaction old = transactions.get(left);
                windowMap.get(old.amount).remove(old);
                left++;
            }

            int complement = target - curr.amount;

            if (windowMap.containsKey(complement)) {
                for (Transaction match : windowMap.get(complement)) {
                    result.add(new int[]{match.id, curr.id});
                }
            }

            // Insert into window map
            windowMap.computeIfAbsent(curr.amount, k -> new ArrayList<>()).add(curr);
        }

        return result;
    }

    // --------------------------------------------------
    // 3. K-SUM (Generalized)
    // --------------------------------------------------
    public List<List<Integer>> findKSum(int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        int[] arr = transactions.stream().mapToInt(t -> t.amount).toArray();
        int[] ids = transactions.stream().mapToInt(t -> t.id).toArray();

        Arrays.sort(arr);
        Arrays.sort(ids);

        backtrackKSum(arr, ids, k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private void backtrackKSum(int[] arr, int[] ids, int k, int target, int start,
                               List<Integer> temp, List<List<Integer>> result) {

        if (k == 2) {  
            // Classic two-pointer
            int left = start, right = arr.length - 1;
            while (left < right) {
                int sum = arr[left] + arr[right];
                if (sum == target) {
                    result.add(Arrays.asList(ids[left], ids[right]));
                    left++; right--;
                } else if (sum < target) left++;
                else right--;
            }
            return;
        }

        for (int i = start; i < arr.length - k + 1; i++) {
            temp.add(ids[i]);
            backtrackKSum(arr, ids, k-1, target - arr[i], i + 1, temp, result);
            temp.remove(temp.size() - 1);
        }
    }

    // --------------------------------------------------
    // 4. DUPLICATE DETECTION (Same amount, same merchant)
    // --------------------------------------------------
    public Map<String, Set<String>> detectDuplicates() {
        Map<String, Map<Integer, Set<String>>> duplicateMap = new HashMap<>();

        for (Transaction t : transactions) {
            duplicateMap
                .computeIfAbsent(t.merchant, m -> new HashMap<>())
                .computeIfAbsent(t.amount, a -> new HashSet<>())
                .add(t.account);
        }

        // Only keep duplicates (>=2 accounts)
        Map<String, Set<String>> result = new HashMap<>();
        for (String merchant : duplicateMap.keySet()) {
            for (int amount : duplicateMap.get(merchant).keySet()) {
                Set<String> accounts = duplicateMap.get(merchant).get(amount);
                if (accounts.size() > 1) {
                    result.put(merchant + " | $" + amount, accounts);
                }
            }
        }

        return result;
    }

    // --------------------------------------------------
    // SAMPLE DEMO
    // --------------------------------------------------
    public static void main(String[] args) {
        TransactionAnalyzer analyzer = new TransactionAnalyzer();

        analyzer.addTransaction(new Transaction(1, 500, "Store A", "acc1", time("10:00")));
        analyzer.addTransaction(new Transaction(2, 300, "Store B", "acc2", time("10:15")));
        analyzer.addTransaction(new Transaction(3, 200, "Store C", "acc3", time("10:30")));
        analyzer.addTransaction(new Transaction(4, 500, "Store A", "acc2", time("11:00"))); // duplicate

        // Classic Two Sum
        System.out.println("TwoSum 500: ");
        for (int[] p : analyzer.findTwoSum(500)) {
            System.out.println(Arrays.toString(p));
        }

        // Duplicate Detection
        System.out.println("\nDuplicates:");
        System.out.println(analyzer.detectDuplicates());

        // K-Sum
        System.out.println("\nK-Sum k=3 target=1000:");
        System.out.println(analyzer.findKSum(3, 1000));
    }

    private static long time(String hhmm) {
        try {
            return new java.text.SimpleDateFormat("HH:mm").parse(hhmm).getTime();
        } catch (Exception e) {
            return 0;
        }
    }
}
