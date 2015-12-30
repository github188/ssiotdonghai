package com.ssiot.donghai.data.model.view;

import java.sql.Timestamp;

public class ControlDeviceViewModel{
    public int DeviceID;
    public int ControlNodeID;
    public int DeviceNo;
    public String DeviceName;
    public int RunTime = -1;//?
    public String StartTime;//smalldatetime
    public int ControlActionID;//?
    public int ControlType;//?
    public String CollectUniqueIDs;
    public String ControlCondition;
    public Timestamp OperateTime;
    public int StateNow;//?
    public String Operate;
    
    
}