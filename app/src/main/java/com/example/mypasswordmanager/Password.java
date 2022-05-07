package com.example.mypasswordmanager;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Password {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "label")
    public String label;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "website")
    public String website;

    @ColumnInfo(name = "was_generated_randomly")
    public boolean wasGeneratedRandomly;

    @ColumnInfo(name = "was_pwned")
    public boolean wasPwned;

    @ColumnInfo(name = "breach_info")
    public boolean breachInfo;

    @ColumnInfo(name = "has_letters")
    public boolean hasLetters;

    @ColumnInfo(name = "has_numbers")
    public boolean hasNumbers;

    @ColumnInfo(name = "has_uppercase")
    public boolean hasUppercase;

    @ColumnInfo(name = "has_special_characters")
    public boolean hasSpecialCharacters;
}
