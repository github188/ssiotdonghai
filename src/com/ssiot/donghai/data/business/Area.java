package com.ssiot.donghai.data.business;

import android.text.TextUtils;
import android.util.Log;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.AreaModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//should copy from 
//Business/Angel.Extend.Business/

public class Area{
    private static final String tag = "Area";
    
    public Area(){
        
    }
    
    public AreaModel GetModel(int AreaID){
        StringBuilder strSql = new StringBuilder();
        strSql.append("select  top 1 * from Area ");
        strSql.append(" where AreaID=?");//?? TODO
//        SqlParameter[] parameters = { 
//                new SqlParameter("@AreaID", SqlDbType.Int,4)
//        };  
//        parameters[0].Value = AreaID;
        ArrayList<String> paramArray = new ArrayList<String>();
        paramArray.add("" + AreaID);
        ResultSet ds = DbHelperSQL.Query(strSql.toString(), paramArray);
        try {
            if (null != ds){
                if (ds.next()) {
                    return DataRowToModel(ds);
                }
                ds.close();
                Log.v(tag, "-------------GetModel-ok");
            } else {
                Log.e(tag, "-------------resultset=null !!!!!!!!!!!!"+strSql.toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<AreaModel> GetModelList(String strWhere) {
        StringBuilder strSql = new StringBuilder();
        strSql.append("select * ");
        strSql.append(" FROM Area ");
        if (strWhere.trim() != "") {
            strSql.append(" where " + strWhere);
        }
        ResultSet rs = DbHelperSQL.Query(strSql.toString());
        return DataTableToList(rs);
    }
    
    private List<AreaModel> DataTableToList(ResultSet c){
        List<AreaModel> mAreaModels = new ArrayList<AreaModel>();
//        int rowCount = c.size();
        AreaModel AreaModel = new AreaModel();
//        for (int i = 0; i < rowCount; i ++){
//            c.next();
//            
//        }
        try {
            while(c.next()){
                AreaModel = DataRowToModel(c);
                if (AreaModel != null){
                    mAreaModels.add(AreaModel);
                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mAreaModels;
    }
    
    public List<AreaModel> GetSelfAndChildrenAreaByAreaCode(String areacode) {
        List<AreaModel> area_list = new ArrayList<AreaModel>();
        if (!TextUtils.isEmpty(areacode)) {
            String sql = "  SELECT * FROM Area WHERE AreaCode LIKE '" + areacode.trim() + "%'";
            ResultSet ds = DbHelperSQL.Query(sql);
            area_list = DataTableToList(ds);
        }
        return area_list;
    }
    
    private AreaModel DataRowToModel(ResultSet c){//先.next !!
        AreaModel uModel = new AreaModel();
        try {
            uModel._areaid = Integer.parseInt(c.getString("AreaID"));
            uModel._areacode = c.getString("AreaCode");
            //TODO
            return uModel;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}