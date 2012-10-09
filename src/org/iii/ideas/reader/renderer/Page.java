package org.iii.ideas.reader.renderer;

import java.util.ArrayList;

/**
 * 計算分頁時用來紀錄一頁起訖和內容的container。
 * @author III
 * 
 */
public class Page {
	private ArrayList<ReaderDrawable> content;
	/**
	 * 頁面起點span
	 */
	public int startSpan;
	/**
	 * 頁面起點index
	 */
	public int startIdx;
	/**
	 * 頁面終點span
	 */
	public int endSpan;
	/**
	 * 頁面終點index
	 */
	public int endIdx;
	/**
	 * 
	 * @param startSpan_ 起始span
	 * @param startIdx_ 起始index
	 */
	public Page(int startSpan_,int startIdx_){
		content = new ArrayList<ReaderDrawable>();
		startSpan=startSpan_;startIdx=startIdx_;
	}
	
	/**
	 * 
	 * @param content_ 頁面內容
	 * @param startSpan_ 起始span
	 * @param startIdx_ 起始index
	 */
	public Page(ArrayList<ReaderDrawable> content_,int startSpan_,int startIdx_){
		content = content_;
		startSpan=startSpan_;startIdx=startIdx_;
	}
	
	/**
	 * 設定終點
	 * @param endSpan_ 終點span
	 * @param endIdx_ 終點index
	 */
	public void setEnd(int endSpan_,int endIdx_){
		endSpan=endSpan_;
		endIdx=endIdx_;
	}
	
	/**
	 * 新增頁面內容
	 * @param item 頁面內容項目
	 */
	public void addDrawable(ReaderDrawable item){
		content.add(item);
	}
	
	/**
	 * 設定頁面內容
	 * @param content_ 頁面內容list
	 */
	public void setContent(ArrayList<ReaderDrawable> content_){
		content=content_;
	}
	
	/**
	 * 清除內容
	 */
	public void clearContent(){
		content=null;
	}
	
	/**
	 * 頁面內容是否為null
	 * @return 頁面內容是否為null
	 */
	public boolean isContentNull(){
		if(content==null)
			return true;
		else
			return false;
	}
	
	/**
	 * 取得頁面內容
	 * @return 頁面內容
	 */
	public ArrayList<ReaderDrawable> getContent(){
		return content;
	}
}
