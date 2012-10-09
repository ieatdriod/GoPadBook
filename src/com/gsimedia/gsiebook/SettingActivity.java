package com.gsimedia.gsiebook;

import java.util.Locale;

import com.gsimedia.gsiebook.lib.GSiDatabaseAdapter;
import com.taiwanmobile.myBook_PAD.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

public class SettingActivity extends ListActivity {

	HelpAdapter iAdapter = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gsi_setting);
		findViews();
		this.bRotate = !(getIntent().getBooleanExtra(RendererActivity.KEY_Fixed, true));
		this.bSwitch = (getIntent().getBooleanExtra(RendererActivity.KEY_Switch, false));
		iAdapter = new HelpAdapter(this);
		iListView.setAdapter(iAdapter);
		
	}
	ListView iListView = null;
	Button iBackView = null;
	private void findViews(){
		iListView = this.getListView();
		iBackView = (Button)findViewById(R.id.gsimedia_btn_title_left);
		iBackView.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				handleResult();
				finish();
			}
			
		});
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			handleResult();
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	private void handleResult(){
		Intent intent = new Intent();
		Bundle aBundle = new Bundle();
		intent.setClass(SettingActivity.this, RendererActivity.class);
		aBundle.putBoolean(RendererActivity.KEY_Fixed, bRotate);
		aBundle.putBoolean(RendererActivity.KEY_FirstPage, bShowFirstPage);
		aBundle.putBoolean(RendererActivity.KEY_Switch, bSwitch);
		intent.putExtras(aBundle);
		setResult(RendererActivity.Result_Setting, intent);
	}
	private boolean bRotate = false;
	private boolean bSwitch = false;
	private boolean bShowFirstPage = false;
	class HelpAdapter extends BaseAdapter{
		private static final int ITEM_ROTATE = 0;
		private static final int ITEM_SWITCH = 1;
		private static final int ITEM_HELP = 2;
		private static final int ITEM_Count = ITEM_HELP+1;
		TextView iHelpView = null;
		private LayoutInflater mInflater;
		HelpAdapter(Context c){
			mInflater = LayoutInflater.from(c);
			
		}

		public int getCount() {
			return ITEM_Count;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			switch(position){
			case ITEM_ROTATE:
				final CheckedTextView aView = (CheckedTextView)mInflater.inflate(R.layout.gsi_check_row, null);
				aView.setText(R.string.GSI_ROTATE_SETTING);
				aView.setChecked(bRotate);
				aView.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						aView.toggle();
						bRotate = aView.isChecked();
					}
					
				});
				convertView = aView;
				break;
			case ITEM_HELP://
				convertView = mInflater.inflate(R.layout.gsi_help_row, null);
				TextView aText = (TextView)convertView.findViewById(R.id.text);
				aText.setText(R.string.GSI_HELP_SETTING);
				convertView.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						bShowFirstPage = true;
						handleResult();
						// make renderer open first page
//						Intent intent = new Intent();
//						intent.setClass(SettingActivity.this, RendererActivity.class);
//						setResult(RendererActivity.Result_FirstPage, intent);
						finish();
					}
					
				});
				break;
			case ITEM_SWITCH:
				final CheckedTextView aSwitchView = (CheckedTextView)mInflater.inflate(R.layout.gsi_check_row, null);
				
				aSwitchView.setText(R.string.GSI_SWITCH_SETTING);
				aSwitchView.setChecked(bSwitch);
				aSwitchView.setOnClickListener(new OnClickListener(){

					public void onClick(View v) {
						aSwitchView.toggle();
						bSwitch = aSwitchView.isChecked();
					}
					
				});
				convertView = aSwitchView;

				break;
			
			}
			return convertView;
		}
		
	}
	
}
