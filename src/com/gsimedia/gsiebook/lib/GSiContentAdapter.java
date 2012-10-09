package com.gsimedia.gsiebook.lib;

import java.util.ArrayList;

import com.taiwanmobile.myBook_PAD.R;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class GSiContentAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<String> iItems = null;
	public GSiContentAdapter(Context c, ArrayList<Outline> aOutlines){
		mInflater = LayoutInflater.from(c);
		mCheckStates = new SparseBooleanArray();
		this.setOutlines(aOutlines);
	}
	public void setOutlines(ArrayList<Outline> aOutlines){
		if(iItems == null)
			iItems = new ArrayList<String>();
		else
			iItems.clear();
		if(aOutlines == null)
			return;
		for(int i=0;i<aOutlines.size();i++)
			iItems.add(aOutlines.get(i).getTitle());
	}
	public void setBookmarks(ArrayList<GSiBookmark>aBookmarks){
		if(iItems == null)
			iItems = new ArrayList<String>();
		else
			iItems.clear();
		if(aBookmarks== null)
			return;
		for(int i=0;i<aBookmarks.size();i++)
			iItems.add(aBookmarks.get(i).iTitle);
	}
	public void setAnnotations(ArrayList<GSiAnnotation>aAnnotations){
		if(iItems == null)
			iItems = new ArrayList<String>();
		else
			iItems.clear();
		if(aAnnotations == null)
			return;
		for(int i=0;i<aAnnotations.size();i++)
			iItems.add(aAnnotations.get(i).iAnnotation);
	}
	
	public void setMarkers(ArrayList<MarkResult>aMarkResults){
		if(iItems == null)
			iItems = new ArrayList<String>();
		else
			iItems.clear();
		if(aMarkResults == null)
			return;
		for(int i=0;i<aMarkResults.size();i++)
			iItems.add(aMarkResults.get(i).getText(null).trim());
	}
	private boolean iSelMode = false;
	public void setSelectionMode(boolean aSelMode){
		iSelMode = aSelMode;
	}
	public boolean getSelectionMode(){
		return iSelMode;
	}
	private SparseBooleanArray mCheckStates;
	public SparseBooleanArray getCheckedItems(){
		return this.mCheckStates;
	}	
	public void setCheckedItems(SparseBooleanArray aItems){
		this.mCheckStates = aItems;
	}
	public int getCount() {
		return iItems.size();
	}

	public Object getItem(int position) {
		return iItems.get(position);
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
		if(this.iSelMode){
			holder.iCheck.setVisibility(View.VISIBLE);
			holder.iCheck.setChecked(this.mCheckStates.get(position, false));
		}else
			holder.iCheck.setVisibility(View.GONE);

		holder.iText.setText(iItems.get(position));
		
				
		return convertView;
	}
	/* class ViewHolder */
	public class ViewHolder
	{
		TextView iText;
		public CheckedTextView iCheck;
	}
	
	
	
}
