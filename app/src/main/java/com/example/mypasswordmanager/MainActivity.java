package com.example.mypasswordmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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

    // for making asynchronous calls to DB
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helloTextview = findViewById(R.id.hello_textview);

        PasswordDb db = PasswordDb.getDatabase(this);
        passwordDao = db.passwordDao();

        // write some passwords (async)
        Password p1 = new Password();
        p1.label = "label1";
        Password p2 = new Password();
        p2.label = "label2";
        mDisposable.add(passwordDao.insertAll(p1, p2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onDbUpdated));
        onDbUpdated();
    }

    private void onDbUpdated() {
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
            labels.append(password.label).append(" ");
        }
        helloTextview.setText(labels.toString());
        Log.i("MainActivity", "Updated textview");
    }

    @Override
    public void onBackPressed() {
        // back press would normally return to authorize activity, instead exit app
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}