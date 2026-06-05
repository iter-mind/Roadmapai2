package com.skilio.app.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class RoadmapStep {
    private String id;
    private int stepNumber;
    private String title;
    private String description;
    private List<Resource> resources;
    private String lessonContent;
    private boolean completed;
    private boolean lessonGenerated;

    public RoadmapStep() {
        this.resources = new ArrayList<>();
        this.lessonContent = "";
        this.completed = false;
        this.lessonGenerated = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getStepNumber() { return stepNumber; }
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Resource> getResources() { return resources; }
    public void setResources(List<Resource> resources) { this.resources = resources; }

    public String getLessonContent() { return lessonContent; }
    public void setLessonContent(String lessonContent) { this.lessonContent = lessonContent; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isLessonGenerated() { return lessonGenerated; }
    public void setLessonGenerated(boolean lessonGenerated) { this.lessonGenerated = lessonGenerated; }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("stepNumber", stepNumber);
        obj.put("title", title);
        obj.put("description", description);
        obj.put("lessonContent", lessonContent != null ? lessonContent : "");
        obj.put("completed", completed);
        obj.put("lessonGenerated", lessonGenerated);
        JSONArray resArr = new JSONArray();
        if (resources != null) {
            for (Resource res : resources) {
                resArr.put(res.toJson());
            }
        }
        obj.put("resources", resArr);
        return obj;
    }

    public static RoadmapStep fromJson(JSONObject obj) throws JSONException {
        RoadmapStep step = new RoadmapStep();
        step.setId(obj.optString("id", ""));
        step.setStepNumber(obj.optInt("stepNumber", 0));
        step.setTitle(obj.optString("title", ""));
        step.setDescription(obj.optString("description", ""));
        step.setLessonContent(obj.optString("lessonContent", ""));
        step.setCompleted(obj.optBoolean("completed", false));
        step.setLessonGenerated(obj.optBoolean("lessonGenerated", false));
        JSONArray resArr = obj.optJSONArray("resources");
        List<Resource> resources = new ArrayList<>();
        if (resArr != null) {
            for (int i = 0; i < resArr.length(); i++) {
                resources.add(Resource.fromJson(resArr.getJSONObject(i)));
            }
        }
        step.setResources(resources);
        return step;
    }
}
