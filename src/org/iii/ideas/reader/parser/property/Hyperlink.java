package org.iii.ideas.reader.parser.property;

/**
 * 在一HtmlSpan如有超連結，以此class之物件標記該連結起訖index。
 * @author III
 * 
 */
public class Hyperlink { 
	public int start=0;
	public int end=0;
	public String href="";
	/**
	 * 
	 * @param s 起始index
	 * @param length 超連結顯示文字長度
	 * @param ref href
	 */
	public Hyperlink(int s,int length,String ref){
		start=s;
		end=s+length-1;
		href=ref;
	}
}
