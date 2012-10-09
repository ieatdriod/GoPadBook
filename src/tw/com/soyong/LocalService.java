/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tw.com.soyong;

import tw.com.soyong.mebook.MebookHelper;
import tw.com.soyong.utility.Native;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Provide API for player and handle player event
 * @author Victor
 *
 */
public class LocalService extends Service {
	
	private static final boolean DEBUG = false ;
	private static final String TAG = "LocalService";

    public class LocalBinder extends Binder {
        LocalService getService() {
            return LocalService.this;
        }
    }
    
    private AudioTrack mAt ;
    private int mSamplePerSec;
    private int [] mFrameTable;
    private long mTotalFrams;
    
    private static RingBuffer mBuf;
    private static final int RING_BUF_SIZE = 4 ;
    private static final int FRAME_PER_BUF = 4 ;
    
    /**
     * Mp3 frame tolerance
     */
    public static final int TOLERANCE = (26*(FRAME_PER_BUF+1));	
    private static final int SAMPLES_PER_FRAME = 1152;
    private DecodeThread mDecThread = null; 
    private PlayerThread mPlayThread = null;
    
    private volatile boolean mCreateDone = false ;
    
    /**
     * Get player initial state
     * @return true: initial done
     * 		   false : not initial
     */
    public synchronized boolean isInitial(){
    	return mCreateDone;
    }
    
    @Override
    public void onCreate() {
    	
    	if (DEBUG) Log.e(TAG, "============> .onCreate");
    	
        int channel;
        int numCh = Native.getNumChannel();
        if ( 1 == numCh){
        	channel = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        } else {
        	channel = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        }
        
        final int samplePerSec = Native.getSamplePerSec();
        mSamplePerSec = samplePerSec;
        
        int intSize = android.media.AudioTrack.getMinBufferSize(samplePerSec, channel,AudioFormat.ENCODING_PCM_16BIT);
        int frameSize = SAMPLES_PER_FRAME * 2 * numCh;
     	AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, samplePerSec, channel,
                AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);
     	mAt = at ;
     	
     	// cache frame table
     	if ( null != MebookHelper.mFrameTable){
     		mFrameTable = MebookHelper.mFrameTable;
     	}
     	
     	long totlaFrames = mFrameTable.length << 2 ;
     	mTotalFrams = totlaFrames;
     	 
     	mBuf = new RingBuffer(RING_BUF_SIZE);

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
     	DecodeThread decThread = new DecodeThread(mBuf , RING_BUF_SIZE , frameSize*FRAME_PER_BUF);
     	decThread.setThreadPause(false);
     	decThread.start();
     	mDecThread = decThread;
        
    	PlayerThread thrPlayer = new PlayerThread(mBuf , frameSize*FRAME_PER_BUF , FRAME_PER_BUF , at);
    	thrPlayer.setThreadPause(true);
    	thrPlayer.start();  
    	mPlayThread = thrPlayer;
    	
    	
		// Register a listener for call state
		MyPhoneStateListener phoneListener=new MyPhoneStateListener(); 
		TelephonyManager telephonyManager =(TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
		telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE); 
		
		mCreateDone = true ;
    }
    
   
    @Override
    public void onDestroy() {
    	
    	if (DEBUG ) Log.e(TAG, "============> .onDestroy");
    	
    	if ( null != mDecThread ){
    		mDecThread.shutdownRequest();
    	}
    	if ( null != mPlayThread){
    		mPlayThread.shutdownRequest();
    	}
    	
        // Cancel the persistent notification.
        //mNM.cancel(R.string.local_service_started);

        // Tell the user we stopped.
        //Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    	if ( null != mAt){
    		mAt.stop();
    		mAt = null;
    	}
    	
    	// TODO: close will crash!!!
    	Native.Close();
    }

    @Override
    public IBinder onBind(Intent intent) {
    	if (DEBUG) Log.e(TAG, "============> .onBind");
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Play mp3 from begin time tile specific end time
     * @param beginTime 開始時間
     * @param endTime 結束時間
     */
    public void play( final int beginTime , final int endTime){
    	mPlayThread.setThreadPause(true);
    	seekTo(beginTime);
    	
    	// calc endFrame
    	long frame = endTime / 100 * mSamplePerSec  / SAMPLES_PER_FRAME /10 ;
    	frame = Math.min( frame , mTotalFrams ); 
    	mPlayThread.setEndFrame(frame);

    	mPlayThread.setThreadPause(false);
    	mIsPlaying = true ;	
    }
    
    /**
     *  Play mp3 until EOS time
     */
    public void play() {
    	mPlayThread.setEndFrame(mEosFrame);
    	
    	mPlayThread.setThreadPause(false);
    	mIsPlaying = true ;
    }
    	
    
    // for pause/ replay
    /**
     *  Pause playing mp3/ or replay paused mp3
     */
    public void pause() {
    	//mAt.pause();
    	boolean b = mIsPlaying;
    	mPlayThread.setThreadPause(b);
    	mIsPlaying = !b;
    }
    
    /**
     *  stop mp3 playing
     */
    public void stop() {
    	//mAt.stop();
    	mPlayThread.setThreadPause(true);
    	mIsPlaying = false ;
    }
    
    /**
     * get current playing time
     * @return integer value for current playing time
     */
    public int getCurrentPosition(){
    	
    	long frames = mPlayThread.getFrameCount();
    	
    	long samplePerSec = mSamplePerSec;
    	
    	frames = frames * 10 * SAMPLES_PER_FRAME + (samplePerSec >>1 );
    	
    	//frames = Math.min(frames, mTotalFrams);    	
    	
    	return (int)(frames/samplePerSec*100) ;
    }
    
//    public int getDuration() {
//    	
//    	
//    	//long totlaFrames = (mFrameTable.length << 2) * 10 * SAMPLES_PER_FRAME + (samplePerSec >>1 );
//    	//return  (int) (mTotalFrams*10 / mSamplePerSec*100) ;
//    	return (int) (mTotalFrams * 10 * SAMPLES_PER_FRAME / mSamplePerSec * 100);
//    }
    
    /**
     * set mp3 frame table for random seek
     * @param table mp3 frame table
     */
    public void setFrameTable(int[] table){
    	
    	mFrameTable = table;
     	long totlaFrames = table.length << 2 ;
     	mTotalFrams = totlaFrames;
    }
    
    
    long mEosFrame ;
    /**
     * set mp3 End Of Sentence time
     * @param eos End Of Sentence time
     */
    public void setEosTime(int eos){
    	
    	if ( eos <= 0 ){
    		mEosFrame = mTotalFrams;
    	}else {
	    	long frame = eos / 100 * mSamplePerSec  / SAMPLES_PER_FRAME /10 ;
	    	frame = Math.min( frame , mTotalFrams );
	    	mEosFrame = frame ;
    	}
    }
    
    /**
     * seek playing position to specific time
     * @param msec time to seek
     */
    public void seekTo(int msec) {
    	
    	// monkey
    	if ( null ==  mDecThread){
    		Log.e(TAG , "mDecThread null !!!" );
    		return ;
    	}
    	
    	mDecThread.setThreadPause(true);

    	long frame = msec / 100 * mSamplePerSec  / SAMPLES_PER_FRAME /10 ;
    	
    	frame = Math.min( frame , mTotalFrams );
    	
    	int index = (int)(frame >> 2) ;

    	
    	//int index = frame >> 2 ;
    	frame = index << 2 ;
    	
    	long offset ;
    	
    	try {
    		offset = mFrameTable[index];
    	} catch (IndexOutOfBoundsException e) {
    		offset = 0 ;
    	}
    	
    	Native.seek(offset);
    	mBuf.clearAll();
    	mPlayThread.setFrameCount(frame);
    	
    	mDecThread.setThreadPause(false);
    	
    	if ( mBuf.getCount() < (RING_BUF_SIZE/2)){
    		try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	
    }
    
    private boolean mIsPlaying = false ;
    
    /**
     * get current playing state
     * @return true playing, otherwise pause or stop
     */
    public boolean  isPlaying  (){
    	return mIsPlaying;
    }
    
//    private boolean mAutoRepeat = true ;
//    public void setAutoRepeat(boolean enable) {
//    	mAutoRepeat = enable ;
//    }
    
    public void destroy() {
    	
    	if ( null != mDecThread && null != mPlayThread){
    	
    		mDecThread.shutdownRequest();
			mPlayThread.shutdownRequest();
			mBuf.clearAll();

			try {
				mDecThread.join(500);
				mPlayThread.join(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mDecThread = null;
			mPlayThread = null;
    	}
    }
    
    //////////////////////////////////////////////
    
    /**
     *  Receive phone state change event
     */
	public class MyPhoneStateListener extends PhoneStateListener {
		boolean mPauseByPhone = false ;

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			
			final AudioTrack at = mAt;
			if( null == at ){
				return ;
			}
			
			boolean isPlaying = mIsPlaying;

		    switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				if ( DEBUG ) Log.e(TAG, "PHONE STATE - IDLE " + incomingNumber);
				if ( true == mPauseByPhone){
					mPauseByPhone = false ;
					at.play();
					play();
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if ( DEBUG ) Log.e(TAG, "PHONE STATE - OFFHOOK " + incomingNumber);
				if (isPlaying) {
					at.stop();
					stop();
					mPauseByPhone = true ;
				}				
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				if ( DEBUG ) Log.e(TAG, "PHONE STATE - RINGING " + incomingNumber);

				if (isPlaying) {
					at.stop();
					stop();
					mPauseByPhone = true ;
				}
				break;
			} 			
		}
	}    
    
}

