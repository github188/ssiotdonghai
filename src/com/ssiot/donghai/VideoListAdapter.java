package com.ssiot.donghai;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ssiot.donghai.data.model.VLCVideoInfoModel;

import java.util.List;

public class VideoListAdapter extends BaseAdapter{
    private static String tag = "VideoListAdapter";
    private List<VLCVideoInfoModel> mDataList;
    private LayoutInflater mInflater;
    
    public VideoListAdapter(Context c,List<VLCVideoInfoModel> ss){
        Log.v(tag, "----------videolistsize:"+ss.size());
        mDataList = ss;
        mInflater = LayoutInflater.from(c);
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
        ViewHolder holder;
        if (null == convertView){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.videolist_item, null);
            holder.imageView = (ImageView) convertView.findViewById(R.id.videolist_icon);
            holder.textView = (TextView) convertView.findViewById(R.id.videoitem_text);
            holder.video_type = (TextView) convertView.findViewById(R.id.video_type);
            holder.typeText = (TextView) convertView.findViewById(R.id.videoitem_type);
//            holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);
            holder.status = (ImageView) convertView.findViewById(R.id.status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        VLCVideoInfoModel vModel = mDataList.get(position);
        holder.imageView.setImageResource(vModel._tcpport == 0 ? R.drawable.video_rtsp_6 : R.drawable.video_dahua_6);
        holder.textView.setText(vModel._address.trim());
        holder.video_type.setText("设备类型：" +vModel._type);
        holder.typeText.setText(vModel._type);
        if(vModel.status == 1){
            holder.status.setImageResource(R.drawable.connect_ok_green);
        } else if (vModel.status == 2){
            holder.status.setImageResource(R.drawable.connect_fail_2);
        } else {
            holder.status.setImageBitmap(null);
        }
        return convertView;
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