package tw.com.soyong.utility;


import java.io.File;

import com.taiwanmobile.myBook_PAD.R;

import android.app.Activity;
import android.content.Context;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;

public class Util {
	
	static final public String TAG = "Util";
	static final private float K_SCALE_RATIO = (float) 1.5;
	
	static public final String formatTimeString(int time) {
		int hh, mm, ss, ff;
		hh = time / 3600000;
		time = time % 3600000;

		mm = time / 60000;
		time = time % 60000;

		ss = time / 1000;
		ff = time % 1000;
		ff /= 100;

		String str;
		if (hh > 0) {
			str = String.format("%d:%02d:%02d", hh, mm, ss);
		} else {
			str = String.format("%02d:%02d", mm, ss);
		}
		return str;
	}	
	
	static public final void replaceKK(StringBuilder sb) {
		
		final String str = sb.toString();
		int kkBegin = str.indexOf('[');
		if ( -1 == kkBegin){
			return ;
		}
		
		int kkEnd = str.indexOf(']');
		if ( -1 == kkEnd || kkBegin > kkEnd){
			return ;
		}
		kkBegin++;
		String phone = str.substring(kkBegin , kkEnd);
		phone = ReplacePhonogramChars(phone);
		sb.replace(kkBegin, kkEnd, phone);
	}
	
	static public final boolean removePhonetic(StringBuilder sb) {
		
		final String str = sb.toString();
		
		int index , start , end ;
		
		boolean ret = false ;
		index = str.lastIndexOf(']');
		while( index > -1){
			end = index+1 ;
			index = str.lastIndexOf('[' , index);
			if ( index > -1){
				start = index ;
				
				if ( end > start){
					sb.delete(start, end);
					ret = true ;
				}else{
					Log.e(TAG, "tag mismatch!");
					ret = false ;
					break;
				}
			}
			index = str.lastIndexOf(']',index);
		}
		return ret ;
	}	


	static public final void replaceCRLF(final String str, StringBuilder sb) {
		
		if ( null == str){
			return ;
		}
		
		int index=str.length()-1 ;
		while ( (index = sb.lastIndexOf("\n" , index)) >= 0 ){
			sb.replace(index-1, index+1, "<br>");
			index-=2;
		}
	}	
	
	static public final void removeFontTag(final String str , StringBuilder sb){
		if ( null == str){
			return ;
		}
		
		int index ; //=str.length()-1 ;
		while ( (index = sb.lastIndexOf("</h>" )) >= 0 ){
			sb.replace(index, index+4, "");
		}
		while ( (index = sb.lastIndexOf("<h>" )) >= 0 ){
			sb.replace(index, index+3, "");
		}		
		while ( (index = sb.lastIndexOf("</b>")) >= 0 ){
			sb.replace(index, index+4, "");
		}		
		while ( (index = sb.lastIndexOf("<b>" )) >= 0 ){
			sb.replace(index, index+3, "");
		}		
		while ( (index = sb.lastIndexOf("</u>" )) >= 0 ){
			sb.replace(index, index+4, "");
		}		
		while ( (index = sb.lastIndexOf("<u>" )) >= 0 ){
			sb.replace(index, index+3, "");
		}		
		while ( (index = sb.lastIndexOf("</i>" )) >= 0 ){
			sb.replace(index, index+4, "");
		}		
		while ( (index = sb.lastIndexOf("<i>" )) >= 0 ){
			sb.replace(index, index+3, "");
		}
		
		while ( (index = sb.lastIndexOf("</H>" )) >= 0 ){
			sb.replace(index, index+4, "");
		}
		while ( (index = sb.lastIndexOf("<H>" )) >= 0 ){
			sb.replace(index, index+3, "");
		}		
		while ( (index = sb.lastIndexOf("</B>")) >= 0 ){
			sb.replace(index, index+4, "");
		}		
		while ( (index = sb.lastIndexOf("<B>" )) >= 0 ){
			sb.replace(index, index+3, "");
		}		
		while ( (index = sb.lastIndexOf("</U>" )) >= 0 ){
			sb.replace(index, index+4, "");
		}		
		while ( (index = sb.lastIndexOf("<U>" )) >= 0 ){
			sb.replace(index, index+3, "");
		}		
		while ( (index = sb.lastIndexOf("</I>" )) >= 0 ){
			sb.replace(index, index+4, "");
		}		
		while ( (index = sb.lastIndexOf("<I>" )) >= 0 ){
			sb.replace(index, index+3, "");
		}		
		
	}
	
	static public final void replaceFontTag(StringBuilder sb){
		
		final String mHltColorBegin = "<font color=\"#0000ff\">";
		final String mColorEnd = "</font>";
		
		int index = sb.lastIndexOf("</h>");
		while( index >=0 ){
			//sb.insert(index, mColorEnd);
			sb.replace(index, index+4, mColorEnd);
			index = sb.lastIndexOf("<h>" , index);
			if ( index >= 0 ){
				//sb.insert(index, mHltColorBegin);
				sb.replace(index, index+3, mHltColorBegin);
				
				index = sb.lastIndexOf("</h>");
			}
		}
		
	}
	
	
	private static final char [] mPhoneMapKK = {
		//  0   1   2   3   4   5   6   7   8   9   a   b   c   d   e   f
		   0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 0
		   0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 1
		   0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 2 
		   0, 0, 0x1E43, 0x1E47, 0, 0x02C8, 0, 0x02CC, 0, 0, 0, 0, 0, 0, 0, 0, // 3
		   0, 0x00E6, 0x0251, 0x0252, 0x259, 0x0259, 0x0283, 0, 0, 0x026A, 0x28A, 0, 0x025A, 0, 0x014B, 0, // 4
		   0, 0x028C, 0x0254, 0, 0x00F0, 0x028A, 0x0292, 0x03B8, 0, 0, 0x025B, 0x025D, 0x25C, 231, 'g', 0, // 5
		   0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 6
		   0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x1E37, 0, 0, 0  // 7
	};
	

	static public final String ReplacePhonogramChars(String phone) {
		char symbol;
		char chWide;
		final int nCount = phone.length();

		final char[] phoneArr = phone.toCharArray();
		StringBuilder sb = new StringBuilder(phone);

		for (int k = 0; k < nCount; k++) {
			chWide = phoneArr[k];

			if (chWide >= 256) {
				continue;
			}

			symbol = mPhoneMapKK[(int)chWide];
			if (0 == symbol) {
				continue; // no replace.
			}
			
			sb.deleteCharAt(k);
			sb.insert(k, symbol);
		}
		return sb.toString();
	}	
	
	static public final boolean isAlphabet(char c){
        boolean ret = false ;

        if (c >= 'a' && c <= 'z' ||
            c >= 'A' && c <= 'Z' ||
            c == '-' || c == '\'') {
            ret = true ;
        }
        return ret ;
    }
	
	static public final int getWord( String str , int index ){
		
		int count = str.length();
		int ret = count ;
		int i ;
		for (i = index ; i < count ; i++){
			if ( false == isAlphabet( str.charAt(i)) ){
				ret = i ;
				break;
			}
		}
		return ret;
	}
	
	public static String[] getScaledFontArray(Context ctx) {
		String [] fonts = ctx.getResources().getStringArray(R.array.font);
        DisplayMetrics dm = new DisplayMetrics();
        dm = ctx.getApplicationContext().getResources().getDisplayMetrics();
        float scaledDensity = dm.scaledDensity;
        
		final int count = fonts.length;  
		for (int i=0;i<count;i++){
			fonts[i] = String.valueOf(Float.parseFloat(fonts[i])/(scaledDensity/K_SCALE_RATIO));
		}
		return fonts;
	}
	
	private static float [] mFontSizeTbl = null;//{9,10,11,12,13,14,15,16,17,18,20,22,24};
	
	public static final  float getFontSizeFromPref(int fontType, Context ctx) {
		float fontSize;
		
		if (null == mFontSizeTbl){
			String [] Fonts= getScaledFontArray(ctx);
			mFontSizeTbl = new float[Fonts.length];
			for (int i=0;i<mFontSizeTbl.length;i++)
				mFontSizeTbl[i] = Float.parseFloat(Fonts[i]);
		}
		
		if ( -1 == fontType ){
			fontType = mFontSizeTbl.length/2;
		}
		
		fontSize = 	mFontSizeTbl[fontType];
		return fontSize;
	}	
	
	static public final  boolean isPadResolution(Activity act){
		DisplayMetrics metrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float screen_height = 0;
		float screen_width = 0;
		//screen_height = (int) (metrics.heightPixels /* * metrics.density*/);
		//screen_width = (int) (metrics.widthPixels /* * metrics.density*/);
		screen_height = Math.max(metrics.heightPixels, metrics.widthPixels);
		screen_width = Math.min(metrics.heightPixels, metrics.widthPixels);
		
		
		if (screen_height < 1023 && screen_width < 599)// 1024x600
		{
			return false ;
		}
		return true ;
	}
	
	static public final boolean isBigPad(Activity act){
		boolean big = true;
                DisplayMetrics metrics = new DisplayMetrics();
                act.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float screen_height = 0;
		float screen_width = 0;
		screen_height = Math.max(metrics.heightPixels, metrics.widthPixels);
		screen_width = Math.min(metrics.heightPixels, metrics.widthPixels);
		if(screen_width == 600 && screen_height == 1024)
			big = false;
	
		return big;
	}
	static public String getStorePath(Context ctx){	
		File store = ctx.getExternalFilesDir(null);
		String path = null ;
		if ( null != store){
		
		  File storeage = new File(ctx.getExternalFilesDir(null), "twmebook");
		  path = storeage.getPath() + "/";
		  storeage.mkdir();
		  storeage = null;
		}else {
			path = ctx.getFilesDir().toString()+"/";
		}
		
		StatFs stat = new StatFs(path);
		calFreeSize(stat);
		
		return path ;
	}
	
	/**
	 * 重新計算剩餘空間
	 */
	static public long calFreeSize(StatFs stat) {
		int size = stat.getBlockSize();
		int num = stat.getAvailableBlocks();
		return (long)num * size ;
	}	
	
}
