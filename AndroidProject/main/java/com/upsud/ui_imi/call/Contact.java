package com.upsud.ui_imi.call;

import android.graphics.Bitmap;

/**
 * Created by Maxence Bobin on 27/02/15.
 */
public class Contact {

    private String name;
    private String phoneNumber;
    private Bitmap photo;

    public Contact () {
        this.name = "";
        this.photo = null;
        this.phoneNumber = "";
    }

    public Contact (String name, String phoneNumber, Bitmap photo) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }
}
