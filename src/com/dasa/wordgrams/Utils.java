package com.dasa.wordgrams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.TypedValue;

public class Utils {
	public static float toDip(float px) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, App.it.getResources().getDisplayMetrics());
	}
	
	public static Bitmap getBitmap(int id, float size) {
		return getBitmap(id, size, size);
	}
	
	public static Bitmap getBitmap(int id, float w, float h) {
		Bitmap bm = BitmapFactory.decodeResource(App.it.getResources(), id);
		
		// TODO account for `h`
		float scale = w/bm.getWidth();
	    
	    Matrix matrix = new Matrix();
	    matrix.postScale(scale, scale);
	    
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false);
	    return resizedBitmap;
	}
}
