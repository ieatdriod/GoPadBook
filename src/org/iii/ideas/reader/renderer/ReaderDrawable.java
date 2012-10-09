package org.iii.ideas.reader.renderer;

import android.graphics.Canvas;
import android.text.TextPaint;

/**
 * 每一個內容物的base interface 
 * @author III
 *
 */
public interface ReaderDrawable {
	/**
	 * 畫出所屬內容
	 * @param cv canvas
	 */
	public void draw(Canvas cv);
	/**
	 * 取得高
	 * @return height
	 */
	public int getHeight();
	/**
	 * 取得寬
	 * @return width
	 */
	public int getWidth();
	/**
	 * 設定位置
	 * @param x_ x
	 * @param y_ y
	 */
	public void setPosition(int x_,int y_);
	/**
	 * 取得x值 
	 * @return x
	 */
	public int getX();
	/**
	 * 取得y值 
	 * @return y
	 */
	public int getY();
	/**
	 * 判斷click是否作用在該物件範圍並回傳結果
	 * @param inX touch event x
	 * @param inY touch event y
	 * @return click結果
	 */
	public DrawableOnClickResult onClicked(int inX,int inY);
	/**
	 * 取得第幾個span
	 * @return 第幾個span
	 */
	public int getSpanIdx();
	/**
	 * 畫線
	 * @param cv canvas
	 * @param tp text paint
	 * @param startIdx 起點index
	 * @param endIdx 終點 index
	 */
	public void drawLine(Canvas cv,TextPaint tp,int startIdx,int endIdx);
	
	/**
	 * 畫線
	 * @param cv canvas
	 * @param tp text paint
	 * @param startIdx 起點index
	 * @param endIdx 終點 index
	 * @param xOffset 距離baseline多遠(直書baseline為文字左緣)
	 */
	public void drawRect(Canvas cv,TextPaint tp,int startIdx,int endIdx, int xOffset);
		/**
	 * 是否為文字內容
	 * @return  是否為文字內容
	 */
	public int isLinedContent();
}
