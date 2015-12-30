package com.ssiot.donghai.data.model.view;

import java.sql.Timestamp;
import java.util.List;

public class ControlActionViewInfoModel<T>{
    public int _id;
    public int _areaid;
    public String _controlname;
    public String _uniqueid;
    public String _devicename;
    public int _controltype;
    public String _collectuniqueids;
    public List<T> _controlcondition;
    public String _othercontrolcondition;
    public Timestamp _operatetime;
    public int _statenow;
    public String _operate;
    public String _remark;
}