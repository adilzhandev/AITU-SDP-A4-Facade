package subsystem;

import java.util.HashMap;
import java.util.Map;

public class ShippingService {
    private final Map<Integer, Boolean> shipments = new HashMap<>();

    public boolean createShipment(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Order ID must be greater than zero"
            );
        }

        if (shipments.containsKey(orderId)) {
            return false;
        }

        shipments.put(orderId, true);

        System.out.println("Shipment created for order: " + orderId);
        return true;
    }

    public boolean cancelShipment(int orderId) {
        if (!Boolean.TRUE.equals(shipments.get(orderId))) {
            return false;
        }

        shipments.put(orderId, false);

        System.out.println("Shipment cancelled for order: " + orderId);
        return true;
    }

    public boolean isShipped(int orderId) {
        return Boolean.TRUE.equals(shipments.get(orderId));
    }
}