package com.ssiot.donghai;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridAdapter extends SimpleAdapter{
    private static final String tag = "GridAdapter";
    public Context mContext;
    ArrayList<HashMap<String, Object>> mData;

    public GridAdapter(Context context, List<? extends Map<String, ?>> data, int resource,
            String[] from, int[] to) {
        super(context, data, resource, from, to);
        mContext = context;
        mData = (ArrayList<HashMap<String, Object>>) data;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder = null;
        if (null == convertView) {
            holder = new Holder();
            LayoutInflater mInflater = LayoutInflater.from(mContext);
            convertView = mInflater.inflate(R.layout.item_icon, parent, false);
            /*根据parent动态设置convertview的大小*/
            convertView.setLayoutParams(new AbsListView.LayoutParams((int) (parent.getWidth() / 2) - 1, (int) (parent.getHeight() / 3)));// 动态设置item的高度
            holder.imageView = (ImageView) convertView.findViewById(R.id.item_image);
            holder.imageView.setImageResource((Integer) mData.get(position).get("ItemImage"));
            holder.textView = (TextView) convertView.findViewById(R.id.item_text);
            holder.textView.setText((String) mData.get(position).get("ItemText"));
            Log.v(tag, "-------------"+position +"    "+(Integer) mData.get(position).get("ItemBack"));
            holder.imageView.setBackgroundColor((Integer) mData.get(position).get("ItemBack"));
            final int index = position;
//            holder.imageView.setClickable(true);
//            holder.imageView.setOnTouchListener(new View.OnTouchListener() {//must add clickable=true
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    Log.v(tag, "---onTouch---"+ event.getAction() +" "+ v);
//                    if (MotionEvent.ACTION_DOWN ==event.getAction()){
//                        v.setBackgroundColor(mContext.getResources().getColor(R.color.grey));
//                    } else if (MotionEvent.ACTION_UP == event.getAction()){
//                        v.setBackgroundColor((Integer) mData.get(index).get("ItemBack"));
//                    }
//                    return false;
//                }
//            });
//            convertView.setBackgroundResource((Integer) mData.get(position).get("ItemBack"));
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
            /*解决动态设置convertview大小，第一项不显示的BUG*/
            convertView.setLayoutParams(new AbsListView.LayoutParams((int) (parent.getWidth() / 2) - 1, (int) (parent.getHeight() / 3)));// 动态设置item的高度
        }
        // _Holder.btn_gv_item.setText(mLists.get(position));

        return convertView;
    }
    
    @Override
    public HashMap<String, Object> getItem(int position) {
        return mData.get(position);
    }
    
    public class Holder{
        ImageView imageView;
        TextView textView;
    }
    
}