package com.example.mypasswordmanager;

public class PasswordData {

    private String label;
    private String password;
    private String website;

    // Constructor
    public PasswordData(String label, String password, String website) {
        this.label = label;
        this.password = password;
        this.website = website;
    }

    // Getter and Setter
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
