package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.model.User;
import tn.esprit.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AdminDashboardController {

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalDoctorsLabel;
    @FXML
    private Label totalPatientsLabel;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Long> idColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, Void> actionsColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Pagination pagination;

    private ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;
    private static final int ITEMS_PER_PAGE = 10;

    private User currentUser;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();
        updateStatistics();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roles"));
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");

                editButton.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    handleEditUser(user);
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    public void loadData() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String query = "SELECT id, email, password, roles FROM user";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            masterData.clear();
            while (rs.next()) {
                User user = new User(
                    rs.getLong("id"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("roles")
                );
                masterData.add(user);
            }

            filteredData = new FilteredList<>(masterData, p -> true);
            userTable.setItems(filteredData);
            
            setupPagination();
        } catch (SQLException e) {
            showError("Erreur lors du chargement des données");
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                return user.getEmail().toLowerCase().contains(lowerCaseFilter)
                    || user.getRoles().toLowerCase().contains(lowerCaseFilter);
            });
            updatePagination();
        });
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) filteredData.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredData.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount);
    }

    private TableView<User> createPage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredData.size());
        
        // Créer une nouvelle instance de TableView avec les mêmes colonnes
        TableView<User> pageTable = new TableView<>();
        pageTable.getStyleClass().add("user-table");
        pageTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Copier les colonnes existantes
        TableColumn<User, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setSortable(true);
        
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        emailCol.setSortable(true);
        
        TableColumn<User, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("roles"));
        roleCol.setPrefWidth(150);
        roleCol.setSortable(true);
        
        // Ajouter un comparateur personnalisé pour la colonne des rôles
        roleCol.setComparator((String role1, String role2) -> {
            // Convertir les rôles en format lisible pour l'affichage
            String displayRole1 = convertRoleToDisplay(role1);
            String displayRole2 = convertRoleToDisplay(role2);
            return displayRole1.compareTo(displayRole2);
        });
        
        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setSortable(false); // La colonne des actions n'est pas triable
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");

                editButton.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    handleEditUser(user);
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        // Ajouter les colonnes à la nouvelle table
        pageTable.getColumns().addAll(idCol, emailCol, roleCol, actionsCol);
        
        // Définir les données
        ObservableList<User> pageData = FXCollections.observableArrayList(
            filteredData.subList(fromIndex, toIndex)
        );
        pageTable.setItems(pageData);
        
        // Définir la taille minimale de la table
        pageTable.setMinWidth(600);
        pageTable.setMinHeight(400);
        
        return pageTable;
    }

    /**
     * Convertit un rôle technique en format d'affichage lisible
     */
    private String convertRoleToDisplay(String role) {
        if (role == null) return "";
        
        switch (role) {
            case "ROLE_ADMIN":
                return "Administrateur";
            case "ROLE_MEDECIN":
                return "Médecin";
            case "ROLE_PATIENT":
                return "Patient";
            default:
                return role;
        }
    }

    public void updateStatistics() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Total utilisateurs
            String totalQuery = "SELECT COUNT(*) as total FROM user";
            PreparedStatement totalStmt = conn.prepareStatement(totalQuery);
            ResultSet totalRs = totalStmt.executeQuery();
            if (totalRs.next()) {
                totalUsersLabel.setText(String.valueOf(totalRs.getInt("total")));
            }

            // Total médecins
            String doctorsQuery = "SELECT COUNT(*) as total FROM user WHERE roles = 'ROLE_MEDECIN'";
            PreparedStatement doctorsStmt = conn.prepareStatement(doctorsQuery);
            ResultSet doctorsRs = doctorsStmt.executeQuery();
            if (doctorsRs.next()) {
                totalDoctorsLabel.setText(String.valueOf(doctorsRs.getInt("total")));
            }

            // Total patients
            String patientsQuery = "SELECT COUNT(*) as total FROM user WHERE roles = 'ROLE_PATIENT'";
            PreparedStatement patientsStmt = conn.prepareStatement(patientsQuery);
            ResultSet patientsRs = patientsStmt.executeQuery();
            if (patientsRs.next()) {
                totalPatientsLabel.setText(String.valueOf(patientsRs.getInt("total")));
            }
        } catch (SQLException e) {
            showError("Erreur lors du chargement des statistiques");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un utilisateur");
            stage.showAndWait();
            
            loadData();
            updateStatistics();
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture du formulaire d'ajout");
            e.printStackTrace();
        }
    }

    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-form.fxml"));
            Parent root = loader.load();
            
            UserFormController controller = loader.getController();
            controller.setUser(user);
            controller.setAdminController(this);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'utilisateur");
            stage.showAndWait();
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture du formulaire de modification");
            e.printStackTrace();
        }
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'utilisateur");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'utilisateur " + user.getEmail() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                String query = "DELETE FROM user WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setLong(1, user.getId());
                stmt.executeUpdate();

                loadData();
                updateStatistics();
            } catch (SQLException e) {
                showError("Erreur lors de la suppression de l'utilisateur");
                e.printStackTrace();
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDashboard() {
        // Déjà sur le tableau de bord
    }

    @FXML
    private void handleUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Utilisateurs");
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la liste des utilisateurs");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (Exception e) {
            showError("Erreur lors de la déconnexion");
            e.printStackTrace();
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
    }
} 