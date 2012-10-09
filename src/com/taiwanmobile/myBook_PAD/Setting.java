package com.taiwanmobile.myBook_PAD;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlparser.Node;
import org.iii.ideas.reader.annotation.Annotation;
import org.iii.ideas.reader.annotation.AnnotationDB;
import org.iii.ideas.reader.bookmark.Bookmark;
import org.iii.ideas.reader.bookmark.Bookmarks;
import org.iii.ideas.reader.last_page.LastPageHelper;
import org.iii.ideas.reader.underline.Underline;
import org.iii.ideas.reader.underline.UnderlineDB;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import tw.com.soyong.AnReader;
import tw.com.soyong.utility.SyBookmark;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.gsimedia.gsiebook.common.Config;
import com.gsimedia.gsiebook.lib.GSiDatabaseAdapter;
import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.GSiMediaRegisterProcess.DataClass;
import com.gsimedia.sa.GSiMediaRegisterProcess.DeviceIDException;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;
import com.gsimedia.sa.GSiMediaRegisterProcess.IllegalNetworkException;
import com.gsimedia.sa.GSiMediaRegisterProcess.TimeOutException;
import com.gsimedia.sa.GSiMediaRegisterProcess.XmlException;
import com.gsimedia.sa.GSiMediaRegisterProcess.XmlP12FileException;
/**
 * 書櫃設定
 * @author III
 * 
 */
public class Setting extends Activity{
	private final class downloadAnnotation extends Thread {
		public void run(){
			String deviceID = "";
			try{
				deviceID = GSiMediaRegisterProcess.getID(getApplicationContext());
			}catch(Throwable e){};
	    	if (deviceID == null) deviceID = "";
	    	
			String uriAPI = getResources().getString(R.string.iii_twm_download_read_record)+"?device_id="+deviceID+"&token="+RealBookcase.getToken();     
			HttpPost httprequest = new HttpPost(uriAPI);
		    try {
//		    	HttpResponse httpResponse = new DefaultHttpClient().execute(httprequest);
//		    	SAXParserFactory spf = SAXParserFactory.newInstance();
//		    	SAXParser sp;
//		    	sp = spf.newSAXParser();
//		    	XMLReader xr = sp.getXMLReader(); 
//		    	
//		        AnnotationDB adb = new AnnotationDB(getBaseContext());
//		        adb.deleteAllAnn();
//		        adb.closeDB();
//		        Bookmarks bm= new Bookmarks(getBaseContext());
//		        bm.deleteAllBookmark();
//		        bm.closeDB();
//		        UnderlineDB ul= new UnderlineDB(getBaseContext());
//		        ul.deleteAllUnderline();
//		        ul.closeDB();	
//		        
//		        //write to file
//		        final HttpEntity entity = httpResponse.getEntity();
//		        InputStream is = entity.getContent();
//		         
//		        //paraser epub, mebook
//		    	XmlParseHandler handler = new XmlParseHandler(/*getBaseContext()*/ Setting.this);
//		    	xr.setContentHandler(handler);
//		    	xr.parse( new InputSource(is));
//		    	is.close();

			    	HttpResponse httpResponse = new DefaultHttpClient().execute(httprequest);
			    	SAXParserFactory spf = SAXParserFactory.newInstance();
			    	SAXParser sp;
			    	sp = spf.newSAXParser();
			    	XMLReader xr = sp.getXMLReader(); 
			    	
			        AnnotationDB adb = new AnnotationDB(getBaseContext());
			        adb.deleteAllAnn();
			        adb.closeDB();
			        Bookmarks bm= new Bookmarks(getBaseContext());
			        bm.deleteAllBookmark();
			        bm.closeDB();
			        UnderlineDB ul= new UnderlineDB(getBaseContext());
			        ul.deleteAllUnderline();
			        ul.closeDB();	
			        
			    	XmlParseHandler handler = new XmlParseHandler( Setting.this);
			    	
			    	//for test
			    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			    	DocumentBuilder db = dbf.newDocumentBuilder();
			    	InputStream is  = httpResponse.getEntity().getContent();
			    	Document doc = db.parse(is);
					NodeList nError = doc.getElementsByTagName("status");
					String ebook_error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
					
					NodeList nDesc = doc.getElementsByTagName("description");
					String ebook_description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();

					NodeList nData = doc.getElementsByTagName("data");
					String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
					InputStream dataStream = covertStringToStream(RevData);
					
				if (ebook_error.equals("1")) {
					xr.setContentHandler(handler);
//					threadTestttMsg(getResources().getString(
//							R.string.iii_read_record_download_ok));
					xr.parse(new InputSource(dataStream));
					threadTestttMsg(getResources().getString(
							R.string.iii_read_record_download_ok));
				} else {
					if(deviceID.length() == 0){
						threadTestttMsg(getResources().getString(
								R.string.GSI_DEVICE_ID_EMPTY_MSG));
					}else{
					threadHandleMsg(download_annotation, ebook_description);
				}
				}
		    	
		    } catch (ParserConfigurationException e) {
		    	//TODO Auto-generated catch block
		    	threadTestttMsg(getResources().getString(R.string.iii_read_record_download_error)+1);	        	
		    } catch (SAXException e) {
		    	// 	TODO Auto-generated catch block
		    	threadTestttMsg(getResources().getString(R.string.iii_read_record_download_error)+2);
		    } catch (IllegalStateException e) {
		    	// TODO Auto-generated catch block
		    	threadTestttMsg(getResources().getString(R.string.iii_read_record_download_error)+3);
		    } catch (Exception e) {
		    	// TODO Auto-generated catch block
		    	e.printStackTrace();
		    	threadTestttMsg(getResources().getString(R.string.iii_read_record_download_error)+4);
		    } 
		}
	}
	private final class backupAnnotation extends Thread {
		private String ebook_error;
		private String ebook_description;

		public void run(){
			try{
				cursorDBData = tdb.select("isDownloadBook = '1'");
				
				cursorDBData.moveToFirst();
				StringBuffer sbTemp = new StringBuffer();
				sbTemp.append("<?xml version='1.0' encoding='UTF-8'?>");
				sbTemp.append("<ebooks>");
				sbTemp.append("<error>0</error>");
				//String temp = "";
				tdb.close();
				//Log.e("xxxxxxxxxxxxxxxxxxxxxx",saveFilelocation+cursorDBData.getString(5)+".epub");
				//Log.e("xxxxxxxxxxxxxxxxxxxxxx",String.valueOf(cursorDBData.getCount()));
				for(int i=0;i<cursorDBData.getCount();i++){
					if(cursorDBData.getString(21).equals(fileType[1])){//mebook
						
						sbTemp.append("<ebook>");
						sbTemp.append("<Delivery-ID>");
						sbTemp.append(cursorDBData.getString(5));
						sbTemp.append("</Delivery-ID>");
						sbTemp.append("<title>");
						sbTemp.append(cursorDBData.getString(1));
						sbTemp.append("</title>");
						
						
						SyBookmark bm = new SyBookmark(Setting.this, 0); 						
						String xml = bm.getBookmarkXml(cursorDBData.getString(5)); 						  
						sbTemp.append(xml);
						
						sbTemp.append("</ebook>");  
						
					}else if (cursorDBData.getString(21).equals(fileType[2])){//PDF
						
						sbTemp.append("<ebook>");
						sbTemp.append("<Delivery-ID>");
						sbTemp.append(cursorDBData.getString(5));
						sbTemp.append("</Delivery-ID>");
						sbTemp.append("<title>");
						sbTemp.append(cursorDBData.getString(1));
						sbTemp.append("</title>");
						
			    		Context context = getApplicationContext();
			    		String aXml = GSiDatabaseAdapter.getBookmarkXml(context, cursorDBData.getString(5));
			    		sbTemp.append(aXml);
			    		//Log.d(Config.LOGTAG,aID+":"+aXml);
			    		//GSiDatabaseAdapter.setBookmark(context, cursorDBData.getString(5), aXml);
			    		aXml = GSiDatabaseAdapter.getAnnotationXml(context, cursorDBData.getString(5));
			    		sbTemp.append(aXml);
			    		sbTemp.append("<pdfmarkers>");
			    		aXml = GSiDatabaseAdapter.getMarkerXml(context, cursorDBData.getString(5));			    		
			    		sbTemp.append(aXml);
			    		sbTemp.append("</pdfmarkers>");
			    		//Log.d(Config.LOGTAG,aID+":"+aXml);
			    		//GSiDatabaseAdapter.setAnnotation(context, cursorDBData.getString(5), aXml);
			    		sbTemp.append("</ebook>");  
					}else{
						sbTemp.append("<ebook>");
						sbTemp.append("<Delivery-ID>");
						sbTemp.append(cursorDBData.getString(5));
						sbTemp.append("</Delivery-ID>");
						sbTemp.append("<title>");
						sbTemp.append(cursorDBData.getString(1));
						sbTemp.append("</title>");
						
						AnnotationDB adb = new AnnotationDB(getBaseContext());		
						Log.e("xxxxxxxxxxxxxxxxxxxxxx",saveFilelocation+cursorDBData.getString(5)+".epub");
						ArrayList<Annotation> ann = adb.getAnnsByEpubPath(cursorDBData.getString(5));
						adb.closeDB();
						Log.e("111", "111");
						Log.e(saveFilelocation+cursorDBData.getString(5)+".epub",saveFilelocation+cursorDBData.getString(5)+".epub");
						if(ann.size()>0){
							Log.e("ann.size()", String.valueOf(ann.size()));
							sbTemp.append("<notes>");
							Log.e("1", "1");
							for(int j=0;j<ann.size();j++){
								sbTemp.append("<note>");
								sbTemp.append("<chapter>");
								sbTemp.append(ann.get(j).chapterName.toString());
								Log.e("2", "2");
								sbTemp.append("</chapter>");
								sbTemp.append("<position1>");
								sbTemp.append(String.valueOf(ann.get(j).position1));
								Log.e("3", "3");
								Log.e(String.valueOf(ann.get(j).position1), String.valueOf(ann.get(j).position1));
								sbTemp.append("</position1>");
								sbTemp.append("<position2>");
								sbTemp.append(String.valueOf(ann.get(j).position2));
								Log.e("4", "4");
								Log.e(String.valueOf(ann.get(j).position2), String.valueOf(ann.get(j).position2));
								sbTemp.append("</position2>");
								sbTemp.append("<percentage>");
								sbTemp.append("</percentage>");
								sbTemp.append("<content>");
								sbTemp.append(ann.get(j).content.toString());
								Log.e("5", "5");
								
								if(ann.get(j).description==null){
									Log.e("null", "5");
								}
								Log.e(ann.get(j).content.toString(), ann.get(j).content.toString());
								sbTemp.append("</content>");
								sbTemp.append("<description>");
								Log.e(ann.get(j).description.toString(), ann.get(j).description.toString());
								sbTemp.append(ann.get(j).description.toString());
								Log.e("6", "6");
								Log.e("6", "6");
								sbTemp.append("</description>");
								
								sbTemp.append("</note>");
							}
							sbTemp.append("</notes>");		
							Log.e("7", "7");
						}
						//cursorDBData.close();

						Log.e("222", "222");
						UnderlineDB udb = new UnderlineDB(getBaseContext());		
						
						ArrayList<Underline> ul = udb.getUnderlineByEpubPath(cursorDBData.getString(5));
						udb.closeDB();	
						if(ul.size()>0){
							sbTemp.append("<underlines>");
							for(int j=0;j<ul.size();j++){
								sbTemp.append("<underline>");
								sbTemp.append("<chapter>");
								sbTemp.append(ul.get(j).chapterName.toString());
								sbTemp.append("</chapter>");
								sbTemp.append("<span1>");
								sbTemp.append(String.valueOf(ul.get(j).span1));
								sbTemp.append("</span1>");
								sbTemp.append("<span2>");
								sbTemp.append(String.valueOf(ul.get(j).span2));
								sbTemp.append("</span2>");
								sbTemp.append("<idx1>");
								sbTemp.append(String.valueOf(ul.get(j).idx1));
								sbTemp.append("</idx1>");
								sbTemp.append("<idx2>");
								sbTemp.append(String.valueOf(ul.get(j).idx2));
								sbTemp.append("</idx2>");			
								sbTemp.append("<position1>");
								sbTemp.append("</position1>");
								sbTemp.append("<position2>");
								sbTemp.append("</position2>");						
								sbTemp.append("<content>");
								sbTemp.append(ul.get(j).content.toString());
								sbTemp.append("</content>");
								sbTemp.append("<description>");
								sbTemp.append(ul.get(j).description.toString());
								sbTemp.append("</description>");			
								sbTemp.append("</underline>");
							}
							sbTemp.append("</underlines>");
						}
						Log.e("333", "333");
						//cursorDBData.close();
						Bookmarks bdb = new Bookmarks(getBaseContext());						
						ArrayList<Bookmark> bl = bdb.getBookmarksByEpubPath(cursorDBData.getString(5));
						bdb.closeDB();	
						if(bl.size()>0){
							sbTemp.append("<bookmarks>");
							for(int j=0;j<bl.size();j++){
								sbTemp.append("<bookmark>");
								sbTemp.append("<chapter>");
								sbTemp.append(bl.get(j).chapterName.toString());
								sbTemp.append("</chapter>");
								sbTemp.append("<position1>");
								sbTemp.append(String.valueOf(bl.get(j).position1));
								sbTemp.append("</position1>");
								sbTemp.append("<position2>");
								sbTemp.append(String.valueOf(bl.get(j).position2));
								sbTemp.append("</position2>");
								sbTemp.append("<percentage>");
								sbTemp.append("</percentage>");
								sbTemp.append("<description>");
								sbTemp.append(bl.get(j).description.toString());
								sbTemp.append("</description>");			
								sbTemp.append("</bookmark>");
							}
							sbTemp.append("</bookmarks>");
						}
						//cursorDBData.close();
						
						
						sbTemp.append("</ebook>");
					}
					cursorDBData.moveToNext();
				}	
				//Log.e("bbb.getCount()",String.valueOf(adb.getCount()));
				//Log.e("bbb.getCount()",cursorDBData.getString(5));
				Log.e("444", "444");
				sbTemp.append("</ebooks>");
				Log.e("sbTemp", sbTemp.toString());
				//http://124.29.140.83/DeliverWeb/allupload
				Setting.this.stopManagingCursor(cursorDBData);
				cursorDBData.close();
//				URL url= new URL(getResources().getString(R.string.iii_twm_backup_read_record)+"?device_id="+GSiMediaRegisterProcess.getID(getBaseContext()));
//				URL myURL= new URL(getResources().getString(R.string.iii_twm_backup_read_record)+"?device_id="+GSiMediaRegisterProcess.getID(getBaseContext())+"&token="+RealBookcase.getToken());
//				
//				HttpURLConnection con =(HttpURLConnection)url.openConnection();
//				con.setDoInput (true);
//				con.setDoOutput (true);
//				con.setUseCaches (false);
//				con.setRequestProperty("Content-Type", "application/soap+xml;charset=UTF-8");
//				con.setRequestProperty("Accept","text/html,application/xml,application/xhtml+xml,text/html");
//				con.setRequestProperty("Accept-Charset", "UTF-8");
//
//				Log.e("0", "TTTTTTTTTTTTTTTTTTTT");
//				DataOutputStream ds = new DataOutputStream(con.getOutputStream());
//				//Log.e("sbTemp.toString().getBytes(UTF-8).toString()",sbTemp.toString().getBytes("UTF-8").toString() );
//				ds.writeUTF(sbTemp.toString());
//				ds.flush();
//				InputStream is = con.getInputStream();
//				StringBuffer sb = new StringBuffer();
//				//FileOutputStream fos = new FileOutputStream(saveFilelocation+"cvcvcv.txt");
//				byte[] buf = new byte[1024];
//				while (true) {
//					int bytesRead = is.read(buf);
//					if (bytesRead == -1)
//						break;
//					sb.append(new String(buf));								    			
//				}
//				
//				con.disconnect();
				HttpURLConnection conn = null;
				String deviceID = "";
				try{
					deviceID = GSiMediaRegisterProcess.getID(getApplicationContext());
				}catch(Throwable e){};
		    	if (deviceID == null) deviceID = "";
		    	
				URL myURL= new URL(getResources().getString(R.string.iii_twm_backup_read_record)+"?device_id="+deviceID+"&token="+RealBookcase.getToken());
				
				String sbVal = "xml="+sbTemp;
				conn = (HttpURLConnection) myURL.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Charset", "UTF-8");
				conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-Language", "UTF-8");				
				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.setDoOutput(true);
				
				DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
				wr.write(sbVal.getBytes("UTF-8"));
				wr.flush();
				wr.close();
				
				int resp = conn.getResponseCode();
				if (resp != HttpURLConnection.HTTP_OK) {
					threadTestttMsg(getResources().getString(
							R.string.iii_NetworkNotConnMessage));
					return;
				}
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream is = conn.getInputStream();
				Document doc = db.parse(is);
				NodeList nError = doc.getElementsByTagName("status");
				ebook_error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
				
				NodeList nDesc = doc.getElementsByTagName("description");
				ebook_description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();

				NodeList nData = doc.getElementsByTagName("data");
				String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
				InputStream dataStream = covertStringToStream(RevData);
				conn.disconnect();
				
				if (ebook_error.equals("1")) {
					StringBuffer sb = new StringBuffer();
					byte[] buf = new byte[1024];
					while (true) {
						int bytesRead = dataStream.read(buf);
						if (bytesRead == -1)
							break;
						sb.append(new String(buf));
					}

					// fos.close();
					if (sb.toString().indexOf(
							"<response><error>0</error></response>") > 0)
						threadTestttMsg(getResources().getString(
								R.string.iii_read_record_backup_ok));
					else
						threadTestttMsg(getResources().getString(
								R.string.iii_read_record_backup_error));
				}
				else{
					if(deviceID.length() == 0){
						threadTestttMsg(getResources().getString(
								R.string.GSI_DEVICE_ID_EMPTY_MSG));
					}else{
					threadHandleMsg(backup_annotation,ebook_description);
				}
				}
			}catch(Exception e){
				System.out.println(e);
				e.printStackTrace();
				threadTestttMsg(getResources().getString(R.string.iii_read_record_backup_error));
			}
		}


	}

	private final byte download_annotation = 0;
	private final byte backup_annotation = 1;
	
	private ImageButton iv_setting_up;
	private ImageView iv_setting_default;
	private ProgressDialog pDialog;
	//private String[] fileType = {".teb",".tvb"};
	private TextView 
					tv_setting_bookcase_background_style,tv_setting_bookcase_background_style_value,
					tv_setting_auto_sync_new_book,tv_setting_auto_sync_last_read_page,
					tv_setting_backup_read_record,
					tv_setting_download_read_record,
					tv_setting_del_all_download_book,
					tv_setting_re_register,
					tv_setting_about;
	
	private Button but_setting_backup_read_record_value,but_setting_download_read_record_value,but_setting_del_all_download_book_value,
					but_setting_re_register_value;
	
	private CheckBox cb_setting_auto_sync_new_book_value,cb_setting_auto_sync_last_read_page_value;

	private ListView lv_setting_bookcase_background_style_value;
	
	private String[] settingItem;
	
	private SharedPreferences settings;		
	
	private TableRow tr_setting_bookcase_background_style,tr_setting_auto_sync_new_book,tr_setting_auto_sync_last_read_page,
						tr_setting_backup_read_record,tr_setting_download_read_record,
						tr_setting_del_all_download_book,tr_setting_re_register,tr_setting_about;	
	
	private String saveFilelocation="";
	
	private TWMDB tdb = null;
	private Cursor cursorDBData;
/*	private ListView lv_setting;
	private mySettingList msl;
	private SettingView[] settingRow = null;	
	private String[] settingItem;*/
	private Context ctx;
	private String[] fileType = {".teb",".tvb",".tpb",};
	
	public InputStream covertStringToStream(String s){
		/*
		* Convert String to InputStream using ByteArrayInputStream
		* class. This class constructor takes the string byte array
		* which can be done by calling the getBytes() method.
		*/
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return is;		
	}
	/**
	 * 換行符號替代文字
	 */
	public static final String LINE_FEED_ALT = ":~lf~:";
	
	@Override
	protected void onCreate(Bundle icicle){
		super.onCreate(icicle);    
		setContentView(R.layout.iii_setting);
		saveFilelocation = this.getIntent().getExtras().getString("saveFilelocation");
		ctx = this.getBaseContext();
		tdb = new TWMDB(this);
        setViewComponent();
        
        setListener();

        setPreferences();
        
	}
	/**
	 * 初始設定
	 */
	private void setPreferences() {
		// TODO Auto-generated method stub

		settings = getSharedPreferences("setting_Preference", 0);
		
		if("".equals(settings.getString("setting_bookcase_background_style_value", ""))){
			String[] temp = getResources().getStringArray(R.array.iii_bookcase_background_style_value);
			tv_setting_bookcase_background_style_value.setText(temp[0]);
			settings.edit().putString("setting_bookcase_background_style_value", temp[0]).commit();
		}else{
			tv_setting_bookcase_background_style_value.setText(settings.getString("setting_bookcase_background_style_value", ""));
		}
		
		cb_setting_auto_sync_new_book_value.setChecked(settings.getBoolean("setting_auto_sync_new_book_value", true));
		settings.edit().putBoolean("setting_auto_sync_new_book_value", settings.getBoolean("setting_auto_sync_new_book_value", true)).commit();
		
		cb_setting_auto_sync_last_read_page_value.setChecked(settings.getBoolean("setting_auto_sync_last_read_page_value", true));
		settings.edit().putBoolean("setting_auto_sync_last_read_page_value", settings.getBoolean("setting_auto_sync_last_read_page_value", true)).commit();
			
	}
	
	private void threadTestttMsg(String msg) {
		Message m = new Message();
		Bundle data = m.getData();
		data.putString("msg", msg);
		testtt.sendMessage(m);
	}
	private void threadHandleMsg(byte type, String msg) {
		Message m = new Message();
		Bundle data = m.getData();
		data.putString("msg", msg);
		m.setData(data);
		if(type == download_annotation )
			mHandlerdownload.sendMessage(m);
		else if(type == backup_annotation)
			mHandlerbackup.sendMessage(m);
		else{
			//do not thing
		}
	
	}	
	private Handler testtt = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			String msg2 = msg.getData().getString("msg");
			pDialog.dismiss();
			if(msg2.equals("")){
				
			}else{
				showAlertMessage(msg2);
			}				
		}
	};
    private final String TAG_BOOKMARK = "bookmark";
    private final String TAG_PAGE = "page";
    private final String TAG_NOTE = "note";
    private final String TAG_CONTENT = "content";
	private Handler mHandlerdownload = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Bundle bd = msg.getData();
			String desc = bd.getString("msg");
			new AlertDialog.Builder(Setting.this)
			.setTitle(R.string.msgbox_expire_title)
			.setMessage(desc)//R.string.msgbox_expire_content)
			.setPositiveButton(R.string.iii_showAM_ok,
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialoginterface, int i){
							//retrieve token
							//startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(url)) );
							
							if(pDialog != null || pDialog.isShowing()){
								pDialog.dismiss();
								pDialog = null;
							}
							RealBookcase.AuthSSOLogin(Setting.this,RealBookcase.nonebooktype,null,null);
						}
					}
			)
			.setNegativeButton(R.string.iii_showAM_cancel,
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialoginterface, int i){
							//cancel
							//finish();
							if(pDialog != null){
								pDialog.dismiss();
								pDialog = null;
							}
						}
				}
			)				
			.show();		
		}
	};	
	private Handler mHandlerbackup = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			Bundle bd = msg.getData();
			String desc = bd.getString("msg");
			new AlertDialog.Builder(Setting.this)
			.setTitle(R.string.msgbox_expire_title)
			.setMessage(desc)//R.string.msgbox_expire_content)
			.setPositiveButton(R.string.iii_showAM_ok,
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialoginterface, int i){
							//retrieve token
							//startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(url)) );
    	
							if(pDialog != null || pDialog.isShowing()){
								pDialog.dismiss();
								pDialog = null;
							}
							RealBookcase.AuthSSOLogin(Setting.this,RealBookcase.nonebooktype,null,null);
						}
					}
			)
			.setNegativeButton(R.string.iii_showAM_cancel,
					new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialoginterface, int i){
							//cancel
							//finish();
							if(pDialog != null){
								pDialog.dismiss();
								pDialog = null;
							}
						}
				}
			)				
			.show();		
		}
	};	
	/**
	 * 設定view Listener
	 */
	private void setListener() {
		// TODO Auto-generated method stub
		iv_setting_up.setOnClickListener(new ImageView.OnClickListener(){
			public void onClick(View v){
				if ( null != tdb ){
					tdb.close();
					tdb = null;
				}
				finish();
      	  	}
        });
		
		tr_setting_bookcase_background_style.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
					v.setBackgroundColor(Color.LTGRAY);
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});
		
		tr_setting_bookcase_background_style.setOnClickListener(new TableLayout.OnClickListener(){
			public void onClick(View v){
				lv_setting_bookcase_background_style_value.setVisibility(View.VISIBLE);
      	  	}
        });
		
		lv_setting_bookcase_background_style_value.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String[] temp = getResources().getStringArray(R.array.iii_bookcase_background_style_value);
				settings.edit().putString("setting_bookcase_background_style_value", temp[arg2]).commit();
				lv_setting_bookcase_background_style_value.setVisibility(View.GONE);
				tv_setting_bookcase_background_style_value.setText(temp[arg2]);
			}
		});
		
		tr_setting_auto_sync_new_book.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});
		
		cb_setting_auto_sync_new_book_value.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
	        	settings.edit().putBoolean("setting_auto_sync_new_book_value", isChecked).commit();		
			}
		});
		
		tr_setting_auto_sync_last_read_page.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});		
		
		cb_setting_auto_sync_last_read_page_value.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
	        	settings.edit().putBoolean("setting_auto_sync_last_read_page_value", isChecked).commit();			
			}
		});		
		
/*		tr_setting_notification_visible.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});			
		
		cb_setting_notification_visible_value.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
	        	settings.edit().putBoolean("setting_notification_visible_value", isChecked).commit();			
			}
		});		*/	
		
		tr_setting_backup_read_record.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});				
		
		but_setting_backup_read_record_value.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v){
				
				new AlertDialog.Builder(Setting.this)
				.setTitle(R.string.iii_check_backup_record_title)
				.setMessage(R.string.iii_check_backup_record)
				.setPositiveButton(R.string.iii_showAM_ok,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){
								pDialog = ProgressDialog.show(Setting.this, "", getResources().getString(R.string.iii_read_record_backuping));
								pDialog.setCancelable(false);
								
						        new backupAnnotation().start();	
							}
						}
				)
				.setNegativeButton(R.string.iii_showAM_cancel,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){

							}
					}
				)				
				.show();	
      	  	}
        });
		
		tr_setting_download_read_record.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});	
		

	    
		but_setting_download_read_record_value.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v){
				
				new AlertDialog.Builder(Setting.this)
				.setTitle(R.string.iii_check_download_record_title)
				.setMessage(R.string.iii_check_download_record)
				.setPositiveButton(R.string.iii_showAM_ok,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){
								pDialog = ProgressDialog.show(Setting.this, "", getResources().getString(R.string.iii_read_record_downloading));
								pDialog.setCancelable(false);
								
								new downloadAnnotation().start();	
							}
						}
				)
				.setNegativeButton(R.string.iii_showAM_cancel,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){

							}
					}
				)				
				.show();		
      	  	}
        });
		
		tr_setting_del_all_download_book.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});			
		
		but_setting_del_all_download_book_value.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v){
				new AlertDialog.Builder(Setting.this)
				.setTitle(R.string.iii_del_all_book)
				.setMessage(R.string.iii_del_all_book_message)
				.setPositiveButton(R.string.iii_showAM_ok,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){
								pDialog = ProgressDialog.show(Setting.this, "", getResources().getString(R.string.iii_setting_deling));
								pDialog.setCancelable(false);
								new Thread(){
									public void run(){
										cursorDBData = tdb.select();
										cursorDBData.moveToFirst();
										for(int j=0;j<cursorDBData.getCount();j++){
											tdb.delete(cursorDBData.getInt(0));
											new File(cursorDBData.getString(9)).delete();
											new File(cursorDBData.getString(19)).delete();
											new File(cursorDBData.getString(19)+".tmp").delete();
											new File(cursorDBData.getString(19).substring(0, cursorDBData.getString(19).lastIndexOf(".")) + ".epub").delete();
											SyBookmark bm = new SyBookmark(ctx, 0);
											bm.delBookmark(cursorDBData.getString(5));
										    AnReader.deleteLastPageOfBook(ctx, cursorDBData.getString(5));
											cursorDBData.moveToNext();
											
                                           
										}
										Setting.this.stopManagingCursor(cursorDBData);
										cursorDBData.close();
									    AnnotationDB adb = new AnnotationDB(getBaseContext());
									    adb.deleteAllAnn();
									    adb.closeDB();
									    Bookmarks bm= new Bookmarks(getBaseContext());
									    bm.deleteAllBookmark();
									    bm.closeDB();
									    UnderlineDB ul= new UnderlineDB(getBaseContext());
									    ul.deleteAllUnderline();
									    ul.closeDB();	
									    LastPageHelper.deleteAllLastPage(getBaseContext());
									    
							    		GSiDatabaseAdapter.delAllBookmark(ctx);
							    		GSiDatabaseAdapter.delAllAnnotation(ctx);
							    		GSiDatabaseAdapter.delAllMarker(ctx);
							    		GSiDatabaseAdapter.deleteAllLastPage(ctx);
									    threadTestttMsg(getResources().getString(R.string.iii_setting_del_done));
									}										
								}.start();	
							}
						}
				)
				.setNegativeButton(R.string.iii_showAM_cancel,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){

							}
					}
				)				
				.show();		
				
		 			
		 		//同時刪除圖片
/*		        for(int i=0;i<new File(saveFilelocation).list().length;i++){
		        	Log.e("", new File(saveFilelocation).list()[i]);
		        	new File(saveFilelocation+new File(saveFilelocation).list()[i]).delete();
		        }
		        new File(saveFilelocation).delete();	*/	
      	  	}
        });
		
		tr_setting_re_register.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});			
		
		but_setting_re_register_value.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v){
				new AlertDialog.Builder(Setting.this)
				.setTitle(R.string.iii_re_register)
				.setMessage(R.string.iii_re_register_message)
				.setPositiveButton(R.string.iii_showAM_ok,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){
								register(Setting.this.getFilesDir().toString());	
								Log.e(Setting.this.getFilesDir().toString(), Setting.this.getFilesDir().toString());
							}
						}
				)
				.setNegativeButton(R.string.iii_showAM_cancel,
						new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialoginterface, int i){

							}
					}
				)				
				.show();				
      	  	}
        });
		
		iv_setting_default.setOnClickListener(new ImageView.OnClickListener(){
			public void onClick(View v){
				iv_setting_default.setEnabled(false);
				iv_setting_default.postDelayed(new Runnable(){ 
					@Override 
					public void run() { 
						iv_setting_default.setEnabled(true);
					} 
				},400);
				setDefault();
				Toast.makeText(getApplicationContext(),R.string.iii_setting_default, Toast.LENGTH_SHORT).show();
      	  	}
        });
		
		/*		tr_setting_intoTWM.setOnTouchListener(new TableLayout.OnTouchListener(){
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				clear();
			}
			if (event.getAction() == MotionEvent.ACTION_UP){
				clear();
			}
			return false;
		}			
	});	
	
	tv_setting_intoTWM.setOnClickListener(new TextView.OnClickListener(){
		public void onClick(View v){
			clear();
			startActivity( (new Intent()).setAction(Intent.ACTION_VIEW).setData(Uri.parse(getResources().getString(R.string.iii_book_city_setting_url))) );
  	  	}		
	});	*/
		
		tr_setting_about.setOnTouchListener(new TableLayout.OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					clear();
				}
				if (event.getAction() == MotionEvent.ACTION_UP){
					clear();
				}
				return false;
			}			
		});	
		
		tv_setting_about.setOnClickListener(new TextView.OnClickListener(){
			public void onClick(View v){
				clear();
//				tr_setting_about.setBackgroundColor(Color.LTGRAY);
//				View convertView = LayoutInflater.from(Setting.this).inflate(R.layout.iii_about, null);				
//				new AlertDialog.Builder(Setting.this)
//				.setTitle(R.string.iii_about_twm)
//				.setView(convertView)
//				.setPositiveButton(R.string.iii_showAM_ok,
//						new DialogInterface.OnClickListener(){
//							public void onClick(DialogInterface dialoginterface, int i){
// 
//							}
//						}
//				)
//				.show();
				
				
				
				Intent it = new Intent(Setting.this, AboutActivity.class);
				startActivity(it);	
      	  	}		
		});			
	}
	
	/**
	 * 清除每個項目底紋
	 */
	
	public void clear(){
		tr_setting_bookcase_background_style.setBackgroundColor(Color.TRANSPARENT);
		tr_setting_auto_sync_new_book.setBackgroundColor(Color.TRANSPARENT);
		tr_setting_auto_sync_last_read_page.setBackgroundColor(Color.TRANSPARENT);
		//tr_setting_notification_visible.setBackgroundColor(Color.TRANSPARENT);
		tr_setting_backup_read_record.setBackgroundColor(Color.TRANSPARENT);
		tr_setting_download_read_record.setBackgroundColor(Color.TRANSPARENT);
		tr_setting_del_all_download_book.setBackgroundColor(Color.TRANSPARENT);
		tr_setting_re_register.setBackgroundColor(Color.TRANSPARENT);
		tr_setting_about.setBackgroundColor(Color.TRANSPARENT);
	}
	
	/**
	 * register
	 * @param path p12路徑
	 */
	public void register(String path){
		try {
			DataClass dataclass = GSiMediaRegisterProcess.register(path, Setting.this,RealBookcase.getToken());
			if(dataclass !=  null) {
				String disString = "";
				if(dataclass.resultCode_P12 != null){
					disString += "resultCode_P12= ";
					disString += dataclass.resultCode_P12;
				}
				if(dataclass.resultCode_Domain != null){
					disString += ", resultCode_Domain= ";
					disString += dataclass.resultCode_Domain;
				}
				if(dataclass.domain != null){
					disString += ", domain= ";
					disString += dataclass.domain;
				}
				if(dataclass.index != null && dataclass.index.length > 0){
					disString += ", index[0]= ";
					disString += dataclass.index[0];
				}
				if(dataclass.type != null && dataclass.type.length > 0){
					disString += ", type[0]= ";
					disString += dataclass.type[0];
				}
				if(dataclass.index != null && dataclass.index.length > 1){
					disString += ", index[1]= ";
					disString += dataclass.index[1];
				}
				if(dataclass.type != null && dataclass.type.length > 1){
					disString += ", type[1]= ";
					disString += dataclass.type[1];
				}
				if(dataclass.index !=null && dataclass.index.length > 2){
					disString += ", index[2]= ";
					disString += dataclass.index[2];
				}
				if(dataclass.type != null && dataclass.type.length > 2){
					disString += ", type[2]= ";
					disString += dataclass.type[2];
				}
				checkResultCode(1, dataclass.resultCode_P12,dataclass.resultCode_Domain,dataclass.index,dataclass.type);
			}else {
				Log.d("dataclass index =","null");
			}
		} catch (IllegalNetworkException e) {
			// TODO Auto-generated catch block
			showAlertMessage(getResources().getString(R.string.iii_net_not_conn));
			e.printStackTrace();
		} catch (TimeOutException e) {
			// TODO Auto-generated catch block
			showAlertMessage(getResources().getString(R.string.iii_server_time_out));
			e.printStackTrace();
		} catch (XmlP12FileException e) {
			// TODO Auto-generated catch block		
			showAlertMessage(getResources().getString(R.string.iii_get_p12_error));
			e.printStackTrace();
		} catch (DeviceIDException e) {
			//showAlertMessage(getResources().getString(R.string.iii_get_deviceID_error));
			showAlertMessage(getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG));			
			e.printStackTrace();	
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			showAlertMessage(getResources().getString(R.string.iii_server_return_error));
			e.printStackTrace();	
		}catch (Exception e) {
			// TODO Auto-generated catch block
			showAlertMessage(getResources().getString(R.string.iii_register_error));
			e.printStackTrace();
		}	
	}
	/**
	 * 依據server回傳的code做出不同動作
	 * @param model 0 checkDomain   1 register  3 manageDomain
	 * @param resultCode_P12 resultCode_P12
	 * @param resultCode_Domain resultCode_Domain
	 * @param index 手機id
	 * @param type 手機名稱
	 */
	public void checkResultCode(int model , String resultCode_P12,String resultCode_Domain,String[] index,String[] type){
		String resultCode = "";
		if(model == 0){
			if(resultCode_Domain == null){
				resultCode = resultCode_P12;
			}else{
				resultCode = resultCode_Domain;
			}			
		}else if(model == 1){
			resultCode = resultCode_P12;
		}else if(model == 3){
			resultCode = resultCode_Domain;
		}

		//resultCode_P12= -8
		
		if(resultCode.equals("0")){
			if(model == 1){
				if(resultCode_Domain.equals("-1")){
					showAlertMessage(getResources().getString(R.string.iii_register_error));				
				}else if(resultCode_Domain.equals("-3")){
					showAlertMessage(getResources().getString(R.string.iii_register_num_over_limit));
				}else{
					showAlertMessage(getResources().getString(R.string.iii_p12_download_ok));
				}/*else if(resultCode_Domain.equals("-10")){
					Message m = new Message();
					Bundle data = m.getData();				
					data.putString("msg", "");
					testtt.sendMessage(m);	
				}else {
					Message m = new Message();
					Bundle data = m.getData();				
					data.putString("msg", "");
					testtt.sendMessage(m);	
				}*/
			}/*else if( model == 0 ){
				Message m = new Message();
				Bundle data = m.getData();				
				data.putString("msg", "");
				testtt.sendMessage(m);	
			}else if(model == 3){
				Message m = new Message();
				Bundle data = m.getData();
				data.putString("msg", "");
				testtt.sendMessage(m);
			}*/
		}else if(resultCode.equals("-10")){
			if(model == 1){
				if(resultCode_Domain.equals("-1")){
					showAlertMessage("test1");		
				}else if(resultCode_Domain.equals("-3")){
					showAlertMessage("test2");
				}else{
					showAlertMessage("test3");
				}
			}
		}else if(resultCode.equals("-1")){
			showAlertMessage(getResources().getString(R.string.iii_register_num_over_limit));
		}else if(resultCode.equals("-8")){
			showAlertMessage(getResources().getString(R.string.iii_plz_use_twm_net));
		}else if(resultCode.equals("-6")){
			showAlertMessage(getResources().getString(R.string.iii_server_busy_notregister));
		}else if(resultCode.equals("-7")||resultCode.equals("-9")){
			showAlertMessage(getResources().getString(R.string.iii_server_register_error));
		}else if(resultCode.equals("-3")){
			showAlertMessage(getResources().getString(R.string.iii_register_num_over_limit));
		}else if(resultCode.equals("-4")){
			showAlertMessage(getResources().getString(R.string.iii_cant_update_phone));
		}else{
			showAlertMessage(getResources().getString(R.string.iii_register_error));
		}		
	}
	/**
	 * 回復預設值
	 */
	public void setDefault(){
		
		String[] temp = getResources().getStringArray(R.array.iii_bookcase_background_style_value);
		tv_setting_bookcase_background_style_value.setText(temp[0]);
		settings.edit().putString("setting_bookcase_background_style_value", temp[0]).commit();
		
		cb_setting_auto_sync_new_book_value.setChecked(true);
		settings.edit().putBoolean("reader_setting_auto_sync_new_book_value", true).commit();
		
		cb_setting_auto_sync_last_read_page_value.setChecked(true);
		settings.edit().putBoolean("reader_setting_auto_sync_last_read_page_value", true).commit();		
				
	}	
	/**
	 * 設定畫面
	 */
	private void setViewComponent() {
		// TODO Auto-generated method stub
		tr_setting_bookcase_background_style = (TableRow) findViewById(R.id.tr_setting_bookcase_background_style);
		tr_setting_auto_sync_new_book = (TableRow) findViewById(R.id.tr_setting_auto_sync_new_book);
		tr_setting_auto_sync_last_read_page = (TableRow) findViewById(R.id.tr_setting_auto_sync_last_read_page);
		//tr_setting_notification_visible = (TableRow) findViewById(R.id.tr_setting_notification_visible);
		tr_setting_backup_read_record = (TableRow) findViewById(R.id.tr_setting_backup_read_record);
		tr_setting_download_read_record = (TableRow) findViewById(R.id.tr_setting_download_read_record);
		tr_setting_del_all_download_book = (TableRow) findViewById(R.id.tr_setting_del_all_download_book);
		tr_setting_re_register = (TableRow) findViewById(R.id.tr_setting_re_register);
		//tr_setting_default = (TableRow) findViewById(R.id.tr_setting_default);
		tr_setting_about = (TableRow) findViewById(R.id.tr_setting_about);
		//tr_setting_intoTWM = (TableRow) findViewById(R.id.tr_setting_intoTWM);
		//tv_setting_intoTWM = (TextView) findViewById(R.id.tv_setting_intoTWM);
		iv_setting_up = (ImageButton) findViewById(R.id.iv_setting_up);
		//tv_setting_title = (TextView) findViewById(R.id.tv_setting_title);
		iv_setting_default = (ImageView) findViewById(R.id.iv_setting_default);
		
		tv_setting_bookcase_background_style = (TextView) findViewById(R.id.tv_setting_bookcase_background_style);
		tv_setting_bookcase_background_style_value = (TextView) findViewById(R.id.tv_setting_bookcase_background_style_value);
		tv_setting_auto_sync_new_book = (TextView) findViewById(R.id.tv_setting_auto_sync_new_book);
		tv_setting_auto_sync_last_read_page = (TextView) findViewById(R.id.tv_setting_auto_sync_last_read_page);
		//tv_setting_notification_visible = (TextView) findViewById(R.id.tv_setting_notification_visible);
		tv_setting_backup_read_record = (TextView) findViewById(R.id.tv_setting_backup_read_record);
		but_setting_backup_read_record_value = (Button) findViewById(R.id.but_setting_backup_read_record_value);
		tv_setting_download_read_record = (TextView) findViewById(R.id.tv_setting_download_read_record);
		but_setting_download_read_record_value = (Button) findViewById(R.id.but_setting_download_read_record_value);	
		tv_setting_del_all_download_book = (TextView) findViewById(R.id.tv_setting_del_all_download_book);
		but_setting_del_all_download_book_value = (Button) findViewById(R.id.but_setting_del_all_download_book_value);
		tv_setting_re_register = (TextView) findViewById(R.id.tv_setting_re_register);
		but_setting_re_register_value = (Button) findViewById(R.id.but_setting_re_register_value);
		tv_setting_about = (TextView) findViewById(R.id.tv_setting_about);

		cb_setting_auto_sync_new_book_value = (CheckBox) findViewById(R.id.cb_setting_auto_sync_new_book_value);
		cb_setting_auto_sync_last_read_page_value = (CheckBox) findViewById(R.id.cb_setting_auto_sync_last_read_page_value);
		//cb_setting_notification_visible_value = (CheckBox) findViewById(R.id.cb_setting_notification_visible_value);
		
		lv_setting_bookcase_background_style_value = (ListView) findViewById(R.id.lv_setting_bookcase_background_style_value);
		
		settingItem = getResources().getStringArray(R.array.iii_settingItem);		
		
		tv_setting_bookcase_background_style.setText(settingItem[0]);
		tv_setting_auto_sync_new_book.setText(settingItem[1]);
		tv_setting_auto_sync_last_read_page.setText(settingItem[2]);
		//tv_setting_notification_visible.setText(settingItem[3]);
		tv_setting_backup_read_record.setText(settingItem[3]);
		tv_setting_download_read_record.setText(settingItem[4]);
		tv_setting_del_all_download_book.setText(settingItem[5]);
		tv_setting_re_register.setText(settingItem[6]);
		tv_setting_about.setText(settingItem[7]);
		//tv_setting_intoTWM.setText(settingItem[7]);
		tv_setting_about.setBackgroundColor(Color.TRANSPARENT);
		//tv_setting_intoTWM.setBackgroundColor(Color.TRANSPARENT);
		lv_setting_bookcase_background_style_value.bringToFront();
		lv_setting_bookcase_background_style_value.setAdapter(
				new ArrayAdapter<Object> (this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.iii_bookcase_background_style_value)));
		lv_setting_bookcase_background_style_value.setBackgroundColor(Color.GRAY);
		
		but_setting_backup_read_record_value.setText(R.string.iii_setting_backup_read_record_value);		
		but_setting_download_read_record_value.setText(R.string.iii_setting_download_read_record_value);
		but_setting_del_all_download_book_value.setText(R.string.iii_setting_del_all_download_book_value);
		but_setting_re_register_value.setText(R.string.iii_setting_now_register);
		
		settings = getSharedPreferences("setting_Preference", 0);
	}
	/**
	 * 顯示訊息
	 */
	private void showAlertMessage(String message){
		new AlertDialog.Builder(Setting.this)
	  		.setTitle(R.string.iii_showAM_exception)
	  		.setMessage(message)
	  		.setPositiveButton(R.string.iii_showAM_ok,
	  				new DialogInterface.OnClickListener(){
  						public void onClick(DialogInterface dialoginterface, int i){
      
  						}
        			}
       		)
      .show();    	 
	}
	public String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
}
