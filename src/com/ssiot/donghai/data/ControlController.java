package com.ssiot.donghai.data;

import android.text.TextUtils;
import android.util.Log;

import com.ssiot.donghai.MainActivity;
import com.ssiot.donghai.data.business.ControlActionInfo;
import com.ssiot.donghai.data.business.ControlLog;
import com.ssiot.donghai.data.model.ControlActionInfoModel;
import com.ssiot.donghai.data.model.ControlLogModel;

import java.sql.Timestamp;
import java.util.List;

public class ControlController{
    private static final String tag = "ControlController";
    ControlActionInfo controlActionInfoBll = new ControlActionInfo();
    ControlLog controllogbll = new ControlLog();
    
    public boolean SaveControlTimeUser(String timeCondition, String seldevice, String UniqueID, int controlType, String DeviceNos,String updateid){
        if (TextUtils.isEmpty(timeCondition)){
            Log.e(tag, "----SaveControlTimeUser!!!!----timeCondition = null");
            return false;
        }
        
        ControlActionInfoModel controlActionInfo  = new ControlActionInfoModel();
//        object id = Session["ControlID"];
        
        if (!TextUtils.isEmpty(updateid)){//更新
            controlActionInfo = controlActionInfoBll.GetModel(Integer.parseInt(updateid));
            controlActionInfo._areaid = GetNowAccountAreaID();
            controlActionInfo._controlname = getControlTypeString(controlType);
            controlActionInfo._uniqueid = UniqueID;
            controlActionInfo._deviceno = Integer.parseInt(DeviceNos);
            controlActionInfo._controltype = controlType;
            controlActionInfo._controlcondition = timeCondition;
            controlActionInfo._operatetime = new Timestamp(System.currentTimeMillis());
            controlActionInfo._statenow = 0;
            controlActionInfo._operate = "打开";
            return updateControlActionInfoAndAddLog(controlActionInfo);
        } else {//添加
            if (seldevice == null && DeviceNos != ""){
                if (DeviceNos.endsWith(",")){
                    DeviceNos = DeviceNos.substring(0, DeviceNos.length()-1);
                }
                String[] nos = DeviceNos.split(",");
                for (String devicetmp : nos){
                    controlActionInfo._areaid = GetNowAccountAreaID();
                    controlActionInfo._controlname = getControlTypeString(controlType);// (string)Session["controlActionName"];
                    controlActionInfo._uniqueid = UniqueID;
                    controlActionInfo._deviceno = Integer.parseInt(devicetmp);
                    controlActionInfo._controltype = controlType;
                    controlActionInfo._controlcondition = timeCondition;
                    controlActionInfo._operatetime = new Timestamp(System.currentTimeMillis());
                    controlActionInfo._statenow = 0; 
                    controlActionInfo._operate = "打开";
                    if(false == addControlActionInfoAndAddLog(controlActionInfo)){
                        return false;
                    }
                }
                return true;
            }
        }
        
        
        
        return false;
    }
    
    
    private boolean updateControlActionInfoAndAddLog(ControlActionInfoModel controlActionInfo){//ControlActionInfo表 ControlLog表
        if (controlActionInfoBll.Update(controlActionInfo)) {
            List<ControlLogModel> controlLog_list = DataAPI.ConvertControlActionInfoToControlLog(controlActionInfo);
            try {
                int rtnCount = controllogbll.AddManyCount(controlLog_list);
                if (rtnCount == 0) {
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
    
    private boolean addControlActionInfoAndAddLog(ControlActionInfoModel controlActionInfo){//ControlActionInfo表 ControlLog表
        if (controlActionInfoBll.Add(controlActionInfo) > 0) {
            List<ControlLogModel> controlLog_list = DataAPI.ConvertControlActionInfoToControlLog(controlActionInfo);
            try {
                int rtnCount = controllogbll.AddManyCount(controlLog_list);
                if (rtnCount == 0) {
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }
    
    private String getControlTypeString (int controlType){
        String names = "";
        if (controlType == 1) {
            names = "立即开启";
        } else if (controlType == 3) {
            names = "定时";
        } else if (controlType == 5) {
            names = "循环";
        }
        return names;
    }
    
    private int GetNowAccountAreaID(){
        if (MainActivity.AreaID < 0){
            Log.e(tag, "----!!!! MainActivity.AreaID < 0");
        }
        return MainActivity.AreaID;
    }
}