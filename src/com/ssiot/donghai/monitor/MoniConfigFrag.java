package com.ssiot.donghai.monitor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ssiot.donghai.BaseFragment;
import com.ssiot.donghai.R;
import com.ssiot.donghai.data.AjaxGetNodesDataByUserkey;
import com.ssiot.donghai.data.model.SettingInfo1Model;

import java.util.ArrayList;
import java.util.List;

public class MoniConfigFrag extends BaseFragment{
    public static final String tag = "ConfigFragment";
    private FConfigBtnClickListener mFConfigBtnClickListener;
    
    private Bundle mBundle;
    private int nodeno = -1;
    AjaxGetNodesDataByUserkey.LoadSetting loadSetting;
    int reportId =  4;
    
    private EditText mNameEdit;
    private TextView mIDTextView;
    private Spinner mSpinner;
    private EditText mLontiEdit;
    private EditText mLatiEdit;
    private EditText mRemarkEdit;
    private Button mBtn;
    
    private static final int MSG_GETREPORTF_END = 0;
    private static final int MSG_SEND_REPORT_FREQ_END = 1;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if (!isVisible()){
                Log.e(tag, "-----not visible: msg:" + msg.what);
                return;
            }
            switch (msg.what) {
                case MSG_GETREPORTF_END:
                    if (null != loadSetting){
                        initUI();
                    }
                    break;
                case MSG_SEND_REPORT_FREQ_END:
                    boolean b = (Boolean) msg.obj;
                    if (b){
                        mBtn.setText("发送成功");
                    } else {
                        mBtn.setText("发送失败");
                    }
                    break;

                default:
                    break;
            }
        };
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mBundle = getArguments();
        nodeno = mBundle.getInt("nodeno", -1);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_moni_conf, container, false);
        mNameEdit = (EditText) v.findViewById(R.id.conf_name);
        mIDTextView = (TextView) v.findViewById(R.id.conf_id);
        mSpinner = (Spinner) v.findViewById(R.id.conf_freq);
        mLontiEdit = (EditText) v.findViewById(R.id.conf_longti);
        mLatiEdit = (EditText) v.findViewById(R.id.conf_lati);
        mRemarkEdit = (EditText) v.findViewById(R.id.conf_remark);
        mBtn = (Button) v.findViewById(R.id.conf_btn);
        initTitleBar(v);
        new GetReportFreqThread().start();
        
        return v;
    }
    
    private void initTitleBar(View rootView){
        TextView mTitleView = (TextView) rootView.findViewById(R.id.moni_title);
        ImageView mOnlineView = (ImageView) rootView.findViewById(R.id.moni_status);
        ImageView mNetTypeView = (ImageView) rootView.findViewById(R.id.moni_net_type);
        mTitleView.setText(mBundle.getString("nodetitle"));
        mOnlineView.setImageResource(mBundle.getBoolean("status", false) ? R.drawable.online : R.drawable.offline);
        mNetTypeView.setImageResource(mBundle.getBoolean("isgprs", false) ? R.drawable.connect_gprs : R.drawable.connect_zigbee);
    }
    
    private void initUI(){
        if (null != loadSetting){
            mNameEdit.setText(loadSetting.Name);
            mIDTextView.setText(loadSetting.UniqueID);
            final ArrayList<String> as = new ArrayList<String>();
            as.add("请选择");
            List<SettingInfo1Model> rList = loadSetting.ReportList;
            for (int i =0; i < rList.size(); i ++){
                as.add(rList.get(i)._name);
            }
            ArrayAdapter<String> arr_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,as);
            mSpinner.setAdapter(arr_adapter);
            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 1){
                        reportId = loadSetting.ReportList.get(position-1)._id;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    
                }
            });
            mLontiEdit.setText(""+loadSetting.Longtude);
            mLatiEdit.setText(""+loadSetting.latitude);
            mRemarkEdit.setText(""+loadSetting.Remark);
        }
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtn.setText("正在发送中");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String nodeName = mNameEdit.getText().toString();
                        String uniqueid = loadSetting.UniqueID;//不允许改动
                        String longitude = mLontiEdit.getText().toString();
                        String latitude = mLatiEdit.getText().toString();
                        String remark = mRemarkEdit.getText().toString();
                        boolean b = new AjaxGetNodesDataByUserkey().SendNodeReportFrequency(nodeName, uniqueid, reportId, longitude, latitude, remark);
                        Message msg = mHandler.obtainMessage(MSG_SEND_REPORT_FREQ_END);
                        msg.obj = b;
                        mHandler.sendMessage(msg);
                    }
                }).start();
                
            }
        });
    }
    
    private class GetReportFreqThread extends Thread{
        @Override
        public void run() {
            if (nodeno != -1){
                loadSetting = new AjaxGetNodesDataByUserkey().GetReportFrequency(""+nodeno);
                mHandler.sendEmptyMessage(MSG_GETREPORTF_END);
            }
            
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.Config, menu);
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
    
    public void setClickListener(FConfigBtnClickListener listen){
        mFConfigBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FConfigBtnClickListener {  
        void onFConfigBtnClick();  
    }
}