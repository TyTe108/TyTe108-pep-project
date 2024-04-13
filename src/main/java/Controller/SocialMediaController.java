package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;

public class SocialMediaController {
    private AccountService accountService;
    private MessageService messageService;

    // Constructors
    public SocialMediaController(AccountService accountService, MessageService messageService) {
        this.accountService = accountService;
        this.messageService = messageService;
    }
    
    public SocialMediaController() {
        // Assumes both AccountService and MessageService have parameterless constructors
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }

    public Javalin startAPI() {
        Javalin app = Javalin.create();

        // Register new user
        app.post("/register", this::registerUser);
        // User login
        app.post("/login", this::loginUser);
        // Create new message
        app.post("/messages", this::postMessage);
        // Get all messages
        app.get("/messages", this::getAllMessages);
        // Get a message by ID
        app.get("/messages/{message_id}", this::getMessageById);
        // Delete a message
        app.delete("/messages/{message_id}", this::deleteMessage);
        // Update a message
        app.patch("/messages/{message_id}", this::updateMessage);
        // Get messages by user
        app.get("/accounts/{account_id}/messages", this::getMessagesByUser);

        return app;
    }

    public void registerUser(Context context) {
        try {
            Account account = context.bodyAsClass(Account.class);
            if (account.getUsername() == null || account.getUsername().trim().isEmpty()) {
                context.status(400).result("");  // Respond with an empty body for blank usernames
                return;
            }
            Account registeredAccount = accountService.registerAccount(account);
            if (registeredAccount != null) {
                context.status(200).json(registeredAccount);
            } else {
                context.status(400).result(""); // Still return an empty body if the username is taken or any other registration failure
            }
        } catch (Exception e) {
            context.status(400).result(""); // Ensure all error responses for registration issues return an empty body
        }
    }
    
    

    public void loginUser(Context context) {
        try {
            Account account = context.bodyAsClass(Account.class);
            Account loggedAccount = accountService.login(account.getUsername(), account.getPassword());
            if (loggedAccount != null) {
                context.status(200).json(loggedAccount);
            } else {
                // Ensure the response body is empty on failed login
                context.status(401).result("");  // Set an empty response body for unauthorized access
            }
        } catch (Exception e) {
            // Ensure even error responses don't send back any message body
            context.status(400).result("");
        }
    }
    

    private void postMessage(Context context) {
        try {
            Message message = context.bodyAsClass(Message.class);
            // Validate the message content
            if (message.getMessage_text() == null || message.getMessage_text().isEmpty()) {
                context.status(400).result(""); // Empty response for empty message
                return;
            }
            if (message.getMessage_text().length() > 255) {
                context.status(400).result(""); // Empty response for message too long
                return;
            }
            // Check if the user exists
            if (!accountService.exists(message.getPosted_by())) {
                context.status(400).result(""); // Modify here to return an empty response body when user does not exist
                return;
            }
            // Create the message
            Message postedMessage = messageService.postMessage(message);
            context.status(200).json(postedMessage);
        } catch (IllegalArgumentException e) {
            context.status(400).result(e.getMessage());
        } catch (Exception e) {
            context.status(500).result("Internal server error: " + e.getMessage());
        }
    }
    
    
    
    
    

    private void getAllMessages(Context context) {
        try {
            List<Message> messages = messageService.getAllMessages();
            context.json(messages);
        } catch (Exception e) {
            context.status(400).result("Failed to retrieve messages: " + e.getMessage());
        }
    }

    public void getMessageById(Context context) {
        try {
            int messageId = Integer.parseInt(context.pathParam("message_id"));
            Message message = messageService.getMessageById(messageId);
            if (message != null) {
                context.status(200).json(message);
            } else {
                context.status(200).result("");  // Return 200 OK with an empty response body
            }
        } catch (NumberFormatException e) {
            context.status(400).result("Invalid message ID format");
        } catch (Exception e) {
            context.status(500).result("Internal server error: " + e.getMessage());
        }
    }
    

    private void deleteMessage(Context context) {
        try {
            int messageId = Integer.parseInt(context.pathParam("message_id"));
            Message messageToDelete = messageService.getMessageById(messageId);
            if (messageToDelete != null) {
                boolean isDeleted = messageService.deleteMessage(messageId);
                if (isDeleted) {
                    context.status(200).json(messageToDelete); // Return the JSON representation of the deleted message
                } else {
                    context.status(500).result("Failed to delete message");
                }
            } else {
                context.status(200).result(""); // Ensure to return an empty response body for non-existent message IDs
            }
        } catch (NumberFormatException e) {
            context.status(400).result("Invalid message ID format");
        } catch (Exception e) {
            context.status(400).result("Error processing request: " + e.getMessage());
        }
    }
    

    public void updateMessage(Context context) {
        int messageId = Integer.parseInt(context.pathParam("message_id"));
        String requestBody = context.body();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;
    
        try {
            rootNode = objectMapper.readTree(requestBody);
        } catch (IOException e) {
            context.status(400).result("Invalid JSON format");
            return;
        }
    
        String messageText = rootNode.path("message_text").asText();
        if (messageText == null || messageText.trim().isEmpty()) {
            context.status(400).result("");
            return;
        }
        if (messageText.length() > 255) {
            context.status(400).result("");
            return;
        }
    
        try {
            Message updatedMessage = messageService.updateMessageText(messageId, messageText);
            if (updatedMessage != null) {
                context.status(200).json(updatedMessage);
            } else {
                // Handle null returned when message is not found
                context.status(400).result(""); // Change this to match the expected status code and body
            }
        } catch (IllegalArgumentException e) {
            context.status(400).result(e.getMessage());
        } catch (SQLException e) {
            // Specific handling if the error message from the SQL exception indicates the ID was not found
            if (e.getMessage().contains("not found")) {
                context.status(400).result("");
            } else {
                context.status(500).result("Database error: " + e.getMessage());
            }
        } catch (Exception e) {
            context.status(500).result("Internal server error: " + e.getMessage());
        }
    }
    
    
    
    

    private void getMessagesByUser(Context context) {
        try {
            int userId = Integer.parseInt(context.pathParam("account_id"));
            List<Message> messages = messageService.getMessagesByUserId(userId);
            if (messages != null) {
                context.json(messages);
            } else {
                context.status(404).result("User not found or no messages for user");
            }
        } catch (Exception e) {
            context.status(400).result("Failed to retrieve messages: " + e.getMessage());
        }
    }

    // Validate message content (assuming message_text length is the primary concern)
    private void validateMessage(Message message) throws IllegalArgumentException {
        if (message.getMessage_text() == null || message.getMessage_text().isEmpty()) {
            throw new IllegalArgumentException("Message text cannot be empty.");
        }
        if (message.getMessage_text().length() > 255) {
            throw new IllegalArgumentException("Message text cannot exceed 255 characters.");
        }
    }

}
