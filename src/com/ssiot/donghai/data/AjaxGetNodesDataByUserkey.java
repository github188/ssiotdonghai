package com.ssiot.donghai.data;

import android.R.integer;
import android.text.TextUtils;
import android.util.Log;

import com.ssiot.donghai.Utils;
import com.ssiot.donghai.R.color;
import com.ssiot.donghai.data.business.ControlActionInfo;
import com.ssiot.donghai.data.business.ControlDevice;
import com.ssiot.donghai.data.business.ControlLog;
import com.ssiot.donghai.data.business.Node;
import com.ssiot.donghai.data.business.Sensor;
import com.ssiot.donghai.data.business.SensorModifyData;
import com.ssiot.donghai.data.business.SettingInfo1;
import com.ssiot.donghai.data.model.ControlActionInfoModel;
import com.ssiot.donghai.data.model.ControlDeviceModel;
import com.ssiot.donghai.data.model.ControlLogModel;
import com.ssiot.donghai.data.model.ControlNodeModel;
import com.ssiot.donghai.data.model.NodeModel;
import com.ssiot.donghai.data.model.SensorModel;
import com.ssiot.donghai.data.model.SensorModifyDataModel;
import com.ssiot.donghai.data.model.SettingInfo1Model;
import com.ssiot.donghai.data.model.SettingModel;
import com.ssiot.donghai.data.model.view.ControlActionViewModel;
import com.ssiot.donghai.data.model.view.ControlDeviceView2Model;
import com.ssiot.donghai.data.model.view.ControlDeviceView3Model;
import com.ssiot.donghai.data.model.view.ControlDeviceViewModel;
import com.ssiot.donghai.data.model.view.ControlNodeViewModel;
import com.ssiot.donghai.data.model.view.NodeView2Model;
import com.ssiot.donghai.data.model.view.NodeViewModel;
import com.ssiot.donghai.data.model.view.SensorViewModel;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.sql.RowSet;

public class AjaxGetNodesDataByUserkey{
    private static final String tag = "AjaxGetNodesDataByUserkey";
    private Sensor sensorBll = new Sensor();
    private ControlActionInfo controlActionInfoBll = new ControlActionInfo();
    private ControlDevice controlDeviceBll = new ControlDevice();
    private ControlLog controllogbll = new ControlLog();
    private SensorModifyData sensorModifyDataBll = new SensorModifyData();
    private Node nodeBll = new Node();
    private com.ssiot.donghai.data.business.Setting settingbll = new com.ssiot.donghai.data.business.Setting();
    private SettingInfo1 settingInfoBll = new SettingInfo1();
    
    public List<NodeView2Model> GetAllNodesDataByUserkey(String userkey){
        try {
            if (!TextUtils.isEmpty(userkey)){
                String areaids = DataAPI.GetAreaIDsByUserKey(userkey);
                List<NodeModel> node_list = DataAPI.GetNodeListByAreaIDs(areaids); 
              //根据节点对象列表获取由","分隔的节点编号字符串
                String nodenostr = DataAPI.GetNodeNoStringByNodeList(node_list);
                Log.v(tag, "-------GetAllNodesDataByUserkey---------nodenostr:" + nodenostr);
                if (TextUtils.isEmpty(nodenostr)){
                    return new ArrayList<NodeView2Model>();
                }
                List<NodeView2Model> nodeView2_list = MobileAPI.GetNodesInfoAndDataByNodenos(nodenostr);
                
                
                return nodeView2_list;
                
            } else {
                Log.e(tag, "------------no userkey!!!!!!!!!!!!!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<NodeView2Model>();
    }
    
    public List<NodeViewModel> GetMapDataByUserkey(String userkey){
        try {
            String userAreaids = "";//(string)Session["Mobiles_Areaids"];
            if (!TextUtils.isEmpty(userAreaids)){
                
            } else {
                userAreaids = DataAPI.GetAreaIDsByUserKey(userkey);
            }
            String userIDs = DataAPI.GetSelfAndChildrenUserIDsByAreaIDs(userAreaids);
            Log.v(tag, "-----GetMapDataByUserkey------userAreaids:" + userAreaids + " userIDs:" + userIDs);
            List<NodeViewModel> nodestate = DataAPI.GetNodesStateByUserIDs(userIDs);
            
//            String result = JsonHelper.JsonSerializer(nodestate);
            return nodestate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(tag, "------GetMapDataByUserkey---null !!!!!!!!!!!!");
        return new ArrayList<NodeViewModel>();
    }
    
    //根据节点编号加载要素和通道
    public HashMap<String,ArrayList<Integer>> GetSensorInfoByNodeNo(String nodeNo){//jingbo 做了修改，只返回一个列表！
        HashMap<String,ArrayList<Integer>> hashList = new HashMap<String,ArrayList<Integer>>();
        try {
            List<SensorViewModel> sensorView_list2 = new ArrayList<SensorViewModel>();
            if (!TextUtils.isEmpty(nodeNo)){
                sensorView_list2 = DataAPI.GetSensorListByNodeNoString(nodeNo);
                if (null != sensorView_list2 && sensorView_list2.size() > 0){
                    for (SensorViewModel m : sensorView_list2){
//                        strList.add(m._shortname + m._channel);
                        if (!hashList.containsKey(m._shortname)){
                            ArrayList<Integer> channels = new ArrayList<Integer>();
                            channels.add(m._channel);
                            hashList.put(m._shortname, channels);
                        } else {
                            hashList.get(m._shortname).add(m._channel);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashList;
    }
    
    //根据要素值（shortName）加载修正类型  
    public String GetModifyTypeBySensorName(String sensorName){//jingbo: 原先是返回两个字符串
        if (!TextUtils.isEmpty(sensorName)){
            SensorModel sensorModel = DataAPI.GetSensorModelBySensorName(sensorName);
            if (null != sensorModel){
                List<SensorModifyDataModel> sensorModifyData_list = sensorModifyDataBll.GetModelList("SensorNo=" + sensorModel._sensorno);
                boolean isBiaoDing = false;//标定
                boolean isJiaoZhun = false;//校准
                String respStr = "";
                for (SensorModifyDataModel modifyData :sensorModifyData_list){
                    switch (modifyData._type) {
                        case 1:
                            isBiaoDing = true;
                            break;
                        case 3:
                            isJiaoZhun = true;
                            break;
                        default:
                            break;
                    }
                    if (isBiaoDing && isJiaoZhun){
                        break;
                    }
                }
                if (isBiaoDing && isJiaoZhun){
                    respStr = "1,3";
                } else if (isBiaoDing){
                    respStr = "1";
                } else if (isJiaoZhun){
                    respStr = "3";
                }
                return respStr;
            }
        }
        return "";
    }
    
    //根据要素与类型加载修正值
    //nodeno节点编号(校准用)
    //channel通道（校准用）
    public ArrayList<String> GetModifyDataBySensorAndType(String sensorName, int modifyType, int nodeno, int channel){
        if (TextUtils.isEmpty(sensorName) || modifyType < 0){
            return null;
        } else {
            if (modifyType == 3){
                List<SensorModifyDataModel> sensorModifyData_list = MobileAPI.GetSensorModifyDataListBySensorNameAndType(sensorName,modifyType);
//                return sensorModifyData_list;
                ArrayList<String> ret = new ArrayList<String>();
                if (sensorModifyData_list != null){
                    for (SensorModifyDataModel m : sensorModifyData_list){
                        ret.add(m._id + ":" + m._remark);//jingbo 例如 5：标准液4
                    }
                }
                return ret;
            } else if (modifyType == 1){//校准
                if (nodeno >= 0 &&  channel >= 0){
                    //根据传感器名称获取传感器相关信息
                    SensorModel sensorModel = DataAPI.GetSensorModelBySensorName(sensorName);
                    //根据节点编号获取节点信息
                    NodeModel nodeModel = DataAPI.GetNodeListByNodenolist(""+nodeno).get(0);
                    //获取上次发送成功的校准信息
                    SettingModel settingModel = MobileAPI.GetJiaoZhunBySensorNameAndType(nodeModel._uniqueid, modifyType, sensorModel._sensorno, channel);
//                    return settingModel._value;
                    ArrayList<String> ret = new ArrayList<String>();
                    ret.add(""+settingModel._value);
                    return ret;
                }
            }
            return null;
        }
    }
    
    //发送保存校准数据
    public boolean SendModify(int modifyId, int channel,int nodeNo2,String jzValue, String sensorShortName){//jzValue 就是value 校准值
        try {
            NodeModel nodeModel = nodeBll.GetModelByNodeNo(""+nodeNo2);//获取节点信息
            if (modifyId == 0){//校准
                
              //根据传感器名称获取传感器对象
                SensorModel sensorObj = DataAPI.GetSensorModelBySensorName(sensorShortName);
                if (settingbll.Exists(nodeModel._uniqueid, 1, sensorObj._sensorno, channel)){//更新
                    SettingModel settingModel_New = settingbll.GetSettigModel("UniqueID='" + nodeModel._uniqueid + 
                            "' AND Type=1 and SettingMark=" + sensorObj._sensorno + " and Chanel=" + channel + " ORDER BY ID DESC");
                    settingModel_New._other = 0;
                    float temp_ft = Float.parseFloat(jzValue);
                    settingModel_New._value = temp_ft;
                    Timestamp dt = new Timestamp(System.currentTimeMillis());
                    settingModel_New._timespan = (int) dt.getTime()/1000;//TODO
                    settingModel_New._sendtime = dt;
                    settingModel_New._sendstate = 0; 
                    settingModel_New._resendcount = 0;
                    return settingbll.Update(settingModel_New);
                } else {//新增
                    SettingModel  settingModel_Add = new SettingModel();
                    settingModel_Add._uniqueid = nodeModel._uniqueid;
                    settingModel_Add._type = 1;
                    settingModel_Add._settingmark = sensorObj._sensorno;
                    settingModel_Add._chanel = channel;
                    settingModel_Add._other = 0; 
                    float temp_ft = Float.parseFloat(jzValue);
                    settingModel_Add._value = temp_ft;
                    Timestamp dt = new Timestamp(System.currentTimeMillis());
                    settingModel_Add._timespan = (int) dt.getTime()/1000;
                    settingModel_Add._sendtime = dt;
                    int count = settingbll.Add(settingModel_Add);
                    return count > 0;
                }
            } else {//标定 sensorModifyData表里的ID值
                SensorModifyDataModel sensorModifyDataModel = sensorModifyDataBll.GetModel(modifyId);
                SettingModel settingModel = new SettingModel();
                settingModel._settingmark = sensorModifyDataModel._sensorno;
                settingModel._chanel = channel;
                settingModel._other = sensorModifyDataModel._other;
                settingModel._type = sensorModifyDataModel._type;
                settingModel._uniqueid = nodeModel._uniqueid;
                settingModel._value = (float)(sensorModifyDataModel._value);
                Timestamp dt = new Timestamp(System.currentTimeMillis());
                settingModel._timespan = (int) dt.getTime()/1000;
                settingModel._sendtime = dt;
                int count = settingbll.Add(settingModel);
                return count > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    //验证是否发送成功( 这种写法存在一定的风险：当多个人同时对这个点修改时可能出现反馈不准)
    public int ModifyIsOK(int type_yz, int channel_yz, int nodeno_yz, String shortName_yz){
        try {
            if (!TextUtils.isEmpty(shortName_yz) && type_yz >= 0 && channel_yz >= 0 && nodeno_yz >= 0){
                SensorModel sensorModel = DataAPI.GetSensorModelBySensorName(shortName_yz);
                NodeModel nodeModel = DataAPI.GetNodeListByNodenolist(""+nodeno_yz).get(0);
              //获取上次发送成功的校准信息
                SettingModel settingModel = settingbll.GetSettigModel("UniqueID='" + nodeModel._uniqueid 
                        + "' and Type=" + type_yz + " and SettingMark=" + sensorModel._sensorno + " and Chanel=" + channel_yz + " order by ID desc");
                return settingModel._sendstate;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 3;
    }
    
    //加载上报频率
    public LoadSetting GetReportFrequency(String nodeNoSetting){
        try {
            //加载该节点信息
            NodeModel nodeInfo = new NodeModel();
            SettingInfo1Model settingInfoModel = new SettingInfo1Model();
            String nodeUniqueId = "";
            if (!TextUtils.isEmpty(nodeNoSetting)) {
                nodeInfo = nodeBll.GetModelByNodeNo(nodeNoSetting);
                nodeUniqueId = nodeInfo._uniqueid;
            }
            //加载该节点上报频率信息
            if (!TextUtils.isEmpty(nodeUniqueId)) {
                SettingModel settingInfo = settingbll.GetSettigModel("UniqueID='" + nodeUniqueId + "' order by ID desc");
                List<SettingInfo1Model> settingInfo1_list = new ArrayList<SettingInfo1Model>();
                if (settingInfo != null && settingInfo._id > 0)  {
                    settingInfo1_list = settingInfoBll.GetModelList("Type=" + settingInfo._type + " and SettingMark=" + settingInfo._settingmark + 
                            " and Channel=" + settingInfo._chanel + "and Other=" + settingInfo._other + " and Value=" + settingInfo._value);
                }
                if (settingInfo1_list != null && settingInfo1_list.size() > 0) {
                    settingInfoModel = settingInfo1_list.get(0);
                }
            }
            //加载上报频率
            List<SettingInfo1Model> settingInfo_list = settingInfoBll.GetModelList("ReportFrequency=3");
            LoadSetting loadSetting = new LoadSetting();
            loadSetting.Name = nodeInfo._location;
            loadSetting.UniqueID = nodeInfo._uniqueid;
            loadSetting.Report = settingInfoModel;
            loadSetting.Longtude = nodeInfo._longitude;
            loadSetting.latitude = nodeInfo._latitude;
            loadSetting.Remark = nodeInfo._remark;
            loadSetting.ReportList = settingInfo_list;
            return loadSetting;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    //修改节点（名称，经纬度，上报频率等）
    public boolean SendNodeReportFrequency(String nodeName, String uniqueid, int reportId,String longitude,String latitude,String remark){
        try {
            if (!TextUtils.isEmpty(uniqueid)){
                boolean isModifyNodeSuccess = false;
                boolean isAddSettingSuccess = false;
                
                //修改节点(Node)
                NodeModel nodeModel = nodeBll.GetModelList(" UniqueID='" + uniqueid +"'").get(0);//??????????TODO TODO 为什么在ajax中使用了缓存？
                if (null != nodeModel){
                    if (!TextUtils.isEmpty(nodeName)){
                        nodeModel._location = nodeName;
                    }
                    if (!TextUtils.isEmpty(longitude) && !TextUtils.isEmpty(latitude)){
                        try {
                            nodeModel._longitude = Float.parseFloat(longitude);
                            nodeModel._latitude = Float.parseFloat(latitude);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!TextUtils.isEmpty(remark)){
                        nodeModel._remark = remark;
                    }
                    if (nodeBll.Update(nodeModel)) {
                        isModifyNodeSuccess = true;
                    }
                }
                
                // 将上报频率等相关信息写入配置表（setting）
                //加载相应的上报频率信息
                if (!TextUtils.isEmpty(""+reportId)) {
                    SettingInfo1Model settingInfoModel = settingInfoBll.GetModel(reportId);
                    SettingModel settingModel = new SettingModel();
                    if (settingInfoModel != null) {
                        settingModel._chanel = settingInfoModel._channel;
                        settingModel._other = settingInfoModel._other;
                        Timestamp dt = new Timestamp(System.currentTimeMillis());
                        settingModel._timespan = (int) dt.getTime()/1000;
                        settingModel._sendtime = dt;
                        settingModel._settingmark = settingInfoModel._settingmark;
                        settingModel._type = settingInfoModel._type;
                        settingModel._uniqueid = uniqueid;
                        settingModel._value = (float)settingInfoModel._value;
                        if (settingbll.Add(settingModel) != 0)
                        {
                            isAddSettingSuccess = true;
                        }
                    }
                }
                
                if (isModifyNodeSuccess && isAddSettingSuccess){
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<NodeView2Model> GetNodesDataByUserkeyAndType(String userkey, String nodeno, String grainsize){//详细数据 TODO
        String beginTime = "";
//        Timestamp now = new Timestamp(System.currentTimeMillis());
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String endTime = formatter.format(now);
        try {
            if (TextUtils.isEmpty(userkey) || TextUtils.isEmpty(grainsize) || TextUtils.isEmpty(nodeno)) {
                return null;
            }
            else {
                if ("十分钟".equals(grainsize)){
                    grainsize = "10分钟";
                    beginTime = buildTime(-2 * 3600 * 1000);
                } else if ("小时".equals(grainsize)){
                    grainsize = "逐小时"; beginTime = buildTime(-24 * 3600 * 1000);
                } else if ("天".equals(grainsize)){
                    grainsize = "逐日"; beginTime = buildTime(-15 * 24 * 3600 * 1000);
                } else if ("月".equals(grainsize)){
                    grainsize = "逐月"; beginTime = buildTime(-365 * 24 * 3600 * 1000);
                } else if ("年".equals(grainsize)){
                    grainsize = "逐年"; beginTime = buildTime(-10 * 365 * 24 * 3600 * 1000);
                } else {
                    grainsize = "逐小时"; beginTime = buildTime(-24 * 3600 * 1000);
                }
                //获取节点数据
                List<NodeView2Model> nodeView2_list = MobileAPI.GetNodesDataByNodenosAndQueryType(nodeno, grainsize, "平均值", beginTime, endTime);
                return nodeView2_list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //根据Usekey获取控制节点的扩展信息
    public List<ControlNodeViewModel> GetControlNodesByUserkey(String userkey){
        if (TextUtils.isEmpty(userkey)) {
            return null;
        }
        try
        {
            String areaIds = "";
//            if (Session["Mobiles_Areaids"] != null)//TODO
//            {
//                areaIds = Session["Mobiles_Areaids"].ToString();
//            } else {
                areaIds = DataAPI.GetAreaIDsByUserKey(userkey);
//            }
            List<ControlNodeViewModel> controlNodes_list = MobileAPI.GetControlNodesInfoByAreaIds(areaIds);
            return controlNodes_list;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    //this is in ControlController.cs web端使用的接口
    public List<ControlNodeModel> ControlNodeList(String nodeno,String userIDs,int userrole){//(String id, int pageindex = 1)
        String where = "1=1 ";
        if (!TextUtils.isEmpty(nodeno)){//正常是空的表示查找所有
            where += "AND NodeNo ='" + nodeno + "'";
        }

        if (userrole != 1) {//管理员权限
//            String userIDs = "";
//            userIDs = (string)System.Web.HttpContext.Current.Session["UserIDs"];
            where += "AND AreaID in(" + DataAPI.GetAreaIDByUserIDs(userIDs) + ")";
        }
//        ViewBag.UserRole = User.UserRole;
        List<ControlNodeModel> list = (List<ControlNodeModel>) DataAPI.mControlNodeService.GetModelList(where);
//        List<ControlNodeModel> list2 = list.OrderByDescending(c => c.NodeNo).ToList();
//        PagedList<ControlNodeModel> clist = list2.ToPagedList(pageindex, 8);
        return list;
    }
    
    public List<ControlDeviceView3Model> GetDeviceActionInfo(String controlNodeId,String controlUnique){//获取控制节点下的所有设备及其关联的控制动作
        try {
            if (!TextUtils.isEmpty(controlNodeId) && !TextUtils.isEmpty(controlUnique))
            {
                HashMap<String, String> sensor_dic = new HashMap<String, String>();
                List<SensorModel> sensorList = sensorBll.GetModelList("1=1");
                for(SensorModel sensor : sensorList){
                    sensor_dic.put(""+sensor._sensorno, sensor._shortname);
                }
                List<ControlDeviceViewModel> controlView_list = DataAPI.GetDeviceActionInfo(Integer.parseInt(controlNodeId), controlUnique);
                List<ControlDeviceView2Model> controlView2_list = new ArrayList<ControlDeviceView2Model>();
                
                for (ControlDeviceViewModel controlDeviceView : controlView_list){
                    ControlDeviceView2Model controlView2 = new ControlDeviceView2Model();
                    controlView2.DeviceID = controlDeviceView.DeviceID;
                    controlView2.ControlNodeID = controlDeviceView.ControlNodeID;
                    controlView2.DeviceNo = controlDeviceView.DeviceNo;
                    controlView2.DeviceName = controlDeviceView.DeviceName;
                    if (controlDeviceView.RunTime > 0 && !TextUtils.isEmpty(controlDeviceView.StartTime)){
                        int runTime_val = controlDeviceView.RunTime;//jingbo 貌似是秒数
                        Timestamp createTime = Timestamp.valueOf(controlDeviceView.StartTime);
                        if (System.currentTimeMillis() > (createTime.getTime() + controlDeviceView.RunTime * 1000)) {
                            controlView2.DeviceStateNow = "关闭";
                        } else {
                            controlView2.DeviceStateNow = "开启";
                        }
                    } else {
                        controlView2.DeviceStateNow = "关闭";
                    }
                    controlView2.ControlActionID = controlDeviceView.ControlActionID;
                    controlView2.ControlType = controlDeviceView.ControlType;
                    controlView2.CollectUniqueIDs = controlDeviceView.CollectUniqueIDs;

                    controlView2.ControlCondition = controlDeviceView.ControlCondition;
                    controlView2.OperateTime = controlDeviceView.OperateTime;
                    controlView2.Operate = controlDeviceView.Operate;
                    
                  //匹配触发条件 把触发条件名称改为看得懂的
                    if (controlView2.ControlType == 6) {
                        String condition = controlView2.ControlCondition;
                        condition = jingboRegex(condition, sensor_dic);
                        controlView2.ControlCondition = condition;
                        controlView2_list.add(controlView2);
                    } else {
                        //不是触发
                        controlView2_list.add(controlView2);
                    }
                }
                List<ControlDeviceView3Model> controlView3_list = new ArrayList<ControlDeviceView3Model>();
                if (controlView2_list.size() > 0){
                    for (int i = 0; i < controlView2_list.size(); i ++){
                        ControlDeviceView2Model view2 = controlView2_list.get(i);
                        ControlDeviceView3Model view3 = getExistCDV3Model(controlView3_list, view2.DeviceID);
                        boolean isNewCreated = false;
                        if (view3 == null){//这个device不存在，就新添加 例如：设备2
                            view3 = new ControlDeviceView3Model();
                            view3.DeviceID = view2.DeviceID;
                            view3.DeviceName = view2.DeviceName;
                            view3.DeviceNo = view2.DeviceNo;
                            view3.ControlNodeID = view2.ControlNodeID;
                            view3.DeviceStateNow = view2.DeviceStateNow;
                            isNewCreated = true;
                        }
                        
                        if (view3.ActionList == null){
                            view3.ActionList = new ArrayList<ControlActionViewModel>();
                        }
                        
                        
                        if (3 == view2.ControlType){//数据表中定时规则可能一行多条
                            ArrayList<String> multiTimingConditions = Utils.parseJSON_MultiTiming(view2.ControlCondition);
                            for (String condi : multiTimingConditions){
                                ControlActionViewModel actionView = new ControlActionViewModel();
                                actionView.ControlActionID = view2.ControlActionID;
                                actionView.CollectUniqueIDs = view2.CollectUniqueIDs;
                                actionView.ControlCondition = condi;
                                actionView.ControlType = view2.ControlType;
                                actionView.Operate = view2.Operate;
                                actionView.OperateTime = view2.OperateTime;
                                actionView.StateNow = view2.StateNow;   
                                view3.ActionList.add(actionView);
                            }
                        } else if (view2.ControlType > 0){//jingbo把空的过滤掉；GetDeviceActionInfo中早就把空的添加了，也是有必要的
                            ControlActionViewModel actionView = new ControlActionViewModel();
                            actionView.ControlActionID = view2.ControlActionID;
                            actionView.CollectUniqueIDs = view2.CollectUniqueIDs;
                            actionView.ControlCondition = view2.ControlCondition;
                            actionView.ControlType = view2.ControlType;
                            actionView.Operate = view2.Operate;
                            actionView.OperateTime = view2.OperateTime;
                            actionView.StateNow = view2.StateNow;
                            view3.ActionList.add(actionView);
                        }
                        
                        if (isNewCreated){
                            controlView3_list.add(view3);
                        }
                    }
                }
                reOrder(controlView3_list);//by jingbo
                return controlView3_list;
                //TODO
            } else {
                Log.e(tag, "--------string is null:"+controlNodeId + controlUnique);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //根据节点标识（UniqueID）和设备编号（DeviceNo）控制设备 jingbo 代码中好像只用到了close部分
    public String ControlDevice(String uniqueID,String deviceNo,String openTime, String isOpen){
        if ("open".equalsIgnoreCase(isOpen)){
            if (!TextUtils.isEmpty(uniqueID)){
                if (!TextUtils.isEmpty(deviceNo)){
                    String rtnStr = DataAPI.SetTimeControl(uniqueID, deviceNo, openTime);
                    if (rtnStr.contains("done")){
                        return "true";
                    } else {
                        return "false";
                    }
                } else {//deviceNo为空，表示全部打开
                    String returnInfo = "";
                    List<ControlDeviceModel> controlDevice_list = controlDeviceBll.GetModelList("ControlNodeID in (SELECT ID FROM ControlNode WHERE UniqueID='" + uniqueID + "')");
                    if (controlDevice_list != null && controlDevice_list.size() > 0)  {
                        for (ControlDeviceModel controlDevice : controlDevice_list){//TODO
                            String rtnStr = DataAPI.SetTimeControl(uniqueID, ""+controlDevice._deviceno, openTime);//log ？？写入数据库
                            if (rtnStr.contains("done")) {
                                returnInfo += controlDevice._deviceno + "号设备:true,";
                            } else {
                                returnInfo += controlDevice._deviceno + "号设备:false,";
                                Log.e(tag, "-------returnInfo--!!!!!!!!!!");
                            }
                        }
                    }
                    if (returnInfo.contains("false")) {
                        returnInfo = "false";
                    } else {
                        returnInfo = "true";
                    }
                    return returnInfo;
                }
            } else {
                Log.e(tag, "-------uniqueid is null !!!!!");
                return "";
            }
        } else if ("close".equalsIgnoreCase(isOpen)){
            Log.v(tag, "-------------" + uniqueID + " " + deviceNo);
            if (!TextUtils.isEmpty(uniqueID)){
                if (!TextUtils.isEmpty(deviceNo)){//单个关闭
                    Log.v(tag, "单个关闭");
                    String rtnStr = DataAPI.ControlControlCloseNow(uniqueID, deviceNo);//ControlLog表
                    List<ControlActionInfoModel> actioninfo = controlActionInfoBll
                            .GetModelList(" UniqueID='" + uniqueID + "' and DeviceNo=" + deviceNo + " and ControlType=1");
                    if (actioninfo != null && actioninfo.size() > 0){
                        for (ControlActionInfoModel opens : actioninfo){
                            try {
                                controlActionInfoBll.Delete(opens._id);//ControlActionInfo表
                            } catch (Exception e) {
                                e.printStackTrace();
                                return "false";
                            }
                        }
                    }
                    
                    if (rtnStr.contains("done")){
                        return "true";
                    } else {
                        return "false";
                    }
                } else {//全部关闭？？
                    Log.v(tag, "全部关闭");
                    String returnInfo = "";
                    List<ControlDeviceModel> controlDevice_list = controlDeviceBll
                            .GetModelList("ControlNodeID in (SELECT ID FROM ControlNode WHERE UniqueID='" + uniqueID + "')");
                    if (controlDevice_list != null && controlDevice_list.size() > 0){
                        for (ControlDeviceModel controlDevice : controlDevice_list){
                            String rtnStr = DataAPI.ControlControlCloseNow(uniqueID, ""+controlDevice._deviceno);
                            if (rtnStr.contains("done")){
                                returnInfo += controlDevice._deviceno + "号设备:true,";
                            } else {
                                returnInfo += controlDevice._deviceno + "号设备:false,";
                            }
                            List<ControlActionInfoModel> actioninfo = controlActionInfoBll
                                    .GetModelList(" UniqueID='" + uniqueID + "' and DeviceNo=" + controlDevice._deviceno + " and ControlType=1" );
                            if (actioninfo != null && actioninfo.size() > 0){
                                for (ControlActionInfoModel opens : actioninfo){
                                    try {
                                        controlActionInfoBll.Delete(opens._id);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        returnInfo = "false";
                                    }
                                }
                            }
                        }
                    }
                    if (returnInfo.contains("false")){
                        returnInfo = "false";
                    } else {
                        returnInfo = "true";    
                    }
                    return returnInfo;
                }
            } else {
                Log.e(tag, "!!!!!!!!!!!!!!!");
                return "";
            }
        }
        return "";
    }
    
    public boolean SaveControlAdd(String userkey, String timeCondition,String UniqueID, int controlTypes,String DeviceNo,String updateid){
        String names = "";
        try {
            if (!TextUtils.isEmpty(timeCondition)){//必须有规则字符串
                ControlActionInfoModel controlActionInfo = new ControlActionInfoModel();
                if (TextUtils.isEmpty(DeviceNo)){//全部立即开启
                    List<ControlDeviceModel> controlDevice_list = controlDeviceBll
                            .GetModelList("ControlNodeID in (SELECT ID FROM ControlNode WHERE UniqueID='" + UniqueID + "')");
                    if (controlDevice_list != null && controlDevice_list.size() > 0){
                        for(ControlDeviceModel controlDevice : controlDevice_list){
                          //1.controlDevice.DeviceNo  判断设备是否已经有立即开启数据  有就查找出要修改的ID
                            List<ControlActionInfoModel> actioninfo = controlActionInfoBll
                                    .GetModelList(" UniqueID='" + UniqueID + "' and DeviceNo=" + controlDevice._deviceno + " and ControlType=" + controlTypes);
                            if (actioninfo != null && actioninfo.size() > 0){
                                for (ControlActionInfoModel open : actioninfo){
                                    controlActionInfo = controlActionInfoBll.GetModel(open._id);
                                    controlActionInfo._areaid = Integer.parseInt(DataAPI.GetAreaIDsByUserKeys(userkey));
                                    names = "立即开启";
                                    controlActionInfo._controlname = names;
                                    controlActionInfo._uniqueid = UniqueID;
                                    controlActionInfo._deviceno = controlDevice._deviceno;
                                    controlActionInfo._controltype = controlTypes;
                                    controlActionInfo._controlcondition = timeCondition;
                                    controlActionInfo._operatetime = new Timestamp(System.currentTimeMillis());
                                    controlActionInfo._statenow = 0;
                                    controlActionInfo._operate = "打开";
                                    if (false == updateControlActionInfoAndAddLog(controlActionInfo)){
                                        return false;
                                    }
                                }
                            } else {//没有已存的数据
                                controlActionInfo._areaid = Integer.parseInt(DataAPI.GetAreaIDsByUserKeys(userkey));
                                if (controlTypes == 1) {
                                    names = "立即开启";
                                } else {
                                    return false;
                                }
                                controlActionInfo._controlname = names;// (string)Session["controlActionName"];
                                controlActionInfo._uniqueid = UniqueID;
                                controlActionInfo._deviceno = controlDevice._deviceno;
                                controlActionInfo._controltype = controlTypes;
                                controlActionInfo._controlcondition = timeCondition;
                                controlActionInfo._operatetime = new Timestamp(System.currentTimeMillis());
                                controlActionInfo._statenow = 0;
                                controlActionInfo._operate = "打开";
                                if (false == addControlActionInfoAndAddLog(controlActionInfo)){
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                }//全部立即开启结束
                
                //修改
                if (!TextUtils.isEmpty(updateid)){
                    controlActionInfo = controlActionInfoBll.GetModel(Integer.parseInt(updateid));
                    controlActionInfo._areaid = Integer.parseInt(DataAPI.GetAreaIDsByUserKeys(userkey));
                    if (controlTypes == 1) {
                        names = "立即开启";
                    } else if (controlTypes == 3) {
                        names = "定时";
                    } else if (controlTypes == 5) {
                        names = "循环";
                    }
                    controlActionInfo._controlname = names;// (string)Session["controlActionName"];
                    controlActionInfo._uniqueid = UniqueID;
                    controlActionInfo._deviceno = Integer.parseInt(DeviceNo);
                    controlActionInfo._controltype = controlTypes;
                    controlActionInfo._controlcondition = timeCondition;
                    controlActionInfo._operatetime = new Timestamp(System.currentTimeMillis());
                    controlActionInfo._statenow = 0; 
                    controlActionInfo._operate = "打开";
                    if (false == updateControlActionInfoAndAddLog(controlActionInfo)){
                        return false;
                    }
                } else {//新增
                    if (!TextUtils.isEmpty(DeviceNo)){
                        if (DeviceNo.endsWith(",")){
                            DeviceNo = DeviceNo.substring(0, DeviceNo.length()-1);
                        }
                        String[] nos = DeviceNo.split(",");
                        for(String devicesel : nos){
                            controlActionInfo._areaid = Integer.parseInt(DataAPI.GetAreaIDsByUserKeys(userkey));
                            if (controlTypes == 1) {
                                names = "立即开启";
                            } else if (controlTypes == 3) {
                                names = "定时";
                            } else if (controlTypes == 5) {
                                names = "循环";
                            }
                            controlActionInfo._controlname = names;// (string)Session["controlActionName"];
                            controlActionInfo._uniqueid = UniqueID;
                            controlActionInfo._deviceno = Integer.parseInt(devicesel);
                            controlActionInfo._controltype = controlTypes;
                            controlActionInfo._controlcondition = timeCondition;
                            controlActionInfo._operatetime = new Timestamp(System.currentTimeMillis());
                            controlActionInfo._statenow = 0;
                            controlActionInfo._operate = "打开";
                            
                            if (false == addControlActionInfoAndAddLog(controlActionInfo)){
                                return false;
                            }
                        }
                        return true;
                        
                    } else {
                        Log.e(tag, "新增规则，但deviceno 为空");
                        return false;
                    }
                }
            } else {
                Log.e(tag, "timeCondition 字符串为空");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean SaveControlTriggerUser(String UniqueID, int deviceNo, String selectedNodes, String conditionStr, String updateid, String userkeys){
        ControlActionInfoModel controlActionInfo = new ControlActionInfoModel();
        if (null != selectedNodes && selectedNodes.endsWith(",")){
            selectedNodes = selectedNodes.substring(0,selectedNodes.length() -1);
        }
        if (!TextUtils.isEmpty(updateid)){
            int id = Integer.parseInt(updateid);
            controlActionInfo = controlActionInfoBll.GetModel(id);
            controlActionInfo._controlname = "触发";
            controlActionInfo._uniqueid = UniqueID;
            controlActionInfo._deviceno = deviceNo;
            controlActionInfo._controltype = 6;
            controlActionInfo._collectuniqueids = selectedNodes;
            controlActionInfo._controlcondition = conditionStr;
            controlActionInfo._operatetime = new Timestamp(System.currentTimeMillis());
            controlActionInfo._operate = "打开";
            controlActionInfo._statenow = 0;
            controlActionInfo._remark = "";
            return controlActionInfoBll.Update(controlActionInfo);
        } else {
            controlActionInfo._areaid = Integer.parseInt(DataAPI.GetAreaIDsByUserKeys(userkeys));
            controlActionInfo._controlname = "触发";
            controlActionInfo._uniqueid = UniqueID;
            controlActionInfo._deviceno = deviceNo;
            controlActionInfo._controltype = 6;
            controlActionInfo._collectuniqueids = selectedNodes;
            controlActionInfo._controlcondition = conditionStr;
            controlActionInfo._operatetime = new Timestamp(System.currentTimeMillis());
            controlActionInfo._operate = "打开";
            controlActionInfo._statenow = 0;
            controlActionInfo._remark = "";
            return controlActionInfoBll.Add(controlActionInfo) > 0;
        }
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
    
    public boolean DelControl(int id){//TODO
        try {
            ControlActionInfoModel controlActionInfo = controlActionInfoBll.GetModel(id);
            List<ControlLogModel> controlLog_list_before = DataAPI.ConvertControlActionInfoToControlLog(controlActionInfo);//
            if (controlActionInfoBll.Delete(id)) {
                if (controlLog_list_before.size() == 0) {
                    return true;
                }
                List<ControlLogModel> controlLog_list_after = new ArrayList<ControlLogModel>();
                for (ControlLogModel controlLog : controlLog_list_before) {
                    ControlLogModel controlLogModel = new ControlLogModel();
                    controlLogModel._logtype = 0; 
                    controlLogModel._uniqueid = controlLog._uniqueid;
                    controlLogModel._deviceno = controlLog._deviceno;
                    controlLogModel._starttype = controlLog._starttype;
                    controlLogModel._startvalue = 0; 
                    controlLogModel._runtime = 0; 
                    controlLogModel._endtype = 0; 
                    controlLogModel._endvalue = 0; 
                    controlLogModel._sendstate = controlLog._sendstate;
                    controlLogModel._createtime = controlLog._createtime;
                    controlLogModel._edittime = controlLog._edittime;
                    controlLogModel._timespan = controlLog._timespan;
                    controlLog_list_after.add(controlLogModel);
                }
                if (controllogbll.AddManyCount(controlLog_list_after) < 1)  {
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private String jingboRegex(String condition,HashMap<String, String> sensor_dic){
        String str = condition;
        String reg = "\\d+-\\d";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(reg);
        java.util.regex.Matcher matcher=pattern.matcher(str);
        Log.v("AjaxGertddddddddddd", "--------------count:"+matcher.groupCount());
        while(matcher.find()){
            String sensorNo = matcher.group().split("-")[0];
            Log.v("d", "-------------------sensorNo:" +sensorNo);
            String sensorName = sensor_dic.get(sensorNo);
            str = str.replace(matcher.group(), sensorName + "-" + matcher.group().split("-")[1]);
        }
        Log.v("--------=-", str);
        return str;
    }
    
    private boolean deviceExists(List<ControlDeviceView3Model> cdv3m, int deviceId){
        if (cdv3m != null){
            for (int i = 0; i < cdv3m.size(); i ++){
                if (cdv3m.get(i).DeviceID == deviceId && deviceId > 0){
                    return true;
                }
            }
        }
        return false;
    }
    
    private ControlDeviceView3Model getExistCDV3Model(List<ControlDeviceView3Model> cdv3m, int deviceId){
        if (cdv3m != null){
            for (int i = 0; i < cdv3m.size(); i ++){
                if (cdv3m.get(i).DeviceID == deviceId && deviceId > 0){
                    return cdv3m.get(i);
                }
            }
        }
        return null;
    }

    
    /*
    //根据节点编号和传感器加载查询的数据，并以曲线图的形式返回
    public String GetMobileChart(String nodeno,String linetype,String type,String starttime,String sensorChannel){
        String query_nodeno = nodeno;
        try {
            if (!TextUtils.isEmpty(query_nodeno)){
                List<SensorViewModel> sensorView_list = DataAPI.GetSensorListByNodeNoString(query_nodeno);
                if (TextUtils.isEmpty(sensorChannel)){//根据sensorChannel是否为空来确定是否是第一次加载
                    if (sensorView_list.get(0)._channel > 0){
                        sensorChannel = sensorView_list.get(0)._shortname + sensorView_list.get(0)._channel;
                    } else {
                        sensorChannel = sensorView_list.get(0)._shortname;
                    }
                }
                String dataChart = new AjaxNodesData().AjaxGetJsonChart(query_nodeno, linetype, type, starttime, sensorChannel);
                
                //将NodeNo的Sensor+Channel传给客户端
                String sensorChannel_list = "";
                for (SensorViewModel sensorview : sensorView_list){
                    if (sensorview._channel > 0){
                        sensorChannel_list += sensorview._shortname + sensorview._channel + ";";
                    } else {
                        sensorChannel_list += sensorview._shortname + ";";
                    }
                }
                if (null != sensorChannel_list && sensorChannel_list.endsWith(",")){
                    sensorChannel_list = sensorChannel_list.substring(0, sensorChannel_list.length()-1);
                }
                return dataChart + "|" + sensorChannel_list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }*/
    
    private String buildTime(long seconds){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date(System.currentTimeMillis() + seconds * 1000);
        return formatter.format(d);
        
    }
    
    private void reOrder(List<ControlDeviceView3Model> list){
//        final Collator sCollator = Collator.getInstance();
        Collections.sort(list, new Comparator<ControlDeviceView3Model>() {
            @Override
            public int compare(ControlDeviceView3Model lhs, ControlDeviceView3Model rhs) {
                // TODO Auto-generated method stub
                return lhs.DeviceNo - rhs.DeviceNo;
            }
        });
    }
    
    public class LoadSetting{
        public String Name;
        public String UniqueID;
        public SettingInfo1Model Report;
        public float Longtude;
        public float latitude;
        public String Remark;
        public List<SettingInfo1Model> ReportList;
    }
    
    ////////////////////////////////////////////////////////////////////////
    //根据UniqueIDs，获取传感器
    public List<SensorViewModel> GetSensorInfo(String selectednodes){
        List<SensorViewModel> sv_list;// = new ArrayList<SensorViewModel>();
        String[] uniquelist = null;
        if (!TextUtils.isEmpty(selectednodes)){
            uniquelist = selectednodes.split(",");
        }
        
        //在Node表中，根据UniqueID获取NodeNo
        if (null != uniquelist && uniquelist.length > 0) {
            String struniqueids = ""; 
            for (String uniqueid : uniquelist) {
                struniqueids += "'" + uniqueid + "',";
            }
            struniqueids = struniqueids.substring(0, struniqueids.lastIndexOf(","));
            String strwhere = "UniqueID in (" + struniqueids + ")";
            List<NodeModel> nodelist = nodeBll.GetModelList(strwhere);
            String nodeslist = ""; 
            if (null == nodelist || nodelist.size() == 0){
                return null;
            }
            for (NodeModel node : nodelist) {
                nodeslist += node._nodeno + ",";
            }
            nodeslist = nodeslist.substring(0, nodeslist.lastIndexOf(","));
            //根据Uniqueid关联的节点编号（Nodeno）查询
            sv_list = DataAPI.GetSensorListByNodeNoString(nodeslist);
        } else {
            sv_list = null;
        }
        return sv_list;
    }
}