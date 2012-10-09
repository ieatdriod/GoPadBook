package com.taiwanmobile.myBook_PAD;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gsimedia.gsiebook.RendererActivity;

import tw.com.soyong.AnReader;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StatFs;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
/**
 * 搜尋書本
 * @author III
 * 
 */
public class SearchBook extends Activity{
	ImageButton ib_search_back;
	ImageButton iFindButton = null;
	
	ImageButton imgBtn_TopBG = null;
	
	ImageView imgVbg_book_tittle = null;
	ImageButton ib_search_book_tittle = null;
	
	ImageView imgVbg_authors = null;
	ImageButton ib_search_authors = null;
	
	ImageView imgVbg_publisher = null;
	ImageButton ib_search_publisher = null;
	
	EditText et_search_text_input;
	
	ListView lv_search_result;
	private ProgressDialog progress;
	//private String path;
	//private ArrayList<String> spineList;
	//private final int SEARCH_FINISHED=0;
	private InputMethodManager imm;
	private TWMDB tdb;
	private int searchMode = 0;// 0書名   1作者   2出版社 
	private Cursor cursorDBData;
	ArrayList<String> text;
	//private String[] fileType = {".teb",".tvb"};
	private String saveFilelocation = "/sdcard/twmebook/";
	private SharedPreferences settings;	
	private String[] fileType = {".teb",".tvb",".tpb",};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);    
		setContentView(R.layout.iii_search_book);
		tdb = new TWMDB(this);
		isInnerSD();
		settings = getSharedPreferences("setting_Preference", 0);
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setViewComponent();
        setListener();
        
	}
	/**
	 * 設定按下KEYCODE_ENTER 及 KEYCODE_BACK 的事件
	 * KEYCODE_ENTER搜尋
	 * KEYCODE_BACK結束搜尋程式
	 * @param event
	 * @return 回傳事件是否被執行
	 */
    public  boolean	dispatchKeyEvent(KeyEvent event){
    	if(event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
    		if(event.getAction()==KeyEvent.ACTION_UP){
        		String kw = et_search_text_input.getText().toString().trim();
        		//Log.d("kw","is:"+kw);
        		imm.hideSoftInputFromWindow(et_search_text_input.getWindowToken(), 0);
        		progress = ProgressDialog.show(SearchBook.this, "", getResources().getString(R.string.iii_searching));
        		
        		if(kw.equals("")){
        			progress.dismiss();
        			Toast.makeText(this.getApplicationContext(),R.string.iii_search_tip, Toast.LENGTH_SHORT).show();
        		}else{
        			search(kw);
        			showResult();
        		}
    		}
    		return true;
    	}else if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
			finish();
			return true;
    	}
    	return super.dispatchKeyEvent(event);
    }
	/**
	 * 依照目前模式來搜尋相關內容
	 * @param key 搜尋字串
	 */
    private void search(String key) {
		// TODO Auto-generated method stub
    	text = new ArrayList<String>();
    	if (searchMode==0){       		
    		cursorDBData = tdb.select("isdownloadbook = '1' AND title LIKE '%"+key+"%'");
    	}else if (searchMode==1){
    		cursorDBData = tdb.select("isdownloadbook = '1' AND authors LIKE '%"+key+"%'");
    	}else {
    		cursorDBData = tdb.select("isdownloadbook = '1' AND publisher LIKE '%"+key+"%'");
    	}
    	
		cursorDBData.moveToFirst();
		for(int i=0;i<cursorDBData.getCount();i++){
			text.add(cursorDBData.getString(1));
			cursorDBData.moveToNext();
		}		
		tdb.close();
	}
	/**
	 * 宣告畫面view
	 */
	private void setViewComponent() {        
    	et_search_text_input = (EditText)findViewById(R.id.et_search_text_input);
    	
    	ib_search_back = (ImageButton)findViewById(R.id.ib_search_back);
    	imgBtn_TopBG = (ImageButton) findViewById(R.id.TopBG);
    	
    	imgVbg_book_tittle = (ImageView) findViewById(R.id.ex_ImageButton_BookName_bg);
    	ib_search_book_tittle = (ImageButton)findViewById(R.id.ib_search_book_tittle);
    	
    	imgVbg_authors = (ImageView) findViewById(R.id.ex_ImageButton_Author_bg);
    	ib_search_authors = (ImageButton)findViewById(R.id.ib_search_authors);
    	
    	imgVbg_publisher = (ImageView) findViewById(R.id.ex_ImageButton_Publisher_bg);
    	ib_search_publisher = (ImageButton)findViewById(R.id.ib_search_publisher);
    	
    	lv_search_result = (ListView)findViewById(R.id.lv_search_result);
    	
//    	ib_search_back.setBackgroundColor(Color.TRANSPARENT);
//    	ib_search_book_tittle.setBackgroundColor(Color.TRANSPARENT);
//    	ib_search_authors.setBackgroundColor(Color.TRANSPARENT);
//    	ib_search_publisher.setBackgroundColor(Color.TRANSPARENT);
//    	
//    	et_search_text_input.setBackgroundColor(Color.TRANSPARENT);
    	imm.showSoftInput(et_search_text_input, 0);
    	
    	iFindButton = (ImageButton)findViewById(R.id.btnSearchBegin);
    }
	/**
	 * 設定畫面view的 Listener
	 */
	private void setListener() {
		ib_search_back.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				finish();
      	  	}
        });
		ib_search_book_tittle.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				searchMode = 0;
				setSearchMode();
      	  	}
        });
		ib_search_authors.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				searchMode = 1;
				setSearchMode();
      	  	}
        });
		ib_search_publisher.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				searchMode = 2;
				setSearchMode();
      	  	}
        });	
		
		iFindButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				String kw = et_search_text_input.getText().toString().trim();
        		imm.hideSoftInputFromWindow(et_search_text_input.getWindowToken(), 0);
        		progress = ProgressDialog.show(SearchBook.this, "", getResources().getString(R.string.iii_searching));
        		
        		if(kw.equals("")){
        			progress.dismiss();
        			Toast.makeText(SearchBook.this.getApplicationContext(),R.string.iii_search_tip, Toast.LENGTH_SHORT).show();
        		}else{
        			search(kw);
        			showResult();
        		}
			}
		});
    }
	/**
	 * 依照目前模式顯示相關圖片
	 */
	public void setSearchMode() {
		switch (searchMode) {
			case 0:
				setBookTitleSelect(true);
				setAuthorSelect(false);
				setPublisherSelect(false);
				break;
			case 1:
				setBookTitleSelect(false);
				setAuthorSelect(true);
				setPublisherSelect(false);
				break;
			case 2:
				setBookTitleSelect(false);
				setAuthorSelect(false);
				setPublisherSelect(true);
				break;
		}
	}
	
	private void setBookTitleSelect(Boolean iSelected){
		if(iSelected){
			imgBtn_TopBG.setBackgroundResource(R.drawable.gsi_br06);
			imgVbg_book_tittle.setBackgroundResource(R.drawable.gsi_title_bg01);
			ib_search_book_tittle.setBackgroundResource(R.drawable.gsi_title14);
		}else{
			imgVbg_book_tittle.setBackgroundResource(R.drawable.gsi_title_bg01_1);
			ib_search_book_tittle.setBackgroundResource(R.drawable.gsi_title14_1);
		}
	}
	
	private void setAuthorSelect(Boolean iSelected){
		if(iSelected){
			imgBtn_TopBG.setBackgroundResource(R.drawable.gsi_br07);
			imgVbg_authors.setBackgroundResource(R.drawable.gsi_title_bg02);
			ib_search_authors.setBackgroundResource(R.drawable.gsi_title15);
		}else{
			imgVbg_authors.setBackgroundResource(R.drawable.gsi_title_bg02_1);
			ib_search_authors.setBackgroundResource(R.drawable.gsi_title15_1);
		}
	}
	
	private void setPublisherSelect(Boolean iSelected){
		if(iSelected){
			imgBtn_TopBG.setBackgroundResource(R.drawable.gsi_br08);
			imgVbg_publisher.setBackgroundResource(R.drawable.gsi_title_bg03);
			ib_search_publisher.setBackgroundResource(R.drawable.gsi_title16);
		}else{
			imgVbg_publisher.setBackgroundResource(R.drawable.gsi_title_bg03_1);
			ib_search_publisher.setBackgroundResource(R.drawable.gsi_title16_1);
		}
	}
	
	/**
	 * 建立清單資料
	 */
	private List<Map<String, Object>> buildListForSimpleAdapter() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(2);
		Map<String, Object> map;
		for(int i=0;i<text.size();i++){
			map = new HashMap<String, Object>();
			map.put("searchResult", text.get(i));
			list.add(map);
		}

		return list;
	}
	/**
	 * 將搜尋結果顯示出來
	 */
	private void showResult(){		
		//lv_search_result.setAdapter(new ArrayAdapter<String> (SearchBook.this,android.R.layout.simple_list_item_1,text));
		List<Map<String, Object>> list = buildListForSimpleAdapter();
		
		SimpleAdapter notes = new SimpleAdapter(this, list, R.layout.iii_ditem_row,
				new String[] { "searchResult"}, new int[] { R.id.searchResult});
		lv_search_result.setAdapter(notes);
		
		progress.dismiss();
		
		if(list.size()==0){
			Toast.makeText(this.getApplicationContext(),R.string.iii_search_not_found, Toast.LENGTH_SHORT).show();
		}
		
		lv_search_result.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,	int pos, long arg3) {
				lv_search_result.setEnabled(false);
				lv_search_result.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						lv_search_result.setEnabled(true);
					} 
				},300);
				// TODO Auto-generated method stub
				cursorDBData.moveToFirst();
				for(int i=0;i<pos;i++){
					cursorDBData.moveToNext(); 
				}
        		tdb.update(cursorDBData.getInt(0), "lastreadtime", String.valueOf(System.currentTimeMillis()));
        		tdb.update(cursorDBData.getInt(0) , "isread" , "1" );	
        		tdb.close();
        		openBook(cursorDBData.getString(2),cursorDBData.getString(19),
        				SearchBook.this.getFilesDir().toString(),cursorDBData.getString(13),cursorDBData.getString(9),
        				settings.getBoolean("setting_auto_sync_last_read_page_value", true),cursorDBData.getString(15),
        				cursorDBData.getString(1),cursorDBData.getString(12),cursorDBData.getString(11),cursorDBData.getString(3),cursorDBData.getString(17),cursorDBData.getString(21));
        		cursorDBData.close();

        		finish();
			}
			
		});
	}
	/**
	 * 檢查存放路徑
	 */
	public void isInnerSD(){
		 File a = new File(getExternalFilesDir(null), "twmebook");
		 saveFilelocation = a.getPath() + "/";

		 File b = new File(saveFilelocation);
		if ( !b.exists() ||  !b.isDirectory()) {
			saveFilelocation = getBaseContext().getFilesDir().toString() + "/";
		}

	}
	/**
	 * 開啟書本
	 * @param type 有書書  或 epub
	 * @param coverPath 檔案路徑
	 * @param p1 p12 p12路徑
	 * @param p2 isSample 是否試閱
	 * @param p3 coverPath 圖片路徑
	 * @param p4 syncLastPage 同步最後閱讀頁
	 * @param p5 content_id 書本content id
	 * @param p6 book_title 書本名稱
	 * @param p7 book_authors 書本作者
	 * @param p8 book_publisher 書本出版社
	 * @param p9 book_category 書本類別
	 * @param p10 book_vertical 書本是否垂直觀看
	 */
	private void openBook(String type,String coverPath,String p1,String p2,String p3,Boolean p4,String p5,String p6,String p7,String p8,String p9,String p10, String p11) {
		//this.showAlertMessage("開啟書本-- 路徑為 "+coverpath);		
 		Intent it = null;
 		if(type.equals(getResources().getString(R.string.iii_mebook))){
 			it= new Intent(SearchBook.this,AnReader.class);
 			it.setData(Uri.parse("mebook://"+coverPath));
 		}else if(p11.equals(fileType[2])){
 				
 				it= new Intent(SearchBook.this,RendererActivity.class);
 	 			it.setData(Uri.parse("pdf://"+coverPath));//"mnt/sdcard/sample.pdf"));	

 			
 		}else{
 			it= new Intent(SearchBook.this,Reader.class);
 			it.setData(Uri.parse("epub://"+coverPath));	
 		} 		
 		it.putExtra("p12",p1);
 		it.putExtra("isSample", p2);
 		it.putExtra("coverPath", p3);
 		it.putExtra("syncLastPage", p4);//同步最後閱讀頁
 		it.putExtra("content_id", p5);
 		it.putExtra("book_title", p6);
 		it.putExtra("book_authors", p7);
 		it.putExtra("book_publisher", p8);
 		it.putExtra("book_category", p9);
 		if(p10.equals("0"))
 			it.putExtra("book_vertical", false);
 		else
 			it.putExtra("book_vertical", true); 
 		it.putExtra("book_token", RealBookcase.getToken());
		startActivity(it);
	}	
}
