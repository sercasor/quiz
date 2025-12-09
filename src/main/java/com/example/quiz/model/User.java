package com.example.quiz.model;
import jakarta.validation.constraints.*;

public class User {
    @NotEmpty(message="Debes insertar un nombre de usuario")
    @Size(min=4, max=100, message="El nombre debe tener un mínimo de 4 caracteres")
    private String username; // Username of the user
    @NotEmpty(message="Debes insertar una password")
    private String password; // Password of the user (encoded)
    @NotEmpty(message="Debes insertar un rol ")
    private String role;
    @Email(message="Debes insertar un email con un formato adecuado")
    private String email;



    public User() {
    }

    public User(String username, String password, String role, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
