package tw.com.soyong.utility;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.taiwanmobile.myBook_PAD.R;

import tw.com.soyong.AnReader;
import android.content.Context;
import android.content.SharedPreferences;

public final class Preference {
	
	Context mContext;

	static final String PRO_FILE_PATH = "PRO_FILE_PATH";
	static final String WORKING_PATH = "WORKING_PATH";
	static final String ACTIV_STATE = "activ_state";
	static final String ACTIV_CODE = "activ_code";
	SharedPreferences mPref ;
	
	public Preference(Context context , final String prefFileName ){
		mContext = context;
		mPref = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
	}
	
	public final void setProfilePath( final String path){
		SharedPreferences pref = mPref;
		pref.edit()
		    .putString(PRO_FILE_PATH, path)
		    .commit();
	}
	
	public final String getProfilePath(){
		SharedPreferences pref = mPref;
		return  pref.getString(PRO_FILE_PATH, "");
	}
	
	public final void setWorkingPath( final String path){
		SharedPreferences pref = mPref;
		pref.edit()
		    .putString(WORKING_PATH, path)
		    .commit();
	}
	
	public final String getWorkingPath(){
		SharedPreferences pref = mPref;
		String defPath = AnReader.getExternalStoreageName();
		return  pref.getString(WORKING_PATH, defPath);
	}
	
	public final String getActivityState(){
		SharedPreferences pref = mPref;
		String def = mContext.getResources().getString(R.string.active_state_off);
		return  pref.getString(ACTIV_STATE, def);
	}
	
	public final void setActivityState(){
		String def = mContext.getResources().getString(R.string.active_state_on);
		SharedPreferences pref = mPref;
		pref.edit()
	    .putString(ACTIV_STATE,def)
	    .commit();
	}	
	
	public final void setActivityCode(byte [] buf){
		 try {
			FileOutputStream fos = mContext.openFileOutput("AnReader", Context.MODE_PRIVATE);
			fos.write(buf.length);
			fos.write(buf);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public final byte [] getActivityCode(){
//		SharedPreferences pref = mPref;
//		return  pref.getString(ACTIV_CODE, "0");
		try {
			FileInputStream fis = mContext.openFileInput("AnReader");
			int len = fis.read();
			byte [] buf = new byte[len];
			fis.read(buf);
			return buf ;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	
}
