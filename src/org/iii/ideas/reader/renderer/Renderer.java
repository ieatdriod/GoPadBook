package org.iii.ideas.reader.renderer;

import java.util.ArrayList;

import org.iii.ideas.reader.PartialUnzipper;
import org.iii.ideas.reader.underline.Underline;

/**
 * renderer interface，未使用
 * @author III
 *
 */
public interface Renderer {
	public void loadChapterByTextIdx(String url,int idx,PartialUnzipper uz,String chapname);
	public void loadChapterBySpanAndIdx(String url,int startSpan,int startIdx,PartialUnzipper uz,String chapname);
	public void loadPageByTextIdx(int tid);
	public void render();
	public boolean canPageUp();
	public boolean canPageDown();
	public void pageUp();
	public void pageDown();
	public void changeFontSize(int fontSize);
	public int getCurPageStartSpan();	
	public int getCurPageStartIdxInSpan();
	public int getCurPageEndSpan();
	public int getCurPageEndIdxInSpan();
	public void reload();
	public void insertBookmark(String title, String chapName, String epubPath);
	public boolean isCurPageBookmarked();
	public boolean isCurPageAnnotated();
	public boolean isCurPageUnderlined();
	public int getCurPageAnnotation();
	public void deleteCurPageBookmark(String title, String chapName, String epubPath);
	public void deleteCurPageAnnotation(String title, String chapName, String epubPath);
	public void insertAnn(String title, String chapName, String epubPath,String input,int span,int idx);
	public void deleteAnnById(int id);
	public void closeDB();
	public int getPercentageInChapter();
	public String getChapTitle();
	public void insertUnderline(String title, String chapName, String epubPath,int span1,int idx1,int span2,int idx2);
	public void loadChapterByPercentage(String url, int per, PartialUnzipper uz, String chapName_);
	public void setTurningMethod(int type);
	public ArrayList<Underline> getUnderlineBySpanAndIdx(int span,int idx);
	public void deleteUnderline(ArrayList<Underline> underlines);
}
