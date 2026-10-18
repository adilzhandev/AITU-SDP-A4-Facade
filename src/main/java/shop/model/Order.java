package shop.model;

import java.util.Map;

public record Order(String id, Customer customer, Map<String, Integer> items,
                    long totalCents, Status status) {
    public Order {
        items = Map.copyOf(items);
    }

    public enum Status {
        PLACED,
        CANCELLED
    }
}
