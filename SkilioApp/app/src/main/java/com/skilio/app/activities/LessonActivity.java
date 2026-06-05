package com.skilio.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.skilio.app.R;
import com.skilio.app.models.Roadmap;
import com.skilio.app.models.RoadmapStep;
import com.skilio.app.models.Resource;
import com.skilio.app.network.GeminiService;
import com.skilio.app.storage.LocalStorage;
import io.noties.markwon.Markwon;

public class LessonActivity extends AppCompatActivity {

    private TextView tvStepTitle, tvStepDescription, tvLessonContent;
    private MaterialButton btnGenerateLesson, btnMarkComplete;
    private ProgressBar progressBar;
    private LinearLayout layoutLesson, layoutResources;
    private ChipGroup chipGroupResources;

    private GeminiService geminiService;
    private LocalStorage localStorage;
    private Roadmap roadmap;
    private RoadmapStep step;
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        geminiService = new GeminiService();
        localStorage = new LocalStorage(this);
        markwon = Markwon.create(this);

        tvStepTitle = findViewById(R.id.tv_step_title);
        tvStepDescription = findViewById(R.id.tv_step_description);
        tvLessonContent = findViewById(R.id.tv_lesson_content);
        btnGenerateLesson = findViewById(R.id.btn_generate_lesson);
        btnMarkComplete = findViewById(R.id.btn_mark_complete);
        progressBar = findViewById(R.id.progress_bar);
        layoutLesson = findViewById(R.id.layout_lesson);
        chipGroupResources = findViewById(R.id.chip_group_resources);

        String roadmapId = getIntent().getStringExtra("roadmap_id");
        String stepId = getIntent().getStringExtra("step_id");
        loadStep(roadmapId, stepId);

        btnGenerateLesson.setOnClickListener(v -> generateLesson());
        btnMarkComplete.setOnClickListener(v -> toggleComplete());
    }

    private void loadStep(String roadmapId, String stepId) {
        roadmap = localStorage.getRoadmap(roadmapId);
        if (roadmap == null) { finish(); return; }

        for (RoadmapStep s : roadmap.getSteps()) {
            if (s.getId().equals(stepId)) {
                step = s;
                break;
            }
        }
        if (step == null) { finish(); return; }

        getSupportActionBar().setTitle("Step " + step.getStepNumber());
        tvStepTitle.setText(step.getTitle());
        tvStepDescription.setText(step.getDescription());

        // Resources chips
        chipGroupResources.removeAllViews();
        for (Resource res : step.getResources()) {
            Chip chip = new Chip(this);
            chip.setText(res.getTitle());
            chip.setChipIconResource(getResourceIcon(res.getType()));
            chip.setClickable(true);
            chip.setOnClickListener(v -> {
                if (res.getUrl() != null && !res.getUrl().isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(res.getUrl())));
                }
            });
            chipGroupResources.addView(chip);
        }

        // Show existing lesson if already generated
        if (step.isLessonGenerated() && step.getLessonContent() != null && !step.getLessonContent().isEmpty()) {
            showLesson(step.getLessonContent());
            btnGenerateLesson.setText("Regenerate Lesson");
        }

        updateCompleteButton();
    }

    private void generateLesson() {
        if (!localStorage.hasApiKey()) {
            Toast.makeText(this, "Please set your Gemini API key in Settings", Toast.LENGTH_LONG).show();
            return;
        }
        setLoading(true);
        geminiService.generateLesson(step, roadmap.getTitle(), localStorage.getApiKey(), new GeminiService.Callback<String>() {
            @Override
            public void onSuccess(String content) {
                runOnUiThread(() -> {
                    setLoading(false);
                    step.setLessonContent(content);
                    step.setLessonGenerated(true);
                    localStorage.updateStep(roadmap.getId(), step);
                    showLesson(content);
                    btnGenerateLesson.setText("Regenerate Lesson");
                    Toast.makeText(LessonActivity.this, "Lesson saved locally!", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(LessonActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLesson(String content) {
        layoutLesson.setVisibility(View.VISIBLE);
        markwon.setMarkdown(tvLessonContent, content);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGenerateLesson.setEnabled(!loading);
        btnGenerateLesson.setText(loading ? "Generating..." :
            (step.isLessonGenerated() ? "Regenerate Lesson" : "Generate Lesson"));
    }

    private void toggleComplete() {
        step.setCompleted(!step.isCompleted());
        localStorage.updateStep(roadmap.getId(), step);
        updateCompleteButton();
        Toast.makeText(this, step.isCompleted() ? "Marked as complete!" : "Marked as incomplete", Toast.LENGTH_SHORT).show();
    }

    private void updateCompleteButton() {
        if (step.isCompleted()) {
            btnMarkComplete.setText("✓ Completed");
            btnMarkComplete.setBackgroundColor(getResources().getColor(R.color.colorSuccess));
        } else {
            btnMarkComplete.setText("Mark as Complete");
            btnMarkComplete.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    }

    private int getResourceIcon(String type) {
        switch (type) {
            case "video": return R.drawable.ic_video;
            case "book": return R.drawable.ic_book;
            case "tool": return R.drawable.ic_tool;
            default: return R.drawable.ic_web;
        }
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
