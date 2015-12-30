package com.ssiot.donghai;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ssiot.donghai.data.DataAPI;
import com.ssiot.donghai.data.DbHelperSQL;
import com.ssiot.donghai.data.model.VLCVideoInfoModel;
import com.ssiot.donghai.hikvision.RTSPVideo;
import com.ssiot.donghai.hikvision.VideoActivity;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoFragment extends Fragment{
    public static final String tag = "VideoFragment";
    private FMainBtnClickListener mFMainBtnClickListener;
    VideoView mVideoView;
    ListView mListView;
    private VideoListAdapter mAdapter;
    private List<VLCVideoInfoModel> mVideoModels;
    
    private boolean cancelStatus = false;
    
    private static final int MSG_QUERY_OK = 1;
    private static final int MSG_ACCESS_OK = 2;
    private static final int MSG_ACCESS_FAIL = 3;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_QUERY_OK:
                    initListView();
                    new UpdateStatusThread(mVideoModels).start();
                    break;
                case MSG_ACCESS_OK:
                    mVideoModels.get(msg.arg1).status = 1;
                    mAdapter.notifyDataSetChanged();
                    break;
                case MSG_ACCESS_FAIL:
                    mVideoModels.get(msg.arg1).status = 2;
                    mAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        };
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View v = inflater.inflate(R.layout.fragment_video, container, false);
        
//        mVideoView = (VideoView) v.findViewById(R.id.video_monitor);
//        Uri mUri = Uri.parse("rtsp://192.168.1.107:8000/sample_100kbit.mp4");
//        mUri = Uri.parse("rtsp://admin:admin12345@192.168.1.64:554");///h264/ch1/main/av_stream
//        mVideoView.setVideoURI(mUri);
//        mVideoView.start();
        
        mListView = (ListView) v.findViewById(R.id.cameralist);
        
        
      //TODO in a better way
        final String uniqueIdString = ((MainActivity) getActivity()).getUnique();
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userName = mPref.getString("username", "");
        
        if (Utils.isNetworkConnected(getActivity())){
            final Dialog d = Utils.createLoadingDialog(getActivity(), "正在查询");
            d.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String areaIds = DataAPI.GetAreaIDsByUserKey(uniqueIdString);//多个 例如13，29
                    if (!TextUtils.isEmpty(areaIds)){
                        mVideoModels = DataAPI.GetVLCVideoMapInfoByAreaIds(areaIds);
                    }
                    if (null == mVideoModels){
                        Log.e(tag, "-----get video ==null, uniqueIdString:" +uniqueIdString +" areaIds:"+areaIds);
                        mVideoModels = new ArrayList<VLCVideoInfoModel>();
                    }
                    DbHelperSQL.closeAll();
                    mHandler.sendEmptyMessage(MSG_QUERY_OK);
                    if(null != d && d.isShowing()){
                        d.dismiss();
                    }
                }
            }).start();
        } else {
            Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_LONG).show();
        }
        
        return v;
    }
    
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    
    @Override
    public void onDestroy() {
        cancelStatus = true;
        mHandler.removeMessages(MSG_QUERY_OK);
        mHandler.removeMessages(MSG_ACCESS_OK);
        mHandler.removeMessages(MSG_ACCESS_FAIL);
        super.onDestroy();
    }
    
    private void initListView(){
        if (null == mVideoModels || mVideoModels.size() == 0){
            Toast.makeText(getActivity(), "未找到视频节点", Toast.LENGTH_LONG).show();
        }
        mAdapter = new VideoListAdapter(getActivity(), mVideoModels);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mVideoModels.get(position)){
                    Log.v(tag, "----------onclick------type:"+ mVideoModels.get(position)._type);
                    VLCVideoInfoModel vModel = mVideoModels.get(position);
                    if (!Utils.isNetworkConnected(getActivity())){
                        Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if ("海康".equals(vModel._type) && vModel._tcpport != 0){
                        Intent intent = new Intent(getActivity(), VideoActivity.class);
                        Bundle videoBundle = new Bundle();
                        videoBundle.putString("videoip", vModel._ip);
                        videoBundle.putString("videoname", vModel._username);
                        videoBundle.putString("videopswd", vModel._password);
                        videoBundle.putString("addrtitle", vModel._address);
                        videoBundle.putInt("tcpport", vModel._tcpport);
                        intent.putExtra("videobundle", videoBundle);
                        startActivity(intent);
                    } else {
                        Intent i = new Intent(getActivity(), RTSPVideo.class);
                        i.putExtra("videourl", vModel._url);
                        i.putExtra("addrtitle", vModel._address);
                        startActivity(i);
                    }
//                    Intent intent = new Intent(getActivity(), DahuaLiveActivity.class);
//                    intent.putExtra("videoip", "lvfa110.dahuaddns.com");//"127.0.0.1"
//                    intent.putExtra("videoport", "");
//                    intent.putExtra("videoname", "admin");
//                    intent.putExtra("videopswd", "admin");
//                    intent.putExtra("addrtitle", "jingbotest");
//                    intent.putExtra("tcpport", 37777);//16765
//                    startActivity(intent);
                }
                
            }
        });
    }
    
    private List<Map<String, Object>> getListData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 
        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("title", "G1");
//        map.put("info", "google 1");
////        map.put("img", R.drawable.i1);
//        list.add(map);
// 
//        map = new HashMap<String, Object>();
//        map.put("title", "G2");
//        map.put("info", "google 2");
////        map.put("img", R.drawable.i2);
//        list.add(map);
// 
//        map = new HashMap<String, Object>();
//        map.put("title", "G3");
//        map.put("info", "google 3");
////        map.put("img", R.drawable.i3);
//        list.add(map);
        
        if (null != mVideoModels){
            for (VLCVideoInfoModel v : mVideoModels){
                map = new HashMap<String, Object>();
                map.put("title", v._address);
                list.add(map);
            }
        }
        
        return list;
    }
    
    @Override
    public void onDestroyView() {
        if (null != mVideoView){
            mVideoView.stopPlayback();
        }
        super.onDestroyView();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        inflater.inflate(R.menu.menu_video_frag, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case R.id.action_video_refresh:
                Log.v(tag, "----------------action-settting");
                if (mRunning == false){
                    if (null != mVideoModels && null != mAdapter){
                        for (int j = 0; j< mVideoModels.size(); j ++){
                            mVideoModels.get(j).status = 0;
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    new UpdateStatusThread(mVideoModels).start();
                    Toast.makeText(getActivity(), R.string.action_refresh_link, Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
        return true;
    }
    
    public void setClickListener(FMainBtnClickListener listen){
        mFMainBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FMainBtnClickListener {  
        void onFMainBtnClick();  
    }
    
    private boolean mRunning = false;
    public class UpdateStatusThread extends Thread{
        List<VLCVideoInfoModel> models;
        public UpdateStatusThread(List<VLCVideoInfoModel> ms){
            models = ms;
            cancelStatus = false;
        }
        @Override
        public void run() {
            mRunning = true;
            if (null != models){
                int size = models.size();
                for (int i = 0; i < size; i ++){
                    if (cancelStatus){
                        mRunning = false;
                        return;
                    }
                    Socket socket = new Socket();
                    try {
                        int port = models.get(i)._tcpport;
                        if (port == 0){
                            port = Integer.parseInt(models.get(i)._port);
//                            if (port==5566){
//                                port =77;
//                            }
                        }
                        Log.v(tag, "-----start to test " + models.get(i)._ip +  port + "  tcpport:"+models.get(i)._tcpport);
                        SocketAddress socketAddress = new InetSocketAddress(models.get(i)._ip, port);
                        socket.connect(socketAddress, 3000);
                        Message msg = mHandler.obtainMessage();
                        msg.what = MSG_ACCESS_OK;
                        msg.arg1 = i;
                        mHandler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message msg = mHandler.obtainMessage();
                        msg.what = MSG_ACCESS_FAIL;
                        msg.arg1 = i;
                        mHandler.sendMessage(msg);
                    }
                    try {
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                }
            }
            mRunning = false;
        }
    }
}