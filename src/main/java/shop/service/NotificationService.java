package shop.service;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    private final List<String> messages = new ArrayList<>();

    public void send(String email, String message) {
        if (email == null || !email.contains("@") || email.startsWith("@") || email.endsWith("@")) {
            throw new IllegalArgumentException("Recipient email is invalid");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Notification message is required");
        }
        messages.add(email + ": " + message);
    }

    public List<String> getMessages() {
        return List.copyOf(messages);
    }
}
