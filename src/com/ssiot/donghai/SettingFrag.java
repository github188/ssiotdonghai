package com.ssiot.donghai;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;

public class SettingFrag extends Fragment{
    public static final String tag = "SettingFragment";
    private FSettingBtnClickListener mFSettingBtnClickListener;
    
    private TextView mVersionTextView;
    private TextView mVerStatusView;
    HashMap<String, String> mHashMap;
    private int remoteVersion = 0;
    
    private String showText = "";
    private static final int MSG_GETVERSION_END = 1;
    private static final int MSG_PROGRESS_UPDATE = 2;
    private static final int MSG_DOWNLOAD_FINISH = 3;
    private static final int MSG_SHOWTEXT = 4;
//    private Handler mHandler = new Handler(){
//        public void handleMessage(android.os.Message msg) {
//            switch (msg.what) {
//                case MSG_GETVERSION_END:
//                    if (remoteVersion == 0){
//                        Toast.makeText(getActivity(), "未获取到最新版本信息", Toast.LENGTH_SHORT).show();
//                        
//                    } else if (remoteVersion > getCurVersionCode()){
//                        if (null != mVerStatusView){
//                            mVerStatusView.setText("有新版本");
//                        }
//                        showNoticeDialog();
//                    } else if (remoteVersion == getCurVersionCode()){
//                        if (null != mVerStatusView){
//                            mVerStatusView.setText("已是最新");
//                        }
//                    } else {
//                        Toast.makeText(getActivity(), "版本错误", Toast.LENGTH_SHORT).show();
//                    }
//                        
//                    break;
//                case MSG_PROGRESS_UPDATE:
//                    break;
//                case MSG_DOWNLOAD_FINISH:
//                    break;
//                case MSG_SHOWTEXT:
//                    Toast.makeText(getActivity(), showText, Toast.LENGTH_SHORT).show();
//                    break;
//                default:
//                    break;
//            }
//        };
//    };
    
    public static final String ACTION_SSIOT_UPDATE = "com.ssiot.donghai.update";
    BroadcastReceiver updateBroadcastReceiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent) {
            int checkRet = intent.getIntExtra("checkresult", -1);
            Log.v(tag, "----------updateBroadcastReceiver---" + checkRet);
            switch (checkRet) {
                case 0:
                    mVerStatusView.setText("未获取到最新版本信息");
                    break;
                case 1:
                    mVerStatusView.setText("有新版本");
                    break;
                case 2:
                    mVerStatusView.setText("已是最新");
                    break;

                default:
                    break;
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mFSettingBtnClickListener = (FSettingBtnClickListener) getActivity();
        getActivity().registerReceiver(updateBroadcastReceiver, new IntentFilter(ACTION_SSIOT_UPDATE));
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View v = inflater.inflate(R.layout.fragment_setting, container, false);
        mVersionTextView = (TextView) v.findViewById(R.id.app_version);
        mVerStatusView = (TextView) v.findViewById(R.id.app_version_status);
        String text = getActivity().getResources().getString(R.string.app_name) + getCurVersionName(getActivity());
        mVersionTextView.setText(text);
        Button b = (Button) v.findViewById(R.id.checkupdate);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Utils.isNetworkConnected(getActivity())){
                    Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_SHORT).show();
                }
                mVerStatusView.setText("");
                if (UpdateManager.updating){
                    Toast.makeText(getActivity(), "更新正在运行,请等待。", Toast.LENGTH_SHORT).show();
                } else {
                    if (null != mFSettingBtnClickListener){
                        mFSettingBtnClickListener.onFSettingBtnClick();
                    }
                }
            }
        });
        return v;
    }
    
    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(updateBroadcastReceiver);
        super.onDestroyView();
    }
    
    private String getCurVersionName(Context c){
        String versionName = "";
        try {
            versionName = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return versionName;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.Setting, menu);
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
    
    public void setClickListener(FSettingBtnClickListener listen){
        mFSettingBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FSettingBtnClickListener {  
        void onFSettingBtnClick();  
    }
}