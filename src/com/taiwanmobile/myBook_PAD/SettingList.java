package com.taiwanmobile.myBook_PAD;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
/**
 * 建構書櫃設定項目
 * @author III
 * 
 */
public class SettingList extends BaseAdapter{
	protected LayoutInflater mInflater;
	protected SettingView holder;
	protected List<String> setting_title;
	protected List<String> setting_contents;
	protected String[] settingItem;
	/**
	 * 宣告設定list
	 * @param context context
	 */
	public SettingList(Context context){
		mInflater = LayoutInflater.from(context);
		setting_title = new ArrayList<String>();  
		setting_contents = new ArrayList<String>();  
		
		settingItem = context.getResources().getStringArray(R.array.iii_settingItem);
		
		for(int i=0;i<settingItem.length;i++){
			setting_title.add(settingItem[i]);
		}
		
		SharedPreferences settings = context.getSharedPreferences("bookcase_Preference", 0);
/*		settings.edit().putString("style", "木紋").commit();
		settings.edit().putString("syncNewBook", "ON").commit();
		settings.edit().putString("lastReadPage", "ON").commit();
		settings.edit().putString("backup", "備份").commit();
		settings.edit().putString("download", "下載").commit();
		settings.edit().putString("delBook", "刪除").commit();
		settings.edit().putString("intoTWM", "進入").commit();
*/
		setting_contents.add(settings.getString("style", ""));
		setting_contents.add(settings.getString("newBook", ""));
		setting_contents.add(settings.getString("lastReadPage", ""));
		setting_contents.add(settings.getString("backup", ""));
		setting_contents.add(settings.getString("download", ""));
		setting_contents.add(settings.getString("delBook", ""));
		setting_contents.add(settings.getString("intoTWM", ""));
	}

	/**
	 * 取得資料筆數
	 * @return 資料筆數
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return setting_title.size();
	}
	/**
	 * 取得資料
	 * @param position 第幾筆資料
	 * @return 第幾筆資料
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return setting_title.get(position);
	}
	/**
	 * 取得資料ID
	 * @param position 第幾筆資料
	 * @return 第幾筆資料ID
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	/**
	 * 取得畫面
	 * @param position 位置
	 * @param convertView 當前view
	 * @param parent ViewGroup
	 * @return 當前畫面
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.iii_setting_row, null);			
			holder = new SettingView();
			holder.itemName = (TextView) convertView.findViewById(R.id.tv_setting_itemName);	
			holder.itemValue = (TextView) convertView.findViewById(R.id.tv_setting_itemValue);	
			holder.cb_setting_itemValue = (CheckBox) convertView.findViewById(R.id.cb_setting_itemValue);
			holder.setting_seekBar = (SeekBar) convertView.findViewById(R.id.setting_seekBar);
			convertView.setTag(holder);
		}else{
			holder = (SettingView) convertView.getTag();	
		}
		
		if(position==1||position==2){
			holder.cb_setting_itemValue.setVisibility(View.VISIBLE);
			holder.itemValue.setVisibility(View.GONE);
		}else{
			holder.cb_setting_itemValue.setVisibility(View.GONE);
			holder.itemValue.setVisibility(View.VISIBLE);
		}
		if(position==3){
			holder.setting_seekBar.setMax(100);

			holder.setting_seekBar.setProgress(30);
			
			holder.itemValue.setOnClickListener(new TextView.OnClickListener(){
	      	  	public void onClick(View v){
	      	  		holder.itemValue.setVisibility(View.GONE);
	      	  		holder.setting_seekBar.setVisibility(View.VISIBLE);
	      	  	}	
	        }); 	

			holder.setting_seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {				
				@Override
				public void onProgressChanged(SeekBar seekBar,int progress,boolean fromTouch){
					Log.v("onProgressChanged()",String.valueOf(progress) + ", " + String.valueOf(fromTouch));		
				}	
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
			        Log.v("onStartTrackingTouch()",String.valueOf(seekBar.getProgress()));
			    }
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
			        Log.v("onStopTrackingTouch()", String.valueOf(seekBar.getProgress()));
			    }
			});
		}
		holder.itemName.setText(setting_title.get(position));
		holder.itemValue.setText(setting_contents.get(position));
		return convertView;
	}
	/**
	 * 設定view list的資料結構
	 */
	public class SettingView {
		TextView itemName;
		TextView itemValue;
		CheckBox cb_setting_itemValue;
		SeekBar setting_seekBar;
	}
}
