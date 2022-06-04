package com.example.mypasswordmanager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

<<<<<<< HEAD
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
=======
import java.util.ArrayList;
import java.util.List;
>>>>>>> 4d7f1ea2d5848eda4310a0a9658ee2cd1d5d0501
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CreatePasswordActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {
    private TextView includeTextview;
    private Button saveButton;
    private TextInputLayout nameInput;
    private TextInputLayout passwordInput;
    private TextInputLayout lengthInput;
    private TextInputLayout websiteInput;
    private MaterialCheckBox numbersCheckbox;
    private MaterialCheckBox lettersCheckbox;
    private MaterialCheckBox uppercaseCheckbox;
    private MaterialCheckBox specialCharsCheckbox;
    private List<Password> passwords;
    private final String[] create_mode = {"Save my password", "Generate password"};
    private PasswordDao passwordDao;
    private Password p;
    private boolean generatePassword;
    private boolean editMode;
    private int passwordID ;
    // for making asynchronous calls to DB
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    // functions needed for new password mode selection (save/generate) on drop-down menu

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        if (position == 0) {
            generatePassword = false;
            // show/hide fields according to selected mode
            lengthInput.setVisibility(View.GONE);
            nameInput.setVisibility(View.VISIBLE);
            passwordInput.setVisibility(View.VISIBLE);
            websiteInput.setVisibility(View.VISIBLE);
            numbersCheckbox.setVisibility(View.GONE);
            uppercaseCheckbox.setVisibility(View.GONE);
            specialCharsCheckbox.setVisibility(View.GONE);
            lettersCheckbox.setVisibility(View.GONE);
            includeTextview.setVisibility(View.GONE);
        } else if (position == 1) {
            generatePassword = true;
            lengthInput.setVisibility(View.VISIBLE);
            nameInput.setVisibility(View.VISIBLE);
            passwordInput.setVisibility(View.GONE);
            websiteInput.setVisibility(View.VISIBLE);
            numbersCheckbox.setVisibility(View.VISIBLE);
            uppercaseCheckbox.setVisibility(View.VISIBLE);
            specialCharsCheckbox.setVisibility(View.VISIBLE);
            lettersCheckbox.setVisibility(View.VISIBLE);
            includeTextview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPasswords();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_activity);
        Spinner spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);
        TextView createModeTextview = findViewById(R.id.select_create_mode);
        createModeTextview.setText(R.string.password_creation_mode_prompt);
        saveButton=(Button)findViewById(R.id.button);
        includeTextview = findViewById(R.id.include);
        nameInput = findViewById(R.id.password_label);
        passwordInput = findViewById(R.id.password);
        lengthInput = findViewById(R.id.password_length);
        websiteInput = findViewById(R.id.password_website);
        numbersCheckbox = findViewById(R.id.include_numbers);
        lettersCheckbox = findViewById(R.id.include_letters);
        uppercaseCheckbox = findViewById(R.id.include_uppercase);
        specialCharsCheckbox = findViewById(R.id.include_special);
        // hide 'generate password' fields for the default option
        numbersCheckbox.setVisibility(View.GONE);
        uppercaseCheckbox.setVisibility(View.GONE);
        specialCharsCheckbox.setVisibility(View.GONE);
        lettersCheckbox.setVisibility(View.GONE);
        includeTextview.setVisibility(View.GONE);
        lengthInput.setVisibility(View.GONE);
        editMode = false;
        passwords = new ArrayList<>();
        // edit password mode, get password details to be updated, sent from intent
        Intent intent = getIntent();
        String password = intent.getStringExtra("password");
        String label = intent.getStringExtra("label");
        String website = intent.getStringExtra("website");
        String id = intent.getStringExtra("id");
        if(password != null && label != null && website != null && id!= null){
            saveButton.setText("UPDATE");
            editMode = true;
            // hide "select save/generate mode" spinner
            spin.setVisibility(View.GONE);
            createModeTextview.setVisibility(View.GONE);
            // fill text fields with password data to be edited
            passwordID = Integer.parseInt(id);
            nameInput.getEditText().setText(label);
            passwordInput.getEditText().setText(password);
            websiteInput.getEditText().setText(website);
        }
        //Creating the ArrayAdapter instance for "select save/generate mode" spinner
        ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, create_mode);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);

        PasswordDb db = PasswordDb.getDatabase(this);
        passwordDao = db.passwordDao();
        p = new Password();
        generatePassword = false;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void savePassword(View view) {
        p.label = nameInput.getEditText().getText().toString();
        p.website = websiteInput.getEditText().getText().toString();
        p.wasGeneratedRandomly = generatePassword;
        p.hasLetters = lettersCheckbox.isChecked();
        p.hasNumbers = numbersCheckbox.isChecked();
        p.hasUppercase = uppercaseCheckbox.isChecked();
        p.hasSpecialCharacters = specialCharsCheckbox.isChecked();

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US);
        df.setTimeZone(tz);
        p.lastUpdate = df.format(Calendar.getInstance().getTime());
        p.wasPwned = false;
        Log.d("CreatePasswordActivity", "Password UTC ISO 8601 date: " + p.lastUpdate);

        // edit password mode
        if (editMode){
            // search which password to update in all passwords in the db and find by id
            Password password_to_update = new Password();
            for(Password password:passwords){
                if(password.id == passwordID)
                    password_to_update = password;
            }
            password_to_update.label = nameInput.getEditText().getText().toString();
            password_to_update.website = websiteInput.getEditText().getText().toString();
            password_to_update.password = passwordInput.getEditText().getText().toString();
            mDisposable.add(passwordDao.update(password_to_update)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe());
            Toast.makeText(getApplicationContext(), "Password updated!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // generate password mode
        if (generatePassword) {
            if(!p.hasLetters && !p.hasNumbers && !p.hasUppercase && !p.hasSpecialCharacters){
                Toast.makeText(getApplicationContext(), "Please select one of the Include options!", Toast.LENGTH_LONG).show();
                return ;
            }
            if(!lengthInput.getEditText().getText().toString().matches(""))
                p.password = generatePassword(Integer.parseInt(lengthInput.getEditText().getText().toString()));
            else {
                Toast.makeText(getApplicationContext(), "Password can't be empty!", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            p.password = passwordInput.getEditText().getText().toString();
        }
        // save password mode (default)
        mDisposable.add(passwordDao.insert(p)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
        Toast.makeText(getApplicationContext(), "Password saved!", Toast.LENGTH_LONG).show();
        finish();
    }

    public String generatePassword(int length) {
        Random rnd = new Random();
        final String uppercase =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lowercase = uppercase.toLowerCase(Locale.ROOT);
        final String digits = "0123456789";
        final String specialChars = "$&@?<>~!%#";
        String characterOptions = "";
        if (lettersCheckbox.isChecked())
            characterOptions += lowercase;
        if (numbersCheckbox.isChecked())
            characterOptions += digits;
        if (uppercaseCheckbox.isChecked())
            characterOptions += uppercase;
        if (specialCharsCheckbox.isChecked())
            characterOptions += specialChars;
        StringBuilder generatedPassword = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            generatedPassword.append(characterOptions.charAt(rnd.nextInt(characterOptions.length())));
        return generatedPassword.toString();
    }

    // get all passwords from db so that we can find the password to update by its id
    private void loadPasswords() {
        mDisposable.add(passwordDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onbData,
                        throwable -> Log.e("MainActivity", "Failed to read data")));
    }

    private void onbData(List<Password> newPasswords) {
        this.passwords.clear();
        this.passwords.addAll(newPasswords);
    }
}
