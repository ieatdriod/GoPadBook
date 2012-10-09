package com.gsimedia.gsiebook.lib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gsimedia.gsiebook.SettingActivity;
import com.gsimedia.gsiebook.common.Config;
import com.taiwanmobile.common.Base64;
import com.taiwanmobile.myBook_PAD.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Rect;
import android.graphics.RegionIterator;
import android.net.Uri;
import android.util.Log;

public class GSiDatabaseAdapter{
	
	public static final int KErrNone = 0;
	public static final int KErrNotExist = -1;
	public static final int KErrNotReady = -2;
	
	private DatabaseHelper iDBHelper = null;
	private SQLiteDatabase iDB = null;
	private int iCurrentMaxRegionGroup =0 ;
	
	/**
	 * constructor
	 */
	public GSiDatabaseAdapter(Context context){
		iDBHelper = new DatabaseHelper(context);
	}

	/**
	 * db field
	 */
	static final String KEY_DBName="GSieBookDB.db";
	static final int DBVersion = 3;
	
	static final String KEY_BookmarkTable = "Bookmark";
	static final String KEY_BookID = "BookID";
	static final String KEY_Page = "Page";

	static final String KEY_AnnoTable="Annotation";
	static final String KEY_Anno="Anno";
	
	static final String KEY_MarkerTable = "Marker";
	static final String KEY_MarkerRegionTable = "MarkerRegion";
	static final String KEY_RegionGroup="Region";
	static final String KEY_MarkTop="Top";
	static final String KEY_MarkLeft="Left";
	static final String KEY_MarkBottom="Bottom";
	static final String KEY_MarkRight="Right";
	static final String KEY_MarkText="Text";
	
	static final String KEY_LastPageTable = "LastPage";
	static final String KEY_TimeStamp = "TimeStamp";
	
	static final String KEY_SwitchTable = "PageSwitch";
	static final String KEY_Direction = "Direction";
	
	static final String CREATE_Bookmark = 
		"CREATE TABLE "+KEY_BookmarkTable+" ("+
		" "+KEY_BookID+" text ,"+
		" "+KEY_Page+" integer not null "+
		")";
	static final String CREATE_Annotation = 
		"CREATE TABLE "+KEY_AnnoTable+" ("+
		" "+KEY_BookID+" text ,"+
		" "+KEY_Page+" INTEGER NOT NULL ,"+
		" "+KEY_Anno+" text not null "+
		")";
	static final String CREATE_Marker = 
		"CREATE TABLE "+KEY_MarkerTable+" ("+
		" "+KEY_BookID+" text NOT NULL,"+
		" "+KEY_Page+" INTEGER NOT NULL,"+
		" "+KEY_RegionGroup+" INTEGER NOT NULL ,"+
		" "+KEY_MarkText+" text "+
		")";
	static final String CREATE_MarkerRegion = 
		"CREATE TABLE "+KEY_MarkerRegionTable+" ("+
		" "+KEY_BookID+" text NOT NULL,"+
		" "+KEY_Page+" INTEGER NOT NULL,"+
		" "+KEY_RegionGroup+" INTEGER NOT NULL ,"+
		" "+KEY_MarkTop+" INTEGER NOT NULL ,"+
		" "+KEY_MarkLeft+" INTEGER NOT NULL ,"+
		" "+KEY_MarkBottom+" INTEGER NOT NULL ,"+
		" "+KEY_MarkRight+" INTEGER NOT NULL "+
		")";
	static final String CREATE_LastPage = 
		"CREATE TABLE "+KEY_LastPageTable+" ("+
		" "+KEY_BookID+" text ,"+
		" "+KEY_Page+" integer not null ,"+
		" "+KEY_TimeStamp+" long not null"+
		")";	
	static final String CREATE_PageSwitch= 
		"CREATE TABLE "+KEY_SwitchTable+" ("+
		" "+KEY_BookID+" text ,"+
		" "+KEY_Direction+" bool not null"+
		")";	

	/**
	 * GSi DatabaseHelper
	 * @author water
	 *
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context/*, String name,
				CursorFactory factory, int version*/) {
			super(context, KEY_DBName, null, DBVersion);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_Bookmark);
			db.execSQL(CREATE_Annotation);
			db.execSQL(CREATE_LastPage);
			db.execSQL(CREATE_PageSwitch);
			db.execSQL(CREATE_Marker);
			db.execSQL(CREATE_MarkerRegion);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.e(Config.LOGTAG,"Database upgrade!");
			if(db.getVersion()<DBVersion){
				switch(db.getVersion()){
				case 1:
				db.execSQL("drop table "+KEY_BookmarkTable);
				db.execSQL("drop table "+KEY_AnnoTable);
				db.execSQL("drop table "+KEY_LastPageTable);
				
				db.execSQL(CREATE_Bookmark);
				db.execSQL(CREATE_Annotation);
				db.execSQL(CREATE_LastPage);
				db.execSQL(CREATE_PageSwitch);

					db.execSQL(CREATE_Marker);
					db.execSQL(CREATE_MarkerRegion);
					break;
				case 2:
//					db.execSQL("drop table "+KEY_BookmarkTable);
//					db.execSQL("drop table "+KEY_AnnoTable);
//					db.execSQL("drop table "+KEY_LastPageTable);
					db.execSQL(CREATE_Marker);
					db.execSQL(CREATE_MarkerRegion);
					break;
				}
				
			}
			
		}
		
	}
	
	
	/**
	 * open the bookmark database
	 * 
	 * @return the Bookmark object
	 * @throws SQLException
	 */
	public GSiDatabaseAdapter open() throws SQLException{
		if(iDB == null)
			iDB = iDBHelper.getWritableDatabase();
		return this;
	}

	/**
	 * close the database
	 */
	public void close() {
		if(iDB!=null){
			iDB.close();
			iDBHelper.close();
			iDB = null;
		}
	}
	/**
	 * set book switch page direction
	 */
	public void setPageSwitch(String aBookID, int aDir){
		if(iDB == null){
			Log.e(Config.LOGTAG,"setPageSwitch iDB null");
			return ;
		}

		Log.d(Config.LOGTAG,"page switch direction"+aBookID);
		// add into db
		ContentValues cv = new ContentValues();
		cv.put(KEY_BookID, aBookID);
		cv.put(KEY_Direction, aDir);
		// update if exist
		String sql = KEY_BookID + "=?";
		if (iDB.update(KEY_SwitchTable, cv, sql, new String[]{aBookID}) == 0) {
			iDB.insert(KEY_SwitchTable, null, cv);
			Log.d(Config.LOGTAG,"switch direction "+aDir);
		}
		
	}
	public int getPageSwitch(String aBookID){
		if(iDB == null){
			Log.e(Config.LOGTAG,"getPageSwitch iDB null");
			return MODE_DEFAULT;
		}
		int aDir = MODE_DEFAULT;
		Cursor cur = iDB.query(true, KEY_SwitchTable, new String[] { KEY_Direction },
				KEY_BookID + "=?", new String[]{aBookID},
				null, null, null, "1");
		if (cur != null) {
			if (cur.moveToFirst()) {
				aDir = cur.getInt(0);
			}
			cur.close();
		}
		return aDir;
	}
	/**
	 * add a bookmark into database
	 * @param aBookID
	 * @param aBookmark
	 */
	public void addBookmark(String aBookID, GSiBookmark aBookmark){
		if(iDB == null){
			Log.e(Config.LOGTAG,"addBookmark iDB null");
			return ;
		}
		// add bookmark into db
		ContentValues cv = new ContentValues();
		cv.put(KEY_BookID, aBookID);
		cv.put(KEY_Page, aBookmark.iPage);
		// update if exist
		String sql = KEY_BookID + "=? AND " + KEY_Page + "=" +aBookmark.iPage;
		Log.d(Config.LOGTAG, "update database "+sql);
		if (iDB.update(KEY_BookmarkTable, cv, sql, new String[]{aBookID}) == 0) {
			iDB.insert(KEY_BookmarkTable, null, cv);
			Log.d(Config.LOGTAG,"insert "+aBookmark.iPage);
		}
	}
	/**
	 * remove a bookmark
	 * @param aBookID
	 * @param aBookmark
	 * @return 
	 */
	public int removeBookmark(String aBookID, GSiBookmark aBookmark){
		if(iDB == null){
			Log.e(Config.LOGTAG,"removeBookmark iDB null");
			return KErrNotReady;
		}
		// remove bookmark from db
		iDB.delete(KEY_BookmarkTable, KEY_BookID+"=? AND "+KEY_Page+"=?;",new String[] {aBookID,aBookmark.iPage.toString()});
		return KErrNone;
	}
	/**
	 * get bookmarks of given bookID (back/restore function)
	 * @param aBookID
	 * @return
	 */
	public ArrayList<GSiBookmark> getBookmarks(String aBookID,Context aCaller, String aTitle){
		if(iDB == null){
			Log.e(Config.LOGTAG,"getBookmarks iDB null");
			return null;
		}
		ArrayList<GSiBookmark> aBookmarks = new ArrayList<GSiBookmark>();
		//query all bookmark
		Cursor cur = iDB.query(true, KEY_BookmarkTable, new String[] { KEY_Page },
				KEY_BookID + "=?", new String[]{aBookID},
				null, null, null, null);
		Log.d(Config.LOGTAG, "total "+cur.getCount()+" items in table "+KEY_BookmarkTable);
		if (cur != null) {
			if (cur.moveToFirst()) {
				do{
					int aPage = cur.getInt(0);
					String bookmarkTitle = aTitle+"  "+String.format(aCaller.getString(R.string.GSI_BOOKMARK_TEXT),aPage+1);
					aBookmarks.add(new GSiBookmark(Integer.valueOf(aPage),bookmarkTitle));
					if(cur.isLast())
						break;
					else
						cur.moveToNext();
				}while(true);
			}
			cur.close();
		}
		return aBookmarks;
	}
	/**
	 * set book marks (backup/restore function)
	 * @param aBookID
	 * @param aBookmarks
	 * @return
	 */
	public int setBookmarks(String aBookID, ArrayList<GSiBookmark> aBookmarks){
		// add bookmarks
		for(int i=0;i<aBookmarks.size();i++){
			addBookmark(aBookID, aBookmarks.get(i));
		}
			
		return KErrNone;
	}
	public int deleteBookmarks(String aBookID){
		if(iDB == null){
			Log.e(Config.LOGTAG,"deleteBookmarks iDB null");
			return KErrNotReady;
		}
		// remove from database
		Log.d(Config.LOGTAG,"delete all bookmarks of "+aBookID);
		iDB.delete(KEY_BookmarkTable, KEY_BookID+"=?",new String[]{aBookID});
		return KErrNone;
	}
	public int deleteAllBookmarks(){
		if(iDB == null){
			Log.e(Config.LOGTAG,"deleteBookmarks iDB null");
			return KErrNotReady;
		}
		// remove from database
		Log.d(Config.LOGTAG,"delete all bookmarks");
		iDB.delete(KEY_BookmarkTable, null, null);
		return KErrNone;
	}
	public void printDatabase(String aBookID,Context c,String title){
		//query all bookmark
		ArrayList<GSiBookmark> aBookmarks = this.getBookmarks(aBookID,c,title);
		for(int i=0;i<aBookmarks.size();i++)
			Log.d(Config.LOGTAG,"page:"+aBookmarks.get(i).iPage+" title:"+aBookmarks.get(i).iTitle);
		ArrayList<GSiAnnotation> aAnns = this.getAnnotations(aBookID);
		for(int i=0;i<aAnns.size();i++)
			Log.d(Config.LOGTAG,"page:"+aAnns.get(i).iPage+" Annotation:"+aAnns.get(i).iAnnotation);
	}
	
	/**
	 * annotation related functions
	 */
	/**
	 * add annotation
	 * @param aBookID
	 * @param aAnn
	 */
	public void addAnnotation(String aBookID, GSiAnnotation aAnn){
		if(iDB == null){
			Log.e(Config.LOGTAG,"addAnnotation iDB null");
			return ;
		}
		ContentValues cv = new ContentValues();
		cv.put(KEY_BookID, aBookID);
		cv.put(KEY_Page, aAnn.iPage);
		cv.put(KEY_Anno, aAnn.iAnnotation);
		// update if exist
		String sql = KEY_BookID + "=? AND " + KEY_Page + "=" +aAnn.iPage;
		Log.d(Config.LOGTAG, "update database "+sql);
		if (iDB.update(KEY_AnnoTable, cv, sql, new String[]{aBookID}) == 0) {
			iDB.insert(KEY_AnnoTable, null, cv);
			Log.d(Config.LOGTAG,"insert "+aAnn.iPage);
		}
	}
	/**
	 * remove an annotation
	 * @param aBookID
	 * @param aAnn
	 * @return
	 */
	public int removeAnnotation(String aBookID, GSiAnnotation aAnn){
		if(iDB == null){
			Log.e(Config.LOGTAG,"removeAnnotation iDB null");
			return KErrNotReady;
		}
		iDB.delete(KEY_AnnoTable, KEY_BookID+"=? AND "+KEY_Page+"=?;",new String[] {aBookID,aAnn.iPage.toString()});
		return KErrNone;
	}
	/**
	 * get annotations of bookID
	 * @param aBookID
	 * @return
	 */
	public ArrayList<GSiAnnotation> getAnnotations(String aBookID){
		if(iDB == null){
			Log.e(Config.LOGTAG,"getAnnotations iDB null");
			return null;
		}
		ArrayList<GSiAnnotation> aAnnotations = new ArrayList<GSiAnnotation>();
		Cursor cur = iDB.query(true, KEY_AnnoTable, new String[] { KEY_Page, KEY_Anno },
				KEY_BookID + "=?", new String[]{aBookID},
				null, null, null, null);
		Log.d(Config.LOGTAG, "total "+cur.getCount()+" items in table "+KEY_AnnoTable);
		if (cur != null) {
			if (cur.moveToFirst()) {
				do{
					aAnnotations.add(new GSiAnnotation(Integer.valueOf(cur.getInt(0)),cur.getString(1)));
					if(cur.isLast())
						break;
					else
						cur.moveToNext();
				}while(true);
			}
			cur.close();
		}
		return aAnnotations;
	}
	/**
	 * set annokations of bookID
	 * @param aBookID
	 * @param aAnn
	 * @return
	 */
	public int setAnnotations(String aBookID, ArrayList<GSiAnnotation> aAnn){
		for(int i=0;i<aAnn.size();i++){
			addAnnotation(aBookID, aAnn.get(i));
		}
		return KErrNone;
	}
	/**
	 * delete annotations of bookID
	 * @param aBookID
	 * @return
	 */
	public int deleteAnnotations(String aBookID){
		if(iDB == null){
			Log.e(Config.LOGTAG,"deleteAnnotations iDB null");
			return KErrNotReady;
		}
		// remove from database
		iDB.delete(KEY_AnnoTable, KEY_BookID+"=?",new String[] {aBookID});
		return KErrNone;
	}
	public int deleteAllAnnotations(){
		if(iDB == null){
			Log.e(Config.LOGTAG,"deleteAllAnnotations iDB null");
			return KErrNotReady;
		}
		// remove from database
		iDB.delete(KEY_AnnoTable, null,null);
		return KErrNone;
	}
	
	/**
	 * marker related functions
	 */
	/**
	 * add marker 
	 * @param aBookID
	 * @param aMarker
	 */
	public void addMarker(String aBookID, MarkResult aMarker, int RegionGroup){
		if(iDB == null){
			Log.e(Config.LOGTAG,"addMarker iDB null");
			return ;
		}
		iDB.beginTransaction();
		try{
			aMarker.regionID = RegionGroup;
			ContentValues cv = new ContentValues();
		
			cv.put(KEY_BookID, aBookID);
			cv.put(KEY_Page, aMarker.page);
			cv.put(KEY_RegionGroup, RegionGroup);
			cv.put(KEY_MarkText,aMarker.getText(null));
			// update if exist
			final String sql_update_marker = KEY_BookID + "=? AND " + KEY_Page + "=" +aMarker.page+" AND "+KEY_RegionGroup+ "="+RegionGroup; 
			final String sql_del_region = KEY_BookID + "=? AND " + KEY_Page +"="+aMarker.page +" AND "+ KEY_RegionGroup + "=" +RegionGroup; 
			
			Log.d(Config.LOGTAG, "update database "+sql_update_marker);
			long iRet = -1;
			if (iDB.update(KEY_MarkerTable, cv, sql_update_marker, new String[]{aBookID}) == 0) {
				//we don't have previously added marker on (page,group)
				iRet = iDB.insert(KEY_MarkerTable, null, cv);
			}else{
				iRet = iDB.delete(KEY_MarkerRegionTable, sql_del_region, new String[]{aBookID});
			}
			if (iRet != -1){
				RegionIterator iter = new RegionIterator(aMarker.markers);			
				Rect r = new Rect();
				ContentValues RegionCV= new ContentValues();
				RegionCV.put(KEY_BookID, aBookID);
				RegionCV.put(KEY_Page, aMarker.page);
				RegionCV.put(KEY_RegionGroup, RegionGroup);
				
				while (iter.next(r)) {
					RegionCV.put(KEY_MarkTop,r.top);
					RegionCV.put(KEY_MarkLeft,r.left);
					RegionCV.put(KEY_MarkBottom,r.bottom);
					RegionCV.put(KEY_MarkRight,r.right);
					iRet = iDB.insert(KEY_MarkerRegionTable, null, RegionCV);
					if (iRet == -1)
						break;
				}
			}
		
			if (-1 != iRet)
				iDB.setTransactionSuccessful();
		} finally {
			iDB.endTransaction();
		}
	}
	/**
	 * remove an Marker 
	 * @param aBookID
	 * @param aMarker
	 * @return
	 */
	public int removeMarker(String aBookID, MarkResult aMarker){
		if(iDB == null){
			Log.e(Config.LOGTAG,"removeMarker iDB null");
			return KErrNotReady;
		}
		iDB.beginTransaction();
		try{
			iDB.delete(KEY_MarkerTable, KEY_BookID+"=? AND "+KEY_Page+"=? AND "+ KEY_RegionGroup+ " =?",
					new String[] {aBookID,String.valueOf(aMarker.page),String.valueOf(aMarker.regionID)});
			
			iDB.delete(KEY_MarkerRegionTable, KEY_BookID+"=? AND "+KEY_Page+"=? AND "+ KEY_RegionGroup+ " =?",
					new String[] {aBookID,String.valueOf(aMarker.page),String.valueOf(aMarker.regionID)});
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			//no matter what happened, we set transaction to successful
			iDB.setTransactionSuccessful();
			iDB.endTransaction();
		}
		return KErrNone;
	}
	public int getMaxRegionGroupID(String aBookID){
		Cursor cur2 = iDB.query(true, KEY_MarkerRegionTable, new String[] {"max("+KEY_RegionGroup+")"},
				KEY_BookID + "=?", new String[]{aBookID},
				null, null, null, null);
		if (cur2 != null){
			if (cur2.moveToFirst()){
				iCurrentMaxRegionGroup = cur2.getInt(0);
			}
			cur2.close();
		}
		return this.iCurrentMaxRegionGroup;
	}
	
	private int markerPageNumber = -1;
	public ArrayList<MarkResult> getMarkers(String aBookID, int page){
		markerPageNumber = page;
		ArrayList<MarkResult> result = getMarkers(aBookID, true);
		markerPageNumber = -1;
		return result;
	}
	/**
	 * get Markers of bookID
	 * @param aBookID
	 * @return
	 */
	public ArrayList<MarkResult> getMarkers(String aBookID, boolean isNeedMarkerRect){
		if(iDB == null){
			Log.e(Config.LOGTAG,"getMarkers iDB null");
			return null;
		}
		ArrayList<MarkResult> aMarkers= new ArrayList<MarkResult>();
		
		Cursor cur;
		if (markerPageNumber > -1) {
			cur = iDB.query(true, KEY_MarkerTable, new String[] { "*" },
					KEY_BookID + "=? AND "+KEY_Page+"=?", new String[] { aBookID,String.valueOf(markerPageNumber) }, null, null,
					KEY_Page + ", " + KEY_RegionGroup, null);
		} else {
			cur = iDB.query(true, KEY_MarkerTable, new String[] { "*" },
					KEY_BookID + "=?", new String[] { aBookID }, null, null,
					KEY_Page + ", " + KEY_RegionGroup, null);
		}
		Log.d(Config.LOGTAG, "total "+cur.getCount()+" items in table "+KEY_MarkerTable);
		if (cur != null) {
			if (cur.getCount() > 0) {
				int MarkPageIdx = cur.getColumnIndex(KEY_Page);
				int MarkTextIdx = cur.getColumnIndex(KEY_MarkText);
				int MarkGroupIdx = cur.getColumnIndex(KEY_RegionGroup);
				if ((-1 != MarkPageIdx && -1 != MarkGroupIdx && -1 != MarkTextIdx)
						&& cur.moveToFirst()) {
					do {
						MarkResult aMarkResult = new MarkResult();
						aMarkResult.page = cur.getInt(MarkPageIdx);
						aMarkResult.regionID = cur.getInt(MarkGroupIdx);
						aMarkResult.setText(cur.getString(MarkTextIdx));
						Cursor curRegion = iDB.query(
								true,
								KEY_MarkerRegionTable,
								new String[] { "*" },
								KEY_BookID + "=? AND " + KEY_Page + " =? AND "
										+ KEY_RegionGroup + "=?",
								new String[] { aBookID,
										String.valueOf(aMarkResult.page),
										String.valueOf(aMarkResult.regionID) },
								null, null, KEY_Page + ", " + KEY_RegionGroup,
								null);
						if (curRegion != null) {
							int MarkTopIdx = curRegion.getColumnIndexOrThrow(KEY_MarkTop);
							int MarkLeftIdx = curRegion.getColumnIndexOrThrow(KEY_MarkLeft);
							int MarkBottomIdx = curRegion.getColumnIndexOrThrow(KEY_MarkBottom);
							int MarkRightIdx = curRegion.getColumnIndexOrThrow(KEY_MarkRight);

							if (isNeedMarkerRect) {
								if (curRegion.moveToFirst()) {
									for (int i = 0; i < curRegion.getCount(); i++) {
										aMarkResult.addMarker(
														curRegion.getInt(MarkLeftIdx),
														curRegion.getInt(MarkTopIdx),
														curRegion.getInt(MarkRightIdx),
														curRegion.getInt(MarkBottomIdx));
										curRegion.moveToNext();
									}
								}
							}

							curRegion.close();
						}

						aMarkers.add(aMarkResult);
						if (cur.isLast()) {
							break;
						} else {
							cur.moveToNext();
						}
					} while (true);
				}
			}
			cur.close();
		}
		

		return aMarkers;
	}
	/**
	 * set markers (backup/restore function)
	 * @param aBookID
	 * @param aMarkResults
	 * @return
	 */
	public int setMarksers(String aBookID, ArrayList<MarkResult> aMarkResults){
		// add bookmarks
		for(int i=0;i<aMarkResults.size();i++){
			addMarker(aBookID, aMarkResults.get(i),i);
		}
			
		return KErrNone;
	}
	public int deleteMarkers(String aBookID){
		if(iDB == null){
			Log.e(Config.LOGTAG,"deleteMarkers iDB null");
			return KErrNotReady;
		}
		// remove from database
		
		iDB.beginTransaction();
		try{
			iDB.delete(KEY_MarkerTable, KEY_BookID+"=? ", new String[] {aBookID});
			
			iDB.delete(KEY_MarkerRegionTable, KEY_BookID+"=? ", new String[] {aBookID});
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			//no matter what happened, we set transaction to successful
			iDB.setTransactionSuccessful();
			iDB.endTransaction();
		}
		
		Log.d(Config.LOGTAG,"delete all Markers of "+aBookID);
		return KErrNone;
	}
	public int deleteAllMarkers(){
		if(iDB == null){
			Log.e(Config.LOGTAG,"deleteMarkers iDB null");
			return KErrNotReady;
		}
		// remove from database
		
		iDB.beginTransaction();
		try{
			iDB.delete(KEY_MarkerTable, null, null);
			
			iDB.delete(KEY_MarkerRegionTable, null, null);
		}catch(Throwable e){
			e.printStackTrace();
		}finally{
			//no matter what happened, we set transaction to successful
			iDB.setTransactionSuccessful();
			iDB.endTransaction();
		}
		
		Log.d(Config.LOGTAG,"delete all Markers");
		return KErrNone;
	}
	
	public int setLastPage(String aBookID, Integer aPage, boolean aSync, Context c,String aDeliverID, String aBookToken){
		if(iDB == null){
			Log.e(Config.LOGTAG,"setLastPage iDB null");
			return KErrNotReady;
		}
		
		Log.d(Config.LOGTAG,"set last page of:"+aBookID);
		GSiLastPage aLastPage = new GSiLastPage(aPage);
		// add into db
		ContentValues cv = new ContentValues();
		cv.put(KEY_BookID, aBookID);
		cv.put(KEY_Page, aLastPage.getPage());
		cv.put(KEY_TimeStamp, aLastPage.getTime());
		// update if exist
		String sql = KEY_BookID + "=?";
		Log.d(Config.LOGTAG, "update database "+sql);
		boolean bOpen = false;
		if(iDB == null){
			this.open();
			bOpen = true;
		}
		if (iDB.update(KEY_LastPageTable, cv, sql, new String[]{aBookID}) == 0) {
			iDB.insert(KEY_LastPageTable, null, cv);
			Log.d(Config.LOGTAG,"insert "+aPage);
		}
		Log.d(Config.LOGTAG,"setLastPage: page="+aPage + ", timestamp="+aLastPage.getTime());
		if(bOpen)
			this.close();
		//handle upload last page
		if(aSync){
			GSiHttpEngine.uploadLastPage(c, aLastPage, aDeliverID ,aBookToken);
		}
		
		return KErrNone;
	}
	public static class GetLastPageResult{
		boolean iHttpResult, bIsDeviceIDEmpty = false;
		int iPage;
		public GetLastPageResult(boolean aResult,int aPage){
			iHttpResult = aResult;
			iPage = aPage;
		}
		public GetLastPageResult(boolean aResult,int aPage,boolean isDevideIDEmpty){
			iHttpResult = aResult;
			iPage = aPage;
			bIsDeviceIDEmpty = isDevideIDEmpty;
		}
		public boolean getHttpResult(){return iHttpResult;}
		public boolean getDeviceIDEmpty(){return bIsDeviceIDEmpty;}
		public int getLastPage(){return iPage;}
	}
	public GetLastPageResult getLastPage(String aBookID,boolean aSync, Context c,String aDeliverID, String aBookToken){
		if(iDB == null){
			Log.e(Config.LOGTAG,"getLastPage iDB null");
			return new GetLastPageResult(false,-1);
		}
		int aPage = -1;
		long aTime = 0;
		Cursor cur = iDB.query(true, KEY_LastPageTable, new String[] { KEY_Page, KEY_TimeStamp},
				KEY_BookID + "=?", new String[]{aBookID},
				null, null, null, "1");
		if (cur != null) {
			if (cur.moveToFirst()) {
				aPage = cur.getInt(0);
				aTime = cur.getLong(1);
				Log.d(Config.LOGTAG, "last page:"+aPage+", time:"+aTime);
			}
			cur.close();
		}
		
		//handle downloaded last page
		GSiLastPage aLastPage = null;
		boolean aHttpResult = false;
		if(aSync){
			aLastPage = GSiHttpEngine.downloadLastPage(c , aDeliverID, aBookToken);
			if(aLastPage.getTime()!=0)
				aHttpResult = true;
				
			if(aLastPage.getTime()>aTime)
				aPage = aLastPage.getPage();
		}
				
		return new GetLastPageResult(aHttpResult,aPage,aLastPage.getDeviceIDEmpty());		
	}
	/**
	 * delete last page
	 */
	public boolean deleteLastPage(String aDeliverID){
		if(iDB == null){
			Log.e(Config.LOGTAG,"deletelastPage iDB null");
			return false;
		}
		int result = iDB.delete(KEY_LastPageTable, KEY_BookID+"=?",new String[] {aDeliverID});
		Log.d(Config.LOGTAG,"delete last page of:"+aDeliverID+" "+result);
		return true;
	}
	public boolean deleteAllLastPage(){
		if(iDB == null){
			Log.e(Config.LOGTAG,"deleteAllLastPage iDB null");
			return false;
		}
		iDB.delete(KEY_LastPageTable, null, null );
		Log.d(Config.LOGTAG,"deleteAllLastPage");
		return true;
	}

	/**
	 * interface for integration
	 */
	public static String getBookmarkXml (Context context, String deliveryID ){
		String aHead = "<bookmarks><bookmark>";
		String aTail = "</bookmark></bookmarks>";
		String aBody = "<page>%s</page>";	
		
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		ArrayList<GSiBookmark> aBookmarks = null;
		aDB.open();
		//get bookmarks
		aBookmarks = aDB.getBookmarks(deliveryID,context,"");
		StringBuilder aSB = new StringBuilder();
		
		aSB.append(aHead);
		for(int i=0;i<aBookmarks.size();i++){
			aSB.append(String.format(aBody, aBookmarks.get(i).iPage));
		}
		aSB.append(aTail);
		
		aDB.close();
		
		return aSB.toString();
	}
	/**
	 * 
	 * @param context: activity context
 	 * @param deliveryID: book deliver id for database key
	 * @param aXml: bookmark xml
	 */

	public static void setBookmark(Context context, String deliveryID , String aXml){
		//parse xml
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		aDB.open();
		DocumentBuilder db;
		Document doc;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = db.parse(new ByteArrayInputStream( aXml.getBytes() ));
			doc.getDocumentElement().normalize();  
			NodeList aChilds = doc.getElementsByTagName("page");
			int aPage = 0;
			for(int i=0;i<aChilds.getLength();i++){
				aPage = Integer.parseInt(aChilds.item(i).getChildNodes().item(0).getNodeValue());
//				String bookmarkText = String.format(context.getString(R.string.GSI_BOOKMARK_TEXT),aPage+1);
				String bookmarkText = "";
				aDB.addBookmark(deliveryID, new GSiBookmark(aPage,bookmarkText));
			}
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		aDB.close();
	}
	
	public static void delBookmark(Context context, String deliveryID ){
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		aDB.open();
		aDB.deleteBookmarks(deliveryID);
		aDB.close();
	}
	public static void delAllBookmark(Context context){
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		aDB.open();
		aDB.deleteAllBookmarks();
		aDB.close();
	}
	//Annotation
	public static String getAnnotationXml (Context context,String deliveryID ){
		String aHead = "<notes>";
		String aTail = "</notes>";
		String aBody = "<note><page>%s</page><content>%s</content></note>";	
		
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		ArrayList<GSiAnnotation> aAnnotations= null;
		aDB.open();
		//get bookmarks
		aAnnotations = aDB.getAnnotations(deliveryID);
		StringBuilder aSB = new StringBuilder();
		
		aSB.append(aHead);
		for(int i=0;i<aAnnotations.size();i++){
			aSB.append(String.format(aBody, aAnnotations.get(i).iPage,aAnnotations.get(i).iAnnotation));
		}
		aSB.append(aTail);
		
		aDB.close();
		
		return aSB.toString();
	}

	public static void setAnnotation (Context context, String deliveryID , String aXml){
		//parse xml
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		aDB.open();
		DocumentBuilder db;
		Document doc;
		try {
			
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = db.parse(new ByteArrayInputStream( aXml.getBytes() ));
			doc.getDocumentElement().normalize();  
			int aPage = 0;
			String aNote = null;
				NodeList aPageChild = doc.getElementsByTagName("page");
				NodeList aNoteChild = doc.getElementsByTagName("content");
			for(int i=0;i<aPageChild.getLength();i++){
				aPage = Integer.parseInt(aPageChild.item(i).getChildNodes().item(0).getNodeValue());
				aNote = aNoteChild.item(i).getChildNodes().item(0).getNodeValue();
				aDB.addAnnotation(deliveryID, new GSiAnnotation(aPage,aNote));
			}
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		aDB.close();
	}

	public static void delAnnotation (Context context, String deliveryID ){
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		aDB.open();
		aDB.deleteAnnotations(deliveryID);
		aDB.close();
	}
	public static void delAllAnnotation (Context context){
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		aDB.open();
		aDB.deleteAllAnnotations();
		aDB.close();
	}
	/**	
	 * interface for integration
	 */
	public static String getMarkerXml (Context context, String deliveryID ){
		String aHead = "<underlines>";
		String aTail = "</underlines>";
		
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		ArrayList<MarkResult> aMarkResults= null;
		aDB.open();
		//get markers 
		aMarkResults = aDB.getMarkers(deliveryID, true);
		StringBuilder aSB = new StringBuilder();
		
		aSB.append(aHead);
		if (aMarkResults != null){
			for(MarkResult markResult: aMarkResults) {
				aSB.append(markResult.toXML());
			}
		}
		aSB.append(aTail);
		
		aDB.close();
		String retStr = Base64.encodeBytes(aSB.toString().getBytes());
		retStr = Uri.encode(retStr);
		return retStr;
	}
	/**
	 * 
	 * @param context: activity context
 	 * @param deliveryID: book deliver id for database key
	 * @param aXml: bookmark xml
	 */

	public static void setMarker(Context context, String deliveryID , String aXmlBase64){
		String aXml = Uri.decode(aXmlBase64);
		try {
			aXml = new String(Base64.decode(aXmlBase64));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//parse xml
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		aDB.open();
		DocumentBuilder db;
		Document doc;
		ArrayList<MarkResult> aMarkers= new ArrayList<MarkResult>();
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = db.parse(new ByteArrayInputStream( aXml.getBytes() ));
			doc.getDocumentElement().normalize();  
			NodeList aMarkerList= doc.getElementsByTagName("underline");
			for(int i=0;i<aMarkerList.getLength();i++){
				MarkResult aMarkResult = new MarkResult();
				NodeList aMarker = aMarkerList.item(i).getChildNodes();
				for (int j=0;j<aMarker.getLength();j++){
					String aElementName = aMarker.item(j).getNodeName();
					if (aElementName.compareToIgnoreCase("text") == 0){
						aMarkResult.setText(aMarker.item(j).getChildNodes().item(0).getNodeValue());
					}else if (aElementName.compareToIgnoreCase("page") == 0){
						aMarkResult.page = Integer.parseInt(aMarker.item(j).getChildNodes().item(0).getNodeValue());
					}else if (aElementName.compareToIgnoreCase("rect") == 0){
						String RectVal = aMarker.item(j).getChildNodes().item(0).getNodeValue();
						String[] aRectStrs = RectVal.split(",");
						if (aRectStrs.length == 4){
							aMarkResult.addMarker(Integer.parseInt(aRectStrs[0]), Integer.parseInt(aRectStrs[1]), Integer.parseInt(aRectStrs[2]), Integer.parseInt(aRectStrs[3]));
						}
					}
				}
				aMarkers.add(aMarkResult);
//				String bookmarkText = "";
//				aDB.addBookmark(deliveryID, new GSiBookmark(aPage,bookmarkText));
			}
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		aDB.setMarksers(deliveryID, aMarkers);
		aDB.close();
	}
	
	public static void delMarker(Context context, String deliveryID ){
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		aDB.open();
		aDB.deleteMarkers(deliveryID);
		aDB.close();
	}
	public static void delAllMarker(Context context){
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(context);
		aDB.open();
		aDB.deleteAllMarkers();
		aDB.close();
	}
	/**	
	 * 刪除某本書最後閱讀頁紀錄(只刪local端)
	 * @param ctx context
	 * @param did deliver id
	 * @return true:無錯誤發生; false: 發生錯誤
	 */
	public static boolean deleteLastPageOfBook(Context ctx, String did){
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(ctx);
		aDB.open();
		boolean result = aDB.deleteLastPage(did);
		aDB.close();
		return result;
	}
	/**
	 * 刪除所有最後閱讀頁
	 * @param ctx context
	 * @return true:無錯誤發生; false: 發生錯誤
	 */
	public static boolean deleteAllLastPage(Context ctx){
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(ctx);
		aDB.open();
		boolean result = aDB.deleteAllLastPage();
		aDB.close();
		return result;
	}
	public static final int MODE_DEFAULT = 0;
	public static final int MODE_LEFT = 1;
	public static final int MODE_RIGHT = 2;
	public static void setSwitchSetting(Activity aCaller, String aDeliverID, int aMode){
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(aCaller);
		aDB.open();
		aDB.setPageSwitch(aDeliverID, aMode);
		aDB.close();
	}
	public static int getSwitchSetting(Activity aCaller,String aDeliverID){
		GSiDatabaseAdapter aDB = new GSiDatabaseAdapter(aCaller);
		aDB.open();
		int result = aDB.getPageSwitch(aDeliverID);
		aDB.close();
		return result;
	}
	
}
