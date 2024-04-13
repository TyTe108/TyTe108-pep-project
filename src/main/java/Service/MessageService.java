package Service;

import DAO.MessageDAO;
import Model.Message;
import java.sql.SQLException;
import java.util.List;

public class MessageService {
    private final MessageDAO messageDAO;

    // Parameterless constructor
    public MessageService() {
        this.messageDAO = new MessageDAO(); // Assumes MessageDAO has a no-arg constructor
    }


    public MessageService(MessageDAO messageDAO) {
        this.messageDAO = messageDAO;
    }

    public Message postMessage(Message message) throws Exception {
        // Validate the message content
        if (message.getMessage_text() == null || message.getMessage_text().trim().isEmpty()) {
            throw new IllegalArgumentException("Message text cannot be empty.");
        }
        if (message.getMessage_text().length() > 255) {
            throw new IllegalArgumentException("Message text cannot exceed 255 characters.");
        }
        // Assuming posted_by is already validated as an existing user ID elsewhere
        // Insert the message into the database
        return messageDAO.createMessage(message);
    }

    public List<Message> getAllMessages() throws SQLException {
        return messageDAO.getAllMessages();
    }

    public Message getMessageById(int messageId) throws SQLException {
        return messageDAO.getMessageById(messageId);
    }

    public boolean deleteMessage(int messageId) throws SQLException {
        return messageDAO.deleteMessage(messageId);
    }

    public Message updateMessageText(int messageId, String newText) throws Exception {
        if (newText == null || newText.trim().isEmpty()) {
            throw new IllegalArgumentException("Message text cannot be empty.");
        }
        if (newText.length() > 255) {
            throw new IllegalArgumentException("Message text cannot exceed 255 characters.");
        }
        Message existingMessage = getMessageById(messageId);
        if (existingMessage == null) {
            throw new SQLException("Message with ID " + messageId + " not found.");
        }
        existingMessage.setMessage_text(newText);
        return messageDAO.updateMessage(existingMessage);
    }

    public List<Message> getMessagesByUserId(int userId) throws SQLException {
        return messageDAO.getMessagesByUserId(userId);
    }

    // Additional methods as needed...
}
