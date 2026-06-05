package com.skilio.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.skilio.app.R;
import com.skilio.app.adapters.StepListAdapter;
import com.skilio.app.models.Roadmap;
import com.skilio.app.models.RoadmapStep;
import com.skilio.app.storage.LocalStorage;

public class RoadmapActivity extends AppCompatActivity implements StepListAdapter.OnStepClickListener {

    private TextView tvTitle, tvSubtitle, tvMeta;
    private RecyclerView rvSteps;
    private StepListAdapter adapter;
    private LocalStorage localStorage;
    private Roadmap roadmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roadmap);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        localStorage = new LocalStorage(this);

        tvTitle = findViewById(R.id.tv_roadmap_title);
        tvSubtitle = findViewById(R.id.tv_roadmap_subtitle);
        tvMeta = findViewById(R.id.tv_roadmap_meta);
        rvSteps = findViewById(R.id.rv_steps);

        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StepListAdapter(this);
        rvSteps.setAdapter(adapter);

        String roadmapId = getIntent().getStringExtra("roadmap_id");
        loadRoadmap(roadmapId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload to reflect lesson generation changes
        String roadmapId = getIntent().getStringExtra("roadmap_id");
        if (roadmapId != null) loadRoadmap(roadmapId);
    }

    private void loadRoadmap(String id) {
        roadmap = localStorage.getRoadmap(id);
        if (roadmap == null) {
            finish();
            return;
        }
        getSupportActionBar().setTitle("Roadmap");
        tvTitle.setText(roadmap.getTitle());
        tvSubtitle.setText(roadmap.getSubtitle());

        int total = roadmap.getSteps().size();
        long done = roadmap.getSteps().stream().filter(RoadmapStep::isCompleted).count();
        tvMeta.setText(total + " steps  •  " + done + "/" + total + " completed");

        adapter.setSteps(roadmap.getSteps());
    }

    @Override
    public void onStepClick(RoadmapStep step) {
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra("roadmap_id", roadmap.getId());
        intent.putExtra("step_id", step.getId());
        startActivity(intent);
    }

    @Override
    public void onStepComplete(RoadmapStep step) {
        step.setCompleted(!step.isCompleted());
        localStorage.updateStep(roadmap.getId(), step);
        // Reload to reflect change
        loadRoadmap(roadmap.getId());
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
