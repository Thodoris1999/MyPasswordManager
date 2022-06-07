package com.example.mypasswordmanager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
    private final String HIBP_API = "https://haveibeenpwned.com/api/v3/breaches";

    public PwnCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(HIBP_API)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String bodyString  = Objects.requireNonNull(response.body()).string();
            JSONArray body = new JSONArray(bodyString);
            String[] breachedDomains = new String[body.length()];
            String[] breachedDates = new String[body.length()];
            String[] descriptions = new String[body.length()];
            for (int i = 0; i < body.length(); i++) {
                breachedDomains[i] = body.getJSONObject(i).getString("Domain");
                breachedDates[i] = body.getJSONObject(i).getString("AddedDate");
                descriptions[i] = body.getJSONObject(i).getString("Description");
            }

            // Check if there is a password that was breached more recently than its last edit
            PasswordDb db = PasswordDb.getDatabase(context);
            PasswordDao passwordDao = db.passwordDao();
            List<Password> passwords = passwordDao.getAll().blockingFirst();
            for (Password password : passwords) {
                for (int i = 0; i < breachedDomains.length; i++) {
                    String breachedDomain = breachedDomains[i];
                    // website URL to domain conversion
                    String passwordDomain;
                    try {
                        String prefix = password.website.startsWith("https") ? "" : "https://";
                        String passwordHost = new URI(prefix + password.website).getHost();
                        passwordDomain = passwordHost.startsWith("www.") ? passwordHost.substring(4) : passwordHost;
                    } catch (URISyntaxException | NullPointerException e) {
                        Log.d("PwnWorker", e.getMessage());
                        continue;
                    }
                    if (breachedDomain.equals(passwordDomain)) {
                        String date = breachedDates[i];
                        // ISO 8601 timestamps can be compared lexicographically
                        if (password.lastUpdate.compareTo(date) < 0) {
                            // breach detected!
                            password.wasPwned = true;
                            passwordDao.update(password).blockingAwait();

                            // Create an explicit intent for an Activity in your app
                            Intent intent = new Intent(context, AuthorizationActivity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                            // define notification
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.BREACH_CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_baseline_error_red_24)
                                    .setContentTitle(context.getString(R.string.breach_notification_title))
                                    .setContentText(context.getString(R.string.breach_notification_short_text))
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText(descriptions[i]))
                                    .setContentIntent(pendingIntent)
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setAutoCancel(true);

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                            notificationManager.notify(i, builder.build());
                        }
                    }
                }
            }

            return Result.success();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
