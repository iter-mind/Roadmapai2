package com.skilio.app.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.skilio.app.models.Roadmap;
import com.skilio.app.models.RoadmapStep;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class LocalStorage {

    private static final String PREFS_NAME = "skilio_prefs";
    private static final String KEY_ROADMAP_IDS = "roadmap_ids";
    private static final String KEY_GEMINI_API = "gemini_api_key";

    private SharedPreferences prefs;

    public LocalStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ─── API Key ──────────────────────────────────────────────────────────────

    public void saveApiKey(String key) {
        prefs.edit().putString(KEY_GEMINI_API, key).apply();
    }

    public String getApiKey() {
        return prefs.getString(KEY_GEMINI_API, "");
    }

    public boolean hasApiKey() {
        String key = getApiKey();
        return key != null && !key.trim().isEmpty();
    }

    // ─── Roadmaps ─────────────────────────────────────────────────────────────

    public void saveRoadmap(Roadmap roadmap) {
        try {
            // Save the roadmap JSON
            prefs.edit().putString("roadmap_" + roadmap.getId(), roadmap.toJson().toString()).apply();

            // Add ID to index if not already there
            List<String> ids = getRoadmapIds();
            if (!ids.contains(roadmap.getId())) {
                ids.add(0, roadmap.getId()); // newest first
                saveRoadmapIds(ids);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Roadmap getRoadmap(String id) {
        String json = prefs.getString("roadmap_" + id, null);
        if (json == null) return null;
        try {
            return Roadmap.fromJson(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Roadmap> getAllRoadmaps() {
        List<Roadmap> roadmaps = new ArrayList<>();
        for (String id : getRoadmapIds()) {
            Roadmap r = getRoadmap(id);
            if (r != null) roadmaps.add(r);
        }
        return roadmaps;
    }

    public void deleteRoadmap(String id) {
        prefs.edit().remove("roadmap_" + id).apply();
        List<String> ids = getRoadmapIds();
        ids.remove(id);
        saveRoadmapIds(ids);
    }

    // Update a specific step inside a saved roadmap (e.g. after lesson generated)
    public void updateStep(String roadmapId, RoadmapStep updatedStep) {
        Roadmap roadmap = getRoadmap(roadmapId);
        if (roadmap == null) return;
        List<RoadmapStep> steps = roadmap.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getId().equals(updatedStep.getId())) {
                steps.set(i, updatedStep);
                break;
            }
        }
        roadmap.setSteps(steps);
        saveRoadmap(roadmap);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private List<String> getRoadmapIds() {
        String json = prefs.getString(KEY_ROADMAP_IDS, "[]");
        List<String> ids = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                ids.add(arr.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ids;
    }

    private void saveRoadmapIds(List<String> ids) {
        JSONArray arr = new JSONArray();
        for (String id : ids) arr.put(id);
        prefs.edit().putString(KEY_ROADMAP_IDS, arr.toString()).apply();
    }
}
