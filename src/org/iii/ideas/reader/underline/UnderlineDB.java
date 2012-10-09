package org.iii.ideas.reader.underline;

import java.io.File;
import java.util.ArrayList;

import org.iii.ideas.reader.renderer.MacroRenderer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
/**
 * 使用者重點劃線資料資料庫，處理劃線資料的刪除/增加/查詢
 * @author III
 * 
 * 
 */
public class UnderlineDB {
	//註解處理class
	private static final int DB_VERSION = 8;
	private static final String DB_NAME = "Underline.db";
	private static final String TABLE_NAME = "underline";
	private static final String CREATE_DB = "create table "+TABLE_NAME+"( _id INTEGER PRIMARY KEY,"
	+"bookName TEXT, chapterName TEXT, description TEXT, bookType INTEGER, createDate TEXT, span1 INTEGER, idx1 INTEGER, epubPath TEXT, content TEXT,span2 INTEGER, idx2 INTEGER);";
	private UlDBHelper dbh;
	private SQLiteDatabase db;
	private Context ctx;
	/**
	 * constructor
	 * @param context context
	 * @throws SQLException db開啟失敗exception
	 */
	public UnderlineDB(Context context) throws SQLException{
		ctx=context;
		dbh=new UlDBHelper(context);
		db=dbh.getWritableDatabase();
	}
	
	/**
	 * 根據傳入的劃線資料刪除其在DB相對應的紀錄。
	 * @param list 欲刪除的劃線資料
	 * @return 刪除筆數
	 */
	public int deleteUnderline(ArrayList<Underline> list){
		int delCount=0;
		if(list==null)
			return 0;
		for(int i=0;i<list.size();i++){
			Underline bm = list.get(i);
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
	 * 根據id(即table中的_id欄位之值)刪除劃線
	 * @param id id
	 * @return 影響筆數，即1成功；0未刪除
	 */
	public int deleteUnderlineById(int id){
		int delCount=0;
		delCount=db.delete(TABLE_NAME,"_id=?",new String[]{""+id});
		return delCount;
	}
	
	/**
	 * 插入單筆劃線資料，若與過往紀錄有重疊則將紀錄合併
	 * @param ul 該筆劃線資料 
	 * @param renderer renderer物件，用來在紀錄合併的情況下取得合併後的劃線文字內容，可為null。若為null則採用傳入的Underline物件的description值。
	 */
	public void insertUnderline(Underline ul,MacroRenderer renderer){
		try{
			ArrayList<Underline> dbUls = getOverlappedUnderline(ul);
			if(dbUls==null){
				ContentValues values = new ContentValues();
				values.put("chapterName", ul.chapterName);
				values.put("description", ul.description);
				values.put("bookName", ul.bookName);
				values.put("span1", ul.span1);
				values.put("span2", ul.span2);
				values.put("idx1", ul.idx1);
				values.put("idx2", ul.idx2);
				values.put("createDate", ""+System.currentTimeMillis());
				values.put("bookType", ul.bookType);
				values.put("epubPath", ul.epubPath);
				values.put("content", ul.content);
				db.insert(TABLE_NAME,null,values);
			}else{
				ContentValues values = new ContentValues();
				values.put("chapterName", ul.chapterName);
				values.put("bookName", ul.bookName);
				//int span1,span2,idx1,idx2;
				for(int i=0;i<dbUls.size();i++){
					Underline dbUl = dbUls.get(i);
					if(ul.span1>dbUl.span1){
						ul.span1 =  dbUl.span1;
						ul.idx1 =  dbUl.idx1;
					}else if(ul.span1==dbUl.span1){
						ul.span1 = ul.span1;
						ul.idx1 = Math.min(dbUl.idx1, ul.idx1);
					}else{
						ul.span1 = ul.span1;
						ul.idx1 = ul.idx1;
					}
					if(ul.span2<dbUl.span2){
						ul.span2 = dbUl.span2;
						ul.idx2 = dbUl.idx2;
					}else if(ul.span2==dbUl.span2){
						ul.span2 = ul.span2;
						ul.idx2 = Math.max(dbUl.idx2, ul.idx2);
					}else{
						ul.span2 = ul.span2;
						ul.idx2 = ul.idx2;
					}
					deleteUnderlineById(dbUl.id);
				}
				
				values.put("span1", ul.span1);
				values.put("idx1", ul.idx1);
				values.put("span2", ul.span2);
				values.put("idx2", ul.idx2);
				if(renderer!=null)
					values.put("description", renderer.getUnderlineDescription(ul.span1, ul.idx1, ul.span2, ul.idx2));
				else
					values.put("description", ul.description);
				values.put("createDate", ""+System.currentTimeMillis());
				values.put("bookType", ul.bookType);
				values.put("epubPath", ul.epubPath);
				values.put("content", ul.content);
				db.insert(TABLE_NAME,null,values);
			}

		}catch(Exception e){
			Log.e("AnnotationDB:insertAnn",e.toString());
		}
	}

	/**
	 * 根據epub path(在此專案為delivery id)刪除該書本所有劃線資料。
	 * @param path epub path(在此專案為delivery id)
	 */
	public void deleteUnderlineByEpubPath(String path){
		db.delete(TABLE_NAME,"epubPath=?",new String[]{path});
	}
	
	/**
	 *  清空資料庫所有資料
	 */
	public void deleteAllUnderline(){
		db.delete(TABLE_NAME,null,null);
	}
	
	
	/**
	 * 根據某一定位點(在此可視為某一書籍內容文字)取得包含該點的劃線內容
	 * @param path epub path(delivery id)
	 * @param chapName 該章節於spine中的相對路徑
	 * @param span 該定位點位於該章節的span
	 * @param idx 該定位點位於span中哪一位置
	 * @return 劃線內容
	 */
	public ArrayList<Underline> getUnderlineBySpanAndIdx(String path,String chapName,int span,int idx){
		ArrayList<Underline> bml = new ArrayList<Underline>();
		Cursor bms = db.rawQuery("select * from "+TABLE_NAME+" where epubPath=? and chapterName=? and (span1<? or (span1=? and idx1<=?)) and (span2>? or (span2=? and idx2>=?))",new String[]{path,chapName,""+span,""+span,""+idx,""+span,""+span,""+idx} );
		for(int j=0;j<bms.getCount();j++){
			bms.moveToPosition(j);
			Underline bm = new Underline();
			bm.id = bms.getInt(0);
			bm.bookName =bms.getString(1);
			bm.chapterName =bms.getString(2);
			bm.description =bms.getString(3);
			bm.bookType = bms.getInt(4);
			bm.createDate = bms.getString(5);
			bm.span1 = bms.getInt(6);
			bm.idx1 = bms.getInt(7);
			//Log.d("bmposition2","is:"+bm.position2);
			bm.epubPath = bms.getString(8);
			bm.content = bms.getString(9);
			bm.span2 = bms.getInt(10);
			bm.idx2 = bms.getInt(11);
			bml.add(bm);
		}
		bms.close();
		return bml;
	}
	
	
	/**
	 * 取得某一span中所有劃線資料
	 * @param path epub path(delivery id)
	 * @param chapName 該章節於spine中的相對路徑
	 * @param span 該章節中哪一span
	 * @return 某一span中所有劃線資料
	 */
	public ArrayList<Underline> getUnderlineDividedBySpan(String path,String chapName,int span){
		ArrayList<Underline> bml = new ArrayList<Underline>();
		Cursor bms = db.rawQuery("select * from "+TABLE_NAME+" where epubPath=? and chapterName=? and span1<=? and span2>=?",new String[]{path,chapName,""+span,""+span} );
		for(int j=0;j<bms.getCount();j++){
			bms.moveToPosition(j);
			Underline bm = new Underline();
			bm.id = bms.getInt(0);
			bm.bookName =bms.getString(1);
			bm.chapterName =bms.getString(2);
			bm.description =bms.getString(3);
			bm.bookType = bms.getInt(4);
			bm.createDate = bms.getString(5);
			bm.span1 = bms.getInt(6);
			bm.idx1 = bms.getInt(7);
			//Log.d("bmposition2","is:"+bm.position2);
			bm.epubPath = bms.getString(8);
			bm.content = bms.getString(9);
			bm.span2 = bms.getInt(10);
			bm.idx2 = bms.getInt(11);
			if(bm.span1<span){
				bm.span1=span;
				bm.idx1=0;
			}
			if(bm.span2>span){
				bm.span2=span;
				bm.idx2=-1;
			}
			bml.add(bm);
		}
		bms.close();
		return bml;
	}
	
	
	/**
	 * 取得某本書劃線筆數
	 * @param path epub path
	 * @return 該書劃線筆數
	 */
	public int getUnderlineCountByEpubPath(String path){
		try{
			Cursor bms = db.rawQuery("select * from "+TABLE_NAME+" where epubPath=?",new String[]{path} );
			int count  = bms.getCount();
			bms.close();
			return count;
		}catch(Exception e){
			Log.e("UnderlineDB:getUnderlineCountByEpubPath",e.toString());
			return 0;
		}
	}
	
	/**
	 * 取得某本書所有劃線內容
	 * @param path epub path(delivery id)
	 * @return 該書所有劃線內容
	 */
	public ArrayList<Underline> getUnderlineByEpubPath(String path){
		ArrayList<Underline> bml = new ArrayList<Underline>();
		Cursor bms = db.rawQuery("select * from "+TABLE_NAME+" where epubPath=?",new String[]{path} );
		for(int j=0;j<bms.getCount();j++){
			bms.moveToPosition(j);
			Underline bm = new Underline();
			bm.id = bms.getInt(0);
			bm.bookName =bms.getString(1);
			bm.chapterName =bms.getString(2);
			bm.description =bms.getString(3);
			bm.bookType = bms.getInt(4);
			bm.createDate = bms.getString(5);
			bm.span1 = bms.getInt(6);
			bm.idx1 = bms.getInt(7);
			bm.epubPath = bms.getString(8);
			bm.content = bms.getString(9);
			bm.span2 = bms.getInt(10);
			bm.idx2 = bms.getInt(11);
			bml.add(bm);
		}
		bms.close();
		return bml;
	}
	
	/**
	 * 根據id取得某筆劃線資料
	 * @param id id
	 * @return 劃線資料
	 */
	public Underline getUnderlineById(int id){
		Cursor bms = db.rawQuery("select * from where _id"+TABLE_NAME,new String[]{String.valueOf(id)});
		bms.moveToFirst();
		try {
			Underline bm = new Underline();
			bm.id = bms.getInt(0);
			bm.bookName =bms.getString(1);
			bm.chapterName =bms.getString(2);
			bm.description =bms.getString(3);
			bm.bookType = bms.getInt(4);
			bm.createDate = bms.getString(5);
			bm.span1 = bms.getInt(6);
			bm.idx1 = bms.getInt(7);
			bm.epubPath = bms.getString(8);
			bm.content = bms.getString(9);
			bm.span2 = bms.getInt(10);
			bm.idx2 = bms.getInt(11);
			return bm;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 取得db中所有劃線資料
	 * @return db中所有劃線資料
	 */
	public ArrayList<Underline> getUnderline(){
		ArrayList<Underline> bml = new ArrayList<Underline>();
		Cursor bms = db.rawQuery("select * from "+TABLE_NAME,null);
		for(int j=0;j<bms.getCount();j++){
			bms.moveToPosition(j);
			Underline bm = new Underline();
			bm.id = bms.getInt(0);
			bm.bookName =bms.getString(1);
			bm.chapterName =bms.getString(2);
			bm.description =bms.getString(3);
			bm.bookType = bms.getInt(4);
			bm.createDate = bms.getString(5);
			bm.span1 = bms.getInt(6);
			bm.idx1 = bms.getInt(7);
			bm.epubPath = bms.getString(8);
			bm.content = bms.getString(9);
			bm.span2 = bms.getInt(10);
			bm.idx2 = bms.getInt(11);
			bml.add(bm);
		}
		bms.close();
		return bml;
	}
	
	
	private ArrayList<Underline> getOverlappedUnderline(Underline ul){
		try {
			Cursor bms;
			bms = db
					.rawQuery(
							"select * from "
									+ TABLE_NAME
									+ " where epubPath=? and chapterName = ? and (span2>? or (span2=? and idx2>=?)) and (span1<? or (span1=? and idx1<=?))",
							new String[] { ul.epubPath, ul.chapterName,
									"" + ul.span1, "" + ul.span1, "" + ul.idx1,
									"" + ul.span2, "" + ul.span2, "" + ul.idx2 });
			if (bms.getCount() <= 0) {
				bms.close();
				return null;
			} else {
				ArrayList<Underline> uls = new ArrayList<Underline>();
				for(int i=0;i<bms.getCount();i++){
					bms.moveToPosition(i);
					Underline bm = new Underline();
					bm.id = bms.getInt(0);
					bm.bookName =bms.getString(1);
					bm.chapterName =bms.getString(2);
					bm.description =bms.getString(3);
					bm.bookType = bms.getInt(4);
					bm.createDate = bms.getString(5);
					bm.span1 = bms.getInt(6);
					bm.idx1 = bms.getInt(7);
					bm.epubPath = bms.getString(8);
					bm.content = bms.getString(9);
					bm.span2 = bms.getInt(10);
					bm.idx2 = bms.getInt(11);
					uls.add(bm);
				}
				bms.close();
				return uls;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * 判斷當前頁面是否有任何劃線資料
	 * @param path epub path
	 * @param chapName 章節相對路徑
	 * @param startSpan 頁面首字span
	 * @param startIdx 頁面首字idx
	 * @param endSpan 頁面末字span
	 * @param endIdx 頁面末字idx
	 * @return 當前頁面是否有任何劃線資料
	 */
	public boolean isCurPageUnderlined(String path,String chapName,int startSpan,int startIdx,int endSpan,int endIdx){
		Cursor cursor1,cursor2,cursor3;
		int count=0;
		cursor1= db.rawQuery("select _id from "+TABLE_NAME+" where epubPath=? and chapterName = ? and (span2>? or (span2=? and idx2>=?)) and (span1<? or (span1=? and idx1<=?))",new String[]{path,chapName,""+startSpan,""+startSpan,""+startIdx,""+startSpan,""+startSpan,""+startIdx} );
		count+=cursor1.getCount();
		cursor1.close();
		cursor2= db.rawQuery("select _id from "+TABLE_NAME+" where epubPath=? and chapterName = ? and (span2>? or (span2=? and idx2>=?)) and (span1<? or (span1=? and idx1<=?))",new String[]{path,chapName,""+endSpan,""+endSpan,""+endIdx,""+startSpan,""+startSpan,""+startIdx} );
		count+=cursor2.getCount();
		cursor2.close();
		cursor3= db.rawQuery("select _id from "+TABLE_NAME+" where epubPath=? and chapterName = ? and (span2<? or (span2=? and idx2<=?)) and (span1>? or (span1=? and idx1>=?))",new String[]{path,chapName,""+endSpan,""+endSpan,""+endIdx,""+startSpan,""+startSpan,""+startIdx} );
		count+=cursor3.getCount();
		cursor3.close();
		//Log.d("isCurPageUnderlined","indb count:"+count);
		if(count>0){
			return true;
		}else{
			return false;
		}
	}
	

	
	/**
	 * 取得db紀錄筆數
 	 * @return db紀錄筆數
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
		dbh=new UlDBHelper(ctx);
		db=dbh.getWritableDatabase();
	}
	
	/**
	 * 取得.db檔案路徑
	 * @return db檔案路徑
	 */
	public String getDBPath(){
		return ctx.getDir("dbs", Context.MODE_PRIVATE)+"/"+DB_NAME;
	}
    
	private class UlDBHelper{
		//notes DB處理class
		SQLiteDatabase db;
		private String dbPath = getDBPath();
		public UlDBHelper(Context context) {
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
