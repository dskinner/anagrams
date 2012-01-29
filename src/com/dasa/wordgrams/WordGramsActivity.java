package com.dasa.wordgrams;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.Display;
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
		
		private WordTiles[] mWordTiles;
		
		private GameThread mGameThread;
		private Paint mPaint;
		private TextPaint mTextPaint;
		
		private Bitmap mBmTile;
		
		private float mNumColumns = 8;
		private float mTileSize;
		
		private float mScreenWidth;
		private float mScreenHeight;
		
		private long mDelta;

		public GameView(Context context) {
			super(context);
			
			Display display = WordGramsActivity.this.getWindowManager().getDefaultDisplay();
			mScreenWidth = display.getWidth();
			mScreenHeight = display.getHeight();
			
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
			
			mBmTile = Utils.getBitmap(R.drawable.tile_bg_default, mTileSize);
			
			mWordTiles = new WordTiles[7];
			mWordTiles[0] = new WordTiles("OPERANTS", null, mTileSize, 0, mTileSize*0);
			mWordTiles[1] = new WordTiles("OPERANTS", null, mTileSize, 0, mTileSize*1);
			mWordTiles[2] = new WordTiles("OPERANTS", null, mTileSize, 0, mTileSize*2);
			mWordTiles[3] = new WordTiles("OPERANTS", null, mTileSize, 0, mTileSize*3);
			mWordTiles[4] = new WordTiles("OPERANTS", null, mTileSize, 0, mTileSize*4);
			mWordTiles[5] = new WordTiles("OPERANTS", null, mTileSize, 0, mTileSize*5);
			mWordTiles[6] = new WordTiles("OPERANTS", null, mTileSize, 0, mTileSize*6);
			
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
			
			for (WordTiles wordTiles : mWordTiles) {
				wordTiles.onDraw(mDelta, canvas, mPaint, mTextPaint, mBmTile);
			}
			
			drawFps(canvas);
		}
		
		@Override public boolean onTouchEvent(MotionEvent event) {
			for (WordTiles wordTiles : mWordTiles) {
				wordTiles.onTouchEvent(event);
			}
			return true;
		}
		
		
		
		private long accum = 0;
		private long lastDelta = 1;
		private void drawFps(Canvas canvas) {
			accum += mDelta;
			if (accum > 500) {
				accum = 0;
				lastDelta = mDelta;
			}
			
			mPaint.setColor(Color.WHITE);
			mPaint.setTextSize(Utils.toDip(20));
			canvas.drawText(String.format("%.2f", (float) (1000f/lastDelta)), 0, mScreenHeight-(mTileSize*2), mPaint);
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