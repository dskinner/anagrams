package com.dasa.wordgrams;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.MotionEvent;

public class WordTiles {
	private final int COLOR_ROW_BG = Color.parseColor("#61b7cf");
	
	private RectF mRectF;
	
	private String[] mAnswers;
	private char[] mLetters;
	
	private float[][] mLetterPosOffset; // [index][x, y, duration]
	float[] mTextPos;
	
	//
	private float mTileSize;
	private float mScreenPad;
	
	
	private boolean mActionDown;
	private int mSelectIndex;
	
	// MotionEvent
	private float mPosX = 0;
	private float mPosY = 50;
	private float mPrevX;
	private float mPrevY;
	private float mDragDx;
	private float mDragDy;
	
	
	//
	float sx = 0;
	float sy;
	float mRot;
	private Matrix mMatrix = new Matrix();
	boolean b;
	boolean b2;
	
	public WordTiles(String[] answers, CanvasContext cc) {
		mAnswers = answers;
		mLetters = mAnswers[0].toCharArray();
		mLetterPosOffset = new float[mLetters.length][3];
		
		mTileSize = cc.tileSize;
		mScreenPad = cc.screenPad;
		
		mTextPos = new float[2];
		mRectF = new RectF(
				cc.screenPad,
				cc.screenPad+cc.tileSize+cc.screenPad,
				cc.screenWidth-cc.screenPad,
				cc.screenHeight-cc.screenPad);
	}
	
	public void onDraw(Canvas canvas, CanvasContext cc) {
		cc.paint.setColor(COLOR_ROW_BG);
		canvas.drawRect(mRectF, cc.paint);
		
		cc.paint.setColor(Color.WHITE);
		
		for (int i=0; i<mLetters.length; ++i) {
			float bmX = cc.screenPad+(i*cc.tileSize);
			float bmY = cc.screenPad;
			
			// adjust for dragging
			if (mActionDown && (i == mSelectIndex)) {
				bmX += mDragDx;
				bmY += mDragDy;
			}
			
			// animate tile movement
			float[] posOffset = mLetterPosOffset[i];
			posOffset[0] -= posOffset[0]/cc.delta*3f;
			posOffset[1] -= posOffset[1]/cc.delta*3f;
			
			if (posOffset[0] > -1 && posOffset[0] < 1) posOffset[0] = 0;
			if (posOffset[1] > -1 && posOffset[1] < 1) posOffset[1] = 0;
			
			bmX += posOffset[0];
			bmY += posOffset[1];
			
			mTextPos[0] = bmX+cc.tileSizeHalf;
			mTextPos[1] = bmY+cc.tileSizeHalf+cc.textHeightHalf;
			
			canvas.drawBitmap(cc.bmTile, bmX, bmY, cc.paint);
			canvas.drawPosText(mLetters, i, 1, mTextPos, cc.textPaint);
		}
		
		
		// TODO have star pause on full scale, but do a minor scale back and forward to make it pop/bounce
		// before shrinking to bottom of screen
		// testing star
		if (b) {
			sx += 0.0025*cc.delta;
			mRot += 0.0025*cc.delta*360;
		}
		
		if (mRot > 360) {
			mRot = 360;
			sx = 1;
			b2 = true;
			b = false;
		}
		
		float y = cc.screenHeight/2f-cc.bmStarOn.getHeight()/2f;
		
		if (b2) {
			sx -= 0.0025*cc.delta;
			
			
			float diff = (cc.screenHeight-cc.bmStarOn.getHeight()/2f) - y;
			
			diff *= 1-sx;
			y += diff;
			
			
			if (sx < 0.2f) sx = .2f;
		}
		
		mMatrix.setRotate(mRot, cc.bmStarOn.getWidth()/2f, cc.bmStarOn.getHeight()/2f);
		mMatrix.postScale(sx, sx, cc.bmStarOn.getWidth()/2f, cc.bmStarOn.getHeight()/2f);
		mMatrix.postTranslate(cc.screenWidth/2f-cc.bmStarOn.getWidth()/2f, y);
		
		if (b || b2) canvas.drawBitmap(cc.bmStarOn, mMatrix, cc.paint);
		
		/*
		for (int i=0; i<mAnswers.length; ++i) {
			int offset = 0;
			if (i == 0) offset = -75;
			if (i == 2) offset = 75;
			Matrix m = new Matrix();
			m.postScale(.2f, .2f);
			m.postTranslate(cc.screenWidth/2+offset-(cc.bmStarOn.getWidth()*.2f/2), cc.screenHeight-100);
			canvas.drawBitmap(cc.bmStarOn, m, cc.paint);
		}
		*/
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		mPosX = event.getX();
		mPosY = event.getY();
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				int index = gridHitTest(mPosX, mPosY);
				if (index == -1) break;
				
				// gridHitTest performs poorly with screen padding
				if (index >= mLetters.length) index = mLetters.length-1;
				
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
				
				//synchronized(mLetters) {
				char tmp = mLetters[mSelectIndex];
				mLetters[mSelectIndex] = mLetters[swapIndex];
				mLetters[swapIndex] = tmp;
				//}
				
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
		if (y < mScreenPad+mTileSize && y > mScreenPad) return (int) ((x)/mTileSize);
		
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
				mRot = 0;
				sx = 0;
				b = true;
				b2 = false;
				break;
			}
		}
	}
}
