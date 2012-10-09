package tw.com.soyong.mebook;

import java.util.HashMap;

public class SyPostscript {
	
	public HashMap<Integer, String> mData;
	
	public SyPostscript() {
		mData = new HashMap<Integer, String>(); 
	}
	
	final public void setData( final Integer key , final String value){
		mData.put(key, value);
	}
	
	final public String getData( final Integer key ){
		return mData.get(key);
	}
}
