package shop.service;

import java.util.HashMap;
import java.util.Map;

public class InventoryService {
    private final Map<String, Long> prices;
    private final Map<String, Integer> stock;
    private final Map<String, Map<String, Integer>> reservations = new HashMap<>();

    public InventoryService(Map<String, Long> prices, Map<String, Integer> stock) {
        if (!prices.keySet().equals(stock.keySet())) {
            throw new IllegalArgumentException("Prices and stock must contain the same products");
        }
        for (String product : prices.keySet()) {
            if (product == null || product.isBlank() || prices.get(product) == null
                    || prices.get(product) <= 0 || stock.get(product) == null || stock.get(product) < 0) {
                throw new IllegalArgumentException("Invalid catalog entry: " + product);
            }
        }
        this.prices = Map.copyOf(prices);
        this.stock = new HashMap<>(stock);
    }

    public long reserve(String orderId, Map<String, Integer> items) {
        if (orderId == null || orderId.isBlank() || reservations.containsKey(orderId)) {
            throw new IllegalArgumentException("Order ID must be new and non-empty");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cart must not be empty");
        }
        Map<String, Integer> requestedItems = Map.copyOf(items);
        long totalCents = 0;
        for (Map.Entry<String, Integer> item : requestedItems.entrySet()) {
            String product = item.getKey();
            int quantity = item.getValue();
            if (!prices.containsKey(product)) {
                throw new IllegalArgumentException("Unknown product: " + product);
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + product);
            }
            if (stock.get(product) < quantity) {
                throw new IllegalStateException("Not enough stock: " + product);
            }
            long itemTotal = Math.multiplyExact(prices.get(product), quantity);
            totalCents = Math.addExact(totalCents, itemTotal);
        }
        requestedItems.forEach((product, quantity) -> stock.put(product, stock.get(product) - quantity));
        reservations.put(orderId, requestedItems);
        return totalCents;
    }

    public void release(String orderId) {
        Map<String, Integer> items = reservations.remove(orderId);
        if (items != null) {
            items.forEach((product, quantity) -> stock.put(product, stock.get(product) + quantity));
        }
    }

    public int getAvailableQuantity(String product) {
        if (!stock.containsKey(product)) {
            throw new IllegalArgumentException("Unknown product: " + product);
        }
        return stock.get(product);
    }
}
