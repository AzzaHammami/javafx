package com.example.rendez_vous.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.time.LocalDateTime;

public class Disponibilite {
    private SimpleIntegerProperty id;
    private SimpleIntegerProperty medecinId;
    private SimpleObjectProperty<LocalDateTime> dateDebut;
    private SimpleObjectProperty<LocalDateTime> dateFin;
    private SimpleStringProperty statut;

    public Disponibilite() {
        this.id = new SimpleIntegerProperty();
        this.medecinId = new SimpleIntegerProperty();
        this.dateDebut = new SimpleObjectProperty<>();
        this.dateFin = new SimpleObjectProperty<>();
        this.statut = new SimpleStringProperty("Disponible");
    }

    public Disponibilite(int medecinId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        this();
        this.medecinId.set(medecinId);
        this.dateDebut.set(dateDebut);
        this.dateFin.set(dateFin);
    }

    // Getters et Setters pour les propriétés
    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public SimpleIntegerProperty medecinIdProperty() {
        return medecinId;
    }

    public int getMedecinId() {
        return medecinId.get();
    }

    public void setMedecinId(int medecinId) {
        this.medecinId.set(medecinId);
    }

    public SimpleObjectProperty<LocalDateTime> dateDebutProperty() {
        return dateDebut;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut.get();
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut.set(dateDebut);
    }

    public SimpleObjectProperty<LocalDateTime> dateFinProperty() {
        return dateFin;
    }

    public LocalDateTime getDateFin() {
        return dateFin.get();
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin.set(dateFin);
    }

    public SimpleStringProperty statutProperty() {
        return statut;
    }

    public String getStatut() {
        return statut.get();
    }

    public void setStatut(String statut) {
        this.statut.set(statut);
    }

    @Override
    public String toString() {
        return "Disponibilite{" +
                "id=" + id.get() +
                ", medecinId=" + medecinId.get() +
                ", dateDebut=" + dateDebut.get() +
                ", dateFin=" + dateFin.get() +
                ", statut='" + statut.get() + '\'' +
                '}';
    }
}
