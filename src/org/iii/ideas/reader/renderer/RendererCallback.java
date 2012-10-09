package org.iii.ideas.reader.renderer;

/**
 * Renderer回call main activity的interface
 * @author III
 * 
 */
public interface RendererCallback {
	/**
	 * 告知main activity頁面內容已呈現完畢
	 */
	public void onRenderingFinished();
	
	/**
	 * 取得deliver id
	 * @return deliver id
	 */
	public String getDeliverId();
}
