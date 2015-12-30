package com.ssiot.donghai.data.business;

import android.text.TextUtils;
import android.util.Log;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.LatestDataModel;
import com.ssiot.donghai.data.model.SensorModel;
import com.ssiot.donghai.data.model.view.SensorViewModel;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Sensor {
    private static final String tag = "Sensor";
    
    public List<SensorViewModel> GetSensorListByNodeNoString(String nodenos) {//在ProductSensor表中！！?
        List<SensorViewModel> list = null;
        ResultSet sensords = null;
        if (TextUtils.isEmpty(nodenos)) {
            sensords = DbHelperSQL.Query("SELECT DISTINCT  SensorNo ,  Channel ,  SensorName ,  ShortName ,  Unit , Accuracy ,MinValue ,MaxValue,  Color ,  SensorCategoryNo " +
            		"FROM    [ProductSensor]  JOIN [Sensor] ON [ProductSensor].SensorID = [Sensor].SensorNO WHERE   CAST([SensorID] AS VARCHAR) + '_' + CAST([Channel] AS VARCHAR) IN ( SELECT  CAST([SensorID] AS VARCHAR) + '_' + CAST([Channel] AS VARCHAR)  FROM    productsensor  )");
        } else {
            sensords = DbHelperSQL.Query("SELECT DISTINCT  SensorNo ,  Channel ,  SensorName ,  ShortName ,  Unit ,  Accuracy , MinValue ,MaxValue,  Color ,  SensorCategoryNo " +
            		"FROM    [ProductSensor]  JOIN [Sensor] ON [ProductSensor].SensorID = [Sensor].SensorNO WHERE   CAST([SensorID] AS VARCHAR) + '_' + CAST([Channel] AS VARCHAR) IN ( SELECT  CAST([SensorID] AS VARCHAR) + '_' + CAST([Channel] AS VARCHAR)  FROM    productsensor  WHERE   ProductID IN ( SELECT DISTINCT  ProductID FROM     [Node] WHERE    NodeNo in (" + nodenos + " )) )");
        }

        try {
            if (sensords != null) {
                list = new ArrayList<SensorViewModel>();
                while(sensords.next()){
                    SensorViewModel model = DataRowToViewModel(sensords);
                    list.add(model);
                }
                sensords.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    public List<SensorModel> GetModelList(String strWhere)
    {
        ResultSet ds = GetList_dataaccess(strWhere);
        return DataTableToList(ds);
    }
    
    //-------------------------------------------------------
    public ResultSet GetList_dataaccess(String strWhere) {
        StringBuilder strSql = new StringBuilder();
        strSql.append("select SensorNo,SensorCategoryNo,SensorName,ShortName,Unit,Accuracy,MinValue,MaxValue,Color ");
        strSql.append(" FROM Sensor ");
        if (!TextUtils.isEmpty(strWhere.trim())) {
            strSql.append(" where " + strWhere);
        }
        return DbHelperSQL.Query(strSql.toString());
    }
    
    public List<SensorModel> DataTableToList(ResultSet c){
        List<SensorModel> mModels = new ArrayList<SensorModel>();
        SensorModel mm = new SensorModel();
        try {
            while(c.next()){
                mm = DataRowToModel(c);
                if (mm != null){
                    mModels.add(mm);
                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mModels;
    }
    
    public SensorModel DataRowToModel(ResultSet rs){
        SensorModel m = new SensorModel();
        try {
            m._sensorno = Integer.parseInt(rs.getString("SensorNo"));
            m._sensorcategoryno = Integer.parseInt(rs.getString("SensorCategoryNo"));
            m._sensorname = rs.getString("SensorName");
            m._shortname = rs.getString("ShortName");
            m._unit = rs.getString("Unit");
            m._accuracy = Integer.parseInt(rs.getString("Accuracy"));
            m._minvalue = rs.getFloat("MinValue");
            m._maxvalue = rs.getFloat("MaxValue");
            m._color = rs.getString("Color");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }
    
    public SensorViewModel DataRowToViewModel(ResultSet rs){
        SensorViewModel m = new SensorViewModel();
        try {
            m._sensorno = rs.getInt("SensorNO");
            m._channel = Integer.parseInt(rs.getString("Channel"));
            m._sensorcategoryno = rs.getInt("SensorCategoryNo");
            m._sensorname = rs.getString("SensorName");
            m._shortname = rs.getString("ShortName");
            m._unit = rs.getString("Unit");
            m._accuracy = rs.getInt("Accuracy");
            m._minvalue = rs.getFloat("MinValue");
            m._maxvalue = rs.getFloat("MaxValue");
            m._color = rs.getString("Color");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m;
    }
}