package org.iii.ideas.reader.parser;
/**
 * 根據header大小取得字體大小 (in em)
 * @author III
 * 
 */
public class HeaderConverter {
	/**
	 * 取得字體大小(以em為單位)
	 * @param headerSize header size
	 * @return 字體大小
	 */
	public static float getEm(int headerSize){
		switch(headerSize){
		case 1:
			return 2f;
		case 2:
			return 1.5f;
		case 3:
			return 1.2f;
		case 4:
			return 1f;
		case 5:
			return 0.8f;
		case 6:
			return 0.7f;
		default:		
			return 1f;	
		}
	}
}
