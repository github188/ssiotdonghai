
package com.ssiot.donghai;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

public class FirstStartActivity extends Activity {
    private final String tag = "FirstStartActivity";
    private String username = "";
    private String password = "";
    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        if (mPref != null) {
            username = mPref.getString("username", "");
            password = mPref.getString("password", "");

            Log.v(tag, "---------preference:"+username + password);
//            username = "tscmy";
//            password = "tscmy12345";
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                new Thread(new autoLogin()).start();// auto login
            } else {
                startLoginUI();
            }
        } else {
            Log.e(tag, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            startLoginUI();
        }
        super.onResume();
    }

    class autoLogin implements Runnable {

        @Override
        public void run() {
            try {
                long old = SystemClock.uptimeMillis();
                ConSQL sql = new ConSQL();
                sql.ConnectSQl();
                String uniqueID = sql.getUniqueIDFromDB(username, password);
                long now = SystemClock.uptimeMillis();
                Log.v(tag, "-------uniqueID:"+uniqueID);
                if (TextUtils.isEmpty(uniqueID)) {
                    startLoginUI();
                } else {
                    // 转到下一个Activity
                    Intent intent = new Intent(FirstStartActivity.this, MainActivity.class);
                    intent.putExtra("userkey", uniqueID);
                    // startActivity(intent);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void startLoginUI() {
        Intent intent = new Intent(FirstStartActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
