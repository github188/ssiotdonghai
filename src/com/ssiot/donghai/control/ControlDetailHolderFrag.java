package com.ssiot.donghai.control;

import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ssiot.donghai.BaseFragment;
import com.ssiot.donghai.R;
import com.ssiot.donghai.control.RuleAdapter.DeleteListener;
import com.ssiot.donghai.control.RuleAdapter.TimeCountDownHolder;
import com.ssiot.donghai.data.AjaxGetControlActionInfo;
import com.ssiot.donghai.data.AjaxGetNodesDataByUserkey;
import com.ssiot.donghai.data.ControlController;
import com.ssiot.donghai.data.model.ControlActionInfoModel;
import com.ssiot.donghai.data.model.view.ControlDeviceView3Model;
import com.ssiot.donghai.view.HVScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControlDetailHolderFrag extends BaseFragment{
    public static final String tag = "ContrDetailHoldFragment";
    private FContrDetailHoldBtnClickListener mFContrDetailHoldBtnClickListener;
    ViewPager pager = null;
    PagerTabStrip tabStrip = null;
    ArrayList<View> viewContainter = new ArrayList<View>();
    ArrayList<String> titleContainer = new ArrayList<String>();
    ArrayList<RadioButton> mRadioButtons = new ArrayList<RadioButton>();
    private LinearLayout mIndicator;
    private String userkey;
    private String controlnodeuniqueid;
    private String controlnodeid;
    private String controlnodename;
    List<ControlDeviceView3Model> listDatas;
    HorizontalScrollView mScrollView;
    LayoutInflater mInflater;
    
    Timestamp mStartTime;//添加规则时使用的
    Timestamp mEndTime;
    
    public static final String[] spinnerDatas = {"5分钟","10分钟","15分钟","20分钟","25分钟","30分钟","35分钟","40分钟","45分钟","50分钟","55分钟"}; 
    
    private static final int MSG_GET_CONTROLDETAIL_END = 0;
    public static final int MSG_TIME_COUNT_DOWN = 1;
    public static final int MSG_REFRESH = 2;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            Log.v(tag, "-----------handleMessage----------what:" + msg.what);
            switch (msg.what) {
                case MSG_GET_CONTROLDETAIL_END:
                    if (null != listDatas && null != mScrollView){
                        Log.v(tag, "--------size:"+listDatas.size());
                        //TODO
                        viewContainter.clear();
                        for (int k = 0;k < listDatas.size();k ++){
                            View view1 = mInflater.inflate(R.layout.control_tab, pager,false);
                            viewContainter.add(view1);
                        }
                        initIndicatorView(mScrollView,viewContainter.size());
                        initViewPagerbyData();
                    }
                    break;
                case MSG_TIME_COUNT_DOWN:
                    TimeCountDownHolder t = (TimeCountDownHolder) msg.obj;
                    if (null != t.textView){
                        if (t.mEndDate.getTime() > new Date().getTime()){
                            int seconds = (int) (t.mEndDate.getTime()- new Date().getTime()) / 1000;
                            String str = buildCountDownStr(seconds);
                            t.textView.setText(str);
                            Message m = obtainMessage(MSG_TIME_COUNT_DOWN);
                            m.obj = t;
                            sendMessageDelayed(m, 1000);
                        } else {
                            t.textView.setText("运行结束");
                        }
                    } else {
                        removeMessages(MSG_TIME_COUNT_DOWN);
                    }
                    break;
                case MSG_REFRESH:
                    new GetControlActionInfoThread().start();
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
        if (null != getArguments()){
            userkey = getArguments().getString("userkey");
            controlnodeuniqueid = getArguments().getString("controlnodeuniqueid");
            controlnodeid = getArguments().getString("controlnodeid");
            controlnodename = getArguments().getString("controlnodename");
        } else {
            Log.e(tag, "----!!!! getArguments = null");
        }
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        View v = inflater.inflate(R.layout.frag_contr_detail_holder, container, false);
        TextView title = (TextView) v.findViewById(R.id.control_d_node_title);
        title.setText(controlnodename);
        pager = (ViewPager) v.findViewById(R.id.viewpager);
        mScrollView = (HorizontalScrollView) v.findViewById(R.id.control_d_indicator);
        TextView ctr_d_btn_newtiming = (TextView) v.findViewById(R.id.ctr_d_btn_newtiming);
        ctr_d_btn_newtiming.setOnClickListener(new View.OnClickListener() {//新建定时规则
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_TIMING);
            }
        });
        TextView ctr_new_tri = (TextView) v.findViewById(R.id.ctr_d_btn_newtrigger);
        ctr_new_tri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_TRIGGER);
            }
        });
//        int tabSize = 17;
//        for (int i = 0; i < tabSize;i ++){
//            View view1 = inflater.inflate(R.layout.control_tab, pager,false);
//            viewContainter.add(view1);
//            titleContainer.add(""+i);
//        }
//        
//        initIndicatorView(mScrollView,viewContainter.size());
//        initViewPagerbyData();
        
        new GetControlActionInfoThread().start();
        return v;
    }
    
    private void initIndicatorView(HorizontalScrollView scrollView,int size){
//        FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
//                Gravity.CENTER);
        RadioGroup rGroup = new RadioGroup(getParentFragment().getActivity());//放在xml中会导致布局不对 TODO 代码中布局也有问题
//        rGroup.setLayoutParams(flp);
        rGroup.setGravity(Gravity.CENTER);
        rGroup.setOrientation(RadioGroup.HORIZONTAL);
        mRadioButtons.clear();
        for (int i = 1; i <= size; i ++){
            RadioButton t = new RadioButton(getActivity());
//            t.setText(""+i);
            t.setButtonDrawable(null);
//            t.setBackgroundResource(R.drawable.radiobutton_indicator);
            t.setId(i);
            t.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT));
//            TextView ss = new TextView(getActivity());
            mRadioButtons.add(t);
            rGroup.addView(t);
        }
        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (null != pager){
                    RadioButton radioButton = (RadioButton) group.findViewById(checkedId);
                    
                    Log.v(tag, "----------onCheckedChanged----------"+checkedId + radioButton.isChecked());
                    if (radioButton.isChecked()){
                        pager.setCurrentItem(checkedId-1, true);
                    }
                }
            }
        });
        rGroup.check(1);
        scrollView.removeAllViews();
        scrollView.addView(rGroup);
    }
    
    private void fillPagerViewDataFromList(View v,List<ControlDeviceView3Model> datas, int position){
        if (null != mInflater && null != v){
//            View v = mInflater.inflate(R.layout.control_tab, pager,false);
            TextView tIndex = (TextView) v.findViewById(R.id.ctr_t_index);
            TextView tName = (TextView) v.findViewById(R.id.ctr_t_name);
            ListView tListView = (ListView) v.findViewById(R.id.ctr_t_action_list);
            TextView openBtn = (TextView) v.findViewById(R.id.ctr_t_btn_open);
            TextView closeBtn = (TextView) v.findViewById(R.id.ctr_t_btn_close);
            final ControlDeviceView3Model m = datas.get(position);
            openBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            btn(controlnodeuniqueid, ""+m.DeviceNo, "open");
                        }
                    }).start();
                }
            });
            closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            btn(controlnodeuniqueid, ""+m.DeviceNo, "close");
                        }
                    }).start();
                }
            });
            tIndex.setText(""+(position +1));
            tName.setText(datas.get(position).DeviceName);
//            tBtnCtr
            RuleAdapter adapter = new RuleAdapter(getParentFragment().getActivity(), datas.get(position).ActionList, mHandler,mDeleteListener);
            tListView.setAdapter(adapter);
        }
    }
    
    private void initViewPagerbyData() {
        pager.setAdapter(new PagerAdapter() {

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // return super.instantiateItem(container, position);
                Log.v("pageradapter", "-----instantiateItem------" + position);
                View page = viewContainter.get(position);
                container.addView(page);
                fillPagerViewDataFromList(page, listDatas, position);
                return viewContainter.get(position);
            }
            
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return viewContainter.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                // super.destroyItem(container, position, object);
                ((ViewPager) container).removeView(viewContainter.get(position));
            }
        });
        pager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
                Log.d(tag, "--------changed:" + arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
//                Log.d(tag, "-------scrolled arg0:" + arg0 + " arg1:" + arg1 + " arg2:" + arg2);
                
            }

            @Override
            public void onPageSelected(int arg0) {
                Log.d(tag, "------selected:" + arg0);
                if (mRadioButtons.size() > arg0){
                    mRadioButtons.get(arg0).setChecked(true);
                }
            }
        });
        pager.setCurrentItem(0);
    }
    
    public class GetControlActionInfoThread extends Thread{
        @Override
        public void run() {
            listDatas = new AjaxGetNodesDataByUserkey().GetDeviceActionInfo(controlnodeid, controlnodeuniqueid);
            mHandler.sendEmptyMessage(MSG_GET_CONTROLDETAIL_END);
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.ContrDetailHold, menu);
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
    
    @Override
    public void onDestroyView() {
        Log.v(tag, "-------onDestroyView-------");
        mHandler.removeMessages(MSG_GET_CONTROLDETAIL_END);
        mHandler.removeMessages(MSG_TIME_COUNT_DOWN);
        mHandler.removeMessages(MSG_REFRESH);
        super.onDestroyView();
    }
    
//    public boolean canGoback() {
//        return true;
//    };
//    
//    @Override
//    public void onMyBackPressed() {
//        Log.v(tag, "----onMyBackPressed----");
//        getFragmentManager().popBackStack();
//    }
    
    private void btn(String uniqueIDs,String deviceNos, String isOpens){
        String ids = "";
        String openTimes = "";
        List<ControlActionInfoModel> data = new AjaxGetControlActionInfo().GetControlActionInfo(uniqueIDs, deviceNos);//TODO
        if (null != data){
            for (int i = 0; i < data.size(); i ++){//此设备下控制规则是1的，找出最后一个 运行时间 ？
                if (data.get(i)._controltype == 1){
                    ids = "" + data.get(i)._id;
                    openTimes = data.get(i)._controlcondition;
                }
            }
        }
        
        if ("open".equalsIgnoreCase(isOpens)) {
//            var options = {};
//            options.title = "立即开启";
//            options.width = 250;
//            options.height = 100;
//            options.drag = "true";
//            options.content = "iframe:Controlopen.html?uniqueIDs=" + uniqueIDs + "&userkey=" + userkey + "&deviceNos=" + deviceNos + "&updateid=" + ids + "&runtimes=" + openTimes;//+ "&isOpens=" + isOpens;
//            options.callback = "";
//            startDia(options);
            startDia(deviceNos, ids);
        } else {
            
            String data2 = new AjaxGetNodesDataByUserkey().ControlDevice(uniqueIDs, deviceNos, "无", isOpens);
            if ("true".equalsIgnoreCase(data2)){
//                getdate();//TODO
            } else {
                //show error
                Log.e(tag, "---------__!!!!!!!!!!!!!!!!!");
            }
            mHandler.sendEmptyMessage(MSG_REFRESH);
        }
        
    }
    
    private void delstate(int id){
        boolean ret = new AjaxGetNodesDataByUserkey().DelControl(id);
        if (ret){
            
        } else {
            Log.e(tag, "------delstate------failed:" + id);
        }
        mHandler.sendEmptyMessage(MSG_REFRESH);
    }
    
    private void showDialog(int controlType,String UniqueID, String nodeno){
        if (3 == controlType){//定时
            
        } else if (5 == controlType){
            
        } else if (6 == controlType){
            
        }
    }
    
    //paraType=1 表示添加
    private void showDia(int paraType, String userkey,String UniqueID,String controlType,String nodeno,
            String updateid,String deviceno){//
        if (paraType == 1){//添加
            
        } else {//编辑
            
        }
    }
    
    private void startDia(String deviceNo,String id){
        OpenDiaFrag dia = new OpenDiaFrag(deviceNo, id);
        dia.show(getFragmentManager(), "tag_opendiafrag");
    }
    
    public class OpenDiaFrag extends DialogFragment{
        private Spinner spinner;
        private int selectedTime = 5;
        String deviceNos;
        private String ID;
        
        public OpenDiaFrag(String deviceNos,String id){
            this.deviceNos = deviceNos;
            this.ID = id;
        }
        
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View view = inflater.inflate(R.layout.dia_ctr_open, container);  
//            getDialog().setTitle("Hello");  
//            
//            return view;  
//        }
        
        private void initSpinner(View rootView){
            spinner = (Spinner) rootView.findViewById(R.id.d_c_o_spinner);
            ArrayAdapter<String> arr_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,spinnerDatas);
            arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(arr_adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedTime = (1 + position) * 5;//minites
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());  
            LayoutInflater inflater = getActivity().getLayoutInflater();  
            View view = inflater.inflate(R.layout.dia_ctr_open, null);
            builder.setView(view)
                    .setTitle(R.string.opennow)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Log.v(tag, "---onClickPositive---" + userkey + " "+selectedTime + " " +controlnodeuniqueid + " " + deviceNos);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    boolean ret = new AjaxGetNodesDataByUserkey().SaveControlAdd(userkey, ""+selectedTime, controlnodeuniqueid, 1, deviceNos, ID);
                                    mHandler.sendEmptyMessage(MSG_REFRESH);
                                }
                            }).start();
                        }
                    }).setNegativeButton(R.string.cancel, null);
            initSpinner(view);
            return builder.create();
        }
    }
    
    private static final int DIALOG_TIMING = 1;
    private static final int DIALOG_TRIGGER = 2;
    private void showDialog(int dialog){
        switch (dialog) {
            case DIALOG_TIMING:
                AlertDialog.Builder buil = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.dia_ctr_timing2, null);
//                final HVScrollView hv = (HVScrollView) view.findViewById(R.id.hv_view);
                final HorizontalScrollView hv = (HorizontalScrollView) view.findViewById(R.id.two_timepick);
//                final RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.hv_container);
                final RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.hv_con);
                TimePicker tpickS = (TimePicker) view.findViewById(R.id.d_c_o_timepick_start);
                TimePicker tpickE = (TimePicker) view.findViewById(R.id.d_c_o_timepick_end);
                tpickS.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        // TODO Auto-generated method stub
                        Timestamp tNow = new Timestamp(System.currentTimeMillis());
                        mStartTime = new Timestamp(tNow.getYear(), tNow.getMonth(), tNow.getDate(), hourOfDay, minute, 0, 0);
                    }
                });
                tpickE.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        Timestamp tNow = new Timestamp(System.currentTimeMillis());
                        mEndTime = new Timestamp(tNow.getYear(), tNow.getMonth(), tNow.getDate(), hourOfDay, minute, 0, 0);
                    }
                });
                final GridView gv = (GridView) view.findViewById(R.id.device_pick);
                final LinearLayout ll = (LinearLayout) view.findViewById(R.id.time_title);
                ArrayList<DeviceCheckerData> as = new ArrayList<DeviceCheckerData>();
                for (ControlDeviceView3Model c : listDatas){
                    as.add(new DeviceCheckerData(c.DeviceNo, c.DeviceName, false));
                }
                final ControlDeviceGridAdapter adapter = new ControlDeviceGridAdapter(getActivity(), as);
                gv.setAdapter(adapter);
                tpickS.setIs24HourView(true);
                tpickE.setIs24HourView(true);
                buil.setView(view).setTitle(R.string.timing).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mEndTime.getTime() <= mStartTime.getTime()){
                            Toast.makeText(getParentFragment().getActivity(), "结束时间小于开始时间!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONArray jArray= new JSONArray();
                        JSONObject jsonObj = new JSONObject();
                        try {
                            jsonObj.put("ID","1");
                            jsonObj.put("StartTime",buildTimeStr(mStartTime));
                            jsonObj.put("EndTime",buildTimeStr(mEndTime));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }//"{ ID: \"1\", StartTime: "", EndTime: "" }";
                        
                        jArray.put(jsonObj);
                        final String conditionStr = jArray.toString();
                        new Thread(new Runnable() {
                            
                            @Override
                            public void run() {
                                boolean ret = new ControlController().SaveControlTimeUser(conditionStr, null, controlnodeuniqueid, 3, adapter.getSelectedListStr(), "");
                                if (ret){
                                    Log.e(tag, "----!!!! 添加定时规则失败");
                                }
                                mHandler.sendEmptyMessage(MSG_REFRESH);
                            }
                        }).start();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
                    }
                });
                AlertDialog ad = buil.create();
                ad.setOnShowListener(new DialogInterface.OnShowListener() {//初始显示居中
                    @Override
                    public void onShow(DialogInterface dialog) {
                        if (null != hv){
//                            hv.scrollTo((rl.getWidth() - hv.getWidth())/2, 0);
                        }
                        Log.v(tag, "---gridview:w:"+ gv.getWidth() + gv.getHeight());
                    }
                });
                ad.show();
                break;

            case DIALOG_TRIGGER:
                TriggerDiaFrag triggerDiaFrag = new TriggerDiaFrag(true);
                triggerDiaFrag.show(getFragmentManager(), "tag_tridiafrag");
                break;
            default:
                break;
        }
    }
    
    private String buildTimeStr(Timestamp t){
        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return formater.format(t);
    }
    
    private String buildCountDownStr(int sec){
        String str = "";
        int days = sec/(24 * 3600);
        int hours = sec%(24 * 3600) / (3600);
        int mins = sec%3600 / 60;
        int secs = sec%60;
        str += days + "天";
        if (hours < 10){
            str += "0" + hours + ":";
        } else {
            str += hours+":";
        }
        if (mins < 10){
            str += "0" + mins +":";
        } else {
            str+= mins+":";
        }
        if (secs < 10){
            str += "0" + secs;
        } else {
            str += secs;
        }
        return str;
    }
    
    DeleteListener mDeleteListener = new DeleteListener() {//删除按钮
        @Override
        public void onDelete(int id) {
            final int idFinal = id;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    delstate(idFinal);
                }
            }).start();
        }
    };
    
    public void setClickListener(FContrDetailHoldBtnClickListener listen){
        mFContrDetailHoldBtnClickListener = listen;
    }
    
    //回调接口，留给activity使用
    public interface FContrDetailHoldBtnClickListener {  
        void onFContrDetailHoldBtnClick();  
    }
}