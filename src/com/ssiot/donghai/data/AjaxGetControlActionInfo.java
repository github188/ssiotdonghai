package com.ssiot.donghai.data;

import android.text.TextUtils;
import android.util.Log;

import com.ssiot.donghai.data.business.ControlActionInfo;
import com.ssiot.donghai.data.business.ControlLog;
import com.ssiot.donghai.data.business.Sensor;
import com.ssiot.donghai.data.model.ControlActionInfoModel;
import com.ssiot.donghai.data.model.ControlLogModel;
import com.ssiot.donghai.data.model.SensorModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AjaxGetControlActionInfo{
    private Sensor sensorBll = new Sensor();
    private ControlActionInfo controlActionInfo_Bll = new ControlActionInfo();
    private ControlLog controlLogBll = new ControlLog();
    
    //jingbo 一个控制节点有多个控制设备，一个控制设备有多个控制规则，一个规则就是ControlActionInfo的一行
    public List<ControlActionInfoModel> GetControlActionInfo(String controlNode,String controlDeviceNo){
        try {
            if (TextUtils.isEmpty(controlNode) || TextUtils.isEmpty(controlDeviceNo)){
                throw new Exception();
            }
            List<ControlActionInfoModel> controlActionInfo_List = 
                    controlActionInfo_Bll.GetModelList("UniqueID='"+controlNode+"' and DeviceNo="+controlDeviceNo);
            
            HashMap<String, String> sensor_dic = new HashMap<String, String>();
            List<SensorModel> sensorList = sensorBll.GetModelList("1=1");
            for(SensorModel sensor : sensorList){
                sensor_dic.put(""+sensor._sensorno, sensor._shortname);
            }
            //sensor_dic可做cache
            List<ControlActionInfoModel> controlActionInfo_List2 = new ArrayList<ControlActionInfoModel>();
            if(controlActionInfo_List != null && controlActionInfo_List.size()>0){
                for (ControlActionInfoModel controlActionInfo : controlActionInfo_List){
                    if(controlActionInfo._controltype!=6){
                        //不是触发
                        controlActionInfo_List2.add(controlActionInfo);
                    } else {//触发类的规则  769-0 改为 湿度-0
                        String condition = controlActionInfo._controlcondition;
                        //
                        condition = jingboRegex(condition, sensor_dic);
                        controlActionInfo._controlcondition = condition;
                        controlActionInfo_List2.add(controlActionInfo);
                    }
                }
            }
            return controlActionInfo_List2;
            //TODO
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private String jingboRegex(String condition,HashMap<String, String> sensor_dic){
        String str = condition;
        String reg = "\\d+-\\d";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(reg);
        java.util.regex.Matcher matcher=pattern.matcher(str);
        Log.v("AjaxGetddddddddddd", "--------------count:"+matcher.groupCount());
        while(matcher.find()){
            String sensorNo = matcher.group().split("-")[0];
            Log.v("d", "-------------------sensorNo:" +sensorNo);
            String sensorName = sensor_dic.get(sensorNo);
            str = str.replace(str, sensorName + "-" + matcher.group().split("-")[1]);
        }
        return str;
    }
    
    //.根据控制节点标识(nodeUnique)、设备编号(deviceNo)和控制类型(controlType)获取设备当前设备状态
    public int GetDeviceState(String ControlType,String deviceNo,String nodeNo){
        try {
            String nodeUnique = nodeNo;//
            int deviceNo_int = Integer.parseInt(deviceNo);//
            int controlType_int = Integer.parseInt(ControlType);//
            ControlLogModel controlLog = controlLogBll.GetLatestData(nodeUnique, deviceNo_int,
                    controlType_int);
            return controlLog._sendstate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 3;
    }
}