package com.xyt.sipphone.model;

/**
 * Created by apple on 16/9/6.
 */
public class SortModel {
    private String name;   //显示的数据
    private String sortLetters;  //显示数据拼音的首字母
    private String domain;
    private String number;

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    private String keyId;  //用于删除


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSortLetters() {
        return sortLetters;
    }
    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
