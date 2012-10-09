/**
 * 
 */
package com.gsimedia.sa.Internet;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.gsimedia.sa.GSiMediaRegisterProcess.TimeOutException;
import com.gsimedia.sa.GSiMediaRegisterProcess.XmlException;

/**
 * @author WilliamHsieh
 *
 * 20110506 http post
 */
public class InternetXMLDocument {
	private static String sURL = "";
    private DocumentBuilderFactory docbf = null;
    private DocumentBuilder docb = null;  
	private Document doc = null;
	private static String HttpEncoding = HTTP.UTF_8;
	/**
	 * @param URL
	 */
	public InternetXMLDocument(String URL) throws TimeOutException, XmlException {
		this(URL, null);
    }
	
	public InternetXMLDocument(String URL, String[] SendData) throws TimeOutException, XmlException {
		sURL = URL;
        //read xml here
		HttpURLConnection httpURLConn = null; 
        InputStream is = null;  
        //
        try {
            //Create Http Post Link
            HttpPost httpRequest = new HttpPost(sURL);
            List <NameValuePair> params = new ArrayList <NameValuePair>();
            
            int SendDataIndex = SendData.length;
            int num = 0;
            while(SendDataIndex > num) {
            	if((SendDataIndex-num) >= 2) {
            		//DeviceID/platform/token or add Num
            		params.add(new BasicNameValuePair(SendData[num], SendData[num+1]));
            		num+=2;
            	}
            }
            
            //send out Http Request
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            
            //set Timeout
            HttpParams httpParameters = new BasicHttpParams(); 
            // Set the timeout in milliseconds until a connection is established. 
            int timeoutConnection = InternetConstantManager.NETWORK_CONNECTTIMEOUT; 
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection); 
            // Set the default socket timeout (SO_TIMEOUT)  
            // in milliseconds which is the timeout for waiting for data. 
            int timeoutSocket = InternetConstantManager.NETWORK_SOCKETTIMEOUT; 
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket); 
            
            //get Http Response
            HttpResponse httpResponse = new DefaultHttpClient(httpParameters).execute(httpRequest);
            //Check Status code
            int responseCode = httpResponse.getStatusLine().getStatusCode();

            HttpEntity entity = httpResponse.getEntity(); 
            if (entity != null) {
            	is = entity.getContent();
            }
        	
        }catch (java.net.SocketTimeoutException e) {
        	throw new TimeOutException(e.getMessage());
        }catch (IOException e) {
        	throw new XmlException(e);
        }catch (java.lang.NullPointerException e) {
        	throw new XmlException("No XML Document");
        }catch (Exception e) {
        	throw new XmlException(e); 
        } 
        
        try {
	    	docbf = DocumentBuilderFactory.newInstance();
	    	docb = docbf.newDocumentBuilder(); 
	    	InputSource source = new InputSource(is);
	    	source.setEncoding(HttpEncoding);  
	    	doc = docb.parse(source);    
	        doc.getDocumentElement().normalize();
        }catch (java.net.SocketTimeoutException e) {
        	throw new TimeOutException(e.getMessage());
        }catch (IOException e) {
        	throw new XmlException(e);
        }catch (java.lang.NullPointerException e) {
        	throw new XmlException("No XML Document");
        }catch (Exception e) {
        	throw new XmlException(e); 
        }finally { 
			try { 
				if(is != null)
					is.close(); 
			} catch (java.io.IOException e) { 
			    e.printStackTrace(); 
			} 
        } 
	}
	
	private void setHttpEncoding(String encoding) {
		HttpEncoding = encoding;
	}
	
	public Document getDocument() {
		return doc;
	}
	
	public  String convertXMLToString() throws XmlException, TimeOutException { 
		HttpURLConnection httpURLConn = null; 
        InputStream is = null;  
        StringBuilder sb = new StringBuilder(); 
        try {
            URL url = new URL(sURL); 
            httpURLConn = (HttpURLConnection) url.openConnection(); 
            //set Connect Timeout
        	httpURLConn.setConnectTimeout(InternetConstantManager.NETWORK_CONNECTTIMEOUT);
        	httpURLConn.setReadTimeout(InternetConstantManager.NETWORK_CONNECTTIMEOUT);
        	is = httpURLConn.getInputStream();
        	
    		java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is)); 
            String line = null; 
        	while ((line = reader.readLine()) != null) { 
        		sb.append(line + "\n"); 
        	}
        }catch (java.net.SocketTimeoutException e) {
        	throw new TimeOutException(e);
        }catch (Exception e) {
        	throw new XmlException(e); 
        }finally { 
			try { 
				is.close(); 
			} catch (java.io.IOException e) { 
			    e.printStackTrace(); 
			} 
        } 
        return sb.toString();
	}
}