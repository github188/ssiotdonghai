package com.ssiot.donghai.hikvision;

import android.R.integer;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.PTZCommand;
import com.hikvision.netsdk.RealPlayCallBack;
import com.ssiot.donghai.BaseFragment;
import com.ssiot.donghai.MainActivity;
import com.ssiot.donghai.R;
import com.ssiot.donghai.Utils; 
//import com.ssiot.donghai.monitor.AllMoniFrag;
import com.ssiot.donghai.data.DataAPI;
import com.ssiot.donghai.data.model.VLCVideoInfoModel;

import org.MediaPlayer.PlayM4.Player;

import java.util.ArrayList;
import java.util.List;

public class HCLiveFrag extends BaseFragment{
    private static final String tag = "HCFragment";
    private static final String TAG = "HCFragment";
    PowerManager.WakeLock mWakeLock;
    private FHCBtnClickListener mFHCBtnClickListener;
    
    private SurfaceView     m_osurfaceView          = null;
    
    private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = null;
    
    private int             m_iLogID                = -1;               // return by NET_DVR_Login_v30
    private int             m_iPlayID               = -1;               // return by NET_DVR_RealPlay_V30
    private int             m_iPlaybackID           = -1;               // return by NET_DVR_PlayBackByTime 
    
    private int             m_iPort                 = -1;               // play port
    private int             m_iStartChan            = 0;                // start channel no
    private int             m_iChanNum              = 0;                //channel number
    
    private boolean         m_bMultiPlay            = false;
    
    private boolean         m_bNeedDecode           = true;
    
    private String mStrIp = "";//221.131.87.240
    private int mPort = 8004;
    private String mStrUser = "admin";
    private String mStrPwd = "js123456";
    private String mTitle = "海康视频监控";
    
    private static final int MSG_SHOW_MSG = 1;
    private static final int MSG_SHOW_DEFAULT_MSG = 2;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SHOW_MSG:
                    Toast.makeText(getActivity(), "连接失败，请检查。", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_SHOW_DEFAULT_MSG:
                    Toast.makeText(getActivity(), "未找到句容节点，显示测试节点！", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        };
    };
    
    //TODO 为什么从allvideo进会崩溃 ，为什么进入时会执行一个ondestroy
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "-----onCreate-----");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mStrIp = getArguments().getString("videoip");
        mStrUser = getArguments().getString("videoname");
        mStrPwd = getArguments().getString("videopswd");
        mPort = getArguments().getInt("tcpport");
        mTitle = getArguments().getString("addrtitle");
        initeSdk();
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, tag);
        if (null != mWakeLock){
            mWakeLock.acquire();
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "-----onCreateView-----");
        View v = inflater.inflate(R.layout.frag_single_hc_video, container, false);    
        m_osurfaceView = (SurfaceView) v.findViewById(R.id.hc_preview);
        if (true){
            RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) m_osurfaceView.getLayoutParams();
            rl.height = Utils.dip2px(getActivity(), 240+30);//大厅view上没有title导致下面按钮大小不正常
            m_osurfaceView.setLayoutParams(rl);
        }
        
        m_osurfaceView.getHolder().addCallback(callback);
        setBigTitle(v);
        RelativeLayout rLayout = (RelativeLayout) v.findViewById(R.id.ptzroot);
        initPTZ(rLayout);
        sendShowMyDlg("正在连接");
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ret = init();//等到surfaceView显现出来才能init
                if (!ret){
                    mHandler.sendEmptyMessage(MSG_SHOW_MSG);
                }
                sendDismissDlg();
            }
        }).start();
        
        return v;
    }
    
    private void setBigTitle(View rootView){
//        RelativeLayout headerView = (RelativeLayout) rootView.findViewById(R.id.video_big_top);
//        headerView.setBackgroundResource(JuRongActivity.AREA_DRAWABLE_ID[JuRongActivity.currentArea - 1]);
        TextView t = (TextView) rootView.findViewById(R.id.big_title);
    }
    
    private VLCVideoInfoModel getChosedVideoInfo(String areaids, int index, boolean isHall){//isHall jurong大厅
        List<VLCVideoInfoModel> videoInfos = DataAPI.GetVLCVideoMapInfoByAreaIds(areaids);
        if (null != videoInfos && videoInfos.size() > 0){
            return videoInfos.get(index);
        }
        return null;
    }
    
    private boolean isInList(int[] d, String val){
        try {
            int value = Integer.parseInt(val);
            for (int i = 0; i < d.length; i ++){
                if (d[i] == value){
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
    
    private boolean init(){//jingbo add
        if (TextUtils.isEmpty(mStrIp) || TextUtils.isEmpty(mStrUser) || TextUtils.isEmpty(mStrPwd) || mPort < 1){
            mHandler.sendEmptyMessage(MSG_SHOW_DEFAULT_MSG);
            return false;
        }
        
        if(loginFunc()){
            previewFunc();
            return true;
        } else {
            return false;
        }
    }
    
    private boolean initeSdk() {
        //init net sdk
        if (!HCNetSDK.getInstance().NET_DVR_Init()) {
            Log.e(TAG, "HCNetSDK init is failed!");
            return false;
        }
        HCNetSDK.getInstance().NET_DVR_SetLogToFile(3, "/mnt/sdcard/sdklog/",true);
        return true;
    }
    
    private int loginDevice() {
        // get instance
        m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        if (null == m_oNetDvrDeviceInfoV30) {
            Log.e(TAG, "HKNetDvrDeviceInfoV30 new is failed!");
            return -1;
        }
        String strIP = mStrIp;
        int nPort = mPort;
        String strUser = mStrUser;
        String strPsd = mStrPwd;
        Log.v(tag, "------loginDevice-" +mStrIp + mPort + mStrUser + mStrPwd);
        // call NET_DVR_Login_v30 to login on, port 8000 as default
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort, strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            Log.e(TAG, "NET_DVR_Login is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return -1;
        }
        if(m_oNetDvrDeviceInfoV30.byChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byChanNum;
        }
        else if(m_oNetDvrDeviceInfoV30.byIPChanNum > 0)
        {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartDChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byIPChanNum + m_oNetDvrDeviceInfoV30.byHighDChanNum * 256;
        }
        Log.i(TAG, "NET_DVR_Login is Successful!");
        
        return iLogID;
    }
    
    private boolean loginFunc(){
        try {
            m_iLogID = -1;//jingbo add this to login
            if(m_iLogID < 0) {
                // login on the device
                m_iLogID = loginDevice();
                if (m_iLogID < 0) {
                    Log.e(TAG, "This device logins failed!");
                    return false;
                }
                // get instance of exception callback and set
                ExceptionCallBack oexceptionCbf = getExceptiongCbf();
                if (oexceptionCbf == null) {
                    Log.e(TAG, "ExceptionCallBack object is failed!");
                    return false;
                }
                
                if (!HCNetSDK.getInstance().NET_DVR_SetExceptionCallBack(oexceptionCbf)) {
                    Log.e(TAG, "NET_DVR_SetExceptionCallBack is failed!");
                    return false;
                }
                
//                m_oLoginBtn.setText("Logout");
                Log.i(TAG, "Login sucess ****************************1***************************");
            } else {
                // whether we have logout
                if (!HCNetSDK.getInstance().NET_DVR_Logout_V30(m_iLogID)) {
                    Log.e(TAG, " NET_DVR_Logout is failed!");
                    return false;
                }
//                m_oLoginBtn.setText("Login");
                m_iLogID = -1;
            }       
        } catch (Exception err) {
            Log.e(TAG, "error: " + err.toString());
            return false;
        }
        return true;
    }
    
    private void previewFunc(){
        try {
//            ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).
//            hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);              
            if(m_iLogID < 0) {
                Log.e(TAG,"please login on device first");
                return ;
            }
            if(m_bNeedDecode) {
                if(m_iChanNum > 1) {//preview more than a channel
                    /*if(!m_bMultiPlay) {
                        startMultiPreview();
                        m_bMultiPlay = true;
//                        m_oPreviewBtn.setText("Stop");
                    } else {
                        stopMultiPreview();
                        m_bMultiPlay = false;
//                        m_oPreviewBtn.setText("Preview");
                    }*/
                    Log.v(TAG, "-------------eeeeeeeeerror-----m_iChanNum:"+m_iChanNum);//jingbo delete above to test
                } else {   //preivew a channel
                    if(m_iPlayID < 0) {   
                        startSinglePreview();
                    } else {
                        stopSinglePreview();
//                        m_oPreviewBtn.setText("Preview");
                    }
                }
            } else {
                
            }                               
        } catch (Exception err) {
            Log.e(TAG, "error: " + err.toString());
        }
    }
    
    private void initPTZ(RelativeLayout m_layoutPtz){
        ImageButton m_btUp;
        ImageButton m_btDown;
        ImageButton m_btRight;
        ImageButton m_btLeft;
        ImageButton m_btLUp;
        ImageButton m_btRUp;
        ImageButton m_btLDown;
        ImageButton m_btRDown;
        ImageButton m_btMore;
        ImageButton m_btZoomA;
        ImageButton m_btZoomD;
        ImageButton m_btFocusA;
        ImageButton m_btFocusD;
        
        
        m_btUp = (ImageButton)m_layoutPtz.findViewById(R.id.btn_up);
        m_btUp.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.v(tag, "-----------onTouch----");
                return PTZControl(event, PTZCommand.TILT_UP);
            }});
            
        m_btDown = (ImageButton)m_layoutPtz.findViewById(R.id.btn_down);
        m_btDown.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, PTZCommand.TILT_DOWN);
            }});
        
        m_btLeft = (ImageButton)m_layoutPtz.findViewById(R.id.btn_left);
        m_btLeft.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, PTZCommand.PAN_LEFT);
            }});
        
        m_btRight = (ImageButton)m_layoutPtz.findViewById(R.id.btn_right);
        m_btRight.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, PTZCommand.PAN_RIGHT);
            }});
        
        m_btLUp = (ImageButton)m_layoutPtz.findViewById(R.id.btn_lup);
        m_btLUp.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, PTZCommand.UP_LEFT);
            }});
        
        m_btRUp = (ImageButton)m_layoutPtz.findViewById(R.id.btn_rup);
        m_btRUp.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, PTZCommand.UP_RIGHT);
            }});
        
        m_btLDown = (ImageButton)m_layoutPtz.findViewById(R.id.btn_ldown);
        m_btLDown.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, PTZCommand.DOWN_LEFT);
            }});
        
        m_btRDown = (ImageButton)m_layoutPtz.findViewById(R.id.btn_rdown);
        m_btRDown.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, PTZCommand.DOWN_RIGHT);
            }});
        
        
        m_btZoomA = (ImageButton)m_layoutPtz.findViewById(R.id.btn_z_add);
        m_btZoomA.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, PTZCommand.ZOOM_IN);
            }
        });
        
        m_btZoomD = (ImageButton)m_layoutPtz.findViewById(R.id.btn_z_dec);
        m_btZoomD.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, PTZCommand.ZOOM_OUT);
            }
        });
        
        m_btFocusA = (ImageButton)m_layoutPtz.findViewById(R.id.btn_f_add);
        m_btFocusA.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, PTZCommand.FOCUS_FAR);
            }
        });
        
        m_btFocusD = (ImageButton)m_layoutPtz.findViewById(R.id.btn_f_dec);
        m_btFocusD.setOnTouchListener(new  OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return PTZControl(event, PTZCommand.FOCUS_NEAR);
            }
        });
    }
    
    private void startSinglePreview() {
        if(m_iPlaybackID >= 0)
        {
            Log.i(TAG, "Please stop palyback first");
            return ;
        }
        RealPlayCallBack fRealDataCallBack = getRealPlayerCbf();
        if (fRealDataCallBack == null)
        {
            Log.e(TAG, "fRealDataCallBack object is failed!");
            return ;
        }
        Log.i(TAG, "m_iStartChan:" +m_iStartChan);
                
        NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
        previewInfo.lChannel = m_iStartChan;
        previewInfo.dwStreamType = 1; //substream
        previewInfo.bBlocked = 1;       
        // HCNetSDK start preview
        m_iPlayID = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(m_iLogID, previewInfo, fRealDataCallBack);
        if (m_iPlayID < 0)
        {
            Log.e(TAG, "NET_DVR_RealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return ;
        }
        
        Log.i(TAG, "NetSdk Play sucess ***********************3***************************");                                       
//        m_oPreviewBtn.setText("Stop");
    }
    
    private void stopSinglePreview() {
        if ( m_iPlayID < 0) {
            Log.e(TAG, "m_iPlayID < 0");
            return;
        }
        
        //  net sdk stop preview
        if (!HCNetSDK.getInstance().NET_DVR_StopRealPlay(m_iPlayID)) {
            Log.e(TAG, "StopRealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        
        m_iPlayID = -1;     
        stopSinglePlayer();
    }
    
    private void stopSinglePlayer() {
        Player.getInstance().stopSound();       
        // player stop play
        if (!Player.getInstance().stop(m_iPort)) {
            Log.e(TAG, "stop is failed!");
            return;
        }   
        
        if(!Player.getInstance().closeStream(m_iPort)) {
            Log.e(TAG, "closeStream is failed!");
            return;
        }
        if(!Player.getInstance().freePort(m_iPort)) {
            Log.e(TAG, "freePort is failed!" + m_iPort);
            return;
        }
        m_iPort = -1;
    }
    
    private boolean PTZControl(MotionEvent event , int ptzcommand){
        try {
            if(m_iLogID < 0) {
                Log.e(TAG,"please login on a device first");
                return false;
            }
            if(event.getAction()== MotionEvent.ACTION_DOWN) {
                if(!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, m_iStartChan, ptzcommand, 0)) {
                    Log.e(TAG, "start PAN_LEFT failed with error code: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                    return false;
                } else {
                    Log.i(TAG, "start PAN_LEFT succ");
                    return false;
                }
            } else if(event.getAction() == MotionEvent.ACTION_UP) {
                if(!HCNetSDK.getInstance().NET_DVR_PTZControl_Other(m_iLogID, m_iStartChan, ptzcommand, 1)) {
                    Log.e(TAG, "start PAN_LEFT failed with error code: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
                    return false;
                } else {
                    Log.i(TAG, "start PAN_LEFT succ");
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private ExceptionCallBack getExceptiongCbf() {
        ExceptionCallBack oExceptionCbf = new ExceptionCallBack()
        {
            public void fExceptionCallBack(int iType, int iUserID, int iHandle)
            {
                System.out.println("recv exception, type:" + iType);
            }
        };
        return oExceptionCbf;
    }
    
    private RealPlayCallBack getRealPlayerCbf()
    {
        RealPlayCallBack cbf = new RealPlayCallBack()
        {
             public void fRealDataCallBack(int iRealHandle, int iDataType, byte[] pDataBuffer, int iDataSize)
             {
                // player channel 1
                processRealData(1, iDataType, pDataBuffer, iDataSize, Player.STREAM_REALTIME); 
             }
        };
        return cbf;
    }
    
    public void processRealData(int iPlayViewNo, int iDataType, byte[] pDataBuffer, int iDataSize, int iStreamMode) {
        if(!m_bNeedDecode) {
        //   Log.i(TAG, "iPlayViewNo:" + iPlayViewNo + ",iDataType:" + iDataType + ",iDataSize:" + iDataSize);
        } else {
            if(HCNetSDK.NET_DVR_SYSHEAD == iDataType) {
                if(m_iPort >= 0) {
                    return;
                }                   
                m_iPort = Player.getInstance().getPort();   
                if(m_iPort == -1) {
                    Log.e(TAG, "getPort is failed with: " + Player.getInstance().getLastError(m_iPort));
                    return;
                }
                Log.i(TAG, "getPort succ with: " + m_iPort);
                if (iDataSize > 0) {
                    if (!Player.getInstance().setStreamOpenMode(m_iPort, iStreamMode)) {  //set stream mode
                        Log.e(TAG, "setStreamOpenMode failed");
                        return;
                    }
                    if (!Player.getInstance().openStream(m_iPort, pDataBuffer, iDataSize, 2*1024*1024)) { //open stream
                        Log.e(TAG, "openStream failed");
                        return;
                    }
                    if (!Player.getInstance().play(m_iPort, m_osurfaceView.getHolder())) {
                        Log.e(TAG, "play failed");
                        return;
                    }   
                    if(!Player.getInstance().playSound(m_iPort)) {
                        Log.e(TAG, "playSound failed with error code:" + Player.getInstance().getLastError(m_iPort));
                        return;
                    }
                }
            } else {
                if (!Player.getInstance().inputData(m_iPort, pDataBuffer, iDataSize)) {
//                  Log.e(TAG, "inputData failed with: " + Player.getInstance().getLastError(m_iPort));
                    for(int i = 0; i < 4000 && m_iPlaybackID >=0 ; i++) {
                        if (!Player.getInstance().inputData(m_iPort, pDataBuffer, iDataSize))
                            Log.e(TAG, "inputData failed with: " + Player.getInstance().getLastError(m_iPort));
                        else
                            break;
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }                       
                    }
                }
            }       
        }
    }
    
    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "Player setVideoWindow release!" + m_iPort);
            if (-1 == m_iPort)
            {
                return;
            }
            if (true == holder.getSurface().isValid()) {
                if (false == Player.getInstance().setVideoWindow(m_iPort, 0, null)) {   
                    Log.e(TAG, "Player setVideoWindow failed!");
                }
            }
        }
        
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            m_osurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            Log.i(TAG, "surface is created" + m_iPort); 
            if (-1 == m_iPort)
            {
                return;
            }
            Surface surface = holder.getSurface();
            if (true == surface.isValid()) {
                if (false == Player.getInstance().setVideoWindow(m_iPort, 0, holder)) { 
                    Log.e(TAG, "Player setVideoWindow failed!");
                }   
            }
        }
        
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    };
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
//        inflater.inflate(R.menu.HC, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
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
    
    public void setClickListener(FHCBtnClickListener listen){
        mFHCBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FHCBtnClickListener {  
        void onFHCBtnClick();  
    }
    
    public void Cleanup() {
        // release player resource
        
        Player.getInstance().freePort(m_iPort);
        m_iPort = -1;
        
        // release net SDK resource
        HCNetSDK.getInstance().NET_DVR_Cleanup();
    }
    
    @Override
    public void onDestroy() {
        Log.v(TAG, "----ondestroy----");
        stopSinglePlayer();
        Cleanup();
        if (mWakeLock!= null && mWakeLock.isHeld()){
            mWakeLock.release();
        }
        super.onDestroy();
    }
}