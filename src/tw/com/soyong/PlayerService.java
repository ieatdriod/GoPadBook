package tw.com.soyong;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * MediaPlayer service for recording
 * @author Victor
 *
 */
public final class PlayerService extends Service {

	private static boolean DEBUG = true ;
	private static final String TAG = "PlayerService";
	private MediaPlayer mMediaPlayer = new MediaPlayer();
	private int mSeekTime = 0;
	private boolean mIsStopNotify = false ;
	private int mEosTime = 0;
	static int mDuration = 0 ;
	private boolean mHaveDataSource = false ;

	/**
	 * This is a list of callbacks that have been registered with the service.
	 * Note that this is package scoped (instead of private) so that it can be
	 * accessed more efficiently from inner classes.
	 */
	final RemoteCallbackList<IPlayerServiceCallback> mCallbacks = new RemoteCallbackList<IPlayerServiceCallback>();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/**
	 * The IRemoteInterface is defined through IDL
	 */
	private final IPlayerService.Stub mBinder = new IPlayerService.Stub() {

		private boolean mIsAutoRepeat = true;
		private boolean mIsAuth = false ;

		/**
		 * media player pause
		 */
		public void pause() throws RemoteException {
			if ( false == mHaveDataSource){
				return ;
			}
			
			MediaPlayer mp = mMediaPlayer;
			if (mp.isPlaying()) {
				mp.pause();				// this function seem not synchronize function,
				SystemClock.sleep(40);	// wait some mese, make sure pause() really done
				mIsStopNotify = true ;
				if ( DEBUG ) Log.i(TAG, "pause()");
			} else {
				mp.start();
				mIsStopNotify = false;
				if ( DEBUG ) Log.i(TAG, "rewind()");
				
			}
		}

		/**
		 * media player play
		 */
		public void play() throws RemoteException {
			if ( false == mHaveDataSource){
				return ;
			}
			
			try {
				mMediaPlayer.start();

				if ( DEBUG ) Log.i(TAG, "play()");
				
				mIsStopNotify = false;
				mHandler.sendEmptyMessage(REPORT_MSG);
			} catch (IllegalStateException e) {
			}
		}

		/**
		 * media player stop
		 */
		public void stop() throws RemoteException {
			
			if ( false == mHaveDataSource){
				return ;
			}
			
			MediaPlayer mp = mMediaPlayer;
			
			
			if (mp.isPlaying()) {
				mp.pause();
				SystemClock.sleep(40);	// wait some mese, make sure pause() really done
			}
			
			mSeekTime = 0;
			mp.seekTo(0);
//			seekTo(0);

			// mMediaPlayer.stop(); // this will go into stop state
			if ( DEBUG ) Log.i(TAG, "stop()");
			mIsStopNotify = true ;
		}

		/**
		 * subscribe callback event
		 * @param cb callback event hanlder
		 */
		public void subscribe(IPlayerServiceCallback cb) throws RemoteException {
			if (cb != null) {
				mCallbacks.register(cb);
				if ( DEBUG ) Log.i(TAG, "subscribe()");
			}
		}

		/**
		 * unsubscribe event
		 * @param cb callback event hanlder that subscribe by subscribe()
		 */
		public void unSubscribe(IPlayerServiceCallback cb)throws RemoteException {
			if (cb != null) {
				mCallbacks.unregister(cb);
				if ( DEBUG ) Log.i(TAG, "unSubscribe()");
			}
		}

		/**
		 * get media player current play position
		 * @see MediaPlayer#getCurrentPosition()
		 */
		public int getCurrentPosition() throws RemoteException {
			return mMediaPlayer.getCurrentPosition();
		}

		public int getDuration() throws RemoteException {
			int duration = mMediaPlayer.getDuration();

			// check EOS from mebook tag!!!
			if ( mEosTime > 0 && duration > mEosTime){
				duration = mEosTime;
			}
			
			// auth check
			if ( false == mIsAuth ){
				if ( duration > 5*60*1000 ){
					duration = 5*60*1000;
					if ( DEBUG ) Log.e(TAG , "not auth limmit duration 5 mins");
				}
			}
			
			mDuration = duration;

			if ( DEBUG ) Log.i(TAG, "getDuration():" + String.valueOf(duration));
			return duration;
		}

		public void seekTo(int msec) throws RemoteException {
			
			MediaPlayer mp = mMediaPlayer;
			
			//
			// for some content, voc voice data put aftter EOS time,
			// so we need seekTo the time that after EOS, for play voc voice data
			// but normal play need stop before reach EOS
			//
			if ( msec > mp.getDuration() || msec < 0 ){
				if ( DEBUG ) Log.e(TAG, "seekTo param error" );
				return ;
			}

//			mHandler.removeMessages(REPORT_MSG);
			mSeekTime = msec;
			mp.seekTo(msec);
			if ( DEBUG ) Log.i(TAG, "seekTo():" + String.valueOf(mp.getCurrentPosition()));
		}

		public void setDataSource(String path) throws RemoteException {
			try {
				final MediaPlayer mp =mMediaPlayer ; 

				mp.reset();
				
			    File file = new File(path);
			    FileInputStream fis = new FileInputStream(file);
			    mp.setDataSource(fis.getFD());
				//mp.setDataSource(path);
				mp.prepare();
				
				mHaveDataSource = true ;

				mp.setOnCompletionListener(new OnCompletionListener() {

					public void onCompletion(MediaPlayer arg0) {
						if ( DEBUG ) Log.i(TAG, "onCompletion()");
//						mHandler.sendEmptyMessage(REPORT_COMPLETION);

						onEos();
					}
				});

				mp.setOnSeekCompleteListener(new OnSeekCompleteListener() {

					public void onSeekComplete(MediaPlayer mp) {
						// TODO double check seek position, this coule be remove after final relase test
						int seekTime = mSeekTime ;
						int curTime = mp.getCurrentPosition();
						if (seekTime != curTime && !mp.isPlaying()) {
							if ( DEBUG ) Log.e(TAG, "seek again!");
							mp.seekTo(seekTime);
						} else {
							if ( DEBUG ) Log.i(TAG, "Seek complete!");
							mHandler.removeMessages(REPORT_MSG);		//clear queue
							mHandler.sendEmptyMessage(REPORT_MSG);
						}
					}
				});

//				mHandler.sendEmptyMessage(REPORT_MSG);
				if ( DEBUG ) Log.i(TAG, "setDataSource()");
				return ;

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mHaveDataSource = false ;
		}
		
		private void onEos() {
			final MediaPlayer mp = mMediaPlayer ;
			if (true == mIsAutoRepeat) {
				mSeekTime = 0;
				mp.seekTo(0);
				//						
				try {
					play();
					if ( DEBUG ) Log.i(TAG,"Auto restart when receive onCompletion notify!");
					// stop();
					// if ( DEBUG ) Log.i(TAG , "Auto stop when receive
					// onCompletion notify!");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}else{
				mHandler.sendEmptyMessage(REPORT_COMPLETION);
			}
		}
		

		public void setEosTime(int time) throws RemoteException {
			mEosTime = time ;
		}

		public boolean isPlaying() throws RemoteException {
			return mMediaPlayer.isPlaying();
		}

		public void setAutoRepeat(boolean enable) throws RemoteException {
			mIsAutoRepeat = enable ;
		}

		public void setAuth(boolean pass) throws RemoteException {
			// auth check
			mIsAuth = pass ;
			if ( DEBUG ) Log.e(TAG , "setAuth:"+pass );
		}
	};

	private static final int MSG_INTERVAL = 250;
	private static final int REPORT_MSG = 1;
	private static final int REPORT_COMPLETION = 2;
	

	/**
	 * Our Handler used to execute operations on the main thread. This is used
	 * to schedule increments of our value.
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {

			case REPORT_MSG: {
				int value = mMediaPlayer.getCurrentPosition();

				if ( DEBUG ) Log.i(TAG, String.valueOf(value));

				final RemoteCallbackList<IPlayerServiceCallback> cb = mCallbacks;
				// Broadcast to all clients the new value.
				final int N = cb.beginBroadcast();
				for (int i = 0; i < N; i++) {
					try {
						cb.getBroadcastItem(i).timeChanged(value);
					} catch (RemoteException e) {
						// The RemoteCallbackList will take care of removing
						// the dead object for us.
					}
				}
				cb.finishBroadcast();

				if ( true != mIsStopNotify /*&& value <= mDuration*/){
					sendMessageDelayed(obtainMessage(REPORT_MSG), MSG_INTERVAL);
				} 
//				else {
//					// TODO should we remove this?
//					if ( value > mDuration ){
//						if ( DEBUG ) Log.e(TAG , "playtime > mDuration???");
//						//sendMessageDelayed(obtainMessage(REPORT_MSG), MSG_INTERVAL);
//					}
//				}
			}
				break;
				
			case REPORT_COMPLETION:
				if ( DEBUG ) Log.i(TAG, "REPORT_COMPLETION" );
				int duration = mMediaPlayer.getDuration();
				
				final RemoteCallbackList<IPlayerServiceCallback> cb = mCallbacks;
				// Broadcast to all clients the new value.
				final int N = cb.beginBroadcast();
				for (int i = 0; i < N; i++) {
					try {
						cb.getBroadcastItem(i).timeChanged(duration);
					} catch (RemoteException e) {
						// The RemoteCallbackList will take care of removing
						// the dead object for us.
					}
				}
				cb.finishBroadcast();				
				break ;

			default:
				super.handleMessage(msg);
			}
		}
	};
	
	
	public class MyPhoneStateListener extends PhoneStateListener {
//		Context context;
		boolean mPauseByPhone = false ;

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			
			MediaPlayer mp = mMediaPlayer;
			if ( null == mp ){
				return ;
			}

			boolean isPlaying = false ;
			try{
				isPlaying = mp.isPlaying();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			
		    switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				if ( DEBUG ) Log.e(TAG, "PHONE STATE - IDLE " + incomingNumber);
				if ( true == mPauseByPhone){
					mPauseByPhone = false ;
					mp.start();
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if ( DEBUG ) Log.e(TAG, "PHONE STATE - OFFHOOK " + incomingNumber);
				if (isPlaying) {
					mp.pause();
					mPauseByPhone = true ;
				}				
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				if ( DEBUG ) Log.e(TAG, "PHONE STATE - RINGING " + incomingNumber);

				if (isPlaying) {
					mp.pause();
					mPauseByPhone = true ;
				}
				break;
			} 			
		}
	} 	
	
	@Override
	public void onCreate() {
		super.onCreate();

		if ( DEBUG ) Log.i(TAG, "onCreate");

		// Register a listener for call state
		MyPhoneStateListener phoneListener=new MyPhoneStateListener(); 
		TelephonyManager telephonyManager =(TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
		telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE); 
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if ( DEBUG ) Log.e(TAG, "PlayerService onDestroy!!!");

		// Unregister all callbacks.
		mCallbacks.kill();

		// Remove the next pending message to increment the counter, stopping
		// the increment loop.
		mHandler.removeMessages(REPORT_MSG);

		// Release media player object
		MediaPlayer mp = mMediaPlayer;
		mp.release();
		mp = null;
	}
}
