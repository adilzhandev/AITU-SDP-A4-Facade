package shop;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import shop.model.Customer;
import shop.model.Order;
import shop.service.InventoryService;
import shop.service.NotificationService;
import shop.service.PaymentProcessor;
import shop.service.ShippingService;

public class OrderFacadeTest {
    private static final String EMAIL = "customer@example.com";
    private static final Customer CUSTOMER = new Customer("Alex", EMAIL, "10 Main Street");
    private static final Map<String, Long> PRICES = Map.of("keyboard", 7500L, "mouse", 2500L);
    private static final Map<String, Integer> STOCK = Map.of("keyboard", 5, "mouse", 8);
    private static int passed;

    public static void main(String[] args) {
        run("Successful order", OrderFacadeTest::successfulOrder);
        run("Payment failure releases stock", OrderFacadeTest::paymentFailure);
        run("Shipping failure refunds payment", OrderFacadeTest::shippingFailure);
        run("Notification failure cancels shipment", OrderFacadeTest::notificationFailure);
        run("Inventory reservation is atomic", OrderFacadeTest::atomicReservation);
        run("Cancellation restores resources once", OrderFacadeTest::cancellation);
        run("Unknown order is rejected", OrderFacadeTest::unknownOrder);
        run("Cart snapshot is immutable", OrderFacadeTest::cartSnapshot);
        run("Invalid carts are rejected", OrderFacadeTest::invalidCarts);
        run("Invalid customers are rejected", OrderFacadeTest::invalidCustomers);
        run("Subsystems work independently", OrderFacadeTest::independentSubsystems);
        run("Public facade constructor works", OrderFacadeTest::publicConstructor);
        System.out.println("Passed " + passed + " tests.");
    }

    private static void successfulOrder() {
        TestShop shop = createShop(50000L, new ShippingService(), new NotificationService());
        Order order = shop.facade().placeOrder(Map.of("keyboard", 1, "mouse", 2), CUSTOMER);

        equal(Order.Status.PLACED, order.status());
        equal(12500L, order.totalCents());
        equal(CUSTOMER, order.customer());
        equal(4, shop.inventory().getAvailableQuantity("keyboard"));
        equal(6, shop.inventory().getAvailableQuantity("mouse"));
        equal(37500L, shop.payment().getBalance(EMAIL));
        check(shop.shipping().hasShipment(order.id()), "Shipment should exist");
        equal(1, shop.notifications().getMessages().size());
    }

    private static void paymentFailure() {
        TestShop shop = createShop(1000L, new ShippingService(), new NotificationService());

        expect(IllegalStateException.class,
                () -> shop.facade().placeOrder(Map.of("keyboard", 1), CUSTOMER));

        equal(5, shop.inventory().getAvailableQuantity("keyboard"));
        equal(1000L, shop.payment().getBalance(EMAIL));
        equal(0, shop.notifications().getMessages().size());
    }

    private static void shippingFailure() {
        IllegalStateException failure = new IllegalStateException("Shipping unavailable");
        ShippingService shipping = new ShippingService() {
            @Override
            public void ship(String orderId, String address) {
                throw failure;
            }
        };
        TestShop shop = createShop(50000L, shipping, new NotificationService());

        IllegalStateException result = expect(IllegalStateException.class,
                () -> shop.facade().placeOrder(Map.of("keyboard", 1), CUSTOMER));

        equal(failure, result.getCause());
        equal(5, shop.inventory().getAvailableQuantity("keyboard"));
        equal(50000L, shop.payment().getBalance(EMAIL));
        equal(0, shop.notifications().getMessages().size());
    }

    private static void notificationFailure() {
        TrackingShippingService shipping = new TrackingShippingService();
        IllegalStateException failure = new IllegalStateException("Notifications unavailable");
        NotificationService notifications = new NotificationService() {
            @Override
            public void send(String email, String message) {
                throw failure;
            }
        };
        TestShop shop = createShop(50000L, shipping, notifications);

        IllegalStateException result = expect(IllegalStateException.class,
                () -> shop.facade().placeOrder(Map.of("keyboard", 1), CUSTOMER));

        equal(failure, result.getCause());
        check(shipping.lastOrderId != null, "Shipping should run before notification");
        check(!shipping.hasShipment(shipping.lastOrderId), "Shipment should be cancelled");
        equal(5, shop.inventory().getAvailableQuantity("keyboard"));
        equal(50000L, shop.payment().getBalance(EMAIL));
        expect(IllegalArgumentException.class, () -> shop.facade().cancelOrder(shipping.lastOrderId));
    }

    private static void atomicReservation() {
        InventoryService inventory = new InventoryService(PRICES, STOCK);
        Map<String, Integer> cart = new LinkedHashMap<>();
        cart.put("keyboard", 1);
        cart.put("mouse", 9);

        expect(RuntimeException.class, () -> inventory.reserve("too-large", cart));

        equal(5, inventory.getAvailableQuantity("keyboard"));
        equal(8, inventory.getAvailableQuantity("mouse"));
    }

    private static void cancellation() {
        TestShop shop = createShop(50000L, new ShippingService(), new NotificationService());
        Order placed = shop.facade().placeOrder(Map.of("keyboard", 1), CUSTOMER);

        Order cancelled = shop.facade().cancelOrder(placed.id());
        Order cancelledAgain = shop.facade().cancelOrder(placed.id());

        equal(Order.Status.CANCELLED, cancelled.status());
        equal(placed.id(), cancelled.id());
        equal(cancelled, cancelledAgain);
        equal(5, shop.inventory().getAvailableQuantity("keyboard"));
        equal(50000L, shop.payment().getBalance(EMAIL));
        check(!shop.shipping().hasShipment(placed.id()), "Cancelled shipment should be removed");
        equal(2, shop.notifications().getMessages().size());
    }

    private static void unknownOrder() {
        TestShop shop = createShop(50000L, new ShippingService(), new NotificationService());
        expect(IllegalArgumentException.class, () -> shop.facade().cancelOrder("unknown"));
        equal(5, shop.inventory().getAvailableQuantity("keyboard"));
        equal(50000L, shop.payment().getBalance(EMAIL));
    }

    private static void cartSnapshot() {
        TestShop shop = createShop(50000L, new ShippingService(), new NotificationService());
        Map<String, Integer> cart = new LinkedHashMap<>();
        cart.put("keyboard", 1);
        Order order = shop.facade().placeOrder(cart, CUSTOMER);

        cart.put("keyboard", 4);
        equal(Map.of("keyboard", 1), order.items());
        expect(UnsupportedOperationException.class, () -> order.items().put("mouse", 1));
        shop.facade().cancelOrder(order.id());
        equal(5, shop.inventory().getAvailableQuantity("keyboard"));
    }

    private static void invalidCarts() {
        InventoryService inventory = new InventoryService(PRICES, STOCK);

        expect(IllegalArgumentException.class, () -> inventory.reserve("empty", Map.of()));
        expect(IllegalArgumentException.class, () -> inventory.reserve("zero", Map.of("keyboard", 0)));
        expect(IllegalArgumentException.class, () -> inventory.reserve("negative", Map.of("keyboard", -1)));
        expect(IllegalArgumentException.class, () -> inventory.reserve("unknown", Map.of("monitor", 1)));

        equal(5, inventory.getAvailableQuantity("keyboard"));
        equal(8, inventory.getAvailableQuantity("mouse"));
    }

    private static void invalidCustomers() {
        expect(IllegalArgumentException.class, () -> new Customer(" ", EMAIL, "10 Main Street"));
        expect(IllegalArgumentException.class, () -> new Customer("Alex", "invalid", "10 Main Street"));
        expect(IllegalArgumentException.class, () -> new Customer("Alex", EMAIL, " "));
    }

    private static void independentSubsystems() {
        InventoryService inventory = new InventoryService(PRICES, STOCK);
        PaymentProcessor payment = new PaymentProcessor(Map.of(EMAIL, 50000L));
        ShippingService shipping = new ShippingService();
        NotificationService notifications = new NotificationService();

        long total = inventory.reserve("direct-order", Map.of("mouse", 2));
        equal(5000L, total);
        payment.charge("direct-order", EMAIL, total);
        shipping.ship("direct-order", CUSTOMER.address());
        notifications.send(EMAIL, "Your order is ready");
        equal(45000L, payment.getBalance(EMAIL));
        check(shipping.hasShipment("direct-order"), "Direct shipment should exist");
        equal(1, notifications.getMessages().size());

        shipping.cancel("direct-order");
        shipping.cancel("direct-order");
        payment.refund("direct-order");
        payment.refund("direct-order");
        inventory.release("direct-order");
        inventory.release("direct-order");
        equal(50000L, payment.getBalance(EMAIL));
        equal(8, inventory.getAvailableQuantity("mouse"));
        check(!shipping.hasShipment("direct-order"), "Direct shipment should be removed");
    }

    private static void publicConstructor() {
        OrderFacade facade = new OrderFacade(PRICES, STOCK, Map.of(EMAIL, 50000L));
        Order order = facade.placeOrder(Map.of("mouse", 1), CUSTOMER);
        equal(2500L, order.totalCents());
        equal(Order.Status.CANCELLED, facade.cancelOrder(order.id()).status());
    }

    private static TestShop createShop(long balance, ShippingService shipping,
                                       NotificationService notifications) {
        InventoryService inventory = new InventoryService(PRICES, STOCK);
        PaymentProcessor payment = new PaymentProcessor(Map.of(EMAIL, balance));
        OrderFacade facade = new OrderFacade(inventory, payment, shipping, notifications);
        return new TestShop(facade, inventory, payment, shipping, notifications);
    }

    private static void run(String name, Runnable test) {
        test.run();
        passed++;
        System.out.println("PASS: " + name);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void equal(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    private static <T extends Throwable> T expect(Class<T> type, Runnable action) {
        try {
            action.run();
        } catch (Throwable failure) {
            if (type.isInstance(failure)) {
                return type.cast(failure);
            }
            throw new AssertionError("Expected " + type.getSimpleName() + ", got " + failure, failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private record TestShop(OrderFacade facade, InventoryService inventory,
                            PaymentProcessor payment, ShippingService shipping,
                            NotificationService notifications) {
    }

    private static class TrackingShippingService extends ShippingService {
        private String lastOrderId;

        @Override
        public void ship(String orderId, String address) {
            super.ship(orderId, address);
            lastOrderId = orderId;
        }
    }
}
