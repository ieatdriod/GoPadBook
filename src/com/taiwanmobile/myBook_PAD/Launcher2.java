package com.taiwanmobile.myBook_PAD;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * 未使用
 * @author III
 *
 */
public class Launcher2 extends Activity {
    /** Called when the activity is first created. */
    @Override 
    public void onCreate(Bundle savedInstanceState) {
    	Log.d("In","Launcher2");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.iii_main_l);
        TextView tv = (TextView) findViewById(R.id.tv);
        if(getIntent().getData()!=null)
        	tv.setText("Intent:"+getIntent().getData().toString());
    }
    

}