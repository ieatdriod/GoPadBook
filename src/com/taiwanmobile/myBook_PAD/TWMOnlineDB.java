package com.taiwanmobile.myBook_PAD;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 線上書櫃用的資料庫
 * @author III
 * 
 */
public class TWMOnlineDB extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "iii_TWM_Online"; 
	
	private static final int DATABASE_VERSION = 1; 
	
	private static final String TABLE_NAME = "Books"; 
	
	private static final String FIELD_ID = "_id";//0
	private static final String FIELD_TITLE = "title";	//1
	private static final String FIELD_TYPE = "type";//2
	private static final String FIELD_CATEGORY = "category";	//3
	private static final String FIELD_COVER = "cover";//4
	private static final String FIELD_DELIVERY_ID = "deliveryID";	//5
	private static final String FIELD_ISREAD = "isRead";//6
	private static final String FIELD_LASTREAD_TIME = "lastReadTime";//7
	private static final String FIELD_IS_DOWNLOAD_BOOK = "isDownloadBook";//8
	private static final String FIELD_COVER_PATH = "coverPath";//9
	private static final String FIELD_BUYTIME = "buyTime";//10
	
	private static final String FIELD_PUBLISHER = "publisher";//11
	private static final String FIELD_AUTHORS = "authors";	//12
	private static final String FIELD_TRAIL = "trial";//13
	private static final String FIELD_DOWNLOAD_PERCENT = "downloadPercent";//14
	
	private static final String FIELD_CONTENT_ID = "contendID";//15
	private static final String FIELD_UPDATE_DATE = "updateDate";//16
	private static final String FIELD_VERTICAL = "vertical";//17
	private static final String FIELD_TRIAL_DUE_DATE = "trialDueDate";//18
	private static final String FIELD_BOOK_PATH = "bookPath";//19
	private static final String FIELD_BOOK_SIZE = "bookSize";//20
	private static final String FIELD_BOOK_TYPE = "bookType";//21
	private static final String FIELD_BOOK_OTHER_INFO = "bookOtherInfo";//22
	
	private Activity mCtx;
	private SQLiteDatabase mDb;
	public TWMOnlineDB(Activity context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mCtx = context;
	}
	/**
	 * 建立資料庫
	 * @param db 資料庫
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String sql = "CREATE TABLE " + TABLE_NAME +" ("+ FIELD_ID +" integer primary key autoincrement, " +
				FIELD_TITLE +" text not null, "+FIELD_TYPE+" text not null, "+FIELD_CATEGORY+" text not null, " +
				FIELD_COVER +" text not null, "+FIELD_DELIVERY_ID+" text not null, "+FIELD_ISREAD+" text not null, " +
				FIELD_LASTREAD_TIME +" text not null, "+FIELD_IS_DOWNLOAD_BOOK+" text not null, " + FIELD_COVER_PATH + " text not null, " +
				FIELD_BUYTIME+ " text not null, "+FIELD_PUBLISHER+" text not null, "+FIELD_AUTHORS+" text not null, "+
				FIELD_TRAIL+" text not null, "+FIELD_DOWNLOAD_PERCENT+" text not null, "+FIELD_CONTENT_ID+" text not null, "+
				FIELD_UPDATE_DATE+" text not null, "+FIELD_VERTICAL+" text not null, "+FIELD_TRIAL_DUE_DATE+" text not null, "+
				FIELD_BOOK_PATH+" text not null, "+FIELD_BOOK_SIZE+" text not null, "+FIELD_BOOK_TYPE+" text not null, "+
				FIELD_BOOK_OTHER_INFO+" text not null)";
		
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME); 
		onCreate(db); 		
	}
	/**
	 * 開啓資料庫
	 */
	public void openDB() {
		if ( null == mDb){
			mDb = this.getWritableDatabase();
		}
	}
	/**
	 * 取得資料
	 * @return 回傳全部資料
	 */
	public Cursor select(){
	    //SQLiteDatabase db = openDB();
	    Cursor cursor = mDb.query(TABLE_NAME, null, null, null, null, null, null);
	    mCtx.startManagingCursor(cursor);
	    return cursor;
	}
	/**
	 * 取得資料
	 * @param where 條件
	 * @return 回傳符合條件的全部資料
	 */
	public Cursor select(String where){
	    //SQLiteDatabase db = openDB();
	    Cursor cursor = mDb.query(TABLE_NAME, null, where, null, null, null, null);
	    mCtx.startManagingCursor(cursor);
	    return cursor;
	}
	/**
	 * 取得資料
	 * @param columns 欄位
	 * @return 回傳符合欄位的全部資料
	 */
	public Cursor select(String[] columns){
	    //SQLiteDatabase db = openDB();
	    Cursor cursor = mDb.query(true, TABLE_NAME, columns, null, null, null, null, null, null);
	    mCtx.startManagingCursor(cursor);
	    return cursor;
	}	
	/**
	 * 取得資料
	 * @param columns 欄位
	 * @param where 條件
	 * @return 回傳符合欄位與條件的全部資料
	 */
	public Cursor select(String[] columns, String where){
		//SQLiteDatabase db = openDB();
	    Cursor cursor = mDb.query(true, TABLE_NAME, columns, where, null, null, null, null, null);
	    mCtx.startManagingCursor(cursor);
	    return cursor;
	}	
	/**
	 * 取得資料
	 * @param orderBy 排序欄位
	 * @return 回傳依照排序欄位的全部資料
	 */
	public Cursor selectOrderBy(String orderBy){
		//SQLiteDatabase db = openDB();
	    Cursor cursor = mDb.query(TABLE_NAME, null, null, null, null, null, orderBy);
	    mCtx.startManagingCursor(cursor);
	    return cursor;
	}
	/**
	 * 取得資料
	 * @param orderBy 排序欄位
	 * @param where 條件
	 * @return 回傳符合條件與依照排序欄位的全部資料
	 */
	public Cursor selectOrderBy(String where, String orderBy){
	    //SQLiteDatabase db = openDB();
	    Cursor cursor = mDb.query(TABLE_NAME, null, where, null, null, null, orderBy);
	    mCtx.startManagingCursor(cursor);
	    return cursor;
	}			
	/**
	 * 插入資料
	 */
	public long insert(String title,String type,String category,
						String cover,String deliveryid,String isread,
						String lasttime,String isdownloadbook,String coverpath,
						String buytime,String publisher,String authors,
						String trial,String downloadPercent,String contendID,
						String updateDate,String vertical,String trialDueDate,
						String bookPath,String bookSize,String bookType,
						String bookOtherInfo){
	    //SQLiteDatabase db = this.getWritableDatabase();
	    ContentValues cv = new ContentValues();
	    cv.put(FIELD_TITLE, title);
	    cv.put(FIELD_TYPE, type);
	    cv.put(FIELD_CATEGORY, category);
	    cv.put(FIELD_COVER, cover);
	    cv.put(FIELD_DELIVERY_ID, deliveryid);
	    cv.put(FIELD_ISREAD, isread);
	    cv.put(FIELD_LASTREAD_TIME, lasttime);
	    cv.put(FIELD_IS_DOWNLOAD_BOOK, isdownloadbook);
	    cv.put(FIELD_COVER_PATH, coverpath);
	    cv.put(FIELD_BUYTIME, buytime);
	    cv.put(FIELD_PUBLISHER, publisher);
	    cv.put(FIELD_AUTHORS, authors);
	    cv.put(FIELD_TRAIL, trial);
	    cv.put(FIELD_DOWNLOAD_PERCENT, downloadPercent);
	    cv.put(FIELD_CONTENT_ID, contendID);
	    cv.put(FIELD_UPDATE_DATE, updateDate);
	    cv.put(FIELD_VERTICAL, vertical);
	    cv.put(FIELD_TRIAL_DUE_DATE, trialDueDate);
	    cv.put(FIELD_BOOK_PATH, bookPath);
	    cv.put(FIELD_BOOK_SIZE, bookSize);
	    cv.put(FIELD_BOOK_TYPE, bookType);
	    cv.put(FIELD_BOOK_OTHER_INFO, bookOtherInfo);
	    long row = mDb.insert(TABLE_NAME, null, cv);
	    return row;
	}
	/**
	 * 刪除資料
	 * @param id 
	 */
	public void delete(int id){
		//SQLiteDatabase db = openDB();
	    String where = FIELD_ID + " = ?";
	    String[] whereValue =
	    { Integer.toString(id) };
	    mDb.delete(TABLE_NAME, where, whereValue);
	}
	/**
	 * 更新資料
	 */
	public void update(int id, String title,String type,String category,
								String cover,String deliveryid,String isread,
								String lasttime,String isdownloadbook,String coverpath,
								String buytime,String publisher,String authors,
								String trial,String downloadPercent,String contendID,
								String updateDate,String vertical,String trialDueDate,
								String bookPath,String bookSize,String bookType,
								String bookOtherInfo){
	    //SQLiteDatabase db = openDB();
	    String where = FIELD_ID + " = ?";
	    String[] whereValue = { Integer.toString(id) };
	    ContentValues cv = new ContentValues();
	    cv.put(FIELD_TITLE, title);
	    cv.put(FIELD_TYPE, type);
	    cv.put(FIELD_CATEGORY, category);
	    cv.put(FIELD_COVER, cover);
	    cv.put(FIELD_DELIVERY_ID, deliveryid);
	    cv.put(FIELD_ISREAD, isread);
	    cv.put(FIELD_LASTREAD_TIME, lasttime);
	    cv.put(FIELD_IS_DOWNLOAD_BOOK, isdownloadbook);
	    cv.put(FIELD_COVER_PATH, coverpath);	 
	    cv.put(FIELD_BUYTIME, buytime);
	    cv.put(FIELD_PUBLISHER, publisher);
	    cv.put(FIELD_AUTHORS, authors);
	    cv.put(FIELD_TRAIL, trial);
	    cv.put(FIELD_DOWNLOAD_PERCENT, downloadPercent);	   
	    cv.put(FIELD_CONTENT_ID, contendID);
	    cv.put(FIELD_UPDATE_DATE, updateDate);
	    cv.put(FIELD_VERTICAL, vertical);
	    cv.put(FIELD_TRIAL_DUE_DATE, trialDueDate);
	    cv.put(FIELD_BOOK_PATH, bookPath);
	    cv.put(FIELD_BOOK_SIZE, bookSize);
	    cv.put(FIELD_BOOK_TYPE, bookType);
	    cv.put(FIELD_BOOK_OTHER_INFO, bookOtherInfo);	    
	    mDb.update(TABLE_NAME, cv, where, whereValue);
	}	
	
	/**
	 * 更新某欄位資料
	 * @param id id
	 * @param field 
	 * @param data 
	 */	
	public void update(int id, String field,String data){
	    //SQLiteDatabase db = openDB();
	    String where = FIELD_ID + " = ?";
	    String[] whereValue = { Integer.toString(id) };
	    ContentValues cv = new ContentValues();
	    cv.put(field, data);
	    mDb.update(TABLE_NAME, cv, where, whereValue);
	}		
	
	/**
	 * 更新某欄位資料
	 * @param id DeliveryId
	 * @param field 
	 * @param data 
	 */	
	public void updateByDeliveryId(String id, String field,String data){
	    //SQLiteDatabase db = openDB();
	    String where = FIELD_DELIVERY_ID + " = ?";
	    String[] whereValue = { id };
	    ContentValues cv = new ContentValues();
	    cv.put(field, data);
	    mDb.update(TABLE_NAME, cv, where, whereValue);
	}	
	/**
	 * 關閉資料庫
	 */	
	public void close() {
		if ( null != mDb){
			mDb.close();
			mDb = null;
		}
	}
	/**
	 * 刪除資料庫資料
	 */	
	public void deleteAll(){
		//SQLiteDatabase db = openDB();
		mDb.delete(TABLE_NAME,null,null);
		//db.close();
	}
}