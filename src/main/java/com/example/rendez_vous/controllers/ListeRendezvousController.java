package com.example.rendez_vous.controllers;

import com.example.rendez_vous.models.RendezVous;
import com.example.rendez_vous.models.Disponibilite;
import com.example.rendez_vous.models.User;
import com.example.rendez_vous.services.Servicerendez_vous;
import com.example.rendez_vous.services.Servicedisponibilite;
import com.example.rendez_vous.services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class ListeRendezvousController implements Initializable {
    @FXML private TableView<RendezVous> tableView;
    @FXML private TableColumn<RendezVous, Integer> idCol;
    @FXML private TableColumn<RendezVous, String> motifCol;
    @FXML private TableColumn<RendezVous, Integer> patientIdCol;
    @FXML private TableColumn<RendezVous, Integer> medecinIdCol;
    @FXML private TableColumn<RendezVous, String> dateCol;
    @FXML private TableColumn<RendezVous, String> statutCol;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private TextField motifField;
    @FXML private ComboBox<User> patientComboBox;
    @FXML private TextField medecinIdField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private ComboBox<String> statutComboBox;

    private final Servicerendez_vous service = new Servicerendez_vous();
    private final Servicedisponibilite serviceDisponibilite = new Servicedisponibilite();
    private final ServiceUser userService = new ServiceUser();

    private RendezVous selectedRendezVous = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        motifCol.setCellValueFactory(new PropertyValueFactory<>("motif"));
        patientIdCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        medecinIdCol.setCellValueFactory(new PropertyValueFactory<>("medecinId"));
        dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDate() != null ?
                        cellData.getValue().getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""
        ));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        heureCombo.setItems(FXCollections.observableArrayList("08", "09", "10", "11", "14", "15", "16", "17"));
        minuteCombo.setItems(FXCollections.observableArrayList("00", "15", "30", "45"));
        statutComboBox.setItems(FXCollections.observableArrayList("En attente", "Confirmé", "Annulé"));

        // Remplir la ComboBox des patients par leur nom
        List<User> patients = userService.getAllPatients();
        patientComboBox.setItems(FXCollections.observableArrayList(patients));
        patientComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        patientComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedRendezVous = newSel;
                motifField.setText(newSel.getMotif());
                patientComboBox.setValue(userService.getUserById(newSel.getPatientId()));
                medecinIdField.setText(String.valueOf(newSel.getMedecinId()));
                if (newSel.getDate() != null) {
                    datePicker.setValue(newSel.getDate().toLocalDate());
                    heureCombo.setValue(String.format("%02d", newSel.getDate().getHour()));
                    minuteCombo.setValue(String.format("%02d", newSel.getDate().getMinute()));
                } else {
                    datePicker.setValue(null);
                    heureCombo.setValue(null);
                    minuteCombo.setValue(null);
                }
                statutComboBox.setValue(newSel.getStatut());
            }
        });

        rafraichirListe();
    }

    private void rafraichirListe() {
        List<RendezVous> list = service.listerRendezVous();
        ObservableList<RendezVous> observableList = FXCollections.observableArrayList(list);
        tableView.setItems(observableList);
    }

    private boolean validateInputs() {
        String motif = motifField.getText();
        User patient = patientComboBox.getValue();
        String medecinId = medecinIdField.getText();
        LocalDate date = datePicker.getValue();
        String heure = heureCombo.getValue();
        String minute = minuteCombo.getValue();
        String statut = statutComboBox.getValue();
        StringBuilder errorMsg = new StringBuilder();
        if (motif == null || motif.trim().isEmpty()) errorMsg.append("- Le motif est obligatoire.\n");
        if (patient == null) errorMsg.append("- Le patient est obligatoire.\n");
        if (medecinId == null || !medecinId.matches("\\d+") || Integer.parseInt(medecinId) <= 0) errorMsg.append("- L'ID médecin doit être un nombre positif.\n");
        if (date == null) errorMsg.append("- La date est obligatoire.\n");
        if (heure == null) errorMsg.append("- L'heure est obligatoire.\n");
        if (minute == null) errorMsg.append("- La minute est obligatoire.\n");
        if (statut == null) errorMsg.append("- Le statut est obligatoire.\n");
        // Vérification date dans le futur
        if (date != null && heure != null && minute != null) {
            try {
                LocalDateTime dateTime = date.atTime(Integer.parseInt(heure), Integer.parseInt(minute));
                if (dateTime.isBefore(LocalDateTime.now())) {
                    errorMsg.append("- La date/heure doit être dans le futur.\n");
                }
                // Vérification disponibilité du médecin
                if (medecinId != null && medecinId.matches("\\d+") && Integer.parseInt(medecinId) > 0) {
                    try {
                        boolean disponible = false;
                        for (Disponibilite disp : serviceDisponibilite.getDisponibilitesByMedecin(Integer.parseInt(medecinId))) {
                            if (disp.getDateDebut() != null && disp.getDateFin() != null &&
                                    (dateTime.isEqual(disp.getDateDebut()) || dateTime.isEqual(disp.getDateFin()) ||
                                            (dateTime.isAfter(disp.getDateDebut()) && dateTime.isBefore(disp.getDateFin())))) {
                                disponible = true;
                                break;
                            }
                        }
                        if (!disponible) {
                            errorMsg.append("- Le médecin n'est pas disponible à cette date/heure.\n");
                        }
                    } catch (Exception e) {
                        errorMsg.append("- Erreur lors de la vérification de la disponibilité du médecin.\n");
                    }
                }
            } catch (Exception e) {
                errorMsg.append("- Heure ou minute invalide.\n");
            }
        }
        // Vérification existence du médecin
        if (medecinId != null && medecinId.matches("\\d+") && Integer.parseInt(medecinId) > 0) {
            // Medecin medecin = serviceMedecin.getMedecinById(Integer.parseInt(medecinId));
            // if (medecin == null) {
            //     errorMsg.append("- Aucun médecin trouvé avec cet ID.\n");
            // }
        }
        // Vérification chevauchement avec d'autres rendez-vous du même médecin
        if (medecinId != null && medecinId.matches("\\d+") && Integer.parseInt(medecinId) > 0 && date != null && heure != null && minute != null) {
            try {
                LocalDateTime dateTime = date.atTime(Integer.parseInt(heure), Integer.parseInt(minute));
                for (RendezVous rv : service.listerRendezVous()) {
                    if (rv.getMedecinId() == Integer.parseInt(medecinId)) {
                        // Si modification, ignorer le rendez-vous en cours d'édition
                        if (selectedRendezVous != null && rv.getId() == selectedRendezVous.getId()) continue;
                        if (rv.getDate() != null && rv.getDate().isEqual(dateTime)) {
                            errorMsg.append("- Le médecin a déjà un rendez-vous à cette date/heure.\n");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                errorMsg.append("- Erreur lors de la vérification des chevauchements de rendez-vous.\n");
            }
        }
        if (errorMsg.length() > 0) {
            showError(errorMsg.toString());
            return false;
        }
        return true;
    }

    @FXML
    private void ajouterRendezVous() {
        if (!validateInputs()) return;
        try {
            String motif = motifField.getText();
            User patient = patientComboBox.getValue();
            int patientId = patient != null ? patient.getId() : -1;
            int medecinId = Integer.parseInt(medecinIdField.getText());
            LocalDate date = datePicker.getValue();
            String heure = heureCombo.getValue();
            String minute = minuteCombo.getValue();
            String statut = statutComboBox.getValue();
            LocalDateTime dateTime = date.atTime(Integer.parseInt(heure), Integer.parseInt(minute));
            RendezVous rv = new RendezVous();
            rv.setMotif(motif);
            rv.setPatientId(patientId);
            rv.setMedecinId(medecinId);
            rv.setDate(dateTime);
            rv.setStatut(statut);
            service.ajouterRendezVous(rv);
            rafraichirListe();
            clearFields();
            showSuccess("Le rendez-vous a bien été ajouté !");
        } catch (Exception e) {
            showError("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void modifierRendezVous() {
        if (selectedRendezVous == null) {
            showError("Veuillez sélectionner un rendez-vous à modifier.");
            return;
        }
        if (!validateInputs()) return;
        try {
            String motif = motifField.getText();
            User patient = patientComboBox.getValue();
            int patientId = patient != null ? patient.getId() : -1;
            int medecinId = Integer.parseInt(medecinIdField.getText());
            LocalDate date = datePicker.getValue();
            String heure = heureCombo.getValue();
            String minute = minuteCombo.getValue();
            String statut = statutComboBox.getValue();
            LocalDateTime dateTime = date.atTime(Integer.parseInt(heure), Integer.parseInt(minute));
            selectedRendezVous.setMotif(motif);
            selectedRendezVous.setPatientId(patientId);
            selectedRendezVous.setMedecinId(medecinId);
            selectedRendezVous.setDate(dateTime);
            selectedRendezVous.setStatut(statut);
            service.modifierRendezVous(selectedRendezVous);
            rafraichirListe();
            clearFields();
            showSuccess("Le rendez-vous a bien été modifié !");
        } catch (Exception e) {
            showError("Erreur lors de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void supprimerRendezVous() {
        RendezVous selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Suppression");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un rendez-vous à supprimer.");
            alert.showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce rendez-vous ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                service.supprimerRendezVous(selected.getId());
                rafraichirListe();
            }
        });
    }

    private void clearFields() {
        motifField.clear();
        patientComboBox.setValue(null);
        medecinIdField.clear();
        datePicker.setValue(null);
        heureCombo.setValue(null);
        minuteCombo.setValue(null);
        statutComboBox.setValue(null);
        selectedRendezVous = null;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
