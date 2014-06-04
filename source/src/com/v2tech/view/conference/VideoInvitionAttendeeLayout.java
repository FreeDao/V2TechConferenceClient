package com.v2tech.view.conference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.v2tech.R;
import com.v2tech.service.GlobalHolder;
import com.v2tech.service.jni.JNIResponse;
import com.v2tech.service.jni.RequestConfCreateResponse;
import com.v2tech.view.PublicIntent;
import com.v2tech.view.cus.DateTimePicker;
import com.v2tech.view.cus.DateTimePicker.OnDateSetListener;
import com.v2tech.vo.Conference;
import com.v2tech.vo.ConferenceGroup;
import com.v2tech.vo.Group;
import com.v2tech.vo.Group.GroupType;
import com.v2tech.vo.User;

public class VideoInvitionAttendeeLayout extends LinearLayout {

	private static final int UPDATE_LIST_VIEW = 1;
	private static final int UPDATE_ATTENDEES = 2;
	private static final int UPDATE_SEARCHED_USER_LIST = 3;
	private static final int CREATE_CONFERENC_RESP = 4;

	private static final int PAD_LAYOUT = 1;
	private static final int PHONE_LAYOUT = 0;

	private Context mContext;
	private LocalHandler mLocalHandler = new LocalHandler();

	private EditText searchedTextET;
	private ListView mContactsContainer;
	private ContactsAdapter adapter = new ContactsAdapter();
	private EditText mConfTitleET;
	private EditText mConfStartTimeET;
	private View mInvitionButton;

	private LinearLayout mErrorNotificationLayout;

	private LinearLayout mAttendeeContainer;

	private View mScroller;

	private boolean mIsStartedSearch;

	private List<ListItem> mItemList = new ArrayList<ListItem>();
	private List<ListItem> mCacheItemList;
	private List<Group> mGroupList;

	// Used to save current selected user
	private Set<User> mAttendeeList = new HashSet<User>();

	private Conference conf;

	private int landLayout = PAD_LAYOUT;
	
	private Listener listener;
	
	
	
	public interface Listener {
		public void requestInvitation(Conference conf, List<User> l); 
	}

	public VideoInvitionAttendeeLayout(Context context, Conference conf) {
		super(context);
		this.conf = conf;
		initLayout();
	}

	
	private void initLayout() {
		mContext = getContext();
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.video_invition_attendee_layout, null, false);

		mContactsContainer = (ListView) view
				.findViewById(R.id.conf_create_contacts_list);
		mContactsContainer.setOnItemClickListener(itemListener);
		mContactsContainer.setAdapter(adapter);

		mAttendeeContainer = (LinearLayout) view.findViewById(R.id.conference_attendee_container);
		mAttendeeContainer.setGravity(Gravity.CENTER);
		landLayout = mAttendeeContainer.getTag().equals("vertical") ? PAD_LAYOUT
				: PHONE_LAYOUT;


		mConfTitleET = (EditText) view.findViewById(R.id.conference_create_conf_name);
		mConfTitleET.setEnabled(false);
		mConfStartTimeET = (EditText) view.findViewById(R.id.conference_create_conf_start_time);
		mConfStartTimeET.setEnabled(false);
		

		searchedTextET = (EditText) view.findViewById(R.id.contacts_search);
		searchedTextET.addTextChangedListener(textChangedListener);

		mErrorNotificationLayout = (LinearLayout) view.findViewById(R.id.conference_create_error_notification);
		mScroller = view.findViewById(R.id.conf_create_scroll_view);
		mInvitionButton =  view.findViewById(R.id.video_invition_attendee_ly_invition_button);
		mInvitionButton.setOnClickListener(confirmButtonListener);

		this.addView(view, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		initData();
		
		new LoadContactsAT().execute();
	}
	
	public void setListener(Listener l) {
		this.listener = l;
	}
	
	private void initData() {
		mConfTitleET.setText(conf.getName());
		mConfStartTimeET.setText(conf.getStartTimeStr());
	}

	public boolean isScreenLarge() {
		final int screenSize = getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
		return screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	private void updateView(int pos) {
		ListItem item = mItemList.get(pos);
		if (item.g == null) {
			return;
		}
		if (item.isExpanded == false) {
			for (Group g : item.g.getChildGroup()) {
				ListItem cache = new ListItem(g, g.getLevel());
				mItemList.add(++pos, cache);
			}
			List<User> sortList = new ArrayList<User>();
			sortList.addAll(item.g.getUsers());
			Collections.sort(sortList);
			for (User u : sortList) {
				ListItem cache = new ListItem(u, item.g.getLevel() + 1);
				mItemList.add(++pos, cache);
				updateItem(cache);
			}

		} else {
			if (item.g.getChildGroup().size() <= 0
					&& item.g.getUsers().size() <= 0) {
				return;
			}

			int startRemovePos = pos + 1;
			int endRemovePos = pos;
			for (int index = pos + 1; index < mItemList.size(); index++) {
				ListItem li = mItemList.get(index);
				if (li.g != null && li.g.getLevel() <= item.g.getLevel()) {
					break;
				}
				if (li.u != null && li.level == item.g.getLevel()) {
					break;
				}
				endRemovePos++;
			}

			while (startRemovePos <= endRemovePos
					&& endRemovePos < mItemList.size()) {
				mItemList.remove(startRemovePos);
				endRemovePos--;
			}
		}

		item.isExpanded = !item.isExpanded;
		adapter.notifyDataSetChanged();
	}

	private void updateUserToAttendList(final User u) {
		if (u == null) {
			return;
		}
		boolean remove = false;
		for (User tu : mAttendeeList) {
			if (tu.getmUserId() == u.getmUserId()) {
				mAttendeeList.remove(tu);
				remove = true;
				break;
			}
		}

		if (remove) {
			removeAttendee(u);
		} else {
			addAttendee(u);
		}

	}

	private void removeAttendee(User u) {
		mAttendeeContainer.removeAllViews();
		for (User tmpU : mAttendeeList) {
			addAttendee(tmpU);
		}
	}

	private void updateItem(ListItem it) {
		if (it == null || it.u == null) {
			return;
		}

		for (User u : mAttendeeList) {
			if (it.u.getmUserId() == u.getmUserId()) {
				((ContactUserView) it.v).updateChecked();
			}
		}
	}

	private void addAttendee(User u) {
		if (u.isCurrentLoggedInUser()) {
			return;
		}
		mAttendeeList.add(u);

		View v = null;
		if (landLayout == PAD_LAYOUT) {
			v = new ContactUserView(mContext, u, false);
			v.setTag(u);
			v.setOnClickListener(removeAttendeeListener);
		} else {
			v = getAttendeeView(u);
		}
		mAttendeeContainer.addView(v);

		if (mAttendeeContainer.getChildCount() > 0) {
			mScroller.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mAttendeeContainer.getChildCount() <= 0) {
						return;
					}
					View child = mAttendeeContainer
							.getChildAt(mAttendeeContainer.getChildCount() - 1);
					if (landLayout == PAD_LAYOUT) {
						((ScrollView) mScroller).scrollTo(child.getRight(),
								child.getBottom());
					} else {
						((HorizontalScrollView) mScroller).scrollTo(
								child.getRight(), child.getBottom());
					}
				}

			}, 100L);
		}
	}

	/**
	 * Use to add scroll view
	 * 
	 * @param u
	 * @return
	 */
	private View getAttendeeView(final User u) {
		final LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER);

		ImageView iv = new ImageView(mContext);
		if (u.getAvatarBitmap() != null) {
			iv.setImageBitmap(u.getAvatarBitmap());
		} else {
			iv.setImageResource(R.drawable.avatar);
		}
		ll.addView(iv, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		TextView tv = new TextView(mContext);
		tv.setText(u.getName());
		tv.setEllipsize(TruncateAt.END);
		tv.setSingleLine(true);
		tv.setTextSize(8);
		tv.setMaxWidth(60);
		ll.setTag(u.getmUserId() + "");
		ll.addView(tv, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		ll.setPadding(5, 5, 5, 5);
		if (u.isCurrentLoggedInUser()) {
			return ll;
		}
		ll.setTag(u);
		ll.setOnClickListener(removeAttendeeListener);

		return ll;
	}

	private void updateSearchedUserList(List<User> lu) {
		mItemList = new ArrayList<ListItem>();
		for (User u : lu) {
			ListItem item = new ListItem(u, -1);
			((ContactUserView) item.v).removePadding();
			mItemList.add(item);
			updateItem(item);
		}
		adapter.notifyDataSetChanged();
	}

	private OnClickListener removeAttendeeListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			User u = (User) view.getTag();
			Message.obtain(mLocalHandler, UPDATE_ATTENDEES, u).sendToTarget();
			for (int index = 0; index < mItemList.size(); index++) {
				ListItem li = mItemList.get(index);
				if (li.u != null && u.getmUserId() == li.u.getmUserId()) {
					((ContactUserView) li.v).updateChecked();
				}
			}
		}

	};

	private TextWatcher textChangedListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			if (s != null && s.length() > 0) {
				if (!mIsStartedSearch) {
					mCacheItemList = mItemList;
					mIsStartedSearch = true;
				}
			} else {
				if (mIsStartedSearch) {
					mItemList = mCacheItemList;
					adapter.notifyDataSetChanged();
					mIsStartedSearch = false;
				}
				return;
			}
			String str = s == null ? "" : s.toString();
			List<User> searchedUserList = new ArrayList<User>();
			for (Group g : mGroupList) {
				Group.searchUser(str, searchedUserList, g);
			}
			Message.obtain(mLocalHandler, UPDATE_SEARCHED_USER_LIST,
					searchedUserList).sendToTarget();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

	};

	private OnItemClickListener itemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos,
				long id) {
			ListItem item = mItemList.get(pos);
			if (item.g != null) {
				((ContactGroupView) mItemList.get(pos).v)
						.doExpandedOrCollapse();
				Message.obtain(mLocalHandler, UPDATE_LIST_VIEW, pos, 0)
						.sendToTarget();
			} else {
				ContactUserView cuv = (ContactUserView) view;
				for (ListItem li : mItemList) {
					if (li.u != null
							&& li.u.getmUserId() == cuv.getUser().getmUserId()) {
						((ContactUserView) li.v).updateChecked();
					}
				}
				Message.obtain(mLocalHandler, UPDATE_ATTENDEES, item.u)
						.sendToTarget();
			}
		}

	};

	private OnClickListener confirmButtonListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			List<User> l = new ArrayList<User>(mAttendeeList);
			if (listener != null) {
				listener.requestInvitation(conf, l);
			}
		}

	};

	private DateTimePicker dtp;
	private OnTouchListener mDateTimePickerListener = new OnTouchListener() {

		@Override
		public boolean onTouch(final View view, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {

				if (dtp == null) {
					dtp = new DateTimePicker(mContext,
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					dtp.setOnDateSetListener(new OnDateSetListener() {

						@Override
						public void onDateTimeSet(int year, int monthOfYear,
								int dayOfMonth, int hour, int minute) {
							((EditText) view).setText(year
									+ "-"
									+ monthOfYear
									+ "-"
									+ dayOfMonth
									+ " "
									+ (hour < 10 ? ("0" + hour) : (hour + ""))
									+ ":"
									+ (minute < 10 ? ("0" + minute)
											: (minute + "")));
						}

					});
				}

				dtp.showAsDropDown(view);
				InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				mConfStartTimeET.setError(null);

			}
			return true;
		}

	};


	class LoadContactsAT extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mGroupList = GlobalHolder.getInstance().getGroup(GroupType.ORG);
			if (mGroupList != null) {
				for (Group g : mGroupList) {
					mItemList.add(new ListItem(g, g.getLevel()));
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			adapter.notifyDataSetChanged();
		}

	};

	class ListItem {
		long id;
		Group g;
		User u;
		View v;
		boolean isExpanded;
		int level;

		public ListItem(Group g, int level) {
			super();
			this.g = g;
			this.id = 0x02000000 | g.getmGId();
			this.v = new ContactGroupView(mContext, g, null);
			isExpanded = false;
			this.level = level;
		}

		public ListItem(User u, int level) {
			super();
			this.u = u;
			this.id = 0x03000000 | u.getmUserId();
			this.v = new ContactUserView(mContext, u);
			isExpanded = false;
			this.level = level;
		}

	}

	class ContactsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mItemList.size();
		}

		@Override
		public Object getItem(int position) {
			ListItem item = mItemList.get(position);
			return item.g == null ? item.u : item.g;
		}

		@Override
		public long getItemId(int position) {
			return mItemList.get(position).id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return mItemList.get(position).v;
		}

	}

	class LocalHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_LIST_VIEW:
				updateView(msg.arg1);
				break;
			case UPDATE_ATTENDEES:
				updateUserToAttendList((User) msg.obj);
				break;
			case UPDATE_SEARCHED_USER_LIST:
				updateSearchedUserList((List<User>) msg.obj);
				break;
			case CREATE_CONFERENC_RESP:
				JNIResponse rccr = (JNIResponse) msg.obj;
				if (rccr.getResult() != JNIResponse.Result.SUCCESS) {
					mErrorNotificationLayout.setVisibility(View.VISIBLE);
					break;
				}
				User currU = GlobalHolder.getInstance().getCurrentUser();
				ConferenceGroup g = new ConferenceGroup(
						((RequestConfCreateResponse) rccr).getConfId(),
						GroupType.CONFERENCE, conf.getName(),
						currU.getmUserId() + "", conf.getDate().getTime()
								/ 1000 + "", currU.getmUserId());
				g.setOwnerUser(currU);
				g.setChairManUId(currU.getmUserId());
				g.addUserToGroup(new ArrayList<User>(mAttendeeList));
				GlobalHolder.getInstance().addGroupToList(GroupType.CONFERENCE,
						g);
				Intent i = new Intent();
				i.putExtra("newGid", g.getmGId());
				i.setAction(PublicIntent.BROADCAST_NEW_CONFERENCE_NOTIFICATION);
				i.addCategory(PublicIntent.DEFAULT_CATEGORY);
				mContext.sendBroadcast(i);
				break;
			}
		}

	}

}