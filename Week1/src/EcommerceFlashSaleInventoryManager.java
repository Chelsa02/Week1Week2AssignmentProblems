import java.util.*;

public class EcommerceFlashSaleInventoryManager {

    // productId -> stock count
    private Map<String, Integer> stockMap = new HashMap<>();

    // productId -> waiting list (FIFO)
    private Map<String, LinkedHashMap<Integer, Integer>> waitingListMap = new HashMap<>();

    // Initialize product with stock
    public void addProduct(String productId, int stock) {
        stockMap.put(productId, stock);
        waitingListMap.put(productId, new LinkedHashMap<>());
    }

    // Check stock availability
    public int checkStock(String productId) {
        return stockMap.getOrDefault(productId, 0);
    }

    // Purchase item (thread-safe)
    public synchronized String purchaseItem(String productId, int userId) {

        int stock = stockMap.getOrDefault(productId, 0);

        if (stock > 0) {
            stockMap.put(productId, stock - 1);
            return "Success, " + (stock - 1) + " units remaining";
        }

        // Add to waiting list
        LinkedHashMap<Integer, Integer> waitingList = waitingListMap.get(productId);
        int position = waitingList.size() + 1;
        waitingList.put(userId, position);

        return "Added to waiting list, position #" + position;
    }

    // Show waiting list
    public void showWaitingList(String productId) {
        LinkedHashMap<Integer, Integer> waitingList = waitingListMap.get(productId);

        for (Map.Entry<Integer, Integer> entry : waitingList.entrySet()) {
            System.out.println("User " + entry.getKey() + " -> Position " + entry.getValue());
        }
    }

    public static void main(String[] args) {

        EcommerceFlashSaleInventoryManager manager =
                new EcommerceFlashSaleInventoryManager();

        manager.addProduct("IPHONE15_256GB", 100);

        System.out.println("Stock: " + manager.checkStock("IPHONE15_256GB"));

        // Simulate purchases
        for (int i = 1; i <= 102; i++) {
            System.out.println(
                    "User " + i + ": " +
                            manager.purchaseItem("IPHONE15_256GB", i)
            );
        }

        System.out.println("\nWaiting List:");
        manager.showWaitingList("IPHONE15_256GB");
    }
}