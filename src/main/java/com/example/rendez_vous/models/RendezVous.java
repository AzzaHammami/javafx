package com.example.rendez_vous.models;

import java.time.LocalDateTime;

public class RendezVous {
    private int id;
    private LocalDateTime date;
    private String statut;
    private String motif;
    private int patientId;
    private int medecinId;
    private LocalDateTime dateCreation;

    public RendezVous() {
    }

    public RendezVous(LocalDateTime date, String statut, String motif, int patientId, int medecinId) {
        this.date = date;
        this.statut = statut;
        this.motif = motif;
        this.patientId = patientId;
        this.medecinId = medecinId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(int medecinId) {
        this.medecinId = medecinId;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "RendezVous{" +
                "id=" + id +
                ", date=" + date +
                ", statut='" + statut + '\'' +
                ", motif='" + motif + '\'' +
                ", patientId=" + patientId +
                ", medecinId=" + medecinId +
                ", dateCreation=" + dateCreation +
                '}';
    }
}