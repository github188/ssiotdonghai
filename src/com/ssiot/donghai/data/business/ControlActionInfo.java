package com.ssiot.donghai.data.business;

import android.R.integer;
import android.text.TextUtils;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.ControlActionInfoModel;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ControlActionInfo{
    
    public int Add(ControlActionInfoModel model){
        StringBuilder strSql = new StringBuilder();
        strSql.append("insert into ControlActionInfo(");
        strSql.append("AreaID,ControlName,UniqueID,DeviceNo,ControlType,CollectUniqueIDs,ControlCondition,OperateTime,StateNow,Operate,Remark)");
        strSql.append(" values (");
        strSql.append("?,?,?,?,?,?,?,?,?,?,?)");
        strSql.append(";select @@IDENTITY");
        
        ArrayList<Object> parameters = new ArrayList<Object>();
        parameters.add(model._areaid);
        parameters.add(model._controlname);
        parameters.add(model._uniqueid);
        parameters.add(model._deviceno);
        parameters.add(model._controltype);
        parameters.add(model._collectuniqueids);
        parameters.add(model._controlcondition);
        parameters.add(model._operatetime);
        parameters.add(model._statenow);
        parameters.add(model._operate);
        parameters.add(model._remark);
        return DbHelperSQL.Update_object(strSql.toString(), parameters);
    }
    
    public boolean Update(ControlActionInfoModel model){
        StringBuilder strSql = new StringBuilder();
        strSql.append("update ControlActionInfo set ");
        strSql.append("ControlName=?,");
        strSql.append("UniqueID=?,");
        strSql.append("DeviceNo=?,");
        strSql.append("ControlType=?,");
        strSql.append("CollectUniqueIDs=?,");
        strSql.append("ControlCondition=?,");
        strSql.append("OperateTime=?,");
        strSql.append("StateNow=?,");
        strSql.append("Operate=?,");
        strSql.append("Remark=?");
        strSql.append(" where ID=?");
        
        ArrayList<Object> parameters = new ArrayList<Object>();
        parameters.add(model._controlname);
        parameters.add(model._uniqueid);
        parameters.add(model._deviceno);
        parameters.add(model._controltype);
        parameters.add(model._collectuniqueids);
        parameters.add(model._controlcondition);
        parameters.add(model._operatetime);
        parameters.add(model._statenow);
        parameters.add(model._operate);
        parameters.add(model._remark);
        parameters.add(model._id);
        
        int rows = DbHelperSQL.Update_object(strSql.toString(), parameters);
        return rows > 0;
    }
    
    public boolean Delete(int ID) {

        StringBuilder strSql=new StringBuilder();
        strSql.append("delete from ControlActionInfo ");
        strSql.append(" where ID=" +ID);
//        SqlParameter[] parameters = { 
//                new SqlParameter("@ID", SqlDbType.Int,4)
//        };
//        parameters[0].Value = ID; 

        int rows=DbHelperSQL.Update(strSql.toString());//,parameters);
        if (rows > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public ControlActionInfoModel GetModel(int id){
        return GetModel_dataaccess(id);
    }
    
    public List<ControlActionInfoModel> GetModelList(String strWhere){
        ResultSet ds = GetList_dataaccess(strWhere);
        return DataTableToList(ds);
    }
    
    //-----------------------------------------------------------------
    public ControlActionInfoModel GetModel_dataaccess(int id){
        StringBuilder strSql = new StringBuilder();
        strSql.append("select  top 1 ID,AreaID,ControlName,UniqueID,DeviceNo,ControlType,CollectUniqueIDs,ControlCondition,OperateTime,StateNow,Operate,Remark from ControlActionInfo ");
        strSql.append(" where ID=?");
//        SqlParameter[] parameters = { 
//                new SqlParameter("@ID", SqlDbType.Int,4)
//        };
        ArrayList<String> parameters = new ArrayList<String>();
        parameters.add(""+id);
//        parameters[0].Value = ID; 

        ControlActionInfoModel model = new ControlActionInfoModel();
        ResultSet ds = DbHelperSQL.Query(strSql.toString(), parameters);
        if (null != ds){
            try {
                if (ds.next()){
                    return DataRowToModel(ds);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        return null;
    }
    
    public ResultSet GetList_dataaccess(String strWhere)
    {
        StringBuilder strSql=new StringBuilder();
        strSql.append("select ID,AreaID,ControlName,UniqueID,DeviceNo,ControlType,CollectUniqueIDs,ControlCondition,OperateTime,StateNow,Operate,Remark ");
        strSql.append(" FROM ControlActionInfo ");
        if(!TextUtils.isEmpty(strWhere.trim())) {
            strSql.append(" where "+strWhere);
        }
     //   SqlParameter[] parameters = new SqlParameter[] { new SqlParameter("@strWhere", strWhere) };

        return DbHelperSQL.Query(strSql.toString());
    }
    
    private List<ControlActionInfoModel> DataTableToList(ResultSet c){
        List<ControlActionInfoModel> modelList = new ArrayList<ControlActionInfoModel>();
        ControlActionInfoModel model = new ControlActionInfoModel();
        try {
            while(c.next()){
                model = DataRowToModel(c);
                if (model != null){
                    modelList.add(model);
                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelList;
    }
    
    private ControlActionInfoModel DataRowToModel(ResultSet s){
        ControlActionInfoModel m = new ControlActionInfoModel();
        try {
            m._id = Integer.parseInt(s.getString("ID"));
            m._areaid = Integer.parseInt(s.getString("AreaID"));
            m._controlname = s.getString("ControlName");
            m._uniqueid = s.getString("UniqueID");
            m._deviceno = Integer.parseInt(s.getString("DeviceNo"));
            m._controltype = Integer.parseInt(s.getString("ControlType"));
            m._collectuniqueids = s.getString("CollectUniqueIDs");
            m._controlcondition = s.getString("ControlCondition");
            m._operatetime = s.getTimestamp("OperateTime");
            m._statenow = Integer.parseInt(s.getString("StateNow"));
            m._operate = s.getString("Operate");
            m._remark = s.getString("Remark");
            return m;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}