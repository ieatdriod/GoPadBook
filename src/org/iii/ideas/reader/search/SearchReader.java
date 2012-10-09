package org.iii.ideas.reader.search;

import java.util.ArrayList;

import org.iii.ideas.reader.PartialUnzipper;
import org.xml.sax.XMLReader;

import android.util.Log;

public class SearchReader {
	
	/**
	 * 搜尋某一章節的中介程式，初始化handler進行搜尋
	 * @param uri 該章節檔案uri
	 * @param uz unzipper物件 
	 * @param kw 關鍵字
	 * @param chapName 章節相對路徑
	 * @return 搜尋結果列表
	 */
	public static ArrayList<SearchResult> getResults(String uri,PartialUnzipper uz,String kw,String chapName){
		//搜尋速度有提升的可能(改採先直接用java sax parser，失敗再用tagsoup)
		try{
		XMLReader xr = new org.ccil.cowan.tagsoup.Parser(); 
        ContentSearchHandler handler = new ContentSearchHandler(uri,uz,kw,chapName);
        xr.setContentHandler(handler);
        xr.parse(uri);
        
        return handler.getResults();
		}catch(Exception e){
		Log.e("ContentReader",e.toString());
		return null;
		}
		
	}
	/*
	public static void getContentByCallBack(String uri,PartialUnzipper uz,HtmlSpanReceiver hsr,final int threadIdx){

		try{
		//SAXParserFactory spf = SAXParserFactory.newInstance();
        //SAXParser sp = spf.newSAXParser(); 
        //XMLReader xr = sp.getXMLReader();
		XMLReader xr = new org.ccil.cowan.tagsoup.Parser(); 
        ContentSearchHandler handler = new ContentSearchHandler(uri,uz,hsr,threadIdx);
        xr.setContentHandler(handler);
        xr.parse(uri);
        
		}catch(Exception e){
		Log.e("ContentReader",e.toString());
		}
		
	}*/
}
