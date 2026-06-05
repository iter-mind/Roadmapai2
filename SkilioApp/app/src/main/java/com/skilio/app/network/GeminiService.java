package com.skilio.app.network;

import com.skilio.app.models.Roadmap;
import com.skilio.app.models.RoadmapStep;
import com.skilio.app.models.Resource;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GeminiService {

    private static final String MODEL = "gemini-2.0-flash";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final String ROADMAP_SYSTEM_PROMPT =
        "You are an expert learning curriculum designer. Given a topic, create a step-by-step learning roadmap.\n\n" +
        "Return a JSON object with this EXACT structure (no markdown, no extra text):\n" +
        "{\n" +
        "  \"title\": \"Title of the learning path\",\n" +
        "  \"subtitle\": \"Brief description\",\n" +
        "  \"steps\": [\n" +
        "    {\n" +
        "      \"stepNumber\": 1,\n" +
        "      \"title\": \"Step title\",\n" +
        "      \"description\": \"2-3 sentence description of what to learn\",\n" +
        "      \"resources\": [\n" +
        "        {\"title\": \"Resource name\", \"url\": \"https://...\", \"type\": \"video|website|pdf|book|tool\"}\n" +
        "      ]\n" +
        "    }\n" +
        "  ]\n" +
        "}\n\n" +
        "Guidelines:\n" +
        "- Create 7-12 steps from beginner to advanced\n" +
        "- Each step should have 2-4 real working resource links\n" +
        "- For YouTube videos, use: https://www.youtube.com/results?search_query=KEYWORDS\n" +
        "IMPORTANT: Return ONLY raw JSON. No markdown fences. No extra text.";

    private static final String LESSON_SYSTEM_PROMPT =
        "You are an expert educator. Generate detailed lesson content in markdown format for the given step.\n\n" +
        "Include:\n" +
        "- Clear explanations\n" +
        "- Code examples where relevant\n" +
        "- Practice exercises\n" +
        "- Key takeaways\n\n" +
        "Return ONLY this JSON (no markdown fences, no extra text):\n" +
        "{\"content\": \"...markdown content here...\"}";

    private OkHttpClient client;

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public GeminiService() {
        client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    }

    public void generateRoadmap(String topic, String apiKey, Callback<Roadmap> callback) {
        String prompt = ROADMAP_SYSTEM_PROMPT + "\n\nTopic: " + topic;
        callGemini(apiKey, prompt, new Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    Roadmap roadmap = parseRoadmap(result, topic);
                    callback.onSuccess(roadmap);
                } catch (Exception e) {
                    callback.onError("Failed to parse roadmap: " + e.getMessage());
                }
            }
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void generateLesson(RoadmapStep step, String roadmapTitle, String apiKey, Callback<String> callback) {
        String prompt = LESSON_SYSTEM_PROMPT +
            "\n\nRoadmap: \"" + roadmapTitle + "\"" +
            "\nStep " + step.getStepNumber() + ": \"" + step.getTitle() + "\"" +
            "\nDescription: " + step.getDescription();
        callGemini(apiKey, prompt, new Callback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    String cleaned = cleanJson(result);
                    JSONObject obj = new JSONObject(cleaned);
                    callback.onSuccess(obj.optString("content", result));
                } catch (Exception e) {
                    // If JSON parsing fails, return raw content
                    callback.onSuccess(result);
                }
            }
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private void callGemini(String apiKey, String prompt, Callback<String> callback) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject content = new JSONObject();
                JSONArray parts = new JSONArray();
                JSONObject part = new JSONObject();
                part.put("text", prompt);
                parts.put(part);
                content.put("role", "user");
                content.put("parts", parts);
                contents.put(content);
                body.put("contents", contents);

                JSONObject genConfig = new JSONObject();
                genConfig.put("temperature", 0.7);
                genConfig.put("maxOutputTokens", 8192);
                body.put("generationConfig", genConfig);

                String url = BASE_URL + MODEL + ":generateContent?key=" + apiKey;
                RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json")
                );
                Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    callback.onError("Gemini API error " + response.code() + ": " + responseBody);
                    return;
                }

                JSONObject responseJson = new JSONObject(responseBody);
                String text = responseJson
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

                callback.onSuccess(text);
            } catch (Exception e) {
                callback.onError("Network error: " + e.getMessage());
            }
        }).start();
    }

    private String cleanJson(String raw) {
        String text = raw.trim();
        // Remove markdown fences
        if (text.startsWith("```json")) text = text.substring(7);
        else if (text.startsWith("```")) text = text.substring(3);
        if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
        return text.trim();
    }

    private Roadmap parseRoadmap(String raw, String topic) throws JSONException {
        String cleaned = cleanJson(raw);

        // Try to extract JSON object
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }

        JSONObject parsed = new JSONObject(cleaned);
        Roadmap roadmap = new Roadmap();
        roadmap.setId(UUID.randomUUID().toString());
        roadmap.setTitle(parsed.optString("title", topic));
        roadmap.setSubtitle(parsed.optString("subtitle", "A learning roadmap for " + topic));
        roadmap.setCreatedAt(new java.util.Date().toString());

        JSONArray stepsArr = parsed.optJSONArray("steps");
        List<RoadmapStep> steps = new ArrayList<>();
        if (stepsArr != null) {
            for (int i = 0; i < stepsArr.length(); i++) {
                JSONObject stepObj = stepsArr.getJSONObject(i);
                RoadmapStep step = new RoadmapStep();
                step.setId(UUID.randomUUID().toString());
                step.setStepNumber(stepObj.optInt("stepNumber", i + 1));
                step.setTitle(stepObj.optString("title", "Step " + (i + 1)));
                step.setDescription(stepObj.optString("description", ""));
                step.setLessonContent("");
                step.setCompleted(false);
                step.setLessonGenerated(false);

                JSONArray resArr = stepObj.optJSONArray("resources");
                List<Resource> resources = new ArrayList<>();
                if (resArr != null) {
                    for (int j = 0; j < resArr.length(); j++) {
                        JSONObject resObj = resArr.getJSONObject(j);
                        Resource res = new Resource(
                            resObj.optString("title", ""),
                            resObj.optString("url", ""),
                            resObj.optString("type", "website")
                        );
                        resources.add(res);
                    }
                }
                step.setResources(resources);
                steps.add(step);
            }
        }
        roadmap.setSteps(steps);
        return roadmap;
    }
}
