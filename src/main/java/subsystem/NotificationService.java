package subsystem;

import model.Customer;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    private final List<String> notificationHistory = new ArrayList<>();

    public void sendNotification(Customer customer, String message) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        String notification = "To: " + customer.getEmail()
                + " | Message: " + message;

        notificationHistory.add(notification);

        System.out.println("Notification sent: " + notification);
    }

    public List<String> getNotificationHistory() {
        return List.copyOf(notificationHistory);
    }
}