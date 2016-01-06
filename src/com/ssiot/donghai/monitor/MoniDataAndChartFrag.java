package com.ssiot.donghai.monitor;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ssiot.donghai.BaseFragment;
import com.ssiot.donghai.MainActivity;
import com.ssiot.donghai.MyCache;
import com.ssiot.donghai.R;
import com.ssiot.donghai.data.AjaxGetNodesDataByUserkey;
import com.ssiot.donghai.data.model.view.NodeView2Model;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

//2016新设计，只有data和chart
//copy from MoniDetailHolderfrag
public class MoniDataAndChartFrag extends BaseFragment{
    public static final String tag = "MoniDataAndChartFragment";
    private FMoniDataAndChartBtnClickListener mFMoniDataAndChartBtnClickListener;
    private FragmentManager moniChildFragmentManager;
    RadioGroup mRadiogroup;
    private String mTitleText;
    private boolean mStatus;
    private boolean mNetType;
    Bundle mBundle;
    
    TextView mTitleView;
    ImageView mOnlineView;
    ImageView mNetTypeView;
    ListView tableList;
    RelativeLayout chartContainer;
    Spinner chartSpinner;
    List<NodeView2Model> mListData;
    DataTableAdapter mAdapter;
//    private Bundle mBundle;
    private int nodeno = -1;
    private String grainSize = "十分钟";
    private String[] grainList = {"十分钟","小时","天","月","年"};
    
    //---------------------------------chart相关
    private LineChartView chart;
    private LineChartData data;
    private int numberOfLines = 1;
    private int maxNumberOfLines = 4;
    private int numberOfPoints = 12;
    private int selectedSensor = 0;
    private float viewportTop = 100;
    private float viewportBottom = 0;

    float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];

    private boolean hasAxes = true;//显示坐标轴及底纹所有
    private boolean hasAxesNames = false;
    private boolean hasLines = true;
    private boolean hasPoints = true;
    private ValueShape shape = ValueShape.CIRCLE;//点的形状 圆点 方点 星点
    private boolean isFilled = false;
    private boolean hasLabels = false;//每个点显示数值
    private boolean isCubic = true;//曲线是弧形
    private boolean hasLabelForSelected = true;//选中是否会一直显示数值, true时hasLabels不能也true
    private boolean pointsHaveDifferentColor;
    
    private static final int MSG_GET_END = 0;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_GET_END:
                    if (null != mListData){
                        if (mRadiogroup.getCheckedRadioButtonId() == R.id.radio_data){
                            initListView();
                        } else {
                            initChart();
                        }
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
        if (mBundle != null){
            nodeno = mBundle.getInt("nodeno", -1);
            mTitleText = mBundle.getString("nodetitle");
            mStatus = mBundle.getBoolean("status", false);
            mNetType = mBundle.getBoolean("isgprs", false);
            Log.e(tag, "----onCreate----getArguments = nodetitle:"+mTitleText);
        } else {
            Log.e(tag, "----onCreate----getArguments = null");
        }
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_moni_data_chart, container, false);
        moniChildFragmentManager = getChildFragmentManager();
        mRadiogroup = (RadioGroup) v.findViewById(R.id.rg_data_chart);
        mTitleView = (TextView) v.findViewById(R.id.moni_title);
        mOnlineView = (ImageView) v.findViewById(R.id.moni_status);
        mNetTypeView = (ImageView) v.findViewById(R.id.moni_net_type);
        tableList = (ListView) v.findViewById(R.id.table_list);
        chartContainer = (RelativeLayout) v.findViewById(R.id.chart_container);
        chartSpinner = (Spinner) v.findViewById(R.id.chart_select);
        chart = (LineChartView) v.findViewById(R.id.chart);
        mRadiogroup.check(R.id.radio_data);
        mRadiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {  
            @Override  
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //TODO
                Log.v(tag, "----onCheckChanged----" + checkedId);
                if (checkedId == R.id.radio_data){
                    tableList.setVisibility(View.VISIBLE);
                    chartContainer.setVisibility(View.GONE);
                    initListView();
                } else {
                    tableList.setVisibility(View.GONE);
                    chartContainer.setVisibility(View.VISIBLE);
                    initChartSpinner(mListData);
                    initChart();
                }
//                FragmentTransaction transaction = moniChildFragmentManager.beginTransaction();
//                Fragment fragment = getInstanceByIndex(checkedId);
//                transaction.replace(R.id.moni_detail_holder_content, fragment);
//                transaction.commit();
            }
        });
        
        
        initTitleBar();
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
        
        new GetMoniDataThread().start();
        return v;
    }
    
    private void initListView(){//表格
        if (null != mListData && mListData.size() != 0 && null != tableList){
            mAdapter = new DataTableAdapter(getParentFragment().getActivity(), mListData);
            tableList.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        } else {//初始化时可能会多次调用导致出现这个
            Toast.makeText(getActivity(), "error initListView", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void initTitleBar(){
        mTitleView.setText(mBundle.getString("nodetitle"));
        mOnlineView.setImageResource(mBundle.getBoolean("status", false) ? R.drawable.online : R.drawable.offline);
        mNetTypeView.setImageResource(mBundle.getBoolean("isgprs", false) ? R.drawable.connect_gprs : R.drawable.connect_zigbee);
        Log.v(tag, "-----odetitle:"+mBundle.getString("nodetitle"));
    }
    
    private void initChartSpinner(List<NodeView2Model> daList){
        if (null == daList || daList.size() == 0){
            return;
        }
        ArrayList<String> mSensorNames = new ArrayList<String>();
        if (null != chartSpinner && null != daList && daList.size() != 0){
            int sensorCount = daList.get(0)._nodeData_list.size();
            for (int i = 0; i < sensorCount; i ++){
                String str = daList.get(0)._nodeData_list.get(i)._name;
                mSensorNames.add(str);
            }
            ArrayAdapter<String> arr_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,
                    mSensorNames);
            chartSpinner.setAdapter(arr_adapter);
            chartSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedSensor = position;
                    initChart();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }
    
    private void initChart(){
        chart.setOnValueTouchListener(new ValueTouchListener());
        generateValues();
        generateData();
        chart.setViewportCalculationEnabled(true);
        resetViewport();
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
        MainActivity ma = (MainActivity) getParentFragment().getActivity();
        MyCache cache = ma.getCaheManager();
        cache.put("monidata_list", mListData);
    }
    
    private List<NodeView2Model> getCacheData(){
        MainActivity ma = (MainActivity) getParentFragment().getActivity();
        MyCache cache = ma.getCaheManager();
        return (List<NodeView2Model>) cache.get("monidata_list");
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.monidetail_holder, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }
    
    public void setClickListener(FMoniDataAndChartBtnClickListener listen){
        mFMoniDataAndChartBtnClickListener = listen;
    }
    
    public interface FMoniDataAndChartBtnClickListener {  
        void onFMoniDataAndChartBtnClick();  
    }
    
    private class DataTableAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private Context context;

        List<NodeView2Model> mData;

        public DataTableAdapter(Context c, List<NodeView2Model> d) {
            mData = d;
            context = c;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if (mData.size() == 0) {// 没有值就不显示标题，否则在buildHeaderView会空指针
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
            if (0 == position) {
                convertView = buildHeaderView(mData.get(0));
            } else {
                convertView = buildView(mData.get(position - 1));
            }
            return convertView;
        }

        private View buildView(NodeView2Model n2m) {
            long time1 = SystemClock.uptimeMillis();
            LinearLayout rootLayout = new LinearLayout(context);// 在xml中固定了listview的高度后就没重复调用了
            rootLayout.setOrientation(LinearLayout.HORIZONTAL);
            TextView timeText = new TextView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            timeText.setText("" + n2m._detailTime);
            timeText.setEms(5);
            timeText.setGravity(Gravity.CENTER);
            timeText.setMaxLines(2);
            timeText.setBackgroundColor(getResources().getColor(R.color.ssiot_title_yellow));
            rootLayout.addView(timeText, lp);

            View dividerView = new View(context);
            LinearLayout.LayoutParams lpdivider = new LinearLayout.LayoutParams(1,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            rootLayout.addView(dividerView, lpdivider);

            // View leftView = timeText;
            for (int j = 0; j < n2m._nodeData_list.size(); j++) {
                TextView t = new TextView(context);
                if (null != n2m._nodeData_list.get(j)._unit) {
                    t.setText("" + n2m._nodeData_list.get(j)._data
                            + n2m._nodeData_list.get(j)._unit);
                } else {
                    t.setText("");
                }
                t.setSingleLine();
                t.setGravity(Gravity.CENTER);
                // t.setMaxEms(5);
                // t.setMinEms(5);
                t.setEms(5);
                // t.setPadding(0, 10, 0, 10);
                t.setBackgroundColor(getResources().getColor(R.color.ssiot_title_yellow));
                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                // lp2.leftMargin = 10;
                // lp2.rightMargin = 10;
                rootLayout.addView(t, lp2);
                // leftView = t;
            }
            // Log.v(tag, "----cost time:" +
            // (SystemClock.uptimeMillis()-time1));
            return rootLayout;
        }

        private View buildHeaderView(NodeView2Model n2m) {
            LinearLayout rootLayout = new LinearLayout(context);// 在xml中固定了listview的高度后就没重复调用了
                                                                // 解决了一个bug
            rootLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView timeText = new TextView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            timeText.setText("时间");
            timeText.setGravity(Gravity.CENTER);
            timeText.setEms(5);
            timeText.setBackgroundColor(getResources().getColor(R.color.datalist_green));
            rootLayout.addView(timeText, lp);

            // View leftView = timeText;
            for (int j = 0; j < n2m._nodeData_list.size(); j++) {
                TextView t = new TextView(context);
                t.setText(n2m._nodeData_list.get(j)._name);
                t.setSingleLine();
                t.setMaxEms(5);
                t.setMinEms(5);
                t.setGravity(Gravity.CENTER);
                t.setBackgroundColor(getResources().getColor(R.color.datalist_header));
                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                // lp2.leftMargin = 10;
                // lp2.rightMargin = 10;
                rootLayout.addView(t, lp2);
                // leftView = t;
            }
            return rootLayout;
        }
    }
    
    //-------------------------------------------------------chart相关函数 开始
    
    private void generateValues() {
        List<NodeView2Model> n2datas = mListData;
        if (null == n2datas || n2datas.size() == 0){
            return;
        }
        numberOfPoints = n2datas.size();
        randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];
        viewportTop = viewportBottom = n2datas.get(0)._nodeData_list.get(selectedSensor)._data;
        for (int i = 0; i < maxNumberOfLines; ++i) {
            for (int j = 0; j < numberOfPoints; ++j) {
//                randomNumbersTab[i][j] = (float) Math.random() * 100f;
                float val = n2datas.get(numberOfPoints - j - 1)._nodeData_list.get(selectedSensor)._data;//顺序反转一下！
                viewportBottom = viewportBottom > val ? val : viewportBottom;
                viewportTop =  viewportTop > val ? viewportTop : val;
                randomNumbersTab[i][j] = val;
            }
        }
        if (viewportBottom >= viewportTop){
            viewportBottom = viewportBottom - 1;
            viewportTop = viewportBottom + 2;
        }
        float totalHeight = viewportTop - viewportBottom;//上下空出一点 防止曲线溢出
        viewportBottom = viewportBottom - totalHeight/20;
        viewportTop = viewportTop + totalHeight/20;
        Log.v(tag, "-----viewportBottom:" + viewportBottom + "  viewportTop:" + viewportTop  + " first:" + randomNumbersTab[0][0]);
    }

    private void reset() {
        numberOfLines = 1;

        hasAxes = true;
        hasAxesNames = true;
        hasLines = true;
        hasPoints = true;
        shape = ValueShape.CIRCLE;
        isFilled = false;
        hasLabels = false;
        isCubic = false;
        hasLabelForSelected = false;
        pointsHaveDifferentColor = false;

        chart.setValueSelectionEnabled(hasLabelForSelected);
        resetViewport();
    }

    private void resetViewport() {
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = viewportBottom;
        v.top = viewportTop;
        v.left = 0;
        v.right = numberOfPoints - 1;
        if (v.right < 1){
            v.right = 1;
        }
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    private void generateData() {

        List<Line> lines = new ArrayList<Line>();
        for (int i = 0; i < numberOfLines; ++i) {

            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < numberOfPoints; ++j) {
                values.add(new PointValue(j, randomNumbersTab[i][j]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLORS[i]);
            line.setShape(shape);
            line.setCubic(isCubic);
            line.setFilled(isFilled);
            line.setHasLabels(hasLabels);
            line.setHasLabelsOnlyForSelected(hasLabelForSelected);
            chart.setValueSelectionEnabled(hasLabelForSelected);//jingbo copy
            line.setHasLines(hasLines);
            line.setHasPoints(hasPoints);
            if (pointsHaveDifferentColor){
                line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
            }
            lines.add(line);
        }

        data = new LineChartData(lines);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setName("X 轴");
                axisY.setName("Y 轴");
            }
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);
    }
    
    
    
    
    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            if (null != mListData && mListData.size() != 0){
                String timeStr = mListData.get(numberOfPoints - pointIndex - 1)._detailTime;//顺序反转一下
                Toast.makeText(getActivity(), "时间:" + timeStr + ",传感器值:" + value.getY(), Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(getActivity(), "error onValueSelected", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub

        }

    }
}