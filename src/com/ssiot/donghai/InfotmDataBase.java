
package com.ssiot.donghai;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import android.database.SQLException;

public class InfotmDataBase {
    private static final String tag = "InfotmDataBase";
    private static final String COMMIT_COMMAND = "UPDATE %s SET TestResult='1',CodeBurningResult='%d' WHERE %s='%s'";
    private static final String SELECT_COMMAND = "SELECT * FROM %s WHERE %s='%s'";
    private static final String UPDATE_BURN_COMMAND = "UPDATE %s SET CodeBurningResult='1' WHERE %s='%s'";
    private static final String UPDATE_TEST_COMMAND = "UPDATE %s SET TestResult='1' WHERE %s='%s'";
    private static final String QUERY_COMMAND = "SELECT %sCodeBurningResult FROM %s WHERE %s='%s'";
    private static final String TAG = "INFOTM_DATABASE";

    private Connection mConnection;
    private DataBaseConfig dbC;

    static {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
        } catch (Exception e) {
            Log.e(TAG, "load the driver failed");
        }
    }

    public InfotmDataBase(DataBaseConfig c) {
        this.dbC = c;
    }

    public InfotmDataBase() {
        dbC = new DataBaseConfig(dbserver, dbname, dbtable, dbaccount, dbpassword);
    }

    private Connection getConnect() throws java.sql.SQLException {
        if (mConnection == null) {
//            String connString = String.format("jdbc:jtds:sqlserver://%s/%s", this.dbC.dbserver,
//                    this.dbC.dbname);
            String connString = String.format("%s/%s", this.dbC.dbserver,
                    this.dbC.dbname);
            Log.v(tag, "=======================connString:"+connString);
            int retry = 0;
            while (retry < 3) {
                try {
                    this.mConnection = DriverManager.getConnection(connString, this.dbC.dbaccount,
                            this.dbC.dbpassword);
                    Log.d(TAG, "DataBase connection success!");
                    break;
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw, true));
                    String str = sw.toString();
                    Log.e(TAG, "==========================retry " + retry
                            + "===================================");
                    Log.e(TAG, str);
                } catch (Exception e) {
                    e.printStackTrace();
                    StringWriter sw1 = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw1, true));
                    String str1 = sw1.toString();
                    Log.e(TAG, "==========================retry " + retry
                            + "===================================");
                    Log.e(TAG, str1);
                }
                retry++;
            }
        }
        return this.mConnection;
    }

    public String getValueFromDB(String outKey, String inKey, String value)
            throws java.sql.SQLException {
        String getKey = null;
        try {
            String sql = String.format(SELECT_COMMAND, this.dbC.dbtable, inKey, value);
            getConnect();
            if (mConnection == null)
                return null;
            Statement stmt = mConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                getKey = rs.getString(outKey);
                if (getKey != null) {
                    Log.i(TAG, "Get the value " + outKey + "=" + getKey);
                } else {
                    Log.i(TAG, "Get the value " + outKey + "=NULL");
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage().toString());
        }
        return getKey;
    }

    public boolean updateBurnToDB(String inKey, String value)
            throws java.sql.SQLException {
        try {
            String sql = String.format(UPDATE_BURN_COMMAND, this.dbC.dbtable, inKey, value);
            getConnect();
            if (mConnection == null)
                return false;
            Statement stmt = mConnection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage().toString());
        }
        return false;
    }

    public boolean updateTestToDB(String inKey, String value)
            throws java.sql.SQLException {
        try {
            String sql = String.format(UPDATE_TEST_COMMAND, this.dbC.dbtable, inKey, value);
            getConnect();
            if (mConnection == null)
                return false;
            Statement stmt = mConnection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage().toString());
        }
        return false;
    }

    public boolean closeDB()
            throws java.sql.SQLException {
        try {
            if (mConnection != null) {
                mConnection.close();
                mConnection = null;
            }
            return true;
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage().toString());
        }
        return false;
    }

    public class DataBaseConfig {
        public String dbserver;
        public String dbname;
        public String dbtable;
        public String dbaccount;
        public String dbpassword;

        public DataBaseConfig(String s, String n, String t, String a, String p) {
            dbserver = s;
            dbname = n;
            dbtable = t;
            dbaccount = a;
            dbpassword = p;
        }
    }

    private String dbserver = "ssiot2014.sqlserver.rds.aliyuncs.com:3433";
    private String dbname = "iot2014";
    private String dbtable = "dbo.ysten";
    private String dbaccount = "angeliot";
    private String dbpassword = "1qaz_PL";
}
