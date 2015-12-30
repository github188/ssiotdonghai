package com.ssiot.donghai.monitor;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ListView;
import android.widget.Toast;

import com.ssiot.donghai.BaseFragment;
import com.ssiot.donghai.R;
import com.ssiot.donghai.Utils;
import com.ssiot.donghai.data.AjaxGetNodesDataByUserkey;
import com.ssiot.donghai.data.model.view.NodeView2Model;
import com.ssiot.donghai.data.model.view.NodeViewModel;
import com.ssiot.donghai.monitor.MonitorListAdapter2;
import com.ssiot.donghai.monitor.MonitorListAdapter2.DetailListener;
import com.ssiot.donghai.monitor.MonitorListAdapter2.ShowAllListener;
import com.ssiot.donghai.monitor.MonitorListAdapter2.ThumnailHolder;

import java.util.ArrayList;
import java.util.List;

public class MoniNodeListFrag extends BaseFragment{
    public static final String tag = "MoniNodeListFrag";
    private FMoniNodeListBtnClickListener mFMoniNodeListBtnClickListener;
    private DetailListener mDetailListener;
    private String userKey = "";
    List<NodeView2Model> mNodes = new ArrayList<NodeView2Model>();
    ListView mNodeListView;
    MonitorListAdapter2 mNodeAdapter;
    
    private static final int MSG_GETNODES_END = 1;
    public static final int MSG_GET_ONEIMAGE_END = 2;
    private static final int MSG_REFRESH = 3;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if (!isVisible()){
                Log.e(tag, "----fragment is not visible----!!!!" + msg.what);
                return;
            }
            Log.v(tag, "---handleMessage----" + msg.what + " size:" + mNodes.size());
            switch (msg.what) {
                case MSG_GETNODES_END:
                    if (null != mNodes && mNodes.size() > 0 && null != mNodeAdapter){
                        Log.v(tag, "----------refresh node list");
                        mNodeAdapter = new MonitorListAdapter2(getParentFragment().getActivity(), mNodes,mShowAllListener,mDetailListener,mHandler);
                        mNodeListView.setAdapter(mNodeAdapter);
                        mNodeAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(tag, "----------MSG_GETNODES_END-error!!!!!!!!!!!!!!");
                    }
                    break;
                case MSG_GET_ONEIMAGE_END:
//                    mNodeAdapter.notifyDataSetChanged();//不能notify，会无限循环
                    ThumnailHolder thumb = (ThumnailHolder) msg.obj;
                    thumb.imageView.setImageBitmap(thumb.bitmap);
                    break;
                case MSG_REFRESH:
                    new GetAllMoniNodeThread().start();
                    break;

                default:
                    break;
            }
        };
    };
    ShowAllListener mShowAllListener = new ShowAllListener() {
        
        @Override
        public void onShowAll(int index) {
            // TODO Auto-generated method stub
            mNodes.get(index).showAll = !mNodes.get(index).showAll;
            mNodeAdapter.notifyDataSetChanged();
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        userKey = getArguments().getString("uniqueid");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(tag, "----onCreateView----");//TODO BUG onBack 返回到这个时，如果重新获取失败，原页面listener会无效
        View v = inflater.inflate(R.layout.fragment_monitor_tab, container, false);
        if (TextUtils.isEmpty(userKey)){
            Toast.makeText(getActivity(), "未获取到key", Toast.LENGTH_SHORT).show();
        }
        if (!Utils.isNetworkConnected(getActivity())){
            Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_SHORT).show();
        }
        mNodeListView = (ListView) v.findViewById(R.id.moni_list);
        mNodeAdapter = new MonitorListAdapter2(getActivity(), mNodes,null,null,mHandler);
        mNodeListView.setAdapter(mNodeAdapter);
        mNodeAdapter.notifyDataSetChanged();
        if (!Utils.isNetworkConnected(getActivity())){
            Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_LONG).show();
        } else {
            new GetAllMoniNodeThread().start();
        }
        return v;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        inflater.inflate(R.menu.menu_monitor, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_monitor_refresh:
                if (!Utils.isNetworkConnected(getActivity())){
                    Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_LONG).show();
                } else {
                    showRefreshAnimation(item,mNodeListView);
                    mHandler.sendEmptyMessage(MSG_REFRESH);
                }
                break;

            default:
                break;
        }
        return true;
    }
    
    @Override
    public void onDestroyView() {
        mHandler.removeMessages(MSG_GETNODES_END);
        mHandler.removeMessages(MSG_GET_ONEIMAGE_END);
        super.onDestroyView();
    };
    
    MenuItem refreshItem;
    @SuppressLint("NewApi")
    public void showRefreshAnimation(MenuItem item,View uibase) {
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
            //TODO real action
            uibase.postDelayed(new Runnable() {
                @Override
                public void run() {
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
    
    public void onMyBackPressed(){//add by jingbo
    }
    
    public void setDetailListener(DetailListener lis){
        mDetailListener = lis;
    }
    
    public void setClickListener(FMoniNodeListBtnClickListener listen){
        mFMoniNodeListBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FMoniNodeListBtnClickListener {  
        void onFMoniNodeListBtnClick(int position);  
    }
    
    private class GetAllMoniNodeThread extends Thread{
        @Override
        public synchronized void run() {
            sendShowMyDlg("正在查询");
            mNodes = new AjaxGetNodesDataByUserkey().GetAllNodesDataByUserkey(userKey);
            sendDismissDlg();
            mHandler.sendEmptyMessage(MSG_GETNODES_END);
        }
    }
}