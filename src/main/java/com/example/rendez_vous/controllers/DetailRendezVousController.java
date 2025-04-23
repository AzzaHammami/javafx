package com.example.rendez_vous.controllers;
import com.example.rendez_vous.models.RendezVous;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DetailRendezVousController {
    @FXML
    private Label dateLabel;

    @FXML
    private Label statutLabel;

    @FXML
    private Label motifLabel;

    @FXML
    private Label patientIdLabel;

    @FXML
    private Label medecinIdLabel;

    @FXML
    private Label dateCreationLabel;

    // Method to set the details of the RendezVous object
    public void setDetails(RendezVous rv) {
        dateLabel.setText(rv.getDate().toString());
        statutLabel.setText(rv.getStatut());
        motifLabel.setText(rv.getMotif());
        patientIdLabel.setText(String.valueOf(rv.getPatientId()));
        medecinIdLabel.setText(String.valueOf(rv.getMedecinId()));
        dateCreationLabel.setText(rv.getDateCreation().toString());
    }
}
