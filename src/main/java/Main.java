
import facade.OrderFacade;
import model.Customer;
import model.Order;
import model.Product;

public class Main {

    public static void main(String[] args) {

        // Initialize Facade
        OrderFacade orderFacade = new OrderFacade();

        // Create products
        Product laptop = new Product(1, "Laptop", 450000);
        Product gamingPC = new Product(2, "Gaming PC", 1200000);

        Customer customer = new Customer(
                1,
                "Alex",
                "alex@example.com"
        );

        orderFacade.addStock(laptop.getId(), 10);
        orderFacade.addStock(gamingPC.getId(), 5);

        System.out.println("\n=== SCENARIO 1: SUCCESSFUL ORDER ===");

        Order order1 = new Order(101, laptop, customer, 2);

        boolean result1 = orderFacade.placeOrder(order1);

        System.out.println("Order successful: " + result1);
        System.out.println("Order status: " + order1.getStatus());
        System.out.println(
                "Remaining stock: "
                        + orderFacade.getAvailableStock(laptop.getId())
        );


        System.out.println("\n=== SCENARIO 2: PAYMENT DECLINED ===");

        Order order2 = new Order(102, gamingPC, customer, 1);

        boolean result2 = orderFacade.placeOrder(order2);

        System.out.println("Order successful: " + result2);
        System.out.println("Order status: " + order2.getStatus());
        System.out.println(
                "Remaining stock: "
                        + orderFacade.getAvailableStock(gamingPC.getId())
        );

        System.out.println("\n=== SCENARIO 3: INSUFFICIENT STOCK ===");

        Order order3 = new Order(103, laptop, customer, 20);

        boolean result3 = orderFacade.placeOrder(order3);

        System.out.println("Order successful: " + result3);
        System.out.println("Order status: " + order3.getStatus());


        System.out.println("\n=== SCENARIO 4: CANCEL ORDER ===");

        boolean cancelled = orderFacade.cancelOrder(101);

        System.out.println("Cancellation successful: " + cancelled);
        System.out.println("Order status: " + order1.getStatus());
        System.out.println(
                "Restored stock: "
                        + orderFacade.getAvailableStock(laptop.getId())
        );


        System.out.println("\n=== SCENARIO 5: NOTIFICATIONS ===");

        System.out.println(
                "Total notifications: "
                        + orderFacade.getNotificationCount()
        );
    }
}
