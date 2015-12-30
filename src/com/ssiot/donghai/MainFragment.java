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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.ssiot.donghai.view.IconView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainFragment extends Fragment{
    public static final String tag = "MainFragment";
    private FMainBtnClickListener mFMainBtnClickListener;
    public int[] iconIds = {R.drawable.icon_monitor_select,R.drawable.icon_control_select,R.drawable.icon_history_select,
            R.drawable.icon_video_select,R.drawable.icon_expert_select,R.drawable.icon_info_select};//fffcdc d9f0ea ffe0e0 eceeff
//    private int[] iconBackColors = {R.color.icon_yellow,R.color.icon_green,R.color.icon_pink,R.color.icon_purple,R.color.icon_yellow,R.color.icon_green};
    private int[] iconBackColors = {R.drawable.bk_yellow_select,R.drawable.bk_green_select,R.drawable.bk_pink_select,R.drawable.bk_purple_select,
            R.drawable.bk_yellow_select,R.drawable.bk_green_select};
    private int[] iconStrings = {R.string.iconstr_monitor,R.string.iconstr_control,R.string.iconstr_history,
            R.string.iconstr_video,R.string.iconstr_expert,R.string.iconstr_info};
    
    public String clickString = "";
     
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(tag, "------onCreate-----");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mFMainBtnClickListener = (FMainBtnClickListener) getActivity();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(tag, "----------oncreateView");
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//        ImageButton b = (ImageButton) rootView.findViewById(R.id.btn_main_f);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.v(tag, "------------button click");
//                if (null != mFMainBtnClickListener){
//                    mFMainBtnClickListener.onFMainBtnClick(clickString);
//                }
//            }
//        });
        
//        initGridView(rootView);
        initIconViews(rootView);
        return rootView;
    }
    
    public void initIconViews(View rootView){
        IconView mMonitor = (IconView) rootView.findViewById(R.id.btn_monitor);
        IconView mControl = (IconView) rootView.findViewById(R.id.btn_control);
        IconView mHistory = (IconView) rootView.findViewById(R.id.btn_history);
        IconView mVideo = (IconView) rootView.findViewById(R.id.btn_video);
        IconView mExpert = (IconView) rootView.findViewById(R.id.btn_expert);
        IconView mInfo = (IconView) rootView.findViewById(R.id.btn_info);
        mMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mFMainBtnClickListener){
                    mFMainBtnClickListener.onFMainBtnClick(getResources().getString(R.string.iconstr_monitor));
                }
            }
        });
        mControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mFMainBtnClickListener){
                    mFMainBtnClickListener.onFMainBtnClick(getResources().getString(R.string.iconstr_control));
                }
            }
        });
        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mFMainBtnClickListener){
                    mFMainBtnClickListener.onFMainBtnClick(getResources().getString(R.string.iconstr_history));
                }
            }
        });
        mVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mFMainBtnClickListener){
                    mFMainBtnClickListener.onFMainBtnClick(getResources().getString(R.string.iconstr_video));
                }
            }
        });
        mExpert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mFMainBtnClickListener){
                    mFMainBtnClickListener.onFMainBtnClick(getResources().getString(R.string.iconstr_expert));
                }
            }
        });
        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mFMainBtnClickListener){
                    mFMainBtnClickListener.onFMainBtnClick(getResources().getString(R.string.iconstr_info));
                }
            }
        });
    }
    
    
    public void onClickIconView(View v) {
        // TODO Auto-generated method stub
        Log.v(tag, "--------onClick------");
    }
    
    public void initGridView(View rootView){
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < 6; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemImage", iconIds[i]);// 添加图像资源的ID
            map.put("ItemText", getResources().getString(iconStrings[i]));
            map.put("ItemBack", getResources().getColor(iconBackColors[i]));
            lstImageItem.add(map);
        }

        final ArrayList<HashMap<String, Object>> itemDatas = lstImageItem;
        GridAdapter mAdapter = new GridAdapter(getActivity(), //jingbo 原先是simpleAdapter
                lstImageItem,// 数据来源
                R.layout.item_icon,// night_item的XML实现
                // 动态数组与ImageItem对应的子项
                new String[] {
                        "ItemImage", "ItemText"
                },
                // ImageItem的XML文件里面的一个ImageView,两个TextView ID
                new int[] {
                        R.id.item_image, R.id.item_text
                });
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                String itmTxt = (String) itemDatas.get(position).get("ItemText");
                Log.v(tag, "-----------------onclick:" + itmTxt);
                if (itmTxt.equals(getResources().getString(R.string.iconstr_monitor))){
                    
                } else if (itmTxt.equals(getResources().getString(R.string.iconstr_control))){
                    
                } else if (itmTxt.equals(getResources().getString(R.string.iconstr_history))){
                    
                } else if (itmTxt.equals(getResources().getString(R.string.iconstr_video))){
                    
                } else if (itmTxt.equals(getResources().getString(R.string.iconstr_expert))){
                    
                } else if (itmTxt.equals(getResources().getString(R.string.iconstr_info))){
                    
                }
                if (null != mFMainBtnClickListener){
                    clickString = itmTxt;
                    mFMainBtnClickListener.onFMainBtnClick(clickString);
                }
            }
        });
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        inflater.inflate(R.menu.menu_f_main, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case R.id.action_frag_main_setting:
                if (null != mFMainBtnClickListener){
                    mFMainBtnClickListener.onFMainBtnClick("setting");
                }
                break;

            default:
                break;
        }
        return true;
    }
    
    public void setClickListener(FMainBtnClickListener listen){
        mFMainBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FMainBtnClickListener {  
        void onFMainBtnClick(String str);  
    }
}