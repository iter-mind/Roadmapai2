package com.skilio.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.skilio.app.R;
import com.skilio.app.adapters.RoadmapListAdapter;
import com.skilio.app.models.Roadmap;
import com.skilio.app.network.GeminiService;
import com.skilio.app.storage.LocalStorage;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RoadmapListAdapter.OnRoadmapClickListener {

    private TextInputEditText etTopic;
    private MaterialButton btnGenerate;
    private ProgressBar progressBar;
    private RecyclerView rvRoadmaps;
    private TextView tvEmpty;
    private LinearLayout layoutGenerate;

    private GeminiService geminiService;
    private LocalStorage localStorage;
    private RoadmapListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Skilio");

        geminiService = new GeminiService();
        localStorage = new LocalStorage(this);

        etTopic = findViewById(R.id.et_topic);
        btnGenerate = findViewById(R.id.btn_generate);
        progressBar = findViewById(R.id.progress_bar);
        rvRoadmaps = findViewById(R.id.rv_roadmaps);
        tvEmpty = findViewById(R.id.tv_empty);
        layoutGenerate = findViewById(R.id.layout_generate);

        rvRoadmaps.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoadmapListAdapter(this);
        rvRoadmaps.setAdapter(adapter);

        btnGenerate.setOnClickListener(v -> handleGenerate());

        // Quick topic chips
        String[] quickTopics = {"Machine Learning", "JavaScript", "Python", "UX Design", "Finance", "Android Dev"};
        LinearLayout chipContainer = findViewById(R.id.chip_container);
        for (String topic : quickTopics) {
            TextView chip = new TextView(this);
            chip.setText(topic);
            chip.setPadding(32, 16, 32, 16);
            chip.setBackgroundResource(R.drawable.chip_background);
            chip.setTextColor(getResources().getColor(R.color.colorAccent));
            chip.setTextSize(13f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(params);
            chip.setOnClickListener(v -> etTopic.setText(topic));
            chipContainer.addView(chip);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRoadmaps();
    }

    private void loadRoadmaps() {
        List<Roadmap> roadmaps = localStorage.getAllRoadmaps();
        adapter.setRoadmaps(roadmaps);
        tvEmpty.setVisibility(roadmaps.isEmpty() ? View.VISIBLE : View.GONE);
        rvRoadmaps.setVisibility(roadmaps.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void handleGenerate() {
        String topic = etTopic.getText().toString().trim();
        if (TextUtils.isEmpty(topic)) {
            etTopic.setError("Please enter a topic");
            return;
        }
        if (!localStorage.hasApiKey()) {
            showApiKeyDialog();
            return;
        }
        generateRoadmap(topic);
    }

    private void generateRoadmap(String topic) {
        setLoading(true);
        String apiKey = localStorage.getApiKey();
        geminiService.generateRoadmap(topic, apiKey, new GeminiService.Callback<Roadmap>() {
            @Override
            public void onSuccess(Roadmap roadmap) {
                runOnUiThread(() -> {
                    setLoading(false);
                    localStorage.saveRoadmap(roadmap);
                    openRoadmap(roadmap.getId());
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void openRoadmap(String roadmapId) {
        Intent intent = new Intent(this, RoadmapActivity.class);
        intent.putExtra("roadmap_id", roadmapId);
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGenerate.setEnabled(!loading);
        btnGenerate.setText(loading ? "Generating..." : "Generate Roadmap");
    }

    private void showApiKeyDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_api_key, null);
        EditText etKey = dialogView.findViewById(R.id.et_api_key);
        String existing = localStorage.getApiKey();
        if (!TextUtils.isEmpty(existing)) etKey.setText(existing);

        new AlertDialog.Builder(this)
            .setTitle("Gemini API Key")
            .setMessage("Enter your Google Gemini API key to generate roadmaps.")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String key = etKey.getText().toString().trim();
                if (!TextUtils.isEmpty(key)) {
                    localStorage.saveApiKey(key);
                    Toast.makeText(this, "API key saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Key cannot be empty", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRoadmapClick(Roadmap roadmap) {
        openRoadmap(roadmap.getId());
    }

    @Override
    public void onRoadmapDelete(Roadmap roadmap) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Roadmap")
            .setMessage("Delete \"" + roadmap.getTitle() + "\"? This cannot be undone.")
            .setPositiveButton("Delete", (d, w) -> {
                localStorage.deleteRoadmap(roadmap.getId());
                loadRoadmaps();
                Toast.makeText(this, "Roadmap deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
