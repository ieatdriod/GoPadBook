package org.iii.ideas.reader.last_page;

/**
 * 最後閱讀頁物件
 * @author III
 * 
 * 
 */
public class LastPage {
	/**
	 * deliver id
	 */
	public String deliverId;
	/**
	 * span
	 */
	public int span;
	/**
	 * index
	 */
	public int idx;
	/**
	 * 在全書百分比
	 */
	public int percentage;
	/**
	 * 最後閱讀日期
	 */
	public String read_at;
	/**
	 * 章節名稱
	 */
	public String chapName;
	/**
	 * 是否成功
	 */
	public boolean isSuccessful=true;
	public LastPage(){
		idx=span=percentage=-1;
		read_at="0";
		deliverId="";
		chapName="";
	}
	
}
