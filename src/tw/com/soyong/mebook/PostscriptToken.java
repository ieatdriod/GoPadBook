package tw.com.soyong.mebook;

import java.util.regex.Matcher;

public class PostscriptToken extends MebookToken {
	
	public static final int TOK_PAR		= 100 ;
	
	
	final String [] mToken={
		"PAR",
	};
	
	private int mPar ;
	private int mBegin;
	
	public PostscriptToken(String txt) {
		super(txt);
	}

	@Override
	public boolean next() throws MebookException {
		
		boolean ret = false ;
		Matcher matcher = mMatcher;
		if ( !matcher.find() )
			return ret ;
		
		int count = mToken.length;
		String matchStr = matcher.group();
		int id = -1 ;
		for( int i =0; i < count ; ++ i){
			if ( matchStr.regionMatches(1, mToken[i],0, 3) ){
				id = i+TOK_PAR ;		// find match (begin) token
				break ;
			}
		}
		
		// find begin token
		if ( -1 != id ){
			
			switch( id ){
			// single token
			case TOK_PAR:
				String strPar = mTxt.substring(matcher.start()+5 , matcher.end()-1);
				mPar = Integer.valueOf(strPar);
				mBegin = matcher.end();
				break;
			}
			
			mID = id ;
			ret = true ;
		}
		return ret;	

	}
	
	public final int getPar() {
		return mPar;
	}
	
	public final int getDataBegin(){
		return mBegin ;				// previous matcher.end();
	}
	
	public final int getDataEnd(){
		return mMatcher.start();	// current matcher.start();
	}

}
