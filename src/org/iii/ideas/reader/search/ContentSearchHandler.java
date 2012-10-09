package org.iii.ideas.reader.search;

import java.util.ArrayList;

import org.iii.ideas.reader.PartialUnzipper;
import org.iii.ideas.reader.parser.HtmlParseHandler;
import org.iii.ideas.reader.parser.HtmlSpan;
import org.xml.sax.Attributes;

/**
 * parse某一xhtml檔案並搜尋內文，並建立索引以供跳至某一結果
 * @author III
 * 
 */
public class ContentSearchHandler extends HtmlParseHandler{
	private ArrayList<SearchResult> results = new ArrayList<SearchResult>();
	private String kw;
	private String chapName;
	
	/**
	 * 
	 * @param path epub path
	 * @param uz_ 用來解壓縮的工具物件(由於採用部分解壓縮，某些章節可能因為尚未閱讀到所以仍未從epub中解壓縮)
	 * @param kw_ 關鍵字
	 * @param cname 章節相對路徑
	 */
	public ContentSearchHandler(String path,PartialUnzipper uz_,String kw_,String cname){
		super(path,uz_);
		chapName=cname; 
		kw=kw_;
		results = new ArrayList<SearchResult>();
	}
	
	/**
	 * 取得搜尋結果
	 * @return 搜尋結果
	 */
	public  ArrayList<SearchResult> getResults(){
		return results;
	}
	
	/**
	 * css屬性不影響定位，故override原始html parser中css屬性處理的method提升search速度
	 */
	@Override
	public void pushCssProperty(String tagName,Attributes atts,boolean inheritBgColor){
		//do nothing
	}
	/**
	 * css屬性不影響定位，故override原始html parser中css屬性處理的method提升search速度
	 */
	@Override
	public void pushAndAddCssProperty(String tagName,Attributes atts){
		//do nothing
	}
	/**
	 * css屬性不影響定位，故override原始html parser中css屬性處理的method提升search速度
	 */
	@Override
	public void addCssProperty(){
		//do nothing
	}
	/**
	 * css屬性不影響定位，故override原始html parser中css屬性處理的method提升search速度
	 */
	@Override
	public void removeCssProperty(String tagName){
		//do nothing
	}
	/**
	 * 在每個span中進行搜尋字搜尋
	 */
    public void addSpan(HtmlSpan span){
    	//Log.d("add","span");
    	if(isCallBack){
    		//hsr.onGetHtmlSpan(spanCounter++, span,threadIdx);
    	}else if( span.type==HtmlSpan.TYPE_IMG){
    		spanCounter++;
    	}else{
    		int id=0;
    		String lowerKw = kw.toLowerCase();
    		String lowerStr = span.content.toString().toLowerCase();
    		while((id=lowerStr.indexOf(lowerKw, id)) >=0){
    			SearchResult result = new SearchResult();
    			result.chapterName=chapName;
    			result.idx=id;
    			result.span=spanCounter;
    			result.description = (span.content.substring(id).length()>=40)? span.content.substring(id,id+40):span.content.substring(id); 
    			results.add(result);
    			id+=lowerKw.length();
    		}
    		spanCounter++;
    	}
    }
}
