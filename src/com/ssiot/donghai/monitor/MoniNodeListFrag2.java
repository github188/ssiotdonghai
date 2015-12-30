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
import android.widget.TextView;
import android.widget.Toast;

import com.ssiot.donghai.data.AjaxGetNodesDataByUserkey;
import com.ssiot.donghai.data.model.view.NodeView2Model;
import com.ssiot.donghai.data.model.view.NodeViewModel;
import com.ssiot.donghai.BaseFragment;
import com.ssiot.donghai.MainActivity;
import com.ssiot.donghai.R;
import com.ssiot.donghai.Utils;
import com.ssiot.donghai.monitor.MonitorListAdapter;
import com.ssiot.donghai.monitor.MonitorListAdapter.DetailListener;
import com.ssiot.donghai.monitor.MonitorListAdapter.ShowAllListener;
import com.ssiot.donghai.monitor.MonitorListAdapter.ThumnailHolder;

import java.util.ArrayList;
import java.util.List;

public class MoniNodeListFrag2 extends BaseFragment{
    public static final String tag = "MoniNodeListFrag";
    private FMoniNodeListBtnClickListener mFMoniNodeListBtnClickListener;
    private DetailListener mDetailListener;
    private String userKey = "";
    List<NodeView2Model> mNodes = new ArrayList<NodeView2Model>();
    ArrayList<NodeView2Model> mShowNodes = new ArrayList<NodeView2Model>();
    ListView mNodeListView;
    MonitorListAdapter mNodeAdapter;
    int currentArea = 1;
    View mHeaderView;
    
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
                        mShowNodes = checkOutNodes(mNodes, currentArea);
                        mNodeAdapter = new MonitorListAdapter(getActivity(), mShowNodes,mShowAllListener,mDetailListener,mHandler);
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
            mNodes.get(index).showAll = !mNodes.get(index).showAll;
            mNodeAdapter.notifyDataSetChanged();
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        userKey = getArguments().getString("uniqueid");
        currentArea = getArguments().getInt("currentArea" , 1);
        Log.v(tag, "----onCreate----currentArea:" + currentArea);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_monitor_tab, container, false);
        if (TextUtils.isEmpty(userKey)){
            Toast.makeText(getActivity(), "未获取到key", Toast.LENGTH_SHORT).show();
        }
        if (!Utils.isNetworkConnected(getActivity())){
            Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_SHORT).show();
        }
        mNodeListView = (ListView) v.findViewById(R.id.moni_list);
        mNodeAdapter = new MonitorListAdapter(getActivity(), mNodes,null,null,mHandler);
//        View headerView = inflater.inflate(R.layout.big_top, mNodeListView, false);
//        headerView.setBackgroundResource(donghaiActivity.AREA_DRAWABLE_ID[currentArea - 1]);
//        mHeaderView = headerView;
//        mNodeListView.addHeaderView(headerView, null, false);
        
        mNodeListView.setAdapter(mNodeAdapter);
        mNodeAdapter.notifyDataSetChanged();
        if (!Utils.isNetworkConnected(getActivity())){
            Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_LONG).show();
        } else {
            new GetAllMoniNodeThread().start();
        }
        return v;
    }
    
    private class GetControlActionInfoThread extends Thread{
        @Override
        public void run() {
            mNodes = new AjaxGetNodesDataByUserkey().GetAllNodesDataByUserkey(userKey);
            mHandler.sendEmptyMessage(MSG_GETNODES_END);
        }
    }
    
    private ArrayList<NodeView2Model> checkOutNodes(List<NodeView2Model> nodes, int currentHolderIndex){
        ArrayList<NodeView2Model> datas = new ArrayList<NodeView2Model>();
        for(NodeView2Model m : nodes){
//            if (isIn(AllMoniFrag.AllStaticNode[currentHolderIndex - 1], m._nodeno)){
                datas.add(m);
//            }
       }
       return datas;
    }
    
    private boolean isIn(int[] js,int j){
        if (js != null && js.length > 0){
            for (int i = 0; i < js.length; i ++){
                if (j == js[i]){
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_monitor, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_monitor_refresh:
                if (!Utils.isNetworkConnected(getActivity())){
                    Toast.makeText(getActivity(), R.string.please_check_net, Toast.LENGTH_LONG).show();
                } else {
                    showRefreshAnimation(item,mHeaderView);
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