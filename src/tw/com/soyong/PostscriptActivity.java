package tw.com.soyong;

import com.taiwanmobile.myBook_PAD.R;

import tw.com.soyong.mebook.MebookHelper;
import tw.com.soyong.utility.Util;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 章節附錄的主畫面，用於顯示章節附錄的內容
 * @author Victor
 *
 */
public class PostscriptActivity extends Activity {
	
	
//    final GradientDrawable mBackgroundGradient =
//        new GradientDrawable(
//                GradientDrawable.Orientation.TOP_BOTTOM,
//                new int[]{Color.rgb(200, 200, 200), Color.rgb(175, 175, 175)});	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postscript);
        
        Bundle extras = getIntent().getExtras();
        String strPs = extras.getString(MeReaderActivity.PS_DATA);
        String bookID = extras.getString(MeReaderActivity.BK_ID);

        // ps
//        final View psLayout = findViewById(R.id.ps_layout);
//        psLayout.setBackgroundDrawable(mBackgroundGradient);

		final Typeface font = Typeface.createFromAsset(getAssets(),"fonts/syphone.ttf");  
        final TextView tvPs = (TextView)findViewById(R.id.ps_area);
        tvPs.setTypeface(font);
        tvPs.setText( formatPs(strPs , bookID) );
        
        final ImageButton imgBtn_Back = (ImageButton) findViewById(R.id.ps_ImageButton_Back);
        imgBtn_Back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});         
    }

	private CharSequence formatPs(String string, String bookID) {
		
		if ( null == string || string.length() <= 0 ){
			return "";
		}
		
		StringBuilder sb = new StringBuilder(string);
		Util.replaceCRLF(string ,sb );
		Util.replaceFontTag(sb);
		
		//if ( 0 == bookID.compareTo(MebookInfo.JP_BASIC)){
		if ( true == MebookHelper.mIsJpBook) {
			Util.removePhonetic(sb);
		}
		
		return Html.fromHtml(sb.toString());
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_OK);
			finish();
			return true ;
		}
		
		return super.onKeyDown(keyCode, event);
	}
}
