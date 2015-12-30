package com.ssiot.donghai.data.business;

import android.text.TextUtils;

import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.view.SensorViewModel;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LiveData{
    
    private Sensor mSensorDataAccess = new Sensor();
    
    //获取流水数据(只在前range条中查找)
    public ResultSet GetData(String grainsize, String valuetype, String begintime, String endtime, String orderby, 
            int beginindex, int endindex, boolean unit, int range, List<SensorViewModel> sensorlist, String nodenolist){
        ResultSet ds = null;
        //传感器列表
        List<SensorViewModel> list = new ArrayList<SensorViewModel>();

        if (sensorlist != null) {
            list = sensorlist;
        } else {
            list = mSensorDataAccess.GetSensorListByNodeNoString(nodenolist);
        }
        if (list.size() > 0) {
            StringBuilder sb_sql = new StringBuilder();
            //用于节点编号，安装地点和更新时间三列的生成
            sb_sql.append("SELECT NodeNo AS 节点编号 , Location AS 安装地点 , [DATETIME] AS 更新时间 ,");

            StringBuilder sb_columns1 = new StringBuilder();
            StringBuilder sb_columns2 = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                String unitstr = "";
                String sensoradd = "";
                if (unit) {
                    unitstr = " + '" + list.get(i)._unit + "'";
                }
                if (list.get(i)._channel > 0) {
                    sensoradd = "" +list.get(i)._channel;
                }
                //sb_columns1.AppendFormat(" replace(convert(VARCHAR,convert(float,round(SensorColumn.[{0}_{1}],{2}))){3}, '32767'{3}, '无') AS {4} ,",
                //    list[i].SensorNO, list[i].Channel, list[i].Accuracy, unitstr, list[i].ShortName
                //    );
                //
                sb_columns1.append(" replace(convert(VARCHAR,convert(float,round(SensorColumn.["+ list.get(i)._sensorno+"_"+list.get(i)._channel+"]," +
                        list.get(i)._accuracy+")))"+unitstr+", '32767'"+unitstr+", '无') AS "+list.get(i)._shortname+sensoradd+" ,");
                sb_columns2.append("["+list.get(i)._sensorno+"_"+list.get(i)._channel+"],");
            }
            sb_columns2.deleteCharAt(sb_columns2.length()-1);
            sb_columns1.deleteCharAt(sb_columns1.length()-1);
            sb_columns1.append(" FROM ( SELECT [Node].NodeNo , [Node].Location ,[ResultData2].DATETIME , CAST([ResultData2].[SensorNo] AS VARCHAR) + '_' + CAST([ResultData2].[Channel] AS VARCHAR) AS [SensorList] , [ResultData2].DATA  FROM (");
            if (sb_columns1.length() > 0){
                sb_sql.append(sb_columns1.toString().substring(1));
            }

            String timelength = "15";
            String tablename = "[LiveData]";
            String indexid = "[LiveDataID]";
            String datacolumn = "[Data]";
            String timecolumn = "[CollectionTime]";
            String rangestr = "";
            rangestr = "TOP " + range;
            if ("逐分钟".equals(grainsize)){
                timelength = "19";
                tablename = "[LiveData]";
                timecolumn = "[CollectionTime]";
            } else if ("10分钟".equals(grainsize)){
                timelength = "16";
                tablename = "[DataByTen]";
                timecolumn = "[CountTime]";
                indexid = "[EveryYearByMinuteID]";
            } else if ("逐小时".equals(grainsize)){
                timelength = "13";
                tablename = " [DataByHour]";
                timecolumn = "[CountTime]";
                indexid = "[EveryYearByHourID]";
            } else if ("逐日".equals(grainsize)){
                timelength = "10";
                tablename = "[DataByDay]";
                timecolumn = "[CountTime]";
                indexid = "[EveryYearByDayID]";
            } else if ("逐月".equals(grainsize)){
                timelength = "7";
                tablename = "[DataByMonth]";
                timecolumn = "[CountTime]";
                indexid = "[EveryYearByMonthID]";
            } else if ("逐年".equals(grainsize)){
                timelength = "4";
                tablename = "[DataByDay]";
                timecolumn = "[CountTime]";
                indexid = "[EveryYearByDayID]";
            } else {
                timelength = "15";
                tablename = "[LiveData]";
                timecolumn = "[CollectionTime]";
                indexid = "[LiveDataID]";
            }
            
            if (!"逐分钟".equals(grainsize)){
                if ("最大值".equals(valuetype.toUpperCase())){
                    datacolumn = "MaxData";
                } else if ("平均值".equals(valuetype)){
                    datacolumn = "AvgData";
                } else if ("最小值".equals(valuetype)){
                    datacolumn = "MinData";
                } else if ("累计值".equals(valuetype)){
                    datacolumn = "SumData";
                } else {
                    datacolumn = "Data";
                }
            }

//TODO let王桂华 改了 varchar变成datetime because mobileapi de timestr
            sb_sql.append(" SELECT SUBSTRING(CONVERT(VARCHAR(20), "+timecolumn+", 20), 1, "+timelength+") AS DATETIME , [UniqueID] , [Channel] , [SensorNo] , MAX("+datacolumn+") AS DATA " +
            		"FROM ( SELECT "+rangestr+" * FROM  "+tablename+" ORDER BY  "+indexid+" DESC ) ResultData1 WHERE 1 = 1 ");

            if (!TextUtils.isEmpty(begintime) && !TextUtils.isEmpty(endtime))
                sb_sql.append(" AND "+timecolumn+" > '"+begintime+"' AND "+timecolumn+" < '"+endtime+"' ");

            sb_sql.append(" GROUP BY SUBSTRING(CONVERT(VARCHAR(20), "+timecolumn+" , 20) , 1 , "+timelength+") ,[UniqueID] , [Channel] , [SensorNo] ) [ResultData2] " +
            		"JOIN [Node] ON [ResultData2].UniqueID = [Node].UniqueID JOIN [Sensor] ON [Sensor].SensorNo = [ResultData2].SensorNo ) [ResultData3] ");

            sb_sql.append("PIVOT ( MAX([ResultData3].DATA) FOR [ResultData3].[SensorList] IN ( "+sb_columns2.toString()+" ) ) [SensorColumn]");
            sb_sql.insert(0, "SELECT TOP 100000 * , ROW_NUMBER() OVER ( ORDER BY " + orderby + " ) AS [RANK] FROM (");
            sb_sql.append(" ) AS [Table] WHERE 1=1");

            if (!TextUtils.isEmpty(nodenolist)) {
                sb_sql.append(" AND 节点编号 IN ("+nodenolist+")");
            }

            if (orderby != null) {
                sb_sql.append(" ORDER BY "+orderby+" ");
            }
            //分页
         
            if (beginindex != -1 && endindex != -1) {
                sb_sql.insert(0, "SELECT * FROM (");
                sb_sql.append(" ) AS [Table] WHERE 1=1");
                sb_sql.append(" AND [Table].[RANK] >= "+beginindex+" AND [Table].[RANK] <"+endindex+" ");
            }
            ds = DbHelperSQL.Query(sb_sql.toString());
        }
        return ds;
    }
    
    //精确查找获取数据
    public ResultSet GetData(String grainsize, String valuetype, String begintime, String endtime, String orderby, 
            int beginindex, int endindex, boolean unit, String nodenolist)
    {
        ResultSet ds = null;
        // 传感器View列表
        List<SensorViewModel> list = new ArrayList<SensorViewModel>();
        if (!TextUtils.isEmpty(nodenolist)) {
            list = mSensorDataAccess.GetSensorListByNodeNoString(nodenolist);
        } else {
            return ds;
        }
        
        if (list.size() > 0) {
            StringBuilder sb_sql = new StringBuilder();
            //用于节点编号，安装地点和更新时间三列的生成
            sb_sql.append("SELECT NodeNo AS 节点编号 , Location AS 安装地点 , [DATETIME] AS 更新时间 ,");

            StringBuilder sb_columns1 = new StringBuilder();//用于标识输出
            StringBuilder sb_columns2 = new StringBuilder();//用于转置
            for (int i = 0; i < list.size(); i++) {
                String unitstr = "";
                String sensoradd = "";
                if (unit) {
                    unitstr = " + '" + list.get(i)._unit + "'";
                }
                if (list.get(i)._channel > 0) {
                    sensoradd = "" +list.get(i)._channel;
                }
                //sb_columns1.AppendFormat(" replace(convert(VARCHAR,convert(float,round(SensorColumn.[{0}_{1}],{2}))){3}, '32767'{3}, '无') AS {4} ,",
                //    list[i].SensorNO, list[i].Channel, list[i].Accuracy, unitstr, list[i].ShortName
                //    );
                //
                sb_columns1.append(" replace(convert(VARCHAR,convert(float,round(SensorColumn.["+ list.get(i)._sensorno+"_"+list.get(i)._channel+"]," +
                        list.get(i)._accuracy+")))"+unitstr+", '32767'"+unitstr+", '无') AS "+list.get(i)._shortname+sensoradd+" ,");
                sb_columns2.append("["+list.get(i)._sensorno+"_"+list.get(i)._channel+"],");
            }
            sb_columns2.deleteCharAt(sb_columns2.length()-1);
            sb_columns1.deleteCharAt(sb_columns1.length()-1);
            sb_columns1.append(" FROM ( SELECT [Node].NodeNo , [Node].Location ,[ResultData2].DATETIME , CAST([ResultData2].[SensorNo] AS VARCHAR) + '_' + CAST([ResultData2].[Channel] AS VARCHAR) AS [SensorList] , [ResultData2].DATA  FROM (");
            if (sb_columns1.length() > 0){
                sb_sql.append(sb_columns1.toString().substring(1));
            }

            String timelength = "15";
            String tablename = "[LiveData]";
            String indexid = "[LiveDataID]";
            String datacolumn = "[Data]";
            String timecolumn = "[CollectionTime]";
            if ("逐分钟".equals(grainsize)){
                timelength = "19";
                tablename = "[LiveData]";
                timecolumn = "[CollectionTime]";
            } else if ("10分钟".equals(grainsize)){
                timelength = "16";
                tablename = "[DataByTen]";
                timecolumn = "[CountTime]";
                indexid = "[EveryYearByMinuteID]";
            } else if ("逐小时".equals(grainsize)){
                timelength = "13";
                tablename = " [DataByHour]";
                timecolumn = "[CountTime]";
                indexid = "[EveryYearByHourID]";
            } else if ("逐日".equals(grainsize)){
                timelength = "10";
                tablename = "[DataByDay]";
                timecolumn = "[CountTime]";
                indexid = "[EveryYearByDayID]";
            } else if ("逐月".equals(grainsize)){
                timelength = "7";
                tablename = "[DataByMonth]";
                timecolumn = "[CountTime]";
                indexid = "[EveryYearByMonthID]";
            } else if ("逐年".equals(grainsize)){
                timelength = "4";
                tablename = "[DataByDay]";
                timecolumn = "[CountTime]";
                indexid = "[EveryYearByDayID]";
            } else {
                timelength = "15";
                tablename = "[LiveData]";
                timecolumn = "[CollectionTime]";
                indexid = "[LiveDataID]";
            }
            
            if (!"逐分钟".equals(grainsize)){
                if ("最大值".equals(valuetype.toUpperCase())){
                    datacolumn = "MaxData";
                } else if ("平均值".equals(valuetype)){
                    datacolumn = "AvgData";
                } else if ("最小值".equals(valuetype)){
                    datacolumn = "MinData";
                } else if ("累计值".equals(valuetype)){
                    datacolumn = "SumData";
                } else {
                    datacolumn = "Data";
                }
            }
            
          //根据Nodeno（节点编号）获取节点标识列表串
            ResultSet ds_nodeUniqs = GetNodeUnqiuesByNodeNos(nodenolist);
            String nodeUniques = "";//节点标识列表（已用"'"号区分）
              if(ds_nodeUniqs!=null) {
                  try {
                      while(ds_nodeUniqs.next()){
                          nodeUniques += "'" +ds_nodeUniqs.getString("UniqueID") + "',";
                      }
                } catch (Exception e) {
                    e.printStackTrace();
                }
              }
              if (!TextUtils.isEmpty(nodeUniques) && nodeUniques.endsWith(",")){
                  nodeUniques = nodeUniques.substring(0, nodeUniques.length()-1);
              }

              sb_sql.append(" SELECT SUBSTRING(CONVERT(VARCHAR(20), "+timecolumn+", 20), 1, "+timelength+") AS DATETIME , [UniqueID] , [Channel] , [SensorNo] , MAX("+datacolumn+") AS DATA " +
              		"FROM ( SELECT  "+timecolumn+" ,UniqueID , Channel ,SensorNo ,"+datacolumn+" FROM  "+tablename+" WHERE   "+timecolumn+" > '"+begintime+"' AND "+timecolumn+" < '"+endtime+"' AND UniqueID IN ("+nodeUniques+") ) ResultData1 ");
              
              sb_sql.append(" GROUP BY SUBSTRING(CONVERT(VARCHAR(20), "+timecolumn+" , 20) , 1 , "+timelength+"),[UniqueID] , [Channel] , [SensorNo] ) [ResultData2] JOIN [Node] ON [ResultData2].UniqueID = [Node].UniqueID JOIN [Sensor] ON [Sensor].SensorNo = [ResultData2].SensorNo ) [ResultData3] ");
              //用于转置
              sb_sql.append("PIVOT ( MAX([ResultData3].DATA) FOR [ResultData3].[SensorList] IN ( "+sb_columns2.toString()+" ) ) [SensorColumn]");
              sb_sql.insert(0, "SELECT  * , ROW_NUMBER() OVER ( ORDER BY " + orderby + " ) AS [RANK] FROM (");
              sb_sql.append(" ) AS [Table] WHERE 1=1");
              //筛选出符合该节点编号的数据
              if (!TextUtils.isEmpty(nodenolist)) {
                  sb_sql.append(" AND 节点编号 IN ("+nodenolist+")");
              }
              
            //分页
              if (beginindex != -1 && endindex != -1) {
                  sb_sql.insert(0, "SELECT * FROM (");
                  sb_sql.append(" ) AS [Table] WHERE 1=1");
                  sb_sql.append(" AND [Table].[RANK] >= "+beginindex+" AND [Table].[RANK] <" + endindex);
              }
              ds = DbHelperSQL.Query(sb_sql.toString());
        }

        return ds;
    }
    
    
    public ResultSet GetNodeUnqiuesByNodeNos(String nodenos) {
        StringBuilder strSql = new StringBuilder();
        strSql.append("select UniqueID");
        strSql.append(" FROM Node ");
        strSql.append("where NodeNo in (" + nodenos + ")");
        return DbHelperSQL.Query(strSql.toString());
    }
    
}