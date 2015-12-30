package com.ssiot.donghai.monitor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ssiot.donghai.BaseFragment;
import com.ssiot.donghai.R;

public class MoniAlarmFrag extends BaseFragment{
    public static final String tag = "AlarmFragment";
    private FAlarmBtnClickListener mFAlarmBtnClickListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_moni_alarm, container, false);
        return v;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.Alarm, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                Log.v(tag, "----------------action-settting");
//                break;
//
//            default:
//                break;
//        }
        return true;
    }
    
    public void setClickListener(FAlarmBtnClickListener listen){
        mFAlarmBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FAlarmBtnClickListener {  
        void onFAlarmBtnClick();  
    }
}