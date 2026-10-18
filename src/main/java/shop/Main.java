package shop;

import java.util.Locale;
import java.util.Map;
import shop.model.Customer;
import shop.model.Order;

public class Main {
    public static void main(String[] args) {
        OrderFacade shop = new OrderFacade(
                Map.of("BOOK", 2500L, "KEYBOARD", 10000L),
                Map.of("BOOK", 5, "KEYBOARD", 1),
                Map.of("alice@example.com", 15000L, "bob@example.com", 1000L));

        Customer alice = new Customer("Alice", "alice@example.com", "10 Abay Avenue, Almaty");
        Customer bob = new Customer("Bob", "bob@example.com", "20 Abay Avenue, Almaty");
        Map<String, Integer> cart = Map.of("KEYBOARD", 1);

        System.out.println("1. Place an order");
        Order order = shop.placeOrder(cart, alice);
        printOrder(order);

        System.out.println("\n2. Cancel the order and restore stock and balance");
        printOrder(shop.cancelOrder(order.id()));

        System.out.println("\n3. Decline a payment and release the reserved stock");
        try {
            shop.placeOrder(cart, bob);
        } catch (IllegalStateException failure) {
            System.out.println(failure.getMessage());
        }

        System.out.println("\n4. Buy the same item after cancellation and rollback");
        printOrder(shop.placeOrder(cart, alice));
    }

    private static void printOrder(Order order) {
        System.out.printf(Locale.ROOT, "%s | %s | total: %.2f%n",
                order.id(), order.status(), order.totalCents() / 100.0);
    }
}
