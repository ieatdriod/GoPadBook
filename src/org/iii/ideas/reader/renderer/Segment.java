package org.iii.ideas.reader.renderer;

import org.iii.ideas.reader.parser.property.SpecialProperty;

import android.graphics.Paint;
import android.text.TextPaint;

/**
 * 將HtmlSpan轉成LinedContent用到的輔助class。如果同一個段落裡文字內容有多種屬性，則會區分為多個segment。
 * @author III
 * 
 */
public 	class Segment{
	public float widths[]=null;
	private int start;
	public int end;
	private TextPaint textPaint;
	public Paint bgPaint;
	private int arrayEnd;
	private boolean isSub=false;
	private boolean isSup=false;
	/**
	 * 
	 * @param start_ start index in an HtmlSpan object 
	 * @param end_end index in an HtmlSpan Object
	 * @param tp 這個segment對應到的TextPaint
	 * @param bp 這個segment對應到的background paint
	 */
	public Segment(int start_,int end_,TextPaint tp,SpecialProperty curProperty){
		start=start_;
		//Log.d("segment","initialize start:"+getStart());
		end=end_;
		arrayEnd = end-getStart();
		textPaint=tp;
		if(curProperty==null)
			bgPaint=null;
		else{
			bgPaint = curProperty.property.getBgPaint();
			if(curProperty.isSub()){
				setIsSub();
			}else if(curProperty.isSup()){
				setIsSup();
			}
		}
	}
	
	/**
	 * 是否為下標
	 * @return 是否為下標
	 */
	public boolean isSub(){
		return isSub;
	}
	
	/**
	 * 是否為上標
	 * @return 是否為上標
	 */
	public boolean isSup(){
		return isSup;
	}
	
	/**
	 * 設定為下標
	 */
	public void setIsSub(){
		isSub=true;
		isSup=false;
	}
	
	/**
	 * 設定為上標
	 */
	public void setIsSup(){
		isSup=true;
		isSub=false;
	}
	
	/**
	 * 標記這個segment的終點
	 * @param e end index
	 */
	public void setEnd(int e){
		end=e;
		arrayEnd=e-getStart();
	}
	
	/**
	 * 計算這個segment的寬度
	 * @param s HtmlSpan.content.toString
	 */
	private void computeWidths(String s){
		if(widths==null){
			widths = new float[arrayEnd+1];
			textPaint.getTextWidths(s, getStart(), end+1, widths);
		}
	}
	
	/**
	 * 取得該segment對應到的紀錄文字寬度的array
	 * @param s string
	 * @return 紀錄文字寬度的array
	 */
	public float[] getWidthsArray(String s){
		if(widths==null){
			computeWidths(s);
		}
		return widths;
	}
	
	/**
	 * 清除寬度array (for memory efficiency consideration)
	 */
	public void clearWidthArray(){
		widths=null;
	}
	
	/**
	 * 取得segment文字寬度
	 * @param s HtmlSpan.content.toString
	 * @return 文字寬度
	 */
	public float getWidth(String s){
		if(widths==null){
			computeWidths(s);
		}
		int width=0;
		for(int i=0;i<=arrayEnd;i++){
			width+=widths[i];
		}
		return width;
	}
	
	/**
	 * white space在此span裡佔了多少寬度
	 * @param s HtmlSpan.content.toString
	 * @return white space所佔寬度
	 */
	public float getSpaceWidth(String s){
		float sum=0;
		if(widths==null)
			computeWidths(s);

		for(int i=getStart();i<=end;i++){
			if(Character.isWhitespace(s.charAt(i)))
				sum+=widthAt(i,s);
		}
		return sum;
	}
	
	/**
	 * 取得某個字的寬度
	 * @param i index in s
	 * @param s HtmlSpan.content.toString
	 * @return 某個字的寬度
	 */
	public float widthAt(int i,String s){
		//Log.d("start0",":"+getStart());
		if(i-getStart()<0 && i-getStart()>arrayEnd){
			return 0;
		}else{
			if(widths!=null){
				return widths[i-getStart()];
			}else{
				float tempWidth[] = new float[1];
				textPaint.getTextWidths(s.substring(i, i+1), tempWidth);
				return tempWidth[0];
			}
		}


	}
	/**
	 * 如果結尾是空白，將其省略讓空白歸零
	 * @param s HtmlSpan.content.toString
	 * @return 調整了多少寬度，如果結尾非空白則為0
	 */
	public float setEndSpaceZero(String s){
		if(Character.isWhitespace(s.charAt(end))){
			float temp =widths[arrayEnd]; 
			widths[arrayEnd]=0;
			return temp;
		}
		return 0;
	}
	/**
	 * 忽略開頭的空白，將其寬度設為0
	 * @param s HtmlSpan.content.toString
	 * @return 調整多少寬度，如果開頭非空白則為0
	 */
	public float setStartSpaceZero(String s){
		if(widths!=null && widths.length>0 && s.length()>getStart() && Character.isWhitespace(s.charAt(getStart()))){
			float temp =widths[0]; 
			widths[0]=0;
			return temp;
		}
		return 0;
	}
	/**
	 * 將該行的空白加大或縮小
	 * @param spaceRatio 縮放比例
	 * @param s HtmlSpan.content.toString
	 */
	public void resetSpaceWidth(float spaceRatio,String s){
		//Log.d("spaceWidth",":"+spaceWidth);
		//Log.d("offset",":"+lineWidthOffset);
		//Log.d("space","ratio:"+spaceRatio);
		for(int i=getStart();i<=end;i++){
			if(Character.isWhitespace(s.charAt(i))){
				//Log.d("resetws","space:"+widths[i-start]);
				widths[i-getStart()] = widths[i-getStart()]*spaceRatio;
				//Log.d("after_resetws","space:"+widths[i-start]);
			}
		}
	}
	
	/**
	 * 取得此segment的text paint，如果需要設定文字顏色可透過傳入參數設定。
	 * @param shouldSetColor 是否要設定文字顏色
	 * @param textColor 文字顏色
	 * @return TextPaint
	 */
	public TextPaint getTextPaint(boolean shouldSetColor,int textColor){
		if(shouldSetColor){
			//Log.d("seg","getPaintA");
			TextPaint temp = new TextPaint(textPaint);
			temp.setColor(textColor);
			return temp;
		}else{
			//Log.d("seg","getPaintB");
			return textPaint;
		}
	}
	/**
	 * 取得文字大小
	 * @return 文字大小
	 */
	public float getTextSize(){
		return  textPaint.getTextSize();
	}
	/**
	 * 取得起始index
	 * @return 起始index
	 */
	public int getStart(){
		return start;
	}
	
}