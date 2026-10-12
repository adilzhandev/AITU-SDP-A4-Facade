package subsystem;

import java.util.HashMap;
import java.util.Map;

public class PaymentProcessor {
    private final Map<Integer, Boolean> payments = new HashMap<>();

    public boolean processPayment(int orderId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (payments.containsKey(orderId)) {
            throw new IllegalStateException(
                    "Payment already exists for this order"
            );
        }

        if (amount > 1_000_000) {
            System.out.println("Payment declined for order: " + orderId);
            return false;
        }

        payments.put(orderId, true);

        System.out.println("Payment successful for order: " + orderId);
        return true;
    }

    public boolean refundPayment(int orderId) {
        if (!Boolean.TRUE.equals(payments.get(orderId))) {
            return false;
        }

        payments.put(orderId, false);

        System.out.println("Payment refunded for order: " + orderId);
        return true;
    }

    public boolean getPaymentStatus(int orderId) {
        return Boolean.TRUE.equals(payments.get(orderId));
    }
}