package com.example.mypasswordmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private PasswordDao passwordDao;

    // for making asynchronous calls to DB
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private RecyclerView passwordsRV;

    private List<Password> passwords;
    private PasswordListAdapter passwordsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PasswordDb db = PasswordDb.getDatabase(this);
        passwordDao = db.passwordDao();
        passwordsRV = findViewById(R.id.passwordsRV);
        passwords = new ArrayList<>();
        loadPasswords();

        // initializing adapter class and passing arraylist to it
        passwordsAdapter = new PasswordListAdapter(this, passwords);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        passwordsRV.setLayoutManager(linearLayoutManager);
        passwordsRV.setAdapter(passwordsAdapter);
    }

    private void loadPasswords() {
        // read the passwords and display labels on textview
        mDisposable.add(passwordDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onbData,
                        throwable -> Log.e("MainActivity", "Failed to read data")));
    }

    private void onbData(List<Password> newPasswords) {
        this.passwords.clear();
        this.passwords.addAll(newPasswords);
        passwordsAdapter.notifyDataSetChanged();
        Log.i("MainActivity", "Updated textview");
    }

    public void addPassword(View view) {
        // when the floating button is clicked, user is prompted to the CreatePassword activity
        Intent Intent = new Intent(this, CreatePasswordActivity.class);
        startActivity(Intent);
    }
}