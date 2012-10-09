package tw.com.soyong;

import android.media.AudioTrack;
import android.util.Log;

public class PlayerThread extends Thread{
	
	private static final boolean DEBUG = false ;
	private static final String TAG = "PlayerThread";

    ////////////////////////////////////////////////////////
    // Two-Phase Termination
    //	
	private volatile boolean shutdownRequested = false ;
	public void shutdownRequest() {
		shutdownRequested = true ;
		interrupt();
	}
	
	public boolean  isShutdownRequested() {
		return shutdownRequested;
	}
	
	private volatile boolean mThreadPause = true ;			// set default player thread pause!!!
	public synchronized void setThreadPause( boolean isPause) {
		
		mThreadPause = isPause ;
		if ( !isPause ){
			if (DEBUG) Log.e(TAG, "--- setThreadPause:false");
			notifyAll();
			
			mAt.play();
		}else {
			mAt.pause();
		}
	}
	
	public synchronized void waitReady() throws InterruptedException{
		while( true == mThreadPause || getFrameCount() > getEndFrame()){
			if (DEBUG) Log.e(TAG,"+++ waitReady");
			wait();
		}
	}	
	
	
	private RingBuffer mBuf ;
	private int mFrameSize ;
	private AudioTrack mAt;
	private int mFramePerBuf ;

	PlayerThread( final RingBuffer buf, final int frameSize , final int framePerBuf,  final AudioTrack at ){
		super("PlayerThread");
		
		mBuf = buf ;
		mFrameSize = frameSize ;
		mAt = at ;
		mFrameCount = 0 ;
		mFramePerBuf = framePerBuf;
	}	
	
	
	public void run() {

		final int frameSize = mFrameSize;

		byte[] byteData = null;
		byte[] data = new byte[frameSize];
		final int framePerBuf = mFramePerBuf;
		final AudioTrack at = mAt;
		//at.play();

		try {
			while (!shutdownRequested ) {
				
				waitReady();

				byteData = mBuf.take();
				System.arraycopy(byteData, 0, data, 0, frameSize);
				mBuf.taked();
				at.write(data, 0, frameSize);
				//addFrameCount();
				mFrameCount +=framePerBuf;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private volatile long mEndFrame ;
	public synchronized void setEndFrame(long frame){
		mEndFrame = frame ;
		notifyAll();
	}
	
	public synchronized long getEndFrame(){
		return mEndFrame;
	}
	
    private long mFrameCount ;

    public synchronized long getFrameCount() {
		return mFrameCount;
	}

	public synchronized void setFrameCount(long count) {
		this.mFrameCount = count;
	}

}
