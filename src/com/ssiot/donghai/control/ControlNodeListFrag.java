package com.ssiot.donghai.control;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.ssiot.donghai.BaseFragment;
import com.ssiot.donghai.GetImageThread;
import com.ssiot.donghai.R;
import com.ssiot.donghai.Utils;
import com.ssiot.donghai.data.AjaxGetNodesDataByUserkey;
import com.ssiot.donghai.data.model.view.ControlNodeViewModel;
import com.ssiot.donghai.control.ControlListAdapter;
import com.ssiot.donghai.control.ControlListAdapter.ControlDetailListener;

import java.util.ArrayList;
import java.util.List;

public class ControlNodeListFrag extends BaseFragment{
    public static final String tag = "ControlNodeListFrag";
    private FControlNodeListBtnClickListener mFControlNodeListBtnClickListener;
    private ControlDetailListener mCtrDetailListener;
    private String userKey = "";
    List<ControlNodeViewModel> mNodes = new ArrayList<ControlNodeViewModel>();
    ListView mNodeListView;
    ControlListAdapter mNodeAdapter;
    
    private static final int MSG_GETNODES_END = 1;
    public static final int MSG_GET_ONEIMAGE_END = 2;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if (!isVisible()){
                Log.e(tag, "----fragment is not visible----!!!!");
                return;
            }
            Log.v(tag, "---handleMessage----" + msg.what + " " + mNodes.size());
            switch (msg.what) {
                case MSG_GETNODES_END:
                    if (null != mNodes && mNodes.size() > 0 && null != mNodeAdapter){
                        Log.v(tag, "----------refresh node list");
                        
                        mNodeAdapter = new ControlListAdapter(getActivity(), mNodes,mCtrDetailListener,mHandler);
                        mNodeListView.setAdapter(mNodeAdapter);
                        mNodeAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getParentFragment().getActivity(), "节点数量" + mNodes.size(), Toast.LENGTH_SHORT).show();
                        Log.e(tag, "----------MSG_GETNODES_END-error!!!!!!!!!!!!!!");
                    }
                    break;
                case MSG_GET_ONEIMAGE_END:
//                    mNodeAdapter.notifyDataSetChanged();//不能notify，会无限循环
                    GetImageThread.ThumnailHolder thumb = (GetImageThread.ThumnailHolder) msg.obj;
                    thumb.imageView.setImageBitmap(thumb.bitmap);
                    break;

                default:
                    break;
            }
        };
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        userKey = getArguments().getString("uniqueid");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_control_list, container, false);
        if (TextUtils.isEmpty(userKey)){
            Toast.makeText(getActivity(), "未获取到key", Toast.LENGTH_SHORT).show();
        }
        if (!Utils.isNetworkConnected(getActivity())){
            Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_SHORT).show();
        }
        mNodeListView = (ListView) v.findViewById(R.id.control_list);
        mNodeAdapter = new ControlListAdapter(getActivity(), mNodes,null,mHandler);
        mNodeListView.setAdapter(mNodeAdapter);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mNodes = new AjaxGetNodesDataByUserkey().GetControlNodesByUserkey(userKey);
                mHandler.sendEmptyMessage(MSG_GETNODES_END);
            }
        }).start();
        return v;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_control, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_control_refresh:
                if (!Utils.isNetworkConnected(getActivity())){
                    Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_LONG).show();
                } else {
                    showRefreshAnimation(item);
                }
                break;

            default:
                break;
        }
        return true;
    }
    
    @Override
    public void onDestroyView() {
        Log.v(tag, "----onDestroyView----");
        mHandler.removeMessages(MSG_GETNODES_END);
        mHandler.removeMessages(MSG_GET_ONEIMAGE_END);
        super.onDestroyView();
    };
    
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
            //TODO real action
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
    
    public void setCtrDetailListener(ControlDetailListener lis){
        mCtrDetailListener = lis;
    }
    
    public void setClickListener(FControlNodeListBtnClickListener listen){
        mFControlNodeListBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FControlNodeListBtnClickListener {  
        void onFControlNodeListBtnClick(int position);  
    }
}