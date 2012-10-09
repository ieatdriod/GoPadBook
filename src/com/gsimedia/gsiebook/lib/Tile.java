package com.gsimedia.gsiebook.lib;


/**
 * Tile definition.
 * Can be used as a key in maps (hashable, comparable).
 */
public class Tile {

	/**
	 * X position of tile in pixels.
	 */
	private int x;
	
	/**
	 * Y position of tile in pixels.
	 */
	private int y;
	
	private int zoom;
	private int page;
	private int rotation;
	private int w;
	private int h;
	private boolean fittile;
	
	int _hashCode;
	
	public Tile(int page, int zoom, int x, int y, int rotation, int width, int height, boolean isFit) {
		this.page = page;
		this.zoom = zoom;
		this.x = x;
		this.y = y;
		this.rotation = rotation;
		this.w = width;
		this.h = height;
		fittile = isFit;
	}
	
	public String toString() {
		return "Tile(" +
			this.page + ", " +
			this.zoom + ", " +
			this.x + ", " +
			this.y + ", " +
			this.rotation + ", "+ 
			this.w  + ", "+ 
			this.h  + ", "+
			this.fittile + ")";
	}
	
	public int hashCode() {
		return this._hashCode;
	}
	
	public boolean equals(Object o) {
		if (! (o instanceof Tile)) return false;
		Tile t = (Tile) o;
		return (
					this._hashCode == t._hashCode
					&& this.page == t.page
					&& this.zoom == t.zoom
					&& this.x == t.x
					&& this.y == t.y
					&& this.rotation == t.rotation
					&& this.w == t.w
					&& this.h == t.h
				);
	}
	
	public int getPage() {
		return this.page;
	}
	
	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getZoom() {
		return this.zoom;
	}

	public int getRotation() {
		return this.rotation;
	}
	public int getWidth(){
		return this.w;
	}
	public int getHeight(){
		return this.h;
	}
	public boolean isFit(){
		return this.fittile;
	}
}
