package com.ssiot.donghai.data.model.view;

import java.sql.Timestamp;

public class ControlDeviceView2Model{
        public int DeviceID;
        public int ControlNodeID;
        public int DeviceNo;
        public String DeviceName;
        
        public String DeviceStateNow;
        
        public int ControlActionID;
        public int ControlType;
        public String CollectUniqueIDs;
        public String ControlCondition;
        public Timestamp OperateTime;
        public int StateNow;//?
        public String Operate;
    }