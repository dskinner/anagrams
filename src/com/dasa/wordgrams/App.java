package com.dasa.wordgrams;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

public class App extends Application {
	
	public static App it;
	
	@Override public void onCreate() {
		it = this;
	}
	
	public static void log(Object... objects) {
		String msg = "";
		for (Object object : objects) {
			msg += String.valueOf(object) + " ";
		}
		Log.d("GRAM", msg);
	}
	
	public static void toast(Object... objects) {
		String msg = "";
		for (Object object : objects) {
			msg += String.valueOf(object) + " ";
		}
		Toast.makeText(App.it, msg, Toast.LENGTH_LONG).show();
	}
}
