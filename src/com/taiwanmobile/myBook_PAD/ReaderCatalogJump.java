package com.taiwanmobile.myBook_PAD;

import java.util.ArrayList;
import java.util.List;

import org.iii.ideas.reader.annotation.Annotation;
import org.iii.ideas.reader.annotation.AnnotationDB;
import org.iii.ideas.reader.bookmark.Bookmark;
import org.iii.ideas.reader.bookmark.Bookmarks;
import org.iii.ideas.reader.underline.Underline;
import org.iii.ideas.reader.underline.UnderlineDB;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.taiwanmobile.myBook_PAD.ReaderCatalogJumpList.ViewHolder;

/**
 * 目錄與跳頁
 * @author III
 * 
 */
@SuppressWarnings("deprecation")
public class ReaderCatalogJump extends ListActivity {

	/** Called when the activity is first created. */
	private Button ib_reader_back;
	private Button ib_reader_bookmark_del;
	private ImageView tv_reader_title_2;
	
	private RelativeLayout al_reader_book_info;
	private Button ib_reader_catalog_jump_bottom;
	private Button ib_reader_bookmark_index_bottom;
	private Button ib_reader_highlight_index_bottom;
	private Button ib_reader_notes_index_bottom;
	private Button ib_reader_book_info_bottom;
	
	private Button ib_type_score;
	  
	
	
	private ImageView iv_reader_book_info_img;
	
	private TextView tv_reader_book_info_category,tv_reader_book_info_title,tv_reader_book_info_publisher,tv_reader_book_info_authors;
	
	private ListView lv_reader_catalog_jump;
	
	private int nowStatus = 0;// 0 目錄跳頁    	 1 書籤索引  	2劃線索引	 3註記索引	 4本書資訊
	
	private Boolean isEdit = false;
	
	private List<String> rt;
	
	private ReaderCatalogList myrcjl;
	
	private Boolean[] allCheckBoxValue = null;
	
	//private int checkBoxPosition;
	
	private ViewHolder[] mainRow = null;
	
	private String epubPath = "epub:///sdcard/471000000010000084-00-01.teb";
	
	private TWMDB tdb;
	private ArrayList<String> tocHref;
	private ArrayList<String> tocText;
	private ArrayList<Bookmark> bmList;
	private ArrayList<Annotation> annList;
	private ArrayList<Underline> ulList;
	private Cursor cursorDBData;
	private Bookmarks bmHelper;
	private AnnotationDB annHelper;
	private UnderlineDB ulHelper;
	@Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.iii_reader_catalog_jump);
        
        setContentView(R.layout.gsi_detail);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        tdb = new TWMDB(this);
        bmHelper = new Bookmarks(this);
		annHelper = new AnnotationDB(this);
		ulHelper = new UnderlineDB(this);
        
        Intent it = getIntent();
        epubPath=it.getStringExtra("epub_path");
        tocHref=it.getStringArrayListExtra("toc_href");
        tocText=it.getStringArrayListExtra("toc_text");
        setViewComponent();
        setListener();
                
        setList();

    }
    public void onStop(){
    	
    	super.onStop();
    }
	
    private int actualListSize;
	/**
	 * 設定目前畫面list
	 */
	private void setList(){
		switch(nowStatus){
		case 0:
			rt=tocText;
			break;
		case 1:
			bmList=bmHelper.getBookmarksByEpubPath(epubPath);
			rt=new ArrayList<String>();
			Log.e("bmList.size()", String.valueOf(bmList.size()));
			for(int i=0;i<bmList.size();i++){
				rt.add(bmList.get(i).description);
			}
			break;	
		case 2:
			ulList=ulHelper.getUnderlineByEpubPath(epubPath);
			rt=new ArrayList<String>();
			for(int i=0;i<ulList.size();i++){
				rt.add(ulList.get(i).description);
			}
			break;
		case 3:
			annList=annHelper.getAnnsByEpubPath(epubPath);
			rt=new ArrayList<String>();
			for(int i=0;i<annList.size();i++){
				rt.add(annList.get(i).content);
			}
			break;
		}
		
		actualListSize=rt.size();
		while( rt.size()<10 ){
			rt.add("");
		}
		//setCheckBok(rt.size());
		setCheckBok(actualListSize);
        mainRow = new ViewHolder[rt.size()];
        myrcjl = new ReaderCatalogList(this,false,rt);
        lv_reader_catalog_jump.setAdapter(myrcjl);
        lv_reader_catalog_jump.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if(arg2<actualListSize)
					jumpToPosition(arg2,nowStatus);
			}
        	
        });
	}
	/**
	 * 跳置目標位置
	 * @param pos list中第幾項目
	 * @param localStatus 目前狀態0 目錄 1 書籤 2 劃綫 3 註記
	 */
	private void jumpToPosition(final int pos,final int localStatus){
		Intent it=new Intent();
		Bundle result = new Bundle();
		result.putBoolean("shouldJump", true);
		switch(localStatus){
		case 0:
			result.putBoolean("isToc", true);
			result.putString("chap", tocHref.get(pos));
			break;
		case 1:
			result.putString("chap", bmList.get(pos).chapterName);
			result.putInt("span", bmList.get(pos).position1);
			result.putInt("idx", bmList.get(pos).position2);
			break;	
		case 2:
			result.putString("chap", ulList.get(pos).chapterName);
			result.putInt("span", ulList.get(pos).span1);
			result.putInt("idx", ulList.get(pos).idx1);
			break;
		case 3:
			result.putString("chap", annList.get(pos).chapterName);
			result.putInt("span", annList.get(pos).position1);
			result.putInt("idx", annList.get(pos).position2);
			break;
		}
		it.putExtra("result", result);
		setResult(RESULT_OK,it);
		leaveAndFinish();
	}
	/**
	 * 離開目錄與跳頁
	 */
	private void leaveAndFinish(){
		
		if ( null != bmHelper ){
			bmHelper.closeDB();
			bmHelper = null;
		}
		
		if ( null != annHelper ) {
			annHelper.closeDB();
			annHelper = null;
		}
		
		if ( null != ulHelper){
			ulHelper.closeDB();
			ulHelper = null;
		}
		
		if ( null != tdb){
			tdb.close();
			tdb = null;
		}
		finish();
	}
	/**
	 *設定畫面view
	 */
    private void setViewComponent() {        
        
    	ib_reader_back = (Button) findViewById(R.id.gsimedia_btn_title_left);
    	tv_reader_title_2 = (ImageView) findViewById(R.id.gsimedia_title);
    	ib_reader_bookmark_del = (Button) findViewById(R.id.gsimedia_btn_title_right);
    	
    	al_reader_book_info = (RelativeLayout) findViewById(R.id.bookinfo);
    	
        ib_reader_catalog_jump_bottom = (Button) findViewById(R.id.gsimedia_btn_toc);
        ib_reader_bookmark_index_bottom = (Button) findViewById(R.id.gsimedia_btn_bookmark);
        ib_reader_highlight_index_bottom = (Button) findViewById(R.id.gsimedia_btn_marker);
        ib_reader_notes_index_bottom = (Button) findViewById(R.id.gsimedia_btn_annotation);
        ib_reader_book_info_bottom = (Button) findViewById(R.id.gsimedia_btn_info);
        
        //lv_reader_catalog_jump = (ListView) findViewById(R.id.android_list);
        lv_reader_catalog_jump = this.getListView();
        	
        iv_reader_book_info_img = (ImageView) findViewById(R.id.cover);
        tv_reader_book_info_category = (TextView) findViewById(R.id.type);
        tv_reader_book_info_title = (TextView) findViewById(R.id.title);
        tv_reader_book_info_publisher = (TextView) findViewById(R.id.publishertxt);
        tv_reader_book_info_authors = (TextView) findViewById(R.id.authortxt);
        ib_type_score = (Button) findViewById(R.id.more);       
        
        ib_reader_bookmark_del.setVisibility(View.GONE);
        tv_reader_title_2.setImageResource(R.drawable.gsi_title01);
    }
	/**
	 * 判斷按鍵按下動作
	 * @param keyCode keyCode
	 * @param event event
	 * @return 事件是否處理
	 */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent it=new Intent();
			Bundle result = new Bundle();
			result.putBoolean("shouldJump", false);
			it.putExtra("result", result);
			setResult(RESULT_OK, it);
			leaveAndFinish();
           return true;
        }
        return false;
    }
	/**
	 * 設定畫面view Listener
	 */
	private void setListener() {

		ib_reader_back.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				Intent it=new Intent();
				Bundle result = new Bundle();
				result.putBoolean("shouldJump", false);
				it.putExtra("result", result);
				setResult(RESULT_OK, it);
				leaveAndFinish();
      	  	}
        });         
        
		ib_reader_bookmark_del.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){		                
		        clickDel();
      	  	}
        });         
        
		ib_reader_catalog_jump_bottom.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				setButtonStyle(0);
				lv_reader_catalog_jump.setVisibility(View.VISIBLE);
				al_reader_book_info.setVisibility(View.GONE);
				ib_reader_bookmark_del.setVisibility(View.GONE);
				setList();
      	  	}
        }); 
        
		ib_reader_bookmark_index_bottom.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				setButtonStyle(1);
				lv_reader_catalog_jump.setVisibility(View.VISIBLE);
				setList();
		        //myrcjl = new ReaderCatalogList(getBaseContext(),false,rt);
		        //lv_reader_catalog_jump.setAdapter(myrcjl);				
				ib_reader_bookmark_del.setVisibility(View.VISIBLE);
				al_reader_book_info.setVisibility(View.GONE);
      	  	}
        });    

		ib_reader_highlight_index_bottom.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				setButtonStyle(2);
				lv_reader_catalog_jump.setVisibility(View.VISIBLE);
				setList();
		        //myrcjl = new ReaderCatalogList(getBaseContext(),false,rt);
		        //lv_reader_catalog_jump.setAdapter(myrcjl);					
				ib_reader_bookmark_del.setVisibility(View.VISIBLE);
				al_reader_book_info.setVisibility(View.GONE);
      	  	}
        });
        
		ib_reader_notes_index_bottom.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				setButtonStyle(3);
				lv_reader_catalog_jump.setVisibility(View.VISIBLE);
				setList();
		        //myrcjl = new ReaderCatalogList(getBaseContext(),false,rt);
		        //lv_reader_catalog_jump.setAdapter(myrcjl);					
				ib_reader_bookmark_del.setVisibility(View.VISIBLE);
				al_reader_book_info.setVisibility(View.GONE);
      	  	}
        });
        
		ib_reader_book_info_bottom.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
				setButtonStyle(4);
				al_reader_book_info.setVisibility(View.VISIBLE);
				
				cursorDBData = tdb.select("deliveryID = '"+epubPath+"'");
				//System.out.println(testPath.substring(testPath.lastIndexOf("/")+1, testPath.lastIndexOf(".")));
				cursorDBData.moveToFirst();		        
				
				ib_type_score = (Button) findViewById(R.id.more);
		        
				String path = cursorDBData.getString(9);
				Drawable dd = Drawable.createFromPath(path);
		        //iv_reader_book_info_img.setImageDrawable(Drawable.createFromPath(cursorDBData.getString(9)));
				iv_reader_book_info_img.setBackgroundDrawable(dd);
				
		        tv_reader_book_info_category.setText(cursorDBData.getString(3).substring(0, cursorDBData.getString(3).length()-1));		        
		        
		        tv_reader_book_info_title.setText(cursorDBData.getString(1));
		        
		        TextView aa = new TextView(getBaseContext());
		        aa.setText(R.string.iii_publisher);
		        
		        tv_reader_book_info_publisher.setText(aa.getText() + cursorDBData.getString(11));
		        
		        aa.setText(R.string.iii_authors);
		        
		        tv_reader_book_info_authors.setText(aa.getText() + cursorDBData.getString(12));
		        
				ReaderCatalogJump.this.stopManagingCursor(cursorDBData);
		        cursorDBData.close();
				lv_reader_catalog_jump.setVisibility(View.GONE);
				ib_reader_bookmark_del.setVisibility(View.GONE);
      	  	}
        });
		
		ib_type_score.setOnClickListener(new ImageButton.OnClickListener(){
			public void onClick(View v){
		        //Drawable da = null;		        
		        //da.createFromPath(cursorDBData.getString(9));
				Log.e("iii_book_score_url", "iii_book_score_url");
				cursorDBData = tdb.select("deliveryID = '"+epubPath+"'");
				cursorDBData.moveToFirst();
				Intent iIntent = (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(getResources().getString(R.string.iii_book_score_url)+cursorDBData.getString(15)));
				ReaderCatalogJump.this.stopManagingCursor(cursorDBData);
				cursorDBData.close();
				startActivity(iIntent);	
				//finish();
			}
        });        
    }
	/**
	 * 觸發螢幕動作
	 * @param event event
	 * @return 事件是否處理
	 */
    @Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
    	switch(event.getAction()){
    		case MotionEvent.ACTION_UP:
    			//ib_type_score.setImageResource(R.drawable.ani_more01);
    	}
		return true;
	}	
	/**
	 * 切換刪除模式
	 */
	protected void clickDel() {
        if (isEdit){
        	delSelect();
        	
        	setList();
	        isEdit = false;
        }else{ 	
	        myrcjl = new ReaderCatalogList(getBaseContext(),true,rt);
	        lv_reader_catalog_jump.setAdapter(myrcjl);
	        isEdit = true;
        }			
	}
	/**
	 * 刪除所選擇的項目
	 */
	protected void delSelect() {
		// 0 目錄跳頁    	 1 書籤索引  	2劃線索引	 3註記索引	 4本書資訊
		
		switch(nowStatus){
			case 1:
				for(int i=0;i<allCheckBoxValue.length;i++){
					if(allCheckBoxValue[i])
						bmHelper.deleteBookmarkById(bmList.get(i).id);
				}
				break;
			case 2:
				for(int i=0;i<allCheckBoxValue.length;i++){
					if(allCheckBoxValue[i])
						ulHelper.deleteUnderlineById(ulList.get(i).id);
				}
				break;
			case 3:
				for(int i=0;i<allCheckBoxValue.length;i++){
					if(allCheckBoxValue[i])
						annHelper.deleteAnnById(annList.get(i).id);
				}
				break;
		}
	}
	/**
	 * 設定畫面下方功能鍵圖片顯示
	 */
	public void setButtonStyle(int now){
		nowStatus = now;
		setAllCheckBok(false);
		
        
		switch(nowStatus){
			case 0:
		        tv_reader_title_2.setImageResource(R.drawable.gsi_title01);
				break;
			case 1:				
		        tv_reader_title_2.setImageResource(R.drawable.gsi_title02);
				break;
			case 2:				
		        tv_reader_title_2.setImageResource(R.drawable.gsi_title03);
				break;
			case 3:				
		        tv_reader_title_2.setImageResource(R.drawable.gsi_title04);
				break;
			case 4:					
		        tv_reader_title_2.setImageResource(R.drawable.gsi_title05);
				break;
		}
	}
	/**
	 * 設定畫面CheckBok
	 * @param count 數量
	 */
	public void setCheckBok(int count){
		allCheckBoxValue = new Boolean[count];
		setAllCheckBok(false);
	}
	/**
	 * 設定畫面全部CheckBok 是否勾選
	 * @param b true false
	 */
	public void setAllCheckBok(boolean b){// && i<actualListSize  allCheckBoxValue.length
		for(int i=0;i < allCheckBoxValue.length;i++){
			allCheckBoxValue[i] = b;
		}
	}		
	/**
	 * 清單項目
	 * @author III
	 * 
	 */
	public class ReaderCatalogList extends ReaderCatalogJumpList{
		/**
		 * 設定畫面
		 * @param position 第幾個位置
		 * @param convertView 當前畫面
		 * @param parent parent
		 * @return 列表中的某一欄
		 */
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.iii_reader_catalog_jump_row, null);
				mainRow[position] = new ViewHolder();
				mainRow[position].cb = (CheckBox) convertView.findViewById(R.id.reader_catalog_jump_row_cb);
				mainRow[position].text = (TextView) convertView.findViewById(R.id.reader_catalog_jump_row_text);		
				mainRow[position].textTag = (TextView) convertView.findViewById(R.id.reader_catalog_jump_row_textTag);
				convertView.setTag(mainRow[position]);
			}else{
				mainRow[position] = (ViewHolder) convertView.getTag();	
			}
			
			mainRow[position].cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					if(isChecked){
						allCheckBoxValue[position] = true;
						//checkBoxPosition = position;
					}else{
						allCheckBoxValue[position] = false;
					}											
				}
			});
			
			if(isEdit)
				if(position < actualListSize ){
					mainRow[position].cb.setVisibility(View.VISIBLE);
					mainRow[position].cb.setChecked(allCheckBoxValue[position]);
				}else{
					mainRow[position].cb.setVisibility(View.GONE);
				}
			else
				if(position < actualListSize ){
					mainRow[position].cb.setVisibility(View.GONE);
					//mainRow[position].cb.setChecked(allCheckBoxValue[position]);
				}

			mainRow[position].text.setText(rowTitle.get(position).toString());			
			mainRow[position].text.setTextColor(Color.BLACK);		
			mainRow[position].text.setSingleLine(true);
			mainRow[position].text.setWidth(width);
			mainRow[position].text.setMarqueeRepeatLimit(6);
			return convertView;
		}

		public ReaderCatalogList(Context context, boolean b, List<String> rt) {
			super(context,b,rt);
			// TODO Auto-generated constructor stub
		}
		
	}
}