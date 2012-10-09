package org.iii.ideas.reader.search;

/**
 * 搜尋結果物件
 * @author III
 * 
 */
public class SearchResult {
	/**
	 * 結果span
	 */
	public int span;
	/**
	 * 結果index
	 */
	public int idx;
	/**
	 * 章節相對路徑
	 */
	public String chapterName;
	
	/**
	 * 自關鍵字起算一定字數作為搜尋結果描述
	 */
	public String description;
	
	@Override
	public String toString(){
		if(description==null)
			return "";
		return description;
	}
	
}
