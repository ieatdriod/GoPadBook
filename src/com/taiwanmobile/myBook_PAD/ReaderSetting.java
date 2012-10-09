package com.taiwanmobile.myBook_PAD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 閱讀設定主程式
 * @author III
 * 
 */
public class ReaderSetting extends Activity{
	static final private float K_SCALE_RATIO = (float) 1.5;
	
	private ImageView iv_reader_setting_up;
	//private TextView tv_reader_setting_title;
	private PtPxConverter ppc;
	
	private SampleView sv_reader_setting_sample;
	
	private ReaderSettingGetter rsg;
	
	private TextView 
				tv_reader_setting_book_background_style,tv_reader_setting_book_background_style_value,
				tv_reader_setting_chinese_type,tv_reader_setting_night_mode,
				tv_reader_setting_font_size,
				tv_reader_setting_font_color,tv_reader_setting_font_color_value,
				tv_reader_setting_crossed_color,tv_reader_setting_crossed_color_value,
				tv_reader_setting_flip,tv_reader_setting_flip_value,
				tv_reader_setting_screen_rotation,tv_reader_setting_hidden,
				tv_reader_setting_instructions,
				tv_reader_setting_reply_default;
	
	private CheckBox cb_reader_setting_chinese_type_value,cb_reader_setting_night_mode_value,
				tv_reader_setting_screen_rotation_value,tv_reader_setting_hidden_value;
	
	private SeekBar sb_reader_setting_font_size_value;
	
	private ListView lv_reader_setting_book_background_style_value,lv_reader_setting_font_color_value,
				lv_reader_setting_crossed_color_value,
				lv_reader_setting_flip_value;
	
	private String[] settingItem;
	
	private SharedPreferences settings;
	private String deliverId;
	
	private String font_color_value_temp="";
	private String crossed_color_value_temp="";
	
	private TableRow tr_reader_setting_book_background_style,tr_reader_setting_font_size_value,tr_reader_setting_font_color,
						tr_reader_setting_crossed_color,tr_reader_setting_flip,
						tr_reader_setting_chinese_type,tr_reader_setting_night_mode,
						tr_reader_setting_screen_rotation,tr_reader_setting_hidden,tr_reader_setting_instructions,
						tr_reader_setting_reply_default;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		deliverId = getIntent().getStringExtra("deliverId");
		setContentView(R.layout.iii_reader_setting);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		rsg = new ReaderSettingGetter(getBaseContext());
        setViewComponent();
        
        setListener();

        setPreferences();
        
	}
	/**
	 * 設定並顯示閱讀設定的值 若無值則採用預設值 若有值則已xml為主
	 */
	private void setPreferences() {
		// TODO Auto-generated method stub
		
		settings = getSharedPreferences("reader_Preference", 0);
		
		if("".equals(settings.getString("reader_setting_book_background_style_value", ""))){
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value);
			tv_reader_setting_book_background_style_value.setText(temp[0]);
			settings.edit().putString("reader_setting_book_background_style_value", temp[0]).commit();
		}else{
			tv_reader_setting_book_background_style_value.setText(settings.getString("reader_setting_book_background_style_value", ""));
		}
		
		cb_reader_setting_chinese_type_value.setChecked(settings.getBoolean(deliverId+"reader_setting_chinese_type_value", false));
		settings.edit().putBoolean(deliverId+"reader_setting_chinese_type_value", settings.getBoolean(deliverId+"reader_setting_chinese_type_value", false)).commit();
		
		cb_reader_setting_night_mode_value.setChecked(settings.getBoolean("reader_setting_night_mode_value", false));
		settings.edit().putBoolean("reader_setting_night_mode_value", settings.getBoolean("reader_setting_night_mode_value", false)).commit();
		
		sb_reader_setting_font_size_value.setProgress(settings.getInt(deliverId+"reader_setting_font_size_value", Reader.DEFAULT_FONTSIZE_IDX));
		settings.edit().putInt(deliverId+"reader_setting_font_size_value", settings.getInt(deliverId+"reader_setting_font_size_value", Reader.DEFAULT_FONTSIZE_IDX)).commit();
		
		if("".equals(settings.getString("reader_setting_font_color_value", ""))){
			//String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
			tv_reader_setting_font_color_value.setText(getResources().getString(R.string.default_color));
			//settings.edit().putString(deliverId+"reader_setting_font_color_value", temp[0]).commit();
		}else{
			tv_reader_setting_font_color_value.setText(settings.getString("reader_setting_font_color_value", ""));
		}		

		if("".equals(settings.getString("reader_setting_crossed_color_value", ""))){
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
			tv_reader_setting_crossed_color_value.setText(temp[18]);
			settings.edit().putString("reader_setting_crossed_color_value", temp[18]).commit();
		}else{
			tv_reader_setting_crossed_color_value.setText(settings.getString("reader_setting_crossed_color_value", ""));
		}
		
		if("".equals(settings.getString("reader_setting_flip_value", ""))){
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_flip_value);
			tv_reader_setting_flip_value.setText(temp[0]);
			settings.edit().putString("reader_setting_flip_value", temp[0]).commit();
		}else{
			tv_reader_setting_flip_value.setText(settings.getString("reader_setting_flip_value", ""));
		}
		
		tv_reader_setting_screen_rotation_value.setChecked(settings.getBoolean("reader_setting_screen_rotation_value", true));
		settings.edit().putBoolean("reader_setting_screen_rotation_value", settings.getBoolean("reader_setting_screen_rotation_value", true)).commit();
		
		tv_reader_setting_hidden_value.setChecked(settings.getBoolean("reader_setting_hidden_value", true));
		settings.edit().putBoolean("reader_setting_hidden_value", settings.getBoolean("reader_setting_hidden_value", true)).commit();	
		
	}
	/**
	 * 設定畫面元件
	 */
	@SuppressWarnings({ "static-access" })
	private void setViewComponent() {
		// TODO Auto-generated method stub		
		ppc = new PtPxConverter();
		
		tr_reader_setting_book_background_style = (TableRow) findViewById(R.id.tr_reader_setting_book_background_style);
		tr_reader_setting_font_size_value = (TableRow) findViewById(R.id.tr_reader_setting_font_size_value);
		tr_reader_setting_font_color = (TableRow) findViewById(R.id.tr_reader_setting_font_color);
		tr_reader_setting_crossed_color = (TableRow) findViewById(R.id.tr_reader_setting_crossed_color);
		tr_reader_setting_flip = (TableRow) findViewById(R.id.tr_reader_setting_flip);
		tr_reader_setting_chinese_type = (TableRow) findViewById(R.id.tr_reader_setting_chinese_type);
		tr_reader_setting_night_mode = (TableRow) findViewById(R.id.tr_reader_setting_night_mode);
		tr_reader_setting_screen_rotation = (TableRow) findViewById(R.id.tr_reader_setting_screen_rotation);
		tr_reader_setting_hidden = (TableRow) findViewById(R.id.tr_reader_setting_hidden);
		tr_reader_setting_reply_default = (TableRow) findViewById(R.id.tr_reader_setting_reply_default);
		tr_reader_setting_instructions = (TableRow) findViewById(R.id.tr_reader_setting_instructions);
		
		iv_reader_setting_up = (ImageView) findViewById(R.id.iv_reader_setting_up);
		//tv_reader_setting_title = (TextView) findViewById(R.id.tv_reader_setting_title);
		
		sv_reader_setting_sample = (SampleView) findViewById(R.id.sv_reader_setting_sample);
		tv_reader_setting_book_background_style = (TextView) findViewById(R.id.tv_reader_setting_book_background_style);
		tv_reader_setting_book_background_style_value = (TextView) findViewById(R.id.tv_reader_setting_book_background_style_value);
		tv_reader_setting_chinese_type = (TextView) findViewById(R.id.tv_reader_setting_chinese_type);		
		tv_reader_setting_night_mode = (TextView) findViewById(R.id.tv_reader_setting_night_mode);
		tv_reader_setting_font_size = (TextView) findViewById(R.id.tv_reader_setting_font_size);
		tv_reader_setting_font_color = (TextView) findViewById(R.id.tv_reader_setting_font_color);
		tv_reader_setting_font_color_value = (TextView) findViewById(R.id.tv_reader_setting_font_color_value);	
		tv_reader_setting_crossed_color = (TextView) findViewById(R.id.tv_reader_setting_crossed_color);
		tv_reader_setting_crossed_color_value = (TextView) findViewById(R.id.tv_reader_setting_crossed_color_value);
		tv_reader_setting_flip = (TextView) findViewById(R.id.tv_reader_setting_flip);
		tv_reader_setting_flip_value = (TextView) findViewById(R.id.tv_reader_setting_flip_value);
		tv_reader_setting_screen_rotation = (TextView) findViewById(R.id.tv_reader_setting_screen_rotation);
		tv_reader_setting_hidden = (TextView) findViewById(R.id.tv_reader_setting_hidden);
		tv_reader_setting_reply_default = (TextView) findViewById(R.id.tv_reader_setting_reply_default);	
		tv_reader_setting_instructions = (TextView) findViewById(R.id.tv_reader_setting_instructions);	
		
		cb_reader_setting_chinese_type_value = (CheckBox) findViewById(R.id.cb_reader_setting_chinese_type_value);
		cb_reader_setting_night_mode_value = (CheckBox) findViewById(R.id.cb_reader_setting_night_mode_value);
		tv_reader_setting_screen_rotation_value = (CheckBox) findViewById(R.id.tv_reader_setting_screen_rotation_value);
		tv_reader_setting_hidden_value = (CheckBox) findViewById(R.id.tv_reader_setting_hidden_value);
		
		sb_reader_setting_font_size_value = (SeekBar) findViewById(R.id.sb_reader_setting_font_size_value);
		
		lv_reader_setting_book_background_style_value = (ListView) findViewById(R.id.lv_reader_setting_book_background_style_value);
		lv_reader_setting_font_color_value = (ListView) findViewById(R.id.lv_reader_setting_font_color_value);
		lv_reader_setting_crossed_color_value = (ListView) findViewById(R.id.lv_reader_setting_crossed_color_value);
		lv_reader_setting_flip_value = (ListView) findViewById(R.id.lv_reader_setting_flip_value);		
		
		settingItem = getResources().getStringArray(R.array.iii_reader_settingItem);		
		
		tv_reader_setting_book_background_style.setText(settingItem[0]);
		tv_reader_setting_chinese_type.setText(settingItem[1]);
		tv_reader_setting_night_mode.setText(settingItem[2]);
		tv_reader_setting_font_size.setText(settingItem[3]);
		tv_reader_setting_font_color.setText(settingItem[4]);
		tv_reader_setting_crossed_color.setText(settingItem[5]);
		tv_reader_setting_flip.setText(settingItem[6]);
		tv_reader_setting_screen_rotation.setText(settingItem[7]);
		tv_reader_setting_hidden.setText(settingItem[8]);
		tv_reader_setting_instructions.setText(settingItem[9]);
		tv_reader_setting_reply_default.setText(settingItem[10]);
		
		sb_reader_setting_font_size_value.setMax(11);
		
		lv_reader_setting_book_background_style_value.bringToFront();
/*		lv_reader_setting_book_background_style_value.setAdapter(
				new ArrayAdapter (this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value)));	*/
		List<Map<String, Object>> list;
		list = buildListForSimpleAdapter();
		lv_reader_setting_book_background_style_value.setAdapter(
				new SimpleAdapter(this, list, R.layout.iii_reader_setting_sub_list,	new String[] { "name", "img"}, new int[] { R.id.name,R.id.img}));	
		lv_reader_setting_book_background_style_value.setBackgroundColor(Color.WHITE);
		
		lv_reader_setting_font_color_value.bringToFront();
		list = buildListForColorSimpleAdapter();
		lv_reader_setting_font_color_value.setAdapter(
				new SimpleAdapter(this, list, R.layout.iii_reader_setting_sub_list,	new String[] { "name", "img"}, new int[] { R.id.name,R.id.img}));		
/*		lv_reader_setting_font_color_value.setAdapter(
				new ArrayAdapter (this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.iii_reader_setting_color_value)));	*/
		lv_reader_setting_font_color_value.setBackgroundColor(Color.WHITE);
		
		lv_reader_setting_crossed_color_value.bringToFront();
		list = buildListForColorSimpleAdapter();
		lv_reader_setting_crossed_color_value.setAdapter(
				new SimpleAdapter(this, list, R.layout.iii_reader_setting_sub_list,	new String[] { "name", "img"}, new int[] { R.id.name,R.id.img}));		
/*		lv_reader_setting_crossed_color_value.setAdapter(
				new ArrayAdapter (this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.iii_reader_setting_color_value)));	*/
		lv_reader_setting_crossed_color_value.setBackgroundColor(Color.WHITE);

		lv_reader_setting_flip_value.bringToFront();
		lv_reader_setting_flip_value.setAdapter(
				new ArrayAdapter<Object> (this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.iii_reader_setting_flip_value)));	
		lv_reader_setting_flip_value.setBackgroundColor(Color.GRAY);


		settings = getSharedPreferences("reader_Preference", 0);
		sv_reader_setting_sample.setBackgroundDrawable(rsg.getBackground(settings.getString("reader_setting_book_background_style_value", "")));
		
		sv_reader_setting_sample.setTextColor(rsg.getColor(settings.getString("reader_setting_font_color_value", "")));
		
		sv_reader_setting_sample.setHorizontal(settings.getBoolean(deliverId+"reader_setting_chinese_type_value", false));
		
		int[] fontSize = getScaledFontArray(getApplicationContext());//getResources().getIntArray(R.array.iii_reader_setting_font_size);
		sv_reader_setting_sample.setTextSize(ppc.getPxFromPt(fontSize[sb_reader_setting_font_size_value.getProgress()]));
		
		
		if (cb_reader_setting_night_mode_value.isChecked()){
			String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);		
			sv_reader_setting_sample.setTextColor(rsg.getColor(temp[3]));
			sv_reader_setting_sample.setLineColor(rsg.getColor(temp[3]));
			sv_reader_setting_sample.setHyperColor(rsg.getColor(temp[3]));
			sv_reader_setting_sample.setBackgroundColor(rsg.getColor(temp[0]));
		}
	}
	/**
	 * 按鍵按下事件
	 */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	//if(keyCode==KeyEvent.KEYCODE_BACK)
    	//	return true;
    	
    	return super.onKeyDown(keyCode, event);
    }
	/**
	 * 建構設定背景資料
	 */
    private List<Map<String, Object>> buildListForSimpleAdapter() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(2);
		// Build a map for the attributes

		int[] a = {R.drawable.bg_01_s,R.drawable.bg_02_s,R.drawable.bg_03_s};

//		int[] a = {R.drawable.bg_green_s,R.drawable.bg_stone_s,R.drawable.bg_maple_s,R.drawable.bg_water_s,R.drawable.bg_cloud_s};

		
		Map<String, Object> map;		
		for(int i=0;i<getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value).length;i++){
			map = new HashMap<String, Object>();
			map.put("name", getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value)[i]);
			map.put("img", a[i]);
			list.add(map);	
		}
		return list;
	}	
	/**
	 * 設定當按下back時將結束程式
	 */
    public boolean dispatchKeyEvent(KeyEvent event){
    	if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_UP){
    		finish();
    	}
    	return true;
    }
	/**
	 * 建構設定顏色資料
	 */
    private List<Map<String, Object>> buildListForColorSimpleAdapter() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(2);
		// Build a map for the attributes
		int[] a = {R.drawable.iii_Black_1,R.drawable.iii_Black_2,R.drawable.iii_Black_3,
				R.drawable.iii_While_1,R.drawable.iii_While_2,R.drawable.iii_While_3,
				R.drawable.iii_Brown_1,R.drawable.iii_Brown_2,R.drawable.iii_Brown_3,
				R.drawable.iii_Blue_1,R.drawable.iii_Blue_2,R.drawable.iii_Blue_3,
				R.drawable.iii_Green_1,R.drawable.iii_Green_2,R.drawable.iii_Green_3,
				R.drawable.iii_Orange_2,R.drawable.iii_Orange_3,R.drawable.iii_Yellow_1,
				R.drawable.iii_Red_1,R.drawable.iii_Red_2,R.drawable.iii_Red_3,
				R.drawable.iii_Purple_1,R.drawable.iii_Purple_2,R.drawable.iii_Purple_3};
		Map<String, Object> map;	
		//ReaderSettingGetter aa = new ReaderSettingGetter(getBaseContext());
		//Log.e(" VVV ", String.valueOf(getResources().getStringArray(R.array.iii_reader_setting_color_value).length));
		//Log.e(" VVV s ", String.valueOf(a.length));
		
		for(int i=0;i<getResources().getStringArray(R.array.iii_reader_setting_color_value).length;i++){
			map = new HashMap<String, Object>();
			map.put("name", getResources().getStringArray(R.array.iii_reader_setting_color_value)[i]);
			map.put("img",  a[i]);
			list.add(map);	
		}
		return list;
	}    
	/**
	 * 設定元件觸發時事件
	 */
	private void setListener() {
		// TODO Auto-generated method stub
		
		tr_reader_setting_book_background_style.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});
		
		tr_reader_setting_book_background_style.setOnClickListener(new TableLayout.OnClickListener(){
			public void onClick(View v){
				lv_reader_setting_book_background_style_value.setVisibility(View.VISIBLE);
      	  	}
        });
		
		tr_reader_setting_font_size_value.setOnTouchListener(new TableLayout.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});		
		
		tr_reader_setting_font_size_value.setOnClickListener(new TableLayout.OnClickListener(){
			public void onClick(View v){
				tv_reader_setting_font_size.setVisibility(View.GONE);
				sb_reader_setting_font_size_value.setVisibility(View.VISIBLE);
      	  	}
        });		
		
		tr_reader_setting_font_color.setOnTouchListener(new TableLayout.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});	
		
		tr_reader_setting_font_color.setOnClickListener(new TableLayout.OnClickListener(){
			public void onClick(View v){
				lv_reader_setting_font_color_value.setVisibility(View.VISIBLE);
      	  	}
        });
		
		tr_reader_setting_crossed_color.setOnTouchListener(new TableLayout.OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});	
		
		tr_reader_setting_crossed_color.setOnClickListener(new TableLayout.OnClickListener(){
			public void onClick(View v){
				lv_reader_setting_crossed_color_value.setVisibility(View.VISIBLE);
      	  	}
        });
	
		tr_reader_setting_flip.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});	
		
		tr_reader_setting_flip.setOnClickListener(new TableLayout.OnClickListener(){
			public void onClick(View v){
				lv_reader_setting_flip_value.setVisibility(View.VISIBLE);
      	  	}
        });
		
		tr_reader_setting_chinese_type.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});	
		
		tr_reader_setting_chinese_type.setOnClickListener(new TableLayout.OnClickListener(){
			public void onClick(View v){
				//cb_reader_setting_chinese_type_value.
				//settings.edit().putBoolean("reader_setting_chinese_type_value", isChecked).commit();
      	  	}
        });
		
		tr_reader_setting_night_mode.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});	
		
		tr_reader_setting_night_mode.setOnClickListener(new TableLayout.OnClickListener(){
			public void onClick(View v){
				//lv_reader_setting_flip_value.setVisibility(View.VISIBLE);
      	  	}
        });		
		
		tr_reader_setting_screen_rotation.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});	
		
		tr_reader_setting_screen_rotation.setOnClickListener(new TableLayout.OnClickListener(){
			public void onClick(View v){
				//lv_reader_setting_flip_value.setVisibility(View.VISIBLE);
      	  	}
        });
		
		tr_reader_setting_hidden.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});	
		
		tr_reader_setting_hidden.setOnClickListener(new TableLayout.OnClickListener(){
			public void onClick(View v){
				//lv_reader_setting_flip_value.setVisibility(View.VISIBLE);
      	  	}
        });

		iv_reader_setting_up.setOnClickListener(new TextView.OnClickListener(){
			public void onClick(View v){
				finish();
      	  	}
        });
		
		lv_reader_setting_book_background_style_value.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String[] temp = getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value);
				settings.edit().putString("reader_setting_book_background_style_value", temp[arg2]).commit();
				lv_reader_setting_book_background_style_value.setVisibility(View.GONE);
				tv_reader_setting_book_background_style_value.setText(temp[arg2]);	
				//ReaderSettingGetter aa = new ReaderSettingGetter(getBaseContext());
				
				if (cb_reader_setting_night_mode_value.isChecked()==false){					
					sv_reader_setting_sample.setBackgroundDrawable(rsg.getBackground(temp[arg2]));
				}else{
					sv_reader_setting_sample.setTextColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setLineColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setHyperColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setBackgroundColor(rsg.getColor(temp[0]));
				}						
			}
		});
		
		cb_reader_setting_chinese_type_value.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
	        	settings.edit().putBoolean(deliverId+"reader_setting_chinese_type_value", isChecked).commit();	
	        	sv_reader_setting_sample.setHorizontal(settings.getBoolean(deliverId+"reader_setting_chinese_type_value", false));
			}
		});
		
		cb_reader_setting_night_mode_value.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
	        	settings.edit().putBoolean("reader_setting_night_mode_value", isChecked).commit();	
				String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);			
				//ReaderSettingGetter rsg = new ReaderSettingGetter(getBaseContext());
				
				if (cb_reader_setting_night_mode_value.isChecked()==false){					
					sv_reader_setting_sample.setBackgroundDrawable(rsg.getBackground(settings.getString("reader_setting_book_background_style_value", "")));
					sv_reader_setting_sample.setTextColor(rsg.getColor(settings.getString("reader_setting_font_color_value", "")));
					tr_reader_setting_crossed_color.setEnabled(true);
					tr_reader_setting_font_color.setEnabled(true);
					tr_reader_setting_book_background_style.setEnabled(true);
					tv_reader_setting_book_background_style_value.setTextColor(Color.BLACK);
					tv_reader_setting_font_color_value.setTextColor(Color.BLACK);
					tv_reader_setting_crossed_color_value.setTextColor(Color.BLACK);
					if(font_color_value_temp.equals("")){
						font_color_value_temp = (String) tv_reader_setting_font_color_value.getText();
						crossed_color_value_temp = (String) tv_reader_setting_crossed_color_value.getText();
					}else{
						tv_reader_setting_font_color_value.setText(font_color_value_temp);
						tv_reader_setting_crossed_color_value.setText(crossed_color_value_temp);
					}

				}else{
					font_color_value_temp = (String) tv_reader_setting_font_color_value.getText();
					crossed_color_value_temp = (String) tv_reader_setting_crossed_color_value.getText();
					
					tr_reader_setting_crossed_color.setEnabled(false);
					tr_reader_setting_font_color.setEnabled(false);
					tr_reader_setting_book_background_style.setEnabled(false);
					
					tv_reader_setting_book_background_style_value.setTextColor(Color.GRAY);
										
					tv_reader_setting_font_color_value.setText(temp[3]);
					tv_reader_setting_font_color_value.setTextColor(Color.GRAY);
					
					tv_reader_setting_crossed_color_value.setText(temp[3]);
					tv_reader_setting_crossed_color_value.setTextColor(Color.GRAY);
					
					sv_reader_setting_sample.setTextColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setLineColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setHyperColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setBackgroundColor(rsg.getColor(temp[0]));
				}      	
			}
		});		
		
		sb_reader_setting_font_size_value.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			@SuppressWarnings("static-access")
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				int[] fontSize = getScaledFontArray(getApplicationContext());//getResources().getIntArray(R.array.iii_reader_setting_font_size);
				sv_reader_setting_sample.setTextSize(ppc.getPxFromPt(fontSize[sb_reader_setting_font_size_value.getProgress()]));				
				settings.edit().putInt(deliverId+"reader_setting_font_size_value", sb_reader_setting_font_size_value.getProgress()).commit();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				tv_reader_setting_font_size.setVisibility(View.VISIBLE);
				sb_reader_setting_font_size_value.setVisibility(View.GONE);
			}
		});  		

		lv_reader_setting_font_color_value.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
				settings.edit().putString("reader_setting_font_color_value", temp[arg2]).commit();
				lv_reader_setting_font_color_value.setVisibility(View.GONE);	
				tv_reader_setting_font_color_value.setText(temp[arg2]);
				//ReaderSettingGetter aa = new ReaderSettingGetter(getBaseContext());
				
				if (cb_reader_setting_night_mode_value.isChecked()==false){					
					sv_reader_setting_sample.setTextColor(rsg.getColor(temp[arg2]));
				}else{
					sv_reader_setting_sample.setTextColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setLineColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setHyperColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setBackgroundColor(rsg.getColor(temp[0]));
				}
			}
		});		
		
		lv_reader_setting_crossed_color_value.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String[] temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
				settings.edit().putString("reader_setting_crossed_color_value", temp[arg2]).commit();
				lv_reader_setting_crossed_color_value.setVisibility(View.GONE);		
				tv_reader_setting_crossed_color_value.setText(temp[arg2]);
				
				//ReaderSettingGetter aa = new ReaderSettingGetter(getBaseContext());
				
				if (cb_reader_setting_night_mode_value.isChecked()==false){					
					sv_reader_setting_sample.setLineColor(rsg.getColor(temp[arg2]));
				}else{
					sv_reader_setting_sample.setTextColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setLineColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setHyperColor(rsg.getColor(temp[3]));
					sv_reader_setting_sample.setBackgroundColor(rsg.getColor(temp[0]));
				}
			}
		});		
				
		lv_reader_setting_flip_value.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String[] temp = getResources().getStringArray(R.array.iii_reader_setting_flip_value);
				settings.edit().putString("reader_setting_flip_value", temp[arg2]).commit();
				lv_reader_setting_flip_value.setVisibility(View.GONE);		
				tv_reader_setting_flip_value.setText(temp[arg2]);
			}
		});			
		
		tv_reader_setting_screen_rotation_value.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
	        	settings.edit().putBoolean("reader_setting_screen_rotation_value", isChecked).commit();			
			}
		});	
		
		tv_reader_setting_hidden_value.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
	        	settings.edit().putBoolean("reader_setting_hidden_value", isChecked).commit();			
			}
		});	
		
		tv_reader_setting_instructions.setOnClickListener(new TextView.OnClickListener(){
			public void onClick(View v){
				tv_reader_setting_instructions.setEnabled(false);
				tv_reader_setting_instructions.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						tv_reader_setting_instructions.setEnabled(true);
					} 
				},400);
				settings.edit().putBoolean("show_manual", true).commit();
				finish();


/*				clear();
				View convertView = LayoutInflater.from(ReaderSetting.this).inflate(R.layout.iii_instructions, null);
				tr_reader_setting_instructions.setBackgroundColor(Color.LTGRAY);
				new AlertDialog.Builder(ReaderSetting.this)
				.setTitle(R.string.iii_introduction)
				.setView(convertView)
				.setPositiveButton(R.string.iii_showAM_ok,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){
 
							}
						}
				)
				.show();*/
      	  	}
        });
		
		tr_reader_setting_instructions.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});
		
		tv_reader_setting_reply_default.setOnClickListener(new TextView.OnClickListener(){
			public void onClick(View v){
				tv_reader_setting_reply_default.setEnabled(false);
				tv_reader_setting_reply_default.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						tv_reader_setting_reply_default.setEnabled(true);
					} 
				},400);
				setDefault();
				Toast.makeText(ReaderSetting.this.getApplicationContext(),R.string.iii_setting_default, Toast.LENGTH_SHORT).show();
				clear();
				tr_reader_setting_reply_default.setBackgroundColor(Color.LTGRAY);
      	  	}
        });
		
		tr_reader_setting_reply_default.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});	
	}	
	/**
	 * 清空其他選項背景顏色
	 */
	public void clear(){
		tr_reader_setting_book_background_style.setBackgroundColor(Color.TRANSPARENT);
		tr_reader_setting_chinese_type.setBackgroundColor(Color.TRANSPARENT);
		tr_reader_setting_night_mode.setBackgroundColor(Color.TRANSPARENT);
		tr_reader_setting_font_size_value.setBackgroundColor(Color.TRANSPARENT);
		tr_reader_setting_font_color.setBackgroundColor(Color.TRANSPARENT);
		tr_reader_setting_crossed_color.setBackgroundColor(Color.TRANSPARENT);
		tr_reader_setting_flip.setBackgroundColor(Color.TRANSPARENT);
		tr_reader_setting_screen_rotation.setBackgroundColor(Color.TRANSPARENT);
		tr_reader_setting_hidden.setBackgroundColor(Color.TRANSPARENT);
		tr_reader_setting_instructions.setBackgroundColor(Color.TRANSPARENT);
		tr_reader_setting_reply_default.setBackgroundColor(Color.TRANSPARENT);
	}
	/**
	 * 將值設定為預設值
	 */
	@SuppressWarnings("static-access")
	public void setDefault(){
		
		String[] temp = getResources().getStringArray(R.array.iii_reader_setting_book_background_style_value);
		tv_reader_setting_book_background_style_value.setText(temp[0]);
		settings.edit().putString("reader_setting_book_background_style_value", temp[0]).commit();
		
		cb_reader_setting_chinese_type_value.setChecked(false);
		settings.edit().putBoolean(deliverId+"reader_setting_chinese_type_value", false).commit();
		
		cb_reader_setting_night_mode_value.setChecked(false);
		settings.edit().putBoolean("reader_setting_night_mode_value", false).commit();
		
		sb_reader_setting_font_size_value.setProgress(Reader.DEFAULT_FONTSIZE_IDX);
		settings.edit().putInt(deliverId+"reader_setting_font_size_value", Reader.DEFAULT_FONTSIZE_IDX).commit();
		
		temp = getResources().getStringArray(R.array.iii_reader_setting_color_value);
		
		tv_reader_setting_font_color_value.setText(getResources().getString(R.string.default_color));
		//settings.edit().putString(deliverId+"reader_setting_font_color_value", temp[0]).commit();
		settings.edit().putString("reader_setting_font_color_value", "").commit();
		
		tv_reader_setting_crossed_color_value.setText(temp[18]);
		settings.edit().putString("reader_setting_crossed_color_value", temp[18]).commit();

		temp = getResources().getStringArray(R.array.iii_reader_setting_flip_value);
		tv_reader_setting_flip_value.setText(temp[0]);
		settings.edit().putString("reader_setting_flip_value", temp[0]).commit();
		
		tv_reader_setting_screen_rotation_value.setChecked(true);
		settings.edit().putBoolean("reader_setting_screen_rotation_value", true).commit();
		
		tv_reader_setting_hidden_value.setChecked(true);
		settings.edit().putBoolean("reader_setting_hidden_value", true).commit();	
		
		sv_reader_setting_sample.setBackgroundDrawable(rsg.getBackground(settings.getString("reader_setting_book_background_style_value", "")));
		sv_reader_setting_sample.setTextColor(rsg.getColor(settings.getString("reader_setting_font_color_value", "")));
		sv_reader_setting_sample.setHorizontal(settings.getBoolean(deliverId+"reader_setting_chinese_type_value", false));
		
		int[] fontSize = getScaledFontArray(getApplicationContext());//getResources().getIntArray(R.array.iii_reader_setting_font_size);
		sv_reader_setting_sample.setTextSize(ppc.getPxFromPt(fontSize[sb_reader_setting_font_size_value.getProgress()]));
				
	}
	
	public static final int []getScaledFontArray(Context ctx) {
		int[] fonts= ctx.getResources().getIntArray(R.array.iii_reader_setting_font_size);	
		
        DisplayMetrics dm = new DisplayMetrics();
        dm = ctx.getApplicationContext().getResources().getDisplayMetrics();
        float scaledDensity = dm.scaledDensity;
        
		final int count = fonts.length;  
		for (int i=0;i<count;i++){
			fonts[i] = (int) (fonts[i]/(scaledDensity/K_SCALE_RATIO));
		}
		return fonts;	}
}