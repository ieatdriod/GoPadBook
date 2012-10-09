package com.taiwanmobile.myBook_PAD;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 本地書櫃用的資料庫
 * @author III
 * 
 */
public class TWMDB extends SQLiteOpenHelper{
	private static final String DATABASE_NAME = "iii_TWM_ebook"; 
	
	private static final int DATABASE_VERSION = 3; 
	
	private static final String TABLE_NAME = "Books"; 
	
	private static final String FIELD_ID = "_id";				//0
	private static final String FIELD_TITLE = "title";			//1
	private static final String FIELD_TYPE = "type";			//2,大分類
	private static final String FIELD_CATEGORY = "category";	//3,小分類
	private static final String FIELD_COVER = "cover";			//4,圖片網址
	private static final String FIELD_DELIVERY_ID = "deliveryID";//5
	private static final String FIELD_ISREAD = "isRead";		//6,已讀
	private static final String FIELD_LASTREAD_TIME = "lastReadTime";//7,最後讀取時間
	private static final String FIELD_IS_DOWNLOAD_BOOK = "isDownloadBook";//8,下載狀態 (0:no,1:已下載,2:下載中,3:下載但未完成)
	private static final String FIELD_COVER_PATH = "coverPath";	//9,本機圖片位置
	private static final String FIELD_BUYTIME = "buyTime";		//10,購買時間
	
	private static final String FIELD_PUBLISHER = "publisher";	//11
	private static final String FIELD_AUTHORS = "authors";		//12
	private static final String FIELD_TRAIL = "trial";			//13
	private static final String FIELD_DOWNLOAD_PERCENT = "downloadPercent";	//14,(non-use)
	
	private static final String FIELD_CONTENT_ID = "contendID";	//15
	private static final String FIELD_UPDATE_DATE = "updateDate";//16,(non-use)
	private static final String FIELD_VERTICAL = "vertical";	//17,epub初始顯示方向
	private static final String FIELD_TRIAL_DUE_DATE = "trialDueDate";//18,試閱結束時間
	private static final String FIELD_BOOK_PATH = "bookPath";	//19,書的路徑
	private static final String FIELD_BOOK_SIZE = "bookSize";	//20,書本的大小(bytes)
	private static final String FIELD_BOOK_TYPE = "bookType";	//21,epub/mebook
	private static final String FIELD_BOOK_OTHER_INFO = "bookOtherInfo";//22,僅用於 online book 

	private WeakReference<Activity> mCtx;
	private SQLiteDatabase mDb;
	public TWMDB(Activity context) {
		super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
		mCtx = new WeakReference<Activity>(context);
	}
	/**
	 * 建立資料庫
	 * @param db 資料庫
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String sql = "CREATE TABLE " + TABLE_NAME 
				+" ("+ FIELD_ID +" integer primary key autoincrement, " 
				+FIELD_TITLE +" text not null, "
				+FIELD_TYPE+" text not null, "
				+FIELD_CATEGORY+" text not null, " 
				+FIELD_COVER +" text not null, "
				+FIELD_DELIVERY_ID+" text not null, "
				+FIELD_ISREAD+" text not null, " 
				+FIELD_LASTREAD_TIME +" text not null, "
				+FIELD_IS_DOWNLOAD_BOOK+" text not null, " 
				+FIELD_COVER_PATH + " text not null, " 
				+FIELD_BUYTIME+ " text not null, "
				+FIELD_PUBLISHER+" text not null, "
				+FIELD_AUTHORS+" text not null, "
				+FIELD_TRAIL+" text not null, "
				+FIELD_DOWNLOAD_PERCENT+" text not null, "
				+FIELD_CONTENT_ID+" text not null, "
				+FIELD_UPDATE_DATE+" text not null, "
				+FIELD_VERTICAL+" text not null, "
				+FIELD_TRIAL_DUE_DATE+" text not null, "
				+FIELD_BOOK_PATH+" text not null, "
				+FIELD_BOOK_SIZE+" text not null, "
				+FIELD_BOOK_TYPE+" text not null, "
				+FIELD_BOOK_OTHER_INFO+" text not null)";
		
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
	private SQLiteDatabase openDB() {
		if ( null == mDb){
			mDb = this.getReadableDatabase();
		}
		return mDb;
	}
	/**
	 * 取得資料
	 * @return 回傳全部資料
	 */
	public Cursor select(){
	    SQLiteDatabase db = openDB();
//	    Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
	    
		String sql = "SELECT * FROM " + TABLE_NAME ;
		Cursor cursor = db.rawQuery( sql , null);
		
		cursor.moveToFirst();
		
		final Activity aActivity= mCtx.get();
		if (aActivity != null) 
		    aActivity.startManagingCursor(cursor);
	    return cursor;
	}
	/**
	 * 取得資料
	 * @param where 條件
	 * @return 回傳符合條件的全部資料
	 */
	public Cursor select(String where){
	    SQLiteDatabase db = openDB();
	    Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, null);
		final Activity aActivity= mCtx.get();
		if (aActivity != null) 
		    aActivity.startManagingCursor(cursor);
	    return cursor;
	}
	
	public Cursor select2(String did){
		SQLiteDatabase db = getReadableDatabase() ;
		String sql = "SELECT * FROM " + TABLE_NAME + " where deliveryID="+ "\""+did +"\"";
		Cursor cursor = db.rawQuery( sql , null);
		final Activity aActivity= mCtx.get();
		if (aActivity != null) 
		    aActivity.startManagingCursor(cursor);
		return cursor;
	}
	
	public Cursor select3(){
	    SQLiteDatabase db = openDB();
	    //SELECT * FROM books order by buyTime desc
		String sql = "SELECT * FROM " + TABLE_NAME + " order by buyTime desc" ;
		Cursor cursor = db.rawQuery( sql , null);
		
		cursor.moveToFirst();
		
		final Activity aActivity= mCtx.get();
		if (aActivity != null) 
		    aActivity.startManagingCursor(cursor);
	    return cursor;
	}	
	
	/**
	 * 取得資料
	 * @param columns 欄位
	 * @return 回傳符合欄位的全部資料
	 */
	public Cursor select(String[] columns){
	    SQLiteDatabase db = openDB();
	    Cursor cursor = db.query(true, TABLE_NAME, columns, null, null, null, null, null, null);
		final Activity aActivity= mCtx.get();
		if (aActivity != null) 
		    aActivity.startManagingCursor(cursor);
	    return cursor;
	}	
	/**
	 * 取得資料
	 * @param columns 欄位
	 * @param where 條件
	 * @return 回傳符合欄位與條件的全部資料
	 */
	public Cursor select(String[] columns, String where){
	    SQLiteDatabase db = openDB();
	    Cursor cursor = db.query(true, TABLE_NAME, columns, where, null, null, null, null, null);
		final Activity aActivity= mCtx.get();
		if (aActivity != null) 
		    aActivity.startManagingCursor(cursor);
	    return cursor;
	}	
	/**
	 * 取得資料
	 * @param orderBy 排序欄位
	 * @param where 條件
	 * @return 回傳符合條件與依照排序欄位的全部資料
	 */
	public Cursor selectOrderBy(String where, String orderBy){
	    SQLiteDatabase db = openDB();
	    Cursor cursor = db.query(TABLE_NAME, null, where, null, null, null, orderBy);
		final Activity aActivity= mCtx.get();
		if (aActivity != null) 
		    aActivity.startManagingCursor(cursor);
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
	    SQLiteDatabase db = this.getWritableDatabase();
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
	    long row = db.insert(TABLE_NAME, null, cv);
	    return row;
	}
	/**
	 * 刪除資料
	 * @param id 
	 */
	public void delete(int id){
	    SQLiteDatabase db = openDB();
	    String where = FIELD_ID + " = ?";
	    String[] whereValue =
	    { Integer.toString(id) };
	    db.delete(TABLE_NAME, where, whereValue);
	}
	
	/**
	 * 刪除資料
	 * @param id 
	 */
	public void deleteByDid(String did){
	    SQLiteDatabase db = openDB();
	    String where = FIELD_DELIVERY_ID + " = ?";
	    String[] whereValue = { did };
	    
	    db.delete(TABLE_NAME, where, whereValue);
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
	    SQLiteDatabase db = openDB();
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
	    db.update(TABLE_NAME, cv, where, whereValue);
	}	
	/**
	 * 更新某欄位資料
	 * @param id id
	 * @param field 
	 * @param data 
	 */	
	public void update(int id, String field,String data){
	    SQLiteDatabase db = openDB();
	    String where = FIELD_ID + " = ?";
	    String[] whereValue = { Integer.toString(id) };
	    ContentValues cv = new ContentValues();
	    cv.put(field, data);
	    db.update(TABLE_NAME, cv, where, whereValue);
	}	
	/**
	 * 更新某欄位資料
	 * @param id DeliveryId
	 * @param field 
	 * @param data 
	 */	
	public void updateByDeliveryId(String id, String field,String data){
	    SQLiteDatabase db = openDB();
	    String where = FIELD_DELIVERY_ID + " = ?";
	    String[] whereValue = { id };
	    ContentValues cv = new ContentValues();
	    cv.put(field, data);
	    db.update(TABLE_NAME, cv, where, whereValue);
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
}