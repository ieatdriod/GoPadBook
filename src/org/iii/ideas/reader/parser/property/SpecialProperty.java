package org.iii.ideas.reader.parser.property;

import org.iii.ideas.reader.parser.CssProperty;
import org.iii.ideas.reader.renderer.RendererConfig;

import android.content.Context;
import android.text.TextPaint;

/**
 * 在一HtmlSpan裡標記特殊屬性(即與該HtmlSpan預設屬性不同的區塊，例如以<font>調整某幾字文字大小和字體顏色，或<u>,<s>等特殊屬性元素)
 * @author III
 * 
 */
public class SpecialProperty {
	public CssProperty property=null;
	/**
	 * 無特殊效果，僅字體顏色/粗斜體/大小改變
	 */
	public static final int DEFAULT = 0; 
	/**
	 * 下底線
	 */
	public static final int U=2; 
	/**
	 * 刪除線
	 */
	public static final int STRIKE=5;
	/**
	 * 上標
	 */
	public static final int SUP=6;
	/**
	 * 下標
	 */
	public static final int SUB=7;
	private int type;
	/**
	 * 起點
	 */
	public int start;
	/**
	 * 終點
	 */
	public int end;
	/**
	 * 
	 * @param start_ 此屬性起始點index
	 * @param end_  此屬性終點index
	 * @param type_ 此屬性類別
	 * @param cp 對應到的property
	 */
	public SpecialProperty(int start_,int end_,int type_,CssProperty cp){
		start=start_;
		end=end_;
		type=type_;
		property=cp;
	}
	
	/**
	 * 是否為下標
	 * @return 是否為下標
	 */
	public boolean isSub(){
		if(type==SUB)
			return true;
		else
			return false;
	}
	
	/**
	 * 是否為上標
	 * @return 是否為上標
	 */
	public boolean isSup(){
		if(type==SUP)
			return true;
		else
			return false;
	}
	
	/**
	 * 是否為上下標
	 * @return 是否為上下標
	 */
	public boolean isSupOrSub(){
		if(type==SUB || type==SUP)
			return true;
		else
			return false;
	}
	
	/**
	 * 將此屬性範圍擴大，延後終點index
	 * @param newEnd 新的終點index
	 */
	public void extend(int newEnd){
		end=newEnd;
	}
	
	/**
	 * 取得類型
	 * @return type
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * 取得此屬性的Paint
	 * @param readerFontSize 使用者設定字體大小
	 * @param headerSize header大小
	 * @param ctx context
	 * @return 此屬性的text paint
	 */
	public TextPaint getTextPaint(int readerFontSize, int headerSize, Context ctx){
		if(isSupOrSub())
			return property.getTextPaint((int) (readerFontSize*RendererConfig.SUB_SUP_RATIO), headerSize);
		else
		return property.getTextPaint(readerFontSize, headerSize);
	}
	
	/**
	 * 取得行高
	 * @param readerFontSize 使用者設定字體大小
	 * @param headerSize header大小
	 * @return 行高
	 */
	public int getLineHeight(int readerFontSize,int headerSize){
		if(headerSize>0)
			return property.getHeaderLineHeightInPx(readerFontSize, headerSize);
		else
			return property.getLineHeightInPx(readerFontSize);
	}
}
