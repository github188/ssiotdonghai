package com.ssiot.donghai.data.business;

import android.text.TextUtils;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.ControlLogModel;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ControlLog{
    
    public boolean Add(ControlLogModel model){
        StringBuilder strSql = new StringBuilder();
        strSql.append("insert into ControlLog(");
        strSql.append("LogType,UniqueID,DeviceNo,StartType,StartValue,RunTime,EndType,EndValue,SendState,CreateTime,EditTime,TimeSpan)");
        strSql.append(" values (");
        strSql.append("?,?,?,?,?,?,?,?,?,?,?,?)");
        strSql.append(";select @@IDENTITY");
//        SqlParameter[] parameters = { 
//                new SqlParameter("@LogType", SqlDbType.TinyInt,1),
//                new SqlParameter("@UniqueID", SqlDbType.Char,8),
//                new SqlParameter("@DeviceNo", SqlDbType.TinyInt,1),
//                new SqlParameter("@StartType", SqlDbType.TinyInt,1),
//                new SqlParameter("@StartValue", SqlDbType.Int,4),
//                new SqlParameter("@RunTime", SqlDbType.Int,4),
//                new SqlParameter("@EndType", SqlDbType.TinyInt,1),
//                new SqlParameter("@EndValue", SqlDbType.Int,4),
//                new SqlParameter("@SendState", SqlDbType.Int,4),
//                new SqlParameter("@CreateTime", SqlDbType.SmallDateTime),
//                new SqlParameter("@EditTime", SqlDbType.SmallDateTime),
//                new SqlParameter("@TimeSpan", SqlDbType.Int,4)};
//        parameters[0].Value = model.LogType;
//        parameters[1].Value = model.UniqueID;
//        parameters[2].Value = model.DeviceNo;
//        parameters[3].Value = model.StartType;
//        parameters[4].Value = model.StartValue;
//        parameters[5].Value = model.RunTime;
//        parameters[6].Value = model.EndType;
//        parameters[7].Value = model.EndValue;
//        parameters[8].Value = model.SendState;
//        parameters[9].Value = model.CreateTime;
//        parameters[10].Value = model.EditTime;
//        parameters[11].Value = model.TimeSpan;
        ArrayList<Object> parameters = new ArrayList<Object>();
        parameters.add(model._logtype);
        parameters.add(model._uniqueid);
        parameters.add(model._deviceno);
        parameters.add(model._starttype);
        parameters.add(model._startvalue);
        parameters.add(model._runtime);
        parameters.add(model._endtype);
        parameters.add(model._endvalue);
        parameters.add(model._sendstate);
        parameters.add(model._createtime);
        parameters.add(model._edittime);
        parameters.add(model._timespan);

        return DbHelperSQL.Update_object(strSql.toString(), parameters) > 0;//GetSingle
//        if (obj == null)
//        {
//            return 0;
//        }
//        else
//        {
//            return Convert.ToInt32(obj);
//        }
    }
    
    public int AddManyCount(List<ControlLogModel> controlLogList){
        if(controlLogList!=null&&controlLogList.size()>0){
            return AddManyCount_dataaccess(controlLogList);
        } else {
            return 0;
        }
    }
    
    //获取最新控制设备的状态信息,,,,控制日志类型默认是0 int logType = 0
    public ControlLogModel GetLatestData(String nodeUnique, int deviceNo, int controlType) {
        try
        {
            if(!TextUtils.isEmpty(nodeUnique) && deviceNo>0 && controlType>0) {
                return GetLatestData_dataaccess(nodeUnique, deviceNo, controlType, 0);
            } else {
                throw new Exception(" Some Parameters is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //------------------------
    
    public int AddManyCount_dataaccess(List<ControlLogModel> controlLogList){
        StringBuilder strSql = new StringBuilder();
        strSql.append("insert into ControlLog(");
        strSql.append("LogType,UniqueID,DeviceNo,StartType,StartValue,RunTime,EndType,EndValue,SendState,CreateTime,EditTime,TimeSpan)");
        String sqlValues = ""; 
        try {
            for (ControlLogModel controlLog : controlLogList) {
                sqlValues += " select " + controlLog._logtype + ",'" + controlLog._uniqueid + "'," + controlLog._deviceno + "," + controlLog._starttype + "," 
            + controlLog._startvalue + "," + controlLog._runtime + "," + controlLog._endtype + "," + controlLog._endvalue + "," + controlLog._sendstate + ",'" 
                        + controlLog._createtime + "','" + controlLog._edittime + "'," + controlLog._timespan + " UNION ALL";
            }
            sqlValues = sqlValues.substring(0, sqlValues.length() - 10) + ";";
            strSql.append(sqlValues);
            return DbHelperSQL.Update(strSql.toString());
        } catch (Exception e) {
            return 0;
        }
    }
    
    private ControlLogModel GetLatestData_dataaccess(String nodeUnique,int deviceNo,int controlType,int logType) {
        try {
            StringBuilder strSql = new StringBuilder();
            strSql.append("SELECT  TOP 1 * FROM ControlLog ");
            strSql.append("WHERE LogType=? and ");
            strSql.append(" UniqueID=? AND ");
            strSql.append("DeviceNo=? AND ");
            strSql.append("StartType=? ");
            strSql.append("ORDER BY ID DESC;");
            ArrayList<String> parameters = new ArrayList<String>();
            parameters.add(""+logType);
            parameters.add(""+nodeUnique);
            parameters.add(""+deviceNo);
            parameters.add(""+controlType);
            ResultSet ds = DbHelperSQL.Query(strSql.toString(), parameters);
           return  DataRowToModel(ds);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    private ControlLogModel DataRowToModel(ResultSet s){
        ControlLogModel m = new ControlLogModel();
        try {
            m._id = s.getInt("ID");
            m._logtype = s.getInt("LogType");
            m._uniqueid = s.getString("UniqueID");
            m._uniqueid = s.getString("UniqueID");
            m._deviceno = s.getInt("DeviceNo");
            m._starttype = s.getInt("StartType");
            m._startvalue = s.getInt("StartValue");
            m._runtime = s.getInt("RunTime");
            m._endtype = s.getInt("EndType");
            m._endvalue = s.getInt("EndValue");
            m._sendstate = s.getInt("SendState");
            m._createtime = s.getTimestamp("CreateTime");
            m._edittime = s.getTimestamp("EditTime");
            m._timespan = s.getInt("TimeSpan");
            return m;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}