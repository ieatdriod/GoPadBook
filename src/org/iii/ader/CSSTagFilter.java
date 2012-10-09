package org.iii.ader;

import java.util.HashMap;
//import java.util.Hashtable;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;

/**
 * css parser輔助class，用以過濾 tag。
 * @author III
 *
 */
public class CSSTagFilter implements NodeFilter {

	private static final long serialVersionUID = -1925511707828836281L;
	//private String root = "";
	private String CSSContent = "";
	/**
	 * css對照表。
	 */
	public HashMap<String,HashMap<String,String>> cssSet = new HashMap<String,HashMap<String,String>>();
	
	/**
	 * 
	 * @param CSSContent css string
	 */
	public CSSTagFilter(String CSSContent){
		this.CSSContent = CSSContent;
		//css =  new HashMap<String,ArrayList<Object>>();
	}
	
	/**
	 * 是否接受
	 */
	public boolean accept(Node node) {
		parseCSS(CSSContent);
		return true;
	}
	

	
	private void parseCSS(String content){
		String[] csses = content.split("\\}");
		//css嚙緩嚙稽嚙踝蕭
		
		for (int i=0;i<csses.length;i++){
			int p = csses[i].indexOf("{");
			if (p > -1){
				String names = csses[i].substring(0,p);
				String value = csses[i].substring(p+1);

				String[] vs = value.split("\\;");
				HashMap<String,String> css_value = new HashMap<String,String>();
				for (int j=0;j<vs.length;j++){
					p = vs[j].indexOf(":");
					if (p > -1){
						String cn = vs[j].substring(0,p).trim();
						//Log.d("cs","is:"+cn);
						String cv = vs[j].substring(p+1).trim();
						//嚙盤嚙緻CSS(嚙踝蕭嚙碾嚙瘠嚙踝蕭)
						/*
						if(cn.equalsIgnoreCase("text-align")
								||cn.equalsIgnoreCase("font-weight")
								||cn.equalsIgnoreCase("direction")
								||cn.equalsIgnoreCase("text-decoration")
								||cn.equalsIgnoreCase("text-transform")
								||cn.equalsIgnoreCase("font-style")
								||cn.equalsIgnoreCase("line-height")
								||cn.equalsIgnoreCase("font-size")
						)
						*/
						
						
						//嚙盤嚙緻css(嚙緣嚙碾嚙瘠嚙踝蕭)
						
						//if(
								//!cn.equalsIgnoreCase("font-size")
								//!cn.equalsIgnoreCase("margin-right")
								//&&!cn.equalsIgnoreCase("margin-left")
								//&&!cn.equalsIgnoreCase("margin-top")
								//&&!cn.equalsIgnoreCase("margin-bottom")
								//!cn.equalsIgnoreCase("line-height")			
								//!cn.equalsIgnoreCase("text-indent")	
						//)
						
						
						//{
							css_value.put(cn, cv);
						
						//}
					}
				}
				
				String n = names.replace("&nbsp;", "").replaceAll("\\s", "");
				int index1=0;
				int index2=0;
				
				if(n.contains("/*") ){
					index1 = n.indexOf("/*");
					index2 = n.lastIndexOf("*/");
				}
				String h="";
				if(index2!=0){
					h = n.substring(index1,index2+2);
				}
				
				String gg = n;
				if(h.length()>0){
					gg = n.replace(h, "");
				}
				
				String[] ns = gg.split(",");
				for (int j=0;j<ns.length;j++){
					
					cssSet.put(ns[j], css_value);
				}
			}
		}
	}
	
	/*private class MutilLevelCSS{
		String cssName = null;
		Object nextName = null;
		MutilLevelCSS next = null;
		Hashtable<String,String> cssValue = null;
	}*/
}