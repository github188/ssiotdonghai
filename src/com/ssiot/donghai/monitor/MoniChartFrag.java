package com.ssiot.donghai.monitor;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ssiot.donghai.MainActivity;
import com.ssiot.donghai.MyCache;
import com.ssiot.donghai.R;
import com.ssiot.donghai.data.model.view.NodeView2Model;
import com.ssiot.donghai.view.MoreLineView;

import java.util.ArrayList;
import java.util.List;

public class MoniChartFrag extends Fragment{
    public static final String tag = "ChartFragment";
    private FChartBtnClickListener mFChartBtnClickListener;
    
    TextView mTitleView;
    ImageView mOnlineView;
    ImageView mNetTypeView;
    private Bundle mBundle;
    private Context mContext;
    ArrayList<Integer> lineIndexList = new ArrayList<Integer>();
    MoreLineView lineView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mBundle = getArguments();
        mContext = getParentFragment().getParentFragment().getActivity();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_moni_chart, container, false);
        mTitleView = (TextView) v.findViewById(R.id.moni_title);
        mOnlineView = (ImageView) v.findViewById(R.id.moni_status);
        mNetTypeView = (ImageView) v.findViewById(R.id.moni_net_type);
        initTitleBar();
        
        LinearLayout chartSelectBar = (LinearLayout) v.findViewById(R.id.moni_chart_select);
        
        lineView = (MoreLineView) v.findViewById(R.id.more_line_view);
        initSelectBar(chartSelectBar, getCacheData());
        if (null != chartSelectBar.getChildAt(0)){
            ((CheckBox) (chartSelectBar.getChildAt(0))).setChecked(true);
        }
//        initLineView(lineView,lineIndexList);
        return v;
    }
    
    private void initTitleBar(){
        mTitleView.setText(mBundle.getString("nodetitle"));
        mOnlineView.setImageResource(mBundle.getBoolean("status", false) ? R.drawable.online : R.drawable.offline);
        mNetTypeView.setImageResource(mBundle.getBoolean("isgprs", false) ? R.drawable.connect_gprs : R.drawable.connect_zigbee);
        Log.v(tag, "-----nodetitle:"+mBundle.getString("nodetitle"));
    }
    
    private void initSelectBar(LinearLayout bar,List<NodeView2Model> dataList){
        bar.removeAllViews();
        if (null == dataList || dataList.size() == 0){
            Toast.makeText(getActivity(), "没有数据", Toast.LENGTH_SHORT).show();
            Log.v(tag, "没有数据");
            return;
        }
        int size = dataList.get(0)._nodeData_list.size();
        for (int i = 0 ; i < size; i ++){
            CheckBox cb = new CheckBox(mContext);
            cb.setText(dataList.get(0)._nodeData_list.get(i)._name);
            final int cbIndex = i;
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        for (int ind : lineIndexList){
                            if (cbIndex == ind){
                                Log.w(tag, "----already exists");
                                break;
                            }
                        }
                        lineIndexList.add(cbIndex);
                    } else {
                        for (int j = 0; j < lineIndexList.size(); j ++){
                            if (cbIndex == lineIndexList.get(j)) {
                                lineIndexList.remove(j);
                                break;
                            }
                        }
                    }
                    
                    initLineView(lineView,lineIndexList);
                }
            });
            bar.addView(cb);
        }
    }
    
    private void initLineView(MoreLineView lineView, ArrayList<Integer> lineIndexList){
        lineView.setShowPopup(MoreLineView.SHOW_POPUPS_All);// 控制是否显示节点提示
        ArrayList<ArrayList<Float>> dataLists = new ArrayList<ArrayList<Float>>();
        List<NodeView2Model> datas = getCacheData();
        ArrayList<String> bottomStrList = new ArrayList<String>();
        for (int i = 0; i < datas.size(); i++) {
            bottomStrList.add(datas.get(i)._detailTime);
        }
        lineView.setBottomTextList(bottomStrList);
        //第一根线
        
//        int random = (int) (Math.random() * 100 + 1);
//        for (int i = 0; i < 30; i++) {
//            dataList1.add((int) (Math.random() * random));
//        }
        
        for (int index : lineIndexList){
            ArrayList<Float> dataList1 = new ArrayList<Float>();
            if (null != datas){
                for (int i = 0; i < datas.size(); i ++){
                    dataList1.add(datas.get(i)._nodeData_list.get(index)._data);
                }
                dataLists.add(dataList1);
            }
            
        }
        
        Log.v(tag, "---------------line count:"+dataLists.size());
        lineView.setDataList(dataLists);
    }
    
    private List<NodeView2Model> getCacheData(){
        MainActivity ma = (MainActivity) getParentFragment().getParentFragment().getActivity();
        MyCache cache = ma.getCaheManager();
        return (List<NodeView2Model>) cache.get("monidata_list");
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.Chart, menu);
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
        return true;
    }
    
    public void setClickListener(FChartBtnClickListener listen){
        mFChartBtnClickListener = listen;
    }
    
    public interface FChartBtnClickListener {  
        void onFChartBtnClick();  
    }
}