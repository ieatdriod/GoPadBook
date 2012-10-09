package tw.com.soyong.mebook;

import java.io.IOException;

import com.gsimedia.sa.GSiMediaInputStreamProvider;

public class MebookHelper {
	
	public static GSiMediaInputStreamProvider mSC;
	public static TWMMetaData mMeta;	
	public static int mTrackIndex ;							// reocrd current track index
	public static SyContent mContent; 						// all content (chps/sentencs/ps...)
	public static SySentence[] mSentenceArr;
	
	public static MebookData mBookData;						// hold data head for Pic
	
	public static MebookInfo mHeaderInfo;
	
	public static SyInputStream mISSyd;
	public static SyInputStream mISMp3;
	public static int [] mFrameTable;
	
	public static String mDeliverID;
	public static String mP12Folder;
	public static boolean mIsSample;
	public static String mCoverPath;
	public static boolean mIsSyncLastPage;
	public static String mContentID;
	public static String mBookTitle;
	public static String mBookAuthors;
	public static String mBookPublisher;
	public static String mBookCategory;
	
	public static boolean mIsJpBook ;
	
	public static void closeIS(){
		if ( null != mISSyd ){
			try {
				mISSyd.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mISSyd = null;
		}
		
		if ( null != mISMp3){
			try {
				mISMp3.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mISMp3 = null;
		}		
	}
	
	public static void clear(){
		
		//Log.e("MebookHelper" , "clear +++" );
		
		mSC = null ;
		mMeta = null;
		mTrackIndex = 1;
		mContent = null;
		mSentenceArr = null;
		mBookData = null;
		mHeaderInfo = null;
		mFrameTable = null;
		mDeliverID = null;
		mP12Folder = null;
		mCoverPath = null;
		
		if ( null != mISSyd ){
			try {
				mISSyd.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mISSyd = null;
		}
		
		if ( null != mISMp3){
			try {
				mISMp3.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mISMp3 = null;
		}
		
		//Log.e("MebookHelper" , "clear ---" );
	}
}
