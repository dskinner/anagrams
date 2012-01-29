package com.dasa.wordgrams;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WordGramsActivity extends Activity {

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new GameView(this));
	}

	public class GameView extends SurfaceView implements SurfaceHolder.Callback {
		
		private static final float TEXT_SCALE = 0.6f;
		
		private String[] mLetters = new String[] {"O", "P", "E", "R", "A", "N", "T", "S"};
		private String[] mAnswers = new String[] {"OPERANTS", "PRONATES", "TROPANES", "PATERSON"};
		
		private GameThread mGameThread;
		private Paint mPaint;
		private TextPaint mTextPaint;
		
		private Bitmap mBmTileDefault;
		private Bitmap mBmTileFocus;
		
		private float mNumColumns = 8;
		private float mTileSize;
		
		private float mPosX = 0;
		private float mPosY = 50;
		
		private boolean mActionDown;
		private int mSelectIndex;
		
		private float mPrevX;
		private float mPrevY;
		private float mDragDx;
		private float mDragDy;
		
		private float mScreenWidth;
		
		private long mDelta;

		public GameView(Context context) {
			super(context);
			
			mScreenWidth = WordGramsActivity.this.getWindowManager().getDefaultDisplay().getWidth();
			mTileSize = (mScreenWidth)/mNumColumns;

			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			
			mTextPaint = new TextPaint();
			mTextPaint.setAntiAlias(true);
			mTextPaint.setTextSize(mTileSize*TEXT_SCALE);
			mTextPaint.setShadowLayer((mTileSize*TEXT_SCALE)*0.07f, 0, 0, Color.parseColor("#a65400"));
			mTextPaint.setColor(Color.WHITE);
			mTextPaint.setTextAlign(Align.CENTER);
			mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
			
			mBmTileDefault = Utils.getBitmap(R.drawable.tile_bg_default, mTileSize);
			
			getHolder().addCallback(this);
		}

		@Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

		@Override public void surfaceCreated(SurfaceHolder holder) {
			mGameThread = new GameThread(this, holder);
			mGameThread.start();
		}

		@Override public void surfaceDestroyed(SurfaceHolder holder) {
			mGameThread.setRunning(false);
		}
		
		public void onDraw(Canvas canvas, long delta) {
			mDelta = delta;
			onDraw(canvas);
		}
		
		@Override protected void onDraw(Canvas canvas) {
			mPaint.setColor(Color.parseColor("#057d9f"));
			mPaint.setStyle(Style.FILL);
			
			canvas.drawPaint(mPaint);
			
			mPaint.setColor(Color.WHITE);
			
			for (int i=0; i<mLetters.length; ++i) {
				
				String letter = mLetters[i];
				
				float offsetX = i*mTileSize;
				
				float bmX = offsetX;
				float bmY = 0;
				
				Rect bounds = new Rect();
				mTextPaint.getTextBounds(letter, 0, 1, bounds);
				int textHeight = bounds.bottom-bounds.top;
				
				float textX = bmX+(mTileSize/2);
				float textY = bmY+(mTileSize/2)+(textHeight/2);
				
				if (mActionDown && (i == mSelectIndex)) {
					bmX += mDragDx;
					bmY += mDragDy;
					textX += mDragDx;
					textY += mDragDy;
				}
				
				canvas.drawBitmap(mBmTileDefault, bmX, bmY, mPaint);
				canvas.drawText(letter, textX, textY, mTextPaint);
			}
		}
		
		@Override public boolean onTouchEvent(MotionEvent event) {
			mPosX = event.getX();
			mPosY = event.getY();
			
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mSelectIndex = findHit(mPosX, mPosY);
					mDragDx = 0;
					mDragDy = 0;
					mPrevX = mPosX;
					mPrevY = mPosY;
					mActionDown = true;
					break;
				case MotionEvent.ACTION_MOVE:
					int swapIndex = findHit(mPosX, mPosY);
					if (swapIndex == mSelectIndex) break;
					
					String tmp = mLetters[mSelectIndex];
					mLetters[mSelectIndex] = mLetters[swapIndex];
					mLetters[swapIndex] = tmp;
					
					if (swapIndex < mSelectIndex) mDragDx += mTileSize;
					else mDragDx -= mTileSize;
					
					mSelectIndex = swapIndex;
					
					break;
				case MotionEvent.ACTION_UP:
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
		
		public int findHit(float x, float y) {
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
	
	public class GameThread extends Thread {
		
		private GameView mView;
		private SurfaceHolder mHolder;
		private boolean mRunning;
		private long mPrevTime;
		
		public GameThread(GameView view, SurfaceHolder holder) {
			mView = view;
			mHolder = holder;
			mRunning = true;
		}
		
		public void setRunning(boolean b) {
			mRunning = b;
		}
		
		public void run() {
			Canvas canvas;
			
			while (mRunning) {
				long time = System.currentTimeMillis();
				
				canvas = mHolder.lockCanvas();
				
				if (canvas == null) continue;
				
				mView.onDraw(canvas, time-mPrevTime);
				mHolder.unlockCanvasAndPost(canvas);
				
				mPrevTime = time;
			}
		}
	}
}