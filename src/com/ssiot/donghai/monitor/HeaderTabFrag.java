package com.ssiot.donghai.monitor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import com.ssiot.donghai.BaseFragment;
import com.ssiot.donghai.R;
import com.ssiot.donghai.control.ControlDetailHolderFrag;
import com.ssiot.donghai.control.ControlListAdapter.ControlDetailListener;
import com.ssiot.donghai.control.ControlNodeListFrag;
import com.ssiot.donghai.data.model.view.ControlNodeViewModel;
import com.ssiot.donghai.data.model.view.NodeView2Model;
import com.ssiot.donghai.monitor.MoniNodeListFrag.FMoniNodeListBtnClickListener;
import com.ssiot.donghai.monitor.MonitorListAdapter.DetailListener;

public class HeaderTabFrag extends BaseFragment{
    public static final String tag = "HeaderTabFragment";
    private FHeaderTabBtnClickListener mFHeaderTabBtnClickListener;
    private FragmentManager fragmentManager;
    private RadioGroup radioGroup; 
    String userKey;
    int defaultTab = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        fragmentManager = getChildFragmentManager();
        userKey = getArguments().getString("uniqueid");
        defaultTab = getArguments().getInt("defaulttab", 1);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(tag, "----onCreateView----");
        View v = inflater.inflate(R.layout.fragment_header_tab, container, false);
        
        radioGroup = (RadioGroup) v.findViewById(R.id.rg_tab);
        
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
            @Override  
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                Fragment fragment = getInstanceByIndex(checkedId);
                transaction.replace(R.id.detail_content, fragment);
                fragmentManager.popBackStackImmediate();//TODO 是否是全部弹出还是单个弹出
//                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        switch (defaultTab) {
            case 1:
                radioGroup.check(R.id.radiobutton_moni);
                break;
            case 2:
                radioGroup.check(R.id.radiobutton_control);
                break;
            case 3:
                radioGroup.check(R.id.radiobutton_moni);
                break;
            default:
                radioGroup.check(R.id.radiobutton_moni);
                break;
        }
        
        return v;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.HeaderTab, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                Log.v(tag, "----------------action-settting");
//                break;
//
//            default:
//                break;
//        }
        return super.onOptionsItemSelected(item);
    }
    
    public void setClickListener(FHeaderTabBtnClickListener listen){
        mFHeaderTabBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FHeaderTabBtnClickListener {  
        void onFHeaderTabBtnClick();  
    }
    
    @Override
    public boolean canGoback() {
        if (fragmentManager.getBackStackEntryCount() != 0){
            return true;
        }
        return false;
    }
    
    @Override
    public void onMyBackPressed() {
        Log.v(tag, "----onMyBackPressed----");
        fragmentManager.popBackStack();
    }
    
    private Fragment getInstanceByIndex(int index) {
        Fragment fragment = null;
        switch (index) {
            case R.id.radiobutton_moni:
                fragment = new MoniNodeListFrag();
                Bundle bundle = new Bundle();
                bundle.putString("uniqueid", userKey);
                fragment.setArguments(bundle);
                ((MoniNodeListFrag) fragment).setDetailListener(mDetailListener);
                break;
            case R.id.radiobutton_control:
                fragment = new ControlNodeListFrag();
                Bundle bun = new Bundle();
                bun.putString("uniqueid", userKey);
                fragment.setArguments(bun);
                ((ControlNodeListFrag) fragment).setCtrDetailListener(mCtrDetailListener);
                break;
//            case 3:
//                fragment = new MapFrag();
//                break;
                default:
                    fragment = new ControlDetailHolderFrag();
                    break;
        }
        return fragment;
    }
    
    MonitorListAdapter2.DetailListener mDetailListener = new MonitorListAdapter2.DetailListener() {
        @Override
        public void showDetail(NodeView2Model n2m) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
//            Fragment fragment = new MoniDetailHolderFrag();
            Fragment fragment = new MoniDataAndChartFrag();
            Bundle bundle = new Bundle();
            bundle.putString("nodetitle", n2m._location);
            bundle.putBoolean("status", n2m._isonline.equals("在线"));
            bundle.putBoolean("isgprs", "GPRS".equalsIgnoreCase(n2m._onlinetype));
            bundle.putInt("nodeno", n2m._nodeno);
            fragment.setArguments(bundle);
            transaction.replace(R.id.detail_content, fragment);
            transaction.addToBackStack(null);
            transaction.commit();  
        }
    };
    ControlDetailListener mCtrDetailListener = new ControlDetailListener() {
        @Override
        public void showDetail(int position, ControlNodeViewModel model) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment fragment = new ControlDetailHolderFrag();
            transaction.replace(R.id.detail_content, fragment);
            Bundle bundle = new Bundle();
            bundle.putString("userkey", userKey);
            bundle.putString("controlnodeuniqueid", model._uniqueid);
            bundle.putString("controlnodeid", ""+model._id);
            bundle.putString("controlnodename", model._nodename);
            fragment.setArguments(bundle);
            transaction.addToBackStack(null);
            transaction.commit();  
        }
    };
}