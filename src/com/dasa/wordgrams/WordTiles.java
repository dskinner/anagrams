package com.dasa.wordgrams;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.MotionEvent;

public class WordTiles {
	
	private String[] mAnswers = new String[] {"OPERANTS", "PRONATES", "TROPANES", "PATERSON"};
	private char[] mLetters = new char[] {'O', 'P', 'E', 'R', 'A', 'N', 'T', 'S'};
	
	private float[][] mLetterPosOffset; // [index][x, y, duration]
	float[] mTextPos;
	
	private float mViewPosX;
	private float mViewPosY;
	
	private float mTileSize;
	private float mTextHeightHalf;
	
	private boolean mActionDown;
	private int mSelectIndex;
	
	private float mPosX = 0;
	private float mPosY = 50;
	private float mPrevX;
	private float mPrevY;
	private float mDragDx;
	private float mDragDy;
	
	public WordTiles(String word, String[] answers, float tileSize, float textHeight, float viewPosX, float viewPosY) {
		mTileSize = tileSize;
		mTextHeightHalf = textHeight/2;
		mViewPosX = viewPosX;
		mViewPosY = viewPosY;
		
		mLetterPosOffset = new float[mLetters.length][3];
		mTextPos = new float[mLetters.length*2];
	}
	
	public void onDraw(long delta, Canvas canvas, Paint paint, TextPaint textPaint, Bitmap bmTile, BitmapShader shTile) {
		paint.setColor(Color.WHITE);
		
		for (int i=0; i<mLetters.length; ++i) {
			float bmX = mViewPosX+(i*mTileSize);
			float bmY = mViewPosY;
			
			// adjust for dragging
			if (mActionDown && (i == mSelectIndex)) {
				bmX += mDragDx;
				bmY += mDragDy;
			}
			
			// animate tile movement
			float[] posOffset = mLetterPosOffset[i];
			posOffset[0] -= posOffset[0]/delta*3f;
			posOffset[1] -= posOffset[1]/delta*3f;
			
			if (posOffset[0] > -1 && posOffset[0] < 1) posOffset[0] = 0;
			if (posOffset[1] > -1 && posOffset[1] < 1) posOffset[1] = 0;
			
			bmX += posOffset[0];
			bmY += posOffset[1];
			
			//
			float textX = bmX+(mTileSize/2);
			float textY = bmY+(mTileSize/2)+(mTextHeightHalf);
			
			canvas.drawBitmap(bmTile, bmX, bmY, paint);
			
			mTextPos[i*2] = textX;
			mTextPos[i*2+1] = textY;
		}
		
		synchronized(mLetters) {
			canvas.drawPosText(mLetters, 0, mLetters.length, mTextPos, textPaint);
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
				
				synchronized(mLetters) {
					char tmp = mLetters[mSelectIndex];
					mLetters[mSelectIndex] = mLetters[swapIndex];
					mLetters[swapIndex] = tmp;
				}
				
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
		
		return mActionDown;
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
		for (char letter : mLetters) curWord += letter;
		
		for (String answer : mAnswers) {
			if (curWord.equals(answer)) {
				App.toast("!!! "+curWord+" !!!");
				break;
			}
		}
	}
}
