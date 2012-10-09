package org.iii.ideas.reader.parser;

/**
 * html receiver通知主程式parse錯誤的call back interface
 * @author III
 * 
 */
public interface ParseErrorHandler {
	/**
	 * 通知parse html時發生error
	 */
	public void onHtmlParseError();
	/**
	 * 通知用tagsoup重新parse。為了提升速度，第一次parse不用tagsoup，當sax出錯(如遇到不成對tag)時才會重新用tagsoup輔助，此時會call back進行相關處理
	 */
	public void onTagSoupReload();
}
