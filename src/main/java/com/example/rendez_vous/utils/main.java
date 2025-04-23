package com.example.rendez_vous.utils;

import java.sql.Connection;

public class main {
    public static void main(String[] args) {
        // Tester la connexion
        Connection cnx = MyDataBase.getInstance().getConnection();

        if (cnx != null) {
            System.out.println("✅ Connexion réussie !");
        } else {
            System.out.println("❌ Échec de la connexion.");
        }
    }
}
