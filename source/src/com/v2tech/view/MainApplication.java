package com.v2tech.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Vector;

import net.sourceforge.pinyin4j.PinyinHelper;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.V2.jni.AudioRequest;
import com.V2.jni.ChatRequest;
import com.V2.jni.ConfRequest;
import com.V2.jni.FileRequest;
import com.V2.jni.GroupRequest;
import com.V2.jni.ImRequest;
import com.V2.jni.NativeInitializer;
import com.V2.jni.VideoMixerRequest;
import com.V2.jni.VideoRequest;
import com.V2.jni.WBRequest;
import com.V2.jni.util.V2Log;
import com.v2tech.R;
import com.v2tech.util.AlgorithmUtil;
import com.v2tech.util.GlobalConfig;
import com.v2tech.util.LogService;
import com.v2tech.util.Notificator;
import com.v2tech.util.StorageUtil;
import com.v2tech.view.conference.VideoActivityV2;
import com.v2tech.view.conversation.MessageBuilder;
import com.v2tech.view.conversation.MessageLoader;

public class MainApplication extends Application {

	private static final String TAG = "MainApplication";
	private Vector<WeakReference<Activity>> list = new Vector<WeakReference<Activity>>();
	private final String DATABASE_FILENAME = "hzpy.db";
	private boolean needCopy;

	public boolean netWordIsConnected = false;

	@Override
	public void onCreate() {
		super.onCreate();

		V2Log.isDebuggable = (0 == (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
//		if (!V2Log.isDebuggable) {
//			CrashHandler crashHandler = CrashHandler.getInstance();
//			crashHandler.init(getApplicationContext());
//			new LogcatThread().start();
//		}
		SharedPreferences sf = getSharedPreferences("config",
				Context.MODE_PRIVATE);
		Editor ed = sf.edit();
		ed.putInt("LoggedIn", 0);
		ed.commit();

		try {
			String app_ver = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0).versionName;
			GlobalConfig.GLOBAL_VERSION_NAME = app_ver;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (!V2Log.isDebuggable) {
			new Thread() {

				@Override
				public void run() {
					PinyinHelper.toHanyuPinyinStringArray('c');
				}

			}.start();
		}

		initGloblePath();
		String path = GlobalConfig.getGlobalPath();
		initConfFile();
		
		MessageBuilder.context = getApplicationContext();
		MessageLoader.context = getApplicationContext();
		
		// Load native library
		System.loadLibrary("event");
		System.loadLibrary("v2vi");
		System.loadLibrary("v2ve");
		// System.loadLibrary("NetEvent");
		System.loadLibrary("v2client");

		// Initialize native library
		NativeInitializer.getIntance()
				.initialize(getApplicationContext(), path);
		ImRequest.getInstance(getApplicationContext());
		GroupRequest.getInstance();
		VideoRequest.getInstance(getApplicationContext());
		ConfRequest.getInstance(getApplicationContext());
		AudioRequest.getInstance(getApplicationContext());
		WBRequest.getInstance(getApplicationContext());
		ChatRequest.getInstance(getApplicationContext());
		VideoMixerRequest.getInstance();
		FileRequest.getInstance(getApplicationContext());

		// Start deamon service
		getApplicationContext().startService(
				new Intent(getApplicationContext(), JNIService.class));
		getApplicationContext().startService(
                new Intent(getApplicationContext(), LogService.class));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			this.registerActivityLifecycleCallbacks(new LocalActivityLifecycleCallBack());
		}

		initSQLiteFile();
		initDPI();
	}

	

	/**
	 * 初始化程序数据存储目录
	 */
	private void initGloblePath() {
		// 程序启动时检测SD卡状态
		boolean sdExist = android.os.Environment.MEDIA_MOUNTED
				.equals(android.os.Environment.getExternalStorageState());
		if (!sdExist) {// 如果不存在,
			// --data/data/com.v2tech
			GlobalConfig.DEFAULT_GLOBLE_PATH = getApplicationContext()
					.getFilesDir().getParent();
			V2Log.d(TAG, "SD卡状态异常，数据存储到手机内存中 , 存储路径为："
					+ GlobalConfig.DEFAULT_GLOBLE_PATH);
		} else {
			GlobalConfig.SDCARD_GLOBLE_PATH = StorageUtil
					.getAbsoluteSdcardPath();
			V2Log.d(TAG, "SD卡状态正常，数据存储到SD卡内存中 , 存储路径为："
					+ GlobalConfig.SDCARD_GLOBLE_PATH);
		}
	}

	/**
	 * 初始化搜索用到的hzpy.db文件
	 */
	private void initSQLiteFile() {

		try {
			// 获得.db文件的绝对路径
			String parent = getDatabasePath(DATABASE_FILENAME).getParent();
			File dir = new File(parent);
			// 如果目录不存在，创建这个目录
			if (!dir.exists())
				dir.mkdir();
			String databaseFilename = getDatabasePath(DATABASE_FILENAME)
					.getPath();
			File file = new File(databaseFilename);
			// 目录中不存在 .db文件，则从res\raw目录中复制这个文件到该目录
			if (file.exists()) {
				InputStream is = getResources().openRawResource(R.raw.hzpy);
				if (is == null) {
					V2Log.e("readed sqlite file failed... inputStream is null");
					return;
				}
				String md5 = AlgorithmUtil.getFileMD5(is);
				String currentMD5 = AlgorithmUtil.getFileMD5(new FileInputStream(file));
				if (md5.equals(currentMD5))
					needCopy = false;
				else
					needCopy = true;
			}

			if (!(file.exists()) || needCopy == true) {
				// 获得封装.db文件的InputStream对象
				InputStream is = getResources().openRawResource(R.raw.hzpy);
				if (is == null) {
					V2Log.e("readed sqlite file failed... inputStream is null");
					return;
				}
				FileOutputStream fos = new FileOutputStream(databaseFilename);
				byte[] buffer = new byte[1024];
				int count = 0;
				// 开始复制.db文件
				while ((count = is.read(buffer)) != -1) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
		} catch (Exception e) {
			e.getStackTrace();
			V2Log.e("loading HZPY.db SQListe");
		} finally {
			needCopy = false;
		}
	}

	private void initConfFile() {
		// Initialize global configuration file
		File path = new File(GlobalConfig.getGlobalPath());
		if (!path.exists())
			path.mkdir();

		File optionsFile = new File(path, "log_options.xml");
		if (!optionsFile.exists()) {
			try {
				optionsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		{
			String content = "<xml><path>log</path><v2platform><outputdebugstring>0</outputdebugstring><level>5</level><basename>v2platform</basename><path>log</path><size>1024</size></v2platform></xml>";
			OutputStream os = null;
			try {
				os = new FileOutputStream(optionsFile);
				os.write(content.getBytes());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		{
			File cfgFile = new File(GlobalConfig.getGlobalPath()
					+ "/v2platform.cfg");
			String contentCFG = "<v2platform><C2SProxy><ipv4 value=''/><tcpport value=''/></C2SProxy></v2platform>";

			OutputStream os = null;
			try {
				os = new FileOutputStream(cfgFile);
				os.write(contentCFG.getBytes());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	private void initDPI() {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager manager = (WindowManager) this.getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		manager.getDefaultDisplay().getMetrics(metrics);
		GlobalConfig.GLOBAL_DPI = metrics.densityDpi;
		V2Log.i("Init user device DPI: " + GlobalConfig.GLOBAL_DPI);
		DisplayMetrics dm = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(dm);
		double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
		double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
		GlobalConfig.SCREEN_INCHES = Math.sqrt(x + y);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		V2Log.d(" terminating....");
		ImRequest.getInstance(this).unInitialize();
		GroupRequest.getInstance().unInitialize();
		VideoRequest.getInstance(this).unInitialize();
		ConfRequest.getInstance(this).unInitialize();
		AudioRequest.getInstance(this).unInitialize();
		WBRequest.getInstance(this).unInitialize();
		ChatRequest.getInstance(this).unInitialize();
		this.getApplicationContext().stopService(
				new Intent(this.getApplicationContext(), JNIService.class));
		this.getApplicationContext().stopService(
				new Intent(this.getApplicationContext(), LogService.class));
		V2Log.d(" terminated");

	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		V2Log.e("=================== low memeory :");
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		V2Log.e("=================== trim memeory :" + level);
	}


	public void requestQuit() {
		for (int i = 0; i < list.size(); i++) {
			WeakReference<Activity> w = list.get(i);
			Object obj = w.get();
			if (obj != null) {
				((Activity) obj).finish();
			}
		}

		Handler h = new Handler();
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				GlobalConfig.saveLogoutFlag(getApplicationContext());
				Notificator
						.cancelAllSystemNotification(getApplicationContext());
				System.exit(0);
			}

		}, 1000);
	}

	class LocalActivityLifecycleCallBack implements ActivityLifecycleCallbacks {

		private Object mLock = new Object();
		private int refCount = 0;

		@Override
		public void onActivityCreated(Activity activity,
				Bundle savedInstanceState) {
			Configuration conf = getResources().getConfiguration();
			activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			if (conf.smallestScreenWidthDp >= 600
					|| activity instanceof VideoActivityV2) {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
			V2Log.d(TAG, "add new activity.. : " + activity);
			list.add(0 , new WeakReference<Activity>(activity));
		}

		@Override
		public void onActivityDestroyed(Activity activity) {
			boolean flag = false;
			for (int i = 0; i < list.size(); i++) {
				WeakReference<Activity> w = list.get(i);
				Object obj = w.get();
				if (obj != null && ((Activity) obj) == activity) {
					list.remove(i--);
					flag = true;
					V2Log.d(TAG, "find target activity : " + activity + " --remove it..");
				}
			}
		//	activity.setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
			if(!flag)
				V2Log.d(TAG, "no find the target activity : " + activity + " -- remove failed");
			V2Log.d(TAG, "集合中还剩下 : " + list.size() + " 个activity ");
		}

		@Override
		public void onActivityPaused(Activity activity) {

		}

		@Override
		public void onActivityResumed(Activity activity) {
		}

		@Override
		public void onActivitySaveInstanceState(Activity activity,
				Bundle outState) {

		}

		@Override
		public void onActivityStarted(Activity activity) {
			if (activity instanceof LoginActivity
					|| activity instanceof StartupActivity) {
				return;
			}
			synchronized (mLock) {

				refCount++;
				if (refCount == 1) {
					Notificator.udpateApplicationNotification(
							getApplicationContext(), false, null);
				}

				Notificator
						.cancelAllSystemNotification(getApplicationContext());
			}
		}

		@Override
		public void onActivityStopped(Activity activity) {
			if (activity instanceof LoginActivity
					|| activity instanceof StartupActivity) {
				return;
			}
			synchronized (mLock) {
				refCount--;
				if (refCount == 0) {
					Intent i = activity.getIntent();
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_SINGLE_TOP);
					Notificator.udpateApplicationNotification(
							getApplicationContext(), true, i);
				}
			}
		}
	}

	public boolean theAppIsRunningBackground() {

		String theAppPackageName = getPackageName();
		String currentRuningPackageName = null;

		ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
		List<RunningTaskInfo> listRunningTaskInfo = activityManager
				.getRunningTasks(1);
		if ((listRunningTaskInfo != null) && listRunningTaskInfo.size() > 0) {
			currentRuningPackageName = listRunningTaskInfo.get(0).topActivity
					.getPackageName();

		}

		if ((theAppPackageName == null) || (currentRuningPackageName == null)) {
			return false;
		}

		if (currentRuningPackageName.equals(theAppPackageName)) {
			return false;
		} else {
			return true;
		}
	}


}
