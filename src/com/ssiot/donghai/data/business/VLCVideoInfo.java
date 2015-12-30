
package com.ssiot.donghai.data.business;

import android.text.TextUtils;
import android.util.Log;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.VLCVideoInfoModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VLCVideoInfo {
    private static final String tag = "VLCVideoInfo";

    // 分页获取数据列表
    public List<VLCVideoInfoModel> GetVLCByPage(String strWhere, String orderby, int startIndex,
            int endIndex) {
        List<VLCVideoInfoModel> VLC_list = new ArrayList<VLCVideoInfoModel>();
        ResultSet ds = GetListByPage(strWhere, orderby, startIndex, endIndex);
        try {
            if (ds != null) {
                while (ds.next()) {
                    VLC_list.add(DataRowToModel(ds));
                }
                // foreach(DataRow row in ds.Tables[0].Rows)
                // {
                // VLC_list.Add(dal.DataRowToModel(row));
                // }
            }
            ds.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return VLC_list;
    }
    
    public List<VLCVideoInfoModel> GetVLCmap(String strWhere) {
        List<VLCVideoInfoModel> VLC_list = new ArrayList<VLCVideoInfoModel>();
        ResultSet ds = GetList(strWhere);
        try {
            if(ds!=null) {
                while(ds.next()){
                    VLCVideoInfoModel v = DataRowToModel(ds);
                    if (null != v){
                        VLC_list.add(v);
                    } else {
                        Log.e(tag, "-------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!VLCVideoInfoModel get null");
                    }
                    
                }
            }
            ds.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if(ds!=null) {
//            foreach(DataRow row in ds.Tables[0].Rows)
//            {
//                VLC_list.Add(dal.DataRowToModel(row));
//            }
//        }
        return VLC_list;
    }
    
    public List<VLCVideoInfoModel> GetModelList(String strWhere) {
        ResultSet ds = GetList(strWhere);
        return DataTableToList(ds);
    }
    
    public List<VLCVideoInfoModel> DataTableToList(ResultSet dt) {
        List<VLCVideoInfoModel> modelList = new ArrayList<VLCVideoInfoModel>();
        try {
            while(dt.next()){
                VLCVideoInfoModel modelTmp;
                modelTmp = DataRowToModel(dt);
                if (null != modelTmp){
                    modelList.add(modelTmp);
                }
            }
            dt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modelList;
    }

    // ------------------------------
    
    //获得数据列表
    private ResultSet GetList(String strWhere) {
        StringBuilder strSql = new StringBuilder();
        strSql.append("select VLCVideoInfoID,AreaID,AreaName,UserName,PassWord,URL,IP,Port,Address,Channel,Subtype,Type,CreateTime,Remark,Longitude,Latitude,TcpPort ");
        strSql.append(" FROM VLCVideoInfo ");
        if (strWhere.trim() != "") {
            strSql.append(" where " + strWhere);
        }
        return DbHelperSQL.Query(strSql.toString());
    }
    
    // 分页获取数据列表
    private ResultSet GetListByPage(String strWhere, String orderby, int startIndex, int endIndex) {
        StringBuilder strSql = new StringBuilder();
        strSql.append("SELECT * FROM ( ");
        strSql.append(" SELECT ROW_NUMBER() OVER (");
        if (!TextUtils.isEmpty(orderby.trim())) {
            strSql.append("order by T." + orderby);
        } else {
            strSql.append("order by T.VLCVideoInfoID desc");
        }
        strSql.append(")AS Row, T.*  from VLCVideoInfo T ");
        if (!TextUtils.isEmpty(strWhere.trim())) {
            strSql.append(" WHERE " + strWhere);
        }
        strSql.append(" ) TT");
        strSql.append(" WHERE TT.Row between {" + startIndex+"} and {"+endIndex+"}");
        return DbHelperSQL.Query(strSql.toString());
    }

    private VLCVideoInfoModel DataRowToModel(ResultSet c) {// 先.next !!
        VLCVideoInfoModel vModel = new VLCVideoInfoModel();
        try {
            vModel._vlcvideoinfoid = Integer.parseInt(c.getString("VLCVideoInfoID"));
            vModel._areaid = Integer.parseInt(c.getString("AreaID"));
            vModel._username = c.getString("UserName");
            vModel._password = c.getString("PassWord");
            vModel._url = c.getString("URL");
            vModel._ip = c.getString("IP");
            vModel._port = c.getString("Port");
            vModel._address = c.getString("Address");
            vModel._type = c.getString("Type");
//            
            vModel._tcpport = c.getInt("TcpPort");
            vModel._remark = c.getString("Remark");
//            Log.v(tag, "-------tcpport---------------------------------" + vModel._tcpport + vModel._address);
            // TODO
            return vModel;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
