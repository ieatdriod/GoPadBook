package org.iii.ideas.reader.renderer;

/**
 * 當英文單字太長需要斷字，此class將單字以連字符號分為兩段紀錄下來
 * @author III
 * 
 */
public class HyphenatedWord {
	/**
	 * '-'前的文字
	 */
	public String beforeHyphen;
	/**
	 *  '-'後的文字
	 */
	public String afterHyphen;
}
