package com.example.mypasswordmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    PasswordDao passwordDao;

    // for making asynchronous calls to DB
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    RecyclerView passwordsRV;

    // Arraylist for storing data
    ArrayList<PasswordData> passwordsArrayList;

    PasswordListAdapter passwordsAdapter;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PasswordDb db = PasswordDb.getDatabase(this);
        passwordDao = db.passwordDao();
        passwordsRV = findViewById(R.id.passwordsRV);
        passwordsArrayList = new ArrayList<>();
        loadPasswords();
        // initializing adapter class and passing arraylist to it
        passwordsAdapter = new PasswordListAdapter(this, passwordsArrayList);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
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

    private void onbData(List<Password> passwords) {
        StringBuilder labels = new StringBuilder();
        for (Password password : passwords) {
            passwordsArrayList.add(new PasswordData(password.label, password.password, password.website));
            passwordsAdapter.notifyDataSetChanged();
        }
        Log.i("MainActivity", "Updated textview");
    }

    public void addPassword(View view) {
        // when the floating button is clicked, user is prompted to the CreatePassword activity
        Intent Intent = new Intent(this, CreatePasswordActivity.class);
        startActivity(Intent);
    }
}