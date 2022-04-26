package com.example.mypasswordmanager;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PasswordDao {
    @Query("SELECT * FROM password")
    Flowable<List<Password>> getAll();

    @Insert
    Completable insert(Password password);

    @Insert
    Completable insertAll(Password... passwords);

    @Update
    Completable update(Password password);

    @Delete
    Completable delete(Password password);
}
