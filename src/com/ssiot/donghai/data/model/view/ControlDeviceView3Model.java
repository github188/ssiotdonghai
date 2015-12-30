package com.ssiot.donghai.data.model.view;

import java.util.List;

public class ControlDeviceView3Model{
    public int DeviceID;
    public int ControlNodeID;
    public int DeviceNo;
    public String DeviceName;
    public String DeviceStateNow;
    public List<ControlActionViewModel> ActionList;
}