package com.ssiot.donghai.control;

import android.R.integer;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssiot.donghai.R;
import com.ssiot.donghai.data.model.view.ControlActionViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RuleAdapter extends BaseAdapter{
    private static String tag = "RuleAdapter";
    private List<ControlActionViewModel> mDataList;
    private LayoutInflater mInflater;
    private Context context;
    private Handler uiHandler;
    private DeleteListener mDListener;
    
    public RuleAdapter(Context c,List<ControlActionViewModel> ss,Handler uiHandler,DeleteListener d){
        mDataList = ss;
        mInflater = LayoutInflater.from(c);
        context = c;
//        this.mListener = mListener;
//        this.mDetailListener = d;
        this.uiHandler = uiHandler;
        mDListener = d;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
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
            convertView = mInflater.inflate(R.layout.ctr_rule_item, null);
            holder.ctr_rule_type = (TextView) convertView.findViewById(R.id.ctr_rule_type);
            holder.ctr_rule_detail_linear = (LinearLayout) convertView.findViewById(R.id.ctr_rule_detail_linear);
            holder.ctr_rule_detail_1 = (TextView) convertView.findViewById(R.id.ctr_rule_detail_1);
            holder.ctr_rule_detail_2 = (TextView) convertView.findViewById(R.id.ctr_rule_detail_2);
            holder.ctr_rule_detail_s = (TextView) convertView.findViewById(R.id.ctr_rule_detail_s);
            holder.ctr_rule_stts = (TextView) convertView.findViewById(R.id.ctr_rule_stts);
            holder.ctr_rule_modify = (TextView) convertView.findViewById(R.id.ctr_rule_modify);
            holder.ctr_rule_delete = (TextView) convertView.findViewById(R.id.ctr_rule_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        final ControlActionViewModel m = mDataList.get(position);
        if (3 == m.ControlType){//定时
            holder.ctr_rule_type.setText("定时");
            holder.ctr_rule_type.setBackgroundColor(Color.parseColor("#33588f"));
            holder.ctr_rule_detail_linear.setVisibility(View.VISIBLE);
            holder.ctr_rule_detail_s.setVisibility(View.GONE);
            ArrayList<String> ss = parseTimingCondition(m.ControlCondition);
            holder.ctr_rule_detail_1.setText("开始:"+ ss.get(1));
            holder.ctr_rule_detail_2.setText("结束:"+ ss.get(1));
        } else if (5 == m.ControlType){//循环
            holder.ctr_rule_type.setText("循环");
            holder.ctr_rule_type.setBackgroundColor(Color.parseColor("#88337f"));
            holder.ctr_rule_detail_linear.setVisibility(View.VISIBLE);
            holder.ctr_rule_detail_s.setVisibility(View.GONE);
            ArrayList<String> ss = parseCircleCondition(m.ControlCondition);
            holder.ctr_rule_detail_1.setText("开始：" +ss.get(1) + " 运行："+ss.get(2));
            holder.ctr_rule_detail_2.setText("结束：" + ss.get(4) + " 间隔" + ss.get(3));
        } else if (1 == m.ControlType) {//立即
            holder.ctr_rule_type.setText("立即开启");
            holder.ctr_rule_type.setBackgroundColor(Color.parseColor("#33588f"));
            holder.ctr_rule_detail_linear.setVisibility(View.VISIBLE);
            Message msg  = uiHandler.obtainMessage(ControlDetailHolderFrag.MSG_TIME_COUNT_DOWN);
            holder.ctr_rule_detail_1.setText("运行时间"+ m.ControlCondition + "分钟");
            msg.obj = new TimeCountDownHolder(holder.ctr_rule_detail_2, m.OperateTime,m.ControlCondition);
            uiHandler.sendMessage(msg);
            holder.ctr_rule_detail_s.setVisibility(View.GONE);
        } else if (2== m.ControlType){//立即关闭
            holder.ctr_rule_type.setText("立即关闭");
        } else if (6 == m.ControlType){
            holder.ctr_rule_type.setText("触发");
            holder.ctr_rule_type.setBackgroundColor(Color.parseColor("#cce198"));
            holder.ctr_rule_detail_linear.setVisibility(View.VISIBLE);
            holder.ctr_rule_detail_s.setVisibility(View.GONE);
            ArrayList<String> ss = parseTrigerCondition(m.ControlCondition);
            holder.ctr_rule_detail_2.setText(ss.get(3));
        } else {
            holder.ctr_rule_type.setText("类型错误"+m.ControlType);
        }
        
        holder.ctr_rule_stts.setText(""+m.StateNow);
        holder.ctr_rule_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mDListener) {
                    mDListener.onDelete(m.ControlActionID);
                }
            }
        });
        return convertView;
    }
    
    private ArrayList<String> parseTimingCondition(String condition){
        ArrayList<String> ret = new ArrayList<String>();
        try {
            JSONObject js = new JSONObject(condition);
            ret.add(js.getString("ID"));
            ret.add(js.getString("StartTime"));
            ret.add(js.getString("EndTime"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        while(ret.size() < 3){
            ret.add("");
        }
        return ret;
    }
    
    //{"MinInterval":"30","Relation":"同时满足条件","RunTime":1,
    //"TriggerData":[
    //  {"ID":1,"Element":"1002-1","Type":"小于","Param":"4"},
    //  {"ID":2,"Element":"1002-2","Type":"小于","Param":"4"},
    //  {"ID":3,"Element":"1002-3","Type":"小于","Param":"4"},
    //  {"ID":4,"Element":"1002-4","Type":"小于","Param":"4"}]}
    private ArrayList<String> parseTrigerCondition(String condition){
        ArrayList<String> ret = new ArrayList<String>();
        try {
            JSONObject js = new JSONObject(condition);
            ret.add(js.getString("MinInterval"));
            ret.add(js.getString("Relation"));
            ret.add(js.getString("RunTime"));
            JSONArray ja = js.getJSONArray("TriggerData");
            String trStr = "";
            for (int j = 0; j < ja.length(); j++){
                JSONObject jo = ja.optJSONObject(j);
                trStr += jo.getString("Element");
                trStr += jo.getString("Type");
                trStr += jo.getString("Param");
            }
            ret.add(trStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        while(ret.size() < 4){
            ret.add("");
        }
        return ret;
    }
    
    private ArrayList<String> parseCircleCondition(String condition){
        ArrayList<String> ret = new ArrayList<String>();
        try {
            JSONArray ja = new JSONArray(condition);
            for(int i = 0; i < ja.length(); i ++){
                JSONObject jobj = ja.optJSONObject(i);
                ret.add(jobj.getString("ID"));
                ret.add(jobj.getString("StartTime"));
                ret.add(jobj.getString("OnceRunTime"));
                ret.add(jobj.getString("IntervalTime"));
                ret.add(jobj.getString("EndTime"));
                return ret;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        while(ret.size() < 5){
            ret.add("");
        }
        return ret;
    }
    
    private class ViewHolder{
        public TextView ctr_rule_type;
        public LinearLayout ctr_rule_detail_linear;
        public TextView ctr_rule_detail_1;
        public TextView ctr_rule_detail_2;
        public TextView ctr_rule_detail_s;
        public TextView ctr_rule_stts;
        public TextView ctr_rule_modify;
        public TextView ctr_rule_delete;
    }
    
    public class TimeCountDownHolder{
        TextView textView;
        Timestamp mEndDate;
        
        public TimeCountDownHolder(TextView textView,Timestamp mOperateTime,String condition){
            this.textView = textView;
            int i = 0;
            try {
                i = Integer.parseInt(condition);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mEndDate = new Timestamp(mOperateTime.getTime() + i * 60 * 1000);
        }
    }
    
    public interface DeleteListener{
        public void onDelete(int id);
    }
    
}