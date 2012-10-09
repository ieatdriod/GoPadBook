package com.gsimedia.sa.Internet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * @author WilliamHsieh
 *
 */
public class Internet {
	private static String IMEIStatusHttp = "http://www.artfulbits.com/android/antipiracycheck.ashx?IMEI=";
	
    /**
     * Determines status of device's IMEI.
     * 
     * @return -1 - imei status retrieval failed. 0 - Green status 1 to 3 - Yellow
     * status 3 to 5 - Brown status above 5 - Red status
     */
    public static int getIMEIStatus(Context context, String sDeviceID) throws Exception {
    	// 2. Fetch for IMEI data.
    	// Will look like
    	// http://www.artfulbits.com/android/antipiracycheck.ashx?IMEI=123456789123456
    	String url = IMEIStatusHttp + sDeviceID;
    	// Server will return 200 if request post was successful.
    	int http_ok = 200;
    	// Create new http client.
    	HttpClient client = new DefaultHttpClient();
    	// Create new http post.
    	HttpPost post = new HttpPost(url);
    	// Cache http response.
    	HttpResponse response = null;
    	// Will return -1 unless server provides its own value.
    	int imeiStatus = -1;

		if(haveInternet(context) < 0)
			return -1;
      	 
		// Executind post.
		response = client.execute(post);
		// Making sure we've received correct status code.
		if(response.getStatusLine().getStatusCode() == http_ok) {
			// Retrieving content stream.
			InputStream stream = response.getEntity().getContent();
			// Decorating stream with Input stream reader
			InputStreamReader isr = new InputStreamReader(stream);
			// Decorating input stream reader with buffered stream reader.
			BufferedReader reader = new BufferedReader(isr);
			// Reading imei status from stream.
			imeiStatus = Integer.parseInt(reader.readLine());
			// Closing buffered reader will recursively close decorated input stream
			// reader and input stream.
			reader.close();
		}
    		
    	return imeiStatus;
    }
    
    public static int haveInternet(Context context) throws Exception {
	    ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo info = connManager.getActiveNetworkInfo();
	    if (info == null /*||
	    		!connManager.getBackgroundDataSetting()*/) {
	    	return InternetConstantManager.NETWORK_INACTIVE;
	    }else {
	    	int netType = info.getType();
		    //Only upadte if WiFi or 3G is connected
			if (netType == ConnectivityManager.TYPE_WIFI) {
				if(info.isConnected()) {
					return InternetConstantManager.NETWORK_TYPE_WIFI_CONNECTED;
				}else {
					return InternetConstantManager.NETWORK_TYPE_WIFI_DISCONNECTED;
				}
			}else if (netType == ConnectivityManager.TYPE_MOBILE) {
				String SIMType = getSIMType(context);
				if (SIMType.equals("USIM")) {
					if(info.isConnected()) {
						return InternetConstantManager.NETWORK_TYPE_WCDMAUSIMCARD_CONNECTED;
					}else {
						return InternetConstantManager.NETWORK_TYPE_WCDMAUSIMCARD_DISCONNECTED;
					}
				}else if (SIMType.equals("SIM")) {
					if(info.isConnected()) {
						return InternetConstantManager.NETWORK_TYPE_SIMCARD_CONNECTED;
					}else {
						return InternetConstantManager.NETWORK_TYPE_SIMCARD_DISCONNECTED;
					}
				}else if (SIMType.equals("UIM")) {
					if(info.isConnected()) {
						return InternetConstantManager.NETWORK_TYPE_CDMAUIMCARD_CONNECTED;
					}else {
						return InternetConstantManager.NETWORK_TYPE_CDMAUIMCARD_DISCONNECTED;
					}
				}else {
					if(info.isConnected()) {
						return InternetConstantManager.NETWORK_TYPE_UNKOWN_CONNECTED;
					}else {
						return InternetConstantManager.NETWORK_TYPE_UNKOWN_DISCONNECTED;
					}
				}
			}else {
				if(info.isConnected()) {
					return InternetConstantManager.NETWORK_TYPE_UNKOWN_CONNECTED;
				}else {
					return InternetConstantManager.NETWORK_TYPE_UNKOWN_DISCONNECTED;
				}
			}
	    }
    }
    
    private static String getSIMType(Context context) throws Exception {
    	// TODO Auto-generated method stub
    	String simType = "unknown"; //SIMType

    	//get device SIMType
	    TelephonyManager teleManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    	final int type = teleManager.getNetworkType();
    	if (type == TelephonyManager.NETWORK_TYPE_UMTS) {
    		simType = "USIM";//WCDMA USIM Card
    	} else if (type == TelephonyManager.NETWORK_TYPE_GPRS
                || type == TelephonyManager.NETWORK_TYPE_EDGE) {
    		simType = "SIM";//SIM Card
    	} else {
    		simType = "UIM";//CDMA UIM Card
    	}
    	return simType;
    }

}
