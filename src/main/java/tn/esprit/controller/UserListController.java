package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import tn.esprit.model.User;
import tn.esprit.util.SessionManager;
import tn.esprit.config.DatabaseConfig;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

public class UserListController {
    private static final Logger LOGGER = Logger.getLogger(UserListController.class.getName());

    @FXML
    private FlowPane userCardsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Pagination pagination;

    @FXML
    private Circle userAvatar;

    private int itemsPerPage = 12;
    private List<User> allUsers = new ArrayList<>();

    @FXML
    public void initialize() {
        loadUsers();
        setupSearch();
        setupPagination();
    }

    public void loadUsers() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            LOGGER.info("Connexion à la base de données établie");
        
            // Vérifier les tables disponibles
            ResultSet tables = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                LOGGER.info("Table trouvée : " + tables.getString("TABLE_NAME"));
            }
            
            String sql = "SELECT * FROM user";  // Changed from "users" to "user"
            LOGGER.info("Exécution de la requête : " + sql);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                allUsers.clear();
                
                int count = 0;
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("roles")); // Using "roles" column name
                    allUsers.add(user);
                    count++;
                }
                
                LOGGER.info("Nombre d'utilisateurs chargés : " + count);
                
                updatePagination();
                showPage(0);
    }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des utilisateurs", e);
            // Afficher une alerte à l'utilisateur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de chargement");
            alert.setContentText("Impossible de charger la liste des utilisateurs : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void createUserCard(User user) {
        VBox card = new VBox(10);
        card.getStyleClass().add("user-card");
        card.setPrefWidth(300);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);

        // Avatar
        Circle avatar = new Circle(40);
        avatar.getStyleClass().add("user-avatar");
        
        // Informations utilisateur
        Label emailLabel = new Label(user.getEmail());
        emailLabel.getStyleClass().add("user-email");
        
        Label roleLabel = new Label(formatRole(user.getRole()));
        roleLabel.getStyleClass().add("user-role");

        // Boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);

        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(e -> handleEditUser(user));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> handleDeleteUser(user));

        actions.getChildren().addAll(editButton, deleteButton);

        card.getChildren().addAll(avatar, emailLabel, roleLabel, actions);
        userCardsContainer.getChildren().add(card);
    }

    private String formatRole(String role) {
        if (role == null) return "Inconnu";
        switch (role.toUpperCase()) {
            case "ROLE_ADMIN": return "Administrateur";
            case "ROLE_DOCTOR": return "Médecin";
            case "ROLE_PATIENT": return "Patient";
            default: return role;
        }
    }

    private void setupSearch() {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }

    private void filterUsers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            updatePagination();
            showPage(0);
            return;
                        }

        List<User> filteredUsers = allUsers.stream()
            .filter(user -> 
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText.toLowerCase())) ||
                (user.getRole() != null && formatRole(user.getRole()).toLowerCase().contains(searchText.toLowerCase()))
            )
            .toList();

        userCardsContainer.getChildren().clear();
        filteredUsers.forEach(this::createUserCard);
    }

    private void setupPagination() {
        pagination.setPageCount(calculatePageCount());
            pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(5);
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> 
            showPage(newIndex.intValue()));
        }

    private int calculatePageCount() {
        return (allUsers.size() + itemsPerPage - 1) / itemsPerPage;
    }

    private void updatePagination() {
        pagination.setPageCount(calculatePageCount());
    }

    private void showPage(int pageIndex) {
        userCardsContainer.getChildren().clear();
        int start = pageIndex * itemsPerPage;
        int end = Math.min(start + itemsPerPage, allUsers.size());
        
        for (int i = start; i < end; i++) {
            createUserCard(allUsers.get(i));
        }
    }

    @FXML
    private void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-user.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter un utilisateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Recharger la liste après l'ajout
            loadUsers();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture du formulaire d'ajout", e);
        }
    }

    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-user.fxml"));
            Parent root = loader.load();
            
            EditUserController controller = loader.getController();
            controller.setUser(user);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier l'utilisateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Recharger la liste après la modification
            loadUsers();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture du formulaire de modification", e);
        }
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'utilisateur");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cet utilisateur ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM user WHERE id = ?")) {
                
                stmt.setLong(1, user.getId());
                stmt.executeUpdate();

                // Recharger la liste après la suppression
                loadUsers();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de l'utilisateur", e);
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Erreur de suppression");
                errorAlert.setContentText("Impossible de supprimer l'utilisateur.");
                errorAlert.showAndWait();
            }
        }
    }

    @FXML
    private void handleAccueil() {
        // Navigation vers l'accueil
    }

    @FXML
    private void handleStatistiques() {
        // Navigation vers les statistiques
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.clearUserSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userAvatar.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la déconnexion", e);
        }
    }
} 