/* Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tw.com.soyong;

import com.taiwanmobile.myBook_PAD.R;

import tw.com.soyong.mebook.MebookHelper;
import tw.com.soyong.utility.SettingPreference;
import tw.com.soyong.utility.Util;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * MeReader Viewer 設定頁
 * @author Victor
 *
 */
public class SettingActivity extends Activity {
	
	Spinner mS1;
	Spinner mS2;
	Spinner mS4;
	CheckBox mCheck;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        mS1 = (Spinner) findViewById(R.id.spin_repeat_count);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.count, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mS1.setAdapter(adapter);

        mS2 = (Spinner) findViewById(R.id.spin_gap);
        adapter = ArrayAdapter.createFromResource(this, R.array.gap,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mS2.setAdapter(adapter);
       
        mS4 = (Spinner) findViewById(R.id.spin_hide_percent);
        adapter = ArrayAdapter.createFromResource(this, R.array.hidePercent,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mS4.setAdapter(adapter);          
        
        SettingPreference pref = MeReaderActivity.mSettingPref;
        int repeatCount = pref.getRepeatCount();
		int gap = pref.getGap()/1000;
		int fontType = pref.getFontType();
		int hidePercent = pref.getHidePercent();
		boolean isAutoRepeat = pref.isAutoRepeat();
		
		
        if ( MebookHelper.mIsJpBook){
        	//s4.setVisibility(View.GONE);
        	
        	View view =  findViewById(R.id.hide_percent_cell);
        	view.setVisibility(View.GONE);
        	view =  findViewById(R.id.hide_percent_line);
        	view.setVisibility(View.GONE);
        	hidePercent = 0 ;
        }		

        mS1.setSelection(repeatCount);
        mS2.setSelection(gap);

        mS4.setSelection(hidePercent);
        CheckBox check = (CheckBox)findViewById(R.id.check1);
        check.setChecked(isAutoRepeat);
        mCheck = check;
        
        final Button btnReset = (Button) findViewById(R.id.resetBtn);
        btnReset.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				
		        mS1.setSelection(0);
		        mS2.setSelection(0);
		        mS4.setSelection(0);
		        mCheck.setChecked(true);
		        
		        Toast.makeText(SettingActivity.this.getApplicationContext(),R.string.iii_setting_default, Toast.LENGTH_SHORT).show();
			}
        });
        
        final ImageButton imgBtn_Back = (ImageButton) findViewById(R.id.setting_ImageButton_Back);
     
        imgBtn_Back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				exit();
			}
		});       
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (  KeyEvent.KEYCODE_BACK == keyCode){
			
			exit();			
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void exit() {
		int repeatCount = mS1.getSelectedItemPosition();
		int gap = mS2.getSelectedItemPosition();
		int hidePercent = mS4.getSelectedItemPosition();
		boolean isAutoRepeat = mCheck.isChecked();

		SettingPreference pref = MeReaderActivity.mSettingPref;
		pref.setRepeatCount(repeatCount);
		pref.setGap(gap*1000);
		pref.setAutoRepeat(isAutoRepeat);
		pref.setHidePercent(hidePercent);
		
		setResult(RESULT_OK);
		
		finish();
	}
    
}
