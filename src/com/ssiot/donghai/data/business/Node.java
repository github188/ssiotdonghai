package com.ssiot.donghai.data.business;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.NodeModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Node{
    private static final String tag = "com.ssiot.donghai.data.business.Node";
    
    public Node(){
        
    }
    
    public boolean Update(NodeModel model){
        StringBuilder strSql = new StringBuilder();
        strSql.append("update Node set ");
        strSql.append("UniqueID=?,");
        strSql.append("NodeNo=?,");
        strSql.append("NodeCategoryNo=?,");
        strSql.append("ProductID=?,");
        strSql.append("GatewayNo=?,");
        strSql.append("AreaID=?,");
        strSql.append("Longitude=?,");
        strSql.append("Latitude=?,");
        strSql.append("Location=?,");
        strSql.append("Image=?,");
        strSql.append("OnlineType=?,");
        strSql.append("Color=?,");
        strSql.append("Expression=?,");
        strSql.append("Remark=?");
        strSql.append(" where NodeID=?");
        
        ArrayList<Object> params = new ArrayList<Object>();
        params.add(model._uniqueid);
        params.add(model._nodeno);
        params.add(model._nodecategoryno);
        params.add(model._productid);
        params.add(model._gatewayno);
        params.add(model._areaid);
        params.add(model._longitude);
        params.add(model._latitude);
        params.add(model._location);
        params.add(model._image);
        params.add(model._onlinetype);
        params.add(model._color);
        params.add(model._expression);
        params.add(model._remark);
        params.add(model._nodeid);
        
        return DbHelperSQL.Update_object(strSql.toString(), params) > 0;
    }
    
    public NodeModel GetModelByNodeNo(String nodeno){
        StringBuilder strSql = new StringBuilder();
        strSql.append("select  top 1 NodeID,UniqueID,NodeNo,NodeCategoryNo,ProductID,GatewayNo,AreaID,Longitude,Latitude,Location,Image,OnlineType,Color,Expression,Remark  from Node ");
        strSql.append(" where NodeNo=" + nodeno);
//        ArrayList<String> paramArray = new ArrayList<String>();
//        paramArray.add("" + nodeno);
        ResultSet ds = DbHelperSQL.Query(strSql.toString());
        try {
            if (ds != null && ds.next()) {
                NodeModel model = DataRowToModel(ds);
                ds.close();
                return model;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
//    public List<NodeModel> GetNodeList(String strwhere){
//        ResultSet rs = GetList(strwhere);
//        List<NodeModel> nodelist = new ArrayList<NodeModel>();
//        try {
//            while(rs.next()){
//                nodelist.add(FromDataRow(rs));
//            }
//            return nodelist;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
    
    public List<NodeModel> GetModelList(String strWhere){
//        string cacheKey =( "NodeGetModelList+" + strWhere).GetHashCode().ToString();
//        object objModel = Angel.Common.Web.DataCache.GetCache(cacheKey);
        List<NodeModel> objModel = null;
        if (objModel == null){
            StringBuilder strSql = new StringBuilder();
//            strSql.append("select NodeID,UniqueID,NodeNo,NodeCategoryNo,ProductID,GatewayNo,AreaID,Longitude,Latitude," +
//                    "Location,Image,OnlineType,Color,Expression,Remark ");//jingbo 不知为何写这么复杂，测试发现耗时多
            strSql.append("select * ");
            strSql.append(" FROM Node ");
            if (strWhere.trim() != ""){
                strSql.append(" where " + strWhere);
            }
            ResultSet rs = DbHelperSQL.Query(strSql.toString());
            if (null != rs){
                objModel= DataTableToList(rs);
            }
        }
        return objModel;
    }
    
    public List<NodeModel> GetModelListByAreaIDs(String areaids){
        String strWhere = "";
        if (areaids == "0"){
            strWhere = "1=1";
        } else {
            strWhere = "AreaID in (" + areaids + ")";
        }
        return GetModelList(strWhere);
    }
    
    private List<NodeModel> DataTableToList(ResultSet c){
        List<NodeModel> modelList = new ArrayList<NodeModel>();
//        int rowsCount = dt.Rows.Count;
        NodeModel model = new NodeModel();
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
//        if (rowsCount > 0){
//            Angel.IOT2.Model.Node model;
//            for (int n = 0; n < rowsCount; n++){
//                for (int n = 0; n < rowsCount; n++){
//                    if (model != null){
//                        modelList.Add(model);
//                    }
//                }
//            }
//        }
//        return modelList;
    }
    
    private NodeModel DataRowToModel(ResultSet s){
        NodeModel m = new NodeModel();
        try {
            m._nodeid = s.getInt("NodeID");
            m._uniqueid = s.getString("UniqueID");
            m._nodeno = Integer.parseInt(s.getString("NodeNo"));
            m._nodecategoryno = Integer.parseInt(s.getString("NodeCategoryNo"));
            m._productid = Integer.parseInt(s.getString("ProductID"));
            m._gatewayno = Integer.parseInt(s.getString("GatewayNo"));
            m._areaid = Integer.parseInt(s.getString("AreaID"));
            m._longitude = s.getFloat("Longitude");
            m._latitude = s.getFloat("Latitude");
            m._location = s.getString("Location");
            m._image = s.getString("Image");
            m._onlinetype = s.getString("OnlineType");
            if (null != m._onlinetype){
                m._onlinetype.trim();
            }
            m._color = s.getString("Color");
            m._expression = s.getString("Expression");
            m._remark = s.getString("Remark");
            return m;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //----------------------------------------
//    public DataSet GetList(String strWhere){
//        StringBuilder strSql = new StringBuilder();
//        strSql.append("select NodeID,UniqueID,NodeNo,NodeCategoryNo,ProductID,GatewayNo,AreaID,Longitude,Latitude," +
//        		"Location,Image,OnlineType,Color,Expression,Remark ");
//        strSql.append(" FROM Node ");
//        if (strWhere.trim() != ""){
//            strSql.append(" where " + strWhere);
//        }
//        return DbHelperSQL.Query(strSql.toString());
//    }
    
}