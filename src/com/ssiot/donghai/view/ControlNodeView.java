package com.ssiot.donghai.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import com.ssiot.donghai.R;

public class ControlNodeView extends RelativeLayout{

    public ControlNodeView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        View view = LayoutInflater.from(context).inflate(R.layout.control_node_view, null);
        addView(view);
    }

    public ControlNodeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public ControlNodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }
    
}