
package com.ssiot.donghai;

import android.util.Log;

import java.sql.*;

public class ConSQL{
    private static final String tag = "ConSQL";
    Statement stmt;
    Connection con;

    public boolean ConnectSQl() {
//        String JDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";// SQL数据库引擎
//        String connectDB = "jdbc:microsoft:sqlserver://ssiot2014.sqlserver.rds.aliyuncs.com:3433;databasename=iot2014";// 数据源
        String JDriver = "net.sourceforge.jtds.jdbc.Driver";
        String connectDB = "jdbc:jtds:sqlserver://ssiot2014.sqlserver.rds.aliyuncs.com:3433/iot2014;loginTimeout=9;socketTimeout=9";
        try {
            Class.forName(JDriver);// 加载数据库引擎，返回给定字符串名的类
        } catch (ClassNotFoundException e){
            e.printStackTrace();
            System.out.println("加载数据库引擎失败");
            Log.v(tag, "######-------------###########");
            System.exit(0);
        } catch (Exception e) {
            Log.e(tag, "-------------error");
            e.printStackTrace();
            System.exit(0);
            return false;
        }
        Log.v(tag, "###########################################################");
        System.out.println("加载驱动成功");

        try {
            String user = "angeliot";
            String password = "1qaz_PL";
            con = DriverManager.getConnection(connectDB, user, password);// 连接数据库对象 //TODO 超时！！
            System.out.println("连接数据库成功");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /****
     * 查询
     */
    public void selete() {
        int index = 1;
        try {
            stmt = (Statement) con.createStatement();// 创建SQL命令对象
            String sql = "select * from order";// 查询order表语句
            String sql2 = "select * from tbl_User";
            String sql3 = "SELECT * FROM SYSOBJECTS WHERE XTYPE = 'U'";//查询所有表名
            String sql4 = "select * from Setting";
            String sql5 = "select * from information_schema.columns where table_name='tbl_User'";//查看表结构 wrong
            String sql6 = "sp_help tbl_User";
            String sql7 = "sp_helpdb";//列举数据库, 结果 iot2014 master msdb tempdb
            String sql8 = "select * from tbl_User where Account=" + "'tscmy'" + "order by Account";
            stmt.setQueryTimeout(5);
            ResultSet rs = stmt.executeQuery(sql8);// 执行查询
            StringBuilder str = new StringBuilder();
            int m = rs.getMetaData().getColumnCount();
            String tmpColumeName = "";
            for (int k = 1; k <= m ;k ++){
                tmpColumeName += rs.getMetaData().getColumnName(k) + "\t";
            }
            Log.v(tag, "~~~~~~~~~~~~~~columName" + tmpColumeName);
            Log.v(tag, "((((((((((((((((((((((((((((((((((((((");
            while (rs.next()) {
                for(int i=1;i<=m;i++){
                    str.append(rs.getString(i) + "\t");
                }
                str.append("\n");
//                Log.v(tag, ""+index +"-----"+rs.getString(1));
                index ++;
            }
            Log.v(tag, str.toString());
            Log.v(tag, "))))))))))))))))))))))))))))))))))))))");
            // mSetText(str.toString());

            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            Log.e(tag, "----------------select() error");
            e.printStackTrace();
        }
    }

    /**
     * 注入信息 fhwd:发货网点，shwd:收货网点，ydh:运单号，kh：卡号，
     * dshk:代收货款,fhname:发货人,fhphone:发货电话，shname:收货人，shphone:收货电话
     * yf:运费；yhf:运货费，hwname：货物名称，bz：包装件，bs:保值，hdan:回单标记
     */
    public void insert(String fhwd, String shwd, String ydh, String kh, String dshk, String fhname,
            String fhphone, String shname, Number shphone
            , String yf, String yhf, String hwname, String bz, String num, String bs, int hdan) {

        String sql = "INSERT INTO order VALUES(fhwd,shwd,ydh,kh,dshk,fhname,fhphone,shname,shphone,yf,yhf,hwname,bz,num,bs,hdan)";
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("数据库注入失败！");
            e.printStackTrace();
        }
    }

    public void insert(String name) {
        try {
            System.out.println("name:" + name);
            stmt.executeUpdate(name);
        } catch (SQLException e) {
            System.out.println("数据库注入失败！");
            e.printStackTrace();
        }
    }
    
    public String getUniqueIDFromDB(String name, String password){
        try {
            stmt = (Statement) con.createStatement();
            String sqlcmd = "select * from tbl_User where Account='" + name + "'";
            Log.v(tag, "---------cmd:" + sqlcmd + "-----");
            stmt.setQueryTimeout(5);
            ResultSet rs = stmt.executeQuery(sqlcmd);
            StringBuilder str = new StringBuilder();
            int columnCount = rs.getMetaData().getColumnCount();
            String tmpColumnName = "";
            for (int k = 1; k <= columnCount ;k ++){
                tmpColumnName += rs.getMetaData().getColumnName(k) + "\t";
            }
            Log.v(tag, "~~~~~~~~~~~~~~columnName" + tmpColumnName);
            Log.v(tag, "((((((((((((((((((((((((((((((((((((((");
            while (rs.next()) {
                for(int i=1;i<=columnCount;i++){
                    str.append(rs.getString(i) + "\t");
                    
                }
                
                String passwordInServer = rs.getString("UserPassword").trim();//服务器数据库中有空格！！！！！
                Log.v(tag, "---------username"+ name + " localpass:" + password + " netpass:" + passwordInServer+":end");
                if (null != password && password.equals(passwordInServer)){
                    Log.v(tag, "~~~~~equals~~~~~~");
                    return rs.getString("UniqueID");
                }
                str.append("\n");
            }
            Log.v(tag, str.toString());
            Log.v(tag, "))))))))))))))))))))))))))))))))))))))");

            rs.close();
            stmt.close();
            con.close();
        } catch (Exception e) {
            Log.e(tag, "----------------select() error");
            e.printStackTrace();
        }
        return "";
    }
}
