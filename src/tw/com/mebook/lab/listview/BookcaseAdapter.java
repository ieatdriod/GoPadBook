package tw.com.mebook.lab.listview;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.List;

import org.iii.ideas.reader.annotation.AnnotationDB;
import org.iii.ideas.reader.bookmark.Bookmarks;
import org.iii.ideas.reader.last_page.LastPageHelper;
import org.iii.ideas.reader.underline.UnderlineDB;

import tw.com.mebook.util.BookDownloader;
import tw.com.mebook.util.ImageDownloader;
import tw.com.soyong.AnReader;
import tw.com.soyong.utility.SyBookmark;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.taiwanmobile.myBook_PAD.TWMImageView;

import com.taiwanmobile.myBook_PAD.R;
import com.taiwanmobile.myBook_PAD.RealBookcase;
import com.taiwanmobile.myBook_PAD.TWMDB;

public class BookcaseAdapter extends BaseAdapter {
	
	private static final boolean DEBUG = true ;
	private static final String TAG = "BookcaseAdapter";
	
	private static final int BOOK_PER_CASE = 5;
	private static final int INIT_BOOKCASE_NUM = 10;//6
	private final ImageDownloader mImageDownloader ;
	private final BookDownloader mBookDownloader ;
	
	private Hashtable<String,Bitmap> mDefBmpCache = new Hashtable<String,Bitmap>();
	
	private WeakReference<Activity> mContext;
	private int mBrandCount;
	private int mBrandCase;
	private int mCaseCount;
	
	LayoutInflater mInflater;
	public BookcaseAdapter(Activity context) {
		mContext = new WeakReference<Activity> (context);
		mInflater = LayoutInflater.from(context);
		mImageDownloader = new ImageDownloader();
		mBookDownloader = new BookDownloader(context);
		
		// cache bitmap
		final String strTypes[] = {".teb", ".tvb" , ".tpb" , "brand"};
		final int resIDs[] = {R.drawable.ivi_nonepict01,R.drawable.ivi_nonepict02 , R.drawable.ivi_nonepict03 , R.drawable.ivi_nonepict04 };
		final Hashtable<String,Bitmap> defBmpCache = mDefBmpCache;
		Resources resource=context.getResources();  
		   
		int i = 0 ;
		for (String type : strTypes ){
			defBmpCache.put(type, BitmapFactory.decodeResource(resource, resIDs[i]));
			++i;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////
	public int getCount() {
		
		calCaseCount();
		return mCaseCount ;
	}

	public Object getItem(int pos) {
		return null;
	}

	public long getItemId(int pos) {
		return pos;
	}

	public View getView(int pos, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		if ( null == convertView){
			if(DEBUG) Log.w(TAG, "getView:"+pos +" convertView == null");
			
			convertView = mInflater.inflate(R.layout.listitem, parent , false);

			holder = new ViewHolder();
			
			final int ids[] = {R.id.bookInfo1, R.id.bookInfo2, R.id.bookInfo3, R.id.bookInfo4 , R.id.bookInfo5};
			View view;
			for ( int i = BOOK_PER_CASE-1 ; i >= 0 ;--i ){
				view = convertView.findViewById(ids[i]);
				holder.infos[i].view = view;
				holder.infos[i].cover = (TWMImageView) view.findViewById(R.id.cover);
				holder.infos[i].type = (ImageView) view.findViewById(R.id.type);
				holder.infos[i].delIcon = (ImageView) view.findViewById(R.id.deleteIcon);
				holder.infos[i].prog = (ProgressBar) view.findViewById(R.id.progress);
				
				view.setOnClickListener(mClick);
				view.setOnLongClickListener(mLongClick);
			}
			convertView.setTag(holder);
		}
		else {
			if(DEBUG) Log.e(TAG, "reuse getView:"+pos);
			holder = (ViewHolder) convertView.getTag();
		}
		
		// setup data
		final int [] bgs = {R.drawable.bg01,R.drawable.bg02,R.drawable.bg03};
		convertView.setBackgroundResource(bgs[pos%3]);
		
		int index = pos * BOOK_PER_CASE;
		if ( pos < mBrandCase) {
			setBrandView(holder , index);
		} else {
			index = (pos-mBrandCase) * BOOK_PER_CASE;
			setBookCaseView(holder , index);
		}
	
		return convertView;
	}
	
	public void onPause(){
		mBookDownloader.cancelAll();
	}
	
	private void setBookCaseView(ViewHolder holder, int index) {
		
		final ImageDownloader downloader = this.mImageDownloader;
		final String serverPath = mCoverServerPath;
		final boolean isEdit = mIsEdit;
		
		int id;
		ImageView imgView;
		String url;
		final Cursor c = mCursor;
		final int max = c.getCount();
		
		for (int i = 0; i < BOOK_PER_CASE; ++i) {
			id = index + i;

			holder.infos[i].view.setVisibility(View.INVISIBLE);

			if (id >= max) {
				continue;
			}
			c.moveToPosition(id);

			// view
			holder.infos[i].view.setVisibility(View.VISIBLE);
			holder.infos[i].view.setTag(null);
			holder.infos[i].view.setTag(id);

			// cover image
			url = c.getString(4);
			imgView = holder.infos[i].cover;
			
			imgView.getDrawable().setCallback(null);
			imgView.setImageDrawable(null);
			
			downloader.setCursor(c);
			downloader.download(serverPath + url, imgView , getDefBitmap(id));
					
			// downloaded
			int state = c.getShort(8); //Integer.parseInt( c.getString(8).toString() );
			if ( /*1 == state &&*/ isFileExist(c.getString(19) )){
				imgView.setAlpha(255);
			} else {
				imgView.setAlpha(100);
			}
			
			// dl progress
			final ProgressBar prog = holder.infos[i].prog;
			
			if (false == isEdit) {
				if (DL_STATE.DLING == mDLStates[id]) {
					prog.setVisibility(View.VISIBLE);
					prog.setProgress(mProgs[id]);
				} else {
					prog.setVisibility(View.INVISIBLE);
				}
				holder.infos[i].delIcon.setVisibility(View.INVISIBLE);
			} else {
				prog.setVisibility(View.INVISIBLE);
				
				holder.infos[i].delIcon.setVisibility(View.VISIBLE);
			}
			
			// type
			imgView = holder.infos[i].type;
			imgView.setVisibility(View.INVISIBLE);
			int trail = c.getShort(13); //Integer.parseInt(c.getString(13));
			setBookTrailType(imgView , trail);
		}
	}
	
	private void setBookTrailType(ImageView imgView, int trail) {
		
		switch( trail ){
		case 1:
			imgView.setImageResource(R.drawable.ivi_icon01);
			imgView.setVisibility(View.VISIBLE);
			break;
		case 3:
		case 4:
			imgView.setImageResource(R.drawable.ivi_icon03);
			imgView.setVisibility(View.VISIBLE);
			break;
			
		default:
			imgView.setImageDrawable(null);
			break;
		}
	}

	private void setBrandView(ViewHolder holder, int index) {

		ImageView imgView;
		int id;
		final int max = mBrandCount;
		final ImageDownloader downloader = mImageDownloader;
		for (int i = 0; i < BOOK_PER_CASE; ++i) {
			id = index + i;

			holder.infos[i].view.setVisibility(View.INVISIBLE);
			holder.infos[i].delIcon.setVisibility(View.INVISIBLE);
			holder.infos[i].type.setVisibility(View.INVISIBLE);
			holder.infos[i].prog.setVisibility(View.INVISIBLE);

			if (id >= max) {
				continue;
			}

			holder.infos[i].view.setVisibility(View.VISIBLE);
			holder.infos[i].view.setTag(null);
			holder.infos[i].view.setTag(-id - 1);

			imgView = holder.infos[i].cover;
			imgView.setAlpha(255);
			
			downloader.download(mBrandPIC1s.get(i), imgView, mDefBmpCache.get("brand"));
		}
	}

	public static boolean isIntentAvailable(Context context, String url) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(Intent.ACTION_VIEW , Uri.parse(url));
	    List<ResolveInfo> list =
	            packageManager.queryIntentActivities(intent,
	                    PackageManager.MATCH_DEFAULT_ONLY);
	    return list.size() > 0;
	}


	private Bitmap getDefBitmap(int index) {
		String type = mCursor.getString(21);
		final Hashtable<String,Bitmap> defBmpCache = mDefBmpCache;
		return defBmpCache.get(type);
	}
	
	
	private OnLongClickListener mLongClick = new OnLongClickListener(){

		@Override
		public boolean onLongClick(View v) {
			Integer index = (Integer) v.getTag();
			
			if(DEBUG) Log.d(TAG, "onClick id:"+index);
			if ( index < 0 ){
				return false ;
			}
			
			final Cursor c = mCursor;
			c.moveToPosition(index);
			final String id = c.getString(5);
			final String bookType = c.getString(21);
			final String contentId = c.getString(15);
			final int idx = index ;
			
			final Context ctx = mContext.get();
			if (ctx == null)
				return false;
			String[] item = {ctx.getResources().getString(R.string.iii_long_click_del),ctx.getResources().getString(R.string.iii_long_click_score)};
			new AlertDialog.Builder(ctx)
			  .setItems(item, 
				  new DialogInterface.OnClickListener(){
					 public void onClick(DialogInterface dialog, int whichcountry) {
							 switch(whichcountry) {
								 case 0:	
										new AlertDialog.Builder(ctx)
										.setTitle(R.string.iii_check_del_book)
										.setMessage(R.string.iii_check_del_message)
										.setPositiveButton(R.string.iii_showAM_ok,
												new DialogInterface.OnClickListener(){
													public void onClick(DialogInterface dialoginterface, int i){												      	  		
														mDb.deleteByDid(id);

														final String coverPath = c.getString(9);
														final String bookPath = c.getString(19);
									  	  				new File(coverPath).delete();		//本機圖片位置    
									  	  				new File(bookPath).delete();		//書的路徑
									  	  				new File(bookPath+".tmp").delete();
									  	  				new File(bookPath.substring(0, bookPath.lastIndexOf(".")) + ".epub").delete();

														delDeviceBook(bookType, id);
														removeData(idx);
														
														c.requery();
														c.moveToFirst();
														BookcaseAdapter.this.notifyDataSetChanged();
													}
												}
										)
										.setNegativeButton(R.string.iii_showAM_cancel,
												new DialogInterface.OnClickListener(){
													public void onClick(DialogInterface dialoginterface, int i){
													}
											}
										)				
										.show(); 
									 break;								 
								 case 1:
									 ctx.startActivity((
											 new Intent()).setAction(Intent.ACTION_VIEW)
											 			  .setData(Uri.parse(ctx.getResources().getString(R.string.iii_book_score_url)+contentId)));
									 break;							 
							 }						
					 }				 
				  }
			  ).setCancelable(true)
		      .show();	
			
			
			
			return false;
		}
		
	};
	
	private OnClickListener mClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			Integer index = (Integer) v.getTag();
			
			if(DEBUG) Log.d(TAG, "onClick id:"+index);
			
			final Context ctx = mContext.get();
			if (ctx == null)
				return;
			
			if ( index < 0 ){
				// brand
				index = -index-1;
				
				String url = mBrandURLs.get(index);
				boolean avaliable = isIntentAvailable(ctx , url);
				if ( !avaliable ){
					url = mBrandFailURLs.get(index);
				}
				
				ctx.startActivity((new Intent()).setAction(
						Intent.ACTION_VIEW).setData(Uri.parse(url)));
			}else {
				// boooks

				SharedPreferences pref = ctx.getSharedPreferences("DL_BOOK", Context.MODE_PRIVATE);

				final Cursor c = mCursor;
				c.moveToPosition(index);
				final String id = c.getString(5);
				final String bookType = c.getString(21);
				
				if (false == mIsEdit) {
					String ss = c.getString(8);
					int ss2 = c.getShort(8);
					boolean bookfound = isFileExist(mDlPath+id+bookType);
					
					//if ( !(c.getShort(8) == 1 && isFileExist(mDlPath+id+bookType))) {
					if ( ! isFileExist(mDlPath+id+bookType) ){
						mBookDownloader.download(id,bookType, v, index);
					} else {
						boolean isSyncLastPage = ctx.getSharedPreferences("setting_Preference", 0).getBoolean("setting_auto_sync_last_read_page_value", true);
				 		
						if ( null != mDb ){
				 			mDb.updateByDeliveryId(id , "lastreadtime", String.valueOf(System.currentTimeMillis()));
				 			mDb.updateByDeliveryId(id , "isread" , "1");
				 			c.requery();
				 			c.moveToPosition(index);
				 		}
						
						openBook( "non used" ,
								mDlPath+id+bookType ,
								ctx.getFilesDir().toString(), 
								c.getString(13), //mTrails.get(index) ,
								c.getString(9) ,
								isSyncLastPage , 
								c.getString(15) ,
								c.getString(1) ,
								c.getString(12) , 
								c.getString(11) , 
								c.getString(3), 
								c.getString(17),
								bookType);
					}
				} else {
					
					final int idx = index ;
					new AlertDialog.Builder(ctx)
					.setTitle(R.string.iii_check_del_book)
					.setMessage(R.string.iii_check_del_message)
					.setPositiveButton(R.string.iii_showAM_ok,
							new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialoginterface, int i){												      	  		
									mDb.deleteByDid(id);

									final String coverPath = c.getString(9);
									final String bookPath = c.getString(19);
				  	  				new File(coverPath).delete();		//本機圖片位置    
				  	  				new File(bookPath).delete();		//書的路徑
				  	  				new File(bookPath+".tmp").delete();
				  	  				new File(bookPath.substring(0, bookPath.lastIndexOf(".")) + ".epub").delete();

									delDeviceBook(bookType, id);
									removeData(idx);
									
									c.requery();
									c.moveToFirst();
									BookcaseAdapter.this.notifyDataSetChanged();
								}
							}
					)
					.setNegativeButton(R.string.iii_showAM_cancel,
							new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialoginterface, int i){
								}
						}
					)				
					.show(); 
				}
				
			}

		}
	};
	
	private boolean isFileExist(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}
	
	
	/**
	 * 開啟書本
	 * @param type 有書書  或 epub
	 * @param bookName 檔案路徑
	 * @param p1 p12 p12路徑
	 * @param p2 isSample 是否試閱
	 * @param p3 coverPath 圖片路徑
	 * @param p4 syncLastPage 同步最後閱讀頁
	 * @param p5 content_id 書本content id
	 * @param p6 book_title 書本名稱
	 * @param p7 book_authors 書本作者
	 * @param p8 book_publisher 書本出版社
	 * @param p9 book_category 書本類別
	 * @param p10 book_vertical 書本是否垂直觀看
	 */
	
	private void openBook(String type,String bookName,String p1,String p2,String p3,boolean p4,String p5,String p6,
			String p7,String p8,String p9,String p10, String p11) {
		String[] fileType = {".teb",".tvb",".tpb"};
		
		final Context ctx = mContext.get();
		if (ctx == null)
			return ;
		
		Intent it = null;
		if(p11.equals(fileType[1])){
 			it= new Intent(Intent.ACTION_VIEW , Uri.parse("mebook://"+bookName));//(mCtx,AnReader.class);
		}else if(p11.equals(fileType[2])) {
			//PDF
			it= new Intent(Intent.ACTION_VIEW, Uri.parse("pdf://"+bookName));//(mCtx,RendererActivity.class);
 		}else{
 			it= new Intent(Intent.ACTION_VIEW,Uri.parse("epub://"+bookName));//(mCtx,Reader.class);
 		} 		
		//110420 add to make sure call this app
		it.setPackage("com.taiwanmobile.myBook_PAD");
		
 		it.putExtra("p12",p1);
 		it.putExtra("isSample", p2);
 		it.putExtra("coverPath", p3);
 		it.putExtra("syncLastPage", p4);//同步最後閱讀頁
 		it.putExtra("content_id", p5);
 		it.putExtra("book_title", p6);
 		it.putExtra("book_authors", p7);
 		it.putExtra("book_publisher", p8);
 		it.putExtra("book_category", p9);
 		if(p10.equals("0"))
 			it.putExtra("book_vertical", false);
 		else
 			it.putExtra("book_vertical", true);
 		it.putExtra("book_token", RealBookcase.getToken());
 		ctx.startActivity(it);	
	}
		

	private void removeData(int index) {

//		mDeliverIDs.remove(index);
//		mBookTypes.remove(index);
//		mCoverUrls.remove(index);
		
		calCaseCount();	
	}

	/**
	 * 刪除書本
	 * @param type 書本種類
	 * @param deliveryid deliveryid
	 */
    private void delDeviceBook(String type,String deliveryid) {	
    	Log.e(type, deliveryid);

		final Context ctx = mContext.get();
		if (ctx == null)
			return ;
		
    	if(type.equals(ctx.getResources().getString(R.string.iii_mebook))){
 			new File(mDlPath + deliveryid + ".tvb").delete();
			SyBookmark bm = new SyBookmark(ctx, 0);
			bm.delBookmark(deliveryid);
			
			AnReader.deleteLastPageOfBook(ctx, deliveryid);
 		}
    	
	    AnnotationDB adb = new AnnotationDB(ctx);
	    adb.deleteAnnByEpubPath(deliveryid);
	    adb.closeDB();
	    Bookmarks bm= new Bookmarks(ctx);
	    bm.deleteBookmarksByEpubPath(deliveryid);
	    bm.closeDB();
	    UnderlineDB ul= new UnderlineDB(ctx);
	    ul.deleteUnderlineByEpubPath(deliveryid);
	    ul.closeDB();
	    LastPageHelper.deleteLastPageOfBook(ctx, deliveryid);
	}
	
	
	////////////////////////////////////////////////////
	static public enum DL_STATE {NOACT,DLING,DLED};

//	private List<String> mDeliverIDs;
//	private List<String> mBookTypes;
//	private List<String> mTrails;
//	private List<String> mContentIds;
	private TWMDB mDb;
	private String mCoverServerPath;
//	private List<String> mCoverUrls;
	private String mDlPath;
	private String mDeviceID;
	private String mBookServerPath;	
	
	private DL_STATE [] mDLStates;	// 0:no act, 1:downloading, 2:downloaded
	private int [] mProgs;
	private List<String> mBrandNames;
	private List<String> mBrandPIC1s;
	private List<String> mBrandURLs;
	private List<String> mBrandFailURLs;
	private boolean mIsEdit = false;
	private Cursor mCursor;
	
	public void setDlPath(String dlPath) {
		this.mDlPath = dlPath;
	}

	public void setDeviceID(String deviceID) {
		this.mDeviceID = deviceID;
	}

	public void setCoverData(String serverPath /*, List<String> coverUrls,
			List<String> coverLocalPaths*/) {
		
		this.mCoverServerPath = serverPath;
		//this.mCoverUrls = coverUrls;
		
		final ImageDownloader downloader = mImageDownloader;
		
		downloader.setMode(ImageDownloader.Mode.CORRECT);
		downloader.setDlPath(mDlPath);
		downloader.setDb(mDb);
	}

	public void setBookData(String serverPath /*, List<String> deliverIDs, List<String> bookTypes , List<String> trails , List<String> contentIds*/) {
		this.mBookServerPath = serverPath;
//		this.mDeliverIDs = deliverIDs;
//		this.mBookTypes = bookTypes;	
//		this.mTrails= trails;
//		this.mContentIds = contentIds;
		
		final BookDownloader  downloader = mBookDownloader;
		downloader.setDLUrl(serverPath);
		downloader.setDLPath(mDlPath);
		downloader.setDeviceID(mDeviceID);
		downloader.setDb(mDb, mCursor);
		downloader.setAdapter(this);
		
		final int count = mCursor.getCount() ; //deliverIDs.size(); 
		
		mDLStates = new DL_STATE[count];
		mProgs = new int[count];
		downloader.setOutputData(mDLStates ,mProgs );
		
	}

    protected MyDataSetObserver curosr_observer;
	public void setDB(TWMDB tdb) {
		this.mDb = tdb ;
		this.mCursor = tdb.select3();
	
		if(curosr_observer == null)
			return;
		
		curosr_observer = new MyDataSetObserver();
        mCursor.registerDataSetObserver(curosr_observer);
	}
	
	
	
	///////////////////////////////////////////////////////
	static class InofView {
		View view;
		ImageView cover;
		ImageView type;
		ImageView delIcon;
		ProgressBar prog;
	};
	
	
	static class ViewHolder {
		InofView [] infos;

		ViewHolder(){
			infos = new InofView[BOOK_PER_CASE];
			
			for ( int i = BOOK_PER_CASE-1 ; i >= 0 ; i--){
				infos[i] = new InofView();
			}
		}
	}


	public void setBrandData(List<String> brandNames,
			List<String> brandPic1s, List<String> brandURLs,
			List<String> brandFailURLs) {

		if ( null == brandNames || null == brandPic1s ||
			 null == brandURLs || null == brandFailURLs){
			return ;
		}
		
    	this.mBrandNames = brandNames;
    	this.mBrandPIC1s = brandPic1s;
    	this.mBrandURLs = brandURLs;
    	this.mBrandFailURLs = brandFailURLs;	
    	
		calCaseCount();		
	}

	private int iHeight = 0;
	public void setHeight(int height){
		iHeight = height;
	}
	private int mBookCount =-1 ;
	private void calCaseCount() {
		// for book
		int totalbooks = mCursor.getCount() ; //mDeliverIDs.size();
		
		if ( -1!= mBookCount && mBookCount == totalbooks ){
			return ;
		}
		
		mBookCount = totalbooks;
		int caseCount = totalbooks/BOOK_PER_CASE;
		if ( totalbooks%BOOK_PER_CASE > 0  ){
			caseCount ++;
		}
		
		// for brand
		int brandCase;
		int brandCount =mBrandURLs.size();
		brandCase = brandCount/BOOK_PER_CASE;
		if ( brandCount%BOOK_PER_CASE > 0 ){
			brandCase ++;
		}
		mBrandCase = brandCase;
		mBrandCount = brandCount;
		
		caseCount+=brandCase; 
		
		/**
		 * to cont empty case
		 */
		int caseHeight = Dip2Px(mContext.get(),128); //px
		int fullcase = (iHeight/caseHeight)+((iHeight%caseHeight>0)?1:0);
		if ( caseCount < fullcase){
			caseCount = fullcase;
		}
		
		/*if ( caseCount < INIT_BOOKCASE_NUM ){
			caseCount = INIT_BOOKCASE_NUM;
		}*/
		
		mCaseCount = caseCount;
	}

	public void setEditMode(boolean b) {
		this.mIsEdit  = b ;
		this.notifyDataSetChanged();
	};
	
	
	private class MyDataSetObserver extends DataSetObserver {
	    public void onChanged(){
	        Log.e("345", "CHANGED CURSOR!");
	    }
	    public void onInvalidated(){
	        Log.e("345", "INVALIDATED CURSOR!");
	        if(mCursor == null || mCursor.isClosed()){
	        	//reopen DB
	        	mCursor = mDb.select3();
	        	if(mCursor == null || mCursor.isClosed()){
	        		System.exit(0);
	        		return;
	        	}
	        	mCursor.moveToFirst();
	        }
//	        mCursor.requery();
//	        mCursor.moveToFirst();
	    }
	}
	
	/**
	 * convert dip to pix
	 * @param c context 
	 * @param aDip
	 * @return
	 */
	public static int Dip2Px(Context c,int aDip){
		Resources r = c.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, aDip, r.getDisplayMetrics());
		return (int)px;
	}
	private ImageView.ScaleType getCoverScaleType(){
		Display display = ((WindowManager)(mContext.get().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay(); 
		int width = display.getWidth()/BOOK_PER_CASE; 
		if(width<BookcaseAdapter.Dip2Px(mContext.get(),111)){
			return ImageView.ScaleType.FIT_END;
		}else{
			return ImageView.ScaleType.FIT_CENTER;
		}
	}

}
