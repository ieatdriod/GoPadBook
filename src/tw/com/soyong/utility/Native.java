package tw.com.soyong.utility;

import java.io.IOException;

import tw.com.soyong.mebook.SyInputStream;
import android.util.Log;

import com.gsimedia.sa.GSiMediaInputStreamProvider;


public class Native {
    static {
        System.loadLibrary("native");
    }	

    // for cloze function
	public static native String hideCertainWords(String text , int nHidePercentage, int nMinChars);
	
	// DRM
	public static native boolean open(String path);
	public static native int read(byte[] out); 
	public static native void close();
	public static native void reset();
	
	public static native int getSamplePerSec();
	public static native int getNumChannel();
	
	public static void callback(int depth) {
		
		Log.d("jni-test", "callback:"+depth);
	}	
	
	
	public static GSiMediaInputStreamProvider mSC;
	private static SyInputStream mIs;	
	
	public static boolean InitDRMMp3( GSiMediaInputStreamProvider sc, String title ){
	
		SyInputStream is = new SyInputStream(sc , title , SyInputStream.MODE_MP3 );
		if ( null == is || 0 != is.mOpenState){
			return false ;
		}
		
		mSC = sc ;
		mIs = is ;
		return true;
		
	}
	
	public static void Close() {
		
		if ( null != mIs){
			try {
				mIs.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mIs = null;
		}
		
		Native.close();
	}
	
	/*
	 * Class:     tw_com_soyong_utility_Native
	 * Method:    fat
	 * Signature: (I)[B
	 */	
	// call back from JNI 
	public static  byte [] fatchData( int numByte ) {

		byte [] buf = new byte[numByte];
		try {
			mIs.read(buf);
		} catch (IOException e) {
			
			e.printStackTrace();
			buf = null;
		}
		return buf ;
		
	}
	
	public static void seek( long offset ){
		try {
			
//			byte [] buf = new byte[8];
//			mIs.seek(offset);
//			
//			mIs.read(buf);
			
			mIs.seek(offset);
			Native.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
