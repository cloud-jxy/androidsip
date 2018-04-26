package com.xyt.sipphone.model;

/**
 * Created by apple on 16/9/5.
 */
public class ContactsItem {
    public ContactsItem(int image, String name, String number) {
        this.image = image;
        this.name = name;
        this.number = number;
    }

    int image;
    String name;
    String number;

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
