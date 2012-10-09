package org.iii.ideas.reader.bookmark;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 書籤DB處理class，註:epubpath欄位在twm專案裡存的是did
 * @author III
 * 
 */
public class Bookmarks {
	//private static final int bmPerBook=100;
	private static final int DB_VERSION = 13;
	private static final String DB_NAME = "bookmarks.db";
	private static final String TABLE_NAME = "bookmarks";
	private static final String CREATE_DB = "create table "+TABLE_NAME+"( _id INTEGER PRIMARY KEY,"
	+"bookName TEXT, chapterName TEXT, description TEXT, bookType INTEGER, createDate TEXT, position1 INTEGER, position2 INTEGER, epubPath TEXT);";
	private BookmarksDBHelper dbh;
	private SQLiteDatabase db;
	private Context ctx;
	
	/**
	 * constructor
	 * @param context context
	 * @throws SQLException db開啟失敗
	 */
	public Bookmarks(Context context) throws SQLException{
		ctx=context;
		//Log.d("getBMDB","get");
		dbh=new BookmarksDBHelper(context);
		db=dbh.getWritableDatabase();
	}
	
	/**
	 * 根據傳入array刪除書籤db中相關紀錄
	 * @param list bookmark list
	 * @return 刪除筆數
	 */
	public int deleteBookmarks(ArrayList<Bookmark> list){
		int delCount=0;//,idx;
		for(int i=0;i<list.size();i++){
			Bookmark bm = list.get(i);
			try{
				db.delete(TABLE_NAME,"_id=?",new String[]{""+bm.id});
				delCount++;
			}catch(Exception e){
				Log.e("Bookmarks:deleteBookmark",e.toString());
			}
		}
		return delCount;
	}
	
	/**
	 * 根據id刪除書籤
	 * @param id id
	 * @return 刪除筆數
	 */
	public int deleteBookmarkById(int id){
		int delCount=0;//,idx;
		delCount=db.delete(TABLE_NAME,"_id=?",new String[]{""+id});
		return delCount;
	}
	

	/**
	 * 新增書籤
	 * @param bm 書籤
	 */
	public void insertBookmark(Bookmark bm){
		try{
			//if(getCount()<100){
				//Log.d("position1","is:"+bm.position1);
				//Log.d("position2","is:"+bm.position2);
				//Log.d("Bookmark:insertBookmark","in");
				ContentValues values = new ContentValues();
				values.put("chapterName", bm.chapterName);
				values.put("description", bm.description);
				values.put("bookName", bm.bookName);
				values.put("position1", bm.position1);
				values.put("position2", bm.position2);
				values.put("createDate", ""+System.currentTimeMillis());
				values.put("bookType", bm.bookType);
				values.put("epubPath", bm.epubPath);
				db.insert(TABLE_NAME,null,values);
			//}
		}catch(Exception e){
			Log.e("Bookmarks:insertBookmark",e.toString());
		}
	}
	
	/**
	 * 刪除某本書相關的書籤
	 * @param path epub path
	 */
	public void deleteBookmarksByEpubPath(String path){
		db.delete(TABLE_NAME,"epubPath=?",new String[]{path});
	}
	
	/**
	 * 刪除所有書籤，清空db
	 */
	public void deleteAllBookmark(){
		db.delete(TABLE_NAME,null,null);
	}
	
	
	/**
	 * 取得某本書書籤筆數
	 * @param path epub path 
	 * @return 書籤筆數
	 */
	public int getBookmarkCountByEpubPath(String path){
		try{
			Cursor bms = db.rawQuery("select * from "+TABLE_NAME+" where epubPath=?",new String[]{path} );
			int count  = bms.getCount();
			bms.close();
			return count;
		}catch(Exception e){
			Log.e("Bookmarks:getBookmarkCountByEpubPath",e.toString());
			return 0;
		}
	}
	
	/**
	 * 取得某本書書籤列表
	 * @param path epub path
	 * @return 書籤列表
	 */
	public ArrayList<Bookmark> getBookmarksByEpubPath(String path){
		ArrayList<Bookmark> bml = new ArrayList<Bookmark>();
		Cursor bms = db.rawQuery("select * from "+TABLE_NAME+" where epubPath=?",new String[]{path} );
		for(int j=0;j<bms.getCount();j++){
			bms.moveToPosition(j);
			Bookmark bm = new Bookmark();
			bm.id = bms.getInt(0);
			bm.bookName =bms.getString(1);
			bm.chapterName =bms.getString(2);
			bm.description =bms.getString(3);
			bm.bookType = bms.getInt(4);
			bm.createDate = bms.getString(5);
			bm.position1 = bms.getInt(6);
			bm.position2 = bms.getInt(7);
			bm.epubPath = bms.getString(8);
			
			bml.add(bm);
		}
		bms.close();
		return bml;
	}
	
	/**
	 * 取得所有書籤
	 * @return 書籤列表
	 */
	public ArrayList<Bookmark> getBookmarks(){
		ArrayList<Bookmark> bml = new ArrayList<Bookmark>();
		Cursor bms = db.rawQuery("select * from "+TABLE_NAME,null);
		for(int j=0;j<bms.getCount();j++){
			bms.moveToPosition(j);
			Bookmark bm = new Bookmark();
			bm.id = bms.getInt(0);
			bm.bookName =bms.getString(1);
			bm.chapterName =bms.getString(2);
			bm.description =bms.getString(3);
			bm.bookType = bms.getInt(4);
			bm.createDate = bms.getString(5);
			bm.position1 = bms.getInt(6);
			bm.position2 = bms.getInt(7);
			bm.epubPath = bms.getString(8);
			
			bml.add(bm);
		}
		bms.close();
		return bml;
	}
	
	
	/**
	 * 當前頁是否有加入過書籤
	 * @param path epub path
	 * @param chapName 章節名稱
	 * @param startSpan 頁面起點span
	 * @param startIdx 頁面起點idx
	 * @param endSpan 頁面終點span
	 * @param endIdx 頁面終點index
	 * @return 當前頁是否有加入過書籤
	 */
	public boolean isCurPageBookmarked(String path,String chapName,int startSpan,int startIdx,int endSpan,int endIdx){
		try {
			Cursor cursor;
			//Log.d("sspan","is:"+startSpan);
			//Log.d("sid","is:"+startIdx);
			//Log.d("espan","is:"+endSpan);
			//Log.d("eid","is:"+endIdx);
			
			if(startIdx<=0){
				cursor= db.rawQuery("select _id from "+TABLE_NAME+" where epubPath=? and chapterName = ? and position1>=? and (position1<? or (position1=? and position2<=?))",new String[]{ path,chapName,""+startSpan,""+endSpan,""+endSpan,""+endIdx} );
			}else{
				cursor= db.rawQuery("select _id from "+TABLE_NAME+" where epubPath=? and chapterName = ? and (position1>? or (position1=? and position2>=?)) and (position1<? or (position1=? and position2<=?))",new String[]{path,chapName,""+startSpan,""+startSpan,""+startIdx,""+endSpan,""+endSpan,""+endIdx} );
			}
			if(cursor.getCount()>0){
				cursor.close();
				return true;
			}else{
				cursor.close();
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 刪除目前頁面書籤
	 * @param path epub path
	 * @param chapName 章節名稱
	 * @param startSpan 頁面起點span
	 * @param startIdx 頁面起點index
	 * @param endSpan 頁面終點span
	 * @param endIdx 頁面終點index
	 * @return 是否刪除
	 */
	public boolean deleteCurPageBookmark(String path,String chapName,int startSpan,int startIdx,int endSpan,int endIdx){
		try {
			Cursor cursor;
			if(startIdx<=0){
				cursor= db.rawQuery("select _id from "+TABLE_NAME+" where epubPath=? and chapterName = ? and position1>=? and (position1<? or (position1=? and position2<=?))",new String[]{path,chapName,""+startSpan,""+endSpan,""+endSpan,""+endIdx} );
			}else{
				cursor= db.rawQuery("select _id from "+TABLE_NAME+" where epubPath=? and chapterName = ? and (position1>? or (position1=? and position2>=?)) and (position1<? or (position1=? and position2<=?))",new String[]{path,chapName,""+startSpan,""+startSpan,""+startIdx,""+endSpan,""+endSpan,""+endIdx} );
			}
			if(cursor.getCount()>0){
				for(int i=0;i<cursor.getCount();i++){
					cursor.moveToPosition(i);
					db.delete(TABLE_NAME,"_id=?",new String[]{""+cursor.getInt(0)});
				}
				cursor.close();
				return true;
			}else{
				cursor.close();
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 資料庫筆數
	 * @return 筆數
	 */
	public int getCount(){
	return db.rawQuery("Select _id from "+TABLE_NAME, null).getCount();
	}
	
	/**
	 * 關閉db
	 */
	public void closeDB(){
		db.close();
	}
	
	/**
	 * 開啟db
	 */
	public void openDB(){
		dbh=new BookmarksDBHelper(ctx);
		db=dbh.getWritableDatabase();
	}
	
	/**
	 * 取得db路徑
	 * @return db path
	 */
	public String getDBPath(){
		return ctx.getDir("dbs", Context.MODE_PRIVATE)+"/"+DB_NAME;
	}
    
	/**
	 * 書籤db操作輔助class
	 * @author III
	 * 
	 */
	private class BookmarksDBHelper{
		//notes DB處理class
		SQLiteDatabase db;
		private String dbPath = getDBPath();
		public BookmarksDBHelper(Context context) {
			// TODO Auto-generated constructor stub
			File dbFile = new File(dbPath);
			if(dbFile.exists()){
			db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
				if(db.getVersion()<DB_VERSION){
					onUpgrade();
				}
			}else{
				db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
				onCreate();
			}
		}


		public void onCreate() {
			db.execSQL(CREATE_DB);
			db.setVersion(DB_VERSION);
			// TODO Auto-generated method stub			
		}

		public void onUpgrade() {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
			onCreate();
		}
		public SQLiteDatabase getWritableDatabase(){
			return db;
		}
		
	}

}
