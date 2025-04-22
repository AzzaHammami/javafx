package services;

import models.Message;
import models.User;
import models.Conversation;
import utils.MyDataBase;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageService {
    private static final Logger LOGGER = Logger.getLogger(MessageService.class.getName());
    private Connection conn;
    private UserService userService;
    private ConversationService conversationService;

    public MessageService() {
        conn = MyDataBase.getInstance().getConnection();
        userService = new UserService();
    }
    public void setConversationService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }
    public void sendMessage(Message message) {
        try {
            conn.setAutoCommit(false);

            // For direct messages, ensure conversation exists
            if (message.getReceiverId() > 0 && message.getConversationId() == 0) {
                Conversation conv = conversationService.getOrCreateDirectConversation(
                        message.getSender().getId(),
                        message.getReceiverId()
                );
                message.setConversationId(conv.getId());
            }

            String query = "INSERT INTO message (conversation_id, sender_id, receiver_id, content, sent_at, is_read, message_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, message.getConversationId());
                pstmt.setInt(2, message.getSender().getId());
                pstmt.setInt(3, message.getReceiverId());
                pstmt.setString(4, message.getContent());
                pstmt.setTimestamp(5, Timestamp.valueOf(message.getSentAt()));
                pstmt.setBoolean(6, message.isRead());
                pstmt.setString(7, message.getMessageType());

                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        message.setId(rs.getInt(1));
                    }
                }
            }

            updateConversationLastMessage(message.getConversationId(), message.getSentAt());
            conn.commit();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Rollback failed", ex);
            }
            LOGGER.log(Level.SEVERE, "Error sending message", e);
            throw new RuntimeException("Failed to send message", e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "AutoCommit reset failed", ex);
            }
        }
    }

    public List<Message> getMessagesByConversation(int conversationId) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM message WHERE conversation_id = ? ORDER BY sent_at ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, conversationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setConversationId(rs.getInt("conversation_id"));
                    message.setSender(userService.getUserById(rs.getInt("sender_id")));
                    message.setReceiverId(rs.getInt("receiver_id"));
                    message.setContent(rs.getString("content"));
                    message.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                    message.setRead(rs.getBoolean("is_read"));
                    message.setMessageType(rs.getString("message_type"));
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching messages by conversation", e);
        }
        return messages;
    }

    private void updateConversationLastMessage(int conversationId, LocalDateTime lastMessageAt) {
        String query = "UPDATE conversation SET last_message_at = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(lastMessageAt));
            pstmt.setInt(2, conversationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating lastMessageAt", e);
        }
    }

    public void markMessagesAsRead(int conversationId, int userId) {
        String query = "UPDATE message SET is_read = true " +
                "WHERE conversation_id = ? AND receiver_id = ? AND is_read = false";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, conversationId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking messages as read", e);
        }
    }

    public int getUnreadCount(int userId) {
        String query = "SELECT COUNT(*) FROM message m " +
                "JOIN conversation_participant cp ON m.conversation_id = cp.conversation_id " +
                "WHERE cp.user_id = ? AND m.sender_id != ? AND m.is_read = false";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching unread count", e);
        }
        return 0;
    }
}