package com.ssiot.donghai.data.model;

import java.sql.Timestamp;

public class LatestDataModel {

    public int _latestdataid;
    public Timestamp _collectiontime;
    public String _uniqueid;
    public int _channel;
    public int _sensorno;
//    public decimal _data;
    public int _islive = 0;
}
