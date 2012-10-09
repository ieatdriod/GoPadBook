package tw.com.soyong.utility;

import java.util.ArrayList;

import com.taiwanmobile.myBook_PAD.R;

import tw.com.soyong.ChapterListActivity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ChpInfoAdapter extends BaseAdapter {
	
	Context mContext;
	ArrayList<ChapterInfo> mChpInfos;
	
	LayoutInflater mInflater;
	public ChpInfoAdapter(Context context, ArrayList<ChapterInfo> chpInfos) {
		mContext = context;
		mChpInfos = chpInfos;
		mInflater = LayoutInflater.from(context);
	}
	
	public void removeItem(int pos){
		mChpInfos.remove(pos);
		notifyDataSetChanged();
	}
	
	
	@Override
	public int getCount() {
		return mChpInfos.size();
	}

	@Override
	public Object getItem(int pos) {
		return mChpInfos.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		if ( null == convertView){
			convertView = mInflater.inflate(R.layout.chapter_list_item2, null);
		}
		
		final ChapterInfo info = mChpInfos.get(pos);
		
		//title
		final TextView tvTitle = (TextView)convertView.findViewById(android.R.id.text1);
		
		final Typeface font = Typeface.createFromAsset(mContext.getAssets(),"fonts/syphone.ttf");
		tvTitle.setTypeface(font);
		tvTitle.setText(info.title);
		tvTitle.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				ChapterListActivity activity = (ChapterListActivity) mContext;
				ListView listView = activity.getListView();
				int pos = listView.getPositionForView(v);
				listView.performItemClick(v , pos , v.getId());
			}
			
		});
		
		final ImageButton btn = (ImageButton)convertView.findViewById(R.id.btn);
		if ( info.isValidPs ){
			btn.setVisibility(View.VISIBLE);
		} else {
			btn.setVisibility(View.GONE);
		}
		
		btn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {

				
				ChapterListActivity activity = (ChapterListActivity) mContext;
				ListView listView = activity.getListView();
				int pos = listView.getPositionForView(v);
				listView.performItemClick(v , pos , v.getId());
			}
			
		} );

		return convertView;
	}
	
	public void clickHandler(View v)
	{

	// get the row the clicked button is in
	LinearLayout vwParentRow = (LinearLayout)v.getParent();

	/* get the 2nd child of our ParentRow (remember in java that arrays start with zero,
	so our 2nd child has an index of 1) */

//	Button btnChild = (Button)vwParentRow.getChildAt(1);
//
//	// now set the text of our button
//	btnChild.setText("I've been clicked!");
//
//	// .. and change the colour of our row
	int c = Color.CYAN;

	vwParentRow.setBackgroundColor(c);

	// and redraw our row to reflect our colour change
	vwParentRow.refreshDrawableState();


	}
}
