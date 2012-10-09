/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package com.gsimedia.animation;

import java.util.LinkedList;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.FloatMath;
import android.util.Log;

public abstract class BaseAnimation {
	public static enum Mode {
		NoScrolling(false),
		ManualScrolling(false),
		AutoScrollingForward(true),
		AutoScrollingBackward(true);

		public final boolean Auto;

		Mode(boolean auto) {
			Auto = auto;
		}
	};
	
	public static enum Direction {
		leftToRight(true), rightToLeft(true), up(false), down(false);
		/*leftToRight(0), rightToLeft(1), up(2), down(3);*/
		
		public final boolean IsHorizontal;

		Direction(boolean isHorizontal) {
			IsHorizontal = isHorizontal;
		}
		/*Direction(int nHorizontal){
			if(nHorizontal == 0 || nHorizontal == 1){
				IsHorizontal = true;
			}else{
				IsHorizontal = false;
			}
		}*/
	};
	
	public static enum Animation {
		none, curl, slide, shift
	};
	
	public static enum PageIndex {
		previous, current, next;

		public PageIndex getNext() {
			switch (this) {
				case previous:
					return current;
				case current:
					return next;
				default:
					return null;
			}
		}

		public PageIndex getPrevious() {
			switch (this) {
				case next:
					return current;
				case current:
					return previous;
				default:
					return null;
			}
		}
	};
	
	private static final String TAG = "BaseAnimation";
	private Mode myMode = Mode.NoScrolling;
	
	//private final BitmapManager myBitmapManager;
	protected int myStartX;
	protected int myStartY;
	protected int myEndX;
	protected int myEndY;
	protected Direction myDirection;
	protected float mySpeed;

	protected int myWidth;
	protected int myHeight;
	
	protected Bitmap curPageBm;
	protected Bitmap nextPageBm;
	
	protected boolean bIsVertical;
	
	protected BaseAnimation(Bitmap curPageBitmap, Bitmap nextPageBitmap) {
		curPageBm = curPageBitmap;
		nextPageBm = nextPageBitmap;
	}
	
	public void setBitmap(Bitmap curPageBitmap, Bitmap nextPageBitmap){
		curPageBm = curPageBitmap;
		nextPageBm = nextPageBitmap;
	}
	
	public Mode getMode() {
		return myMode;
	}

	public Direction getDirection(){
		return myDirection;
	}
	
	public void terminate() {
		myMode = Mode.NoScrolling;
		mySpeed = 0;
		myDrawInfos.clear();
	}

	void startManualScrolling(int x, int y, Direction direction, int w, int h) {
		myMode = Mode.ManualScrolling;
		setup(x, y, direction, w, h);
	}

	void scrollTo(int x, int y) {
		if (myMode == Mode.ManualScrolling) {
			myEndX = x;
			myEndY = y;
		}
	}

	final public void startAutoScrolling(boolean isVertical, boolean forward, float startSpeed, Direction direction, int w, int h, Integer x, Integer y, int speed) {
		bIsVertical = isVertical;
		//初始速度+3 or -3
		Log.d(TAG, "[startAutoScrolling]forward = " + forward + ", touch point = (" + x + "," + y + "), speed=" + speed);
		if (myDrawInfos.size() <= 1) {
			//auto scrolling時
			startSpeed *= 3;
		} else {
			//似乎用在manual scrolling的release時
			int duration = 0;
			for (DrawInfo info : myDrawInfos) {
				duration += info.Duration;
			}
			duration /= myDrawInfos.size();
			final long time = System.currentTimeMillis();
			myDrawInfos.add(new DrawInfo(myEndX, myEndY, time, time + duration));
			float velocity = 0;
			for (int i = 1; i < myDrawInfos.size(); ++i) {
				final DrawInfo info0 = myDrawInfos.get(i - 1);
				final DrawInfo info1 = myDrawInfos.get(i);
				final float dX = info0.X - info1.X;
				final float dY = info0.Y - info1.Y;
				velocity += FloatMath.sqrt(dX * dX + dY * dY) / Math.max(1, info1.Start - info0.Start);
			}
			velocity /= myDrawInfos.size() - 1;
			velocity *= duration;
			velocity = Math.min(100, Math.max(15, velocity));
			startSpeed = startSpeed > 0 ? velocity : -velocity;
		}
		myDrawInfos.clear();
		startAutoScrollingInternal(forward, startSpeed, direction, w, h, x, y, speed);
	}

	protected void startAutoScrollingInternal(boolean forward, float startSpeed, Direction direction, int w, int h, Integer x, Integer y, int speed) {
		if (!inProgress()) {
			if (x == null || y == null) {
				if (direction.IsHorizontal) {
					x = speed < 0 ? w : 0;
					y = 0;
				} else {
					x = 0;
					y = speed < 0 ? h : 0;
				}
			}
			setup(x, y, direction, w, h);
		}

		myMode = forward
			? Mode.AutoScrollingForward
			: Mode.AutoScrollingBackward;
		mySpeed = startSpeed;
	}

	public boolean inProgress() {
		return myMode != Mode.NoScrolling;
	}

	protected int getScrollingShift() {
		return myDirection.IsHorizontal ? myEndX - myStartX : myEndY - myStartY;
	}

	private void setup(int x, int y, Direction direction, int width, int height) {
		//20110518 benson modified for backing animation of previous page.
		/*if(direction == Direction.rightToLeft)
		{
			myStartX = width;
			//20110519 benson modified for switching animation of previous page.(up and down)
			//myStartY = y > height/2 ? height : 0;
			myStartY = y > height/2 ? 0 : height;
			myEndX = -width;
			myEndY = height/2;
		}else{
			myStartX = x;
			myStartY = y;
			myEndX = x;
			myEndY = y;
		}*/
		
		if(bIsVertical){
			if(direction == Direction.leftToRight){
				myStartX = 0;
				myStartY = y > height/2 ? 0 : height;
				myEndX = 2 * width;
				myEndY = height/2;
			}else{
				myStartX = x;
				myStartY = y;
				myEndX = x;
				myEndY = y;
			}
		}else{
			if(direction == Direction.rightToLeft){
				myStartX = width;
				//20110519 benson modified for switching animation of previous page.(up and down)
				//myStartY = y > height/2 ? height : 0;
				myStartY = y > height/2 ? 0 : height;
				myEndX = -width;
				myEndY = height/2;
			}else{
				myStartX = x;
				myStartY = y;
				myEndX = x;
				myEndY = y;
			}
		}
		//original fbreader's code
		/*myStartX = x;
		myStartY = y;
		myEndX = x;
		myEndY = y;*/
		myDirection = direction;
		myWidth = width;
		myHeight = height;
	}

	public abstract void doStep();

	int getScrolledPercent() {
		final int full = myDirection.IsHorizontal ? myWidth : myHeight;
		final int shift = Math.abs(getScrollingShift());
		return 100 * shift / full;
	}

	static class DrawInfo {
		final int X, Y;
		final long Start;
		final int Duration;

		DrawInfo(int x, int y, long start, long finish) {
			X = x;
			Y = y;
			Start = start;
			Duration = (int)(finish - start);
		}
	}
	final private List<DrawInfo> myDrawInfos = new LinkedList<DrawInfo>();

	public final void draw(Canvas canvas) {
		final long start = System.currentTimeMillis();
		drawInternal(canvas);
		myDrawInfos.add(new DrawInfo(myEndX, myEndY, start, System.currentTimeMillis()));
		if (myDrawInfos.size() > 3) {
			myDrawInfos.remove(0);
		}
	}

	protected abstract void drawInternal(Canvas canvas);

	public abstract PageIndex getPageToScrollTo();

	protected Bitmap getBitmapFrom() {
		return curPageBm;
	}

	protected Bitmap getBitmapTo() {
		return nextPageBm;
	}
}
