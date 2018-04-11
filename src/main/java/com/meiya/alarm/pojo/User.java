package com.meiya.alarm.pojo;

import com.google.gson.Gson;

/**
 * Created by Administrator on 2017/12/22.
 */
public class User {


    private Integer userid;
    private String ftphost;
    private Integer ftpport;
    private String ftpdir;
    private String ftpusername;
    private String ftppassword;

    public Integer getFtpport() {
        return ftpport;
    }

    public void setFtpport(Integer ftpport) {
        this.ftpport = ftpport;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getFtphost() {
        return ftphost;
    }

    public void setFtphost(String ftphost) {
        this.ftphost = ftphost;
    }

    public String getFtpdir() {
        return ftpdir;
    }

    public String getFtpusername() {
        return ftpusername;
    }

    public void setFtpusername(String ftpusername) {
        this.ftpusername = ftpusername;
    }

    public String getFtppassword() {
        return ftppassword;
    }

    public void setFtppassword(String ftppassword) {
        this.ftppassword = ftppassword;
    }

    public void setFtpdir(String ftpdir) {
        this.ftpdir = ftpdir;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
