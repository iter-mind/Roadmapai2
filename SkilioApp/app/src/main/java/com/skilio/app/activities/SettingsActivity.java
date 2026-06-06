package com.skilio.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.skilio.app.R;
import com.skilio.app.storage.LocalStorage;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText etApiKey;
    private MaterialButton btnSave, btnTest;
    private TextView tvStatus;
    private LocalStorage localStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");

        localStorage = new LocalStorage(this);
        etApiKey = findViewById(R.id.et_api_key);
        btnSave = findViewById(R.id.btn_save);
        btnTest = findViewById(R.id.btn_test);
        tvStatus = findViewById(R.id.tv_status);

        String existing = localStorage.getApiKey();
        if (!TextUtils.isEmpty(existing)) {
            etApiKey.setText(existing);
            tvStatus.setText("API key is saved");
        }

        btnSave.setOnClickListener(v -> {
            String key = etApiKey.getText().toString().trim();
            if (TextUtils.isEmpty(key)) {
                etApiKey.setError("Key cannot be empty");
                return;
            }
            localStorage.saveApiKey(key);
            tvStatus.setText("API key saved successfully!");
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        });

        btnTest.setOnClickListener(v -> {
            String key = etApiKey.getText().toString().trim();
            if (TextUtils.isEmpty(key)) {
                etApiKey.setError("Enter a key first");
                return;
            }
            tvStatus.setText("Testing...");
            btnTest.setEnabled(false);
            testKey(key);
        });
    }

    private void testKey(String key) {
        new android.os.Handler().postDelayed(() -> {
            // Simple connectivity test
            new Thread(() -> {
                try {
                    okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                    org.json.JSONObject body = new org.json.JSONObject();
                    org.json.JSONArray contents = new org.json.JSONArray();
                    org.json.JSONObject content = new org.json.JSONObject();
                    org.json.JSONArray parts = new org.json.JSONArray();
                    org.json.JSONObject part = new org.json.JSONObject();
                    part.put("text", "Say ok");
                    parts.put(part);
                    content.put("role", "user");
                    content.put("parts", parts);
                    contents.put(content);
                    body.put("contents", contents);

                    okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                        body.toString(), okhttp3.MediaType.parse("application/json"));
                    okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + key)
                        .post(requestBody)
                        .build();
                    okhttp3.Response response = client.newCall(request).execute();
                    boolean ok = response.isSuccessful();
                    runOnUiThread(() -> {
                        btnTest.setEnabled(true);
                        if (ok) {
                            tvStatus.setText("✓ API key is valid!");
                            Toast.makeText(this, "Key works!", Toast.LENGTH_SHORT).show();
                        } else {
                            tvStatus.setText("✗ Invalid key. Check and retry.");
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        btnTest.setEnabled(true);
                        tvStatus.setText("Error: " + e.getMessage());
                    });
                }
            }).start();
        }, 100);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
