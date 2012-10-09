package tw.com.soyong;


import com.taiwanmobile.myBook_PAD.R;

import tw.com.soyong.mebook.MebookHelper;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * 書本資料畫面
 * @author Victor
 *
 */
public class MebookInfoActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mebook_info);
		
		// Listen for button clicks 
        final ImageButton imgBtn_Back = (ImageButton) findViewById(R.id.mi_ImageButton_Back);

        imgBtn_Back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});  		
		
        final ImageButton imgBtn_Url = (ImageButton) findViewById(R.id.mi_ImageButton01);//Id mi_ImageButton01
        imgBtn_Url.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		 		Intent it = new Intent();
				//it.setClass(MebookInfoActivity.this, MeWebView.class);	
				//Bundle bundle = new Bundle();
				String urlString = String.format(getResources().getString(R.string.lastpage_url3), MebookHelper.mContentID);
				//bundle.putString("url", urlString);
				//it.putExtras(bundle);
				it.setAction(Intent.ACTION_VIEW);
				it.setData(Uri.parse(urlString));
				startActivity(it);
			}
		}); 
        
        // book icon
        final ImageView bookIcon = (ImageView)findViewById(R.id.mi_ImageView01);
        Bitmap bmp = BitmapFactory.decodeFile( MebookHelper.mCoverPath );
        if ( null != bmp ){
        	bookIcon.setImageBitmap(bmp);
        }
        //bookIcon
        
//        TWMMetaData xml = MebookHelper.mMeta;
//        String title = xml.getAlbumTitle();
//        String publisher = getResources().getString(R.string.bookinfo_publisher)+ xml.getAlbumPublisher();
//        String auther = getResources().getString(R.string.bookinfo_author)+xml.getAlbumAuthor();
        
        String title = MebookHelper.mBookTitle;
        if ( null == title){
        	title = "";
        }
        
        String publisher ; //= getResources().getString(R.string.bookinfo_publisher)+ MebookHelper.mBookPublisher;
        if ( null == MebookHelper.mBookPublisher ){
        	publisher = getResources().getString(R.string.bookinfo_publisher);
        }else {
        	publisher = getResources().getString(R.string.bookinfo_publisher)+ MebookHelper.mBookPublisher;
        }
        
        String auther ; //= getResources().getString(R.string.bookinfo_author)+MebookHelper.mBookAuthors;   
        if ( null == MebookHelper.mBookAuthors ){
        	auther = getResources().getString(R.string.bookinfo_author);
        }else {
        	auther = getResources().getString(R.string.bookinfo_author)+MebookHelper.mBookAuthors; 
        }
        
        String category = MebookHelper.mBookCategory;
        if ( null == category ){
        	category = "";
        }else{
        	TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter('|');
        	splitter.setString(category);
        
        	StringBuilder sb = new StringBuilder();
        	boolean isFirst = true ;
        	for ( String s:splitter){
        		if (false == isFirst ){
        			sb.append(",");
        		}
        		sb.append(s);
        		isFirst = false ;
        	}
        	category = sb.toString();
        	
        	
        	//String[] cells = category.split('|',0);
//        	if (cells.length==1){
//        		category = cells[0];
//        	}else{
//        		StringBuilder sb = new StringBuilder();
//        		final int max = cells.length-1;
//        		for ( int i = 0 ; i < max ; i++){
//        			sb.append(cells[i]+",");
//        		}
//        		sb.append(cells[max]);
//        		category = sb.toString();
//        	}
        }
        

        EditText tv = (EditText)findViewById(R.id.mi_area_title);
        tv.setText(title);
        
        tv = (EditText)findViewById(R.id.mi_area_publisher);
        tv.setText(publisher);
        
        tv = (EditText)findViewById(R.id.mi_area_auther);
        tv.setText(auther);   
        
        tv = (EditText)findViewById(R.id.mi_area_category);
        tv.setText(category);
	}
	
}
