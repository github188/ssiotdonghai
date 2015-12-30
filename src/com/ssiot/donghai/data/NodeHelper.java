package com.ssiot.donghai.data;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.ssiot.donghai.data.model.NodeModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//"UI/Angel.IOT2.Web/Ajax/Get/AjaxGetJsonLastDataByNodenolist.aspx.cs" 294 行 --32%--
public class NodeHelper{
    private final static String tag = "NodeHelper";
    
    public NodeHelper(){
        
    }
    
    //获取每个节点的最新数据
    public static void GetLastDataByNodenolist(int areaID, int userRole, String userIDs//areaID一个账户只有一个 Usermodel._areaid Usermodel._userrole
            ,String pageIndex, String pageSize){
        long time1 = SystemClock.uptimeMillis();
        String nodenos = "";//Request["nodenos"];
        String nodeplace = "";//Request["nodeplace"];
        ResultSet node_ds;
        List<NodeModel> nodemodellists = new ArrayList<NodeModel>();//存放节点
        //判断是否通过安装地点查询
        if (TextUtils.isEmpty(nodeplace)) {
            
        } else {
            nodemodellists = DataAPI.GetNodeListByAreaIDAndPlace(areaID, nodeplace);
        }
        if (TextUtils.isEmpty(nodenos) && TextUtils.isEmpty(nodeplace)) {
            if (userRole == 1) {
                nodemodellists = DataAPI.GetNodeListByUserIDs("-1");
            } else {
//                String userIDs = ""; 
//                userIDs = (String)Session["UserIDs"];
                nodemodellists = DataAPI.GetNodeListByUserIDs(userIDs);
                if (nodemodellists == null || nodemodellists.size() == 0) {
//                    Response.Write("");
                    Log.e(tag, " no node !!!!!!!!!!!!!!!!!1");
                    return ;
                }
            }
        }
        for (NodeModel model : nodemodellists){
            nodenos += model._nodeno + ",";
        }
        if (nodenos.endsWith(",")){
            nodenos = nodenos.substring(0, nodenos.length() - 1);
        }
        if (!nodenos.contains(",") || pageIndex == null || 
                TextUtils.isEmpty(pageIndex) || pageSize == null
                || TextUtils.isEmpty(pageSize)){
            node_ds = DataAPI.GetLastData("更新时间 DESC", nodenos);
        } else {
            int pgIndex = 1;
            int pgSize = 0;
            try {
                pgIndex = Integer.parseInt(pageIndex);
                pgSize = Integer.parseInt(pageSize);
            } catch (Exception e) {
                e.printStackTrace();
            }
            node_ds = DataAPI.GetLastData("更新时间 DESC", nodenos, ((pgIndex - 1) * pgSize + 1), pgIndex * pgSize);
        }
        
        Log.v(tag, "(((((((((((((((((((((((((((((((-------------------");
        int index = 0;
        try {
            if (null != node_ds){
                StringBuilder str = new StringBuilder();
                int columcount = node_ds.getMetaData().getColumnCount();
                while(node_ds.next()){
                    for (int i = 1 ; i <= columcount; i ++){
                        str.append(node_ds.getString(i) + "\t");
                    }
                    str.append("\n");
                    index ++;
                }
                Log.v(tag, str.toString());
            } else {
                Log.e(tag, "node_ds == null !!!!!!!!!!!!!!!!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.v(tag, ")))))))))))))))))))))))))))))))-----lines:" + index + " time:"+ (SystemClock.uptimeMillis() - time1));
        //TODO
    }
}