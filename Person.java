package com.acme;

import jakarta.enterprise.context.ApplicationScoped;

// @ApplicationScoped
// E' disponibile a fornire i suoi metodi,
// importante per l'injection,
// inutile se usato il costruttore però

public class Person {
    private String userID, username, email, password;

    public Person(String userID, String username, String email, String password){
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }
}
