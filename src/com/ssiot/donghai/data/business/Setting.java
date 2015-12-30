package com.ssiot.donghai.data.business;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.SettingModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Setting{
    
    public SettingModel GetSettigModel(String sqlStr){
        return GetSettingModel_dataaccess(sqlStr);
    }
    
    public int Add(SettingModel model){
        StringBuilder strSql = new StringBuilder();
        strSql.append("insert into Setting(");
        strSql.append("UniqueID,Type,SettingMark,Chanel,Other,Value,TimeSpan,SendTime,SendState,ReSendCount)");
        strSql.append(" values (");
        strSql.append("?,?,?,?,?,?,?,?,?,?)");
        strSql.append(";select @@IDENTITY");
        
        ArrayList<Object> paraArray = new ArrayList<Object>();
        paraArray.add(model._uniqueid);
        paraArray.add(model._type);
        paraArray.add(model._settingmark);
        paraArray.add(model._chanel);
        paraArray.add(model._other);
        paraArray.add(model._value);
        paraArray.add(model._timespan);
        paraArray.add(model._sendtime);
        paraArray.add(model._sendstate);
        paraArray.add(model._resendcount);
        return DbHelperSQL.Update_object(strSql.toString(), paraArray);
    }
    
    public boolean Exists(String uniqueId,int type,int SettingMark,int Channel) {
        StringBuilder strSql = new StringBuilder();
        strSql.append("select count(1) from Setting");
        strSql.append(" where UniqueID=@UniqueID and Type=@Type and SettingMark=@SettingMark and Chanel=@Chanel");
//        SqlParameter[] parameters = { new SqlParameter("@UniqueID",SqlDbType.Char,8),new SqlParameter("@Type",SqlDbType.Int,4),new SqlParameter("@SettingMark",SqlDbType.Int,4),new SqlParameter("@Chanel",SqlDbType.TinyInt,1)};
//        parameters[0].Value = uniqueId;
//        parameters[1].Value = type;
//        parameters[2].Value = SettingMark;
//        parameters[3].Value = Channel;
        ArrayList<String> paraArray = new ArrayList<String>();
        paraArray.add(uniqueId);
        paraArray.add(""+type);
        paraArray.add(""+SettingMark);
        paraArray.add(""+Channel);//TODO int变string是否有问题
        return DbHelperSQL.Exists_a(strSql.toString(), paraArray);
    }
    
    public boolean Update(SettingModel model){
        StringBuilder strSql = new StringBuilder();
        strSql.append("update Setting set ");
        strSql.append("UniqueID=?,");
        strSql.append("Type=?,");
        strSql.append("SettingMark=?,");
        strSql.append("Chanel=?,");
        strSql.append("Other=?,");
        strSql.append("Value=?,");
        strSql.append("TimeSpan=?,");
        strSql.append("SendTime=?,");
        strSql.append("SendState=?,");
        strSql.append("ReSendCount=?");
        strSql.append(" where ID=?");
        ArrayList<Object> paraArray = new ArrayList<Object>();
        paraArray.add(model._uniqueid);
        paraArray.add(model._type);
        paraArray.add(model._settingmark);
        paraArray.add(model._chanel);
        paraArray.add(model._other);
        paraArray.add(model._value);
        paraArray.add(model._timespan);
        paraArray.add(model._sendtime);
        paraArray.add(model._sendstate);
        paraArray.add(model._resendcount);
        paraArray.add(model._id);
//        SqlParameter[] parameters = { 
//                new SqlParameter("@UniqueID", SqlDbType.Char,8),
//                new SqlParameter("@Type", SqlDbType.Int,4),
//                new SqlParameter("@SettingMark", SqlDbType.Int,4),
//                new SqlParameter("@Chanel", SqlDbType.TinyInt,1),
//                new SqlParameter("@Other", SqlDbType.TinyInt,1),
//                new SqlParameter("@Value", SqlDbType.Float,8),
//                new SqlParameter("@TimeSpan", SqlDbType.Int,4),
//                new SqlParameter("@SendTime", SqlDbType.DateTime),
//                new SqlParameter("@SendState", SqlDbType.Int,4),
//                new SqlParameter("@ReSendCount", SqlDbType.Int,4),
//                new SqlParameter("@ID", SqlDbType.Int,4)};
//        parameters[0].Value = model.UniqueID;
//        parameters[1].Value = model.Type;
//        parameters[2].Value = model.SettingMark;
//        parameters[3].Value = model.Chanel;
//        parameters[4].Value = model.Other;
//        parameters[5].Value = model.Value;
//        parameters[6].Value = model.TimeSpan;
//        parameters[7].Value = model.SendTime;
//        parameters[8].Value = model.SendState;
//        parameters[9].Value = model.ReSendCount;
//        parameters[10].Value = model.ID;
        return DbHelperSQL.Update_object(strSql.toString(), paraArray) > 0;
    }
    
    public SettingModel GetSettingModel_dataaccess(String sqlStr){
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("select  top 1 ID,UniqueID,Type,SettingMark,Chanel,Other,Value,TimeSpan,SendTime,SendState,ReSendCount from Setting ");
        queryStr.append(" where "+sqlStr);
        
        ResultSet ds = DbHelperSQL.Query(queryStr.toString());
        try {
            if (ds != null && ds.next()){
                SettingModel m = DataRowToModel(ds);
                ds.close();
                return m;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private SettingModel DataRowToModel(ResultSet s){
        SettingModel m = new SettingModel();
        try {
            m._id = Integer.parseInt(s.getString("ID"));
            m._uniqueid = s.getString("UniqueID");
            m._type = s.getInt("Type");
            m._settingmark = s.getInt("SettingMark");
            m._chanel = s.getInt("Chanel");
            m._other = s.getInt("Other");
            m._value = s.getFloat("Value");
            m._timespan = s.getInt("TimeSpan");
            m._sendtime = s.getTimestamp("SendTime");
            m._sendstate = s.getInt("SendState");
            m._resendcount = s.getInt("ReSendCount");
            return m;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}