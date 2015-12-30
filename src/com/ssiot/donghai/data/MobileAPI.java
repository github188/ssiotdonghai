package com.ssiot.donghai.data;

import android.text.TextUtils;
import android.util.Log;

import com.ssiot.donghai.data.business.SensorModifyData;
import com.ssiot.donghai.data.model.ControlNodeModel;
import com.ssiot.donghai.data.model.NodeModel;
import com.ssiot.donghai.data.model.SensorModel;
import com.ssiot.donghai.data.model.SensorModifyDataModel;
import com.ssiot.donghai.data.model.SettingModel;
import com.ssiot.donghai.data.model.view.ControlNodeViewModel;
import com.ssiot.donghai.data.model.view.NodeData;
import com.ssiot.donghai.data.model.view.NodeView2Model;
import com.ssiot.donghai.data.model.view.SensorViewModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MobileAPI{
    private static final String tag = "MobileAPI";
    public static SensorModifyData sensorModifyDataBll = new SensorModifyData();
    public static com.ssiot.donghai.data.business.Setting settingBll = new com.ssiot.donghai.data.business.Setting();
    
    public static List<NodeView2Model> GetNodesInfoAndDataByNodenos(String nodenos){
        List<NodeView2Model> rtn_nodeViewList = new ArrayList<NodeView2Model>();
        try {
            List<SensorViewModel> sensor_list = DataAPI.GetSensorListByNodeNoString(nodenos);
            List<NodeModel> node_list = DataAPI.GetNodeListByNodenolist(nodenos);
            
            ResultSet last_ds = DataAPI.GetLastData(nodenos);
            List<NodeView2Model> nodeView_list1 = new ArrayList<NodeView2Model>();//存储在线
            List<NodeView2Model> nodeView_list2 = new ArrayList<NodeView2Model>();//离线
            while(last_ds.next()){
                NodeView2Model nodeView2 = new NodeView2Model();
                String isOnline = "";
                nodeView2._nodeno = Integer.parseInt(last_ds.getString("节点编号"));
                for(NodeModel nm : node_list){
                    if (nm._nodeno == nodeView2._nodeno){
                        nodeView2._uniqueid = nm._uniqueid;
                        nodeView2._onlinetype = nm._onlinetype;
                        nodeView2._image = nm._image;
                    } else {
                        continue;
                    }
                }
                nodeView2._location = last_ds.getString("安装地点");
                nodeView2._updatetime = last_ds.getTimestamp("更新时间");
                Timestamp now = new Timestamp(System.currentTimeMillis());
                if ((now.getTime() - nodeView2._updatetime.getTime()) > 60 * 60 * 1000){
                    nodeView2._isonline = "离线";
                    isOnline = "离线";
                } else {
                    nodeView2._isonline = "在线";
                    isOnline = "在线";
                }
                
                List<NodeData> nodeData_list = new ArrayList<NodeData>();
                int columnCout = last_ds.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCout; i ++){
                    try {
                        String sensorname = last_ds.getMetaData().getColumnName(i);
                        if ("节点编号".equals(sensorname) ||
                                "RANK".equals(sensorname) ||
                                "RANK2".equals(sensorname) ||
                                "OrderNumber".equals(sensorname) ||
                                "电池电压".equals(sensorname) ||
                                "安装地点".equals(sensorname) ||
                                "更新时间".equals(sensorname) ||
                                "经度".equals(sensorname) ||
                                "纬度".equals(sensorname) ){
                            continue;
                        }
                        if (!TextUtils.isEmpty(last_ds.getString(sensorname))){
                            NodeData nodeData = new NodeData();
                            nodeData._name = sensorname;
                            String value_str = last_ds.getString(sensorname);//例如 4.3mg  ，温度4 无 表示没有
//                            Log.v(tag, "----------nodedata-:"+sensorname + value_str + (value_str == null));
                            if (null == value_str || "无".equals(value_str)){
                                continue;
                            }
                            String dataString = getNumberPartFromStr(value_str);
                            nodeData._data = Float.parseFloat(dataString);
                            nodeData._unit = value_str.substring(dataString.length(), value_str.length());
                            for (SensorViewModel sensor: sensor_list){
                                if (sensorname.contains(sensor._shortname)){
                                    nodeData._proportion = nodeData._data / sensor._maxvalue;//占比率 
                                    break;
                                } else {
                                    continue;
                                }
                            }
                            if (nodeView2._isonline.equals("离线")){
                                nodeData._compare = "无";
                            }
                            nodeData_list.add(nodeData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                nodeView2._nodeData_list = nodeData_list;
                if ("在线".equals(isOnline)){
                    nodeView_list1.add(nodeView2);
                } else {
                    nodeView_list2.add(nodeView2);
                }
            }
            last_ds.close();

            
            List<NodeView2Model> onlineDataView_list = nodeView_list1;//dic_nodeView2["在线"];
            String onlineNodenos = "";
            if (onlineDataView_list.size() > 0){
                Timestamp minDateTime = onlineDataView_list.get(0)._updatetime;
                for(NodeView2Model nodeView2 : onlineDataView_list){
                    onlineNodenos += nodeView2._nodeno + ",";
                    if (nodeView2._updatetime.getTime() < minDateTime.getTime()) {
                        minDateTime = nodeView2._updatetime;//jingbo 在线节点中 最小的那个更新时间
                    }
                }
                if (onlineNodenos.endsWith(",")){
                    onlineNodenos = onlineNodenos.substring(0, onlineNodenos.length()-1);
                }
                String beginTime = ConvertToSpecificDateTimeString(minDateTime, -12, "yyyy-MM-dd HH:mm:ss");
                String endTime = ConvertToSpecificDateTimeString(minDateTime, -8, "yyyy-MM-dd HH:mm:ss");
              //获取对在线节点延迟十分钟的数据
                ResultSet ds = DataAPI.GetData("10分钟", "平均值", beginTime, endTime, "更新时间 DESC", -1, -1, true, 10000, null, onlineNodenos);
                
                List<NodeView2Model> onlineNodeView2_list = new ArrayList<NodeView2Model>();//用于存储延迟十分钟的在线节点数据
                if (ds!=null){
                    while(ds.next()){
                        NodeView2Model nodeView2 = new NodeView2Model();
                        nodeView2._nodeno = Integer.parseInt(ds.getString("节点编号"));
                        List<NodeData> nodeData_list = new ArrayList<NodeData>();
                        int columnCouts = ds.getMetaData().getColumnCount();
                        for (int i = 1; i <=columnCouts; i ++){
                            String sensorname = ds.getMetaData().getColumnName(i);
                            if ("节点编号".equals(sensorname) ||
                                "RANK".equals(sensorname) ||
                                "RANK2".equals(sensorname) ||
                                "OrderNumber".equals(sensorname) ||
                                "电池电压".equals(sensorname) ||
                                "安装地点".equals(sensorname) ||
                                "更新时间".equals(sensorname))
                                continue;
                            
                            String value_strtmp = ds.getString(sensorname);
                            if (value_strtmp != null && !TextUtils.isEmpty(value_strtmp))
                            {
                                NodeData nodeData = new NodeData();
                                nodeData._name = sensorname;
                                String value_str = value_strtmp.trim();
//                                String regexp = @"-?\d+\.?\d*";
//                                nodeData.Data = float.Parse(Regex.Match(value_str, regexp).ToString());
//                                nodeData.Unit = Regex.Match(value_str, @"[^-?\d+\.?\d*]+").ToString();
                                String dataStr = getNumberPartFromStr(value_str);
                                nodeData._data = Float.parseFloat(dataStr);
                                nodeData._unit = value_str.substring(dataStr.length(), value_str.length());
                                nodeData_list.add(nodeData);
                            }
                        }
                        nodeView2._nodeData_list = nodeData_list;
                        onlineNodeView2_list.add(nodeView2);
                    }
                    ds.close();
                }
                
              //将在线数据进行比较，并存储到新的List中，用于最终返回数
                for (NodeView2Model nodeView1 : onlineDataView_list){
                    NodeView2Model nodeView2 = new NodeView2Model();
                    nodeView2._nodeid = nodeView1._nodeid;
                    nodeView2._nodeno = nodeView1._nodeno;
                    nodeView2._onlinetype = nodeView1._onlinetype;
                    nodeView2._productid = nodeView1._productid;
                    nodeView2._remark = nodeView1._remark;
                    nodeView2._uniqueid = nodeView1._uniqueid;
                    nodeView2._updatetime = nodeView1._updatetime;
                    nodeView2._isonline = nodeView1._isonline;
                    nodeView2._image = nodeView1._image;
                    nodeView2._areaid = nodeView1._areaid;
                    nodeView2._location = nodeView1._location;
                    List<NodeData> nodeData_list = new ArrayList<NodeData>();
                    //判断某节点的传感器值与十分钟之前相比是上长升还是下降
                    if (onlineNodeView2_list.size() > 0){
                        for (NodeView2Model nodeView10 : onlineNodeView2_list){
//                            Log.v(tag, "--------10min数据nodeno：" + nodeView10._nodeno + " 在线nodeno:" + nodeView1._nodeno);
                            if (nodeView10._nodeno == nodeView1._nodeno){
                                for (NodeData nodedata: nodeView1._nodeData_list){
                                    NodeData tmp_nodeData = new NodeData();
                                    tmp_nodeData._name = nodedata._name;
                                    tmp_nodeData._data = nodedata._data;
                                    tmp_nodeData._unit = nodedata._unit;
                                    tmp_nodeData._proportion = nodedata._proportion;
                                    for (NodeData nodedata10 : nodeView10._nodeData_list){
//                                        Log.v(tag, "---------------compare:" + nodedata._name + nodedata10._name);
                                        if (nodedata._name.equals(nodedata10._name)){//
                                            if (nodedata._data > nodedata10._data){
                                                tmp_nodeData._compare = "上升";
                                            }else if (nodedata._data == nodedata10._data){
                                                tmp_nodeData._compare = "相等";
                                            } else {
                                                tmp_nodeData._compare = "下降";
                                            }
                                            break;
                                        }
                                    }
                                    nodeData_list.add(tmp_nodeData);
                                }
                                break;
                            }
                        }
                        if (nodeData_list.size() <= 0){
                            Log.v(tag, "----no data in last 10 min");
                            nodeData_list = nodeView1._nodeData_list;//如果十分钟前无数据，则显示当前数据
                        }
                    } else {
                        nodeData_list = nodeView1._nodeData_list;
                    }
                    
                    nodeView2._nodeData_list = nodeData_list;
                    rtn_nodeViewList.add(nodeView2);
                }
            }
            
            List<NodeView2Model> outlineDataView_list = nodeView_list2;//dic_nodeView2["离线"];
            if (outlineDataView_list.size() > 0) {
                for (NodeView2Model outlineDataView : outlineDataView_list){
                    rtn_nodeViewList.add(outlineDataView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtn_nodeViewList;    
    }
    
    //根据由","号分隔的节点编号字符串和查询方式获取节点数据
    public static List<NodeView2Model> GetNodesDataByNodenosAndQueryType(String nodenos, String grainsize, String queryType, 
            String beginTime, String endTime){
        int range = 10000;
        List<NodeModel> node_list = DataAPI.GetNodeListByNodenolist(nodenos);
        List<NodeView2Model> nodeView2_list = new ArrayList<NodeView2Model>();
        
        ResultSet ds = DataAPI.GetData(grainsize, queryType, beginTime, endTime, "更新时间 DESC", -1, -1, true, range, null, nodenos);
        if (ds != null){
            try {
                while(ds.next()){
                    NodeView2Model nodeView2 = new NodeView2Model();
                    String isOnline = "";
                    nodeView2._nodeno = Integer.parseInt(ds.getString("节点编号"));
                    for (NodeModel node : node_list){
                        if (node._nodeno == nodeView2._nodeno){
                            nodeView2._uniqueid = node._uniqueid;
                            nodeView2._onlinetype = node._onlinetype;
                            nodeView2._image = node._image;
                            break;
                        } else {
                            continue;
                        }
                    }
                    nodeView2._location = ds.getString("安装地点");
                    String timeStr =  ds.getString("更新时间");
                    if (null != timeStr){
                        try {
                            switch (timeStr.length()) {
                                case 13://逐小时lenth13
                                    nodeView2._updatetime = Timestamp.valueOf(timeStr + ":00:00");
                                    break;
                                case 16://10分钟
                                    nodeView2._updatetime = Timestamp.valueOf(timeStr + ":00");
                                    break;
                                case 10://日
                                    nodeView2._updatetime = Timestamp.valueOf(timeStr + " 00:00:00");
                                    break;
                                case 7://
                                    nodeView2._updatetime = Timestamp.valueOf(timeStr + "-01 00:00:00");
                                    break;
                                case 4:
                                    nodeView2._updatetime = Timestamp.valueOf(timeStr + "-01-01 00:00:00");
                                    break;
                                    
                                default:
                                    nodeView2._updatetime = Timestamp.valueOf(timeStr);
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        nodeView2._detailTime = timeStr;
                    }
                    
//                    if (null != timeStr && timeStr.length() == 13)  {
//                        nodeView2._updatetime = Timestamp.valueOf(timeStr + ":00:00");//TODO 这个格式不规范
//                    } else if (null != timeStr) {
//                        nodeView2._updatetime = Timestamp.valueOf(timeStr);
//                    }
                    if (System.currentTimeMillis() - nodeView2._updatetime.getTime() > 3600 * 1000){
                        nodeView2._isonline = "离线";
                        isOnline = "离线";
                    } else {
                        nodeView2._isonline = "在线";
                        isOnline = "在线";
                    }
                    nodeView2._nodeData_list = buildNodeDataListFromResultSet(ds);
                    nodeView2_list.add(nodeView2);
                }
                ds.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return nodeView2_list;
    }
    
    public static List<ControlNodeViewModel> GetControlNodesInfoByAreaIds(String areaids) {
        List<ControlNodeViewModel> controlNodeView_list = new ArrayList<ControlNodeViewModel>();
        try {
            ResultSet ds = DataAPI.mControlNodeService.GetControlNodesExtendInfoByAreaIds(areaids);
            if (ds != null) {
                while (ds.next()) {
                    controlNodeView_list.add( DataRowToObjectModel(ds));
                }
                ds.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return controlNodeView_list;
    }
    
    //根据传感器的名称（ShortName）和修正类型（Type）加载传感器修正数据//现只用于标定
    public static List<SensorModifyDataModel> GetSensorModifyDataListBySensorNameAndType(String name, int type){
        List<SensorModifyDataModel> sensorModifyDataList = new ArrayList<SensorModifyDataModel>();
        try {
            if (!TextUtils.isEmpty(name) && type >= 0){
                SensorModel sensorModel = DataAPI.GetSensorModelBySensorName(name);
                if (sensorModel._sensorno != 0){
                    sensorModifyDataList = sensorModifyDataBll.GetModelList("SensorNo=" + sensorModel._sensorno + " and Type=" + type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sensorModifyDataList;
    }
    
    public static SettingModel GetJiaoZhunBySensorNameAndType(String uniqueId, int type, int sensorNo, int channel){
        int sendState = 2;//jingbo 默认的参数
        try {
            if (TextUtils.isEmpty(uniqueId) && sensorNo > 0 && type > 0){
                SettingModel  settingModel = settingBll.GetSettigModel("UniqueID='" + uniqueId + "' and Type=" + type + 
                        " and SettingMark=" + sensorNo + " and Chanel=" + channel + " and SendState=" + sendState + " order by ID desc");
                return settingModel;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //将日期转化为整分钟的时间格式
 // <param name="time">日期时间</param>
 // <param name="minis">加减分钟数</param>
 // <param name="strFormat">格式</param>
    public static String ConvertToSpecificDateTimeString(Timestamp time, int minis, String strFormat) {
        int tmp_minute = time.getMinutes();
//        Timestamp tmp_time = time.addMinutes(0 - tmp_minute % 10);
//        String timeStr = tmp_time.AddMinutes(minis).ToString(strFormat);
        java.util.Date tmp_date = new Date(time.getTime()- (tmp_minute%10)*60000 + minis * 60000);
        Log.v(tag, "---------ConvertToSpecificDateTimeString--day:"+time.getDate() +" hours:"+time.getHours()+ tmp_minute + " timelong:" + time.getTime() + 
                " /////tmp_date:" + tmp_date.getDate() +tmp_date.getHours());
        SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
        String timeStr = formatter.format(tmp_date);
        return timeStr;

    }
    
    private static List<NodeData> buildNodeDataListFromResultSet(ResultSet ds){
        List<NodeData> nodeData_list = new ArrayList<NodeData>();
        try {
            int columnCouts = ds.getMetaData().getColumnCount();
            for (int i = 1; i <=columnCouts; i ++){
                String sensorname = ds.getMetaData().getColumnName(i);
                if ("节点编号".equals(sensorname) ||
                    "RANK".equals(sensorname) ||
                    "RANK2".equals(sensorname) ||
                    "OrderNumber".equals(sensorname) ||
                    "电池电压".equals(sensorname) ||
                    "安装地点".equals(sensorname) ||
                    "更新时间".equals(sensorname))
                    continue;
                
                String value_strtmp = ds.getString(sensorname);
                if (!TextUtils.isEmpty(value_strtmp)) {
                    NodeData nodeData = new NodeData();
                    nodeData._name = sensorname;
                    String value_str = value_strtmp.trim();
                    String dataStr = getNumberPartFromStr(value_str);
                    nodeData._data = Float.parseFloat(dataStr);
                    nodeData._unit = value_str.substring(dataStr.length(), value_str.length());
                    nodeData_list.add(nodeData);
                } else {//jingbo add this 空的也添加进去，在数据表里空的也需要显示 MoniDataFrag
                    NodeData nodeData = new NodeData();
                    nodeData._name = sensorname;
                    nodeData._unit = null;
                    nodeData_list.add(nodeData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return nodeData_list;
    }
    
    private static String getNumberPartFromStr(String value_str){
        String dataStr = "";
        for (int j = 0; j < value_str.length(); j ++){
            if ((value_str.charAt(j) >= '0' && value_str.charAt(j) <= '9')
                    || value_str.charAt(j) == '.' || value_str.charAt(j) == '-'){
                dataStr += value_str.charAt(j);
            } else {
                break;
            }
        }
        if (TextUtils.isEmpty(dataStr)){
            dataStr = "0";//jingbo set this default
        }
        return dataStr;
    }
    
    public static ControlNodeViewModel DataRowToObjectModel(ResultSet c){//jingbo modified this
        ControlNodeViewModel m = new ControlNodeViewModel();
        try {
            m._id = Integer.parseInt(c.getString("ID"));
            m._uniqueid = c.getString("UniqueID");
//            m._nodename = c.getString("Remark");//NodeName
            m._nodeno = c.getInt("NodeNo");
            m._remark = c.getString("Remark");
            m._areaid = c.getInt("AreaID");
//            m._x = c.getString("X");
//            m._y = c.getString("Y");
          //TODO
            m._nodename = c.getString("Installation");//这个名字！！！！！！！！！！！！！！！！ TODO
            m._image = c.getString("Image");
            //TODO
            m._devicecount = c.getInt("DeviceCount");
            return m;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}