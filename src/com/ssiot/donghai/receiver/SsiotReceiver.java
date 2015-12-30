package com.ssiot.donghai.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class SsiotReceiver extends BroadcastReceiver{
    private static final String tag = "SsiotReceiver";
    public static final String ACTION_SSIOT_MSG = "com.ssiot.donghai.SHOWMSG";
    @Override
    public void onReceive(Context context, Intent intent) {
        
        String action = intent.getAction();
        
        if (ACTION_SSIOT_MSG.equals(action)){
            
            String extraString = intent.getStringExtra("showmsg");
            Log.v(tag, "----onReceive----" +extraString);
            Toast.makeText(context, extraString + "，请检查网络后重试。", Toast.LENGTH_SHORT).show();
        }
    }
}