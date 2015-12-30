package com.ssiot.donghai.control;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ssiot.donghai.R;

import java.util.ArrayList;
import java.util.List;

public class ControlDeviceGridAdapter extends ArrayAdapter<DeviceCheckerData> {
    private final static String tag = "ControlDeviceGridAdapter";
    private LayoutInflater mInflater;
    ArrayList<DeviceCheckerData> mDevices = new ArrayList<DeviceCheckerData>();
    private ArrayList<String> selectedList;

    public ControlDeviceGridAdapter(Context context, ArrayList<DeviceCheckerData> objects) {
        super(context,0, objects);
        mInflater = LayoutInflater.from(context);
        mDevices = objects;
        Log.v(tag, "-------devicecount " + objects.size());
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView){
            convertView = mInflater.inflate(R.layout.control_device_item, parent, false);
        }
        CheckBox cBox = (CheckBox) convertView.findViewById(R.id.grid_cb);
        TextView tv = (TextView) convertView.findViewById(R.id.grid_text);
        final int positionfinal = position;
        tv.setText(mDevices.get(position).deviceName);
        if (mDevices.get(position).isChecked){
            cBox.setChecked(true);
        } else {
            cBox.setChecked(false);
        }
        cBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDevices.get(positionfinal).isChecked = isChecked;
            }
        });
        return convertView;
    }
    
    public String getSelectedListStr(){
        String str = "";
        for (DeviceCheckerData d : mDevices){
            if (d.isChecked){
                str += d.deviceNo + ",";
            }
        }
        if (str.endsWith(",")){
            str = str.substring(0, str.length());
        }
        return str;
    }
}