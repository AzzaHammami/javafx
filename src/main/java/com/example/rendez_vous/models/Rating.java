package com.example.rendez_vous.models;

import java.time.LocalDateTime;

public class Rating {
    private int id;
    private int objectId; // id du médecin
    private String objectType; // "medecin"
    private int ratingValue; // 1 à 5
    private String comment;
    private int userId;
    private LocalDateTime timestamp;

    public Rating() {}

    public Rating(int id, int objectId, String objectType, int ratingValue, String comment, int userId, LocalDateTime timestamp) {
        this.id = id;
        this.objectId = objectId;
        this.objectType = objectType;
        this.ratingValue = ratingValue;
        this.comment = comment;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getObjectId() { return objectId; }
    public void setObjectId(int objectId) { this.objectId = objectId; }

    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }

    public int getRatingValue() { return ratingValue; }
    public void setRatingValue(int ratingValue) { this.ratingValue = ratingValue; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
