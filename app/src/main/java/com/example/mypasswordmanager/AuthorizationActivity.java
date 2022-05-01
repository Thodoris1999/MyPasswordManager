package com.example.mypasswordmanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Single;

public class AuthorizationActivity extends AppCompatActivity {
    private EditText passwordEdittext;
    private TextView passwordError;

    private final Preferences.Key<String> PASSWORD_KEY = PreferencesKeys.stringKey("password");
    private RxDataStore<Preferences> dataStore;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        passwordEdittext = findViewById(R.id.passwordEdittext);
        TextView passwordPrompt = findViewById(R.id.passwordPrompt);
        passwordError = findViewById(R.id.passwordError);
        final Button authorizeButton = findViewById(R.id.authorizeButton);

        // if we open app for the first time, prompt for password
        dataStore = new RxPreferenceDataStoreBuilder(this, "password").build();
        preferences = dataStore.data().blockingFirst();
        Log.i("AuthorizationActivity", preferences.toString());
        if (!preferences.contains(PASSWORD_KEY)) {
            passwordPrompt.setText(R.string.password_prompt_first_time_set);
            authorizeButton.setText(R.string.authorization_button_first_time_set);
        }

        authorizeButton.setOnClickListener(this::authorize);
    }

    private void authorize(View view) {
        String passwordInput = passwordEdittext.getText().toString();
        if (!preferences.contains(PASSWORD_KEY)) {
            dataStore.updateDataAsync(prefsIn -> {
                MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                mutablePreferences.set(PASSWORD_KEY, passwordInput);
                return Single.just(mutablePreferences);
            });
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            String password = preferences.get(PASSWORD_KEY).toString();
            if (password.equals(passwordInput)) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                passwordError.setVisibility(View.VISIBLE);
            }
        }
    }
}