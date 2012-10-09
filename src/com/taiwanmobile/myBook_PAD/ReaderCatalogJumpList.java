package com.taiwanmobile.myBook_PAD;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
/**
 * 目錄跳頁清單
 * @author III
 * 
 */
public class ReaderCatalogJumpList extends BaseAdapter{
	protected LayoutInflater mInflater;
	protected List<String> rowTitle;
	
	protected int width = 300; 
	protected Boolean isEdit;
	protected ViewHolder holder;	
	/**
	 * 宣告設定list
	 * @param context context
	 * @param ie 編輯模式
	 * @param rt 項目清單
	 */
	public ReaderCatalogJumpList(Context context, Boolean ie, List<String> rt){
		mInflater = LayoutInflater.from(context);
		isEdit = ie;
		rowTitle = rt;

	}
	/**
	 * 宣告設定list
	 * @param context context
	 */
	public ReaderCatalogJumpList(Context context){
		mInflater = LayoutInflater.from(context);
		rowTitle = new ArrayList<String>();   	
	}	
	/**
	 * 取得view資料結構
	 * @return view資料結構
	 */
	public ViewHolder getViewHolder(){
		return holder;
	}
	/**
	 * 取得資料筆數
	 * @return 資料筆數
	 */
	@Override
	public int getCount(){
		return rowTitle.size();
	}
	/**
	 * 取得資料
	 * @param position 第幾筆資料
	 * @return 第幾筆資料
	 */
	@Override
	public Object getItem(int position){
		return rowTitle.get(position);
	}
	/**
	 * 取得資料ID
	 * @param position 第幾筆資料
	 * @return 第幾筆資料ID
	 */
	@Override
	public long getItemId(int position){
		return position;
	}
	/**
	 * view資料結構
	 * @author III
	 * 
	 */
	public class ViewHolder {		
		CheckBox cb;
		TextView text;
		TextView textTag;
	}
	/**
	 * 取得畫面
	 * @param position 位置
	 * @param convertView 當前view
	 * @param parent ViewGroup
	 * @return 當前畫面
	 */
	@Override
	public View getView(int position,View convertView,ViewGroup parent){			
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.iii_reader_catalog_jump_row, null);
			
			holder = new ViewHolder();
			holder.cb = (CheckBox) convertView.findViewById(R.id.reader_catalog_jump_row_cb);
			holder.text = (TextView) convertView.findViewById(R.id.reader_catalog_jump_row_text);		
			holder.textTag = (TextView) convertView.findViewById(R.id.reader_catalog_jump_row_textTag);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();	
		}
		
		if(isEdit)
			holder.cb.setVisibility(View.VISIBLE);
		else
			holder.cb.setVisibility(View.GONE);
		
		holder.text.setText(rowTitle.get(position).toString());			
		holder.text.setTextColor(Color.BLACK);	
		holder.text.setSingleLine(true);
		holder.text.setWidth(width);
		holder.text.setMarqueeRepeatLimit(6);
		return convertView;
	}
}