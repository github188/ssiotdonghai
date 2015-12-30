package com.ssiot.donghai;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class HistoryDetailFragment extends BaseFragment{
    public static final String tag = "HisDetailFragment";
    private FHisDetailBtnClickListener mFHisDetailBtnClickListener;
    
    private WebView webView;
    private  String url;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        String userkey = getArguments().getString("userkey");
        url="http://yun.ssiot.com/mobile/pages/Monitor.html?userkey="+userkey;
        url = "http://www.baidu.com";//jingbo for test
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View rootView = inflater.inflate(R.layout.fragment_history_detail, container, false);
        initWebView(rootView);
        return rootView;
    }
    
    public void initWebView(View rootView){
        webView=(WebView) rootView.findViewById(R.id.webView1);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setUseWideViewPort(false);
        webView.setVerticalScrollBarEnabled(true);
        WebSettings settings=webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);    
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
//                setProgress(progress * 100);
            }
        });
        // url要加载的网址
        webView.loadUrl(url);
        Log.v(tag, "-----------loadUri----------"+url);
    }
    
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url){
                super.onPageFinished(view, url);
                String title = webView.getTitle();
//                setTitle(title);
            }
        });
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        inflater.inflate(R.menu.main, menu);
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
    
    public void setClickListener(FHisDetailBtnClickListener listen){
        mFHisDetailBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FHisDetailBtnClickListener {  
        void onFHisDetailBtnClick();  
    }
}