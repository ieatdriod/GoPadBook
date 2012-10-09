package com.taiwanmobile.myBook_PAD;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import tw.com.mebook.util.ImageDownloader;
import tw.com.soyong.utility.Util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 建構本地書櫃 全部列表
 * @author III
 * 
 */
public class BookListAllDetail extends BaseAdapter{
	protected LayoutInflater mInflater;
	protected List<String> ebook_title;
	protected List<String> ebook_type;
	protected List<String> ebook_category;
	protected List<String> ebook_deliveryid;
	protected List<String> ebook_isdownloadbook;
	protected List<String> ebook_trial;
	protected List<String> ebook_cover;
	protected List<String> ebook_cover_url;
	
	protected ImageDownloader iDownloader = null; 
	
	protected int width = 300;
	
	protected TextView tv;	
	protected List<String> tempTypeCount;
	protected List<String> tempCategoryCount;
	protected List<String> tempBookCount;
	protected List<String> tempBookStatus;
	
	protected List<String> tempAllCount;
	protected List<String> tempAllCount_2;
	protected List<Integer> tempAllTag;
	protected List<Integer> tempAllTag_2;
	
	protected List<String> tempTrialTag;
	protected List<String> tempTypeTag;
	protected List<String> tempCoverTag;
	
	protected int typeCount = 0;
	protected int categoryCount = 0;
	protected int bookCount = 0;
	protected Drawable da;
	protected TWMDB tdb;
	protected Context context;
	protected ViewHolder[] mainRow = null;	
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
	
/*	public BookListAllDetail(Context context,Cursor c, String detailTitle, List<String> book){
		mInflater = LayoutInflater.from(context);
		tempAllCount = new ArrayList<String>();
		tempAllTag = new ArrayList<Integer>();
		tempTrialTag = new ArrayList<String>();
		tempTypeTag = new ArrayList<String>();
		tempCoverTag = new ArrayList<String>();
		tempAllCount.add(detailTitle);
		tempAllTag.add(2);	
		
		for(int i=0;i < book.size();i++){
			tempAllCount.add(book.get(i));
			tempAllTag.add(3);
		}		
	}	*/
	
	/**
	 * 建構全部列表第二層
	 * @param con Context
	 * @param c 所有已經下載的書本資料
	 * @param db 資料庫
	 * @param tempAllC_2 主類別
	 * @param tempAllT_2 標記主類別
	 * @param position 第幾個類別
	 */	
	public BookListAllDetail(Context con,Cursor c, TWMDB db,List<String> tempAllC_2,List<Integer> tempAllT_2,int position){
		context = con;
		mInflater = LayoutInflater.from(context);
		ebook_title = new ArrayList<String>();   	
		ebook_type = new ArrayList<String>();   	
		ebook_category = new ArrayList<String>();   
		ebook_deliveryid = new ArrayList<String>();   
		ebook_isdownloadbook = new ArrayList<String>();  
		ebook_trial = new ArrayList<String>();  
		ebook_cover = new ArrayList<String>();   
		ebook_cover_url = new ArrayList<String>();
		tdb = db;
		c.moveToFirst();
		for(int i=0;i<c.getCount();i++){
			ebook_title.add(c.getString(1));
			ebook_type.add(c.getString(2));
			ebook_category.add(c.getString(3));
			ebook_deliveryid.add(c.getString(5));
			ebook_isdownloadbook.add(c.getString(8));	
			ebook_trial.add(c.getString(13));
			ebook_cover.add(c.getString(9));
			ebook_cover_url.add(c.getString(4));
			c.moveToNext(); 
		}	
		tempAllCount_2 = tempAllC_2;
		tempAllTag_2 = tempAllT_2;
		
		iDownloader = new ImageDownloader();
		iDownloader.setMode(ImageDownloader.Mode.CORRECT);
		iDownloader.setDb(tdb);
		iDownloader.setDlPath(Util.getStorePath(con));
		
		loadBradData();
		
		typeCount = getTypeCount().size();		
		for(int i=0;i<tempTypeCount.size();i++){
			categoryCount = categoryCount + getCategoryCount(tempTypeCount.get(i).toString()).size();
		}		
		
		setAll_2(position);
		mainRow = new ViewHolder[tempAllTag.size()];
	}
	
	private static final String BRAND_XML = "/brand.xml";
	
    private final String TAG_AP_NAME = "AP_NAME";
    private final String TAG_AP_PIC1 = "AP_PIC1";
    private final String TAG_AP_URL = "AP_URL";
    private final String TAG_AP_FAIL_URL = "AP_FAIL_URL";	
	
    private List<String> mBrandNames = null;
    private List<String> mBrandPIC1s = null;
    private List<String> mBrandURLs = null;
    private List<String> mBrandFailURLs = null;
    
    private void loadBradData(){
    	
    	mBrandNames = new ArrayList<String>();
    	mBrandPIC1s = new ArrayList<String>();
    	mBrandURLs = new ArrayList<String>();
    	mBrandFailURLs = new ArrayList<String>();
    	
		String fileDir = context.getFilesDir().toString()+BRAND_XML;	

		try {
			InputStream is = new FileInputStream(fileDir);
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc = db.parse(is);
			
			NodeList apNames = doc.getElementsByTagName(TAG_AP_NAME);
			NodeList apPic1s = doc.getElementsByTagName(TAG_AP_PIC1);
			NodeList apUrls = doc.getElementsByTagName(TAG_AP_URL);
			NodeList apFailUrls = doc.getElementsByTagName(TAG_AP_FAIL_URL);
			
			final int count = apNames.getLength();
			for ( int i =0 ; i < count ; i++){
				mBrandNames.add(apNames.item(i).getChildNodes().item(0).getNodeValue().toString());
				mBrandPIC1s.add(apPic1s.item(i).getChildNodes().item(0).getNodeValue().toString());
				mBrandURLs.add(apUrls.item(i).getChildNodes().item(0).getNodeValue().toString());
				mBrandFailURLs.add(apFailUrls.item(i).getChildNodes().item(0).getNodeValue().toString());
			}
			
			apNames = null;
			apPic1s = null;
			apUrls = null;
			apFailUrls = null;
			
			
//			NodeList apdes = doc.getElementsByTagName("DESCRIPTION");
//			len = apdes.getLength();
//			String des;
//			for ( int i = 0 ; i < len; i++){
//				des =  apdes.item(i).getChildNodes().item(0).getNodeValue().toString();
//				Log.e(TAG , apdes.item(i).getChildNodes().item(0).getNodeValue().toString());
//			}					
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }	
	
	
	
	/**
	 * 建構全部列表第一層
	 * @param con Context
	 * @param c 所有已經下載的書本資料
	 * @param db 資料庫
	 */	
	public BookListAllDetail(Context con,Cursor c, TWMDB db){
		context = con;
		mInflater = LayoutInflater.from(context);
		ebook_title = new ArrayList<String>();   	
		ebook_type = new ArrayList<String>();   	
		ebook_category = new ArrayList<String>();   
		ebook_deliveryid = new ArrayList<String>(); 
		ebook_isdownloadbook = new ArrayList<String>();  
		ebook_trial = new ArrayList<String>();  
		ebook_cover = new ArrayList<String>();   
		ebook_cover_url = new ArrayList<String>();
		tdb = db;	
		c.moveToFirst();
		Log.v("c.getCount()", String.valueOf(c.getCount()));
		for(int i=0;i<c.getCount();i++){
			ebook_title.add(c.getString(1));
			ebook_type.add(c.getString(2));
			ebook_cover.add(c.getString(9));
			ebook_cover_url.add(c.getString(4));
			ebook_category.add(c.getString(3));
			ebook_deliveryid.add(c.getString(5));
			ebook_isdownloadbook.add(c.getString(8));	
			ebook_trial.add(c.getString(13));
			c.moveToNext(); 
		}
		
		loadBradData();
		
		typeCount = getTypeCount().size();
		System.out.println("typeCount "+ typeCount) ;
		System.out.println("tempTypeCount.size() "+ tempTypeCount.size());
		for(int i=0;i<tempTypeCount.size();i++){			
			categoryCount = categoryCount + getCategoryCount(tempTypeCount.get(i).toString()).size();
		}

		setAll();
		mainRow = new ViewHolder[tempAllTag.size()];
		//bookCount = getBookCount(tempTypeCount.get(0).toString(),tempCategoryCount.get(0).toString()).size();
	}
	/**
	 * 建構全部列表第一層
	 * 主階層 與 次階層 資料建構
	 */	
	public void setAll(){
		
		tempAllCount = new ArrayList<String>();
		tempAllTag = new ArrayList<Integer>();
		tempTrialTag = new ArrayList<String>();
		tempTypeTag = new ArrayList<String>();
		tempCoverTag = new ArrayList<String>();
		for(int i=0;i<tempTypeCount.size();i++){
			String tmpType = tempTypeCount.get(i);
			tempAllCount.add(tempTypeCount.get(i));
			tempAllTag.add(0);
			//getCategoryCount(tempTypeCount.get(i).toString());
			for(int j=0;j < getCategoryCount(tempTypeCount.get(i).toString()).size();j++){
				
				if (tempTypeCount.get(i).toString().equals("品牌專區") ){
					tempAllCount.add(tempCategoryCount.get(j));
					tempAllTag.add(5);	
				}else{
					tempAllCount.add(tempCategoryCount.get(j)+" ( "+getBookCount(tempTypeCount.get(i).toString(),tempCategoryCount.get(j).toString()).size()+" ) ");
					tempAllTag.add(1);
				}
				//if(i==0)showAlertMessage(tempCategoryCount.get(j).toString()+" ( "+tempTypeCount.get(i).toString()+"||"+tempCategoryCount.get(j).toString()+" ) ");
			}
		}
	}
	/**
	 * 建構全部列表第二層
	 * 主階層 與 次階層 資料建構
	 * @param position 第一層-次階層位置
	 */	
	public void setAll_2(int position){
		tempAllCount = new ArrayList<String>();
		tempAllTag = new ArrayList<Integer>();
		tempTrialTag = new ArrayList<String>();
		tempTypeTag = new ArrayList<String>();
		tempCoverTag = new ArrayList<String>();
		int potition_2 = 0;
		String signTag = tempAllCount_2.get(position).toString().substring(0 ,tempAllCount_2.get(position).toString().indexOf(" ( "));
		for(int i = position;i > 0;i--){
			if (tempAllTag_2.get(i) == 0 ){
				potition_2 = i;
				break;
			}
		}
		tempAllCount.add(tempAllCount_2.get(potition_2)+"-----"+tempAllCount_2.get(position));
		tempAllTag.add(2);
		tempTrialTag.add("");
		tempTypeTag.add("");
		tempCoverTag.add("");
		List<String> temp = getBookCount(tempAllCount_2.get(potition_2).toString(),signTag);
		
		for(int i=0;i < temp.size();i++){
			tempAllCount.add(temp.get(i));
			//if(tempBookStatus.get(i).equals("1")){
				tempAllTag.add(3);
			//}else{
				//tempAllTag.add(4);
			//}
			
		}
		
	}	
	/**
	 * 取得最後點選的書本deliveryid
	 * @param position 位置
	 * @return deliveryid deliveryid
	 */	
	public String getListItemClickBookDeliveryID(int position){
		String deliveryid = "";
		String aaa = tempAllCount.get(position).toString();
		
		for(int i=0;i<ebook_title.size();i++){
			if(ebook_title.get(i).toString().equals(aaa))
				deliveryid = ebook_deliveryid.get(i).toString();
		}		
		return deliveryid;
	}
	/**
	 * 取得主類別
	 * @return tempTypeCount 主類別
	 */
	public List<String> getTypeCount(){
		
		if ( mBrandNames.size() > 0 ){
			typeCount++;	// brand area
		}
		
		tempTypeCount = new ArrayList<String>();
		if (mBrandNames.size() > 0 ){
			tempTypeCount.add("品牌專區");
		}
				
		String[] type = {"type"};
		Cursor cursorDBData = tdb.select(type,"isdownloadbook = '1'");	
		System.out.println("getTypeCount "+cursorDBData.getCount());
		cursorDBData.moveToFirst();
		System.out.println(" cursorDBData.getCount() "+ cursorDBData.getCount()) ;
		for(int i=0;i<cursorDBData.getCount();i++){
			tempTypeCount.add(cursorDBData.getString(0));
			cursorDBData.moveToNext();
		}		
		return tempTypeCount;
	}
	
	/**
	 * 取得次類別
	 * @param type 主類別
	 * @return tempTypeCount 次類別
	 */
	public List<String> getCategoryCount(String type){
		tempCategoryCount = new ArrayList<String>();
		
		//int offset = 0 ;
		if (type.equals("品牌專區")) {
			for (int i = 0; i < mBrandNames.size(); i++) {
				tempCategoryCount.add(mBrandNames.get(i));
			}
			//offset = tempCategoryCount.size();
			return tempCategoryCount;
		}

	
		String[] category = {"category"};
		Cursor cursorDBData = tdb.select(category,"type = '"+type+"' AND isdownloadbook = '1'");	
		cursorDBData.moveToFirst();
		
		System.out.println("category  getTypeCount "+cursorDBData.getCount());
		
		
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
		
		for( int i=0;i<test.size();i++){
			for(int j=i+1;j<test.size();j++){
				if(test.get(i).toString().equals(test.get(j).toString())){
					test.remove(j);
				}
			}
		}		
		tempCategoryCount = test;		
		return tempCategoryCount;
	}	
	/**
	 * 取得最底層書單
	 * @param tempType 主類別
	 * @param tempCategory 次類別
	 * @return tempTypeCount 最底層書單
	 */
	public List<String> getBookCount(String tempType,String tempCategory){
		tempBookCount = new ArrayList<String>();		
		//tempBookStatus = new ArrayList<String>();	
		//Cursor cursorDBData = tdb.select("type = '"+tempType+"'"+"AND category = '"+tempCategory+"'");
		//Cursor cursorDBData = tdb.select("type = '"+tempType+"'"+" and category like '%"+tempCategory+"%'");
		Cursor cursorDBData = tdb.select("type = '"+tempType+"'"+" and category LIKE '%"+tempCategory+"%' AND isdownloadbook = '1'");
		//showAlertMessage("type = '"+tempType+"'"+" and category like '%"+tempCategory+"%'");
		cursorDBData.moveToFirst();
		for(int i=0;i<cursorDBData.getCount();i++){
			tempBookCount.add(cursorDBData.getString(1));
			//tempBookStatus.add(cursorDBData.getString(8));
			tempTrialTag.add(cursorDBData.getString(13));
			tempTypeTag.add(cursorDBData.getString(2));
			tempCoverTag.add(cursorDBData.getString(9));			
			cursorDBData.moveToNext();
		}
		return tempBookCount;
	}	
	/**
	 * 取得資料筆數
	 * @return 資料筆數
	 */
	@Override
	public int getCount(){
		
		int brands = 0 ; // mBrandNames.size();
		int books = tempAllCount.size();
		
		return brands+books;
	}
	/**
	 * 取得資料
	 * @param position 位置
	 * @return 資料
	 */
	@Override
	public Object getItem(int position){
		return tempAllCount.get(position);
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
	 * 取得列表資料
	 * @return 列表資料
	 */
	public List<String> getTempAllCount(){
		return tempAllCount;
	}
	/**
	 * 取得列表資料標記--用來判斷層級
	 * @return 列表資料標記
	 */
	public List<Integer> getTempAllTag(){
		return tempAllTag;
	}
	/**
	 * 取得列表資料
	 * @param position 位置
	 * @return 列表資料
	 */
	public String getTempAllCount(int position){
		String temp;
		temp = tempAllCount.get(position).toString();
		return temp;
	}
	/**
	 * 取得列表資料標記--用來判斷層級
	 * @param position 位置
	 * @return 列表資料標記
	 */
	public String getTempAllTag(int position){
		String temp;
		temp = tempAllTag.get(position).toString();
		return temp;
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
		//da = da.createFromPath("/sdcard/back.jpg");
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.iii_all_type_row, null);
			mainRow[position] = new ViewHolder();
			mainRow[position].text = (TextView) convertView.findViewById(R.id.all_text);
			convertView.setTag(mainRow[position]);		
		}else{
			mainRow[position] = (ViewHolder) convertView.getTag();
		}
		/*
		SharedPreferences settings = mInflater.getContext().getSharedPreferences("setting_Preference", 0);
		if(tempAllTag.get(position)==0){
			mainRow[position].text.setText(tempAllCount.get(position).toString());
			mainRow[position].text.setSingleLine(true);
			mainRow[position].text.setWidth(width);
			mainRow[position].text.setMarqueeRepeatLimit(6);	
			mainRow[position].text.setHeight(30);
			if("".equals(settings.getString("setting_bookcase_background_style_value", ""))){
				mainRow[position].text.setBackgroundResource(R.drawable.wood_ivi_pict01);	
			}else if("木紋".equals(settings.getString("setting_bookcase_background_style_value", ""))){
				mainRow[position].text.setBackgroundResource(R.drawable.wood_ivi_pict01);	
			}else if("科技".equals(settings.getString("setting_bookcase_background_style_value", ""))){
				mainRow[position].text.setBackgroundResource(R.drawable.technology_ivi_pict01);	
			}else if("浪漫".equals(settings.getString("setting_bookcase_background_style_value", ""))){
				mainRow[position].text.setBackgroundResource(R.drawable.romantic_ivi_pict01);	
			}			
			mainRow[position].text.setTextColor(Color.WHITE);
			mainRow[position].text.setTextSize(20);	
			mainRow[position].text.setEnabled(false);
		}else if(tempAllTag.get(position)==1){
			mainRow[position].text.setText(tempAllCount.get(position).toString());
			mainRow[position].text.setSingleLine(true);
			mainRow[position].text.setWidth(width);
			mainRow[position].text.setHeight(44);
			mainRow[position].text.setBackgroundColor(Color.alpha(150));
			mainRow[position].text.setTextColor(Color.rgb(50, 50, 50));
			mainRow[position].text.setMarqueeRepeatLimit(6);	
			mainRow[position].text.setTextSize(20);	
			mainRow[position].text.setEnabled(true);
		}else if(tempAllTag.get(position)==2){
			mainRow[position].text.setText(tempAllCount.get(position).toString());
			mainRow[position].text.setSingleLine(true);
			mainRow[position].text.setWidth(width);
			mainRow[position].text.setMarqueeRepeatLimit(6);	
			mainRow[position].text.setHeight(30);
			if("".equals(settings.getString("setting_bookcase_background_style_value", ""))){
				mainRow[position].text.setBackgroundResource(R.drawable.wood_ivi_pict01);	
			}else if("木紋".equals(settings.getString("setting_bookcase_background_style_value", ""))){
				mainRow[position].text.setBackgroundResource(R.drawable.wood_ivi_pict01);	
			}else if("科技".equals(settings.getString("setting_bookcase_background_style_value", ""))){
				mainRow[position].text.setBackgroundResource(R.drawable.technology_ivi_pict01);	
			}else if("浪漫".equals(settings.getString("setting_bookcase_background_style_value", ""))){
				mainRow[position].text.setBackgroundResource(R.drawable.romantic_ivi_pict01);	
			}			
			mainRow[position].text.setTextColor(Color.WHITE);
			mainRow[position].text.setTextSize(20);	
			mainRow[position].text.setEnabled(false);
		}else if(tempAllTag.get(position)==3){
			mainRow[position].text.setText(tempAllCount.get(position).toString());
			mainRow[position].text.setSingleLine(true);
			mainRow[position].text.setWidth(width);
			mainRow[position].text.setHeight(44);
			mainRow[position].text.setBackgroundColor(Color.alpha(150));
			mainRow[position].text.setTextColor(Color.rgb(50, 50, 50));
			mainRow[position].text.setMarqueeRepeatLimit(6);	
			mainRow[position].text.setTextSize(20);	
			mainRow[position].text.setEnabled(true);
		}
		 */
		return convertView;
	}
	/**
	 * 書單結構
	 * @author III
	 * 
	 */
	public class ViewHolder {
		//CheckBox cb;
		public TextView text;
		public ImageView icon;
		public ImageView iconTag;
		public RelativeLayout rl;
		//ImageButton cancel;
		public String nowStatus;
	}	
}