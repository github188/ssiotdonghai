package com.ssiot.donghai.data.business;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.SensorModifyDataModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SensorModifyData{
    
    public List<SensorModifyDataModel> GetModelList(String strWhere){
        ResultSet ds = GetList_dataaccess(strWhere);
        return DataTableToList(ds);
    }
    
    //------------------------------------------------------
    public ResultSet GetList_dataaccess(String strWhere) {
        StringBuilder strSql = new StringBuilder();
        strSql.append("select ID,SensorNo,Type,Other,Value,Remark ");
        strSql.append(" FROM SensorModifyData ");
        if (strWhere.trim() != "") {
            strSql.append(" where " + strWhere);
        }
        return DbHelperSQL.Query(strSql.toString());
    }
    
    public SensorModifyDataModel GetModel(int ID){
        StringBuilder strSql = new StringBuilder();
        strSql.append("select  top 1 ID,SensorNo,Type,Other,Value,Remark from SensorModifyData ");
        strSql.append(" where ID=" + ID);
//        SqlParameter[] parameters = { 
//                new SqlParameter("@ID", SqlDbType.Int,4)
//        };
//        parameters[0].Value = ID; 

        ResultSet ds = DbHelperSQL.Query(strSql.toString());
        try {
            if (ds != null && ds.next()) {
                SensorModifyDataModel model = DataRowToModel(ds);
                ds.close();
                return model;
            }
            ds.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    private List<SensorModifyDataModel> DataTableToList(ResultSet c){
        List<SensorModifyDataModel> modelList = new ArrayList<SensorModifyDataModel>();
//        int rowsCount = dt.Rows.Count;
        SensorModifyDataModel model = new SensorModifyDataModel();
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
    
    private SensorModifyDataModel DataRowToModel(ResultSet s){
        SensorModifyDataModel m = new SensorModifyDataModel();
        try {
            m._id = Integer.parseInt(s.getString("ID"));
            m._sensorno = Integer.parseInt(s.getString("SensorNo"));
            m._type = Integer.parseInt(s.getString("Type"));
            m._other = Integer.parseInt(s.getString("Other"));
            m._value = Float.parseFloat(s.getString("Value"));
            m._remark = s.getString("Remark");
            return m;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}