package org.iii.ideas.reader.annotation;

import org.iii.ideas.reader.ReadingInfo;

/**
 *  註記資訊包裝物件
 * @author III
 *
 */
public class Annotation implements ReadingInfo{
/**
 * epub path，在twm專案為did
 */
public String epubPath;
/**
 * 頁面描述
 */
public String description="";
/**
 * 註記內容
 */
public String content;
/**
 * 書籍分類
 */
public int bookType;
/**
 * 在全書百分比
 */
public int percentage=-1;
/**
 * create date
 */
public String createDate;
/**
 * span
 */
public int position1;
/**
 * index
 */
public int position2;
/**
 * 書名
 */
public String bookName;
/**
 * 章節名
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
