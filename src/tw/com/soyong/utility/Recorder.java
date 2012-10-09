package tw.com.soyong.utility;

import java.io.File;
import java.io.IOException;

import tw.com.soyong.AnReader;
import android.media.MediaRecorder;

public class Recorder {
	
//	private static final String TAG = "Recorder";
	
	public static final String REC_SOURCE = AnReader.getExternalStoreageName() + "/reader.3gpp";
	
	static MediaRecorder mRecorder ;
	
	public static final void start(){
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setOutputFile(REC_SOURCE);
		try {
			mRecorder.prepare();
			mRecorder.start(); // Recording is now started
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static final  void stop(){
		if ( null != mRecorder){
			mRecorder.stop();
			mRecorder.reset(); // You can reuse the object by going back to setAudioSource() step
			mRecorder.release(); // Now the object cannot be reused
			mRecorder = null ;
		}		
	}
	
	public static final  void clearOutputFile(){
		File file = new File(REC_SOURCE);
		file.delete();		
	}
}
