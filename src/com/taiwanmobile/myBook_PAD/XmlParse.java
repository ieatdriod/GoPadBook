package com.taiwanmobile.myBook_PAD;

import java.util.ArrayList;
import java.util.Map;
/**
 * parse時的資料結構
 * @author III
 * 
 */
public class XmlParse {
	private ArrayList<Map<String,Object>> xmlParse;
	private int pageCount;
	private int totalEntries;
	private int currentPage;
	public XmlParse(){
		xmlParse = new ArrayList<Map<String,Object>>();
	}
	/**
	 * 傳進list
	 * @param list 搜尋結果
	 */
	public void setList(ArrayList<Map<String,Object>> list){
		xmlParse=list;
	}
	/**
	 * 設定頁數
	 * 未使用
	 * @param count 頁數
	 */
	public void setPageCount(int count){
		pageCount=count;
	}
	/**
	 * 設定總筆數
	 * 未使用
	 * @param entries 總筆數
	 */
	public void setTotalEntries(int entries){
		totalEntries=entries;
	}
	/**
	 * 設定當前頁數
	 * 未使用
	 * @param page 當前頁數
	 */
	public void setCurrentPage(int page){
		currentPage=page;
	}
	
	/**
	 * 取得搜尋結果
	 * @return 搜尋結果
	 */
	public ArrayList<Map<String,Object>> getList(){
		return xmlParse;
	}
	/**
	 * 取得頁數
	 * @return 頁數
	 */
	public int getPageCount(){
		return pageCount;
	}
	/**
	 * 取得總筆數
	 * @return 總筆數
	 */
	public int setTotalEntries(){
		return totalEntries;
	}
	/**
	 * 取得當前頁面
	 * @return 當前頁面
	 */
	public int getCurrentPage(){
		return currentPage;
	}
	/**
	 * 取得搜尋筆數
	 * @return 搜尋筆數
	 */
	public int size(){
		return xmlParse.size();
	}
	/**
	 * 將資料加入list
	 * 未使用
	 * @param size 大小
	 * @param item 資料
	 */
	public void add(int size,Map<String,Object> item){
		xmlParse.add(size, item);
	}
}
