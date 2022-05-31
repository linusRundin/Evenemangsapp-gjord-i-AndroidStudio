package com.example.projekt_event_app.firebase;

/**
 * class used to upload an image to firebase.
 */
public class Upload {

    private String name;
    private String mImageUrl;

    public Upload() {
    }

    public Upload(String name, String mImageUrl) {
        this.name = name;
        this.mImageUrl = mImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }


}
