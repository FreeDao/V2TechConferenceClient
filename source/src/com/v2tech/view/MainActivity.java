package com.v2tech.view;

import java.util.List;
import java.util.Vector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;
import android.widget.Toast;

import com.v2tech.R;
import com.v2tech.service.GlobalHolder;
import com.v2tech.util.GlobalConfig;
import com.v2tech.util.Notificator;
import com.v2tech.util.V2Log;
import com.v2tech.vo.Conversation;

public class MainActivity extends FragmentActivity {

	private Context mContext;
	private boolean exitedFlag = false;

	private TabHost mTabHost;
	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;

	public static final String SERVICE_BOUNDED_EVENT = "com.v2tech.SERVICE_BOUNDED_EVENT";
	public static final String SERVICE_UNBOUNDED_EVENT = "com.v2tech.SERVICE_UNBOUNDED_EVENT";

	private TabClass[] mTabClasses = new TabClass[] {
			new TabClass(PublicIntent.TAG_CONTACT,
					R.drawable.selector_tab_contact_button,
					R.string.tab_contact_name,
					ContactsTabFragment.class.getName()),
			new TabClass(PublicIntent.TAG_ORG,
					R.drawable.selector_tab_org_button, R.string.tab_org_name,
					ContactsTabFragment.class.getName()),
			new TabClass(PublicIntent.TAG_GROUP,
					R.drawable.selector_tab_group_button,
					R.string.tab_group_name,
					ConversationsTabFragment.class.getName()),
			new TabClass(PublicIntent.TAG_CONF,
					R.drawable.selector_tab_conference_button,
					R.string.tab_conference_name,
					ConversationsTabFragment.class.getName()),
			new TabClass(PublicIntent.TAG_COV,
					R.drawable.selector_tab_conversation_button,
					R.string.tab_conversation_name,
					ConversationsTabFragment.class.getName()) };

	private LocalReceiver receiver = new LocalReceiver();

	/**
	 * A simple factory that returns dummy views to the Tabhost
	 * 
	 */
	class TabFactory implements TabContentFactory {

		private final Context mContext;

		/**
		 * @param context
		 */
		public TabFactory(Context context) {
			mContext = context;
		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
		 */
		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (GlobalConfig.GLOBAL_LAYOUT_SIZE == Configuration.SCREENLAYOUT_SIZE_XLARGE
				|| GlobalConfig.GLOBAL_LAYOUT_SIZE == Configuration.SCREENLAYOUT_SIZE_LARGE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		// Inflate the layout
		setContentView(R.layout.activity_main);
		// Initialise the TabHost
		mContext = this;
		this.initialiseTabHost(savedInstanceState);
		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
		// Intialise ViewPager
		this.intialiseViewPager();
		initDPI();
		initReceiver();
		// Start animation
		this.overridePendingTransition(R.animator.left_in, R.animator.left_out);
		V2Log.d(" main onCreate ");
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
	 */
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("tab", mTabHost.getCurrentTabTag());
		super.onSaveInstanceState(outState);
	}

	private void initDPI() {
		DisplayMetrics metrics = new DisplayMetrics();

		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		GlobalConfig.GLOBAL_DPI = metrics.densityDpi;
		V2Log.i("Init user device DPI: " + GlobalConfig.GLOBAL_DPI);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
		double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
		GlobalConfig.SCREEN_INCHES = Math.sqrt(x + y);
	}

	/**
	 * Initialise ViewPager
	 */
	private void intialiseViewPager() {

		List<Fragment> fragments = new Vector<Fragment>();

		for (TabClass tc : mTabClasses) {
			Bundle bundle = new Bundle();
			bundle.putString("tag", tc.mTabName);
			Fragment frg = Fragment.instantiate(this, tc.clsName, bundle);
			fragments.add(frg);

		}

		this.mPagerAdapter = new PagerAdapter(
				super.getSupportFragmentManager(), fragments);
		this.mViewPager = (ViewPager) super.findViewById(R.id.viewpager);
		this.mViewPager.setAdapter(this.mPagerAdapter);
		this.mViewPager.setOnPageChangeListener(pageChangeListener);
		this.mViewPager.setOffscreenPageLimit(5);
	}

	/**
	 * Initialize the Tab Host
	 */
	private void initialiseTabHost(Bundle args) {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();

		for (TabClass tc : mTabClasses) {
			TabHost.TabSpec confTabSpec = this.mTabHost.newTabSpec(tc.mTabName)
					.setIndicator(getTabView(tc));
			confTabSpec.setContent(new TabFactory(this));
			mTabHost.addTab(confTabSpec);
		}

		mTabHost.setOnTabChangedListener(tabChnageListener);
	}

	private View getTabView(TabClass tcl) {

		LayoutInflater inflater = LayoutInflater.from(this);
		View v = inflater.inflate(R.layout.tab_widget_view, null, false);
		ImageView iv = (ImageView) v.findViewById(R.id.tab_image);
		if (iv != null) {
			iv.setImageDrawable(this.getResources().getDrawable(tcl.mDraId));
			iv.bringToFront();
		}

		TextView tv = (TextView) v.findViewById(R.id.tab_name);
		if (tv != null) {
			tv.setText(this.getResources().getText(tcl.mTabNameId));
			tv.bringToFront();
		}

		View notifi = v.findViewById(R.id.tab_notificator);
		tcl.notificator = notifi;
		tcl.notificator.setVisibility(View.INVISIBLE);
		return v;
	}

	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(PublicIntent.UPDATE_CONVERSATION);
		filter.addCategory(PublicIntent.DEFAULT_CATEGORY);
		filter.addAction(PublicIntent.FINISH_APPLICATION);
		mContext.registerReceiver(receiver, filter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		V2Log.d(" main onStart ");
	}

	@Override
	protected void onStop() {
		super.onStop();
		V2Log.d(" main onStop ");
	}

	@Override
	public void onBackPressed() {
		requestQuit();
	}

	public void requestQuit() {
		if (exitedFlag) {
			this.getApplicationContext().stopService(
					new Intent(this.getApplicationContext(), JNIService.class));

			GlobalConfig.saveLogoutFlag(this);
			Notificator.cancelAllSystemNotification(this);
			System.exit(0);
		} else {
			exitedFlag = true;
			Toast.makeText(this, R.string.quit_promption, Toast.LENGTH_SHORT)
					.show();
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					exitedFlag = false;
				}

			}, 2500);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mContext.unregisterReceiver(receiver);
		mContext.stopService(new Intent(this.getApplicationContext(),
				JNIService.class));
		V2Log.d("system destroyed v2tech");
	}

	private TabHost.OnTabChangeListener tabChnageListener = new TabHost.OnTabChangeListener() {

		/**
		 * (non-Javadoc)
		 * 
		 * @see android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
		 */
		public void onTabChanged(String tag) {
			int pos = mTabHost.getCurrentTab();
			V2Log.d(" onTabChanged " + "  " + pos);
			if (mViewPager == null) {
				V2Log.e(" MainActivity state is illegal");
				return;
			}
			mViewPager.setCurrentItem(pos);

		}

	};

	private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int pos) {
			mTabHost.setCurrentTab(pos);
		}

	};

	public class PagerAdapter extends FragmentPagerAdapter {

		private List<Fragment> fragments;

		/**
		 * @param fm
		 * @param fragments
		 */
		public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
			super(fm);
			this.fragments = fragments;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
		 */
		@Override
		public Fragment getItem(int position) {
			Fragment frag = this.fragments.get(position);
			return frag;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.support.v4.view.PagerAdapter#getCount()
		 */
		@Override
		public int getCount() {
			return this.fragments.size();
		}
	}

	class LocalReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (PublicIntent.UPDATE_CONVERSATION.equals(action)) {
				String type = intent.getExtras().getString("type");
				View noticator = null;
				if (type.equals(Conversation.TYPE_GROUP)) {
					noticator = mTabClasses[2].notificator;
				} else if (type.equals(Conversation.TYPE_CONFERNECE)) {
					noticator = mTabClasses[3].notificator;
				} else if (type.equals(Conversation.TYPE_CONTACT)) {
					noticator = mTabClasses[4].notificator;
				}

				V2Log.i("type:" + type + "  count:"
						+ GlobalHolder.getInstance().getNoticatorCount(type));
				if (noticator != null) {
					if (GlobalHolder.getInstance().getNoticatorCount(type) > 0) {
						noticator.setVisibility(View.VISIBLE);
					} else {
						noticator.setVisibility(View.GONE);
					}
				}
			} else if (PublicIntent.FINISH_APPLICATION.equals(action)) {
				exitedFlag = true;
				requestQuit();
			}
		}

	}

	class TabClass {
		String mTabName;
		int mDraId;
		int mTabNameId;
		String clsName;
		View notificator;

		public TabClass(String mTabName, int mDraId, int mTabTitle,
				String clsName, View notificator) {
			super();
			this.mTabName = mTabName;
			this.mDraId = mDraId;
			this.mTabNameId = mTabTitle;
			this.clsName = clsName;
			this.notificator = notificator;
		}

		public TabClass(String tabName, int draId, int tabTitle, String clsName) {
			this(tabName, draId, tabTitle, clsName, null);
		}

	}

}