package org.iii.ideas.reader.renderer;

import java.util.HashSet;

/**
 * 斷行輔助用class，負責判斷可以斷在哪些文字。
 * @author III
 * 
 */
public class LineBreak {
	public static HashSet<Integer> lineBreakMap=null;
	
	private static void loadMap(){
		lineBreakMap = new HashSet<Integer>();
		lineBreakMap.add(new Integer('，'));
		lineBreakMap.add(new Integer('。'));
		lineBreakMap.add(new Integer(','));	
		lineBreakMap.add(new Integer('.'));
		lineBreakMap.add(new Integer('；'));
		lineBreakMap.add(new Integer('"'));
		lineBreakMap.add(new Integer(';'));
		lineBreakMap.add(new Integer('-'));
		lineBreakMap.add(new Integer('?'));
		lineBreakMap.add(new Integer('!'));	
		lineBreakMap.add(new Integer(':'));
		lineBreakMap.add(new Integer(' '));
		lineBreakMap.add(new Integer('？'));
		lineBreakMap.add(new Integer('：'));
		lineBreakMap.add(new Integer('！'));
		lineBreakMap.add(new Integer('、'));
		lineBreakMap.add(new Integer('\''));
		lineBreakMap.add(new Integer('　'));
	}
	
	/**
	 * 判斷某一character能否放在行首
	 * @param c character
	 * @return 能否放在行首
	 */
	public static boolean canBeFirstInLine(char c){
		if(lineBreakMap==null)
			loadMap();
		if(lineBreakMap.contains(new Integer(c)))
			return false;
		else
			return true;
	}
	
	/**
	 * 判斷某一character是否為英文alphabet
	 * @param ch character
	 * @return 是否為英文alphabet
	 */
	public static boolean isEnglishLetter(char ch){
		if((ch>=65 && ch<=90)||(ch>=97 && ch<=122))
			return true;
		else
			return false;
	}
}
