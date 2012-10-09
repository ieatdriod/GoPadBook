package tw.com.soyong;

import android.util.Log;

/**
 * Implement ring buffer for mp3 frame data
 * @author Victor
 *
 */
public class RingBuffer {
	
	
	private static final boolean DEBUG = false ;
	private static final String TAG = "RingBuffer";	
	
	private byte[][] mBuf;
	private int mTail;
	private int mHead;
	private volatile int mCount;
	private volatile boolean [] mReady ;
	
	
	/**
	 * RingBuffer constrator
	 * @param count max size of ring buffer
	 */
	public RingBuffer(int count ){
		mHead = 0 ;
		mTail = 0 ;
		mCount = 0 ;
		mBuf = new byte[count][];
		
		mReady = new boolean [count];
		for ( int i = 0 ; i < count ; i++){
			mReady[i] = false ;
		}
	}
	
	/**
	 * put frame data into ring buffer<BR>
	 * if buffer full, the function become wait until buffer available
	 * @param frame frame data in byte 
	 * @throws InterruptedException
	 */
	public synchronized void put( byte[] frame ) throws InterruptedException {
		
		while( true == mReady[mTail]){
			if (DEBUG) Log.e(TAG , Thread.currentThread().getName() + "wait put:" + mTail);
			wait();
		}
		
		final int bufLen = mBuf.length;
		
		int tail = mTail;
		mBuf[tail] = frame;
		mReady[tail]= true ;
		mTail = (tail+1)% bufLen;
		++ mCount;
		
		if (DEBUG) Log.d(TAG , Thread.currentThread().getName() + " put:" + tail + " count:"+ mCount);
		
		notifyAll();		
	}
	
	/**
	 * take frame data from ring buffer<BR>
	 * if ring buffer is empty, the function will wait until buffer have data
	 * @return frame data in byte
	 * @throws InterruptedException
	 */
	public synchronized byte[] take() throws InterruptedException{
		
		while( false == mReady[mHead]){
			if (DEBUG) Log.e(TAG , Thread.currentThread().getName() + "wait take:" + mHead);
			wait();
		}
		
		int head = mHead;
		final byte[][]buf = mBuf ;
		byte [] frame = buf[head];
		if (DEBUG) Log.d(TAG , Thread.currentThread().getName() + " take:" + head);
		
		return frame;
	}
	
	/**
	 * mark frame data in buffer already taked
	 */
	public synchronized void taked(){
		if (DEBUG) Log.d(TAG , Thread.currentThread().getName() + " taked:" + mHead);

		final int head = mHead;
		final byte[][]buf = mBuf ;
		mReady[head] = false ;
		mHead = (head+1)%buf.length;
		-- mCount;
		
		notifyAll();
	}
	
	/**
	 * get ring buffer's count
	 * @return number of data in ring buffer
	 */
	public synchronized int getCount() {
		return mCount;
	}
	
	
	/**
	 *  clear all data in the ring buffer
	 */
	public synchronized void clearAll() {
		mHead = 0 ;
		mTail = 0 ;
		mCount = 0 ;
		
		final int count = mReady.length;
		for ( int i = 0 ; i < count ; i++){
			mReady[i] = false ;
		}
		notifyAll();
	}

}
