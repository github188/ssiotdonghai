package com.ssiot.donghai.data.model;

import java.sql.Timestamp;

public class ControlActionInfoModel{
    public int _id;
    public int _areaid;
    public String _controlname;
    public String _uniqueid;
    public int _deviceno;
    public int _controltype;
    public String _collectuniqueids;
    public String _controlcondition;
    public Timestamp _operatetime;//DateTime
    public int _statenow;
    public String _operate;
    public String _remark;
}