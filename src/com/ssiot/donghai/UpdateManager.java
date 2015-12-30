package com.ssiot.donghai;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class UpdateManager{
    private static final String tag = "UpdateManager";
    public static final int NOTIFICATION_FLAG = 1; 
    private Context mContext;
    private Handler mAppHandler;
    public static boolean updating = false;
    
    public UpdateManager(Context context, Handler v){
        mContext = context;
        mAppHandler = v;
    }
    
    public void startGetRemoteVer(){
        new GetRemoteVerThread().start();
    }
    
    public void startDownLoad(HashMap<String, String> hsMap){
        new DownloadApkThread(hsMap).start();
    }
    
    public void stopDownload(){
        cancelUpdate = true;
    }
    
    private class GetRemoteVerThread extends Thread{
        @Override
        public void run() {
            updating = true;
            try {
                HashMap<String, String> mHashMap;
                int curV = getCurVersionCode(mContext);
                URL url = new URL("http://donghai.ssiot.com/app/down.myapp.com/donghaiversion.xml");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                InputStream inStream = conn.getInputStream();
                mHashMap = Utils.parseXml(inStream);
                if (null != mHashMap){
                    int serviceCode = Integer.valueOf(mHashMap.get("version"));
//                    remoteVersion = serviceCode;
                    if (null != mAppHandler){
                        Message m = mAppHandler.obtainMessage(MainActivity.MSG_GETVERSION_END);
                        m.arg1 = serviceCode;
                        m.arg2 = curV;
                        m.obj = mHashMap;
                        mAppHandler.sendMessage(m);
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
//                Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
                Message m = mAppHandler.obtainMessage(MainActivity.MSG_GETVERSION_END);
                m.arg1 = -1;
                mAppHandler.sendMessage(m);
            }
            updating = false;
            
        }
    }
    
    private int getCurVersionCode(Context c){
        int versionCode = 0;
        try {
            versionCode = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return versionCode;
    }
    
    boolean cancelUpdate = false;
    private class DownloadApkThread extends Thread {
        private HashMap<String, String> mHashMap;
        
        
        public DownloadApkThread(HashMap<String, String> mVerMap){
            mHashMap = mVerMap;
            cancelUpdate = false;
        }
        
        @Override
        public void run() {
            updating = true;
            try {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String mSavePath = getSavePath();
                    Log.v(tag, "--------------mSavePath" +mSavePath);
                    URL url = new URL(mHashMap.get("url"));
                    //URL url = new URL("yun.ssiot.com/UpdateSoftDemo.apk");
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream is = conn.getInputStream();
                    File file = new File(mSavePath);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath, mHashMap.get("name"));
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    int progress = 0;
                    int progressnow = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progressnow = (int) (((float) count / length) * 100);
                        // 更新进度
                        if (progressnow != progress){
                            Message m = mAppHandler.obtainMessage(MainActivity.MSG_DOWNLOADING_PREOGRESS);
                            m.arg1 = progress;
                            mAppHandler.sendMessage(m);
                        }
                        progress = progressnow;
                        
                        if (numread <= 0) {
                            // 下载完成
                            mAppHandler.sendEmptyMessage(MainActivity.MSG_DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                    if (cancelUpdate && apkFile.exists()){
                        apkFile.delete();
                        mAppHandler.sendEmptyMessage(MainActivity.MSG_DOWNLOAD_CANCEL);
                    }
                } else {
                    Log.v(tag, "!!!!!!!!!!!!!!!!! sdcard is not mounted");
                    mAppHandler.sendEmptyMessage(MainActivity.MSG_SHOWERROR);
                }
            } catch (MalformedURLException e) {
//                showText = "URL错误";
                mAppHandler.sendEmptyMessage(MainActivity.MSG_SHOWERROR);
                e.printStackTrace();
            } catch (IOException e) {
//                showText = "IO错误";
                mAppHandler.sendEmptyMessage(MainActivity.MSG_SHOWERROR);
                e.printStackTrace();
            }
            // 取消下载对话框显示
//            if (null != mDownloadDialog && mDownloadDialog.isShowing()){
//                mDownloadDialog.dismiss();
//            }
            updating = false;
        }
        
        public void cancel(){
            cancelUpdate = true;
        }
    };
    
    public String getSavePath(){
        String path = Environment.getExternalStorageDirectory() + "/" + SsiotConfig.CACHE_DIR+ "/";
        return path;
    }
    
    @SuppressLint("NewApi") //必须检查版本
    public Notification showNotification(Context c) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            Notification notification = new Notification(R.drawable.ic_launcher, "更新", System.currentTimeMillis());
            notification.setLatestEventInfo(c, "111111111", "22222222", 
                    PendingIntent.getActivity(c, -1, new Intent(""), 0));
            NotificationManager mnotiManager = (NotificationManager) c
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            mnotiManager.notify(NOTIFICATION_FLAG, notification);
            return notification;
        } else {
            Notification.Builder builder = new Notification.Builder(c);
            // builder.setTicker(title);
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setWhen(System.currentTimeMillis());
            builder.setContentTitle("正在更新");
            builder.setContentText("已下载：0%");
            builder.setAutoCancel(false);
            builder.setProgress(100, 50, false);
            // builder.setContentIntent(PendingIntent.getActivity(c, 0, new
            // Intent(Intent.ACTION_DELETE), 0));

            // Notification noti = new Notification();
            RemoteViews remoteView = new RemoteViews(c.getPackageName(),
                    R.layout.notification_download);
            remoteView.setProgressBar(R.id.noti_progress, 100, 20, false);
            remoteView.setImageViewResource(R.id.noti_image, R.drawable.ic_launcher);
            remoteView.setTextViewText(R.id.noti_text, "我的新通知");
            // builder.setContent(remoteView);
            Notification noti = builder.build();
            // noti.contentView = remoteView;
//            noti.flags |= Notification.FLAG_ONGOING_EVENT;

            NotificationManager mnotiManager = (NotificationManager) c
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            mnotiManager.notify(NOTIFICATION_FLAG, noti);
            return noti;
        }
    }
    
    public void installApk() {
        File apkfile = new File(getSavePath(), "donghai2.apk");
        if (!apkfile.exists()) {
            Toast.makeText(mContext, "未找到文件" + apkfile.getPath(), Toast.LENGTH_SHORT).show();
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
    
//    // in Thread
//    public interface VersionListener{
//        public void onNewVersionFound(HashMap<String, String> h);
//    }
}