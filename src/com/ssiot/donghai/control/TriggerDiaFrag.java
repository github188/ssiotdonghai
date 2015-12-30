package com.ssiot.donghai.control;

import android.R.array;
import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ssiot.donghai.MainActivity;
import com.ssiot.donghai.R;
import com.ssiot.donghai.data.AjaxGetNodesDataByUserkey;
import com.ssiot.donghai.data.model.view.NodeView2Model;
import com.ssiot.donghai.data.model.view.SensorViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//触发规则的对话框
public class TriggerDiaFrag extends DialogFragment{
    private static final String tag = "TriggerDiaFrag";
    private String userkey = MainActivity.mUniqueID;
    private boolean isTriMODE = true;
    
    private ListView mNodeListView;
    NodeListCheckAdapter mNodeAdapter;
    private Button mNextBtn;
//    private List<String> mStrN2mList = new ArrayList<String>();
    private List<NodeView2Model> n2mList = new ArrayList<NodeView2Model>();
    
    List<SensorViewModel> mSVModels = new ArrayList<SensorViewModel>();
    
    private Spinner mIntervalSpinner;
    private Spinner mWorkTimeSpinner;
    private Spinner mRelationTypeSpinner;
    private LinearLayout mTriRuleTitle;
    private ListView mElementList;
    private Button mFinishBtn;
    
    Spinner mSensorSpinner;
    
    private TriRuleAdapter mElementAdapter;
    private ArrayList<TriRuleElementBean> mElementDatas = new ArrayList<TriRuleElementBean>();
    
    private String selectednodesStr = "";
    private int mSelectedIntervalTime = 5;//minutes
    private int mSelectedWorkTime = 5;
    private int mSelectedRelationType = 0;//同时满足条件，满足其中之一
    
    private final String[] typeDatas = {"同时满足条件","满足其中之一"};
    
    ArrayList<String> mSensorDatas = null;//null 表示还没开始获取进程结果
    private final String[] maxMinDatas = {"大于","小于","范围内"};
    
    private static final int MSG_GETNODES_END = 0;
    private static final int MSG_GETCOMSENSORS_END = 1;
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_GETNODES_END:
//                    ArrayAdapter<String> as = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1, mStrN2mList);
                    mNodeAdapter.notifyDataSetChanged();
//                    mNodeListView.setAdapter( new NodeListCheckAdapter(getActivity(), n2mList));
                    
                    break;
                case MSG_GETCOMSENSORS_END:
                    if (mSensorDatas != null && null != mSensorSpinner){
                        ArrayAdapter<String> arr_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,
                                mSensorDatas);
                        mSensorSpinner.setAdapter(arr_adapter);
                    }
                    break;

                default:
                    break;
            }
        };
    };
    
    public TriggerDiaFrag(boolean mode){
        isTriMODE = mode;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (null != mSVModels){
            mSVModels.clear();
            mSVModels = null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dia_ctr_trigger, null);
        builder.setView(view)
                .setTitle(R.string.trigger)
//                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        Log.v(tag, "---onClickPositive---" + userkey + " "+selectedTime + " " +controlnodeuniqueid + " " + deviceNos);
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                boolean ret = new AjaxGetNodesDataByUserkey().SaveControlAdd(userkey, ""+selectedTime, controlnodeuniqueid, 1, deviceNos, ID);
//                                mHandler.sendEmptyMessage(MSG_REFRESH);
//                            }
//                        }).start();
//                    }
//                })
                .setNegativeButton(R.string.cancel, null);
//        initSpinner(view);
        initViews(view);
        new Thread(new Runnable() {//获取所有节点
            @Override
            public void run() {
                List<NodeView2Model> tmpList = new AjaxGetNodesDataByUserkey().GetAllNodesDataByUserkey(userkey);
                n2mList.clear();
                n2mList.addAll(tmpList);
                mHandler.sendEmptyMessage(MSG_GETNODES_END);
            }
        }).start();
        
        return builder.create();
    }
    
    private void initViews(View rootView){
        initFirstPage(rootView);
        initSecondPage(rootView);
    }
    
    private void initFirstPage(View rootView){
        mNodeListView = (ListView) rootView.findViewById(R.id.tri_node_list);
        mNodeAdapter = new NodeListCheckAdapter(getActivity(), n2mList);
        mNodeListView.setAdapter(mNodeAdapter);
        mNextBtn = (Button) rootView.findViewById(R.id.tri_next);
        final RelativeLayout part1 = (RelativeLayout) rootView.findViewById(R.id.part1);
        final RelativeLayout part2 = (RelativeLayout) rootView.findViewById(R.id.part2);
        
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                String selectednodes = "";
                ArrayList<NodeView2Model> checkedList = mNodeAdapter.getCheckedList();
                if (null == checkedList || checkedList.size() <= 0){
                    Toast.makeText(getActivity(), "未选择节点", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < checkedList.size(); i ++){
                    selectednodes += checkedList.get(i)._uniqueid + ",";
                }
                selectednodesStr = selectednodes;
                
                if (null != mSensorDatas){
                    mSensorDatas.clear();//每次都需要清除一下
                    mSensorDatas = null;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mSVModels = new AjaxGetNodesDataByUserkey().GetSensorInfo(selectednodesStr);
                        if (mSensorDatas == null){//null 代表获取进程没结束
                            mSensorDatas = new ArrayList<String>();
                        }
                        if (null != mSVModels){
                            for (int i = 0; i < mSVModels.size(); i ++){
                                mSensorDatas.add(mSVModels.get(i)._shortname + mSVModels.get(i)._channel);
                            }
                        }
                        mHandler.sendEmptyMessage(MSG_GETCOMSENSORS_END);
                    }
                }).start();
                part1.setVisibility(View.GONE);
                part2.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void initSecondPage(View rootView){
        mIntervalSpinner = (Spinner) rootView.findViewById(R.id.tri_interval_time);
        mWorkTimeSpinner = (Spinner) rootView.findViewById(R.id.tri_working_time);
        mRelationTypeSpinner = (Spinner) rootView.findViewById(R.id.tri_relation_type);
        mTriRuleTitle = (LinearLayout) rootView.findViewById(R.id.tri_rule_title);
        mElementList = (ListView) rootView.findViewById(R.id.tri_element_list);
        mFinishBtn = (Button) rootView.findViewById(R.id.tri_finish);
        
        ArrayAdapter<String> arr_adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,ControlDetailHolderFrag.spinnerDatas);
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mIntervalSpinner.setAdapter(arr_adapter);
        mIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedIntervalTime = (1 + position) * 5;//minutes
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //运行时间必须小于触发间隔 
        mWorkTimeSpinner.setAdapter(arr_adapter);
        mWorkTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedWorkTime = (1 + position) * 5;//minutes
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,typeDatas);
        mRelationTypeSpinner.setAdapter(typeAdapter);
        mRelationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedRelationType = position;//minutes
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        int titlecolor = getResources().getColor(R.color.ssiotgreen);
        mTriRuleTitle.findViewById(R.id.ele_name).setBackgroundColor(titlecolor);
        mTriRuleTitle.findViewById(R.id.ele_type).setBackgroundColor(titlecolor);
        mTriRuleTitle.findViewById(R.id.ele_num).setBackgroundColor(titlecolor);
        final ImageButton addBtn = (ImageButton) mTriRuleTitle.findViewById(R.id.tri_btn);
        addBtn.setBackgroundColor(titlecolor);
        addBtn.setImageResource(R.drawable.tri_rule_add);
        final View anchorView = rootView;
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPopup(anchorView);
            }
        });
        
        mElementAdapter = new TriRuleAdapter(getActivity(),mElementDatas);
        mElementList.setAdapter(mElementAdapter);
        
        mFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedWorkTime >= mSelectedIntervalTime){
                    Toast.makeText(getActivity(), "运行时间必须小于触发间隔", Toast.LENGTH_SHORT).show();
                    return;
                }
                String condition = buildTriJSON(mSelectedIntervalTime,mSelectedWorkTime,typeDatas[mSelectedRelationType], mElementDatas);
                Log.v(tag, ""+ condition);
//                boolean ret = new AjaxGetNodesDataByUserkey().SaveControlTriggerUser(UniqueID, deviceNo, selectednodesStr, condition, updateid, userkey);
//                if (!ret){
//                    Toast.makeText(getActivity(), "失败", Toast.LENGTH_SHORT).show();
//                }//TODO 。。。还要添加device选择
            }
        });
    }
    
    private String buildTriJSON(int intervalMinutes, int runMinutes, String relationStr, ArrayList<TriRuleElementBean> elements){
        JSONObject jsRet = new JSONObject();
        
        JSONArray triDataArr = new JSONArray();
        for (int i = 0; i < elements.size(); i ++){
            JSONObject jo = new JSONObject();
            try {
                jo.put("ID", ""+(i +1));
                jo.put("Element", elements.get(i).sensorValue);
                jo.put("Type", elements.get(i).type);
                jo.put("Param", elements.get(i).valueNumber);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            triDataArr.put(jo);
        }
        try {
            jsRet.put("MinInterval", ""+intervalMinutes);
            jsRet.put("Relation", relationStr);
            jsRet.put("RunTime", ""+runMinutes);
            jsRet.put("TriggerData", triDataArr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsRet.toString();
    }
    
    private void showAddPopup(View anchor){
        View popView = LayoutInflater.from(getActivity()).inflate(R.layout.tri_add_popup, null);
        
        mSensorSpinner = (Spinner) popView.findViewById(R.id.tri_pop_sensor_spinner);
        if (null != mSensorDatas){
            ArrayAdapter<String> sensorAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,mSensorDatas);
            mSensorSpinner.setAdapter(sensorAdapter);
        } else {
            String[] pleaseWait = {"正在查找传感器"};
            ArrayAdapter<String> sensorAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,pleaseWait);
            mSensorSpinner.setAdapter(sensorAdapter);
        }
        
        final Spinner mMaxMinSpinner = (Spinner) popView.findViewById(R.id.tri_pop_maxmin_spinner);
        ArrayAdapter<String> maxminAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,maxMinDatas);
        mMaxMinSpinner.setAdapter(maxminAdapter);
        
        final EditText numEdit = (EditText) popView.findViewById(R.id.tri_maxmin_value_edit);
        
        
        
        final PopupWindow popupWindow = new PopupWindow(popView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        
        Button tri_pop_add = (Button) popView.findViewById(R.id.tri_pop_add);
        tri_pop_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SensorViewModel sModel = mSVModels.get(mSensorSpinner.getSelectedItemPosition());
                String type = maxMinDatas[mMaxMinSpinner.getSelectedItemPosition()];
                String value = numEdit.getText().toString();
                if (TextUtils.isEmpty(value)){
                    Toast.makeText(getActivity(), "数值不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                mElementDatas.add(new TriRuleElementBean(sModel._shortname + sModel._channel, sModel._sensorno + "-" + sModel._channel, type, value));
                mElementAdapter.notifyDataSetChanged();
                if (popupWindow.isShowing()){
                    popupWindow.dismiss();
                }
            }
        });
        
        popView.findViewById(R.id.tri_pop_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupWindow.isShowing()){
                    popupWindow.dismiss();
                }
            }
        });
        
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.ssiot_green));
//        popupWindow.showAsDropDown(anchor);
        popupWindow.showAtLocation(anchor, Gravity.CENTER, 0, 0);
    }
    
    public class NodeListCheckAdapter extends BaseAdapter{
        List<NodeView2Model> mDatas;
        private ArrayList<NodeView2Model> checkedList = new ArrayList<NodeView2Model>();
        private LayoutInflater mInflater;

        public NodeListCheckAdapter(Context c, List<NodeView2Model> n2ms){
            mDatas = n2ms;
            mInflater = LayoutInflater.from(c);
        }
        
        public ArrayList<NodeView2Model> getCheckedList(){
            return checkedList;
        }
        
        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView){
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.tri_check_item, null);
                holder.mTextView = (TextView) convertView.findViewById(R.id.tri_node_name);
                holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.tri_node_check);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final NodeView2Model m = mDatas.get(position);
            holder.mTextView.setText(m._nodeno + m._location);
            holder.mCheckBox.setChecked(checkedList.contains(m));
            final int positionFinal = position;
            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        if (!checkedList.contains(m)){
                            checkedList.add(m);
                        }
                    } else {
                        if (checkedList.contains(m)){
                            checkedList.remove(m);
                        }
                    }
                }
            });
            return convertView;
        }
        
        private class ViewHolder{
            TextView mTextView;
            CheckBox mCheckBox;
        }
    }
    
    public class TriRuleAdapter extends BaseAdapter{
        private ArrayList<TriRuleElementBean> mDatas;
        private LayoutInflater mInflater;
        
        public TriRuleAdapter(Context c,ArrayList<TriRuleElementBean> d){
            mDatas = d;
            mInflater = LayoutInflater.from(c);
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHold hold;
            if (null == convertView){
                hold = new ViewHold();
                convertView = mInflater.inflate(R.layout.tri_addrule_item, null);
                hold.sensorNameView = (TextView) convertView.findViewById(R.id.ele_name);
                hold.typeView = (TextView) convertView.findViewById(R.id.ele_type);
                hold.numView = (TextView) convertView.findViewById(R.id.ele_num);
                hold.mBtn = (ImageButton) convertView.findViewById(R.id.tri_btn);
                convertView.setTag(hold);
            } else {
                hold = (ViewHold) convertView.getTag();
            }
            final TriRuleElementBean bean = mDatas.get(position);
            hold.sensorNameView.setText(bean.sensorName);
            hold.typeView.setText(bean.type);
            hold.numView.setText(bean.valueNumber);
            hold.mBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDatas.remove(bean);
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
        
        private class ViewHold {
            public TextView sensorNameView;
            public TextView typeView;
            public TextView numView;
            private ImageButton mBtn;
        }
    }
    
    public class TriRuleElementBean{// extends NodeView2Model{
        private String sensorName = "";
        private String sensorValue = "";
        private String type = "";//大于 小于 之间
        private String valueNumber = "";
        
        
        public TriRuleElementBean(String sensorName, String sensorValue,String type, String valueNumber){
            this.sensorName = sensorName;
            this.sensorValue = sensorValue;
            this.type = type;
            this.valueNumber = valueNumber;
        }
    }
}