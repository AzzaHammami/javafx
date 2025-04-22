package services;

import models.Conversation;
import models.User;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConversationService {
    private static final Logger LOGGER = Logger.getLogger(ConversationService.class.getName());
    private Connection conn;
    private UserService userService;
    private MessageService messageService;

    public ConversationService() {
        conn = MyDataBase.getInstance().getConnection();
        userService = new UserService();

    }
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    public Conversation getOrCreateDirectConversation(int user1Id, int user2Id) {
        // First try to find existing direct conversation
        Conversation existingConversation = findDirectConversation(user1Id, user2Id);
        if (existingConversation != null) {
            return existingConversation;
        }

        // If not found, create a new one
        List<User> participants = new ArrayList<>();
        participants.add(userService.getUserById(user1Id));
        participants.add(userService.getUserById(user2Id));

        return createConversation(participants);
    }

    private Conversation findDirectConversation(int user1Id, int user2Id) {
        String query = "SELECT c.id FROM conversation c " +
                "WHERE c.id IN (" +
                "    SELECT cp1.conversation_id FROM conversation_participant cp1 " +
                "    WHERE cp1.user_id = ?" +
                ") AND c.id IN (" +
                "    SELECT cp2.conversation_id FROM conversation_participant cp2 " +
                "    WHERE cp2.user_id = ?" +
                ") AND NOT EXISTS (" +
                "    SELECT 1 FROM conversation_participant cp3 " +
                "    WHERE cp3.conversation_id = c.id " +
                "    AND cp3.user_id NOT IN (?, ?)" +
                ")";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, user1Id);
            pstmt.setInt(2, user2Id);
            pstmt.setInt(3, user1Id);
            pstmt.setInt(4, user2Id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return getConversationById(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding direct conversation", e);
        }
        return null;
    }

    public Conversation createConversation(List<User> participants) {
        Conversation conversation = null;
        try {
            conn.setAutoCommit(false);
            String query = "INSERT INTO conversation (created_at, last_message_at, is_group) VALUES (NOW(), NOW(), ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setBoolean(1, participants.size() > 2); // Mark as group if more than 2 participants
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int conversationId = rs.getInt(1);
                    for (User participant : participants) {
                        String pQuery = "INSERT INTO conversation_participant (conversation_id, user_id) VALUES (?, ?)";
                        try (PreparedStatement pPstmt = conn.prepareStatement(pQuery)) {
                            pPstmt.setInt(1, conversationId);
                            pPstmt.setInt(2, participant.getId());
                            pPstmt.executeUpdate();
                        }
                    }
                    conn.commit();
                    conversation = getConversationById(conversationId);
                }
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            LOGGER.log(Level.SEVERE, "Error creating conversation", e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "AutoCommit reset failed", ex); }
        }
        return conversation;
    }

    public List<Conversation> getUserConversations(int userId) {
        List<Conversation> conversations = new ArrayList<>();
        String query = "SELECT DISTINCT c.* FROM conversation c " +
                "JOIN conversation_participant cp ON c.id = cp.conversation_id " +
                "WHERE cp.user_id = ? " +
                "ORDER BY c.last_message_at DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Conversation conversation = new Conversation();
                    conversation.setId(rs.getInt("id"));
                    conversation.setGroup(rs.getBoolean("is_group"));
                    conversation.setCreatedAt(rs.getTimestamp("created_at"));
                    conversation.setLastMessageAt(rs.getTimestamp("last_message_at"));
                    conversation.setParticipants(getConversationParticipants(conversation.getId()));
                    conversations.add(conversation);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching user conversations", e);
        }
        return conversations;
    }

    public Conversation getConversationById(int conversationId) {
        String query = "SELECT * FROM conversation WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, conversationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Conversation conversation = new Conversation();
                    conversation.setId(rs.getInt("id"));
                    conversation.setGroup(rs.getBoolean("is_group"));
                    conversation.setCreatedAt(rs.getTimestamp("created_at"));
                    conversation.setLastMessageAt(rs.getTimestamp("last_message_at"));
                    conversation.setParticipants(getConversationParticipants(conversationId));
                    return conversation;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching conversation by id", e);
        }
        return null;
    }

    private List<User> getConversationParticipants(int conversationId) {
        List<User> participants = new ArrayList<>();
        String query = "SELECT user_id FROM conversation_participant WHERE conversation_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, conversationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User participant = userService.getUserById(rs.getInt("user_id"));
                    if (participant != null) {
                        participants.add(participant);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching conversation participants", e);
        }
        return participants;
    }
}