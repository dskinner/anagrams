package com.dasa.wordgrams;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.text.TextPaint;

public class CanvasContext {
	
	public Paint paint;
	public TextPaint textPaint;
	
	public Bitmap bmTile;
	public Bitmap bmStarOn;
	
	public float screenWidth;
	public float screenHeight;
	public float screenPad;
	
	public int textHeight;
	public int textHeightHalf;
	
	public float tileSize;
	public float tileSizeHalf;
	
	public long delta;
	
	public void recycleBitmaps() {
		if (bmTile != null) bmTile.recycle();
		if (bmStarOn != null) bmStarOn.recycle();
	}
}
