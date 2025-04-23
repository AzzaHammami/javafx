package com.example.rendez_vous.utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private final String url = "jdbc:mysql://localhost:3306/rendez_vous";
    private final String user = "root";
    private final String password = "";

    private static MyDataBase instance;
    private static Connection connection;

    private MyDataBase() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connected to database successfully");
        } catch (SQLException e) {
            System.err.println("❌ Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public static Connection getConnection() {
        return connection;
    }
}
