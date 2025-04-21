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

public class UserListController {

    @FXML
    private TableView<User> userTable;
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

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();
    }

    private void setupTable() {
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("roles"));
        
        // Activer le tri pour les colonnes
        emailColumn.setSortable(true);
        roleColumn.setSortable(true);
        
        // Ajouter un comparateur personnalisé pour la colonne des rôles
        roleColumn.setComparator((String role1, String role2) -> {
            // Convertir les rôles en format lisible pour l'affichage
            String displayRole1 = convertRoleToDisplay(role1);
            String displayRole2 = convertRoleToDisplay(role2);
            return displayRole1.compareTo(displayRole2);
        });
        
        setupActionsColumn();
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

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttons = new HBox(10, editButton, deleteButton);

            {
                buttons.getStyleClass().add("action-cell");
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                this.getStyleClass().add("action-column");
                
                editButton.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        handleEditUser(user);
                    }
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        handleDeleteUser(user);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadData() {
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
            showError("Erreur lors du chargement des données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (filteredData != null) {
                    filteredData.setPredicate(user -> {
                        if (newValue == null || newValue.isEmpty()) {
                            return true;
                        }

                        String lowerCaseFilter = newValue.toLowerCase().trim();

                        if (user.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }

                        if (user.getRoles().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }

                        return false;
                    });

                    updatePagination();
                    
                    if (pagination != null) {
                        pagination.setCurrentPageIndex(0);
                    }
                    
                    createPage(0);
                }
            });
        }
    }

    private void setupPagination() {
        if (filteredData != null && pagination != null) {
            int totalItems = filteredData.size();
            int pageCount = Math.max(1, (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE));
            pagination.setPageCount(pageCount);
            pagination.setCurrentPageIndex(0);
            pagination.setPageFactory(this::createPage);
        }
    }

    private void updatePagination() {
        if (filteredData != null && pagination != null) {
            int totalItems = filteredData.size();
            int pageCount = Math.max(1, (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE));
            pagination.setPageCount(pageCount);
        }
    }

    private TableView<User> createPage(int pageIndex) {
        if (filteredData == null) {
            return userTable;
        }

        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredData.size());

        // Créer une nouvelle instance de TableView avec les mêmes colonnes
        TableView<User> pageTable = new TableView<>();
        pageTable.getStyleClass().add("user-table");
        pageTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Copier les colonnes existantes
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(300);
        emailCol.setSortable(true);
        
        TableColumn<User, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("roles"));
        roleCol.setPrefWidth(200);
        roleCol.setSortable(true);
        
        // Ajouter un comparateur personnalisé pour la colonne des rôles
        roleCol.setComparator((String role1, String role2) -> {
            // Convertir les rôles en format lisible pour l'affichage
            String displayRole1 = convertRoleToDisplay(role1);
            String displayRole2 = convertRoleToDisplay(role2);
            return displayRole1.compareTo(displayRole2);
        });
        
        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(450);
        actionsCol.setSortable(false); // La colonne des actions n'est pas triable
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox buttons = new HBox(10, editButton, deleteButton);

            {
                buttons.getStyleClass().add("action-cell");
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                this.getStyleClass().add("action-column");
                
                editButton.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        handleEditUser(user);
                    }
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableRow().getItem();
                    if (user != null) {
                        handleDeleteUser(user);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
        
        // Ajouter les colonnes à la nouvelle table
        pageTable.getColumns().addAll(emailCol, roleCol, actionsCol);
        
        // Définir les données
        ObservableList<User> pageData;
        if (fromIndex >= filteredData.size()) {
            pageData = FXCollections.observableArrayList();
        } else {
            pageData = FXCollections.observableArrayList(
                filteredData.subList(fromIndex, toIndex)
            );
        }
        pageTable.setItems(pageData);
        
        // Définir la taille minimale de la table
        pageTable.setMinWidth(600);
        pageTable.setMinHeight(400);
        
        return pageTable;
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
            controller.setAdminController(null);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'utilisateur");
            stage.showAndWait();
            
            loadData();
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
            } catch (SQLException e) {
                showError("Erreur lors de la suppression de l'utilisateur");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAccueil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Tableau de bord");
        } catch (Exception e) {
            showError("Erreur lors du chargement du tableau de bord");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistics.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques");
        } catch (Exception e) {
            showError("Erreur lors du chargement des statistiques");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (Exception e) {
            showError("Erreur lors de la déconnexion");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 