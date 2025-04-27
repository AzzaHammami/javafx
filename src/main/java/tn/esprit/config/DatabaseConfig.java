package tn.esprit.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DatabaseConfig {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static final String URL = "jdbc:mysql://localhost:3306/symfony_project";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("Driver MySQL chargé avec succès");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver non trouvé", e);
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            LOGGER.info("Tentative de connexion à la base de données...");
            LOGGER.info("URL de connexion: " + URL);
            LOGGER.info("Utilisateur: " + USER);
            
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            LOGGER.info("Connexion à la base de données établie avec succès");
            
            // Tester la connexion
            testConnection(conn);
            
            return conn;
        } catch (SQLException e) {
            LOGGER.severe("Erreur de connexion à la base de données: " + e.getMessage());
            LOGGER.severe("État de la base de données: " + e.getSQLState());
            LOGGER.severe("Code d'erreur: " + e.getErrorCode());
            e.printStackTrace();
            throw new SQLException("Erreur de connexion à la base de données: " + e.getMessage(), e);
        }
    }

    private static void testConnection(Connection conn) {
        try {
            // Vérifier si la connexion est valide
            if (conn.isValid(5)) {
                LOGGER.info("La connexion est valide");
            } else {
                LOGGER.severe("La connexion n'est pas valide");
                return;
            }

            // Tester la lecture de la table user_profile
            try (PreparedStatement stmt = conn.prepareStatement("SHOW TABLES LIKE 'user_profile'")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    LOGGER.info("Table user_profile existe");
                    
                    // Vérifier la structure de la table
                    try (PreparedStatement descStmt = conn.prepareStatement("DESCRIBE user_profile")) {
                        ResultSet descRs = descStmt.executeQuery();
                        LOGGER.info("Structure de la table user_profile:");
                        while (descRs.next()) {
                            LOGGER.info(descRs.getString("Field") + " - " + descRs.getString("Type"));
                        }
                    }
                } else {
                    LOGGER.severe("Table user_profile n'existe pas!");
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors du test de la connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 