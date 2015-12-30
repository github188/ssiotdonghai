package com.ssiot.donghai.data.model;

import java.sql.Timestamp;

public class ControlLogModel{
    public int _id;
    public int _logtype = 0;
    public String _uniqueid;
    public int _deviceno = 0;
    public int _starttype = 0;
    public int _startvalue = 0;
    public int _runtime = 0;
    public int _endtype = 0;
    public int _endvalue = 0;
    public int _sendstate = 0;//0-创建 1-发送 2-成功 3-失败
    public Timestamp _createtime;// = DateTime.Now;
    public Timestamp _edittime;// = DateTime.Now;
    public int _timespan;
    public int _resendcount = 0;
}