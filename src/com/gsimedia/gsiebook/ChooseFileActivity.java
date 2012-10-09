package com.gsimedia.gsiebook;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import com.taiwanmobile.myBook_PAD.R;
import com.gsimedia.gsiebook.common.Config;
import com.gsimedia.gsiebook.lib.GSiDatabaseAdapter;
import com.gsimedia.gsiebook.lib.GSiDatabaseTester;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Minimalistic file browser.
 */
public class ChooseFileActivity extends Activity implements OnItemClickListener {
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if(!this.currentPath.equals(ERootDIR)){
				File aFile = new File(this.currentPath);
				this.currentPath = aFile.getParent();
				this.update();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Logging tag.
	 */
	private final static String TAG = Config.LOGTAG;
	
	private static final String ERootDIR = Environment.getExternalStorageDirectory().getAbsolutePath();
	private String currentPath = ERootDIR;
	
	private TextView pathTextView = null;
	private ListView filesListView = null;
	private static FileFilter fileFilter = null;
	private ArrayAdapter<String> fileListAdapter = null;
	
	private MenuItem aboutMenuItem = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	fileFilter = new FileFilter() {
    		public boolean accept(File f) {
    			if(Config.EShowHiddenFile){
    				return (f.isDirectory() 
    						|| f.getName().toLowerCase().endsWith(".pdf") 
    						|| f.getName().toLowerCase().endsWith(Config.FILE_EXT));
    			}else{
    				if(f.isHidden())
    					return false;
    				else 
    					return (f.isDirectory() 
    							|| f.getName().toLowerCase().endsWith(".pdf")
    							|| f.getName().toLowerCase().endsWith(Config.FILE_EXT));
    			}
    		}
    	};
    	
    	this.setContentView(R.layout.filechooser);
    	
    	this.pathTextView = (TextView) this.findViewById(R.id.path);
    	this.filesListView = (ListView) this.findViewById(R.id.files);
    	this.fileListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    	this.filesListView.setAdapter(this.fileListAdapter);
    	this.filesListView.setOnItemClickListener(this);
    	this.update();
    	
    	/*open single file directly*/
    	/*String aFile = "Stamp.pdf";
    	File clickedFile = null;
		try {
			clickedFile = new File(this.currentPath, aFile).getCanonicalFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Uri aUri = Uri.parse(Config.FILESCHEME+"://"+clickedFile.getAbsolutePath());
    	Intent it = null;
		it= new Intent(this,RendererActivity.class);
	 	it.setData(aUri);	
	 	it.putExtra(Config.KEY_p12,Environment.getExternalStorageDirectory().getPath());
	 	it.putExtra(Config.KEY_isSample, "0");
	 	it.putExtra(Config.KEY_coverPath, Environment.getExternalStorageDirectory()+"/cover.png");
	 	it.putExtra(Config.KEY_syncLastPage, true); 
	 	it.putExtra(Config.KEY_content_id, clickedFile.getPath());
	 	it.putExtra(Config.KEY_book_title, clickedFile.getName());
	 	it.putExtra(Config.KEY_book_authors, "test author");
	 	it.putExtra(Config.KEY_book_publisher, "test publisher");
	 	it.putExtra(Config.KEY_book_category, "test category");
	 	it.putExtra("book_vertical", false);
	 	startActivity(it);
    	*/
    	
    }
    private static ArrayList<String> ListFiles(String aPath){
    	
    	ArrayList<String> aAllFiles = new ArrayList<String>();
    	File aFiles[] = new File(aPath).listFiles(fileFilter);
    	for(int i=0;i<aFiles.length;i++){
    		if(aFiles[i].isDirectory()){
    			aAllFiles.addAll(ListFiles(aFiles[i].getAbsolutePath()));
    		}else{
    			aAllFiles.add(aFiles[i].getAbsolutePath());
    		}
    	}
    	return aAllFiles;
    }
    class UpdateFolderTask extends AsyncTask<String,Integer,ArrayList<String>>{

		@Override
		protected ArrayList<String> doInBackground(String... params) {
			String aPath = params[0];
			ArrayList<String> aAllFiles = new ArrayList<String>();
	    	File aFiles[] = new File(aPath).listFiles(fileFilter);
	    	for(int i=0;i<aFiles.length;i++){
	    		publishProgress(i,aFiles.length-1);
	    		if(aFiles[i].isDirectory()){
	    			aAllFiles.addAll(ListFiles(aFiles[i].getAbsolutePath()));
	    		}else{
	    			aAllFiles.add(aFiles[i].getAbsolutePath());
	    		}
	    	}
			return aAllFiles;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			ArrayList<String> aFiles = result;
			for(int i=0;i<aFiles.size();i++){
				fileListAdapter.add(aFiles.get(i));
			}
			fileListAdapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			pathTextView.setText("Loading..."+values[0]+"/"+values[1]);
			super.onProgressUpdate(values);
		}
    	
    }
    /**
     * Reset list view and list adapter to reflect change to currentPath.
     */
    private void update() {
    	this.pathTextView.setText(this.currentPath);
    	this.fileListAdapter.clear();
    	/* 101229 water:list directory */
    	/*if(!currentPath.equals(ERootDIR))
    		this.fileListAdapter.add("..");
    	
    	File files[] = new File(this.currentPath).listFiles(this.fileFilter);
    	if (files != null) {
	    	try {
	    		Arrays.sort(files, new Comparator<File>() {
	    			public int compare(File f1, File f2) {
	    				if (f1 == null) throw new RuntimeException("f1 is null inside sort");
	    				if (f2 == null) throw new RuntimeException("f2 is null inside sort");
	    				try {
	    					return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
	    				} catch (NullPointerException e) {
	    					throw new RuntimeException("failed to compare " + f1 + " and " + f2, e);
	    				}
	    			}
	    		});
	    	} catch (NullPointerException e) {
	    		throw new RuntimeException("failed to sort file list " + files + " for path " + this.currentPath, e);
	    	}
	    	
	    	for(int i = 0; i < files.length; ++i) 
	    		this.fileListAdapter.add(files[i].getName());
    	}*/
    	
    	/* 101229 water:list all pdf files*/
    	UpdateFolderTask aTask = new UpdateFolderTask();
    	aTask.execute(ERootDIR);
    	
    	this.filesListView.setSelection(0);
    }
    
    private String aID = null;
    @SuppressWarnings("rawtypes")
	public void onItemClick(AdapterView parent, View v, int position, long id){
    	String filename = (String) this.filesListView.getItemAtPosition(position);
    	File clickedFile = null;
    	//clickedFile = new File(this.currentPath, filename).getCanonicalFile();
		clickedFile = new File(filename);
		
    	if(clickedFile.getName().equals("..")){
    		File aFile = new File(this.currentPath);
    		this.currentPath = aFile.getParent();
    		this.update();
    	}else if (clickedFile.isDirectory()) {
    		Log.d(TAG, "change dir to " + clickedFile);
    		this.currentPath = clickedFile.getAbsolutePath();
    		this.update();
    	} else {
    		Log.i(TAG, "post intent to open file " + clickedFile);
    		Uri aUri = Uri.parse(Config.FILESCHEME+"://"+clickedFile.getAbsolutePath());
		String filePath = aUri.getPath();
		aID = new File(filePath).getName();
		aID = aID.replace(Config.FILE_EXT, "");
		aID= DatabaseUtils.sqlEscapeString(aID);
    		Intent it = null;
    		it= new Intent(this,RendererActivity.class);
    	 	it.setData(aUri);	
    	 	it.putExtra(Config.KEY_p12,Environment.getExternalStorageDirectory().getPath());
    	 	it.putExtra(Config.KEY_isSample, "0");
    	 	it.putExtra(Config.KEY_coverPath, Environment.getExternalStorageDirectory()+"/cover.png");
    	 	it.putExtra(Config.KEY_syncLastPage, true); 
    	 	it.putExtra(Config.KEY_content_id, clickedFile.getPath());
    	 	it.putExtra(Config.KEY_book_title, clickedFile.getName());
    	 	it.putExtra(Config.KEY_book_authors, "test author");
    	 	it.putExtra(Config.KEY_book_publisher, "test publisher");
    	 	it.putExtra(Config.KEY_book_category, "test category");
    	 	it.putExtra(Config.KEY_book_vertical, false);
    	 	startActivity(it);

    		/*
    		Intent intent = new Intent();
    		intent.setDataAndType(Uri.fromFile(clickedFile), "application/pdf");
    		intent.setClass(this, RendererActivity.class);
    		intent.setAction("android.intent.action.VIEW");
    		this.startActivity(intent);
    		*/
    	}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
    	if (menuItem == this.aboutMenuItem) {
    		/*
			Intent intent = new Intent();
			intent.setClass(this, AboutPDFViewActivity.class);
			this.startActivity(intent);
			*/
    		/*
			Intent intent = new Intent();
			intent.setClass(this, GSiDatabaseTester.class);
			this.startActivity(intent);
			*/
//    		String aID = "ERT01241000411202";
//    		String aID = "harry_potter_and_the_sorcerer's_stone.pdf";
    		/* 101229 water:for backup interface testing */
    		/*Context context = getApplicationContext();
    		String aXml = GSiDatabaseAdapter.getBookmarkXml(context, aID);
    		Log.d(Config.LOGTAG,aID+":"+aXml);
    		GSiDatabaseAdapter.setBookmark(context, aID, aXml);
    		aXml = GSiDatabaseAdapter.getAnnotationXml(context, aID);
    		Log.d(Config.LOGTAG,aID+":"+aXml);
    		GSiDatabaseAdapter.setAnnotation(context, aID, aXml);
//    		GSiDatabaseAdapter.deleteAllLastPage(context);
    		GSiDatabaseAdapter.deleteLastPageOfBook(context,aID);
  		*/
    		return true;
    	}
    	return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	this.aboutMenuItem = menu.add("About");
    	return true;
    }
}