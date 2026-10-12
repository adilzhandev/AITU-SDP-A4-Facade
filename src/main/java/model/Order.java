package model;

public class Order {
    private final int id;
    private final Product product;
    private final Customer customer;
    private final int quantity;
    private OrderStatus status;

    public Order(int id, Product product, Customer customer, int quantity) {
        this.id = id;
        this.product = product;
        this.customer = customer;
        this.quantity = quantity;
        this.status = OrderStatus.CREATED;
    }

    public int getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}