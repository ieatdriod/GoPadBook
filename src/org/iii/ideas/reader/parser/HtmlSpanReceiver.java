package org.iii.ideas.reader.parser;
/**
 * parser call back interface，用來handle parse的過程
 * @author III
 * 
 */
public interface HtmlSpanReceiver {
	/**
	 * 清除目前parse完的content列表
	 */
	public void clearContent();
	
	/**
	 * parser call back回傳新處理好的HtmlSpan
	 * @param idx 第幾個HtmlSpan
	 * @param span 處理完畢HtmlSpan物件
	 * @param threadIdx 執行緒index
	 */
	public void onGetHtmlSpan(int idx,HtmlSpan span,int threadIdx);
	/**
	 * parser已parse完整份html文件
	 * @param threadIdx 執行緒index
	 */
	public void onParsingFinished(int threadIdx);
	/**
	 * 取得當前章節title
	 * @param title title
	 */
	public void onGetTitle(String title);
	/**
	 * 取得執行緒index
	 * @return 執行緒index
	 */
	public int getThreadIdx();
}
