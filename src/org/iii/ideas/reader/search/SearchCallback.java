package org.iii.ideas.reader.search;

import java.util.ArrayList;

/**
 * 搜尋call back interface
 * @author III
 * 
 */
public interface SearchCallback {
	/**
	 * 搜尋完成結果回傳
	 * @param results 搜尋結果列表
	 */
	public void onGetResults(ArrayList<SearchResult> results, int prog);
	
	/**
	 * 搜尋完成
	 */
	public void onSearchFinished();
	
	/**
	 * 取得執行緒index，作為判斷是否停止的依據
	 * @return 執行緒index
	 */
	public int getThreadIdx();
}
