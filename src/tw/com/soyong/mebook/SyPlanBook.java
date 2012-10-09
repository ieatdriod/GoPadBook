package tw.com.soyong.mebook;

import java.io.FileNotFoundException;
import java.io.IOException;

//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Mebook
//  @ File Name : PlanBook.java
//  @ Date : 2009/3/23
//  @ Author : Victor
//
//

public class SyPlanBook extends MebookData {
	
	byte [] mData;
	
	@Override
	public void load(String file, long pos, int size) {
		super.load(file, pos, size);
		
		byte [] data = new byte [size]; 
		
		SyInputStream is;
		try {
			is = new SyInputStream(file);
			is.seek(pos);
			is.read( data );
			is.close();
			is = null;
			
			SyDecrypt.decrypt(mEncMode, data);
			mData = data;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SyPlanBook(int encMode) {
		super(encMode);
	}
	
	public SyItem getData(String id, int mode) throws MebookException {
		
		if ( 0 != id.compareTo(MebookData.ARTICLE) || DATA_TXT != mode){
			throw new MebookException();
		}
		
		SyItem item = new SyItem(mData);
		return item;
	}
	
}
