package com.ssiot.donghai;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MonitorFragment extends BaseFragment{
    public static final String tag = "MonitorFragment";
    private FMonitorBtnClickListener mFMonitorBtnClickListener;
    private static final String urlString = "http://yun.ssiot.com/mobile/pages/Monitor.html?userkey=";
    private String userKey = "";
    private WebView webView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        userKey = getArguments().getString("uniqueid");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View v = inflater.inflate(R.layout.fragment_monitor, container, false);
//        Button b = (Button) v.findViewById(R.id.btn_monitor_f);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                if (null != mFMonitorBtnClickListener){
//                    mFMonitorBtnClickListener.onFMonitorBtnClick();
//                }
//            }
//        });
        webView = (WebView) v.findViewById(R.id.webview_monitor);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setUseWideViewPort(false);
        webView.setVerticalScrollBarEnabled(true);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
//                setProgress(progress * 100);
            }
        });
        // url要加载的网址
        if (TextUtils.isEmpty(userKey)){
            Toast.makeText(getActivity(), "未获取到key", Toast.LENGTH_SHORT).show();
        }
        if (!Utils.isNetworkConnected(getActivity())){
            Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_SHORT).show();
        }
        webView.loadUrl(urlString + userKey);
        
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!Utils.isNetworkConnected(getActivity())){
                    Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_SHORT).show();
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String title = webView.getTitle();
                // setTitle(title);
            }
        });
        return v;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        inflater.inflate(R.menu.menu_monitor, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case R.id.action_monitor_refresh:
                if (!Utils.isNetworkConnected(getActivity())){
                    Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_LONG).show();
                } else {
                    webView.reload();
                    showRefreshAnimation(item);
                }
                break;

            default:
                break;
        }
        return true;
    }
    
    MenuItem refreshItem;
    @SuppressLint("NewApi")
    private void showRefreshAnimation(MenuItem item) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            hideRefreshAnimation();
            refreshItem = item;
            ImageView refreshActionView = (ImageView) getActivity().getLayoutInflater().inflate(R.layout.action_refreshing, null);
            refreshActionView.setImageResource(R.drawable.ic_action_refresh);
            refreshItem.setActionView(null);
            refreshItem.setActionView(refreshActionView);
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.loading_animation);
            animation.setRepeatMode(Animation.RESTART);
            animation.setRepeatCount(Animation.INFINITE);
            refreshActionView.startAnimation(animation);
            webView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    hideRefreshAnimation();
                }
            }, 800);
        }
    }
    
    @SuppressLint("NewApi")
    private void hideRefreshAnimation() {
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
    
    @Override
    public boolean canGoback(){
        if (null != webView && webView.canGoBack()){
            return true;
        }
        return false;
    }
    
    @Override
    public void onMyBackPressed(){//add by jingbo
        if (webView!= null){
            webView.goBack();
        }
    }
    
    public void setClickListener(FMonitorBtnClickListener listen){
        mFMonitorBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FMonitorBtnClickListener {  
        void onFMonitorBtnClick();  
    }
}