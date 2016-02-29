package com.android.imageshooter.app;


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

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
