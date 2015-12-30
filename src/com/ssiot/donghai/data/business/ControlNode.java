package com.ssiot.donghai.data.business;

import android.text.TextUtils;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.AreaModel;
import com.ssiot.donghai.data.model.ControlNodeModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ControlNode{
    
    //获取控制设备的扩展信息，包括某个控制节点下的设备数量
    public ResultSet GetControlNodesExtendInfoByAreaIds(String areaids) {
        if (!TextUtils.isEmpty(areaids)) {
            return GetControlNodesExtendInfoByAreaIds_dataaccess(areaids);
        } else {
            return null;
        }
    }
    
    public List<ControlNodeModel> GetModelList(String strWhere)
    {
        ResultSet ds = GetList_dataaccess(strWhere);
        return DataTableToList(ds);
    }
    
    //----------------------------------------------------
    public ResultSet GetControlNodesExtendInfoByAreaIds_dataaccess(String areaids) {
        String whereAreaIDStr = "AreaID in ("+areaids+")";
        if ("0".equals(areaids)){//管理员 by jingbo
            whereAreaIDStr = " 1=1 ";
        } else {
            whereAreaIDStr = "AreaID in ("+areaids+")";
        }
        
        String sqlstr = "select CN.ID,CN.UniqueID,CN.NodeNo,CN.Remark,CN.AreaID,CN.Installation,CN.Color,CN.Longitude,CN.Latitude,CN.Image ,DC.DeviceCount from ( select * from ControlNode where "+whereAreaIDStr+")as CN left join ( select Count(DeviceNo) as DeviceCount, ControlNodeID from ControlDevice where ControlNodeID in (select ID from ControlNode where "+whereAreaIDStr+") group by ControlNodeID)as DC on CN.ID=DC.ControlNodeID";
        ResultSet ds=DbHelperSQL.Query(sqlstr);
        return ds; 
    }
    
    public ResultSet GetList_dataaccess(String strWhere) {
        StringBuilder strSql = new StringBuilder();
        strSql.append("select ID,UniqueID,NodeName,NodeNo,Remark,AreaID,X,Y,Longitude,Latitude,Extern,Installation,Color,Image ");
        strSql.append(" FROM ControlNode ");
        if (!TextUtils.isEmpty(strWhere.trim())) {
            strSql.append(" where " + strWhere);
        }
        return DbHelperSQL.Query(strSql.toString());
    }
    
    public List<ControlNodeModel> DataTableToList(ResultSet dt) 
    {
        List<ControlNodeModel> modelList = new ArrayList<ControlNodeModel>();
        ControlNodeModel m = new ControlNodeModel();
        try {
            while(dt.next()){
                m = DataRowToModel(dt);
                if (m != null){
                    modelList.add(m);
                }
            }
            dt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelList;
    }
    
    private ControlNodeModel DataRowToModel(ResultSet c){
        ControlNodeModel m = new ControlNodeModel();
        try {
            m._id = Integer.parseInt(c.getString("ID"));
            m._uniqueid = c.getString("UniqueID");
            m._nodename = c.getString("NodeName");
            m._nodeno = c.getInt("NodeNo");
            m._remark = c.getString("Remark");
            m._areaid = c.getInt("AreaID");
            m._x = c.getString("X");
            m._y = c.getString("Y");
          //TODO
            m._image = c.getString("Image");
            //TODO
            return m;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}