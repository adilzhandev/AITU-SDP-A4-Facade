
package facade;

import model.Order;
import model.OrderStatus;
import subsystem.InventoryService;
import subsystem.PaymentProcessor;
import subsystem.ShippingService;
import subsystem.NotificationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OrderFacade {

    private final InventoryService inventory;
    private final PaymentProcessor payment;
    private final ShippingService shipping;
    private final NotificationService notification;

    private final Map<Integer, Order> orders = new HashMap<>();

    public OrderFacade() {
        this(
                new InventoryService(),
                new PaymentProcessor(),
                new ShippingService(),
                new NotificationService()
        );
    }

    public OrderFacade(
            InventoryService inventory,
            PaymentProcessor payment,
            ShippingService shipping,
            NotificationService notification
    ) {
        this.inventory = Objects.requireNonNull(inventory);
        this.payment = Objects.requireNonNull(payment);
        this.shipping = Objects.requireNonNull(shipping);
        this.notification = Objects.requireNonNull(notification);
    }

    public boolean placeOrder(Order order) {
        Objects.requireNonNull(order, "Order cannot be null");

        if (order.getStatus() != OrderStatus.CREATED) {
            return false;
        }

        int orderId = order.getId();

        if (orders.containsKey(orderId)) {
            return false;
        }

        int productId = order.getProduct().getId();
        int quantity = order.getQuantity();

        boolean stockReserved = false;
        boolean paymentCompleted = false;
        boolean shipmentCreated = false;

        try {
            stockReserved = inventory.reserveItem(productId, quantity);

            if (!stockReserved) {
                order.setStatus(OrderStatus.FAILED);
                return false;
            }

            double totalAmount =
                    order.getProduct().getPrice() * quantity;

            paymentCompleted =
                    payment.processPayment(orderId, totalAmount);

            if (!paymentCompleted) {
                rollbackOrder(
                        order,
                        stockReserved,
                        paymentCompleted,
                        shipmentCreated
                );
                return false;
            }

            shipmentCreated = shipping.createShipment(orderId);

            if (!shipmentCreated) {
                rollbackOrder(
                        order,
                        stockReserved,
                        paymentCompleted,
                        shipmentCreated
                );
                return false;
            }

            order.setStatus(OrderStatus.CONFIRMED);
            orders.put(orderId, order);

        } catch (RuntimeException exception) {
            System.out.println(
                    "Order processing failed: " + exception.getMessage()
            );

            rollbackOrder(
                    order,
                    stockReserved,
                    paymentCompleted || payment.getPaymentStatus(orderId),
                    shipmentCreated || shipping.isShipped(orderId)
            );

            return false;
        }

        sendNotificationSafely(
                order,
                "Your order #" + orderId + " has been confirmed!"
        );

        return true;
    }

    private void rollbackOrder(
            Order order,
            boolean stockReserved,
            boolean paymentCompleted,
            boolean shipmentCreated
    ) {
        int orderId = order.getId();

        if (shipmentCreated) {
            try {
                if (!shipping.cancelShipment(orderId)) {
                    System.out.println(
                            "Shipment rollback failed for order: " + orderId
                    );
                }
            } catch (RuntimeException exception) {
                System.out.println(
                        "Shipment rollback error: " + exception.getMessage()
                );
            }
        }

        if (paymentCompleted) {
            try {
                if (!payment.refundPayment(orderId)) {
                    System.out.println(
                            "Payment rollback failed for order: " + orderId
                    );
                }
            } catch (RuntimeException exception) {
                System.out.println(
                        "Payment rollback error: " + exception.getMessage()
                );
            }
        }

        if (stockReserved) {
            try {
                inventory.releaseItem(
                        order.getProduct().getId(),
                        order.getQuantity()
                );
            } catch (RuntimeException exception) {
                System.out.println(
                        "Inventory rollback error: " + exception.getMessage()
                );
            }
        }

        order.setStatus(OrderStatus.FAILED);
    }

    public boolean cancelOrder(int orderId) {
        Order order = orders.get(orderId);

        if (order == null) {
            return false;
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            return false;
        }

        try {
            if (!shipping.cancelShipment(orderId)) {
                return false;
            }

            if (!payment.refundPayment(orderId)) {
                System.out.println(
                        "Refund failed for order: " + orderId
                );
                return false;
            }

            inventory.releaseItem(
                    order.getProduct().getId(),
                    order.getQuantity()
            );

            order.setStatus(OrderStatus.CANCELLED);

        } catch (RuntimeException exception) {
            System.out.println(
                    "Order cancellation failed: " + exception.getMessage()
            );
            return false;
        }

        sendNotificationSafely(
                order,
                "Your order #" + orderId + " has been cancelled."
        );

        return true;
    }

    public void addStock(int productId, int quantity) {
        inventory.addStock(productId, quantity);
    }

    public int getAvailableStock(int productId) {
        return inventory.getAvailableStock(productId);
    }

    public int getNotificationCount() {
        return notification.getNotificationHistory().size();
    }

    public Order getOrder(int orderId) {
        return orders.get(orderId);
    }

    private void sendNotificationSafely(Order order, String message) {
        try {
            notification.sendNotification(
                    order.getCustomer(),
                    message
            );
        } catch (RuntimeException exception) {
            System.out.println(
                    "Notification failed: " + exception.getMessage()
            );
        }
    }
}
