package org.iii.ideas.reader.renderer;

/**
 * 書籍內容點選結果
 * @author III
 * 
 */
public class DrawableOnClickResult {
	/**
	 * 是否點選到某一資源 (如圖片，hyperlink)
	 */
	private boolean hasUri;
	
	/**
	 * 此次touch event是否作用在此範圍
	 */
	private boolean onClicked;
	
	/**
	 * 作用在哪一個字上
	 */
	private int idxInSpan;
	
	/**
	 * 如果點選處指向某一resource，該resource的位置
	 */
	private String src;
	
	/**
	 * 是否為圖片
	 */
	private boolean isImg=false;
	
	/**
	 * 標記點選到圖片
	 * @param is 是否點選到圖片
	 */
	public void setIsImg(boolean is){
		isImg=is;
	}
	
	/**
	 * 是否為圖片
	 * @return true: 點選到圖片. false: o.w.
	 */
	public boolean isImg(){
		return isImg;
	}
	
	public DrawableOnClickResult (){
		hasUri=false;
		onClicked=false;
		idxInSpan=0;
		src=null;
		isImg=false;
	}
	
	/**
	 * 是否為hyperlink
	 * @return 是否為hyperlink
	 */
	public boolean isLink(){
		return hasUri && !isImg;
	}
	
	/**
	 * 設定resource來源
	 * @param s resource來源
	 */
	public void setSrc(String s){
		hasUri=true;
		src=s;
	}
	
	/**
	 * 標記此touch event是否作用在範圍內
	 * @param status 狀態
	 */
	public void setStatus(boolean status){
		onClicked=status;
	}
	
	/**
	 * 標記點選到該span中哪一個字
	 * @param idx index in span
	 */
	public void setIdx(int idx){
		idxInSpan=idx;
	}
	
	/**
	 * 取得resource位置
	 * @return resource位置
	 */
	public String getSrc(){
		return src;
	}
	
	/**
	 * 是否被點選
	 * @return 是否被點選
	 */
	public boolean getStatus(){
		return onClicked;
	}
	
	/**
	 * 取得點選到哪一個字
	 * @return 點選到哪一個字
	 */
	public int getIdx(){
		return idxInSpan;
	}
}
