package com.ssiot.donghai.monitor;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.ssiot.donghai.BaseFragment;
import com.ssiot.donghai.MainActivity;
import com.ssiot.donghai.MyCache;
import com.ssiot.donghai.R;
import com.ssiot.donghai.data.AjaxGetNodesDataByUserkey;
import com.ssiot.donghai.data.model.view.NodeView2Model;
import java.util.List;
import java.util.Set;

public class MoniDataFrag extends BaseFragment{
    public static final String tag = "DataFragment";
    private FDataBtnClickListener mFDataBtnClickListener;
    TextView mTitleView;
    ImageView mOnlineView;
    ImageView mNetTypeView;
    ListView tableList;
    List<NodeView2Model> mListData;
    DataTableAdapter mAdapter;
    private Bundle mBundle;
    private int nodeno = -1;
    private String grainSize = "十分钟";
    private String[] grainList = {"十分钟","小时","天","月","年"};
    
    private static final int MSG_GET_END = 0;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_GET_END:
                    if (null != mListData){//
                        mAdapter = new DataTableAdapter(getParentFragment().getActivity(), mListData);
                        tableList.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
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
        mBundle = getArguments();
        nodeno = mBundle.getInt("nodeno", -1);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_moni_data, container, false);
        mTitleView = (TextView) v.findViewById(R.id.moni_title);
        mOnlineView = (ImageView) v.findViewById(R.id.moni_status);
        mNetTypeView = (ImageView) v.findViewById(R.id.moni_net_type);
        tableList = (ListView) v.findViewById(R.id.table_list);
        initTitleBar();
        new GetMoniDataThread().start();
        
        SeekBar sb = (SeekBar) v.findViewById(R.id.moni_time_checker);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                grainSize = grainList[progress];
                new GetMoniDataThread().start();
            }
        });
        return v;
    }
    
    private void initTitleBar(){
        mTitleView.setText(mBundle.getString("nodetitle"));
        mOnlineView.setImageResource(mBundle.getBoolean("status", false) ? R.drawable.online : R.drawable.offline);
        mNetTypeView.setImageResource(mBundle.getBoolean("isgprs", false) ? R.drawable.connect_gprs : R.drawable.connect_zigbee);
        Log.v(tag, "-----odetitle:"+mBundle.getString("nodetitle"));
    }
    
    private class GetMoniDataThread extends Thread{
        @Override
        public void run() {
            sendShowMyDlg("正在查询");
            if (nodeno >= 0){
                List<NodeView2Model> nList = new AjaxGetNodesDataByUserkey().GetNodesDataByUserkeyAndType(MainActivity.mUniqueID, ""+nodeno, grainSize);

                mListData = nList;
//                Log.v(tag, "------------size:"+nList.size());
//                if (null != nList){
//                    for (int i = 0; i < nList.size(); i ++){
//                        String str = "";
//                        str += nList.get(i)._updatetime + ":::::";
//                        for (int j = 0; j < nList.get(i)._nodeData_list.size(); j ++){
//                            str += nList.get(i)._nodeData_list.get(j)._name + " " +  nList.get(i)._nodeData_list.get(j)._data +" ";
//                        }
//                        Log.v(tag, str);
//                    }
//                }
            } else {
                Log.e(tag, "----!!!! nodeno < 0!");
            }
            
            sendDismissDlg();
            setCacheData();
//            SimpleAdapter sAdapter = new SimpleAdapter(getParentFragment().getActivity(), data, resource, from, to)
            mHandler.sendEmptyMessage(MSG_GET_END);
        }
    }
    
    private void setCacheData(){
        MainActivity ma = (MainActivity) getParentFragment().getParentFragment().getActivity();
        MyCache cache = ma.getCaheManager();
        cache.put("monidata_list", mListData);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.data, menu);
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
    
    public void setClickListener(FDataBtnClickListener listen){
        mFDataBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FDataBtnClickListener {  
        void onFDataBtnClick();  
    }
    
    private class DataTableAdapter extends BaseAdapter{
        
        private LayoutInflater mInflater;
        private Context context;
        
        List<NodeView2Model> mData;
        
        public DataTableAdapter(Context c, List<NodeView2Model> d){
            mData = d;
            context = c;
            mInflater = LayoutInflater.from(context);
        }
        
        @Override
        public int getCount() {
            if (mData.size() == 0){//没有值就不显示标题，否则在buildHeaderView会空指针
                return 0;
            }
            return mData.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            /*
            convertView = new RelativeLayout(context);
            RelativeLayout rootLayout = (RelativeLayout) convertView;
            if (0 == position){
                
            } else {
                
            }
            
            NodeView2Model n2m = mData.get(position);
            TextView timeText = new TextView(context);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            timeText.setText("" + n2m._updatetime);
            rootLayout.addView(timeText, lp);
            
            View leftView = timeText;
            for (int j = 0; j < n2m._nodeData_list.size(); j ++){
                TextView t = new TextView(context);
                t.setText(""+n2m._nodeData_list.get(j)._data + n2m._nodeData_list.get(j)._unit);
                RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp2.addRule(RelativeLayout.RIGHT_OF, leftView.getId());
                rootLayout.addView(t,lp2);
                leftView = t;
            }*/
//            Log.v(tag, "-----getview----" + position);
            if (0 == position) {
                convertView = buildHeaderView(mData.get(0));
            } else {
                convertView = buildView(mData.get(position - 1));
            }
            return convertView;
        }
        
        private View buildView(NodeView2Model n2m){
            long time1 = SystemClock.uptimeMillis();
            LinearLayout rootLayout = new LinearLayout(context);//在xml中固定了listview的高度后就没重复调用了
            rootLayout.setOrientation(LinearLayout.HORIZONTAL);
            TextView timeText = new TextView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            timeText.setText("" + n2m._detailTime);
            timeText.setEms(5);
            timeText.setGravity(Gravity.CENTER);
            timeText.setMaxLines(2);
            timeText.setBackgroundColor(getResources().getColor(R.color.ssiot_title_yellow));
            rootLayout.addView(timeText, lp);
            
            View dividerView = new View(context);
            LinearLayout.LayoutParams lpdivider = new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT);
            rootLayout.addView(dividerView, lpdivider);
            
//            View leftView = timeText;
            for (int j = 0; j < n2m._nodeData_list.size(); j ++){
                TextView t = new TextView(context);
                if (null != n2m._nodeData_list.get(j)._unit){
                    t.setText(""+n2m._nodeData_list.get(j)._data + n2m._nodeData_list.get(j)._unit);
                } else {
                    t.setText("");
                }
                t.setSingleLine();
                t.setGravity(Gravity.CENTER);
//                t.setMaxEms(5);
//                t.setMinEms(5);
                t.setEms(5);
//                t.setPadding(0, 10, 0, 10);
                t.setBackgroundColor(getResources().getColor(R.color.ssiot_title_yellow));
                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
//                lp2.leftMargin = 10; 
//                lp2.rightMargin = 10;
                rootLayout.addView(t,lp2);
//                leftView = t;
            }
//            Log.v(tag, "----cost time:" + (SystemClock.uptimeMillis()-time1));
            return rootLayout;
        }
        
        private View buildHeaderView(NodeView2Model n2m){
            LinearLayout rootLayout = new LinearLayout(context);//在xml中固定了listview的高度后就没重复调用了 解决了一个bug
            rootLayout.setOrientation(LinearLayout.HORIZONTAL);
            
            TextView timeText = new TextView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            timeText.setText("时间");
            timeText.setGravity(Gravity.CENTER);
            timeText.setEms(5);
            timeText.setBackgroundColor(getResources().getColor(R.color.ssiot_title_yellow));
            rootLayout.addView(timeText, lp);
            
//            View leftView = timeText;
            for (int j = 0; j < n2m._nodeData_list.size(); j ++){
                TextView t = new TextView(context);
                t.setText(n2m._nodeData_list.get(j)._name);
                t.setSingleLine();
                t.setMaxEms(5);
                t.setMinEms(5);
                t.setGravity(Gravity.CENTER);
                t.setBackgroundColor(getResources().getColor(R.color.ssiot_title_yellow));
                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                lp2.leftMargin = 10; 
//                lp2.rightMargin = 10;
                rootLayout.addView(t,lp2);
//                leftView = t;
            }
            return rootLayout;
        }
        
    }
}