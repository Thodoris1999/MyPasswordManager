package com.example.mypasswordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    PasswordDao passwordDao;
    TextView helloTextview;
    String mainMessage = "Hello World";

    // for making asynchronous calls to DB
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helloTextview = findViewById(R.id.hello_textview);
        helloTextview.setText(mainMessage);
        PasswordDb db = PasswordDb.getDatabase(this);
        passwordDao = db.passwordDao();

        loadPasswords();
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
            labels.append(password.label).append(":");
            labels.append(password.password).append(" ");
        }
        helloTextview.setText(labels.toString());
        Log.i("MainActivity", "Updated textview");
    }

    public void addPassword(View view) {
        // when the floating button is clicked, user is prompted to the CreatePassword activity
        Intent Intent = new Intent(this, CreatePasswordActivity.class);
        startActivity(Intent);
    }
}