package org.iii.ideas.reader.underline;

import org.iii.ideas.reader.ReadingInfo;

/**
 * 劃線資料物件
 * @author III
 * 
 */
public class Underline implements ReadingInfo{
/**
 * epub path
 */
public String epubPath;

/**
 * 劃線文字
 */
public String description="";

/**
 * 未用到
 */
public String content;

/**
 * 書籍分類
 */
public int bookType;

/**
 * 建立日期
 */
public String createDate;

/**
 * 劃線起始點span
 */
public int span1;

/**
 * 劃線起始點index
 */
public int idx1;

/**
 * 劃線終點span
 */
public int span2;

/**
 * 劃線終點index
 */
public int idx2;

/**
 * 書名
 */
public String bookName;

/**
 * 章節名稱
 */
public String chapterName;
/**
 * identifier
 */
public int id;
@Override
public int getIdxInSpan() {
	// TODO Auto-generated method stub
	return idx1;
}
@Override
public int getSpan() {
	// TODO Auto-generated method stub
	return span1;
}
}
