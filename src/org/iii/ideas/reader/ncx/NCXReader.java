package org.iii.ideas.reader.ncx;

import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.XMLReader;

import android.util.Log;

/**
 * parse ncx文件的sax reader
 * @author III
 * 
 */
public class NCXReader {
	ArrayList<Map<String,String>> tocList;
	/**
	 * ncx檔reader，可透過parser取得toc list
	 * @param absPath ncx file path
	 */
	public NCXReader(String absPath){

		try{
			//不用tagsoup:以下三行
			SAXParserFactory spf = SAXParserFactory.newInstance();
        	SAXParser sp = spf.newSAXParser(); 
        	XMLReader xr = sp.getXMLReader();
			//用tagsoup:
        	//XMLReader xr = new org.ccil.cowan.tagsoup.Parser(); 
			NCXParseHandler handler = new NCXParseHandler();
			xr.setContentHandler(handler);
			xr.parse(absPath);
			tocList = handler.getTocList();
		}catch(Exception e){
			Log.d("OPFReader","Tagsoup");
			try {
				XMLReader xr = new org.ccil.cowan.tagsoup.Parser(); 
				NCXParseHandler handler = new NCXParseHandler();
				xr.setContentHandler(handler);
				xr.parse(absPath);
				tocList = handler.getTocList();
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				Log.e("NCXReader",e.toString());
			}
		}
	}
	
	/**
	 * 取得toc列表
	 * @return toc list
	 */
	public ArrayList<Map<String,String>> getTocList(){
		return tocList;
	}

}
