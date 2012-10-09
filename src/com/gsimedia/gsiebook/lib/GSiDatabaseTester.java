package com.gsimedia.gsiebook.lib;

import com.taiwanmobile.myBook_PAD.R;
import com.gsimedia.gsiebook.common.Config;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class GSiDatabaseTester extends Activity {

	GSiDatabaseAdapter iDatabase = null;
	private static final String EBookID = "book1";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.databasetester);
		
		iDatabase = new GSiDatabaseAdapter(getApplicationContext());
		
		findViews();
	}
	@Override
	protected void onPause() {
		super.onPause();
		iDatabase.close();
	}
	@Override
	protected void onResume() {
		super.onResume();
		iDatabase.open();
	}
	private EditText iBookView = null;
	private EditText iPageView = null;
	private EditText iTitleView = null;
	private Button iAddButton = null;
	private Button iDelButton = null;
	private Button iDisplay = null;
	private Button iDelAll = null;
	private Button iSwitch = null;
	private Button iGetLast = null;
	private Button iSetLast = null;
	private Button iDelLast = null;
	private void findViews(){
		iBookView = (EditText) findViewById(R.id.book);
		iPageView = (EditText) findViewById(R.id.page);
		iTitleView = (EditText) findViewById(R.id.title);
		iAddButton = (Button) findViewById(R.id.add);
		iAddButton.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				if(iPageView.getText()!=null){
					String aBookID = iBookView.getText().toString();
					String aTitle = iTitleView.getText().toString();
					Integer aPage = Integer.decode(iPageView.getText().toString());
					if(iMode == ModeBookmark)
						iDatabase.addBookmark(aBookID, new GSiBookmark(aPage, aTitle));
					else 
						iDatabase.addAnnotation(aBookID, new GSiAnnotation(aPage, aTitle));
					iTitleView.setText("");
					iPageView.setText("");
				}
			}
			
		});
		iDelButton = (Button) findViewById(R.id.delete);
		iDelButton.setOnClickListener(new OnClickListener(){
			
			public void onClick(View v) {
				if(iPageView.getText()!=null){
					String aBookID = iBookView.getText().toString();
					Integer aPage = Integer.decode(iPageView.getText().toString());
					if(iMode == ModeBookmark)
						iDatabase.removeBookmark(aBookID, new GSiBookmark(aPage, null));
					else
						iDatabase.removeAnnotation(aBookID, new GSiAnnotation(aPage, null));
					iTitleView.setText("");
					iPageView.setText("");
				}
			}
			
		});
		iDisplay = (Button) findViewById(R.id.display);
		iDisplay.setOnClickListener(new OnClickListener(){
			
			public void onClick(View v) {
				String aBookID = iBookView.getText().toString();
				iDatabase.printDatabase(aBookID,GSiDatabaseTester.this,"");
			}
			
		});
		iDelAll = (Button) findViewById(R.id.delall);
		iDelAll.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				String aBookID = iBookView.getText().toString();
				if(iMode == ModeBookmark)
					iDatabase.deleteBookmarks(aBookID);
				else 
					iDatabase.deleteAnnotations(aBookID);
			}
			
		});
		iSwitch = (Button) findViewById(R.id.switcher);
		switch(iMode){
		case ModeBookmark:
			iSwitch.setText("bookmark");
			break;
		case ModeAnnotation:
			iSwitch.setText("annotation");
			break;
		}
		iSwitch.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				switch(iMode){
				case ModeBookmark:
					iMode = ModeAnnotation;
					iSwitch.setText("annotation");
					break;
				case ModeAnnotation:
					iMode = ModeBookmark;
					iSwitch.setText("bookmark");
					break;
				}
			}
			
		});
		iGetLast = (Button) findViewById(R.id.getlast);
		iGetLast.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				String aBookID = iBookView.getText().toString();
				int aLastPage = iDatabase.getLastPage(aBookID,false,GSiDatabaseTester.this,"deliver","btoken").getLastPage();
				Log.d(Config.LOGTAG,"LastPage of "+aBookID+"="+aLastPage);
			}
			
		});
		iSetLast = (Button) findViewById(R.id.setlast);
		iSetLast.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				String aBookID = iBookView.getText().toString();
				Integer aPage = Integer.decode(iPageView.getText().toString());
				iDatabase.setLastPage(aBookID, aPage,false, GSiDatabaseTester.this,"deliver","btoken");
				Log.d(Config.LOGTAG,"set LastPage of "+aBookID+"="+aPage);
			}
			
		});
		iDelLast = (Button) findViewById(R.id.dellast);
		iDelLast.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				String aBookID = iBookView.getText().toString();
				GSiDatabaseAdapter.deleteLastPageOfBook(getApplicationContext(), aBookID);
				Log.d(Config.LOGTAG,"delete LastPage of "+aBookID);
			}
			
		});
	}
	private static final int ModeBookmark = 0;
	private static final int ModeAnnotation = 1;
	private static int iMode = ModeBookmark;
	
}
