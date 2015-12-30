package com.ssiot.donghai;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;


public class BaseFragment extends Fragment{
    private static final String tag = "BaseFragment";
    Dialog mDialog = null;
    
    private static final int MSG_SHOW_DLG = 104;
    private static final int MSG_DISMISS_DLG = 105;
    private static final int MSG_LONGTIME = 106;//要确保是原来的dialog
    private Handler baseHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SHOW_DLG:
                    showMyDlg((String) msg.obj);
                    break;
                case MSG_DISMISS_DLG:
                    dismissMyDialog();
                    break;
                case MSG_LONGTIME:
                    if (null != mDialog  && mDialog.isShowing() && mDialog.equals(msg.obj)){
                        Log.w(tag, "-----dialog time out dismiss");
                        if (null != getActivity()){
                            Toast.makeText(getActivity(), "等待超时,请检查网络后重试！", Toast.LENGTH_SHORT).show();
                        }
//                        try {
//                            DbHelperSQL.connection.Close();//TODO 是否造成崩溃 TODO TODO 卡死主线程！
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        mDialog.dismiss();
                    }
                    break;
                default:
                    break;
            }
        };
    };
    
    public boolean canGoback(){
        return false;
    }
    
    public void onMyBackPressed(){
        
    }
    
    private void showMyDlg(String msg){
        if (null != mDialog && mDialog.isShowing()){//防止多个dialog显示出来？
            mDialog.dismiss();
        }
        mDialog = Utils.createLoadingDialog(getMyActivity(), msg);
        mDialog.show();
        baseHandler.removeMessages(MSG_LONGTIME);
        Message m = baseHandler.obtainMessage(MSG_LONGTIME);
        final Dialog d = mDialog;
        m.obj = d;
        baseHandler.sendMessageDelayed(m, 10 * 1000);
    }
    
    public void sendShowMyDlg(String msg){
        Message m = baseHandler.obtainMessage(MSG_SHOW_DLG);
        m.obj = msg;
        baseHandler.sendMessage(m);
    }
    
    private void dismissMyDialog(){
        if (null != mDialog && mDialog.isShowing()){
            mDialog.dismiss();
        }
    }
    
    public void sendDismissDlg(){
        baseHandler.sendEmptyMessage(MSG_DISMISS_DLG);
    }
    
    @SuppressLint("NewApi")
    public void showRefreshAnimation(MenuItem item,View uibase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            final MenuItem refreshItem = item;
            hideRefreshAnimation(refreshItem);
            ImageView refreshActionView = (ImageView) getActivity().getLayoutInflater().inflate(R.layout.action_refreshing, null);
            refreshActionView.setImageResource(R.drawable.ic_action_refresh);
            refreshItem.setActionView(null);
            refreshItem.setActionView(refreshActionView);
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.loading_animation);
            animation.setRepeatMode(Animation.RESTART);
            animation.setRepeatCount(Animation.INFINITE);
            refreshActionView.startAnimation(animation);
            uibase.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideRefreshAnimation(refreshItem);
                }
            }, 800);
        }
    }
    
    @SuppressLint("NewApi")
    private void hideRefreshAnimation(MenuItem refreshItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            if (refreshItem != null) {
                View view = refreshItem.getActionView();
                if (view != null) {
                    view.clearAnimation();
                    refreshItem.setActionView(null);
                }
            }
        }
    }
    
    private FragmentActivity getMyActivity(){//Fragmnet未知的BUG？？？
        if (super.getActivity() == null){
            if (getParentFragment() != null){
                return getParentFragment().getActivity();
            }
        } else {
            return getActivity();
        }
        return null;
    }
}