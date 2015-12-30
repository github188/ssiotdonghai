
package com.ssiot.donghai.monitor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.ssiot.donghai.R;

public class MonitorNodeView extends RelativeLayout {

    public MonitorNodeView(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.monitor_node_view, null);
        addView(view);
    }

    public MonitorNodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public MonitorNodeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }
    
    
}
