package com.tannuo.sdk.device.server;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Nick on 2016/7/6.
 */
public class Conference {
    /**
     * _id : 576892efd49c87e817f85d58
     * name : test
     * password : 123
     * datetime : 1466471100000
     * location :
     * company :
     * abstract :
     * techBridgeId : 114893792
     * images : []
     * nicknames : ["Nick"]
     * users : ["5733f3acc0e563211498a66b"]
     */
    private String _id;
    private String name;
    private String password;
    private long datetime;
    private String location;
    private String company;
    @SerializedName("abstract")
    private String abstractX;
    private String techBridgeId;
    private List<?> images;
    private List<String> nicknames;
    private List<String> users;

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAbstractX() {
        return abstractX;
    }

    public void setAbstractX(String abstractX) {
        this.abstractX = abstractX;
    }

    public String getTechBridgeId() {
        return techBridgeId;
    }

    public void setTechBridgeId(String techBridgeId) {
        this.techBridgeId = techBridgeId;
    }

    public List<?> getImages() {
        return images;
    }

    public void setImages(List<?> images) {
        this.images = images;
    }

    public List<String> getNicknames() {
        return nicknames;
    }

    public void setNicknames(List<String> nicknames) {
        this.nicknames = nicknames;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}


