package com.ssiot.donghai.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ssiot.donghai.R;

public class SingleDataView extends RelativeLayout{

//    public SingleDataView(Context context) {
//        super(context);
//        // TODO Auto-generated constructor stub
//        View view = LayoutInflater.from(context).inflate(R.layout.singel_data_view, null);
//        addView(view);
//    }

//    public SingleDataView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        // TODO Auto-generated constructor stub
//    }
//
//    public SingleDataView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        // TODO Auto-generated constructor stub
//    }
    
    public SingleDataView(Context context,int i){
        super(context);
        // TODO Auto-generated constructor stub
        View view = LayoutInflater.from(context).inflate(R.layout.singel_data_view, null);
        TextView sensorType = (TextView) view.findViewById(R.id.data_type);
        ProgressBar pb = (ProgressBar) view.findViewById(R.id.data_progress);
        TextView dataText = (TextView) view.findViewById(R.id.data_data);
        dataText.setText(""+i);
        
        addView(view);
    }
    
}