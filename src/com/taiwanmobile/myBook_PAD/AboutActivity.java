package com.taiwanmobile.myBook_PAD;


import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
/**
 * 關於本程式
 * @author III
 * 
 */
public class AboutActivity extends Activity {
	 public void onCreate(Bundle savedInstanceState){
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.about);
	        
	        
	        PackageInfo pInfo = null;
	        try {
	        	String pgkName = this.getClass().getPackage().getName();
				pInfo = getPackageManager().getPackageInfo(pgkName,PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			
			String ver = "1.0.0";
			if ( null != pInfo){
				ver = pInfo.versionName;
			}
	        
	        String version = getResources().getString(R.string.about_msg1) + " "+ver;
	        final TextView tv = (TextView)this.findViewById(R.id.about_version);
	        tv.setText(version);
	        
	        

	        final  ImageButton btn = (ImageButton)this.findViewById(R.id.setting_ImageButton_Back);
	        btn.setOnClickListener( new OnClickListener(){

				@Override
				public void onClick(View v) {
					finish();
				}
	        });
	 }
}
