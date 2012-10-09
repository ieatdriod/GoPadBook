package com.taiwanmobile.myBook_PAD;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
/**
 * 建構書單項目
 * @author III
 * 
 */
public class BookList extends BaseAdapter{
	private static final boolean DEBUG = true ;
	protected LayoutInflater mInflater;
	protected List<String> ebook_title;
	protected List<String> ebook_cover;
	protected List<String> ebook_cover_url;
	protected List<String> ebook_isdownloadbook;
	protected List<String> ebook_deliveryid;
	protected List<String> ebook_trial;
	protected List<String> ebook_id;
	protected List<String> ebook_type;
	protected int width = 300; 
	protected Boolean isEdit;
	protected int allIsEdit = 0;
	protected ViewHolder holder;	
	protected Cursor myCursor;
	protected Boolean isDownload;
	//FIELD_TITLE = "title";						1
	//FIELD_TYPE = "type";							2
	//FIELD_CATEGORY = "category";					3
	//FIELD_COVER = "cover";						4
	//FIELD_DELIVERYID = "deliveryid";				5
	//IELD_ISREAD = "isread";						6
	//FIELD_LASTTIME = "lasttime";					7
	//FIELD_IS_DOWNLOAD_BOOK = "isdownloadbook";	8
	//FIELD_COVER_PATH = "coverpath";				9
	//FIELD_BUYTIME = "buytime";					10
	
	//,Boolean aie
	/**
	 * 宣告設定list
	 * @param context context
	 * @param c 資料庫搜尋出的書單資料
	 * @param ie 是否是編輯模式
	 */
	public BookList(Context context, Cursor c, Boolean ie){
		
		mInflater = LayoutInflater.from(context);
		isEdit = ie;
		myCursor = c;
		//if (DEBUG) Log.e("listbook", "+BookList "+c.) ;
		//isDownload = id;
		ebook_title = new ArrayList<String>();   
		if(myCursor.getCount() == 2)
		{
			int xx =0;
			xx++;
		}
//		try {
//			Thread.sleep(200);
//			myCursor.requery();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		ebook_title.clear();
		ebook_cover = new ArrayList<String>();   
		ebook_isdownloadbook = new ArrayList<String>();  	
		ebook_deliveryid = new ArrayList<String>();  	
		ebook_id = new ArrayList<String>();  
		ebook_trial = new ArrayList<String>();
		
		myCursor.moveToFirst();
		for(int i=0;i<myCursor.getCount();i++){
			ebook_title.add(myCursor.getString(1));
			ebook_cover.add(myCursor.getString(9));
			ebook_isdownloadbook.add(myCursor.getString(8));	
			ebook_deliveryid.add(c.getString(5));
			ebook_trial.add(c.getString(13));
			ebook_id.add(c.getString(0));	
			myCursor.moveToNext(); 
		}	
		/*
		for(int i=0;i<isDownload_2.length;i++){
			isDownload_2[i]=false;
		}
		*/
	}
	
	public void setAllIsEdit(int ais){
		allIsEdit = ais;
	}
	/**
	 * 宣告設定list
	 * @param context context
	 */
	public BookList(Context context){
		mInflater = LayoutInflater.from(context);
		ebook_title = new ArrayList<String>();   	
		ebook_cover = new ArrayList<String>();   
		ebook_isdownloadbook = new ArrayList<String>();
	}	
	/**
	 * 取得列表項目中的資料結構
	 * @return 列表項目中的資料結構
	 */
	public ViewHolder getViewHolder(){
		return holder;
	}
	/**
	 * 設定某一筆資料的圖片
	 * @param position 位置
	 * @param bm 圖片路徑
	 */
	public void setListImage(int position,String bm){
		ebook_cover.set(position, bm);
	}
	/**
	 * 取得資料筆數
	 * @return 資料筆數
	 */
	@Override
	public int getCount(){
		
		if (DEBUG) Log.e("listbook","BookList getCount => "+ebook_title.size()) ;
		return ebook_title.size();
	}
	/**
	 * 取得資料
	 * @param position 位置
	 * @return 資料
	 */
	@Override
	public Object getItem(int position){
		return ebook_title.get(position);
	}
	/**
	 * 取得資料id
	 * @param position 位置
	 * @return 資料
	 */
	@Override
	public long getItemId(int position){
		return position;
	}
	/**
	 * 重劃
	 */
	public void reDraw(){
		notifyDataSetChanged();
	}
	
	/**
	 * 取得list中的item的view
	 * @param position 位置
	 * @param convertView 當前的view
	 * @param parent ViewGroup
	 * @return 列表中的某一欄
	 */
	@Override
	public View getView(int position,View convertView,ViewGroup parent){			
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.iii_file_row, null);
			
			holder = new ViewHolder();
			holder.cb = (CheckBox) convertView.findViewById(R.id.cb);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
			holder.text = (TextView) convertView.findViewById(R.id.text);			
			holder.pbar = (ProgressBar)convertView.findViewById(R.id.progress);
			holder.cancel = (ImageButton) convertView.findViewById(R.id.cancel);
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();	
		}
		/*
		if(allIsEdit == 1){
			holder.cb.setChecked(true);
		}else if (allIsEdit == 2){
			holder.cb.setSelected(false);
		}
		
		if(isEdit)
			holder.cb.setVisibility(View.VISIBLE);
		else
			holder.cb.setVisibility(View.GONE);
		
		holder.text.setText(ebook_title.get(position).toString());
		
		holder.icon.setImageResource(ebook_cover.get(position));
		
		holder.text.setSingleLine(true);
		holder.text.setWidth(width);
		holder.text.setMarqueeRepeatLimit(6);
		
		if (ebook_isdownloadbook.get(position).toString().equals("0")){
			holder.icon.setAlpha(100);		
			//holder.text.setBackgroundColor(Color.argb(20, 50, 50, 0));
		}else{
			holder.icon.setAlpha(255);			
			//holder.text.setBackgroundColor(Color.alpha(255));
		}
		//notifyDataSetChanged();
		
		if(isDownload_2[position] == false){
			isDownload_2[position] = true;
			//Log.e("TAG","false -"+ position);
			download(position);
			book_img.set(position, BitmapFactory.decodeFile("/sdcard/"+(position+1)+".jpg"));
		}else{
			//Log.e("TAG","true -"+ position);
			book_img.set(position, BitmapFactory.decodeFile("/sdcard/"+(position+1)+".jpg"));
		}
		*/
		//Log.e("TAG"," -"+ position);
		
		return convertView;
	}
	/**
	 * 書單結構
	 * @author III
	 * 
	 */
	public class ViewHolder {		
		public CheckBox cb;
		public TextView text;
		public ImageView icon;
		public ImageView iconTag;
		public ProgressBar pbar;
		public ImageButton cancel;
	}
}