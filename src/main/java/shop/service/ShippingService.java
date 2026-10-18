package shop.service;

import java.util.HashMap;
import java.util.Map;

public class ShippingService {
    private final Map<String, String> shipments = new HashMap<>();

    public void ship(String orderId, String address) {
        if (orderId == null || orderId.isBlank() || shipments.containsKey(orderId)) {
            throw new IllegalArgumentException("Order ID must be new and non-empty");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Delivery address is required");
        }
        shipments.put(orderId, address);
    }

    public void cancel(String orderId) {
        shipments.remove(orderId);
    }

    public boolean hasShipment(String orderId) {
        return shipments.containsKey(orderId);
    }
}
