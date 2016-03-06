package com.android.imageshooter.app.Utils;


public class ShotInfos {
    String URL;
    String title;
    String description;

    public ShotInfos(String description, String title, String URL) {
        this.description = description;
        this.title = title;
        this.URL = URL;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getURL() {
        return URL;
    }

}
