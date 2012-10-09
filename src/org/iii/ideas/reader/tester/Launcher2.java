package org.iii.ideas.reader.tester;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.iii.ideas.reader.annotation.AnnotationDB;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.taiwanmobile.myBook_PAD.R;
import com.taiwanmobile.myBook_PAD.XmlParseHandler;

/**
 * 內部測試用launcher，列出sd card上的epub檔案，點選後進入閱讀介面
 * @author III
 * 
 */
public class Launcher2 extends Activity {
    /** Called when the activity is first created. */
    @Override   
    public void onCreate(Bundle savedInstanceState) {
    	//Log.d("In","Launcher2");
    	
    	Log.d("time","is:"+System.currentTimeMillis());
    	/** 
    	 * 改用listview列出sdcard根目錄檔案方便測試 
    	 * */
    	/*Log.d("c","is:"+Character.isLetter('c'));
    	Log.d("c","is:"+Character.isDigit('c'));
    	Log.d("c","is:"+Character.isJavaLetter('c'));
    	Log.d("you","is:"+Character.isLetter('你'));
    	Log.d("you","is:"+Character.isJavaLetter('你'));*/
    	//String test = "abcdefghijklmnopqrstuvwxyz";
    	/*float[] width = new float[26];
    	TextPaint tp = new TextPaint();
    	int count;
    	count=tp.breakText(test, 1, 26, true,100,width);
    	
    	StringBuilder result=new StringBuilder();
    	for(int i=0;i<width.length;i++){
    		result.append(width[i]).append(";");
    	}
    	Log.d("result1","is:"+count);
    	width = new float[26];
    	count=tp.breakText(test, 1, 4, true,100,width);
    	for(int i=0;i<width.length;i++){
    		result.append(width[i]).append(";");
    	}
    	Log.d("result2","is:"+count);*/
        super.onCreate(savedInstanceState);
        //testXmlParse();
        /*try {
			ZipFile zf = new ZipFile(new File("/sdcard/471000000010000085-00-01.teb"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Log.d("123","hash:"+"123".hashCode());
        Log.d("sfjisadjfoasd","hash:"+"sfjisadjfoasd".hashCode());
        Log.d("123","hash:"+"123".hashCode());
        Log.d("sfjisadjfoasd","hash:"+"sfjisadjfoasd".hashCode());*/
        setContentView(R.layout.iii_launcher);
    }
    
    @SuppressWarnings("unused")
	private void testXmlParse(){
    	try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader(); 
			
			AnnotationDB adb = new AnnotationDB(getBaseContext());
			adb.deleteAllAnn();
			adb.closeDB();
			Log.e("JP","delete all ann");
			XmlParseHandler handler = new XmlParseHandler(/*getBaseContext()*/ this);
			xr.setContentHandler(handler);
			xr.parse("file:///sdcard/test.xml");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override
    public void onResume(){
    	
    	super.onResume();
    	//Environment.getExternalStorageDirectory()
    	File sdRoot = Environment.getExternalStorageDirectory();
    	File sd2 = new File(sdRoot,"external_sd");
    	Log.d("sdroot","path:"+sdRoot.getAbsolutePath());
    	String[] rootFiles = sdRoot.list();
    	String[] rf2 = null;
    	if(sd2.exists())
    		rf2 = sd2.list();
        final ArrayList<String> fileArray = new ArrayList<String>();
        for(int i=0;i<rootFiles.length;i++){
        	if(rootFiles[i].endsWith(".epub") || rootFiles[i].endsWith(".html") || rootFiles[i].endsWith(".htm")){
        		fileArray.add(sdRoot.getAbsolutePath()+File.separator+rootFiles[i]);
        	}
        }
        if(rf2!=null){
        	for(int i=0;i<rf2.length;i++){
            	if(rf2[i].endsWith(".epub") || rf2[i].endsWith(".html") || rf2[i].endsWith(".htm")){
            		fileArray.add(sd2.getAbsolutePath()+File.separator+rf2[i]);
            	}
            }
        }
        
        ListView lv = (ListView) findViewById(R.id.lv);
        ArrayAdapter<String> ada = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,fileArray);
        lv.setAdapter(ada);
        lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				if(pos<fileArray.size()){
					if(fileArray.get(pos).endsWith(".epub")){
						//Intent it =(new Intent()).setData(Uri.parse("epub:///sdcard/"+fileArray.get(pos)));
						Intent it =(new Intent()).setData(Uri.parse("epub://"+fileArray.get(pos)));					
						it.putExtra("p12","");
				 		it.putExtra("isSample", false);
				 		it.putExtra("coverPath", "");
				 		it.putExtra("syncLastPage", false);//同步最後閱讀頁
				 		it.putExtra("content_id", "12345");
				 		it.putExtra("book_title", "Title");
				 		it.putExtra("book_authors", "Writer");
				 		it.putExtra("book_publisher", "who gives it a fuck");
				 		it.putExtra("book_category", "123");
				 		it.putExtra("book_vertical", false);
						startActivity(it);
					}else{
						//Intent it =(new Intent()).setData(Uri.parse("epub:///sdcard/"+fileArray.get(pos)));
						Intent it =(new Intent()).setData(Uri.parse("epub://"+fileArray.get(pos)));
						//it.putExtra("shouldJump", true);
						//it.putExtra("chapterNo", 1);
						//it.putExtra("pos", 0);
						//it.putExtra("fontSize", 0);
						//it.putExtra("verisimilitude",false);
						startActivity(it); 
						finish();
					}
				}
			}
        });
        //String testImg = "abcImG";
        //testImg.replaceFirst("[iI][mM][gG]", "img");
        //Log.d("after replace","is:"+testImg);
        //TextView tv = (TextView) findViewById(R.id.tv);
        //startActivity((new Intent()).setData(Uri.parse("file:///sdcard/FairytalesofhansChristianAndersen.epub")));
        //startActivity((new Intent()).setData(Uri.parse("file:///sdcard/TheCatcherintheRye.epub")));
        //startActivity((new Intent()).setData(Uri.parse("file:///sdcard/hong.epub")));
        //startActivity((new Intent()).setData(Uri.parse("file:///sdcard/2.epub")));
        //startActivity((new Intent()).setData(Uri.parse("file:///sdcard/paper.epub")));
        //startActivity((new Intent()).setData(Uri.parse("file:///sdcard/4 .epub")));
        //startActivity((new Intent()).setData(Uri.parse("file:///sdcard/yang.epub")));
        //startActivity((new Intent()).setData(Uri.parse("file:///sdcard/108.epub")));

       Log.v("ader","total memory1:"+Runtime.getRuntime().totalMemory());
      
       /*String m_sRoFileName = "/sdcard/DCF/99999999-9999-9999-9999-d0000000001_72.oro";
       String m_sCoFileName = "/sdcard/DCF/1.DCF";
       String drmFile = "/sdcard/drm.epub";
        DRMAgent agent = new DRMAgent("/sdcard/deviceCert","/sdcard/deviceKey");
        agent.DRM_VerifyPIN("123456".getBytes());

		//String m_sOutFileName = new String("drm.epub");
	
			try {

				agent.DRM_DecryptWholeFile(m_sCoFileName, m_sRoFileName, drmFile);
				

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			agent = null;
			//Runtime.getRuntime().gc();
			Log.v("ader","total memory2:"+Runtime.getRuntime().totalMemory());
			
			startActivity((new Intent()).setData(Uri.parse("file://"+drmFile)));
			*/
       
        //finish();
        ///startActivity((new Intent()).setData(Uri.parse("file:///sdcard/4.epub")));
        //startActivity((new Intent()).setData(Uri.parse("file:///sdcard/hongloumeng.epub")));
        //Log.v("Rona1","freeMemory: "+Runtime.getRuntime().freeMemory());
		//Log.v("Rona1","totalMemory: "+Runtime.getRuntime().totalMemory());
		//Log.v("Rona1","maxMemory: "+Runtime.getRuntime().maxMemory());
    }
}