package com.example.mypasswordmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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

    public static final String BREACH_CHANNEL_ID = "breach-notification-channel-id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PasswordDb db = PasswordDb.getDatabase(this);
        passwordDao = db.passwordDao();
        passwordsRV = findViewById(R.id.passwordsRV);
        passwords = new ArrayList<>();

        // initializing adapter class and passing arraylist to it
        passwordsAdapter = new PasswordListAdapter(this, passwords);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        passwordsRV.setLayoutManager(linearLayoutManager);
        passwordsRV.setAdapter(passwordsAdapter);

        // schedule periodic breach checks with Have I been pwned API
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build();
        PeriodicWorkRequest pwnCheckRequest = new PeriodicWorkRequest.Builder(PwnCheckWorker.class, 12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("pwnCheckRequest",
                ExistingPeriodicWorkPolicy.KEEP, pwnCheckRequest);

        // create notification channel (Oreo)
        createNotificationChannel();
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    public void editPassword(View view, Password password) {
        // when the edit button is clicked, user is prompted to the CreatePassword activity
        Intent Intent = new Intent(this, CreatePasswordActivity.class);
        // send details of the password to be edited to the CreatePasswordActivity
        Intent.putExtra("label", password.label);
        Intent.putExtra("password", password.password);
        Intent.putExtra("website", password.website);
        Intent.putExtra("id", Integer.toString(password.id));
        startActivity(Intent);
    }

    public void deletePassword(View view, Password password){
        mDisposable.add(passwordDao.delete(password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
        Toast.makeText(getApplicationContext(), "Password deleted!", Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.breach_channel_name);
            String description = getString(R.string.channel_description);
            NotificationChannel channel = new NotificationChannel(BREACH_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}