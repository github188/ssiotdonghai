package com.ssiot.donghai.hikvision;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.ssiot.donghai.R;

//句容大厅 专用
public class VideoActivity extends ActionBarActivity{
    private static final String tag = "VideoActivity-hikvision";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_video);
        Intent intent = getIntent();
        if (null != intent){
            if (savedInstanceState == null) {
                HCLiveFrag frag = new HCLiveFrag();
                Bundle bundle = intent.getBundleExtra("videobundle");
                frag.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.video_container, frag)
                        .commit();
            }
        } else {
            Toast.makeText(this, "VideoActivity出现问题", Toast.LENGTH_SHORT).show();
        }
        
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
}