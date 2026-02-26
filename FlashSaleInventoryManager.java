import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleInventoryManager {

    // productId -> stockCount (atomic for thread-safe decrement)
    private final ConcurrentHashMap<String, AtomicInteger> stockTable = new ConcurrentHashMap<>();

    // productId -> waiting list of users (FIFO)
    private final ConcurrentHashMap<String, LinkedBlockingQueue<Integer>> waitingListTable = new ConcurrentHashMap<>();

    // --- Add product with initial stock ---
    public void addProduct(String productId, int stock) {
        stockTable.put(productId, new AtomicInteger(stock));
        waitingListTable.put(productId, new LinkedBlockingQueue<>());
    }

    // --- Check stock in O(1) ---
    public String checkStock(String productId) {
        AtomicInteger stock = stockTable.get(productId);
        if (stock == null) return "Product not found";

        return stock.get() + " units available";
    }

    // --- Purchase operation with atomic decrement ---
    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockTable.get(productId);

        if (stock == null) {
            return "Product does not exist";
        }

        while (true) {
            int currentStock = stock.get();

            // If out of stock → add user to waiting list
            if (currentStock <= 0) {
                int position = addToWaitingList(productId, userId);
                return "Added to waiting list, position #" + position;
            }

            // Attempt atomic decrement
            boolean updated = stock.compareAndSet(currentStock, currentStock - 1);
            if (updated) {
                return "Success, " + (currentStock - 1) + " units remaining";
            }
            // If CAS failed → another thread bought it, retry loop
        }
    }

    // --- Add user to waiting list ---
    private int addToWaitingList(String productId, int userId) {
        LinkedBlockingQueue<Integer> queue = waitingListTable.get(productId);
        queue.add(userId);
        return queue.size();
    }

    // --- Get next user in waiting list (useful for restocks) ---
    public Integer getNextInWaitingList(String productId) {
        LinkedBlockingQueue<Integer> queue = waitingListTable.get(productId);
        return queue.poll();
    }

    // --- Demo / main ---
    public static void main(String[] args) {
        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        manager.addProduct("IPHONE15_256GB", 100);

        System.out.println(manager.checkStock("IPHONE15_256GB"));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 67890));

        // Simulate selling out
        for (int i = 0; i < 98; i++) {
            manager.purchaseItem("IPHONE15_256GB", 80000 + i);
        }

        // Now out of stock
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99999)); // waiting list
    }
}
