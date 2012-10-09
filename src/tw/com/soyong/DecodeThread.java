package tw.com.soyong;

import tw.com.soyong.utility.Native;
import android.util.Log;

public class DecodeThread extends Thread {
	
	private static final boolean DEBUG = false ;
	private static final String TAG = "DecodeThread";
	
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
	
	private volatile boolean mThreadPause = false ;
	public synchronized void setThreadPause( boolean isPause) {
		
		mThreadPause = isPause ;
		if ( !isPause ){
			notifyAll();
		}
	}
	
	public synchronized void waitReady() throws InterruptedException{
		while( true == mThreadPause ){
			if (DEBUG) Log.e(TAG , "+++ waitReady()" );
			wait();
		}
	}
	
	RingBuffer mBuf ;
	int mBufSize ;
	int mFrameSize ;
	
	DecodeThread( final RingBuffer buf, final int bufSize , final int frameSize ){
		super("DecodeThread");
		
		mBuf = buf ;
		mBufSize = bufSize ;
		mFrameSize = frameSize ;
	}
	
	public void run() {

		final int bufSize = mBufSize;
		final byte[][] byteData = new byte[bufSize][mFrameSize];

		int index = 0;
		int readCount;
		final RingBuffer buf = mBuf;
		try {
			
			while (!shutdownRequested ) {
				waitReady();

				readCount = Native.read(byteData[index]);
				if (readCount > 0) {
					buf.put(byteData[index]);
					index = (index + 1) % bufSize;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
		}
	}
}
