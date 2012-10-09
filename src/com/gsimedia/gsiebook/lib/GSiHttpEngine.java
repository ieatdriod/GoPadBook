package com.gsimedia.gsiebook.lib;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.taiwanmobile.myBook_PAD.R;
import com.taiwanmobile.myBook_PAD.RealBookcase;
import com.gsimedia.gsiebook.common.Config;
import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;

public class GSiHttpEngine {

	/**
	 * last page related
	 */
	//同步最後閱讀頁的 url
     	
    public static GSiLastPage downloadLastPage(final Context context, final String aDeliverID, final String aBookToken) {
    	String deviceID = "";
    	try{
    		deviceID = GSiMediaRegisterProcess.getID(context);
		}catch(Throwable e){};
    	if (deviceID == null) deviceID = "";

    	
    	HttpParams httpParameters = new BasicHttpParams();
    	int timeoutConnection = 10000;
    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
    	int timeoutSocket = 10000;
    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);  
    	
    	DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
    	HttpResponse response = null;
    	String apiUrl = context.getResources().getString(R.string.web_api_lastpage_down);
    	GSiLastPage aLastPage = null;
    	String aPage = "-1";
    	String aTime = "0";
    	boolean bResult = false;
    	try {
	    	//HttpGet httGet = new HttpGet(apiUrl+aDeliverID+ "&device_id=" + deviceID);
    		HttpPost httPost = new HttpPost(apiUrl+aDeliverID+ "&device_id=" + deviceID+"&token="+ aBookToken);
    	
    		response = httpclient.execute(httPost);
    		//parse xml
    		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    		Document doc = db.parse(response.getEntity().getContent());
    		
    		//add
    		NodeList nStatus = doc.getElementsByTagName("status");
			String ebook_error = nStatus.item(0).getChildNodes().item(0).getNodeValue().toString();

			NodeList nDesc = doc.getElementsByTagName("description");
			String ebook_description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();
			
			NodeList nData = doc.getElementsByTagName("data");
			String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
			InputStream dataStream = covertStringToStream(RevData);
			doc = db.parse(dataStream);
			
    		doc.getDocumentElement().normalize();  
			//if (ebook_error.equals("1")) {
    		String aValueOfebooks = getValueOfElement(doc,"ebooks");
    		if(aValueOfebooks!=null){
    			if(aValueOfebooks.equals("null"))
    				aLastPage = new GSiLastPage(Integer.parseInt(aPage),Long.parseLong(aTime));
    			else{
    				Log.e(Config.LOGTAG,"parse lastpage xml error!");
    				aLastPage = new GSiLastPage(Integer.parseInt(aPage),Long.parseLong(aTime));
    				aLastPage.setDeviceIDEmpty(deviceID.length()==0);
    			}
    		}else{
    			aPage = getValueOfElement(doc,"read_at");
    			aTime = getValueOfElement(doc,"updated_at");
    			Log.d(Config.LOGTAG,"get last page = "+aPage +", timestamp="+aTime);
    			int page = -1;
    			try{
    				page = Integer.parseInt(aPage);
    			}catch(NumberFormatException e){
    				page = -1;
    				Log.e(Config.LOGTAG,"got page parse to int exception! "+aPage);
    			}
    			aLastPage = new GSiLastPage(page,Long.parseLong(aTime));
    			bResult = true;
    		}
			//}
    		
    	}catch(Throwable e){
    		e.printStackTrace();
    		aLastPage = new GSiLastPage(Integer.parseInt(aPage),Long.parseLong(aTime));
    	}
    	Log.i(Config.LOGTAG,"download last page="+aLastPage.toString());
//    	if(bResult)
//    		Toast.makeText(context, context.getString(R.string.GSI_LASTPAGE_DOWNLOAD_MSG), Toast.LENGTH_SHORT).show();
    	return aLastPage;
    }
	public static InputStream covertStringToStream(String s){
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
    static String getValueOfElement(Document aDoc,String aName){
    	NodeList aChilds = aDoc.getElementsByTagName(aName);
    	
    	String result =null;
    	if(aChilds.getLength()>0){
    		result = aChilds.item(0).getChildNodes().item(0).getNodeValue();
    		Log.d(Config.LOGTAG,aName+" = "+result);
    	}else{
    		result = "not exist";
    	}
    	return result;
    }
    
    static final String convertStreamToString(InputStream is) {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8*1024);
    	StringBuilder sb = new StringBuilder();
    	
    	String line = null;
    	try {
    		while ((line = reader.readLine()) != null) {
    			sb.append(line);
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
 
        return sb.toString();
    }

    public static void uploadLastPage(final Context context,final GSiLastPage aPage, final String aDeliverID, final String aBookToken){
    	
    	UploadTask aTask = new UploadTask();
    	WeakReference<Context> aContext = new WeakReference<Context>(context);
    	aTask.execute(new UploadContext(aContext.get(),aPage,aDeliverID, aBookToken));

    }
    static class UploadContext{
    	WeakReference<Context> iContext;
    	GSiLastPage iPage;
    	String iDeliverID;
    	String iBookToken;
    	UploadContext(Context aContext,GSiLastPage aPage, String aID, String sBookToken){
    		iContext = new WeakReference<Context>(aContext);
    		iPage = aPage;
    		iDeliverID = aID;
    		iBookToken = sBookToken;
    	}
    	Context getContext(){
    		return iContext.get();
    	}
    	GSiLastPage getLastPage(){
    		return iPage;
    	}
    	String getDeliverID(){
    		return iDeliverID;
    	}
    	String getBookToken(){
    		return iBookToken;
    	}
    }
    static class UploadTask extends AsyncTask<UploadContext, Void, Boolean> {

    	private WeakReference<Context> iContext = null;
    	String deviceID ="";
        @Override
        protected Boolean doInBackground(UploadContext... params) {
        	Boolean aResult = Boolean.FALSE;
        	GSiLastPage aPage = params[0].getLastPage();
        	String aDeliverID = params[0].getDeliverID();
        	String aBookToken = params[0].getBookToken();
        	iContext = new WeakReference<Context>(params[0].getContext());
        	final Context context = iContext.get();
        	
        	deviceID ="";
        	try{
        		deviceID = GSiMediaRegisterProcess.getID(context);
    		}catch(Throwable e){};
        	if (deviceID == null) deviceID = "";
        	
			String apiUrl = context.getResources().getString(R.string.web_api_lastpage_up);
			long aTime = aPage.getTime();
			try {
				HttpPost httppost = new HttpPost(apiUrl+aDeliverID+ "&device_id=" + deviceID+"&token="+aBookToken);


				String upload = "xml=<?xml version='1.0' encoding='UTF-8'?>"
				+ "<ebooks><ebook>"
				+ "<Delivery-ID>%s</Delivery-ID><title>%s</title>"
				+ "<read_at>%s</read_at>"
				+ "<updated_at>%s</updated_at>"
				+ "</ebook></ebooks>";
				
				String sendXml = String.format(upload,
						aDeliverID,
						"title",
						Integer.toString(aPage.getPage()),
						Long.toString(aTime));


				StringEntity se = new StringEntity(sendXml, HTTP.UTF_8);
				se.setContentType("text/xml");

//				httppost.setHeader("Accept",
//								"text/html,application/xml,application/xhtml+xml,text/html");
//				httppost.setHeader("Content-Type",
//						"application/soap+xml;charset=UTF-8");
				httppost.setHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
				
				httppost.setEntity(se);

				HttpParams httpParameters = new BasicHttpParams();
				int timeoutConnection = 10000;
				HttpConnectionParams.setConnectionTimeout(httpParameters,
						timeoutConnection);
				int timeoutSocket = 10000;
				HttpConnectionParams.setSoTimeout(httpParameters,
						timeoutSocket);

				DefaultHttpClient httpclient = new DefaultHttpClient(
						httpParameters);
				HttpResponse response;

				response = httpclient.execute(httppost);
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = db.parse(response.getEntity().getContent());
	    		
				//add
	    		NodeList nStatus = doc.getElementsByTagName("status");
				String ebook_error = nStatus.item(0).getChildNodes().item(0).getNodeValue().toString();

				NodeList nDesc = doc.getElementsByTagName("description");
				String ebook_description = nDesc.item(0).getChildNodes().item(0).getNodeValue().toString();
				
				NodeList nData = doc.getElementsByTagName("data");
				String RevData = nData.item(0).getChildNodes().item(0).getNodeValue().toString();
				InputStream dataStream = covertStringToStream(RevData);
				doc = db.parse(dataStream);
				
				
	    		doc.getDocumentElement().normalize();  
				//if (ebook_error.equals("1")) {
	    		String aValue = getValueOfElement(doc,"error");
	    		if(aValue!=null && aValue.equals("0")){
	    			Log.d(Config.LOGTAG,"set last page success!");
	    			aResult = Boolean.TRUE;
	    		}else{//with error
	    			Log.e(Config.LOGTAG,"set last page error: "+aValue);
	    			aResult = Boolean.FALSE;
	    		}
				//}


			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Throwable e){
				e.printStackTrace();
			}
            return aResult;

        }

        protected void onPostExecute(Boolean aResult) {
        	if(Boolean.TRUE == aResult){
        		Toast.makeText(iContext.get().getApplicationContext(), iContext.get().getString(R.string.GSI_LASTPAGE_UPLOAD_MSG), Toast.LENGTH_SHORT).show();
        	}else{
        		if(deviceID.length() == 0){
        			Toast.makeText(iContext.get().getApplicationContext(), iContext.get().getString(R.string.GSI_DEVICE_ID_EMPTY_MSG), Toast.LENGTH_SHORT).show();
        		}
        	}
        }
    }
    
}
