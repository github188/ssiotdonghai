package com.ssiot.donghai.control;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ssiot.donghai.GetImageThread;
import com.ssiot.donghai.R;
import com.ssiot.donghai.SsiotConfig;
import com.ssiot.donghai.data.model.view.ControlNodeViewModel;

import java.io.File;
import java.util.List;

public class ControlListAdapter extends BaseAdapter{
    private static String tag = "ControlListAdapter";
    private List<ControlNodeViewModel> mDataList;
    private LayoutInflater mInflater;
    private Context context;
    private ControlDetailListener mDetailListener;
    private Handler uiHandler = null;
    
    public ControlListAdapter(Context c,List<ControlNodeViewModel> ss,ControlDetailListener d,Handler uiHandler){
        Log.v(tag, "----------controllistsize:"+ss.size() + " context:" + (c!=null));
        mDataList = ss;
        mInflater = LayoutInflater.from(c);
        context = c;
        this.mDetailListener = d;
        this.uiHandler = uiHandler;
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
            convertView = mInflater.inflate(R.layout.control_node_view, null);
            holder.control_title = (TextView) convertView.findViewById(R.id.control_title);
            holder.control_status = (ImageView) convertView.findViewById(R.id.control_status);
            holder.control_img = (ImageView) convertView.findViewById(R.id.control_img);
            holder.control_text_id = (TextView) convertView.findViewById(R.id.control_text_id);
            holder.control_sensorcount = (TextView) convertView.findViewById(R.id.control_sensorcount);
            holder.control_detail_bar = (RelativeLayout) convertView.findViewById(R.id.control_detail_bar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ControlNodeViewModel m = mDataList.get(position);
        
        if (!TextUtils.isEmpty(m._image)){
            File f = new File(Environment.getExternalStorageDirectory() + "/"+SsiotConfig.CACHE_DIR +"/" + m._image);
            if (f.exists()){
                holder.control_img.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
            } else {
                holder.control_img.setImageBitmap(null);
                new GetImageThread(holder.control_img, m._image,uiHandler).start();
            }
        } else {
            holder.control_img.setImageBitmap(null);
        }
        
        holder.control_title.setText(m._nodename);
//        holder.control_status
        holder.control_text_id.setText("ID:" + m._uniqueid);
        holder.control_sensorcount.setText("设备数量：" + m._devicecount);
        
        final int positionFinal = position;
        holder.control_detail_bar.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (null!= mDetailListener){
                    mDetailListener.showDetail(positionFinal,mDataList.get(positionFinal));
                }
            }
        });
        
        return convertView;
    }
    
    private class ViewHolder{
        TextView control_title;
        ImageView control_status;
        ImageView control_img;
        TextView control_text_id;
        TextView control_sensorcount;
        RelativeLayout control_detail_bar;
    }
    
    public interface ControlDetailListener{
        public void showDetail(int position,ControlNodeViewModel model);
    }
}