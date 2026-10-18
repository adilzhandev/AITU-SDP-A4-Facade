package shop.model;

public record Customer(String name, String email, String address) {
    public Customer {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (email == null || !email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new IllegalArgumentException("Customer email is invalid");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Delivery address is required");
        }
    }
}
