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
				
				if (swapIndex < mSelectIndex) mDragDx += mTileSize;
				else mDragDx -= mTileSize;
				
				mSelectIndex = swapIndex;
				
				break;
			case MotionEvent.ACTION_UP:
				if (!mActionDown) break;
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
				App.toast("!!!!!!!!!!!!");
				break;
			}
		}
	}
}
