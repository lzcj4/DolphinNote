package com.tannuo.note.server;

/**
 * Created by Nick_PC on 2016/7/22.
 */
public class User {

    /**
     * _id : 5733f3acc0e563211498a66b
     * name : Nick
     * company :
     * jobTitle :
     * weixinOpenId : o6TxoxMvxixMDuJ7EY-ZhqQ7YG-k
     * avatarUrl : http://wx.qlogo.cn/mmopen/asiauCibkFsK5LpEqbs2jMZc4Nr6GickibuYDlfFHs2LcMgib3ibKSnsWOWWWkvFWqCYJ9WbCSHt3PibdlRoRicCvlyYFUKvNwT4iaib8T/0
     * gender : 男
     * province : Zhejiang
     * city : Hangzhou
     * country : CN
     */

    private String _id;
    private String name;
    private String company;
    private String jobTitle;
    private String weixinOpenId;
    private String avatarUrl;
    private String gender;
    private String province;
    private String city;
    private String country;

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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getWeixinOpenId() {
        return weixinOpenId;
    }

    public void setWeixinOpenId(String weixinOpenId) {
        this.weixinOpenId = weixinOpenId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
