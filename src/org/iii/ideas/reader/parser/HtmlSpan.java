package org.iii.ideas.reader.parser;

import java.util.ArrayList;

import org.iii.ideas.reader.parser.property.Hyperlink;
import org.iii.ideas.reader.parser.property.SpecialProperty;
import org.iii.ideas.reader.renderer.RendererConfig;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
/**
 * parser將html文件轉為一個個HtmlSpan物件，而HtmlSpan可視為一個呈現的區塊(block)，例如一段文字或是一張圖片，並載有呈現的屬性，而renderer即根據HtmlSpan來作分頁呈現。註:此HtmlSpan和文件中的span"無關"，而是文件呈現一個概念上的區塊!
 * @author III
 * 
 */
public class HtmlSpan {
	/**
	 * br處理
	 */
	public int brCount=0; 
	/**
	 * 此物件內的超連結列表
	 */
	public ArrayList<Hyperlink> links;
	/**
	 * 此物件內的特殊屬性區段
	 */
	public ArrayList<SpecialProperty> property=null;
	/**
	 * 此物件的類別
	 */
	public int type;
	/**
	 * 次要類別，為了hr增設
	 */
	public int subtype=0;
	/**
	 * span內容
	 */
	public StringBuilder content;
	/**
	 * 高(for img)
	 */
	public int height;
	/**
	 * 寬(for img)
	 */
	public int width;
	/**
	 * header size
	 */
	public int headerSize;
	/**
	 * 預設文字區塊
	 */
	public static final int TYPE_DEFAULT=0;
	/**
	 * 文字區塊，paragraph屬性(段落完留margin)
	 */
	public static final int TYPE_PAR=1;
	/**
	 * 圖片區塊
	 */
	public static final int TYPE_IMG=2;
	/**
	 * header區塊
	 */
	public static final int TYPE_HEADER=3;  
	/**
	 * 分隔線區塊，隸屬圖片區塊，唯另設subtype以標明和一般圖片不同
	 */
	public static final int SUBTYPE_HR=1;
	//public static final int TYPE_DIV=4;
	//public static final int TYPE_UL=5;
	//public static final int TYPE_OL=6;
	
	private CssProperty cssProperty;
	public HtmlSpan(){
		links=new ArrayList<Hyperlink>();
		type=0;
		content=new StringBuilder();
	}
	
	/**
	 * 
	 * @param in 繼承此HtmlSpan的property和type
	 */
	public HtmlSpan(HtmlSpan in){
		if(in != null){
			cssProperty=in.getCssProperty();
			type=in.type;
		}else{
			type=0;
		}
		links=new ArrayList<Hyperlink>();
		content=new StringBuilder();
	}
	
	/**
	 * 取得此HtmlSpan的type
	 * @return 此HtmlSpan的type
	 */
	public int getType(){
		return type;
	}
	
	/**
	 * 新增此span的超連結區段
	 * @param text 超連結文字
	 * @param href hyperlink href
	 */
	public void addLink(String text,String href){
		//Log.d("Content","length:"+content.length());
		//text = text.trim();
		//Log.d("addlink","txt:["+text+"]");
		Hyperlink link = new Hyperlink(content.length(),text.length(),href);
		if(type!=TYPE_IMG && text.length()>0){
			//Hyperlink link = new Hyperlink(content.length(),content.length()+text.length(),href);
			content.append(text);
			links.add(link);
		}
	}
	
	/**
	 * 清除此HtmlSpan的內容,類別和property
	 */
	public void clear(){
		property=null;
		if(links.size()>0)
			links.clear();
		if(content.length()>0){
			content.setLength(0);
			content.trimToSize();
		}
		type=0;
	}
	
	/**
	 * 新增特殊屬性區段
	 * @param cp 該區段的屬性
	 * @param text 該區段文字
	 * @param type 該區段類別(請參照SpecialProperty.java)
	 */
	public void addProperty(CssProperty cp, String text,int type){
		if(text.length()>0){
			if(property==null)
				property = new ArrayList<SpecialProperty>();
			int size = property.size();
			boolean createNewProperty=true;
			if(size>0){ 
				SpecialProperty p = property.get(size-1);
				if(p.equals(cp) && p.start==content.length()){
					p.extend(content.length()+text.length()-1);
					content.append(text);
					createNewProperty=false;
				}
			}
			if(createNewProperty){
				property.add(new SpecialProperty(content.length(),content.length()+text.length()-1,type,cp));
				content.append(text);
			}
		}
	}
	
	/**
	 * 取得超連結列表
	 * @return 超連結列表
	 */
	public ArrayList<Hyperlink> getLinks(){
		return links;
	}
	
	/**
	 * 設定header size
	 * @param sz header size
	 */
	public void setHeaderSize(int sz){
		headerSize=sz;
	}
	
	/**
	 * 設定css屬性
	 * @param p css property
	 */
	public void setCssProperty(CssProperty p){
		cssProperty=p;
	}
	
	/**
	 * 取得css屬性
	 * @return css property
	 */
	public CssProperty getCssProperty(){
		return cssProperty;
	}
	
	/**
	 * 是否有設定css屬性
	 * @return 是否有設定css屬性
	 */
	public boolean isCssPropertySet(){
		if(cssProperty==null)
			return false;
		else
			return true;
	}
	
	/**
	 * 根據使用者設定文字大小取得真正大小
	 * @param readerFontSize 使用者設定文字大小
	 * @return 文字大小
	 */
	public int getActualFontSize(int readerFontSize){
		if(isCssPropertySet()){
			if(type==TYPE_HEADER){
				return cssProperty.getHeaderFontSizeInPx(readerFontSize, headerSize);
			}else{
				return cssProperty.getFontSizeInPx(readerFontSize);
			}
		}else if(type==TYPE_HEADER)
			return (int) (readerFontSize*HeaderConverter.getEm(headerSize));
		return readerFontSize;
	}
	
	/**
	 * 取得對齊方式
	 * @return 對齊方式
	 */
	public int getAlign(){
		if(isCssPropertySet())
			return cssProperty.getAlign();
		else 
			return CssProperty.ALIGN_JUSTIFY;
	}
	
	/**
	 * 根據使用者設定文字大小取得縮排大小
	 * @param readerFontSize 使用者設定文字大小
	 * @return 文字大小
	 */
	public int getIndent(int readerFontSize){
		if(isCssPropertySet())
			return cssProperty.getIndentInPx(readerFontSize);
		else 
			return 0;
	}
	
	/**
	 * 根據使用者設定文字大小取得行高
	 * @param readerFontSize 使用者設定文字大小
	 * @return 行高
	 */
	public int getLineHeight(int readerFontSize){
		if(isCssPropertySet()){
			//Log.d("span:getLH","reader fs is:"+readerFontSize);
			if(type==TYPE_HEADER){
				//Log.d("getHeaderLineHeight",":"+cssProperty.getHeaderLineHeightInPx(readerFontSize, headerSize));
				//Log.d("header","size:"+headerSize);
				//Log.d("getHeader","lh:"+cssProperty.getHeaderLineHeightInPx(readerFontSize, headerSize));
				return cssProperty.getHeaderLineHeightInPx(readerFontSize, headerSize);
			}else{
				return cssProperty.getLineHeightInPx(readerFontSize);
			}
		}else{
			if(type==TYPE_HEADER)
				return (int) (readerFontSize*HeaderConverter.getEm(headerSize)*1.5f);
			else 
				return (int) (readerFontSize*1.5f);
		}
	}
	

	private TextPaint textPaint=null;
	/**
	 * 取得文字TextPaint
	 * @param readerFontSize 使用者設定文字大小，用來調整TextPaint的text size
	 * @return TextPaint
	 */
	public TextPaint getTextPaint(int readerFontSize){
		if(isCssPropertySet()){
			if(type==TYPE_HEADER){
				//Log.d("span","is italic:"+cssProperty.getTextPaint(readerFontSize, headerSize).getTypeface().isItalic());
				textPaint=cssProperty.getTextPaint(readerFontSize, headerSize);
			}else{
				//Log.d("span","is italic:"+cssProperty.getTextPaint(readerFontSize, 0).getTypeface().isItalic());
				textPaint = cssProperty.getTextPaint(readerFontSize, 0);
				//Log.d("span","tf:"+textPaint.getTypeface());
			}
		}else{
			if(textPaint==null){
				textPaint = new TextPaint();
				RendererConfig.enhanceTextPaint(textPaint);
				textPaint.setColor(Color.BLACK);
			}
			if(type==TYPE_HEADER){
				textPaint.setTextSize((int) (readerFontSize*HeaderConverter.getEm(headerSize)));
			}else{
				textPaint.setTextSize(readerFontSize);
			}
		}
		return textPaint;
	}
	

	/**
	 * 取得背景paint
	 * @return 背景paint
	 */
	public Paint getBgPaint(){
		if(isCssPropertySet()){
			return cssProperty.getBgPaint();
		}else{
			return null;
		}
	}
	
	/**
	 * 取得圖片border寬度
	 * @return 圖片border寬度
	 */
	public int getImgBorderWidth(){
		if(type==HtmlSpan.TYPE_IMG && cssProperty!=null && cssProperty.isImgAttsSet()){
			return cssProperty.getImgAtts().getBorderWidthInPx();
		}
		return 0;
	}
	
	/**
	 * 取得圖片border color
	 * @return 圖片border color
	 */
	public int getImgBorderColor(){
		if(type==HtmlSpan.TYPE_IMG && cssProperty!=null && cssProperty.isImgAttsSet()){
			return cssProperty.getImgAtts().getBorderColor();
		}
		return 0;
	}
	
	/**
	 * 取得圖片border style，如double, solid等，可參照ImgAtts
	 * @return 圖片border style
	 */
	public int getImgBorderStyle(){
		if(type==HtmlSpan.TYPE_IMG && cssProperty!=null && cssProperty.isImgAttsSet()){
			return cssProperty.getImgAtts().getBorderStyle();
		}
		return -1;
	}
	
	/**
	 * 是否設定圖片屬性
	 * @return 圖片屬性
	 */
	public boolean isImgAttsSet(){
		if(type==HtmlSpan.TYPE_IMG && cssProperty!=null)
			return cssProperty.isImgAttsSet();
		return false;
	}
	
	/**
	 * 取得圖片大小
	 * @param screenW 螢幕寬
	 * @param screenH 螢幕高
	 * @return new int[]{w,h}
	 */
	public int[] getImgSize(int screenW,int screenH){
		int w=width,h=height;
		if(type==HtmlSpan.TYPE_IMG && cssProperty!=null && cssProperty.isImgAttsSet()){
			ImgAtts atts = cssProperty.getImgAtts();
			int tempWidth = atts.getWidthInPx(screenW);
			int tempHeight = atts.getHeightInPx(screenH);			
			//Log.d("tempW:"+tempWidth,"tempH:"+tempHeight);
			if(tempWidth>0){
				if(tempHeight>0){
					h=tempHeight;
					w=tempWidth;
				}else{
					float ratio = (float)tempWidth/width;
					h*=ratio;
					w*=ratio;
				}
			}else{
				if(tempHeight>0){
					float ratio = (float)tempHeight/height;
					h*=ratio;
					w*=ratio;
				}
			}
			int borderWidth = atts.getBorderWidthInPx();
			h+=borderWidth+borderWidth;
			w+=borderWidth+borderWidth;
		}
		return new int[]{w,h};
	}
}