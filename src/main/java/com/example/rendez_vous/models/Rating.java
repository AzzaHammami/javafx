package com.example.rendez_vous.models;

public class Rating {
    private int id;
    private int medecinId;
    private int patientId;
    private int value;
    private String commentaire;

    public Rating() {}

    public Rating(int id, int medecinId, int patientId, int value, String commentaire) {
        this.id = id;
        this.medecinId = medecinId;
        this.patientId = patientId;
        this.value = value;
        this.commentaire = commentaire;
    }

    public Rating(int medecinId, int patientId, int value, String commentaire) {
        this.medecinId = medecinId;
        this.patientId = patientId;
        this.value = value;
        this.commentaire = commentaire;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(int medecinId) {
        this.medecinId = medecinId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}
