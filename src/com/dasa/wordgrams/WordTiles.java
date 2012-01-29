package com.dasa.wordgrams;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.MotionEvent;

public class WordTiles {
	
	private String[] mAnswers = new String[] {"OPERANTS", "PRONATES", "TROPANES", "PATERSON"};
	private String[] mLetters = new String[] {"O", "P", "E", "R", "A", "N", "T", "S"};
	
	private float[][] mLetterPosOffset; // [index][x, y, duration]
	
	private float mViewPosX;
	private float mViewPosY;
	
	private float mTileSize;
	
	private boolean mActionDown;
	private int mSelectIndex;
	
	private float mPosX = 0;
	private float mPosY = 50;
	private float mPrevX;
	private float mPrevY;
	private float mDragDx;
	private float mDragDy;
	
	public WordTiles(String word, String[] answers, float tileSize, float viewPosX, float viewPosY) {
		mTileSize = tileSize;
		mViewPosX = viewPosX;
		mViewPosY = viewPosY;
		
		mLetterPosOffset = new float[mLetters.length][3];
	}
	
	public void onDraw(long delta, Canvas canvas, Paint paint, TextPaint textPaint, Bitmap bmTile) {
		paint.setColor(Color.WHITE);
		
		for (int i=0; i<mLetters.length; ++i) {
			
			String letter = mLetters[i];
			
			float offsetX = i*mTileSize;
			
			float bmX = mViewPosX+offsetX;
			float bmY = mViewPosY;
			
			Rect bounds = new Rect();
			textPaint.getTextBounds(letter, 0, 1, bounds);
			int textHeight = bounds.bottom-bounds.top;
			
			float textX = bmX+(mTileSize/2);
			float textY = bmY+(mTileSize/2)+(textHeight/2);
			
			if (mActionDown && (i == mSelectIndex)) {
				bmX += mDragDx;
				bmY += mDragDy;
				textX += mDragDx;
				textY += mDragDy;
			}
			
			// animate tile movement
			float[] posOffset = mLetterPosOffset[i];
			posOffset[0] -= posOffset[0]/delta*2.5f;
			posOffset[1] -= posOffset[1]/delta*2.5f;
			
			if (posOffset[0] > -1 && posOffset[0] < 1) posOffset[0] = 0;
			if (posOffset[1] > -1 && posOffset[1] < 1) posOffset[1] = 0;
			
			bmX += posOffset[0];
			bmY += posOffset[1];
			textX += posOffset[0];
			textY += posOffset[1];
			
			canvas.drawBitmap(bmTile, bmX, bmY, paint);
			canvas.drawText(letter, textX, textY, textPaint);
		}
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		mPosX = event.getX();
		mPosY = event.getY();
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				int index = gridHitTest(mPosX, mPosY);
				if (index == -1) break;
				
				mSelectIndex = index;
				mDragDx = 0;
				mDragDy = 0;
				mPrevX = mPosX;
				mPrevY = mPosY;
				mActionDown = true;
				break;
			case MotionEvent.ACTION_MOVE:
				if (!mActionDown) break;
				
				int swapIndex = rowHitTest(mPosX, mPosY);
				
				if (swapIndex == mSelectIndex
						|| swapIndex >= mLetters.length
						|| swapIndex < 0) break;
				
				String tmp = mLetters[mSelectIndex];
				mLetters[mSelectIndex] = mLetters[swapIndex];
				mLetters[swapIndex] = tmp;
				
				// refers to mSelectIndex since thats where the non-interacted
				// tile will be at
				if (swapIndex > mSelectIndex) mLetterPosOffset[mSelectIndex][0] += mTileSize;
				if (swapIndex < mSelectIndex) mLetterPosOffset[mSelectIndex][0] -= mTileSize;
				
				// prevents tile being dragged from shifting draw position
				// due to new index
				if (swapIndex < mSelectIndex) mDragDx += mTileSize;
				else mDragDx -= mTileSize;
				
				//
				mSelectIndex = swapIndex;
				
				break;
			case MotionEvent.ACTION_UP:
				if (!mActionDown) break;
				mLetterPosOffset[mSelectIndex][0] = mDragDx;
				mLetterPosOffset[mSelectIndex][1] = mDragDy;
				mActionDown = false;
				checkAnswer();
				break;
		}
		
		if (mActionDown) {
			mDragDx += (mPosX-mPrevX);
			mDragDy += (mPosY-mPrevY);
		}
		
		mPrevX = mPosX;
		mPrevY = mPosY;
		return true;
	}
	
	public int gridHitTest(float x, float y) {
		if (y < mViewPosY+mTileSize && y > mViewPosY) return (int) ((x)/mTileSize);
		
		return -1;
	}
	
	public int rowHitTest(float x, float y) {
		return (int) ((x)/mTileSize);
	}
	
	private void checkAnswer() {
		String curWord = "";
		for (String letter : mLetters) curWord += letter;
		
		for (String answer : mAnswers) {
			if (curWord.equals(answer)) {
				App.toast("!!! "+curWord+" !!!");
				break;
			}
		}
	}
}
