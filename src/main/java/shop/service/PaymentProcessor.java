package shop.service;

import java.util.HashMap;
import java.util.Map;

public class PaymentProcessor {
    private final Map<String, Long> balances;
    private final Map<String, Payment> payments = new HashMap<>();

    public PaymentProcessor(Map<String, Long> balances) {
        for (Map.Entry<String, Long> account : balances.entrySet()) {
            if (account.getKey() == null || account.getKey().isBlank()
                    || account.getValue() == null || account.getValue() < 0) {
                throw new IllegalArgumentException("Invalid customer balance");
            }
        }
        this.balances = new HashMap<>(balances);
    }

    public void charge(String orderId, String email, long amountCents) {
        if (orderId == null || orderId.isBlank() || payments.containsKey(orderId)) {
            throw new IllegalArgumentException("Order ID must be new and non-empty");
        }
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        long balance = getBalance(email);
        if (balance < amountCents) {
            throw new IllegalStateException("Payment declined: insufficient balance");
        }
        balances.put(email, balance - amountCents);
        payments.put(orderId, new Payment(email, amountCents));
    }

    public void refund(String orderId) {
        Payment payment = payments.remove(orderId);
        if (payment != null) {
            balances.put(payment.email(), balances.get(payment.email()) + payment.amountCents());
        }
    }

    public long getBalance(String email) {
        return balances.getOrDefault(email, 0L);
    }

    private record Payment(String email, long amountCents) {
    }
}
