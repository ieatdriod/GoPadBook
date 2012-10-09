package org.iii.ideas.reader.bookmark;

import org.iii.ideas.reader.ReadingInfo;

/**
 * 書籤物件
 * @author III
 * 
 */
public class Bookmark implements ReadingInfo{
/**
 * epub path
 */
public String epubPath; 
/**
 * 頁面描述
 */
public String description=""; 
/**
 * 書籍分類, 未使用
 */
public int bookType; 
/**
 * 寫入時間
 */
public String createDate; 
/**
 * 第幾個span
 */
public int position1;
/**
 * span中第幾個字
 */
public int position2; 
/**
 * 書名
 */
public String bookName;  
/**
 * 百分比
 */
public int percentage=-1;
/**
 * 章節名稱
 */
public String chapterName; 
/**
 * id
 */
public int id;
@Override
public int getIdxInSpan() {
	// TODO Auto-generated method stub
	return position2;
}
@Override
public int getSpan() {
	// TODO Auto-generated method stub
	return position1;
}
}
