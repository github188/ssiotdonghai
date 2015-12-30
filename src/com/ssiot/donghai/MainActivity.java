
package com.ssiot.donghai;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.Build;
import android.preference.PreferenceManager;

import com.ssiot.donghai.MainFragment.FMainBtnClickListener;
import com.ssiot.donghai.SettingFrag.FSettingBtnClickListener;
import com.ssiot.donghai.control.ControlNodeListFrag;
import com.ssiot.donghai.data.AjaxGetNodesDataByUserkey;
import com.ssiot.donghai.data.NodeHelper;
import com.ssiot.donghai.monitor.HeaderTabFrag;
import com.ssiot.donghai.monitor.MoniNodeListFrag2;
import com.ssiot.donghai.myzxing.MipcaActivityCapture;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends ActionBarActivity implements FMainBtnClickListener ,FSettingBtnClickListener{
    private static final String tag = "SSIOT-Main";
    public final static int REQUEST_CODE_SCAN = 1;
    private final static String TAG_MONITOR = "tag_monitor";
    private final static String TAG_VIDEO = "tag_video";
    private final static String TAG_HISTORY = "tag_history";
    private final static String TAG_HISTORY_DETAIL = "tag_history_detail";
    
    private final static String TAG_HEADER_TAB = "tag_header_tab";
    private final static String TAG_CONTROL = "tag_control";
    private final static String TAG_EXPERT = "tag_expert";
    private final static String TAG_INFO = "tag_info";
    private final static String TAG_SETTING = "tag_setting";
    
    
    public static String mUniqueID = "";
    private UpdateManager mUpdaManager;
    private Notification mNoti;
    private SharedPreferences mPref;
    
    public static int AreaID= -1;
    private MyCache mCache;
    
    public static final int MSG_GETVERSION_END = 1;
    public static final int MSG_DOWNLOADING_PREOGRESS = 2;
    public static final int MSG_DOWNLOAD_FINISH = 3;
    public static final int MSG_SHOWERROR = 4;
    public static final int MSG_DOWNLOAD_CANCEL = 5;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_GETVERSION_END:
                    if (msg.arg1 <= 0){//大多是网络问题
                        Intent i = new Intent(SettingFrag.ACTION_SSIOT_UPDATE);
                        i.putExtra("checkresult", 0);
                        sendBroadcast(i);
                    } else if (msg.arg1 > msg.arg2){//remoteversion > curVersion
                        HashMap<String, String> mVerMap = (HashMap<String, String>) msg.obj;
                        showUpdateChoseDialog(mVerMap);
                        
                        Intent i = new Intent(SettingFrag.ACTION_SSIOT_UPDATE);
                        i.putExtra("checkresult", 1);
                        sendBroadcast(i);
                    } else if (msg.arg1 == msg.arg2){
                        Intent i = new Intent(SettingFrag.ACTION_SSIOT_UPDATE);
                        i.putExtra("checkresult", 2);
                        sendBroadcast(i);
                    }
                    break;
                case MSG_DOWNLOADING_PREOGRESS:
                    Log.v(tag, "-------PREOGRESS----" +msg.arg1 + " " + (null != mNoti));
                    int pro = msg.arg1;
                    if (null != mNoti){
                        NotificationManager mnotiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                        mNoti.contentView.setProgressBar(R.id.noti_progress, 100, pro, false);
//                        mNoti.contentView.setTextViewText(R.id.noti_text, "" + pro);
                        mNoti.setLatestEventInfo(MainActivity.this, "正在更新", "已下载：" + pro + "%", 
                                PendingIntent.getActivity(MainActivity.this, -1, new Intent(""), 0));
                        mnotiManager.notify(UpdateManager.NOTIFICATION_FLAG, mNoti);
                    }
                    break;
                case MSG_DOWNLOAD_FINISH:
                    NotificationManager mnotiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mnotiManager.cancel(UpdateManager.NOTIFICATION_FLAG);
                    mUpdaManager.installApk();
                    break;
                case MSG_SHOWERROR:
                    NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mManager.cancel(UpdateManager.NOTIFICATION_FLAG);
                    Toast.makeText(MainActivity.this, "下载出现错误", Toast.LENGTH_LONG).show();
                    break;

                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        mCache = new MyCache(this);
        Bundle b = getIntent().getExtras();
        if (null != b){
            mUniqueID = b.getString("userkey");
            Log.v(tag, "------------mUniqueID:" + mUniqueID);
        }
        if (savedInstanceState == null){
            Log.v(tag, "--------------------savedInstanceState == null");
        }
        if (savedInstanceState == null) {//默认的savedInstanceState会存储一些数据，包括Fragment的实例
            MainFragment mMainFragment = new MainFragment();
            Log.v(tag, "---------------fragcount:"+getSupportFragmentManager().getBackStackEntryCount());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mMainFragment)
                    .commit();
//            mMainFragment.setClickListener(mfMainBtnClickListener);
        }
        
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (mPref.getBoolean(Utils.PREF_AUTOUPDATE, true) == true){
            mUpdaManager = new UpdateManager(this, mHandler);
            mUpdaManager.startGetRemoteVer();
        }
//        testsql();
//        Utils.changePic2(getApplicationContext());
        test2();
    }
    
    public MyCache getCaheManager(){
        return mCache;
    }
    
    public void test2(){
        new Thread(new Runnable() {
            @Override
            public void run() {
//                new NodeHelper().GetLastDataByNodenolist(0, 1, "12", "1", "100");
//                new NodeHelper().GetLastDataByNodenolist(126, 0, "119", "1", "100");
//                new AjaxGetNodesDataByUserkey().GetMapDataByUserkey("67873e4a-aca2-45dc-a2ba-70340500");
            }
        }).start();
    }
    
    private void testsql(){
//        try {
//            new InfotmDataBase().getValueFromDB("", "sss", "xxx");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ConSQL conS = new ConSQL();
                conS.ConnectSQl();
                conS.selete();
            }
        }).start();
    }
    
    public String getUnique(){
        return mUniqueID;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(tag, "------onActivityResult----" + requestCode +resultCode);
        switch (requestCode) {
            case REQUEST_CODE_SCAN:
                if (resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    Log.v(tag, "-------"+bundle.getString("result"));
                    Bitmap bitmap = (Bitmap) data.getParcelableExtra("bitmap");
                    
                    FragmentTransaction mTransaction = getSupportFragmentManager().beginTransaction();
                    HistoryDetailFragment hisDetailFragment = new HistoryDetailFragment();
                    mTransaction.replace(R.id.container, hisDetailFragment, TAG_HISTORY_DETAIL);
                    Bundle bundle2 = new Bundle();
                    bundle2.putString("title", bundle.getString("result"));
                    hisDetailFragment.setArguments(bundle);
                    mTransaction.addToBackStack(null);
                    mTransaction.commit();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_logout:
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                Editor e = mPref.edit();
                e.putString("password", "");
                e.commit();
                return true;
            case android.R.id.home:
                View v = getWindow().peekDecorView();
                if (v != null){
                    InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputmanger.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                super.onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
//        FragmentTransaction mTransaction = getSupportFragmentManager().beginTransaction();
//        HistoryFragment tmpFragment = (HistoryFragment) getSupportFragmentManager().findFragmentByTag(TAG_HISTORY);
//        if (null != tmpFragment && tmpFragment.isVisible() && tmpFragment.canGoback()){
//            tmpFragment.onMyBackPressed();
//        } else {
//            super.onBackPressed();
//        }
        
        List<Fragment> frags = getSupportFragmentManager().getFragments();
        for(Fragment f : frags){
            if (f != null && f.isVisible()){
                if (f instanceof BaseFragment && ((BaseFragment) f).canGoback()){
                    ((BaseFragment)f).onMyBackPressed();
                    return;
                }
            }
        }
        super.onBackPressed();
        
    }

    @Override
    public void onFMainBtnClick(String itmTxt) {

        FragmentTransaction mTransaction = getSupportFragmentManager().beginTransaction();
        if (itmTxt.equals(getResources().getString(R.string.iconstr_monitor))){
            HeaderTabFrag monitorFragment = new HeaderTabFrag();
//            MoniNodeListFrag2 monitorFragment = new MoniNodeListFrag2();//东海直接显示
            mTransaction.replace(R.id.container, monitorFragment, TAG_HEADER_TAB);
            Bundle bundle = new Bundle();
            bundle.putString("uniqueid", mUniqueID);
            bundle.putInt("defaulttab", 1);
            monitorFragment.setArguments(bundle);
            mTransaction.addToBackStack(null);
            mTransaction.commit();
        } else if (itmTxt.equals(getResources().getString(R.string.iconstr_control))){
            Toast.makeText(MainActivity.this, "正在开发中", Toast.LENGTH_SHORT).show();
            /*
            HeaderTabFrag controlFragment = new HeaderTabFrag();
            mTransaction.replace(R.id.container, controlFragment, TAG_CONTROL);
            Bundle bundle = new Bundle();
            bundle.putString("uniqueid", mUniqueID);
            bundle.putInt("defaulttab", 2);
            controlFragment.setArguments(bundle);
            mTransaction.addToBackStack(null);
            mTransaction.commit();*/
        } else if (itmTxt.equals(getResources().getString(R.string.iconstr_history))){
            Log.v(tag, "---------------------history");
//            Intent intent = new Intent();
//            intent.setClass(MainActivity.this, MipcaActivityCapture.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivityForResult(intent, REQUEST_CODE_SCAN);
            Toast.makeText(MainActivity.this, "正在开发中", Toast.LENGTH_SHORT).show();
            /*HistoryFragment mHisFragment = new HistoryFragment();
            mTransaction.replace(R.id.container, mHisFragment, TAG_HISTORY);
            Bundle bundle = new Bundle();
            bundle.putString("uniqueid", mUniqueID);
            mHisFragment.setArguments(bundle);
            mTransaction.addToBackStack(null);
            mTransaction.commit();*/
        } else if (itmTxt.equals(getResources().getString(R.string.iconstr_video))){
            if (!Utils.isNetworkConnected(MainActivity.this)){
                Toast.makeText(MainActivity.this, R.string.please_check_net, Toast.LENGTH_SHORT).show();
                return;
            }
            VideoFragment videoFragment = new VideoFragment();
            mTransaction.replace(R.id.container, videoFragment, TAG_VIDEO);
            Bundle bundle = new Bundle();
            bundle.putString("title", "ttttteeeeesssst");
            videoFragment.setArguments(bundle);
            mTransaction.addToBackStack(null);
            mTransaction.commit();
        } else if (itmTxt.equals(getResources().getString(R.string.iconstr_expert))){
            
            ExpertFragment expertFragment = new ExpertFragment();
            mTransaction.replace(R.id.container, expertFragment, TAG_EXPERT);
            Bundle bundle = new Bundle();
            bundle.putString("title", "ttttteeeeesssst");
            expertFragment.setArguments(bundle);
            mTransaction.addToBackStack(null);
            mTransaction.commit();
        } else if (itmTxt.equals(getResources().getString(R.string.iconstr_info))){
            InfoFragment infoFragment = new InfoFragment();
            mTransaction.replace(R.id.container, infoFragment, TAG_INFO);
            Bundle bundle = new Bundle();
            bundle.putString("title", "ttttteeeeesssst");
            infoFragment.setArguments(bundle);
            mTransaction.addToBackStack(null);
            mTransaction.commit();
        } else if (itmTxt.equals("setting")){//设置界面
            SettingFrag settingFragment = new SettingFrag();
            mTransaction.replace(R.id.container, settingFragment, TAG_SETTING);
//            Bundle bundle = new Bundle();
//            bundle.putString("title", "ttttteeeeesssst");
//            settingFragment.setArguments(bundle);
            mTransaction.addToBackStack(null);
            mTransaction.commit();
        }
    }
    
    private void showUpdateChoseDialog(HashMap<String, String> mVerMap){
        final HashMap<String, String> tmpMap = mVerMap;
        AlertDialog.Builder builder =new Builder(this);
        builder.setTitle(R.string.soft_update_title);
        builder.setMessage(R.string.soft_update_info);
        builder.setPositiveButton(R.string.soft_update_updatebtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNoti = mUpdaManager.showNotification(MainActivity.this);
//                        .setProgressBar(R.id.noti_progress, 100, 0, false);
                mUpdaManager.startDownLoad(tmpMap);
//                showDownloadDialog(tmpMap);
                dialog.dismiss();
                Editor e = mPref.edit();
                e.putBoolean(Utils.PREF_AUTOUPDATE, true);
                e.commit();
                Toast.makeText(MainActivity.this, "转向后台下载，可在通知栏中查看进度。", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.soft_update_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Editor e = mPref.edit();
                e.putBoolean(Utils.PREF_AUTOUPDATE, false);
                e.commit();
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    @Override
    public void onFSettingBtnClick() {
        if (mUpdaManager == null){
            mUpdaManager = new UpdateManager(MainActivity.this, mHandler);
        }
        mUpdaManager.startGetRemoteVer();
    }
}
