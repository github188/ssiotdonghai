package com.ssiot.donghai.data.business;

import android.text.TextUtils;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.ControlDeviceModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ControlDevice{
    
    public List<ControlDeviceModel> GetModelList(String strWhere){
        ResultSet ds = GetList_dataaccess(strWhere);
        return DataTableToList(ds);
    }
    
    public ResultSet GetControlDeviceInfo(int controlNodeId, String nodeUnique) {
        try {
            if(controlNodeId <= 0 || TextUtils.isEmpty(nodeUnique)) {
                throw new Exception(" Parameters Exception");
            }
            return GetControlDeviceInfo_dataaccess(controlNodeId, nodeUnique);

        }catch (Exception e){
            e.printStackTrace();
            
        }
        return null;
    }
    
    public List<ControlDeviceModel> DataTableToList(ResultSet dt) 
    {
        List<ControlDeviceModel> modelList = new ArrayList<ControlDeviceModel>();
        ControlDeviceModel m = new ControlDeviceModel();
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
    
    private ControlDeviceModel DataRowToModel(ResultSet c){
        ControlDeviceModel m = new ControlDeviceModel();
        try {
            m._id = Integer.parseInt(c.getString("ID"));
            ///////////////////////////////////////////////////////TODO
            m._controlnodeid = c.getInt("ControlNodeID");
            m._deviceno = c.getInt("DeviceNo");
            m._devicename = c.getString("DeviceName");
            m._devicetype = c.getString("DeviceType");  
            m._address = c.getString("Address");
            m._remark = c.getString("Remark");
            m._extern = c.getString("Extern");
            m._createtime = c.getTimestamp("CreateTime");
            m. _state = c.getString("State");
            return m;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    //-----------------------------
    
    
    public ResultSet GetList_dataaccess(String strWhere) {
        StringBuilder strSql=new StringBuilder();
        strSql.append("select ID,ControlNodeID,DeviceNo,DeviceName,DeviceType,Address,Remark,Extern,CreateTime,State ");
        strSql.append(" FROM ControlDevice ");
        if(strWhere.trim()!="") {
            strSql.append(" where "+strWhere);
        }
        return DbHelperSQL.Query(strSql.toString());
    }
    
    //line459
    public  ResultSet GetControlDeviceInfo_dataaccess(int controlNodeId,String nodeUnique) {
        try {
            StringBuilder strSql = new StringBuilder();
            strSql.append("SELECT Tbl1.ID AS [DeviceID],Tbl1.ControlNodeID,Tbl1.DeviceNo,Tbl1.deviceName AS [DeviceName],CL.RunTime, CL.EditTime AS [StartTime],Tbl2.ID AS[ControlActionID],Tbl2.ControlType,Tbl2.CollectUniqueIDs,Tbl2.ControlCondition,Tbl2.OperateTime,Tbl2.StateNow,Tbl2.Operate FROM (SELECT ID,ControlNodeID,DeviceNo,deviceName FROM dbo.ControlDevice ");
            strSql.append("WHERE ControlNodeID=? ) Tbl1 ");
            strSql.append("LEFT JOIN ( SELECT  DeviceNo , RunTime ,  EditTime  FROM ( SELECT  DeviceNo , RunTime , EditTime , ROW_NUMBER() OVER ( PARTITION BY UniqueID, DeviceNo ORDER BY EditTime DESC ) AS rowNumber FROM ControlLog WHERE UniqueID =? AND DeviceNo IN ( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 ) ) TC WHERE rowNumber = 1 ) AS CL ON Tbl1.DeviceNo = CL.DeviceNo ");
            strSql.append("LEFT JOIN (SELECT  ID,DeviceNo,ControlType,CollectUniqueIDs,ControlCondition,OperateTime,StateNow,Operate FROM dbo.ControlActionInfo  ");
            strSql.append("WHERE UniqueID=?)Tbl2 ON Tbl1.DeviceNo=Tbl2.DeviceNo");
//            SqlParameter[] parameters = { new SqlParameter("@ControlNodeID", SqlDbType.Int, 4), new SqlParameter("@UniqueID", SqlDbType.Char, 8) };
//            parameters[0].Value = controlNodeId;
//            parameters[1].Value = nodeUnique;
            ArrayList<String> parameters = new ArrayList<String>();
            parameters.add(""+controlNodeId);
            parameters.add(nodeUnique);
            parameters.add(nodeUnique);
            return DbHelperSQL.Query(strSql.toString(), parameters);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}