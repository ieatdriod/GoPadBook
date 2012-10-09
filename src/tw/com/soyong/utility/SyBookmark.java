package tw.com.soyong.utility;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SyBookmark {
	
	private static boolean DEBUG = false ;
	private static final String TAG = "SyBookmark";
	
	private BitSet mBookmark;
	SharedPreferences mPref ;

	public SyBookmark( Context context , int totalSentences) {
		if ( totalSentences < 64){
			mBookmark = new BitSet();
		} else {
			mBookmark = new BitSet( totalSentences );
		}
		
		mPref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
	}
	
	public int getLength(){
		return mBookmark.length();
	}
	
	public void clear(){
		mBookmark.clear();
	}
	
	public void set( final int index){
		mBookmark.set(index);
	}
	
	public void clear( final int index){
		mBookmark.clear(index);
	}
	
	public int getNext( final int cur){
		BitSet bs = mBookmark;
		
		int index = bs.nextSetBit(cur+1);
		if ( -1 == index ){
			index = bs.nextSetBit(0);	// get first
		}
		return index;
	}
	
	public int getPrev( final int cur){
		
		int index = -1;
		
		final BitSet bs = mBookmark ; 
		for ( int i = cur -1 ; i >= 0 ; i--){
			if ( true == bs.get(i) ){
				index = i ;
				break ;
			}
		}
		
		if ( -1 == index && bs.length()!= cur){
			index = getPrev(bs.length());
		}
		
		return index ;
	}
	
	public int getFirst(){
		return mBookmark.nextSetBit(0);
	}
	
	public int getLast(){
		if ( DEBUG ) Log.d(TAG , "length:"+ mBookmark.length() + " size:"+mBookmark.size() );
		
		return getPrev(mBookmark.length());
	}
	
	public boolean isMarked( int index){
		final BitSet bs = mBookmark ;
		return bs.get(index);
	} 
	
	public void load(String bookTitle){
		SharedPreferences pref = mPref ;
		String str = pref.getString(bookTitle , "0");

		final int cnt = str.length();
		for (int i = 0; i < cnt; i++) {
			if (str.charAt(i) == '1') {
				set(i);
			}
		}		
	}
	
	public void save(String bookTitle){
		SharedPreferences pref = mPref ;

		final int cnt = getLength();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cnt; i++) {
			if (isMarked(i)) {
				sb.append('1');
			} else {
				sb.append('0');
			}
		}

		SharedPreferences.Editor edit = pref.edit();
		edit.putString(bookTitle, sb.toString());
		edit.commit();
	}
	
	@SuppressWarnings("unchecked")
	public String getBookmarkXml (String deliveryID ) {
		StringBuilder sb = new StringBuilder("<bookmarks>");
		String itemFormat = "<bookmark><track>%s</track><value>%s</value></bookmark>";
		Map<String, String> map = (Map<String, String>) mPref.getAll();
		
		Iterator<Entry<String, String>> kvp = map.entrySet().iterator();
		final int mapSize = map.size();
		for (int i = 0; i < mapSize; i++)
		{
		  Map.Entry<String, String> entry = (Entry<String, String>) kvp.next();
		  String key = entry.getKey();
		  
		  if ( false == key.startsWith(deliveryID) ) {
			continue;  
		  }
		  
		  String value = entry.getValue();
		  if ( value.length() <= 0){
			  continue;
		  }
		  
		  String strTrack = key.substring(key.lastIndexOf("_")+1);
		  
		  
		  sb.append( String.format(itemFormat, strTrack , value));
		  
		}
		sb.append("</bookmarks>");
		return sb.toString();
	}
	
	public void setBookmark( String deliveryID , String track , String value ){
		
		mPref.edit().putString(deliveryID+"_"+track, value).commit();
		
	}
	
	
	@SuppressWarnings("unchecked")
	public void delBookmark( String deliveryID ) {
		Map<String, String> map = (Map<String, String>) mPref.getAll();
		
		Iterator<Entry<String, String>> kvp = map.entrySet().iterator();
		final int mapSize = map.size();
		for (int i = 0; i < mapSize; i++)
		{
		  Map.Entry<String, String> entry = (Entry<String, String>) kvp.next();
		  String key = entry.getKey();
		  
		  if ( false == key.startsWith(deliveryID) ) {
			continue;  
		  }
		  
		  String value = entry.getValue();
		  if ( value.length() <= 0){
			  continue;
		  }
		  
		  mPref.edit().putString(key, "0").commit();
		}
	}
	
}
