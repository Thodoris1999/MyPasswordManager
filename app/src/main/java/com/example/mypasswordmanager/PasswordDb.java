package com.example.mypasswordmanager;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Password.class}, version = 1)
public abstract class PasswordDb extends RoomDatabase {
    public abstract PasswordDao passwordDao();

    // Maintain singleton instance in order to access from all activities
    private static volatile PasswordDb INSTANCE;

    static PasswordDb getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PasswordDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), PasswordDb.class,
                            "password_db").build();
                }
            }
        }
        return INSTANCE;
    }
}
