package com.taiwanmobile.myBook_PAD;

import java.lang.ref.WeakReference;

import com.gsimedia.gsiebook.common.Config;
import com.gsimedia.sa.GSiMediaInputStreamProvider;
import com.gsimedia.sa.GSiMediaRegisterProcess.GSiMediaRegisterProcess;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.http.SslError;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class LoginDialogController implements OnCancelListener {
	
	//stage server
	private static final String LOGIN_SERVER = "https://ssostaging.catch.net.tw/auth/member_login_m.jsp?return_url=";
	private static final String RETURN_URL = "http://124.29.140.83/DeliverWeb/Logon?deviceId=%1$s&from_ch=ert01";
	private static final String JAVASCRIPT_API = "accessor";
	
	private Dialog iLoginDialog = null;
	private LoginDialogObserver iObsRef = null;
	private Object iUserData;
	
	private String deviceID = "";
	public void ShowLoginDialog(Activity aActivity,LoginDialogObserver aObs, Object aUserData){
		if(iLoginDialog == null && aActivity !=null){
			iObsRef = aObs;
			deviceID = "";
			try{
				deviceID = GSiMediaRegisterProcess.getID(aActivity.getApplicationContext());
			}catch(Throwable e){};
	    	if (deviceID == null) deviceID = "";
	    	
	    	if(deviceID.length() == 0){
	    		Toast.makeText(aActivity.getApplicationContext(), aActivity.getString(R.string.GSI_DEVICE_ID_EMPTY_MSG), Toast.LENGTH_LONG).show();
	    		return;
	    	}
			iUserData = aUserData;
			iLoginDialog = new Dialog(aActivity);
			iLoginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			iLoginDialog.setContentView(R.layout.twm_login);
			final TextView iMsgView = (TextView)iLoginDialog.findViewById(R.id.msg);
			
			final WebView input = (WebView)iLoginDialog.findViewById(R.id.view);
			input.setWebViewClient(new WebViewClient(){
				@Override
				public void onReceivedSslError(WebView view,
						SslErrorHandler handler, SslError error) {
					// TODO Auto-generated method stub
					input.setVisibility(View.GONE);
					iMsgView.setVisibility(View.VISIBLE);
					handler.proceed();
				}
	        });	
			WebSettings wvSettings= input.getSettings();
			wvSettings.setJavaScriptEnabled(true); 
			wvSettings.setBuiltInZoomControls(true);
			
			input.addJavascriptInterface(new JavaScriptInterface(this,aObs), JAVASCRIPT_API);
			String ret = String.format(RETURN_URL, ""+deviceID);
			input.loadUrl(LOGIN_SERVER+ret);
			iLoginDialog.setOnCancelListener(this);
			iLoginDialog.show();
		}
	}	
	@Override
	public void onCancel(DialogInterface dialog) {
		DismissLoginDialog();
		if(iObsRef!=null){
			iObsRef.LoginComplete(this,this.iUserData, LoginDialogObserver.KErrCancel);
		}
	}
	
	public void DismissLoginDialog(){
		iLoginDialog.dismiss();
		iLoginDialog = null;
	}
	
	public boolean getDeviceIDEmpty(){
		return (deviceID.length() == 0);
	}
	
	private static class JavaScriptInterface {
		WeakReference<LoginDialogObserver> iObsRef = null;
		WeakReference<LoginDialogController> iControllerRef = null;
		
		JavaScriptInterface(LoginDialogController aController,LoginDialogObserver aObs){
			 iObsRef = new WeakReference<LoginDialogObserver>(aObs);
			 iControllerRef = new WeakReference<LoginDialogController>(aController);
		}
		
	    public void setValue(String result){
	    	Log.d("TWM","result:"+result);
	    	if(iObsRef!=null && iControllerRef!=null){
	    		final LoginDialogObserver aObs = iObsRef.get();
	    		final LoginDialogController aController = iControllerRef.get();
	    		if (result.compareTo("success") == 0)
	    		    aObs.LoginComplete(aController, aController.iUserData , LoginDialogObserver.KErrNone);
	    		else 
		    		aObs.LoginComplete(aController, aController.iUserData , LoginDialogObserver.KErrFailed);
	    	}
	    }
	}


}
