package tw.com.soyong;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.taiwanmobile.myBook_PAD.R;

import tw.com.soyong.mebook.MebookHelper;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 書城介紹頁<br>
 * 出現於最後一句之後
 * @author Victor
 *
 */
public class LastPageActivity extends Activity {
	
	private static final boolean DEBUG = true ;
	private static final String TAG = "LastPageActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lastpage);
		
		final View panel = (View)findViewById(R.id.panel);
		panel.setOnTouchListener( new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				final int act = event.getAction();

				switch (act) {
				case MotionEvent.ACTION_DOWN: {

					Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
							.getDefaultDisplay();
					int width = display.getWidth();

					boolean isGoNext = true;
					if (event.getX() < width / 2) {
						isGoNext = false;
					}

					Bundle bundle = new Bundle();
					Intent intent = new Intent();
					bundle.putBoolean("goNext", isGoNext);
					intent.putExtras(bundle);
					setResult(RESULT_OK, intent);
					finish();
				}
					break;
				}
				return false;
			}
			
		});
		

		final TextView txtVw_Content = (TextView) findViewById(R.id.lastpage_TextView_Content);
		final ImageButton imgBtn_Bar = (ImageButton) findViewById(R.id.lastpage_ImageButton_Bar);

		Bundle bundle = this.getIntent().getExtras();
		final String pageFlg = bundle.getString("pageFlg"); // Try or Active

		if (pageFlg.equals("Try") == true) {
			txtVw_Content.setText(R.string.lastpage_content2);
			imgBtn_Bar.setBackgroundResource(R.drawable.ian_trial01);
		} else {
			txtVw_Content.setText(R.string.lastpage_content1);
			imgBtn_Bar.setBackgroundResource(R.drawable.ian_trial02);
		}

		imgBtn_Bar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Intent it = new Intent();
				it.setAction(Intent.ACTION_VIEW);
				if (pageFlg.equals("Try") == false) {
					try {
						String urlString = getResources().getString(R.string.lastpage_url1);
						
						String key = MebookHelper.mBookTitle;
						if ( null !=  MebookHelper.mBookAuthors && MebookHelper.mBookAuthors.length() > 0   ){
							key  = MebookHelper.mBookAuthors;
						} else if ( null != MebookHelper.mBookPublisher && MebookHelper.mBookPublisher.length() > 0 ){
							key = MebookHelper.mBookPublisher;
						}
						
						String encode = URLEncoder.encode(key, "UTF-8");
						it.setData(Uri.parse(urlString+encode));
						
						if (DEBUG) Log.d(TAG , urlString+encode);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						finish();
						return ;
					}
				} else {
					String urlString = String.format(getResources().getString(R.string.lastpage_url2), MebookHelper.mContentID);
					it.setData(Uri.parse(urlString));
				}

				startActivity(it);
				
				finish();
			}
		});
		
		
	}
}
