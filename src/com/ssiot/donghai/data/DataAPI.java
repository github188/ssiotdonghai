
package com.ssiot.donghai.data;

import android.text.TextUtils;
import android.util.Log;

import com.ssiot.donghai.data.business.Area;
import com.ssiot.donghai.data.business.ControlDevice;
import com.ssiot.donghai.data.business.ControlLog;
import com.ssiot.donghai.data.business.ControlNode;
import com.ssiot.donghai.data.business.LatestData;
import com.ssiot.donghai.data.business.LiveData;
import com.ssiot.donghai.data.business.Node;
import com.ssiot.donghai.data.business.Sensor;
import com.ssiot.donghai.data.business.User;
import com.ssiot.donghai.data.business.VLCVideoInfo;
import com.ssiot.donghai.data.model.AreaModel;
import com.ssiot.donghai.data.model.ControlActionInfoModel;
import com.ssiot.donghai.data.model.ControlLogModel;
import com.ssiot.donghai.data.model.LatestDataModel;
import com.ssiot.donghai.data.model.NodeModel;
import com.ssiot.donghai.data.model.SensorModel;
import com.ssiot.donghai.data.model.UserModel;
import com.ssiot.donghai.data.model.VLCVideoInfoModel;
import com.ssiot.donghai.data.model.view.ControlActionViewInfoModel;
import com.ssiot.donghai.data.model.view.ControlDeviceViewModel;
import com.ssiot.donghai.data.model.view.ControlTimeConditionModel;
import com.ssiot.donghai.data.model.view.NodeViewModel;
import com.ssiot.donghai.data.model.view.SensorViewModel;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

//仿照 Business/Angel.Extend.Business/API/DataAPI.cs
public class DataAPI {
    private static final String tag = "DataAPI";
    public static Sensor mSensorSevice = new Sensor();
    public static LiveData mLiveDataSevice = new LiveData();
    public static User mUserService = new User();
    public static ControlNode mControlNodeService = new ControlNode();
    public static Area bllarea = new Area();
    public static Node mNodeSevice = new Node();
    public static LatestData mLatestDataSevice = new LatestData();
    public static VLCVideoInfo bllVLCvideoinfo = new VLCVideoInfo();
    public static ControlLog ControllogService = new ControlLog();
    
    public static ControlDevice controlDeviceBll = new ControlDevice();
    

    public static String GetAreaIDByUserIDs(String userids){
        String areaids = "";
        //User model = UserSevice.GetModelByCache(userid);
        if (TextUtils.isEmpty(userids)) {
            areaids = "-1";
            return areaids;
        } else {
            List<UserModel> user_list = mUserService.GetModelList("UserID in (" + userids + ")");
            if (user_list != null){
                for (UserModel m : user_list){
                    areaids += m._areaid + ",";
                }
                //除去尾部","号
                if (!TextUtils.isEmpty(areaids) && areaids.endsWith(",")) {
                    areaids = areaids.substring(0, areaids.length()-1);
                }
                return areaids;
            } else {
                return "-1";
            }
        }
    }
    
    //根据UserKey获取用户的AreaIDs(包括其子帐户)
    public static String GetAreaIDsByUserKey(String userkey) {
        List<UserModel> models = mUserService.GetModelList(" UniqueID='" + userkey + "'");
        if (null != models && models.size() > 0){
            UserModel mUserModel = models.get(0);
            return GetSelfAndChildrenAreaIDsByAreaID(mUserModel._areaid);
        }
        return null;
    }
    
    public static String GetAreaIDsByUserKeys(String userkey) {
        List<UserModel> models = mUserService.GetModelList(" UniqueID='" + userkey + "'");
        if (null != models && models.size() > 0){
            UserModel mUserModel = models.get(0);
            return ""+mUserModel._areaid;
        }
        Log.e(tag, "------GetAreaIDsByUserKeys-----null");
        return "";
    }

    private static String GetSelfAndChildrenAreaIDsByAreaID(int areaid) {
        String areaidsStr = "";
        // String catchkey = "GetSelfAndChildrenAreaIDsByAreaID_" +
        // areaid.GetHashCode().toString();//获取hashkey为字典ID
        // String objModel = Angel.Common.Web.DataCache.GetCache(catchkey);
        String objModel = null;
        if (objModel == null) {
            AreaModel area_mdl = new AreaModel();
            List<AreaModel> area_list = new ArrayList<AreaModel>();
            if (areaid == 0){//管理员
                area_list = bllarea.GetModelList(" 1=1");
                if (area_list.size() > 0) {
                    for (AreaModel a : area_list) {
                        areaidsStr += a._areaid + ",";
                    }
                }
            } else {
                area_mdl = bllarea.GetModel(areaid);
                if (area_mdl != null) {
                    area_list = bllarea.GetSelfAndChildrenAreaByAreaCode(area_mdl._areacode);
                    if (area_list.size() > 0) {
                        for (AreaModel a : area_list) {
                            areaidsStr += a._areaid + ",";
                        }
                    }
                } else {
                    areaidsStr = "" + areaid;
                }
            }
            if (!TextUtils.isEmpty(areaidsStr) && areaidsStr.contains(",")) {
                areaidsStr = areaidsStr.trim();// trim(',')
            }
            if (!TextUtils.isEmpty(areaidsStr) && areaidsStr.endsWith(",")){
                areaidsStr = areaidsStr.substring(0, areaidsStr.length()-1);
            }
            objModel = areaidsStr;
            // Angel.Common.Web.DataCache.SetCache(catchkey, objModel);
        }
        Log.v(tag, "------------------------GetSelfAndChildrenAreaIDsByAreaID" + areaid + " return:"+objModel);
        return objModel;
    }
    
 // 根据包括自身在内的所有子节点的AreaID获取UserIDs
    public static String GetSelfAndChildrenUserIDsByAreaIDs(String AreaIDs){
        String userIDs = "";
        String objModel = null;
        if (objModel == null) {
            List<UserModel> user_list = new ArrayList<UserModel>();
            if (!TextUtils.isEmpty(AreaIDs)){
                user_list = mUserService.GetModelList(" AreaID IN (" + AreaIDs + ")");
            }
            if (null != user_list && user_list.size() > 0){
                for(UserModel user : user_list){
                    userIDs += user._userid + ",";
                }
            }
            if (!TextUtils.isEmpty(userIDs) && userIDs.endsWith(",")){
                userIDs = userIDs.substring(0, userIDs.length()-1);
            }
        }
        objModel = userIDs;
        return objModel;
    }
    
    public static List<NodeModel> GetNodeListByAreaIDAndPlace(int areaid, String place){
        String strwhere = "";
        if (areaid == 0){
            strwhere = " Location like '%" + place + "%'";
        } else {
            strwhere = "AreaID=" + areaid + " AND Location like '%" + place + "%'";
        }
//        return mNodeSevice.GetNodeList(strwhere);//在我看来是一个冗余的方法
        return mNodeSevice.GetModelList(strwhere);
    }
    
    public static List<ControlLogModel> ConvertControlActionInfoToControlLog(ControlActionInfoModel actionInfo){
        List<ControlLogModel> controlLog_list = new ArrayList<ControlLogModel>();
        //
        switch (actionInfo._controltype) {
         // 1,为立即开启；2，为立即关闭；3，为定时开启；5，为循环开启；6，为触发
            case 1:
            case 2:
                try {
                    ControlLogModel controlLog = new ControlLogModel();
                    controlLog._logtype = 0;
                    controlLog._uniqueid = actionInfo._uniqueid;
                    controlLog._deviceno = actionInfo._deviceno;
                    controlLog._starttype = actionInfo._controltype;
                    controlLog._startvalue = 0;
                    if (actionInfo._controltype == 1){
                        controlLog._runtime = Integer.parseInt(actionInfo._controlcondition) * 60;
                    } else {//立即关闭
                        controlLog._runtime = 60;
                    }
                    controlLog._endtype = 2;
                    controlLog._endvalue = 0;
                    controlLog._sendstate = actionInfo._statenow;
                    controlLog._createtime = actionInfo._operatetime;
                    controlLog._edittime = actionInfo._operatetime;
                    controlLog._timespan = (int) ConvertDataTimeLong(actionInfo._operatetime);
                    controlLog_list.add(controlLog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                ControlActionViewInfoModel<ControlTimeConditionModel> controlTimeActionView = ConvertToControlTimeActionView(actionInfo);
                if (controlTimeActionView != null){
                    List<ControlTimeConditionModel> controlTimeCondition_list = controlTimeActionView._controlcondition;
                    int count = 0;
                    for (ControlTimeConditionModel timeCondition : controlTimeCondition_list){
                        ControlLogModel controlLog = new ControlLogModel();
                        controlLog._logtype = 0;
                        controlLog._uniqueid = controlTimeActionView._uniqueid;
                        controlLog._deviceno = Integer.parseInt(controlTimeActionView._devicename) + 16 * count;
                        controlLog._starttype = controlTimeActionView._controltype;
                        controlLog._startvalue = (int)ConvertDataTimeLong(timeCondition.StartTime);
                        controlLog._runtime = (int) (timeCondition.EndTime.getTime() - timeCondition.StartTime.getTime())/1000;//由分钟转为秒
                        controlLog._endtype = 4;
                        controlLog._endvalue = (int)ConvertDataTimeLong(timeCondition.EndTime);
                        controlLog._sendstate = controlTimeActionView._statenow;
                        controlLog._createtime = controlTimeActionView._operatetime;
                        controlLog._edittime = controlTimeActionView._operatetime;
                        controlLog._timespan = (int)ConvertDataTimeLong(controlTimeActionView._operatetime)+10*count;
                        controlLog_list.add(controlLog);
                        count++;
                    }
                }
                break;
            case 5:

                break;
            case 6:
                ////触发的有单独的服务运行，不在此转化 TODO
                break;

            default:
                break;
        }
        return controlLog_list;
    }
    
    private static long ConvertDataTimeLong(Timestamp timestamp){
        return timestamp.getTime()/1000;
    }
    
    //辅助方法 将定时类型的控制动作配置解析为TimeActionView
    public static ControlActionViewInfoModel<ControlTimeConditionModel> ConvertToControlTimeActionView(ControlActionInfoModel actionInfo){
        ControlActionViewInfoModel<ControlTimeConditionModel> controlTimeActionView = new ControlActionViewInfoModel<ControlTimeConditionModel>();
        try {
            controlTimeActionView._id = actionInfo._id;
            controlTimeActionView._areaid = actionInfo._areaid;
            controlTimeActionView._controlname = actionInfo._controlname;
            controlTimeActionView._uniqueid = actionInfo._uniqueid;
            // string timekey = actionInfo.UniqueID + "_" + actionInfo.DeviceNo;
            // controlTimeActionView.DeviceName = controlDic[timekey].DeviceName;//获取对应设备名
            controlTimeActionView._devicename = ""+actionInfo._deviceno;//在此存为DeviceNo
            controlTimeActionView._controltype = actionInfo._controltype;

            List<ControlTimeConditionModel> controlTimeCondition_list = new ArrayList<ControlTimeConditionModel>();
            if (!TextUtils.isEmpty(actionInfo._controlcondition)) {
//                JavaScriptSerializer jss = new JavaScriptSerializer();
//                controlTimeCondition_list = jss.Deserialize<List<ControlTimeConditionModel>>(actionInfo.ControlCondition);
                //TODO
            }
            controlTimeActionView._controlcondition = controlTimeCondition_list;

            controlTimeActionView._operatetime = actionInfo._operatetime;
            controlTimeActionView._statenow = actionInfo._statenow;
            controlTimeActionView._operate = actionInfo._operate;
            controlTimeActionView._remark = actionInfo._remark;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return controlTimeActionView;
    }
    
    
    
    private static String buildJSON(){
        return "";
    }
    
    //立即关闭
    public static String ControlControlCloseNow(String uniqueid, String deviceno){
        try{
            ControlLogModel model = new ControlLogModel();
            model._uniqueid = uniqueid.trim();
            model._logtype = 0; 
            model._deviceno = Integer.parseInt(deviceno);
            model._starttype = 2; 
            model._startvalue = 0; 
            model._runtime = 0; 
            model._endtype = 2; 
            model._endvalue = 0; 
            model._timespan = 0; 
            model._createtime = new Timestamp(System.currentTimeMillis());

            if (ControllogService.Add(model)) {
                return "{result:'done'}";
            } else {
                return "{result:'error'}";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "{result:'" + ex + "\'}";
        }
    }
    
 // 获取最新数据列表
    public static List<NodeViewModel> GetNodesStateByUserIDs(String userids){
        List<NodeViewModel> objModel = null;
        if (null == objModel){
            try {
                objModel = GetNodeInfoShow(GetLastDataByUserIDs(userids));
                if (null != objModel){
                    
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return objModel;
    }
    
    //line 583
    public static List<NodeModel> GetNodeListByNodenolist(String nodenolist){
        String strwhere = "";
        if (!TextUtils.isEmpty(nodenolist)){
            strwhere = "NodeNo in (" + nodenolist + ")";
        } else {
//            strwhere = "1=1";//bug 有时非angel会显示所有节点
            return new ArrayList<NodeModel>();
        }
        return mNodeSevice.GetModelList(strwhere);
    }
    
    //根据用户ID获取节点编号列表 in line601
    public static String GetNodeNoStringByUserIDs(String userids){
        List<NodeModel> nodelist = DataAPI.GetNodeListByUserIDs(userids);
        String nodenos = "";
        if (nodelist != null){
            for (int i = 0; i < nodelist.size(); i ++){
                nodenos += nodelist.get(i)._nodeno + ",";
            }
            if (nodenos.endsWith(",")){
                nodenos = nodenos.substring(0, nodenos.length()-1);
            }
        }
        return nodenos;
    }
    
    //获取节点视图包含传感器名称和在线离线 in line 678
    public static List<NodeViewModel> GetNodeInfoShow(ResultSet dt){
        List<NodeViewModel> nodeviewlist = new ArrayList<NodeViewModel>();
        if (null != dt){
            try {
                while(dt.next()){
                    NodeViewModel model = new NodeViewModel();
//                    Node node = new Node();
                    model._nodeno = Integer.parseInt(dt.getString("节点编号"));
                    model._location = dt.getString("安装地点");
                    model._updatetime = dt.getTimestamp("更新时间");//TODO
//                    model._updatetime.getTime();
                    Log.v(tag, "---------更新时间:" + model._updatetime);
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    
                    if ((timestamp.getTime() - model._updatetime.getTime()) > 60 * 60 * 1000){//TODO
                        model._isonline = "离线";
                    } else {
                        model._isonline = "在线";
                    }
                    
                    int columnCout = dt.getMetaData().getColumnCount();
                    for (int i = 1; i <= columnCout; i ++){
                        try {
                            String sensorname = dt.getMetaData().getColumnName(i);
                            if ("节点编号".equals(sensorname) ||
                                    "RANK".equals(sensorname) ||
                                    "RANK2".equals(sensorname) ||
                                    "OrderNumber".equals(sensorname) ||
                                    "电池电压".equals(sensorname) ||
                                    "安装地点".equals(sensorname) ||
                                    "更新时间".equals(sensorname) ){
                                continue;
                            }
                            if (!TextUtils.isEmpty(dt.getString(sensorname))){
                                if (!TextUtils.isEmpty(model._sensornames)){
                                    model._sensornames = model._sensornames + "," + sensorname + ":" + dt.getString(sensorname);
                                } else {
                                    model._sensornames = sensorname + ":" + dt.getString(sensorname);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.v(tag, "------NodeViewMode-location:" + model._location + " sensorname:" + model._sensornames);
                    nodeviewlist.add(model);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return nodeviewlist;
    }
    
    // line 757
    public static List<SensorViewModel> GetSensorListByNodeNoString(String nodenos){
        return mSensorSevice.GetSensorListByNodeNoString(nodenos);
    }
    
    //line766 根据传感器名称（ShortName）获取传感器对象
    public static SensorModel GetSensorModelBySensorName(String shortName){
        if (!TextUtils.isEmpty(shortName)){
            try {
                SensorModel sensorModel = mSensorSevice.GetModelList("ShortName='" + shortName + "'").get(0);
                return sensorModel;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
  /// 获取流水数据 in line 885
    /// </summary>
    /// <param name="grainsize">粒度 10分钟,逐小时,逐日,逐月,逐年</param>
    /// <param name="valuetype">查询类型 平均值,最大值,最小值,累计值</param>
    /// <param name="begintime">开始时间 2014/4/8 14:17:30</param>
    /// <param name="endtime">结束时间 2014/4/8 14:17:30</param>
    /// <param name="orderby">排序 更新时间,节点编号,[传感器简称]</param>
    /// <param name="beginindex">开始索引 分页使用</param>
    /// <param name="endindex">  结束索引 分页使用</param>
    /// <param name="sensorlist">传感器列表 传感器对象列表</param>
    /// <param name="unit">是否带单位 true false</param>
    /// <param name="range">区间 默认写 10000</param>
    /// <param name="nodenolist">节点编号列表 逗号相隔</param>
    /// <returns></returns>
    public static ResultSet GetData(String grainsize, String valuetype, String begintime, String endtime, String orderby, int beginindex, int endindex, 
            boolean unit, int range, List<SensorViewModel> sensorlist, String nodenolist) {
        return mLiveDataSevice.GetData(grainsize, valuetype, begintime, endtime, orderby, beginindex, endindex, unit, range, sensorlist, nodenolist);
    }
    
 // 根据节点编号 in line 997
    public static ResultSet GetLastDataByUserIDs(String userid){
        String nodelist = GetNodeNoStringByUserIDs(userid);
        Log.v(tag, "----------GetLastDataByUserIDs-----nodelist:" + nodelist);
        return GetLastData(nodelist);
    }
    
    public static ResultSet GetLastData(String nodenolist){
        return mLatestDataSevice.GetLastData(nodenolist);
    }
    
    //jingbo add this method
    public static ResultSet GetLastData(String orderby, String nodenolist){
        if (TextUtils.isEmpty(nodenolist)){
            return null;
        }
        ResultSet ds = mLatestDataSevice.GetLastData(orderby, nodenolist, -1, -1);
        return ds;
    }
    
    //默认? int startNum = -1, int endNum = -1
    public static ResultSet GetLastData(String orderby, String nodenolist, int startNum, int endNum){
        if (TextUtils.isEmpty(nodenolist)){
            return null;
        }
        ResultSet ds = mLatestDataSevice.GetLastData(orderby, nodenolist, startNum, endNum);
//        DataTable dt = ds.Tables[0];//以下此处不明白
//        IEnumerable<DataRow> query = from o in dt.AsEnumerable()
//                let nodenos = nodenolist.Split(',')
//                from nodeno in nodenos
//                where o.Field<int>("节点编号").ToString() == nodeno
//                select o;
//        ds.Tables.Clear();
//        if (query.Count() == 0){
//            return null;
//        } else {
//            ds.Tables.Add(query.CopyToDataTable<DataRow>());
//        }
        return ds;
    }

    // 根据AreaID获取VLCVideoInfo数据，默认按创建时间倒序
    public static List<VLCVideoInfoModel> GetVLCVideoInfoByAreaIds(String areaids, int pageindex,
            int pagesize, String oderby) {
        oderby = "CreateTime desc";
        String strwhere = "";
        if ("0".equals(areaids)) {
            strwhere = "1=1";
        } else {
            strwhere = "AreaID in (" + areaids + ")";
        }
        return bllVLCvideoinfo.GetVLCByPage(strwhere, oderby, (pageindex - 1) * pagesize + 1,
                pageindex * pagesize);

    }

    public static List<VLCVideoInfoModel> GetVLCVideoMapInfoByAreaIds(String areaids) {
        String strwhere = "";
        if ("0".equals(areaids)) {
            strwhere = "1=1";
        } else {
            strwhere = "AreaID in (" + areaids + ")";
        }
        strwhere += " and Longitude!='null' AND Latitude!='null'";
        return bllVLCvideoinfo.GetVLCmap(strwhere);
    }

    public static List<VLCVideoInfoModel> GetVLCVideoInfoByIDs(String ids) {
        String strwhere = "";
        if (!TextUtils.isEmpty(ids)) {
            strwhere = "VLCVideoInfoID IN(" + ids + ")";
            return bllVLCvideoinfo.GetModelList(strwhere);
        } else {
            return null;
        }
    }
    
    //定时控制接口
    public static String SetTimeControl(String uniqueid, String deviceno, String opentime) {
        ControlLogModel model = new ControlLogModel();
        model._uniqueid = uniqueid.trim();
        model._logtype = 0; 
        model._deviceno = Integer.parseInt(deviceno);
        model._starttype = 1; 
        model._startvalue = 0; 
        model._runtime = Integer.parseInt(opentime) * 60;
        model._endtype = 2; 
        model._endvalue = 0; 
        model._timespan = (int) (System.currentTimeMillis()/1000);
        model._createtime = new Timestamp(System.currentTimeMillis());

        if (ControllogService.Add(model) == true)  {
            return "{result:'done'}";
        } else {
            return "{result:'error'}";
        }
    }
    
    //in line 568 仅仅是字符串处理
    public static String GetNodeNoStringByNodeList(List<NodeModel> nodelist){
        String nodenos = "";
        for (int i = 0; i < nodelist.size(); i++){
            nodenos += nodelist.get(i)._nodeno + ",";
        }
        if (nodenos.endsWith(",")){
            nodenos = nodenos.substring(0, nodenos.length()-1);
        }
        return nodenos;
    }
    
    //in line 376
    public static List<NodeModel> GetNodeListByAreaIDs(String areaids){
        if ("-1".equals(areaids)){
            return null;
        }
        return mNodeSevice.GetModelListByAreaIDs(areaids);
    }
    
    public static List<NodeModel> GetNodeListByUserIDs(String userids) {
        if (userids == "-1") {
            return mNodeSevice.GetModelList("");
        }
        String areaids = GetAreaIDByUserIDs(userids);
        if (areaids == "-1"){
            return null;
        }
        return mNodeSevice.GetModelListByAreaIDs(areaids);
    }
    
    public static List<ControlDeviceViewModel> GetDeviceActionInfo(int controlNodeId ,String nodeUnique){
        List<ControlDeviceViewModel> controlDeviceView_list = new ArrayList<ControlDeviceViewModel>();
        try {
            ResultSet ds = controlDeviceBll.GetControlDeviceInfo(controlNodeId, nodeUnique);
            if (ds != null) {
                while (ds.next()) {
                    ControlDeviceViewModel controlDeviceView = new ControlDeviceViewModel();
                    
                    controlDeviceView.DeviceID = ds.getInt("DeviceID");
                    controlDeviceView.ControlNodeID = ds.getInt("ControlNodeID");
                    controlDeviceView.DeviceNo = ds.getInt("DeviceNo");
                    controlDeviceView.DeviceName = ds.getString("DeviceName");
                    controlDeviceView.RunTime = ds.getInt("RunTime");
                    controlDeviceView.StartTime = ds.getString("StartTime");
                    controlDeviceView.ControlActionID = ds.getInt("ControlActionID");
                    controlDeviceView.ControlType = ds.getInt("ControlType");
                    controlDeviceView.CollectUniqueIDs = ds.getString("CollectUniqueIDs");
                    controlDeviceView.ControlCondition = ds.getString("ControlCondition");
                    controlDeviceView.OperateTime = ds.getTimestamp("OperateTime");
                    controlDeviceView.StateNow = ds.getInt("StateNow");
                    controlDeviceView.Operate = ds.getString("Operate");
                    
                    controlDeviceView_list.add(controlDeviceView);
                }
                return controlDeviceView_list;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return controlDeviceView_list;
    }
}
