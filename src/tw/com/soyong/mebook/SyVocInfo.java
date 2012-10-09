package tw.com.soyong.mebook;


public class SyVocInfo {
	
	public SyTime<Integer> mBeginTime;
	public SyTime<Integer> mMiddleTime;
	public SyTime<Integer> mEndTime;
    
    public String [] mHlt;  //Highlight
    public String mVoc;  //Vocabulary
    public String mPhonetic;   //phonetic alphabet
    public String mOthers;
    
    public SyVocInfo(){
    }
    
    public boolean isValidTime(){
    	
    	if ( null != mBeginTime && mBeginTime.getValue() >= 0 &&
    		 null != mEndTime && mEndTime.getValue() > mBeginTime.getValue() ){
    		return true ;
    	}
    	return false ;
    }
	
}
