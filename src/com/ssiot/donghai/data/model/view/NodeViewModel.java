package com.ssiot.donghai.data.model.view;

import net.sourceforge.jtds.jdbc.DateTime;

import java.sql.Timestamp;

public class NodeViewModel{
    public int _nodeid;
    public String _uniqueid;
    public int _nodeno;
    public int _nodecategoryno = 0;
    public int _productid;
    public int _gatewayno;
    public int _areaid;
    public float _longitude;
    public float _latitude;
    public String _location;
    public String _color;
    public String _expression;
    public String _remark;
    public String _areaname;
    
    public String _isonline = "离线";
    public String _sensornames = "";
    
    public Timestamp _updatetime = null;//new DateTime();
}