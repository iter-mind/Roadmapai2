package com.skilio.app.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Roadmap {
    private String id;
    private String title;
    private String subtitle;
    private String createdAt;
    private List<RoadmapStep> steps;

    public Roadmap() {
        this.steps = new ArrayList<>();
    }

    public Roadmap(String id, String title, String subtitle, String createdAt, List<RoadmapStep> steps) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.createdAt = createdAt;
        this.steps = steps != null ? steps : new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<RoadmapStep> getSteps() { return steps; }
    public void setSteps(List<RoadmapStep> steps) { this.steps = steps; }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("subtitle", subtitle);
        obj.put("createdAt", createdAt);
        JSONArray stepsArr = new JSONArray();
        for (RoadmapStep step : steps) {
            stepsArr.put(step.toJson());
        }
        obj.put("steps", stepsArr);
        return obj;
    }

    public static Roadmap fromJson(JSONObject obj) throws JSONException {
        Roadmap r = new Roadmap();
        r.setId(obj.optString("id", ""));
        r.setTitle(obj.optString("title", ""));
        r.setSubtitle(obj.optString("subtitle", ""));
        r.setCreatedAt(obj.optString("createdAt", ""));
        JSONArray stepsArr = obj.optJSONArray("steps");
        List<RoadmapStep> steps = new ArrayList<>();
        if (stepsArr != null) {
            for (int i = 0; i < stepsArr.length(); i++) {
                steps.add(RoadmapStep.fromJson(stepsArr.getJSONObject(i)));
            }
        }
        r.setSteps(steps);
        return r;
    }
}
