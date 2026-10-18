package shop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import shop.model.Customer;
import shop.model.Order;
import shop.service.InventoryService;
import shop.service.NotificationService;
import shop.service.PaymentProcessor;
import shop.service.ShippingService;

public class OrderFacade {
    private final InventoryService inventory;
    private final PaymentProcessor payments;
    private final ShippingService shipping;
    private final NotificationService notifications;
    private final Map<String, Order> orders = new HashMap<>();

    public OrderFacade(Map<String, Long> prices, Map<String, Integer> stock,
                       Map<String, Long> customerBalances) {
        this(new InventoryService(prices, stock), new PaymentProcessor(customerBalances),
                new ShippingService(), new NotificationService());
    }

    OrderFacade(InventoryService inventory, PaymentProcessor payments,
                ShippingService shipping, NotificationService notifications) {
        this.inventory = inventory;
        this.payments = payments;
        this.shipping = shipping;
        this.notifications = notifications;
    }

    public Order placeOrder(Map<String, Integer> cart, Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cart must not be empty");
        }
        Map<String, Integer> items = Map.copyOf(cart);
        String orderId = UUID.randomUUID().toString();
        long totalCents = inventory.reserve(orderId, items);
        try {
            payments.charge(orderId, customer.email(), totalCents);
            shipping.ship(orderId, customer.address());
            notifications.send(customer.email(), "Order " + orderId + " placed");
            Order order = new Order(orderId, customer, items, totalCents, Order.Status.PLACED);
            orders.put(orderId, order);
            return order;
        } catch (RuntimeException failure) {
            shipping.cancel(orderId);
            payments.refund(orderId);
            inventory.release(orderId);
            throw new IllegalStateException("Could not place order: " + failure.getMessage(), failure);
        }
    }

    public Order cancelOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Unknown order: " + orderId);
        }
        if (order.status() == Order.Status.CANCELLED) {
            return order;
        }
        shipping.cancel(orderId);
        payments.refund(orderId);
        inventory.release(orderId);
        Order cancelledOrder = new Order(order.id(), order.customer(), order.items(),
                order.totalCents(), Order.Status.CANCELLED);
        orders.put(orderId, cancelledOrder);
        notifications.send(order.customer().email(), "Order " + orderId + " cancelled");
        return cancelledOrder;
    }
}
