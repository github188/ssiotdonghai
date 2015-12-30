package com.ssiot.donghai.hikvision;

public class VideoInfoHolder{
    public String ip = "222.169.186.222";
    public int port = 37781;
    public String user = "admin";
    public String pwd = "754915497x";
    
    public VideoInfoHolder(String ip, int port,String user, String pwd){
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }
}