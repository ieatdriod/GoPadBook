package tw.com.soyong;

import tw.com.soyong.IplayerServiceCallback;

interface IPlayerService{
	void setDataSource (String path);
	void setEosTime(int time);
	void setAutoRepeat( boolean enable );
	void play();
	void pause();
	void stop();
	void seekTo (int msec);
	int getCurrentPosition ();
	int getDuration ();
	boolean isPlaying();
	void setAuth(boolean pass);

	void subscribe(IPlayerServiceCallback cb) ;
	void unSubscribe(IPlayerServiceCallback cb);
}