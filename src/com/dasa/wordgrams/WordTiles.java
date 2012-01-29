package com.dasa.wordgrams;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class WordTiles {
	
	String[] mLetters;
	
	public WordTiles(String word) {
		mLetters = word.split("");
	}
	
	public void onDraw(Canvas canvas, Paint paint, Bitmap bmTileDefault) {
		for (String letter: mLetters) {
			float textOffsetX = paint.measureText(letter)/2f;
			/*
			canvas.drawBitmap(mBmTileDefault, mPosX+offsetX, mPosY, mPaint);
			
			canvas.drawText(letter, mPosX+offsetX+textOffsetX, mPosY+mTextOffsetY, mPaint);
			
			offsetX += mTileSize;
			*/
		}
	}
}
