package com.taiwanmobile.myBook_PAD;

import org.iii.ideas.reader.annotation.Annotation;
import org.iii.ideas.reader.annotation.AnnotationDB;

import com.gsimedia.gsiebook.AnnotationActivity;
import com.gsimedia.gsiebook.RendererActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
/**
 * 設定註記
 * @author III
 * 
 */
public class SettingNotes extends Activity {
    /** Called when the activity is first created. */
	public static final String KEY_MODE = "mode";
	public static final String KEY_NOTE = "note";
	public static final int ModeShow = 0;
	public static final int ModeEdit = 1;
	private int iMode = ModeShow;
	
	private boolean isAnnotated;
	private int span,idxInSpan;
	private String bookName,chapName,epubPath;
	private int id;
	private AnnotationDB annHelper;
	private Annotation curAnn;
    @Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.iii_setting_notes);
        setContentView(R.layout.gsi_anno);
		findViews();
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		annHelper = new AnnotationDB(this);
        Intent it = this.getIntent();
        isAnnotated = it.getBooleanExtra("isAnnotated", false);
        id = it.getIntExtra("id", -1);
        span = it.getIntExtra("span",-1);
        idxInSpan = it.getIntExtra("idx",-1);
        bookName = it.getStringExtra("bookName");
        chapName = it.getStringExtra("chapName");
        epubPath =	it.getStringExtra("epubPath");
       
        
        if(isAnnotated){
        	curAnn = annHelper.getAnnById(id);
        	if(curAnn!=null)
    			iNoteView.setText(curAnn.content);     
        	
        	switchMode(ModeShow);
        }else{
        	switchMode(ModeEdit);
        }
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
					
					if(isAnnotated){
						annHelper.deleteAnnById(id);
						isAnnotated=false;
					}
					Intent it=new Intent();
					insertAnnotation();
					setResult(RESULT_OK, it);
					leaveAndFinish();
					
					break;
				}
			}
			
		});
	}
	
	/**
	 * 插入註記
	 * @return 是否寫入
	 */
    private boolean insertAnnotation(){
    	if(iNoteView.getText().toString().length()==0)
    		return false;
    	try{
    		Annotation bm = new Annotation();
    		
			bm.bookName=bookName;
			bm.position1=span;
			bm.position2=idxInSpan;
			bm.chapterName=chapName;
			bm.content=iNoteView.getText().toString();

			bm.epubPath=epubPath;
			annHelper.insertAnn(bm);
			return true;
    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
    	}
    }
    
    /**
	 * 離開設定註記
	 */
	private void leaveAndFinish(){
		annHelper.closeDB();
		finish();
	}
	
	private void switchMode(int aMode){
		iMode = aMode;
		switch(iMode){
		case ModeShow:
			iEditButton.setBackgroundResource(R.drawable.gsi_button05_btn);
			iNoteView.setFocusable(false);
			iNoteView.setClickable(false);
			iNoteView.setFocusableInTouchMode(false);

			break;
		case ModeEdit:
			iEditButton.setBackgroundResource(R.drawable.gsi_button04_btn);
			iNoteView.setFocusable(true);
			iNoteView.setClickable(true);
			iNoteView.setFocusableInTouchMode(true);

			break;
		}
	}
}