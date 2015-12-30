package com.ssiot.donghai.data.business;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.UserModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//should copy from 
//Business/Angel.Extend.Business/User.cs

public class User{
    
    public User(){
        
    }
    
    public List<UserModel> GetModelList(String strWhere) {
        StringBuilder strSql = new StringBuilder();
        strSql.append("select * ");
        strSql.append(" FROM tbl_User ");
        if (strWhere.trim() != "") {
            strSql.append(" where " + strWhere);
        }
        ResultSet ds = DbHelperSQL.Query(strSql.toString());
        if (null != ds){
            return DataTableToList(ds);
        }
        return null;
    }
    
    public List<UserModel> DataTableToList(ResultSet c){
        List<UserModel> mUserModels = new ArrayList<UserModel>();
//        int rowCount = c.size();
        UserModel userModel = new UserModel();
//        for (int i = 0; i < rowCount; i ++){
//            c.next();
//            
//        }
        try {
            while(c.next()){
                userModel = DataRowToModel(c);
                if (userModel != null){
                    mUserModels.add(userModel);
                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mUserModels;
    }
    
    //jingbo single
    private UserModel DataRowToModel(ResultSet c){//先.next !!
        UserModel uModel = new UserModel();
        try {
            uModel._userid = Integer.parseInt(c.getString("UserID"));
            uModel._uniqueid = c.getString("UniqueID");
            uModel._areaid = Integer.parseInt(c.getString("AreaID"));
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