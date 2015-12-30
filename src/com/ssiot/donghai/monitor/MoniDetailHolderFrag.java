package com.ssiot.donghai.monitor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import com.ssiot.donghai.R;

import java.lang.reflect.Field;

public class MoniDetailHolderFrag extends Fragment{
    public static final String tag = "MoniDetailHolderFragment";
    private FMoniDetailHolderBtnClickListener mFMoniDetailHolderBtnClickListener;
    private FragmentManager moniChildFragmentManager;
    RadioGroup rgBottom;
    private String mTitleText;
    private boolean mStatus;
    private boolean mNetType;
    Bundle mBundle;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mBundle = getArguments();
        if (mBundle != null){
            mTitleText = mBundle.getString("nodetitle");
            mStatus = mBundle.getBoolean("status", false);
            mNetType = mBundle.getBoolean("isgprs", false);
            Log.e(tag, "----onCreate----getArguments = nodetitle:"+mTitleText);
        } else {
            Log.e(tag, "----onCreate----getArguments = null");
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_moni_detail_holder, container, false);
        moniChildFragmentManager = getChildFragmentManager();
        rgBottom = (RadioGroup) v.findViewById(R.id.bottom_tab);
        rgBottom.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
            @Override  
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FragmentTransaction transaction = moniChildFragmentManager.beginTransaction();
                Fragment fragment = getInstanceByIndex(checkedId);
                transaction.replace(R.id.moni_detail_holder_content, fragment);
                transaction.commit();
            }
        });
        rgBottom.check(R.id.radio_moni_data);
        return v;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.monidetail_holder, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }
    
    @Override
    public void onDetach() {//http://www.tuicool.com/articles/FVj6rq 
        //http://blog.csdn.net/primer_programer/article/details/27184877
        //android fragment嵌套fragment出现的问题：no activity
        //不知是否有效 
        super.onDetach();/*
        try {
            // 参数是固定写法
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }*/
    }
    
    public void setClickListener(FMoniDetailHolderBtnClickListener listen){
        mFMoniDetailHolderBtnClickListener = listen;
    }
    
    public interface FMoniDetailHolderBtnClickListener {  
        void onFMoniDetailHolderBtnClick();  
    }
    
    private Fragment getInstanceByIndex(int index) {
        Fragment fragment = null;
        switch (index) {
            case R.id.radio_moni_data:
                fragment = new MoniDataFrag();
                fragment.setArguments(mBundle);
                break;
            case R.id.radio_moni_chart:
                fragment = new MoniChartFrag();
                fragment.setArguments(mBundle);
                break;
            case R.id.radio_moni_calibration:
                fragment = new MoniCalibrationFrag();
                fragment.setArguments(mBundle);
                break;
            case R.id.radio_moni_configure:
                fragment = new MoniConfigFrag();
                fragment.setArguments(mBundle);
                break;
            case R.id.radio_moni_alarm:
                fragment = new MoniAlarmFrag();
                fragment.setArguments(mBundle);
                break;
            default:
                fragment = new MoniDataFrag();
                fragment.setArguments(mBundle);
                break;
        }
        return fragment;
    }
}