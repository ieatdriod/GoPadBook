package com.taiwanmobile.myBook_PAD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.iii.ideas.reader.annotation.Annotation;
import org.iii.ideas.reader.annotation.AnnotationDB;
import org.iii.ideas.reader.bookmark.Bookmark;
import org.iii.ideas.reader.bookmark.Bookmarks;
import org.iii.ideas.reader.search.SearchResult;
import org.iii.ideas.reader.underline.Underline;
import org.iii.ideas.reader.underline.UnderlineDB;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.gsimedia.gsiebook.lib.GSiDatabaseAdapter;

import tw.com.soyong.utility.SyBookmark;
import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
/**
 * parse xml檔案時 對tag的處理
 * @author III
 * 
 */
public class XmlParseHandler extends DefaultHandler{
	private boolean in_ebooks=false;
	//private boolean in_error=false;
	//private boolean in_ebook=false;
	
	//private boolean in_deliveryID=false;
	//private boolean in_title=false;
	
	private boolean in_notes=false;	
	//private boolean in_note=false;
	//private boolean in_chapter=false;
	//private boolean in_position1=false;
	//private boolean in_position2=false;
	//private boolean in_percentage=false;
	//private boolean in_content=false;
	//private boolean in_description=false;	
	
	private boolean in_underlines=false;
	//private boolean in_underline=false;
	//private boolean in_span1=false;
	//private boolean in_span2=false;
	//private boolean in_idx1=false;
	//private boolean in_idx2=false;	
	
	//private boolean in_bookmarks=false;
	//private boolean in_bookmark=false;
	//private boolean in_track=false;
	//private boolean in_value=false;
	
	private String dataString;
	
	private String error;
	private String deliveryID;
	private String title;
	
	Map<String,Object> annotation;
	Map<String,Object> underline;
	Map<String,Object> bookmark;
	ArrayList<String> pageResult;
	
	private TWMDB tdb ;
	private Cursor cursorDBData;

	
	Activity ctx;
	
	public XmlParseHandler(Activity context) {
		ctx=context;
		tdb = new TWMDB(ctx);
		cursorDBData = tdb.select("isdownloadbook = '1'");	
		
		pageResult = new ArrayList<String>();  
	}
	public void startDocument() throws SAXException {
		//tocList=new ArrayList<Map<String,String>>();
		//Log.d("SLH","IN_HANDLER");
		//Log.d("SD1","HANDLER");
   }
	public void endDocument() throws SAXException {
        // do nothing
		Log.d("SLPEND","END");
   }  	
	/**
	 * 取得error訊息
	 * @return error訊息
	 */
	public String getError(){
		return error;
	}	
	/**
	 * 取得DeliveryID
	 * @return DeliveryID
	 */
	public String getDeliveryID(){
		return deliveryID;
	}
	/**
	 * 取得title
	 * @return title
	 */
	public String getTitle(){
		return title;
	}	
	/**
	 * 起始TAG時所觸發的事件
	 */
	public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
		dataString="";
		if (localName.equals("ebooks")) {
			in_ebooks=true;		
		}else if (localName.equals("error")) {
			//in_error=true;
		}else if (localName.equals("ebook")) {
			//in_ebook=true;
		}else if (localName.equals("Delivery-ID")) {
			//in_deliveryID=true;
		}else if (localName.equals("title")) {
			//in_title=true;
		}else if (localName.equals("notes")) {
			in_notes=true;
			annotation = new HashMap<String,Object>();
		}else if (localName.equals("note")) {
			//in_note=true;
		}else if (localName.equals("chapter")) {
			//in_chapter=true;
		}else if (localName.equals("position1")) {
			//in_position1=true;
		}else if (localName.equals("position2")) {
			//in_position2=true;
		}else if (localName.equals("percentage")) {
			//in_percentage=true;
		}else if (localName.equals("content")) {
			//in_content=true;
		}else if (localName.equals("description")) {
			//in_description=true;
		}else if (localName.equals("underlines")) {
			in_underlines=true;
			underline = new HashMap<String,Object>();
		}else if (localName.equals("underline")) {
			//in_underline=true;
		}else if (localName.equals("pdfmarkers")) {
			//in_underline=true;
		}else if (localName.equals("span1")) {
			//in_span1=true;
		}else if (localName.equals("span2")) {
			//in_span2=true;
		}else if (localName.equals("idx1")) {
			//in_idx1=true;
		}else if (localName.equals("idx2")) {
			//in_idx2=true;
		}else if (localName.equals("bookmarks")) {
			//in_bookmarks=true;
			bookmark = new HashMap<String,Object>();
		}else if (localName.equals("bookmark")) {
			//in_bookmark=true;
		}else if (localName.equals("track")) {
			//in_track=true;
		}else if (localName.equals("value")) {
			//in_value=true;
		}
	}
	/**
	 * 結束TAG時所觸發的事件
	 */
	public void endElement(String namespaceURI, String localName,
            String qName) throws SAXException {
		if (localName.equals("ebooks")) {
			in_ebooks=false;		
		}else if (localName.equals("error")) {
			//in_error=false;
			error = dataString;
		}else if (localName.equals("ebook")) {
			//in_ebook=false;
		}else if (localName.equals("Delivery-ID")) {
			//in_deliveryID=false;
			deliveryID = dataString;
		}else if (localName.equals("title")) {
			//in_title=false;
			title = dataString;
		}else if (localName.equals("notes")) {
			in_notes=false;			
		}else if (localName.equals("note")) {
			//in_note=false;
			Annotation  ann = new Annotation();
			Log.e("ann", "ann 1");
			if(annotation.get("chapter") != null)
			{
				ann.chapterName = annotation.get("chapter").toString();
				ann.description = annotation.get("description").toString();
				ann.bookName = title;
				Log.e("ann", "ann 2");
				ann.position1 = Integer.valueOf(annotation.get("position1")
						.toString());
				ann.position2 = Integer.valueOf(annotation.get("position2")
						.toString());
				ann.bookType = 0;
				Log.e("ann", "ann 3");
				ann.epubPath = deliveryID;
				ann.content = annotation.get("content").toString();

				cursorDBData.moveToFirst();
				Log.e("count ", String.valueOf(cursorDBData.getCount()));

				for (int i = 0; i < cursorDBData.getCount(); i++) {
					// Log.e("cursorDBData.getString(5) ",
					// cursorDBData.getString(5));
					// Log.e("deliveryID ", deliveryID);
					if (cursorDBData.getString(5).equals(deliveryID)) {
						AnnotationDB adb = new AnnotationDB(ctx);
						adb.insertAnn(ann);
						adb.closeDB();
						Log.e("note ", " insert DB");
						break;
					}
					cursorDBData.moveToNext();
				}
			}else{//pdf
			
				String strPage = (String) annotation.get("page");
				String strContent = (String) annotation.get("content");
				String aXML = "<notes><note><page>"+strPage+"</page>"+"<content>"+strContent+"</content></note></notes>";
				
				//deliveryID
				Cursor c = tdb.select2(deliveryID);
				if(c.getCount() >0)
				{
					c.moveToFirst();
					GSiDatabaseAdapter.setAnnotation(ctx, deliveryID, aXML);
				}
			}
		}else if (localName.equals("pdfmarkers")){
			String aXML = dataString;
			Log.d("restore",aXML);
			
			//deliveryID
			Cursor c = tdb.select2(deliveryID);
			if(c.getCount() >0)
			{
				c.moveToFirst();
				GSiDatabaseAdapter.setMarker(ctx, deliveryID, aXML);
			}
		}else if (localName.equals("chapter")) {
			//in_chapter=false;
			if(in_notes==true){
				annotation.put("chapter", dataString);
				Log.e("annotation-chapter", dataString);
			}else if(in_underlines==true){
				underline.put("chapter", dataString);
				Log.e("underline-chapter", dataString);
			}else{
				bookmark.put("chapter", dataString);
				Log.e("bookmark-chapter", dataString);
			}			
		}else if (localName.equals("position1")) {
			//in_position1=false;
			if(in_notes==true){
				annotation.put("position1", dataString);
				Log.e("annotation-position1", dataString);
			}else if(in_underlines==true){
				underline.put("position1", dataString);
				Log.e("underline-position1", dataString);
			}else{
				bookmark.put("position1", dataString);
				Log.e("bookmark-position1", dataString);
			}					
		}else if (localName.equals("position2")) {
			//in_position2=false;
			if(in_notes==true){
				annotation.put("position2", dataString);
				Log.e("annotation-position2", dataString);
			}else if(in_underlines==true){
				underline.put("position2", dataString);
				Log.e("underline-position2", dataString);
			}else{
				bookmark.put("position2", dataString);
				Log.e("bookmark-position2", dataString);
			}					
		}else if (localName.equals("percentage")) {
			//in_percentage=false;
			if(in_notes==true){
				annotation.put("percentage", dataString);
				Log.e("annotation-percentage", ":"+dataString);
			}else{
				bookmark.put("percentage", dataString);
				Log.e("bookmark-percentage", ":"+dataString);
			}			
		}else if (localName.equals("content")) {
			//in_content=false;
			if(in_notes==true){
				annotation.put("content", dataString);
				Log.e("annotation-content", dataString);
			}else{
				underline.put("content", dataString);
				Log.e("underline-content", dataString);
			}				
		}else if (localName.equals("description")) {
			//in_description=false;
			if(in_notes==true){
				annotation.put("description", dataString);
			}else if(in_underlines==true){
				underline.put("description", dataString);
			}else{
				bookmark.put("description", dataString);
			}				
		}else if (localName.equals("underlines")) {
			in_underlines=false;
		}else if (localName.equals("underline")) {
			//in_underline=false;
			Underline  ul = new Underline();
			ul.chapterName = underline.get("chapter").toString();
			ul.description = underline.get("description").toString();
			ul.bookName = title;
			ul.span1 = Integer.valueOf(underline.get("span1").toString());
			ul.span2 = Integer.valueOf(underline.get("span2").toString());
			ul.idx1 = Integer.valueOf(underline.get("idx1").toString());
			ul.idx2 = Integer.valueOf(underline.get("idx2").toString());
			ul.bookType = 0;
			ul.epubPath = deliveryID;
			ul.content =underline.get("content").toString();
			
			cursorDBData.moveToFirst();
			for(int i=0;i<cursorDBData.getCount();i++){
				if(cursorDBData.getString(5).equals(deliveryID)){
					UnderlineDB udb = new UnderlineDB(ctx);
					udb.insertUnderline(ul,null);
					udb.closeDB();
					Log.e("underline ", " insert DB");
					break;
				}
				cursorDBData.moveToNext();
			}			

		}else if (localName.equals("span1")) {
			//in_span1=false;
			underline.put("span1", dataString);
		}else if (localName.equals("span2")) {
			//in_span2=false;
			underline.put("span2", dataString);
		}else if (localName.equals("idx1")) {
			//in_idx1=false;
			underline.put("idx1", dataString);
		}else if (localName.equals("idx2")) {
			//in_idx2=false;
			underline.put("idx2", dataString);
		}else if (localName.equals("bookmarks")) {
			//in_bookmarks=false;
		}else if (localName.equals("bookmark")) {
			//in_bookmark=false;
			if(bookmark.get("track")==null){
				Bookmark  bm = new Bookmark();
				if(bookmark.get("chapter")!=null)
				{
					bm.chapterName = bookmark.get("chapter").toString();
					bm.description = bookmark.get("description").toString();
					bm.bookName = title;
					bm.position1 = Integer.valueOf(bookmark.get("position1")
							.toString());
					bm.position2 = Integer.valueOf(bookmark.get("position2")
							.toString());
					bm.bookType = 0;
					bm.epubPath = deliveryID;

					cursorDBData.moveToFirst();
					for (int i = 0; i < cursorDBData.getCount(); i++) {
						if (cursorDBData.getString(5).equals(deliveryID)) {
							Bookmarks bmdb = new Bookmarks(ctx);
							bmdb.insertBookmark(bm);
							bmdb.closeDB();
							Log.e("bookmark epub", " insert DB");
							break;
						}
						cursorDBData.moveToNext();
					}
				}
				else//pdf
				{
					int nCount = pageResult.size();
					String strPage= null;// = (String) bookmark.get("page");
					
					for(int i= 0;i <nCount;i++)
					{
						strPage += "<page>";
						strPage += pageResult.get(i);
						strPage += "</page>"; 
					}
					//
					String aXML = "<bookmarks><bookmark>"+strPage+"</bookmark></bookmarks>";
					
					//deliveryID
					Cursor c = tdb.select2(deliveryID);
					if(c.getCount() >0)
					{
						c.moveToFirst();
						GSiDatabaseAdapter.setBookmark(ctx, deliveryID, aXML);
					}					
					
					
					
				}

			}else{
				//有聲書		
				
				SyBookmark bm = new SyBookmark(ctx, 0);
				String track = (String) bookmark.get("track");
				String value = (String) bookmark.get("value");
				bm.setBookmark(deliveryID , track , value);
				
				Log.e("bookmark audio ", " insert DB");
			}
		}else if (localName.equals("track")) {
			//in_track=false;
			bookmark.put("track", dataString);
		}else if (localName.equals("value")) {
			//in_value=false;
			bookmark.put("value", dataString);
		}else if(localName.equals("page")){
			if(in_notes==true){
				annotation.put("page", dataString);
			}else if(in_underlines==true){
				//underline.put("description", dataString);
			}else{
				bookmark.put("page", dataString);
				pageResult.add(dataString);
			}						
		}
	}
	/**
	 * TAG中間的字串處理
	 */
	public void characters(char ch[], int start, int length) {
		//Log.d("ParseServer",new String(ch, start, length));
        if(in_ebooks){
        	if(ch==null){
        		dataString+="";
        	}else{
        		dataString+=new String(ch, start, length);	
        	}   
        }
	}
}