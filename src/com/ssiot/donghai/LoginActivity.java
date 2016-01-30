package com.ssiot.donghai;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends ActionBarActivity {
    private EditText logEditText;
    private EditText pwdEditText;
    private Button logButton;
    private String userName = "";
    private String password = "";
    private String uniqueID = "";
    private Dialog mWaitDialog;

    private final int MSG_LOGIN_RETURN = 1;
    private final int MSG_LOGIN_TIMEOUT = 2;
    private final int MSG_LOGIN_CON_FAIL = 3;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (null != mWaitDialog){
                mWaitDialog.dismiss();
            }
            switch (msg.what) {
                case MSG_LOGIN_RETURN:  
                    removeMessages(MSG_LOGIN_TIMEOUT);
                    if (!TextUtils.isEmpty(uniqueID)) {
                        SharedPreferences mPref = PreferenceManager
                                .getDefaultSharedPreferences(LoginActivity.this);
                        if (mPref != null) {
                            Editor editor = mPref.edit();
                            editor.putString("username", userName);
                            editor.putString("password", password);
                            editor.commit();
                        }
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("userkey", uniqueID);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "用户名或密码有误",
                                Toast.LENGTH_LONG).show();
                        showLogin(true);
                    }
                    break;
                case MSG_LOGIN_TIMEOUT:
                    Toast.makeText(getApplicationContext(), "登陆失败",
                            Toast.LENGTH_LONG).show();
                    showLogin(true);
                    break;
                case MSG_LOGIN_CON_FAIL:
                    Toast.makeText(getApplicationContext(), "连接服务器失败",
                            Toast.LENGTH_LONG).show();
                    showLogin(true);
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        ColorDrawable cd = new ColorDrawable(0xff087d25);
        getSupportActionBar().setBackgroundDrawable(cd);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        /*
         * LayoutInflater layout=this.getLayoutInflater(); View
         * view=layout.inflate(R.layout.activity_main,null);
         */
        logEditText = (EditText) findViewById(R.id.logEditText);
        pwdEditText = (EditText) findViewById(R.id.pwdEditText);
        logButton = (Button) findViewById(R.id.logButton);
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (mPref != null) {
            String pro_username = mPref.getString("username", "");
            String pro_password = mPref.getString("password", "");
            String pro_isRemember = mPref.getString("rememberPWD", "");
            if (!TextUtils.isEmpty(pro_username)) {
                userName = pro_username;
                password = pro_password;
                if (!TextUtils.isEmpty(userName) && ! TextUtils.isEmpty(password)){// && isDongHaiUser(userName)
//                    mWaitDialog = Utils.createLoadingDialog(LoginActivity.this, "正在登陆");
//                    mWaitDialog.show();
                    mHandler.sendEmptyMessageDelayed(MSG_LOGIN_TIMEOUT, 12000);
                    new Thread(new getUniqueID()).start();
                } else {
                    showLogin(true);
                }
                loadUserInfoToUI(pro_username, pro_password, pro_isRemember);
            } else {
                showLogin(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != logEditText){
            logEditText.requestFocus();
        }
        logButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = logEditText.getText().toString();
                password = pwdEditText.getText().toString();
                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "用户名和密码不能为空", Toast.LENGTH_LONG)
                            .show();
                } else {
                    mWaitDialog = Utils.createLoadingDialog(LoginActivity.this, "正在登陆");
                    mWaitDialog.show();
                    mHandler.sendEmptyMessageDelayed(MSG_LOGIN_TIMEOUT, 8000);
                    new Thread(new getUniqueID()).start();
                }
            }
        });
    }
    
    private boolean isDongHaiUser(String name){
        if ("dhdx".equalsIgnoreCase(name)
                || "dhny".equalsIgnoreCase(name) || "angel".equalsIgnoreCase(name)){
            return true;
        }
        Toast.makeText(this, "请使用东海用户的帐号！", Toast.LENGTH_SHORT).show();
        return false;
    }
    
    private void showLogin(boolean b){
        if (b){
            logButton.setVisibility(View.VISIBLE);
            findViewById(R.id.name_bar).setVisibility(View.VISIBLE);
            findViewById(R.id.pwd_bar).setVisibility(View.VISIBLE);
        } else {
            logButton.setVisibility(View.GONE);
            findViewById(R.id.name_bar).setVisibility(View.GONE);
            findViewById(R.id.pwd_bar).setVisibility(View.GONE);
        }
    }

    /*
     * Ԥ�����û���Ϣ
     * @param name �û���
     * @param password ����
     * @param isRemember �Ƿ��ס����
     */
    private void loadUserInfoToUI(String name, String password, String isRemember) {
        if (isRemember.contains("yes")) {
            logEditText.setText(name);
            pwdEditText.setText(password);
        } else if (!TextUtils.isEmpty(name)) {
            logEditText.setText(name);
        }
    }

    private class getUniqueID implements Runnable {
        @Override
        public void run() {
            try {
                ConSQL sql = new ConSQL();
                if (sql.ConnectSQl()){
                    uniqueID = sql.getUniqueIDFromDB(userName, password);
                    mHandler.sendEmptyMessage(MSG_LOGIN_RETURN);
                } else {
                    mHandler.sendEmptyMessage(MSG_LOGIN_CON_FAIL);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showLogin(true);
            }
        }
    }
    
}
