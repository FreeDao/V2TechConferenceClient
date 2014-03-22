package com.v2tech.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.DisplayMetrics;

public class GlobalConfig {

	public static int GLOBAL_DPI = DisplayMetrics.DENSITY_XHIGH;
	
	public static int GLOBAL_VERSION_CODE = 1;
	
	public static String GLOBAL_VERSION_NAME ="1.3.0.1";
	
	public static double SCREEN_INCHES = 0;
	
	
	public static void saveLogoutFlag(Context context) {
		SharedPreferences sf = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		Editor ed = sf.edit();
		ed.putInt("LoggedIn", 0);
		ed.commit();
	}
}
