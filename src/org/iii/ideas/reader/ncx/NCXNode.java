package org.iii.ideas.reader.ncx;

/**
 * 建立ncx tree時的node物件
 * @author III
 * 
 *
 */
public class NCXNode{
	private String text;
	private String href;
	private int parent;
	
	/**
	 * 
	 * @param p parent index
	 */
	public NCXNode(int p){
		parent=p;
		text="";
		href="";
	}
	/**
	 * 設定parent
	 * @param p parent index
	 */
	public void setParent(int p){parent =p;}
	/**
	 * 設定href
	 * @param h href
	 */
	public void setHref(String h){href =h;}
	/**
	 * 設定章節名稱
	 * @param t 章節名稱
	 */
	public void setText(String t){text =t;}
	
	/**
	 * 取得parent
	 * @return parent index
	 */
	public int getParent(){return parent;}
	/**
	 * 取得href
	 * @return href
	 */
	public String getHref(){return href;}
	
	/**
	 * 取得章節名稱
	 * @return 章節名稱
	 */
	public String getText(){return text;}
}
