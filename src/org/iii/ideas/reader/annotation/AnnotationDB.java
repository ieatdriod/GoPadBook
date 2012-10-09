package org.iii.ideas.reader.annotation;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 註記db class，處理註記存取增刪。註:在twm專案epub path全為did(在操作上只要是唯一識別碼即可)
 * @author III
 *
 */
public class AnnotationDB {
	//註解處理class
	private static final int DB_VERSION = 2;
	private static final String DB_NAME = "Annotation.db";
	private static final String TABLE_NAME = "annotations";
	private static final String CREATE_DB = "create table "+TABLE_NAME+"( _id INTEGER PRIMARY KEY,"
	+"bookName TEXT, chapterName TEXT, description TEXT, bookType INTEGER, createDate TEXT, position1 INTEGER, position2 INTEGER, epubPath TEXT, content TEXT);";
	private AnnDBHelper dbh;
	private SQLiteDatabase db;
	private Context ctx;
	
	/**
	 * @param context context
	 * @throws SQLException db開啟失敗
	 */
	public AnnotationDB(Context context) throws SQLException{
		ctx=context;
		//Log.d("getBMDB","get");
		dbh=new AnnDBHelper(context);
		db=dbh.getWritableDatabase();
	}
	
	/**
	 * 刪除註記，根據傳入的註記array list刪除db中相對應欄位
	 * @param list annotation list
	 * @return 刪除筆數
	 */
	public int deleteAnns(ArrayList<Annotation> list){
		int delCount=0;//,idx;
		for(int i=0;i<list.size();i++){
			Annotation bm = list.get(i);
			try{
				db.delete(TABLE_NAME,"_id=?",new String[]{""+bm.id});
				delCount++;
			}catch(Exception e){
				Log.e("AnnotationDB:deleteAnns",e.toString());
			}
		}
		return delCount;
	}
	
	/**
	 * 根據id刪除註記
	 * @param id _id attribute
	 * @return 刪除筆數
	 */
	public int deleteAnnById(int id){
		int delCount=0;//,idx;
		delCount=db.delete(TABLE_NAME,"_id=?",new String[]{""+id});
		return delCount;
	}
	

	/**
	 * 新增註記，createData可空
	 * @param an annotation
	 */
	public void insertAnn(Annotation an){
		try{
				//Log.d("position1","is:"+bm.position1);
				//Log.d("position2","is:"+bm.position2);
				//Log.d("Bookmark:insertBookmark","in");
				ContentValues values = new ContentValues();
				values.put("chapterName", an.chapterName);
				values.put("description", an.description);
				values.put("bookName", an.bookName);
				values.put("position1", an.position1);
				//Log.d("insertP2","is:"+bm.position2);
				values.put("position2", an.position2);
				values.put("createDate", ""+System.currentTimeMillis());
				values.put("bookType", an.bookType);
				values.put("epubPath", an.epubPath);
				values.put("content", an.content);
				db.insert(TABLE_NAME,null,values);
		}catch(Exception e){
			Log.e("AnnotationDB:insertAnn",e.toString());
		}
	}
	
	/**
	 * 刪除某本書所有註記
	 * @param path epub path，在twm專案為did
	 */
	public void deleteAnnByEpubPath(String path){
		db.delete(TABLE_NAME,"epubPath=?",new String[]{path});
	}
	
	/**
	 * 刪除所有註記
	 */
	public void deleteAllAnn(){
		db.delete(TABLE_NAME,null,null);
	}
	
	/**
	 * 根據id取得某一註記
	 * @param id _id欄位
	 * @return 註記，如果db中無此id則return null
	 */
	public Annotation getAnnById(int id){
		Cursor bms = db.rawQuery("select * from "+TABLE_NAME+" where _id=?",new String[]{""+id} );
		if(bms.getCount()>0){
			bms.moveToPosition(0);
			Annotation bm = new Annotation();
			bm.id = bms.getInt(0);
			bm.bookName =bms.getString(1);
			bm.chapterName =bms.getString(2);
			bm.description =bms.getString(3);
			bm.bookType = bms.getInt(4);
			bm.createDate = bms.getString(5);
			bm.position1 = bms.getInt(6);
			bm.position2 = bms.getInt(7);
			//Log.d("bmposition2","is:"+bm.position2);
			bm.epubPath = bms.getString(8);
			bm.content = bms.getString(9);
			bms.close();
			return bm;
		}else{
			bms.close();
			return null;
		}


	}
	
	/**
	 * 取得某一span的annotation list
	 * @param path epub path，在twm專案為did
	 * @param chapName 章節名稱
	 * @param span 第幾個span
	 * @return 註記列表，db中無資料則回傳null
	 */
	public ArrayList<Annotation> getAnnsBySpan(String path,String chapName,int span){
		ArrayList<Annotation> bml = new ArrayList<Annotation>();
		Cursor bms = db.rawQuery("select * from "+TABLE_NAME+" where epubPath=? and chapterName=? and position1=?",new String[]{path,chapName,""+span} );
		for(int j=0;j<bms.getCount();j++){
			bms.moveToPosition(j);
			Annotation bm = new Annotation();
			bm.id = bms.getInt(0);
			bm.bookName =bms.getString(1);
			bm.chapterName =bms.getString(2);
			bm.description =bms.getString(3);
			bm.bookType = bms.getInt(4);
			bm.createDate = bms.getString(5);
			bm.position1 = bms.getInt(6);
			bm.position2 = bms.getInt(7);
			//Log.d("bmposition2","is:"+bm.position2);
			bm.epubPath = bms.getString(8);
			bm.content = bms.getString(9);
			bml.add(bm);
		}
		bms.close();
		return bml;
	}
	
	/**
	 * 取得某本書註記筆數
	 * @param path epub path，在twm專案為did
	 * @return  註記筆數
	 */
	public int getAnnsCountByEpubPath(String path){
		try{
			Cursor bms = db.rawQuery("select * from "+TABLE_NAME+" where epubPath=?",new String[]{path} );
			int count  = bms.getCount();
			bms.close();
			return count;
		}catch(Exception e){
			Log.e("AnnotationDB:getAnnsCountByEpubPath",e.toString());
			return 0;
		}
	}
	
	/**
	 * 取得某本書註記列表
	 * @param path epub path，在twm專案為did
	 * @return annotation list，or an empty list if not found
	 */
	public ArrayList<Annotation> getAnnsByEpubPath(String path){
		ArrayList<Annotation> bml = new ArrayList<Annotation>();
		Cursor bms = db.rawQuery("select * from "+TABLE_NAME+" where epubPath=?",new String[]{path} );
		for(int j=0;j<bms.getCount();j++){
			bms.moveToPosition(j);
			Annotation bm = new Annotation();
			bm.id = bms.getInt(0);
			bm.bookName =bms.getString(1);
			bm.chapterName =bms.getString(2);
			bm.description =bms.getString(3);
			bm.bookType = bms.getInt(4);
			bm.createDate = bms.getString(5);
			bm.position1 = bms.getInt(6);
			bm.position2 = bms.getInt(7);
			bm.epubPath = bms.getString(8);
			bm.content = bms.getString(9);
			bml.add(bm);
		}
		bms.close();
		return bml;
	}
	
	/**
	 * 取得db所有註記
	 * @return annotation list, or an empty list if not found
	 */
	public ArrayList<Annotation> getAnns(){
		ArrayList<Annotation> bml = new ArrayList<Annotation>();
		Cursor bms = db.rawQuery("select * from "+TABLE_NAME,null);
		for(int j=0;j<bms.getCount();j++){
			bms.moveToPosition(j);
			Annotation bm = new Annotation();
			bm.id = bms.getInt(0);
			bm.bookName =bms.getString(1);
			bm.chapterName =bms.getString(2);
			bm.description =bms.getString(3);
			bm.bookType = bms.getInt(4);
			bm.createDate = bms.getString(5);
			bm.position1 = bms.getInt(6);
			bm.position2 = bms.getInt(7);
			bm.epubPath = bms.getString(8);
			bm.content = bms.getString(9);
			bml.add(bm);
		}
		bms.close();
		return bml;
	}
	
	/**
	 * 當前頁面有無註記
	 * @param path epub path，在twm專案為did
	 * @param chapName 章節名稱
	 * @param startSpan 頁面起始span
	 * @param startIdx 頁面起始index
	 * @param endSpan 頁面終點span
	 * @param endIdx 頁面終點index
	 * @return 當前頁面有無註記
	 */
	public boolean isCurPageAnnotated(String path,String chapName,int startSpan,int startIdx,int endSpan,int endIdx){
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
	 * 取得當前頁面註記id
	 * @param path epub path，在twm專案為did
	 * @param chapName 章節名稱
	 * @param startSpan 頁面起始span
	 * @param startIdx 頁面起始index
	 * @param endSpan 頁面終點span
	 * @param endIdx 頁面終點index
	 * @return 當前頁面註記id
	 */
	public int getCurPageAnnotationId(String path,String chapName,int startSpan,int startIdx,int endSpan,int endIdx){
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
				cursor.moveToFirst();
				int id = cursor.getInt(0);
				cursor.close();
				return id;
			}
			return -1;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * 刪除當前頁面註記
	 * @param path epub path，在twm專案為did
	 * @param chapName 章節名稱
	 * @param startSpan 頁面起始span
	 * @param startIdx 頁面起始index
	 * @param endSpan 頁面終點span
	 * @param endIdx 頁面終點index
	 * @return 如db中有資料且刪除成功，return true; 否則return false
	 */
	public boolean deleteCurPageAnnotation(String path,String chapName,int startSpan,int startIdx,int endSpan,int endIdx){
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
	 * 取得註記筆數
	 * @return 註記筆數
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
		dbh=new AnnDBHelper(ctx);
		db=dbh.getWritableDatabase();
	}
	
	/**
	 * 取得db檔案路徑
	 * @return db檔案路徑
	 */
	public String getDBPath(){
		return ctx.getDir("dbs", Context.MODE_PRIVATE)+"/"+DB_NAME;
	}
    
	/**
	 * 註記db輔助工具
	 * @author III
	 *
	 */
	private class AnnDBHelper{
		//notes DB處理class
		SQLiteDatabase db;
		private String dbPath = getDBPath();
		public AnnDBHelper(Context context) {
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
