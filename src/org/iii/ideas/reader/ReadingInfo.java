package org.iii.ideas.reader;

/**
 * 閱讀紀錄interface，確保實作此interface的class都可取得其定位
 * @author III
 * 
 */
public interface ReadingInfo {
	public int getSpan();
	public int getIdxInSpan();
}
