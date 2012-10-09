package com.gsimedia.gsiebook;

import com.taiwanmobile.myBook_PAD.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AnnotationActivity extends Activity {

	public static final String KEY_MODE = "mode";
	public static final String KEY_NOTE = "note";
	public static final int ModeShow = 0;
	public static final int ModeEdit = 1;
	private int iMode = ModeShow;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gsi_anno);
		findViews();
		Bundle aExtra = getIntent().getExtras();
		int aMode = aExtra.getInt(KEY_MODE); 
		if(aMode == ModeShow){
			String aNote = aExtra.getString(KEY_NOTE);
			iNoteView.setText(aNote);
		}
		switchMode(aMode);
	}
	Button iBackButton = null;
	Button iEditButton = null;
	EditText iNoteView = null;
	private void findViews(){
		iNoteView = (EditText) findViewById(R.id.text);
		iBackButton = (Button) findViewById(R.id.gsimedia_btn_title_left);
		iBackButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				finish();
			}
			
		});
		iEditButton = (Button) findViewById(R.id.gsimedia_btn_title_right);
		iEditButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				switch(iMode){
				case ModeShow:
					// switch to edit mode
					switchMode(ModeEdit);
					iNoteView.requestFocus();
					break;
				case ModeEdit:
					// go back
//					switchMode(ModeShow);
					String aNote = iNoteView.getText().toString();
					if(!aNote.equals("")){
						Intent intent = new Intent();
						Bundle aBundle = new Bundle();
						intent.setClass(AnnotationActivity.this, RendererActivity.class);
						aBundle.putString(RendererActivity.KEY_Annotation, aNote);
						intent.putExtras(aBundle);
						setResult(RendererActivity.Result_Annotation, intent);
					}
					finish();
					break;
				}
			}
			
		});
	}
	private void switchMode(int aMode){
		iMode = aMode;
		switch(iMode){
		case ModeShow:
			//iEditButton.setBackgroundResource(R.drawable.gsi_button18_btn);
			iEditButton.setBackgroundResource(R.drawable.gsi_button05_btn);
			iNoteView.setFocusable(false);
			iNoteView.setClickable(false);
			iNoteView.setFocusableInTouchMode(false);
			/*iNoteView.setFilters(new InputFilter[] {
					new InputFilter() {
						public CharSequence filter(CharSequence src, int start,
								int end, Spanned dst, int dstart, int dend) {
							return src.length() < 1 ? dst.subSequence(dstart, dend) : "";
						}
					}
			});
			iNoteView.setCursorVisible(false);
			*/
			break;
		case ModeEdit:
			//iEditButton.setBackgroundResource(R.drawable.gsi_button17_btn);
			iEditButton.setBackgroundResource(R.drawable.gsi_button04_btn);
			iNoteView.setFocusable(true);
			iNoteView.setClickable(true);
			iNoteView.setFocusableInTouchMode(true);
			/*
			iNoteView.setFilters(new InputFilter[] {
					new InputFilter() {
						public CharSequence filter(CharSequence src, int start,
								int end, Spanned dst, int dstart, int dend) {
							return src;
						}
					}
			});
			iNoteView.setCursorVisible(true);
			*/
			break;
		}
	}

}
