package com.ssiot.donghai.monitor;

import java.util.ArrayList;

public class MonitorNode{
    public String name;
    public boolean online = false;
//    public int data_umidity = 0;//湿度
//    public int data_tempreture = 0;//温度
//    public int data_soil_water_1 = 0;//土壤水
//    public int data_soil_water_2 = 0;//
//    public int data_soil_water_3 = 0;
//    public int data_co2 = 0;//二氧化碳 ppm
//    public int data_light = 0;//光强
    public ArrayList<SensorData> datas = new ArrayList<MonitorNode.SensorData>();
    public String data_lasttime = "";
    public String data_videoString = "";
    //点击button再去查询详细信息。
    
    public MonitorNode(){
        
    }
    
    
    public class SensorData{
        public String sensorName;
        public String sensorData;
    }
    
}