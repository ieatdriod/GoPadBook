package org.iii.ideas.reader.last_page;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import tw.com.soyong.AnReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.gsimedia.sa.DeviceIDException;
import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;
import com.taiwanmobile.myBook_PAD.R;
import com.taiwanmobile.myBook_PAD.RealBookcase;


/**
 * 最後閱讀頁輔助class，負責上傳/下載最後閱讀頁
 * @author III
 * 
 */
public class LastPageHelper {
	
	private Context ctx;
	private String ebook_error;
	private String ebook_description;
	public LastPageHelper(Context ctx_){
		ctx=ctx_;
	}
	
	/**
	 * 上傳最後閱讀頁
	 * @param deliverId deliver id
	 * @param title 書名 
	 * @param chapName 章節名稱
	 * @param span 第幾個span
	 * @param idx 第幾個index
	 * @param time 閱讀時間
	 * @return 是否成功
	 * @throws Exception 上傳過程中是否有例外
	 */
	public boolean uploadXml(String deliverId,String title,String chapName,int span,int idx,long time) throws DeviceIDException,Exception{
//		try {
			XmlSerializer s = Xml.newSerializer();
			StringWriter writer = new StringWriter();
			writer.write("xml=");
			
			s.setOutput(writer);
			s.startDocument("UTF-8", true);
			s.startTag("", "ebooks");
			s.startTag("", "ebook");
			s.startTag("", "Delivery-ID");
			s.text(deliverId);
			s.endTag("", "Delivery-ID");
			/*s.startTag("", "title");
			s.text(title);
			s.endTag("", "title");*/
			s.startTag("", "read_at");
			s.text(String.valueOf(time));
			s.endTag("", "read_at");
			s.startTag("", "chapter");
			s.text(chapName);
			s.endTag("", "chapter");
			s.startTag("", "span");
			s.text(String.valueOf(span));
			s.endTag("", "span");
			s.startTag("", "idx");
			s.text(String.valueOf(idx));
			s.endTag("", "idx");
			s.endTag("", "ebook");
			s.endTag("", "ebooks");
			s.endDocument();
			
			HttpURLConnection conn = null;
			String deviceID;
			try{
				deviceID = GSiMediaRegisterProcess.getID(ctx);
			}catch(Throwable e){deviceID = "";};
	    	if (deviceID == null) deviceID = "";
			URL myURL = new URL(ctx.getResources().getString(R.string.iii_last_page_upload_url)+deliverId+"&device_id="+deviceID+"&token="+RealBookcase.getToken());
			//URL myURL = new URL("http://61.64.54.35/testcode/rf.asp"); 
			//String urlParameters ="&token="+RealBookcase.getToken(); 
			conn = (HttpURLConnection) myURL.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",	"application/x-www-form-urlencoded");


//			conn.setRequestProperty("Content-Length",
//					"" + Integer.toString(writer.toString().length()));
//			conn.setRequestProperty("Content-Language", "UTF-8");
			conn.setRequestProperty("Charset", "UTF-8");

			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(writer.toString());
			wr.flush();
			wr.close();

			// Get Response
			int resp = conn.getResponseCode();
			if (resp != HttpURLConnection.HTTP_OK) {
//				threadTestttMsg(getResources().getString(
//						R.string.iii_NetworkNotConnMessage));
				return false;
			}
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = conn.getInputStream();		
			//for test
//			BufferedReader r = new BufferedReader(new InputStreamReader(is));
//			StringBuilder total = new StringBuilder();
//			String testline;
//			while ((testline = r.readLine()) != null) {
//			    total.append(testline);
//			}	
			Document doc = null ;
			doc = db.parse(is);
			NodeList nError = doc.getElementsByTagName("status");
			ebook_error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
			
			NodeList nDesc = doc.getElementsByTagName("description");
			ebook_description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();

			NodeList nData = doc.getElementsByTagName("data");
			String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
			InputStream dataStream = covertStringToStream(RevData);
			Log.e("Token","ebook_error=>"+ebook_error+" "+"ebook_description=>"+ebook_description);
			//doc = db.parse(dataStream);
			if (ebook_error.equals("1")) {

			DataInputStream in = new DataInputStream(dataStream);
//			URL url = new URL(ctx.getResources().getString(R.string.iii_last_page_upload_url)+deliverId+"&device_id="+GSiMediaRegisterProcess.getID(ctx));
//			URLConnection uc=url.openConnection();
//			uc.setRequestProperty("Content-Type", "application/soap+xml;charset=UTF-8");
//			uc.setRequestProperty("Accept","text/html,application/xml,application/xhtml+xml,text/html");
//			uc.setConnectTimeout(10000);
//			uc.setReadTimeout(10000);
//			DataOutputStream out;
//			DataInputStream in;
//			uc.setDoInput (true);
//			uc.setDoOutput (true);
//			uc.setUseCaches (false);
//			//uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//			out = new DataOutputStream(uc.getOutputStream());
//			out.writeUTF(writer.toString());
//			//Log.d("UploadXml","is:"+writer.toString());
//			out.flush(); 
//			out.close(); 
//			in = new DataInputStream (uc.getInputStream());
			StringBuilder result=new StringBuilder();
			String line;
			while (null != ((line = in.readLine()))){
				result.append(line);
				//Log.d("SyncResult",result);
			}
			in.close();
			
			return parseUploadResult(result.toString());
			}else{
//				String str = ebook_error+ebook_description;
//				CharSequence charseq = new String(str);
//				
//	            Toast.makeText(LastPageHelper.this,"",   
//	                    Toast.LENGTH_SHORT).show(); 
				if(deviceID.length() == 0){
					throw new DeviceIDException(ctx.getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG));
			}
			return false;
		}
			//return writer.toString();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
	}
	
	/**
	 * parse最後閱讀頁上傳後server回傳的結果xml
	 * @param result 結果字串
	 * @return 成功與否
	 * @throws XmlPullParserException result parser exception
	 * @throws IOException result xml file not found
	 */
	private boolean parseUploadResult(String result) throws XmlPullParserException, IOException{
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			boolean in_error=false;
			xpp.setInput( new StringReader ( result ) );
			String resultCode="";
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_DOCUMENT) {
					//System.out.println("Start document");
				} else if(eventType == XmlPullParser.END_DOCUMENT) {
					//System.out.println("End document");
				} else if(eventType == XmlPullParser.START_TAG) {
					//System.out.println("Start tag "+xpp.getName());
					if(xpp.getName().equals("error"))
						in_error=true;
				} else if(eventType == XmlPullParser.END_TAG) {
					//System.out.println("End tag "+xpp.getName());
					if(xpp.getName().equals("error"))
						in_error=false;
				} else if(eventType == XmlPullParser.TEXT) {
					//System.out.println("Text "+xpp.getText());
					if(in_error)
						resultCode=xpp.getText();
				}
				eventType = xpp.next();
			}

			if(resultCode!=null && resultCode.length()==1 && resultCode.charAt(0)=='0')
				return true;
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 下載最後閱讀頁xml並parse，依據時間先後判斷是否更新local的資料
	 * @param lastPage 最後閱讀頁物件
	 * @param deliverId deliver id
	 * @return 是否成功
	 * @throws Exception 下載exception
	 */
	public boolean downloadXml(SharedPreferences lastPage,String deliverId) throws DeviceIDException,Exception{
		HttpURLConnection conn = null;
		File tempDir=ctx.getDir("temp", Context.MODE_PRIVATE);
		File outFile = new File(tempDir.toString()+File.separator+"last_page_temp.xml");
		String deviceID;
		try{
			deviceID = GSiMediaRegisterProcess.getID(ctx);
		}catch(Throwable e){deviceID = "";};
    	if (deviceID == null) deviceID = "";
    	
		URL fileUrl = new URL(ctx.getResources().getString(R.string.iii_last_page_download_url)+deliverId+"&device_id="+deviceID);
		
		//URL fileUrl = new URL("http://61.64.54.35/testcode/rf.asp"); 
		String urlParameters = "&token="+RealBookcase.getToken(); 
		conn = (HttpURLConnection) fileUrl.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");


//		conn.setRequestProperty("Content-Length",
//				"" + Integer.toString(urlParameters.getBytes().length));

		conn.setRequestProperty("Content-Language", "UTF-8");

		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);

		// Send request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		// Get Response
		int resp = conn.getResponseCode();
		if (resp != HttpURLConnection.HTTP_OK) {
//			threadTestttMsg(getResources().getString(
//					R.string.iii_NetworkNotConnMessage));
			return false;
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputStream is = conn.getInputStream();	

		
		Document doc = null ;
		doc = db.parse(is);
		NodeList nError = doc.getElementsByTagName("status");
		ebook_error = nError.item(0).getChildNodes().item(0).getNodeValue().toString();
		
		NodeList nDesc = doc.getElementsByTagName("description");
		ebook_description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();
		
		NodeList nData = doc.getElementsByTagName("data");
		String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
		InputStream dataStream = covertStringToStream(RevData);
		//doc = db.parse(dataStream);
//		URL fileUrl = new URL(ctx.getResources().getString(R.string.iii_last_page_download_url)+deliverId+"&device_id="+GSiMediaRegisterProcess.getID(ctx));
//		HttpURLConnection con = (HttpURLConnection)fileUrl.openConnection();
//		con.setConnectTimeout(10000);
//		con.setReadTimeout(10000);
//		
//		//Long a=System.currentTimeMillis();
//		//Log.e("In lastHelper.downloadXml", String.valueOf(System.currentTimeMillis()));
//		
//		
//		InputStream inStream = con.getInputStream();
		Log.e("Token","ebook_error=>"+ebook_error+" "+"ebook_description=>"+ebook_description);
		if (ebook_error.equals("1")) {
		OutputStream outStream = new FileOutputStream(outFile);
		int len;
		byte[] buffer = new byte[1024];

		while((len = dataStream.read(buffer)) >= 0){
			outStream.write(buffer, 0, len);
		}
		
		dataStream.close(); 
		outStream.close();
	
		//a=System.currentTimeMillis();
		//Log.e("In getLastPage", String.valueOf(System.currentTimeMillis()));
		LastPage last = LastPageReader.getLastPage(outFile.getAbsolutePath());
		//Log.d("!!!!!deliver","id:"+deliverId);
		//Log.d("!!!!!!last.chapName","is:"+last.chapName);
		//Log.d("!!!!!!last.span","is:"+last.span);
		//Log.d("!!!!!!last.idx","is:"+last.idx);
		//Log.d("!!!!!!last.read_at","is:"+last.read_at);
		//Log.e("Done getLastPage", String.valueOf(System.currentTimeMillis()));
		//Log.e("getLastPage need ", String.valueOf(System.currentTimeMillis() - a));
		if(last==null){
			Log.e("LastPageHelper:downloadXml","LastPage is null");
			return false;
		}else{
			if(last.span>=0 && last.idx>=0){
				String localLastPageTime=lastPage.getString(deliverId+"_time", "0");
				Long local =new Long(localLastPageTime);
				Long server = new Long(last.read_at);
				if(local.longValue() < server.longValue()){
					lastPage.edit().putString(deliverId+"_chap_name", last.chapName).commit();
		    		lastPage.edit().putInt(deliverId+"_span", last.span).commit();
		    		lastPage.edit().putInt(deliverId+"_idx", last.idx).commit();
		    		lastPage.edit().putString(deliverId+"_time", String.valueOf(last.read_at)).commit();
				}

			}
			return last.isSuccessful;
		}	
		}else{
			if(deviceID.length() == 0){
				throw new DeviceIDException(ctx.getResources().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG));
			}
			return false;
		}
	}
	
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
	 * 刪除某本書最後閱讀頁紀錄(只刪local端)
	 * @param ctx context
	 * @param did deliver id
	 * @return true:無錯誤發生; false: 發生錯誤
	 */
	public static boolean deleteLastPageOfBook(Context ctx, String did){
		try {
			SharedPreferences lastPages = ctx.getSharedPreferences(ctx.getResources().getString(R.string.iii_last_page_name),0);  
			lastPages.edit().remove(did+"_chap_name").commit();
			lastPages.edit().remove(did+"_span").commit();
			lastPages.edit().remove(did+"_idx").commit();
			lastPages.edit().remove(did+"_time").commit();
			return true;
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 刪除所有最後閱讀頁
	 * @param ctx context
	 * @return true:無錯誤發生; false: 發生錯誤
	 */
	public static boolean deleteAllLastPage(Context ctx){
		try {
			SharedPreferences lastPages = ctx.getSharedPreferences(ctx.getResources().getString(R.string.iii_last_page_name),0);  
			lastPages.edit().clear().commit();
			return true;
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}
