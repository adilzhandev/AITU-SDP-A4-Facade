package subsystem;

import java.util.HashMap;
import java.util.Map;

public class InventoryService {
    private final Map<Integer, Integer> stock = new HashMap<>();

    public void addStock(int productId, int quantity) {
        validateQuantity(quantity);

        int currentStock = stock.getOrDefault(productId, 0);
        stock.put(productId, currentStock + quantity);
    }

    public int getAvailableStock(int productId) {
        return stock.getOrDefault(productId, 0);
    }

    public boolean reserveItem(int productId, int quantity) {
        validateQuantity(quantity);

        int availableStock = getAvailableStock(productId);

        if (availableStock < quantity) {
            return false;
        }

        stock.put(productId, availableStock - quantity);
        return true;
    }

    public void releaseItem(int productId, int quantity) {
        validateQuantity(quantity);

        int currentStock = getAvailableStock(productId);
        stock.put(productId, currentStock + quantity);
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }
}