package com.ssiot.donghai.setting;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class SettingListAdapter extends BaseAdapter{
    private static String tag = "SettingListAdapter";
    private List<String> mDataList;
    private LayoutInflater mInflater;
    private Context mContext;
    
    public SettingListAdapter(Context c,List<String> ss){
        mDataList = ss;
        mInflater = LayoutInflater.from(c);
        mContext = c;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//        if (null == convertView){
//            holder = new ViewHolder();
//            convertView = mInflater.inflate(R.layout.videolist_item, null);
//            holder.imageView = (ImageView) convertView.findViewById(R.id.videolist_icon);
//            holder.textView = (TextView) convertView.findViewById(R.id.videoitem_text);
//            holder.video_type = (TextView) convertView.findViewById(R.id.video_type);
//            holder.typeText = (TextView) convertView.findViewById(R.id.videoitem_type);
////            holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);
//            holder.status = (ImageView) convertView.findViewById(R.id.status);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//        String str = mDataList.get(position);
//        holder.textView.setText(str);
//        
        TextView t = new TextView(mContext);
        t.setText(mDataList.get(position));
        return t;
    }
    
    private class ViewHolder{
        ImageView imageView;
        TextView textView;
        TextView video_type;
        TextView typeText;
//        ImageView arrow;
        ImageView status;
    }
    
}