package org.iii.ideas.reader.renderer;

import java.util.ArrayList;

import org.iii.ideas.reader.underline.Underline;

/**
 * EpubView和main activity的溝通管道。
 * @author III
 * 
 */
public interface EpubViewCallback {
	/**
	 * 檢查是否為underline模式
	 * @return 是否為underline模式
	 */
	public boolean isUnderlineOpen();
	
	/**
	 * 檢查是否為underline移除模式
	 * @return 是否為underline移除模式
	 */
	public boolean isUnderlineRemovalOpen();
	/**
	 * 告知main activity連結被點選
	 * @param url hyperlink url
	 */
	public void onLinkClicked(String url);
	
	/**
	 * 告知main activity圖片被點選
	 * @param url 圖片url
	 * @return 是否成功
	 */
	public boolean onImgClicked(String url);
	
	/**
	 * 上一頁
	 */
	public void pageUp();
	
	/**
	 * 下一頁
	 */
	public void pageDown();
	
	/**
	 * 將使用者拖曳畫線的起訖點傳給main activity進行寫入資料庫的動作
	 * @param startSpan 起點span
	 * @param startIdx 終點span
	 * @param endSpan 起點index
	 * @param endIdx 終點index
	 */
	public void onGetUnderline(int startSpan,int startIdx,int endSpan,int endIdx);
	
	/**
	 * 將使用者點選要刪除的underline位置回傳給main activity
	 * @param span span
	 * @param idx index
	 * @return 要刪除的underline
	 */
	public ArrayList<Underline> onDeleteUnderline(int span,int idx);
	
	/**
	 * 檢查是否為試閱模式(會影響最後促銷頁)
	 * @return 是否為試閱模式
	 */
	public boolean getIsTrial();
	
	/**
	 * 檢查是否為夜間模式
	 * @return 是否為夜間模式
	 */
	public boolean getIsNightMode();
	
	/**
	 * 取得content id(影響最後促銷頁連結)
	 * @return content id
	 */
	public String getContentId();
	
	/**
	 * 取得profile裡的書名，而非opf檔裡的書名(影響最後促銷頁連結)
	 * @return 書名
	 */
	public String getTitleFromProfile();
	
	/**
	 * 取得作者名(影響最後促銷頁連結)
	 * @return 作者名
	 */
	public String getAuthors();
	
	/**
	 * 取得出版社(影響最後促銷頁連結)
	 * @return 出版社
	 */
	public String getPublisher();
	
	/**
	 * 取得delivery id
	 * @return delivery id
	 */
	public String getDeliverId();
	/**
	 * view size changed
	 */
	public void onViewSizeChanged();
	/**
	 * change font size related
	 */
	public void setFontSize(int aIdx);
	public int getFontSize();
	
	public void disableFirstPage();
	
	public void handleShowMenu();
}
