package com.dasa.wordgrams;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.Rect;
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
		
		private final int NUM_COLUMNS = 8;
		private final int COLOR_BG = Color.parseColor("#057d9f");
		
		private final float TEXT_SCALE = 0.6f;
		
		private CanvasContext mCanvasContext;
		
		private WordTiles mWordTiles;
		
		private GameThread mGameThread;

		public GameView(Context context) {
			super(context);
			
			CanvasContext cc = mCanvasContext = new CanvasContext();
			Display display = WordGramsActivity.this.getWindowManager().getDefaultDisplay();
			
			cc.screenWidth = display.getWidth();
			cc.screenHeight = display.getHeight();
			cc.screenPad = cc.screenWidth*0.015f;
			
			cc.tileSize = (cc.screenWidth-(cc.screenPad*2))/NUM_COLUMNS;
			cc.tileSizeHalf = cc.tileSize/2;

			cc.paint = new Paint();
			cc.paint.setAntiAlias(false);
			cc.paint.setDither(false);
			
			cc.textPaint = new TextPaint();
			cc.textPaint.setAntiAlias(true);
			cc.textPaint.setTextSize(cc.tileSize*TEXT_SCALE);
			cc.textPaint.setShadowLayer((cc.tileSize*TEXT_SCALE)*0.07f, 0, 0, Color.parseColor("#a65400"));
			cc.textPaint.setColor(Color.WHITE);
			cc.textPaint.setTextAlign(Align.CENTER);
			cc.textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
			
			cc.bmTile = Utils.getBitmap(R.drawable.tile_bg_default, cc.tileSize);
			cc.bmStarOn = Utils.getBitmap(R.drawable.star_on, cc.tileSize*2);
			
			Rect bounds = new Rect();
			cc.textPaint.getTextBounds("A", 0, 1, bounds);
			cc.textHeight = bounds.bottom-bounds.top;
			cc.textHeightHalf = cc.textHeight/2;
			
			mWordTiles = new WordTiles(new String[] {"OPER", "ROPE", "PORE", "REPO"}, cc);
			
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
			mCanvasContext.recycleBitmaps();
		}
		
		public void onDraw(Canvas canvas, long delta) {
			mCanvasContext.delta = delta;
			onDraw(canvas);
		}
		
		@Override protected void onDraw(Canvas canvas) {
			canvas.drawColor(COLOR_BG);
			
			mWordTiles.onDraw(canvas, mCanvasContext);
		}
		
		@Override public boolean onTouchEvent(MotionEvent event) {
			if (mWordTiles.onTouchEvent(event)) {
				return true;
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
			try {
				join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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