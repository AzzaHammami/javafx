package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private int id;
    private boolean isGroup;
    private String title;
    private Timestamp createdAt;
    private Timestamp lastMessageAt;
    private List<User> participants;
    private List<Message> messages;

    // Constructeurs
    public Conversation() {
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public Conversation(boolean isGroup, String title) {
        this.isGroup = isGroup;
        this.title = title;
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Timestamp lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    // Méthodes utilitaires
    public void addParticipant(User user) {
        if (!participants.contains(user)) {
            participants.add(user);
        }
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", isGroup=" + isGroup +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", lastMessageAt=" + lastMessageAt +
                ", participants=" + participants.size() +
                ", messages=" + messages.size() +
                '}';
    }
}
