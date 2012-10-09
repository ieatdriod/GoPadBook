package com.taiwanmobile.myBook_PAD;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * 無使用
 * @author III
 * 
 */
public class BookListAll extends BaseAdapter{
	private List<String> ebook_title;
	private List<String> ebook_type;
	private List<String> ebook_category;
	
	private List<String> tempTypeCount;
	private List<String> tempCategoryCount;
	private List<String> tempBookCount;
	
	private List<String> tempAllCount;
	
	private int typeCount = 0;
	//private int categoryCount = 0;
	//private int bookCount = 0;
	private String[] groups ;
    private String[][] children ;		
    //private Context mContext = null;

/*	FIELD_ID = "_id";								0
	FIELD_TITLE = "title";							1
	FIELD_TYPE = "type";							2
	FIELD_CATEGORY = "category";					3
	FIELD_COVER = "cover";							4
	FIELD_DELIVERYID = "deliveryid";				5
	FIELD_ISREAD = "isread";						6
	FIELD_LASTREADTIME = "lastreadtime";			7
	FIELD_IS_DOWNLOAD_BOOK = "isdownloadbook";		8
	FIELD_COVER_PATH = "coverpath";					9
	FIELD_BUYTIME = "buytime";						10  */
    private TWMDB tdb;

	public BookListAll(Context context,Cursor c, TWMDB db){
		//this.mContext = context;
		ebook_title = new ArrayList<String>();   	
		ebook_type = new ArrayList<String>();   	
		ebook_category = new ArrayList<String>();   	
		c.moveToFirst();
		for(int i=0;i<c.getCount();i++){
			ebook_title.add(c.getString(1));
			ebook_type.add(c.getString(2));
			ebook_category.add(c.getString(3));
			c.moveToNext(); 
		}
		tdb = db;	

		typeCount = getTypeCount().size();
		groups = new String[typeCount];
		
		int maxCount = 0;
		for(int i=0;i<typeCount;i++){
			groups[i] = tempTypeCount.get(i).toString();
			if(getCategoryCount(groups[i]).size() > maxCount)
				maxCount = getCategoryCount(groups[i]).size();
		}

		children = new String[groups.length][];
		for(int i=0;i<groups.length;i++){
			children[i] = new String[getCategoryCount(groups[i]).size()];
			for(int j=0;j<getCategoryCount(groups[i]).size();j++){
				children[i][j] = getCategoryCount(groups[i]).get(j).toString()+"�@ "+getBookCount(tempTypeCount.get(i).toString(),tempCategoryCount.get(j).toString()).size()+" ��  ";
			}
		}		
	}
	
	public int getListItemClickBook(int position){
		int potition_2 = 0;
		String aaa = tempAllCount.get(position).toString();		
		for(int i=0;i<ebook_title.size();i++){
			if(ebook_title.get(i).toString().equals(aaa))
				potition_2 = i;
		}		
		return potition_2;
	}
	
	public List<String> getTypeCount(){
		tempTypeCount = new ArrayList<String>();		
		String[] type = {"type"};
		Cursor cursorDBData = tdb.select(type);	
		cursorDBData.moveToFirst();
		for(int i=0;i<cursorDBData.getCount();i++){
			tempTypeCount.add(cursorDBData.getString(0));
			cursorDBData.moveToNext();
		}
		return tempTypeCount;
	}
	
	public List<String> getCategoryCount(String type){
		tempCategoryCount = new ArrayList<String>();
		
		String[] type2 = {"category"};
		Cursor cursorDBData = tdb.select(type2,"type = '"+type+"' ");	
		cursorDBData.moveToFirst();
		for(int i=0;i<cursorDBData.getCount();i++){
			tempCategoryCount.add(cursorDBData.getString(0));
			cursorDBData.moveToNext();
		}
		
		List<String> test = new ArrayList<String>();
		for(int i=0;i<tempCategoryCount.size();i++){
			do{
				test.add(tempCategoryCount.get(i).toString().substring(0, tempCategoryCount.get(i).toString().indexOf("|")));
				if(tempCategoryCount.get(i).toString().length() > 1){
					tempCategoryCount.set(i, tempCategoryCount.get(i).toString().substring(tempCategoryCount.get(i).toString().indexOf("|")+1, tempCategoryCount.get(i).toString().length()));
				}else if(tempCategoryCount.get(i).toString().length() == 1){
					tempCategoryCount.set(i, "");
				}
			}while(tempCategoryCount.get(i).toString().indexOf("|")>0);			
		}
		
		for(int i=0;i<test.size();i++){
			for(int j=i+1;j<test.size();j++){
				if(test.get(i).toString().equals(test.get(j).toString())){
					test.remove(j);
				}
			}
		}		
		tempCategoryCount = test;
		return tempCategoryCount;
	}	
	
	public List<String> getBookCount(String tempType,String tempCategory){
		tempBookCount = new ArrayList<String>();		
		Cursor cursorDBData = tdb.select("category LIKE '%"+tempCategory+"%'");
		cursorDBData.moveToFirst();
		for(int i=0;i<cursorDBData.getCount();i++){
			tempBookCount.add(cursorDBData.getString(1));
			cursorDBData.moveToNext();
		}
		return tempBookCount;
	}	
	
	public String getDetailTitle(int groupPosition, int childPosition){
		String detailTitle = "";
		//detailTitle = getGroup(groupPosition).toString()+" "+getChild(groupPosition,childPosition).toString();
		return detailTitle;
	}
	
	public List<String> getBookCount(int groupPosition, int childPosition){
		tempBookCount = new ArrayList<String>();	
/*		String childName = getChild(groupPosition,childPosition).toString().substring(0,getChild(groupPosition,childPosition).toString().indexOf("�@"));
		Cursor cursorDBData = tdb.select("category LIKE '%"+ childName +"%'");
		cursorDBData.moveToFirst();
		for(int i=0;i<cursorDBData.getCount();i++){
			tempBookCount.add(cursorDBData.getString(1));
			cursorDBData.moveToNext();
		}*/
		return tempBookCount;
	}	
  
	public List<String> getTempAllCount(){
		return tempAllCount;
	}
	
	public String getTempAllCount(int position){
		String temp;
		temp = tempAllCount.get(position).toString();
		return temp;
	}

/*	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		
            TextView textView = new TextView(mContext);
            String name = children[groupPosition][childPosition];
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 40);
            textView.setLayoutParams(lp);
            textView.setTextSize(18);
            textView.setPadding(45, 0, 0, 0);
            textView.setText(name);
    		return textView;            
            
           // textView.setText(getChild(groupPosition, childPosition).toString());
            //return textView;
	}*/

/*	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TextView textView = new TextView(mContext);
        String name = groups[groupPosition];
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 40);
        textView.setLayoutParams(lp);
        textView.setTextSize(18);
        textView.setPadding(45, 0, 0, 0);
        textView.setText(name);
		return textView;  
	}*/
	
	public class ViewHolder {
		TextView text;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}	
}