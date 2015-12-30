package com.ssiot.donghai.monitor;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ssiot.donghai.R;
import com.ssiot.donghai.data.model.view.NodeData;
import com.ssiot.donghai.data.model.view.NodeView2Model;
import com.ssiot.donghai.data.model.view.NodeViewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MonitorListAdapter2 extends BaseAdapter{
    private static String tag = "MonitorListAdapter2";
    private List<NodeView2Model> mDataList;
    private LayoutInflater mInflater;
    private Context context;
    private int dataViewHeight = 0;
    private ShowAllListener mListener;
    private DetailListener mDetailListener;
    private Handler uiHandler = null;
//    public static final int[] pics = {R.drawable.pic4, R.drawable.pic2, R.drawable.pic3, R.drawable.pic1};
    
    public MonitorListAdapter2(Context c,List<NodeView2Model> ss,ShowAllListener mListener,DetailListener d,Handler uiHandler){
        Log.v(tag, "----------monitorlistsize:"+ss.size() + " context:" + (c!=null));
        mDataList = ss;
        mInflater = LayoutInflater.from(c);
        context = c;
        this.mListener = mListener;
        this.mDetailListener = d;
        this.uiHandler = uiHandler;
        dataViewHeight = (c.getResources().getDimensionPixelSize(R.dimen.node_img_width) -4) / 4;
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
        Log.v(tag, "-------getView---" + position + " showall:"+ mDataList.get(position).showAll);
        ViewHolder holder;
        if (null == convertView){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.monitor_node_view_2, null);
            holder.moni_title = (TextView) convertView.findViewById(R.id.moni_title);
            holder.moni_status = (ImageView) convertView.findViewById(R.id.moni_status);
            holder.moni_net_type = (ImageView) convertView.findViewById(R.id.moni_net_type);
            holder.moni_img = (ImageView) convertView.findViewById(R.id.moni_img);
            holder.moni_text_id = (TextView) convertView.findViewById(R.id.moni_text_id);
//            holder.moni_progress_list = (ListView) convertView.findViewById(R.id.moni_progress_list);
            holder.moni_data_more_button = (ImageButton) convertView.findViewById(R.id.moni_data_more_button);
            holder.moni_lasttime = (TextView) convertView.findViewById(R.id.moni_lasttime);
            holder.moni_detail_bar = (RelativeLayout) convertView.findViewById(R.id.moni_detail_bar);
            
            holder.more_bar = (RelativeLayout) convertView.findViewById(R.id.moni_more_relative);
            holder.info_bar = (RelativeLayout) convertView.findViewById(R.id.moni_info_bar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//        holder.info_bar = (RelativeLayout) convertView.findViewById(R.id.moni_info_bar);//由于添加的view不容易删除，listview的重用会导致问题
        final NodeView2Model nodeModel = mDataList.get(position);
        holder.moni_title.setText(nodeModel._location);
        Log.v(tag, "----------image:"+nodeModel._image);
        if (!TextUtils.isEmpty(nodeModel._image)){
            File f = new File(Environment.getExternalStorageDirectory() + "/SSIOT/" + nodeModel._image);
            if (f.exists()){
                holder.moni_img.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
            } else {
                holder.moni_img.setImageBitmap(null);
                new GetImageThread(holder.moni_img, nodeModel._image).start();
            }
        } else {
            holder.moni_img.setImageBitmap(null);
        }
        
        holder.moni_text_id.setText("ID:"+nodeModel._nodeno);
        SimpleDateFormat dateformatAll= new SimpleDateFormat("yyyy.MM.dd HH:mm");
        holder.moni_lasttime.setText("最后更新："+ dateformatAll.format(nodeModel._updatetime));
        holder.moni_status.setImageResource(nodeModel._isonline.equals("在线") ? R.drawable.online_3 : R.drawable.offline_2);
        holder.moni_net_type.setImageResource("GPRS".equalsIgnoreCase(nodeModel._onlinetype) ? R.drawable.connect_gprs : R.drawable.connect_zigbee);
        List<NodeData> nodeDatas = nodeModel._nodeData_list;
        
//        RelativeLayout info_bar = (RelativeLayout) convertView.findViewById(R.id.moni_info_bar);
//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        lp.addRule(RelativeLayout.RIGHT_OF, R.id.moni_img);
//        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        lp.height = 25;
//        RelativeLayout dataViewToAdd = (RelativeLayout) mInflater.inflate(R.layout.singel_data_view, null);
//        info_bar.addView(dataViewToAdd, lp);
        
        RelativeLayout data_view1 = (RelativeLayout) convertView.findViewById(R.id.data_view1);
        RelativeLayout data_view2 = (RelativeLayout) convertView.findViewById(R.id.data_view2);
        RelativeLayout data_view3 = (RelativeLayout) convertView.findViewById(R.id.data_view3);
        RelativeLayout data_view4 = (RelativeLayout) convertView.findViewById(R.id.data_view4);
        ArrayList<RelativeLayout> topDataViews = new ArrayList<RelativeLayout>();
        topDataViews.add(data_view1);
        topDataViews.add(data_view2);
        topDataViews.add(data_view3);
        topDataViews.add(data_view4);
        
        boolean showAll = true;//nodeModel.showAll;//写死全部显示 for jurong
        int dataIndex = 0;
        int showCountIndex = 0;//多数情况下 第一个是摄像头,需要剔除
        float GESTURE_THRESHOLD_DP = 21.0f;
        float scale = context.getResources().getDisplayMetrics().density;
        int heightPx = (int) (GESTURE_THRESHOLD_DP * scale + 0.5f);
        for(dataIndex = 0; dataIndex < nodeDatas.size(); dataIndex ++){
            NodeData tmpNodeData = nodeDatas.get(dataIndex);
            if (tmpNodeData._name.contains("是否有摄像头")){
                continue;
            }
            
            if (showCountIndex < 4){
                RelativeLayout tmpDataView = topDataViews.get(showCountIndex);
                setNodeDataToNodeDataView(tmpNodeData, tmpDataView);
                holder.more_bar.setVisibility(View.GONE);
                holder.more_bar.removeAllViews();
            } else {
                if (showAll){//
                    Log.v(tag, "--------showall-----dataViewHeight:" + dataViewHeight);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    lp.addRule(RelativeLayout.RIGHT_OF, R.id.moni_img);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                    lp.addRule(RelativeLayout.BELOW, belowWhatResId);
//                    lp.topMargin = showCountIndex * heightPx;
//                    lp.height = heightPx;
                    lp.topMargin = (showCountIndex-4) * dataViewHeight;
                    lp.height = dataViewHeight-1;//这个1 是devider的高度
                    RelativeLayout dataViewToAdd = (RelativeLayout) mInflater.inflate(R.layout.singel_data_view, null);
                    setNodeDataToNodeDataView(tmpNodeData, dataViewToAdd);
                    holder.more_bar.addView(dataViewToAdd, lp);
                    holder.more_bar.setVisibility(View.VISIBLE);
                } else {
                    holder.more_bar.setVisibility(View.GONE);
                    holder.more_bar.removeAllViews();
                }
            }
            showCountIndex ++;
        }
        
        //箭头的处理
        if (showAll){//箭头删除，重新添加向上的箭头  
            holder.info_bar.removeView(holder.moni_data_more_button);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.RIGHT_OF, R.id.moni_img);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp.addRule(RelativeLayout.BELOW, R.id.top_four_data);
            lp.topMargin = (showCountIndex-4) * dataViewHeight;
            lp.bottomMargin = 5;
            holder.moni_data_more_button.setImageResource(R.drawable.btn_more_nodedata_hide);
            holder.info_bar.addView(holder.moni_data_more_button, lp);
        } else {
            holder.info_bar.removeView(holder.moni_data_more_button);
            RelativeLayout.LayoutParams lpTmp = (RelativeLayout.LayoutParams) holder.moni_data_more_button.getLayoutParams();
            holder.moni_data_more_button.setImageResource(R.drawable.btn_more_nodedata);
            lpTmp.topMargin = 0;
            holder.info_bar.addView(holder.moni_data_more_button, lpTmp);
        }
        data_view1.setVisibility(showCountIndex < 1 ? View.INVISIBLE : View.VISIBLE);
        data_view2.setVisibility(showCountIndex < 2 ? View.INVISIBLE : View.VISIBLE);
        data_view3.setVisibility(showCountIndex < 3 ? View.INVISIBLE : View.VISIBLE);
        data_view4.setVisibility(showCountIndex < 4 ? View.INVISIBLE : View.VISIBLE);
        holder.moni_data_more_button.setVisibility(showCountIndex < 5 ? View.GONE : View.VISIBLE);
        holder.moni_data_more_button.setVisibility(View.GONE);
        
        final View view = data_view4;
        final int positionFinal = position;
        holder.moni_data_more_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(tag, "-------onClick---" + positionFinal);
                dataViewHeight = view.getHeight();
                if (null != mListener){
                    mListener.onShowAll(positionFinal);
                }
            }
        });
        
        holder.moni_detail_bar.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (null != mDetailListener){
                    mDetailListener.showDetail(nodeModel);
                }else {
                    Log.e(tag, "----DetailListener = null---!!!!!!!!!!!!!!!!!!");
                }
            }
        });
//        holder.moni_img.setImageResource(pics[position%4]);
        return convertView;
    }
    
    private void setNodeDataToNodeDataView(NodeData tmpNodeData, RelativeLayout tmpDataView){
        ((TextView) tmpDataView.findViewById(R.id.data_type)).setText(tmpNodeData._name);
        ((TextView) tmpDataView.findViewById(R.id.data_data)).setText("" + tmpNodeData._data + tmpNodeData._unit);
        ((ProgressBar) tmpDataView.findViewById(R.id.data_progress)).setProgress((int)(tmpNodeData._proportion * 100));
        int compareImg = R.drawable.compare_up;
        ImageView compareView = (ImageView) tmpDataView.findViewById(R.id.data_compare);
        if ("上升".equals(tmpNodeData._compare)){
            compareImg = R.drawable.compare_up;
            compareView.setVisibility(View.VISIBLE);
        } else if ("下降".equals(tmpNodeData._compare)){
            compareImg = R.drawable.compare_down;
            compareView.setVisibility(View.VISIBLE);
        } else {//无  相等 null
            compareView.setVisibility(View.INVISIBLE);
        }
        compareView.setImageResource(compareImg);
        compareView.setVisibility(View.GONE);
    }
    
    private class ViewHolder{
        TextView moni_title;
        ImageView moni_status;
        ImageView moni_net_type;
        ImageView moni_img;
        TextView moni_text_id;
        ListView moni_progress_list;
        ImageButton moni_data_more_button;
        TextView moni_lasttime;
        RelativeLayout moni_detail_bar;
        
        RelativeLayout more_bar;
        RelativeLayout info_bar;
    }
    
    public interface ShowAllListener{
        public void onShowAll(int index);
    }
    
    public interface DetailListener{
        public void showDetail(NodeView2Model n2m);
    }
    
    private class GetImageThread extends Thread{
        String url = "";
        ImageView imageView;
        public GetImageThread(ImageView view, String urlString){
            url = urlString;
            imageView = view;
        }
        @Override
        public void run() {
            Bitmap bitmap = getHttpBitmap(url);
            Message message = uiHandler.obtainMessage(MoniNodeListFrag.MSG_GET_ONEIMAGE_END);
            message.obj = new ThumnailHolder(imageView, bitmap);
            uiHandler.sendMessage(message);
        }
    }
    
    public static Bitmap getHttpBitmap(String url){
        URL myFileURL;
        Bitmap bitmap=null;
        try{
            myFileURL = new URL("http://yun.ssiot.com/"+url);
            //获得连接
            HttpURLConnection conn=(HttpURLConnection)myFileURL.openConnection();
            //设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
            conn.setConnectTimeout(4000);
            //连接设置获得数据流
            conn.setDoInput(true);
            //不使用缓存
            conn.setUseCaches(false);
            //这句可有可无，没有影响
            //conn.connect();
            //得到数据流
            InputStream is = conn.getInputStream();
            //解析得到图片
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.outWidth = 64;
//            options.outHeight = 64;
            bitmap = BitmapFactory.decodeStream(is);
//            bitmap = BitmapFactory.decodeStream(is, null, options);
            
            bitmap = resizeBitmap(bitmap, 128, 128);
            //关闭数据流
            is.close();
            saveBitmap(bitmap, Environment.getExternalStorageDirectory() + "/SSIOT/" + url);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return bitmap;
         
    }
    
    public static Bitmap resizeBitmap(Bitmap drawable, int desireWidth,
            int desireHeight) {
        int width = drawable.getWidth();
        int height = drawable.getHeight();

        if (0 < width && 0 < height && desireWidth < width
                || desireHeight < height) {
            // Calculate scale
            float scale;
            if (width < height) {
                scale = (float) desireHeight / (float) height;
                if (desireWidth < width * scale) {
                    scale = (float) desireWidth / (float) width;
                }
            } else {
                scale = (float) desireWidth / (float) width;
            }

            // Draw resized image
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap bitmap = Bitmap.createBitmap(drawable, 0, 0, width, height,
                    matrix, true);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);

            drawable = bitmap;
        }
        return drawable;
    }
    
    public static void saveBitmap(Bitmap bm, String path) {
        File f = new File(path);
        f.getParentFile().mkdirs();
        if (f.exists()) {
            boolean b = f.delete();
            Log.v(tag, "------delete result :" + b);
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    public class ThumnailHolder{
        public ImageView imageView;
        public Bitmap bitmap;
        public ThumnailHolder(ImageView image,Bitmap b) {
            this.bitmap = b;
            this.imageView = image;
        }
    }
    
}