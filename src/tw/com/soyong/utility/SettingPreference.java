package tw.com.soyong.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SettingPreference implements Cloneable {
	
	static final String TAG = "SettingPreference";
	
	static final String REP_COUNT = "rep_count";
	static final String GAP = "gap";
	static final String AUTO_REPEAT = "auto_repeat";
	static final String FONT_TYPE = "Font_Type";
	static final String HIDE_PERCENT = "Hide_Percent";
	
	SharedPreferences mPref ;
	
	int mRepeatCount;
	int mGap;
	boolean mAutoRepeat;
	int mFontType;
	int mHidePercent;
	
	public SettingPreference( Context context){
		//mContext = context ;
		mPref = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
	}
	
	public void load(){
		SharedPreferences pref = mPref ;
		mRepeatCount = pref.getInt(REP_COUNT, 0);
		mGap = pref.getInt(GAP, 0);
		mAutoRepeat = pref.getBoolean(AUTO_REPEAT, true);
		mFontType = pref.getInt(FONT_TYPE, -1);
		mHidePercent = pref.getInt(HIDE_PERCENT, 0);
	}
	
	public void save(){
		Editor edit = mPref.edit();
		edit.putInt(REP_COUNT, mRepeatCount);
		edit.putInt(GAP, mGap);
		edit.putBoolean(AUTO_REPEAT, mAutoRepeat);
		edit.putInt(FONT_TYPE, mFontType);
		edit.putInt(HIDE_PERCENT, mHidePercent);
		edit.commit();
	}
	
	public int getHidePercent() {
		return mHidePercent;
	}

	public void setHidePercent(int hidePercent) {
		mHidePercent = hidePercent;
	}

	public int getRepeatCount() {
		return mRepeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		mRepeatCount = repeatCount;
	}

	public int getGap() {
		return mGap;
	}

	public void setGap(int gap) {
		mGap = gap;
	}

	public boolean isAutoRepeat() {
		return mAutoRepeat;
	}

	public void setAutoRepeat(boolean autoRepeat) {
		mAutoRepeat = autoRepeat;
	}

	public int getFontType() {
		return mFontType;
	}

	public void setFontType(int fontType) {
		mFontType = fontType;
	}
	
	public float getFontSize(Context ctx){
		return Util.getFontSizeFromPref( mFontType, ctx);
	}
	
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
		

}
