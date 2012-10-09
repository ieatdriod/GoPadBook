package org.iii.ideas.reader.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.iii.ideas.reader.PartialUnzipper;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;

/**
 * Html Parser的sax reader class
 * @author III
 * 
 */
public class HtmlContentReader {
	
	/**
	 * 取得該章節parse完後的HtmlSpan array list
	 * @param uri html file uri，ex:file:///sdcard/abc.htm
	 * @param uz unzipper 
	 * @return html span list
	 */
	public static ArrayList<HtmlSpan> getContent(String uri,PartialUnzipper uz){
		try{
			//不用tagsoup
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser(); 
			XMLReader xr = sp.getXMLReader();
			//用tagsoup
			//XMLReader xr = new org.ccil.cowan.tagsoup.Parser(); 
			HtmlParseHandler handler = new HtmlParseHandler(uri,uz);
			xr.setContentHandler(handler);
			xr.parse(uri);        
			return handler.getContent();
		}catch(Exception e){
			Log.e("HtmlContentReader","tagsoup:"+e.toString());  
			try {
				XMLReader xr = new org.ccil.cowan.tagsoup.Parser();
				HtmlParseHandler handler = new HtmlParseHandler(uri, uz);
				xr.setContentHandler(handler);
				xr.parse(uri);
				return handler.getContent();
			} catch (Exception e2) {
				// TODO: handle exception
				Log.e("HtmlContentReader",e.toString());
				return null;
			}
		}

	}
	
	/**
	 * 另開一個執行緒parse，每處理完一個HtmlSpan即call back回傳給HtmlSpanReceiver
	 * @param uri html file uri
	 * @param uz unzipper
	 * @param hsr 承接HtmlSpan
	 * @param threadIdx 執行緒index
	 * @throws Exception 例外
	 */
	public static void getContentByCallBack(String uri,PartialUnzipper uz,HtmlSpanReceiver hsr,final int threadIdx) throws Exception{
		try {
			//不用tagsoup
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			//用tagsoup
			//XMLReader xr = new org.ccil.cowan.tagsoup.Parser(); 
			HtmlParseHandler handler = new HtmlParseHandler(uri, uz, hsr,
					threadIdx);
			xr.setContentHandler(handler);
			xr.parse(uri);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("HtmlContentReader","tagsoup:"+e.toString());
			hsr.clearContent();
			XMLReader xr = new org.ccil.cowan.tagsoup.Parser(); 
			HtmlParseHandler handler = new HtmlParseHandler(uri, uz, hsr,
					threadIdx);
			xr.setContentHandler(handler);
			xr.parse(uri);
		}
	}
	
	/**
	 * 未使用
	 * @param uri
	 * @param inStream
	 * @param uz
	 * @param hsr
	 * @param threadIdx
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void getContentByCallBack(String uri,InputStream inStream,PartialUnzipper uz,HtmlSpanReceiver hsr,final int threadIdx) throws IOException, SAXException{
		//SAXParserFactory spf = SAXParserFactory.newInstance();
        //SAXParser sp = spf.newSAXParser(); 
        //XMLReader xr = sp.getXMLReader();
		XMLReader xr = new org.ccil.cowan.tagsoup.Parser(); 
		HtmlParseHandler handler = new HtmlParseHandler(uri,uz,hsr,threadIdx);
        xr.setContentHandler(handler);
        xr.parse(new InputSource(inStream));
	}
}
