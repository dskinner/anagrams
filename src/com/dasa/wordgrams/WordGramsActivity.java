package com.dasa.wordgrams;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;
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
		
		private final int COLOR_BG = Color.parseColor("#057d9f");
		private final float TEXT_SCALE = 0.6f;
		
		private Display mDisplay;
		
		private WordTiles[] mWordTiles;
		
		private GameThread mGameThread;
		private Paint mPaint;
		private TextPaint mTextPaint;
		
		private Bitmap mBmTile;
		private BitmapShader mShTile;
		
		private float mNumColumns = 8;
		private float mTileSize;
		
		private float mScreenWidth;
		private float mScreenHeight;
		
		private long mDelta;

		public GameView(Context context) {
			super(context);
			
			mDisplay = WordGramsActivity.this.getWindowManager().getDefaultDisplay();
			
			mScreenWidth = mDisplay.getWidth();
			mScreenHeight = mDisplay.getHeight();
			
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
			mShTile = new BitmapShader(mBmTile, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
			
			Rect bounds = new Rect();
			mTextPaint.getTextBounds("A", 0, 1, bounds);
			int textHeight = bounds.bottom-bounds.top;
			
			mWordTiles = new WordTiles[3];
			mWordTiles[0] = new WordTiles("OPERANTS", null, mTileSize, textHeight, 0, mTileSize*0);
			mWordTiles[1] = new WordTiles("OPERANTS", null, mTileSize, textHeight, 0, mTileSize*2);
			mWordTiles[2] = new WordTiles("OPERANTS", null, mTileSize, textHeight, 0, mTileSize*4);
			
			getHolder().addCallback(this);
		}

		@Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

		@Override public void surfaceCreated(SurfaceHolder holder) {
			//holder.setFormat(PixelFormat.RGBA_8888);
			
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
			canvas.drawColor(COLOR_BG);
			
			for (WordTiles wordTiles : mWordTiles) {
				wordTiles.onDraw(mDelta, canvas, mPaint, mTextPaint, mBmTile, mShTile);
			}
		}
		
		@Override public boolean onTouchEvent(MotionEvent event) {
			for (WordTiles wordTiles : mWordTiles) {
				if (wordTiles.onTouchEvent(event)) break;
			}
			return true;
		}
	}
	
	public class GameThread extends Thread {
		private static final long TARGET_FPS = 1000/60;
		
		private GameView mView;
		private SurfaceHolder mHolder;
		private boolean mRunning;
		
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
			long time;
			long prevTime = 0;
			long delta;
			
			while (mRunning) {
				time = System.currentTimeMillis();
				delta = time-prevTime;
				
				if (delta < TARGET_FPS) {
					try {
						Thread.sleep(TARGET_FPS-delta);
						time = System.currentTimeMillis();
						delta = time-prevTime;
					} catch (Exception e) {}
				}
				
				canvas = mHolder.lockCanvas();
				
				if (canvas == null) continue;
				
				mView.onDraw(canvas, delta*TARGET_FPS/delta);
				mHolder.unlockCanvasAndPost(canvas);
				
				prevTime = time;
			}
		}
	}
}