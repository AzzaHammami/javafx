package com.example.rendez_vous.models;

import java.util.List;

public class User {
    private int id;
    private String email;
    private String name;
    private List<String> roles;
    private String password;
    private String imageUrl;
    private String specialite;
    private String adresse;

    public User() {}

    public User(int id, String email, String name, List<String> roles, String password, String imageUrl) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.roles = roles;
        this.password = password;
        this.imageUrl = imageUrl;
    }

    // Constructeur sans imageUrl (pour compatibilité)
    public User(int id, String email, String name, List<String> roles, String password) {
        this(id, email, name, roles, password, null);
    }

    public User(int id, String email, String name, List<String> roles, String password, String imageUrl, String specialite, String adresse) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.roles = roles;
        this.password = password;
        this.imageUrl = imageUrl;
        this.specialite = specialite;
        this.adresse = adresse;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", roles=" + roles +
                ", password='" + password + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", specialite='" + specialite + '\'' +
                ", adresse='" + adresse + '\'' +
                '}';
    }
}
