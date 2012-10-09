package com.gsimedia.gsiebook.lib;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.taiwanmobile.myBook_PAD.R;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class GSiFindResultAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;
	private WeakReference<Context> iContextRef = null;
	public GSiFindResultAdapter(Context c,ArrayList<FindResult> aResult){
		iContextRef = new WeakReference<Context>(c);
		mInflater = LayoutInflater.from(c);
		setFindResult(aResult);
	}

	private ArrayList<FindResult> iResults = null;
	public void setFindResult(ArrayList<FindResult> aResult){
		iResults = aResult;
	}
	private boolean bShowMore = false;
	public void setHasMore(boolean bMore){
		bShowMore = bMore;
	}
	
	public int getCount() {
		if(iResults!=null)
			if(bShowMore)
				return iResults.size()+1;
			else
				return iResults.size();
		else
			return 0;
	}

	public Object getItem(int position) {
		if(iResults!=null)
			if(bShowMore)
				return null;
			else
				return iResults.get(position);
		else
			return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.gsi_ditem_row, null);
			holder = new ViewHolder();
			holder.iText = (TextView) convertView.findViewById(R.id.text);
			holder.iCheck = (CheckedTextView) convertView.findViewById(android.R.id.text1);
			
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}
		/* keep this incase need it in the future
		if(this.iSelMode){
			holder.iCheck.setVisibility(View.VISIBLE);
			holder.iCheck.setChecked(this.mCheckStates.get(position, false));
		}else*/
			holder.iCheck.setVisibility(View.GONE);

			
		holder.iText.setEllipsize(TruncateAt.MARQUEE); 
		if(position>=iResults.size())
			holder.iText.setText(R.string.GSI_FIND_MORE);
		else
			holder.iText.setText(iResults.get(position).getText(iContextRef.get()));
		
				
		return convertView;
	}
	/* class ViewHolder */
	public class ViewHolder
	{
		TextView iText;
		public CheckedTextView iCheck;
	}

}
