package com.example.mypasswordmanager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PwnCheckWorker extends Worker {
    private final Context context;
    public PwnCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        String HIBP_API = "https://haveibeenpwned.com/api/v3/breaches";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(HIBP_API)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String bodyString  = Objects.requireNonNull(response.body()).string();
            JSONArray body = new JSONArray(bodyString);
            String[] breachedDomains = new String[body.length()];
            String[] breachedDates = new String[body.length()];
            for (int i = 0; i < body.length(); i++) {
                breachedDomains[i] = body.getJSONObject(i).getString("Domain");
                breachedDates[i] = body.getJSONObject(i).getString("AddedDate");
            }

            // Check if there is a password that was breached more recently than its last edit
            PasswordDb db = PasswordDb.getDatabase(context);
            PasswordDao passwordDao = db.passwordDao();
            List<Password> passwords = passwordDao.getAll().blockingFirst();
            for (Password password : passwords) {
                for (int i = 0; i < breachedDomains.length; i++) {
                    String breachedDomain = breachedDomains[i];
                    // website URL to domain conversion
                    String passwordHost = new URI(password.website).getHost();
                    String passwordDomain = passwordHost.startsWith("www.") ? passwordHost.substring(4) : passwordHost;
                    if (breachedDomain.equals(passwordDomain)) {
                        String date = breachedDates[i];
                        // ISO 8601 timestamps can be compared lexicographically
                        if (password.lastUpdate.compareTo(date) < 0) {
                            password.wasPwned = true;
                            passwordDao.update(password).blockingAwait();
                        }
                    }
                }
            }

            return Result.success();
        } catch (IOException | JSONException | URISyntaxException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
