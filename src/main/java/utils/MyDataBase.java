package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {


    private final String URL = "jdbc:mysql://localhost:3306/gestion_reclamations";
    private final String USER = "root";
    private final String PASSWORD = "";


    private Connection connection;


    private static MyDataBase instance;


    private MyDataBase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println(" Connexion à la base de données réussie !");
        } catch (SQLException e) {
            System.err.println(" Erreur de connexion : " + e.getMessage());
        }
    }


    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }


    public Connection getConnection() {
        return connection;
    }
}
