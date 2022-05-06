package com.example.mypasswordmanager;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Random;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CreatePasswordActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {
    TextView createModeTextview;
    TextView includeTextview;
    String createModeMessage = "Select password mode";
    String[] create_mode = {"Save my password", "Generate password"};
    TextInputLayout name;
    TextInputLayout password;
    TextInputLayout length;
    TextInputLayout website;
    MaterialCheckBox numbers;
    MaterialCheckBox letters;
    MaterialCheckBox uppercase;
    MaterialCheckBox specialChars;
    PasswordDao passwordDao;
    Password p;
    Boolean generatePassword;
    Boolean hasNumbers;
    Boolean hasUppercase;
    Boolean hasSpecialChars;
    Boolean hasLetters;
    // for making asynchronous calls to DB
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    // functions needed for new password mode selection (save/generate) on drop-down menu
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        if (position == 0) {
            generatePassword = false;
            // show/hide fields according to selected mode
            length.setVisibility(View.GONE);
            name.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            website.setVisibility(View.VISIBLE);
            numbers.setVisibility(View.GONE);
            uppercase.setVisibility(View.GONE);
            specialChars.setVisibility(View.GONE);
            letters.setVisibility(View.GONE);
            includeTextview.setVisibility(View.GONE);
        } else if (position == 1) {
            generatePassword = true;
            length.setVisibility(View.VISIBLE);
            name.setVisibility(View.VISIBLE);
            password.setVisibility(View.GONE);
            website.setVisibility(View.VISIBLE);
            numbers.setVisibility(View.VISIBLE);
            uppercase.setVisibility(View.VISIBLE);
            specialChars.setVisibility(View.VISIBLE);
            letters.setVisibility(View.VISIBLE);
            includeTextview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_activity);
        Spinner spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);
        createModeTextview = findViewById(R.id.select_create_mode);
        createModeTextview.setText(createModeMessage);
        includeTextview = findViewById(R.id.include);
        name = findViewById(R.id.password_label);
        password = findViewById(R.id.password);
        length = findViewById(R.id.password_length);
        website = findViewById(R.id.password_website);
        numbers = findViewById(R.id.include_numbers);
        letters = findViewById(R.id.include_letters);
        uppercase = findViewById(R.id.include_uppercase);
        specialChars = findViewById(R.id.include_special);
        // hide 'generate password' fields for the default option
        numbers.setVisibility(View.GONE);
        uppercase.setVisibility(View.GONE);
        specialChars.setVisibility(View.GONE);
        letters.setVisibility(View.GONE);
        includeTextview.setVisibility(View.GONE);
        PasswordDb db = PasswordDb.getDatabase(this);
        passwordDao = db.passwordDao();
        p = new Password();
        generatePassword = false;
        hasNumbers = true;
        hasUppercase = true;
        hasSpecialChars = true;
        hasLetters = true;
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, create_mode);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);
        // event listeners for checkbox changes
        numbers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean includeNumbers) {
                hasNumbers = includeNumbers;
            }
        });

        letters.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean includeLetters) {
                hasLetters = includeLetters;
            }
        });
        uppercase.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean includeUppercase) {
                hasUppercase = includeUppercase;
            }
        });
        specialChars.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean includeSpecialChars) {
                hasSpecialChars = includeSpecialChars;
            }
        });
    }

    public void savePassword(View view) {
        p.label = name.getEditText().getText().toString();
        p.website = website.getEditText().getText().toString();
        p.wasGeneratedRandomly = generatePassword;
        p.hasLetters = hasLetters;
        p.hasUppercase = hasUppercase;
        p.hasSpecialCharacters = hasSpecialChars;
        if (generatePassword) {
            if(!hasLetters && !hasNumbers && !hasUppercase && !hasSpecialChars){
                Toast.makeText(getApplicationContext(), "Please select one of the Include options!", Toast.LENGTH_LONG).show();
                return ;
            }
            if(!length.getEditText().getText().toString().matches(""))
                p.password = generatePassword(Integer.valueOf(length.getEditText().getText().toString()));
            else {
                Toast.makeText(getApplicationContext(), "Password can't be empty!", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            p.password = password.getEditText().getText().toString();
        }
        mDisposable.add(passwordDao.insert(p)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
        Toast.makeText(getApplicationContext(), "Password saved!", Toast.LENGTH_LONG).show();
        Intent Intent = new Intent(this, MainActivity.class);
        startActivity(Intent);
    }

    public String generatePassword(int length) {
        Random rnd = new Random();
        final String uppercase =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lowercase = uppercase.toLowerCase(Locale.ROOT);
        final String digits = "0123456789";
        final String specialChars = "$&@?<>~!%#";
        String characterOptions = "";
        if (hasLetters)
            characterOptions += lowercase;
        if (hasNumbers)
            characterOptions += digits;
        if (hasUppercase)
            characterOptions += uppercase;
        if (hasSpecialChars)
            characterOptions += specialChars;
        StringBuilder generatedPassword = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            generatedPassword.append(characterOptions.charAt(rnd.nextInt(characterOptions.length())));
        return generatedPassword.toString();
    }

}

