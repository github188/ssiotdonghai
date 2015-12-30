package com.ssiot.donghai.data.model;

import java.sql.Timestamp;

public class SettingModel{
    public int _id;
    public String _uniqueid;
    public int _type;
    public int _settingmark;
    public int _chanel = 0;
    public int _other = 0;
    public float _value = 0;
    public int _timespan;
    public Timestamp _sendtime;
    public int _sendstate = 0;
    public int _resendcount = 0;
}