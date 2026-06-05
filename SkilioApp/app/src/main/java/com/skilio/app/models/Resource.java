package com.skilio.app.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Resource {
    private String title;
    private String url;
    private String type;

    public Resource() {}

    public Resource(String title, String url, String type) {
        this.title = title;
        this.url = url;
        this.type = type;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("title", title);
        obj.put("url", url != null ? url : "");
        obj.put("type", type != null ? type : "website");
        return obj;
    }

    public static Resource fromJson(JSONObject obj) throws JSONException {
        Resource r = new Resource();
        r.setTitle(obj.optString("title", ""));
        r.setUrl(obj.optString("url", ""));
        r.setType(obj.optString("type", "website"));
        return r;
    }
}
