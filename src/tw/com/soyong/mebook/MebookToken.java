package tw.com.soyong.mebook;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class MebookToken {
	
	private static boolean DEBUG = false ;
	private final static String TAG = "MebookToken";
	
	public static final int TOK_UNKONW  = -1 ;
	public static final int TOK_TIME	= 0 ;
	public static final int TOK_ORG		= 1 ;
	public static final int TOK_TRL		= 2 ;
	public static final int TOK_CHP		= 3 ;
	public static final int TOK_MRK		= 4 ;
	public static final int TOK_HLT		= 5 ;
	public static final int TOK_PIC		= 6 ;
	public static final int TOK_VOC		= 7 ;
	public static final int TOK_SYN		= 8 ;
	public static final int TOK_ANT		= 9 ;
	public static final int TOK_EXT		= 10 ;
	public static final int TOK_VO2		= 11 ;
	public static final int TOK_POST	= 12 ;
	public static final int TOK_EOS		= 13 ;
	public static final int TOK_TIMEVS	= 14 ;	//"TIMEVS"
	

	final String [] mToken={
		"TIME", "ORG", "TRL" , "CHP" ,"MRK",
		"HLT", "PIC", "VOC", "SYN", "ANT",
		"EXT", "VO2", "POST", "EOS"
	};
	
	int mID;
	String mData;
	final Matcher mMatcher;
	final String mTxt;
	public MebookToken(final String txt){
		mTxt = txt ;
		mMatcher = tokenizer(txt);
//		dump(mMatcher, txt);
		
		mID = -1 ;
		mData = null;
	}
	
	// for debug
	public void dump(Matcher matcher , String txt) {
		
		matcher.reset();
		// we only use group 0
		boolean matchFound = matcher.find();
		while (matchFound){
			if ( DEBUG ) Log.d(TAG, matcher.start() + "-" + matcher.end()+ matcher.group());
			matchFound = matcher.find(matcher.end());			
		}
	}	
	
	
	final static Matcher tokenizer(String txt)  {
		
		// Mebook token
		// support Vo2
		final String patternStr = "<\\\\?[A-Z][A-Z][A-Z0-9][^<>]*>";	// for Regular Expression rule "\\\\" for Java "\"
		// not support Vo2
		//final String patternStr = "<\\\\?[A-Z][A-Z][A-Z][^<>]*>";	// for Regular Expression rule "\\\\" for Java "\"
        final Pattern pattern = Pattern.compile(patternStr);
        final Matcher matcher = pattern.matcher(txt);
        
		return matcher;
	}	
	
	public boolean next() throws MebookException {
		
		boolean ret = false ;
		Matcher matcher = mMatcher;
		if ( !matcher.find() ){
			return ret ;
		}
		
		int count = mToken.length;
		String matchStr = matcher.group();
		int id = TOK_UNKONW ;
		for( int i =0; i < count ; ++ i){
			if ( matchStr.regionMatches(1, mToken[i],0, 3) ){
				id = i ;		// find match (begin) token
				
				// Workaround for "TIMEVS" tag use for Studio classroom
				if ( id == 0){
					if ( matchStr.regionMatches(1, "TIMEVS",0, 6) ){
						id = TOK_TIMEVS;
					}
				}
				break ;
			}
		}
		
		ret = true ;	// got token patten
		
		// find begin token
		if ( TOK_UNKONW != id && TOK_TIMEVS !=id ){
			
			switch( id ){
			// single token
			case TOK_TIME:
			case TOK_CHP:
			case TOK_MRK:
			case TOK_PIC:
			case TOK_EOS:{
				mData = mTxt.substring(matcher.start()+1 , matcher.end()-1);
//				if ( DEBUG ) Log.d(TAG,mData);
			}break;
				
			// pair token
			default:{
				int beginIndex = matcher.end();
				int endIndex = -1 ;
				
				while (matcher.find(matcher.end())){
					matchStr = matcher.group();
					if ( mToken[id].regionMatches(0, matchStr, 2, 3) ){
						endIndex = matcher.start() ;
						break ;
					}
				}
				
				if ( endIndex > -1 ){
					mData = mTxt.substring(beginIndex , endIndex);
//					if ( DEBUG ) Log.d(TAG,mData);
				}else {
					throw new MebookException();
				}
			}break ;
			}
			
			mID = id ;
//			ret = true ;
		} else {	//TOK_UNKONW
			mID = id ;
			mData = matchStr;
		}
		return ret;
	}

	public final String getData() {
		return mData;
	}

	public final int getID() {
		return mID;
	}
}
