package tw.com.soyong;

import java.util.ArrayList;
import java.util.HashMap;

import com.taiwanmobile.myBook_PAD.R;

import tw.com.soyong.mebook.MebookHelper;
import tw.com.soyong.mebook.MebookToken;
import tw.com.soyong.mebook.SySentence;
import tw.com.soyong.mebook.SyVocInfo;
import tw.com.soyong.utility.Util;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 延伸內容主畫面
 * @author Victor
 *
 */
public class ExtendActivity extends Activity implements ImageGetter {
	
	private static final boolean DEBUG = false ;
	private static final String TAG ="ExtendActivity";
	
	LocalService mSyPlayerSvc = null;
	int mOldPlayPos = 0 ;
	int mPlayEndTime = -1 ;
	int mVocPos = 0 ;
	ArrayList<SyVocInfo> mVocInfo ;
	Button mVocPlay;

	private String formatSpell(SySentence sentence) {
		
		ArrayList<String> spellList  = new ArrayList<String>() ;
		ArrayList<String> wordList = new ArrayList<String>();
		
		HashMap<Integer, String> data = sentence.mData;
		
		// org
		String org = data.get(MebookToken.TOK_ORG);
		
		int index = 0 , start , end ;
		index = org.indexOf('[');
		
		String strSpell;
		while( index > -1 ){
			start = index+1 ;
			index = org.indexOf(']', index);
			if ( index >-1 ){
				end = index ;
				
				if ( end < start){
					if (DEBUG) Log.d(TAG , "tag mismatch !!");
					break;
				}
				
				if (end > start) {
					strSpell = org.substring(start, end);

					spellList.add(strSpell);

					String[] spellCell = strSpell.split(",");
					String word = org.substring(start - 2 - spellCell.length
							+ 1, start - 1);
					wordList.add(word);
				}
			}
			index = org.indexOf('[', index);
		}
		
		// trl
		String trl = data.get(MebookToken.TOK_TRL);
		index = 0;
		index = trl.indexOf('[');
		
		while( index > -1 ){
			start = index+1 ;
			index = trl.indexOf(']', index);
			if ( index >-1 ){
				end = index ;
				
				if ( end < start){
					if (DEBUG) Log.d(TAG , "tag2 mismatch !!");
					break;
				}
				
				if (end > start) {
					strSpell = trl.substring(start, end);

					spellList.add(strSpell);

					String[] spellCell = strSpell.split(",");
					String word = trl.substring(start - 2 - spellCell.length
							+ 1, start - 1);
					wordList.add(word);
				}
			}
			index = trl.indexOf('[', index);
		}		
		
		// voc
		ArrayList<SyVocInfo> vocInfo = sentence.mVocInfo;

		
		for (SyVocInfo voc : vocInfo) {
			
			if ( wordList.indexOf(voc.mVoc) > -1 ){
				continue ;
			}
			
			if (null != voc.mPhonetic && voc.mPhonetic.length() > 0) {
				wordList.add(voc.mVoc);
				spellList.add(voc.mPhonetic);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		
		int len = wordList.size();
		String strPh;
		String strWord;
		String [] spellCell ;
		for (int i = 0; i < len; i++) {
			
			StringBuilder sbWord  = new StringBuilder();
			StringBuilder sbSpell = new StringBuilder();
			

			strWord = wordList.get(i);
			strPh = spellList.get(i);
			spellCell = strPh.split(",");
			
			int count = spellCell.length;
			sb.append("<table border='0' cellspacing='1'>");
			sbWord.append("<tr align=center>");
			sbSpell.append("<tr>");
			
			for ( int k = 0 ; k < count ; k ++){
				
				
				sbWord.append("<td>"+strWord.charAt(k)+"</td>");
				sbSpell.append("<td>"+spellCell[k]+"</td>");
		
			}
			sbWord.append("</tr>");
			sbSpell.append("</tr>");
			
			sb.append(sbSpell);
			sb.append(sbWord);
			sb.append("</table><br>");
			
		}
		return sb.toString();
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extend);
        
        final float fontSize = MeReaderActivity.mSettingPref.getFontSize(this.getApplicationContext());
        Bundle extras = getIntent().getExtras();
        int curIndex = extras.getInt(MeReaderActivity.CUR_INDEX);
        boolean isJpContent = MebookHelper.mIsJpBook;

        final SySentence sentence = MebookHelper.mSentenceArr[curIndex];
        
        String htm = "";
        if ( true == isJpContent){
        	htm = formatSpell(sentence);
        }        
        
        // spell for JP basic content
        final View spellLayout = findViewById(R.id.spell_layout);
        final WebView wvSpell = (WebView)findViewById(R.id.spell_area);
        if ( false == isJpContent || htm.length() < 1 ){
        	spellLayout.setVisibility(View.GONE);
        }else{
        	wvSpell.getSettings().setDefaultTextEncodingName("utf-8");
        	wvSpell.loadDataWithBaseURL("file:///android_asset/",htm, "text/html", "utf8","");
        	wvSpell.setBackgroundColor(Color.TRANSPARENT);
        }
        
        final Typeface font = Typeface.createFromAsset(getAssets(), "fonts/syphone.ttf");
        // voc
        final View vocLayout = findViewById(R.id.voc_layout);
        final TextView tvVoc = (TextView)findViewById(R.id.voc_area);
        if ( null == sentence.mVocInfo || sentence.mVocInfo.size() <= 0 ){
	        vocLayout.setVisibility (View.GONE);
        }else {
        	
        	tvVoc.setTypeface(font); 
        	tvVoc.setTextSize(TypedValue.COMPLEX_UNIT_PT,fontSize);
        	tvVoc.setText( formatVoc(sentence.mVocInfo , isJpContent) );
        	
        	if ( canPlay(sentence.mVocInfo) && null != MeReaderActivity.mSyPlayerSvc){
        		setVolumeControlStream(AudioManager.STREAM_MUSIC);

        		final Button vocPlay = (Button)findViewById(R.id.btn_voc_play);
        		mVocPlay = vocPlay;
        		vocPlay.setVisibility(View.VISIBLE);
        		vocPlay.setOnClickListener( new OnClickListener(){

					@Override
					public void onClick(View view) {
						onVocPlay();
					}
        		} );
        		
        		mVocInfo = sentence.mVocInfo;
        		mSyPlayerSvc = MeReaderActivity.mSyPlayerSvc;
        		mOldPlayPos = mSyPlayerSvc.getCurrentPosition();
        	}else{
        		mSyPlayerSvc = null;
        	}
        }
        
        // syn
        final View synLayout = findViewById(R.id.syn_layout);
        final TextView tvSyn = (TextView)findViewById(R.id.syn_area);
        if ( false == sentence.isDataValid( MebookToken.TOK_SYN)){
	        synLayout.setVisibility (View.GONE);
        }else{
        	tvSyn.setTypeface(font);
        	tvSyn.setText(formatData(sentence.mData.get(MebookToken.TOK_SYN)));
        	tvSyn.setTextSize(TypedValue.COMPLEX_UNIT_PT,fontSize);
        }

        // ant
        final View antLayout = findViewById(R.id.ant_layout);
        final TextView tvAnt = (TextView) findViewById(R.id.ant_area);
		if (false == sentence.isDataValid(MebookToken.TOK_ANT)) {
			antLayout.setVisibility(View.GONE);
		}else{
			tvAnt.setTypeface(font);
			tvAnt.setText(formatData(sentence.mData.get(MebookToken.TOK_ANT)));
			tvAnt.setTextSize(TypedValue.COMPLEX_UNIT_PT,fontSize);
		}

        //ext
		final View extLayout = findViewById(R.id.ext_layout);
		final TextView tvExt = (TextView) findViewById(R.id.ext_area);
		if (false == sentence.isDataValid(MebookToken.TOK_EXT)) {
			extLayout.setVisibility(View.GONE);
		}else{
			tvExt.setTypeface(font);
			tvExt.setText(formatData(sentence.mData.get(MebookToken.TOK_EXT)));
			tvExt.setTextSize(TypedValue.COMPLEX_UNIT_PT,fontSize);
		}

        final ImageButton imgBtn_Back = (ImageButton) findViewById(R.id.ext_ImageButton_Back);
        imgBtn_Back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				exit();
			}
		}); 		
    }

	protected void onVocPlay() {
		
		final int size = mVocInfo.size();
		if ( mVocPos >= size ){
			mVocPos = 0 ;
		}
		
		SyVocInfo voc = null;
		for ( int i = mVocPos ; i < size ; i++){
			voc = mVocInfo.get(i);
			if ( voc.isValidTime()){
				mVocPos = i+1 ;
				break ;
			}
		}
		
		if ( null != voc){
			mSyPlayerSvc.stop();
			int beginTime = voc.mBeginTime.getValue(); 
			int endTime = voc.mEndTime.getValue();
			mSyPlayerSvc.play( beginTime , endTime);
		}
	}

	private boolean canPlay(ArrayList<SyVocInfo> vocInfo) {
		
		boolean bVoice = false ;
		for ( SyVocInfo voc : vocInfo){
			
			if ( voc.isValidTime()){
				bVoice = true ;
				if (DEBUG) Log.d(TAG , "voc could be play!");
				break ;
			}
		}
		return bVoice;
	}

	private CharSequence formatVoc(ArrayList<SyVocInfo> vocInfo , boolean isJpContent) {
		
		StringBuilder sb = new StringBuilder();
		
		for ( SyVocInfo voc : vocInfo){
			sb.append(voc.mVoc);
			sb.append(" ");
			
			if ( false == isJpContent){
			if (null != voc.mPhonetic && voc.mPhonetic.length() > 0) {
					sb.append("[");
					//String phone = Util.ReplacePhonogramChars(voc.mPhonetic);
					//sb.append(phone);
					sb.append(voc.mPhonetic);
					sb.append("] ");
				}
			}
			
			if ( true == isJpContent ){
				StringBuilder sbOther = new StringBuilder(voc.mOthers);
				Util.removePhonetic(sbOther);
				sb.append(sbOther);
			}else {
				sb.append(voc.mOthers);
			}			
			sb.append("<br>");
		}
		Util.replaceFontTag(sb);
		
		Util.replaceCRLF(sb.toString(), sb);
		
		return Html.fromHtml( sb.toString(), this , null);
	}

	private CharSequence formatData(String string) {
		
		StringBuilder sb = new StringBuilder(string);
		sb.append("<br>");
		Util.replaceFontTag(sb);
		Util.replaceCRLF(sb.toString(), sb);
		Util.removePhonetic(sb);
		
		return Html.fromHtml(sb.toString());
	}

	@Override
	public Drawable getDrawable(String source) {
		return null;
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return true ;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	private void exit() {

		if (null != mSyPlayerSvc) {
			mSyPlayerSvc.stop();
			mSyPlayerSvc.seekTo(mOldPlayPos);
			mSyPlayerSvc = null;
		}

		Bundle bundle = new Bundle();
		Intent intent = new Intent();
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		finish();
	}
}
