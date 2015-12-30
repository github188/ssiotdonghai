package com.ssiot.donghai.hikvision;

import android.app.Dialog;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ssiot.donghai.R;
import com.ssiot.donghai.Utils;

public class RTSPVideo extends ActionBarActivity{
    private static final String tag = "RTSPVideo";
    
    private VideoView mVideoView;
    private MediaPlayer mMediaPlayer;
    private Dialog mDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtsp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mVideoView = (VideoView) findViewById(R.id.video_rtsp);
        Uri mUri = Uri.parse("rtsp://192.168.1.107:8000/sample_100kbit.mp4");// test
        mUri = Uri.parse("rtsp://admin:admin12345@192.168.1.64:554");// test
        final String uriString = getIntent().getStringExtra("videourl");
//        final String uriString = "rtsp://admin:admin@49.83.210.22:5555";///cam/realmonitor?channel=1&subtype=0";49.83.210.22
        String textString = getIntent().getStringExtra("addrtitle");
        mUri = Uri.parse(uriString);
//        mVideoView.setVideoURI(mUri);
//        mVideoView.start();
        mMediaPlayer = new MediaPlayer();
        mDialog = Utils.createLoadingDialog(this, "正在连接");
        mDialog.show();
        new Thread(){//BUG videostart后 返回销毁会导致anr？？
            public void run() {
                play2(uriString);
            };
        }.start();
        
        ((TextView) findViewById(R.id.rtsp_title)).setText(textString);
        Log.v(tag, "-------------startRTSP---:" + mUri +" playing:"+mVideoView.isPlaying());
    }
    
    private void play2(String mPath){
        final String pathStr = mPath;
        try {
            mMediaPlayer.setDataSource(mPath);
            mMediaPlayer.prepare();
            mMediaPlayer.setDisplay(mVideoView.getHolder());
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO toast
            mVideoView.post(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Toast.makeText(RTSPVideo.this, "播放出现问题" + pathStr, Toast.LENGTH_LONG).show();
                }
            });
        }
        if (null != mDialog && mDialog.isShowing()){
            mDialog.dismiss();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
//        if (null != mVideoView && !mVideoView.isPlaying()){
//            mVideoView.start();
//        }
    }
    
    @Override
    protected void onPause() {
        long i = SystemClock.uptimeMillis();
//        if (null!= mVideoView && mVideoView.isPlaying()){
//            mVideoView.stopPlayback();
//        }
        Log.v(tag, "=====================pause time:" + (SystemClock.uptimeMillis() -i));
        super.onPause();
    }
    
    private void stopMediaplayerInThread(){
        new Thread(){
            public void run() {
                if (null != mMediaPlayer){
                    try {
                        long time1 = SystemClock.uptimeMillis();
                        mMediaPlayer.stop();
                        mMediaPlayer.release();//这句会anr！
                        mMediaPlayer = null;
                        if (SystemClock.uptimeMillis() - time1 > 5000){
                            Log.e(tag, "-------!!!!!!!!!!!!!mediaplayer!!!!!!" + (SystemClock.uptimeMillis()- time1));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        }.start();
    }
    
    @Override
    protected void onDestroy() {
        long i = SystemClock.uptimeMillis();
        if (null!= mVideoView && mVideoView.isPlaying()){
            mVideoView.stopPlayback();
        }
        stopMediaplayerInThread();
        
        Log.v(tag, "=====================destroy time:" + (SystemClock.uptimeMillis() -i));
        super.onDestroy();
        finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}