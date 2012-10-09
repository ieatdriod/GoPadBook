package com.gsimedia.gsiebook.lib;

public interface PagesObserver{
	public static final int MODE_MIDDLE = 0;
	public static final int MODE_LEFT = 1;
	public static final int MODE_RIGHT = 2;
	public static final int MODE_BOOKMARK = 3;
	public static final int MODE_NOTE = 4;
	public void toggleControl(int aPosition);
	public boolean hasBookmark();
	public boolean hasAnnotation();
	public boolean isControlPanelOn();
	public void onPageChange(int aNewPage, int totalpage);
	public void disableFirstPage();
	public void onRenderingException(RenderingException reason);
	public void onRenderingProgressStart();
	public void onRenderingProgressEnd();
	public void onMarkerAdded(MarkResult aMarkResult);
	public void onMarkerDeleted(MarkResult aMarkResult);
	public void onMarkerUpToLimit();
}
