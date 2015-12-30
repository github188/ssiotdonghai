package com.ssiot.donghai.data.model;

import java.sql.Timestamp;

public class ControlDeviceModel{
    public int _id;
    public int _controlnodeid;
    public int _deviceno;
    public String _devicename;
    public String _devicetype;
    public String _address;
    public String _remark;
    public String _extern;
    public Timestamp _createtime;
    public String _state;
}