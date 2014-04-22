package com.v2tech.view.conversation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.v2tech.R;
import com.v2tech.db.ContentDescriptor;
import com.v2tech.logic.GlobalHolder;
import com.v2tech.logic.User;
import com.v2tech.logic.VImageMessage;
import com.v2tech.logic.VMessage;
import com.v2tech.service.ChatService;
import com.v2tech.view.JNIService;
import com.v2tech.view.PublicIntent;
import com.v2tech.view.cus.ItemScrollView;
import com.v2tech.view.cus.ScrollViewListener;
import com.v2tech.view.vo.Conversation;

public class ConversationView extends Activity {

	private final int START_LOAD_MESSAGE = 1;
	private final int LOAD_MESSAGE = 2;
	private final int END_LOAD_MESSAGE = 3;
	private final int SEND_MESSAGE = 4;
	private final int SEND_MESSAGE_DONE = 5;
	private final int QUERY_NEW_MESSAGE = 6;

	private final int BATCH_COUNT = 10;

	private int offset = 0;

	private LinearLayout mMessagesContainer;

	private ItemScrollView mScrollView;

	private long user1Id;

	private long user2Id;

	private long groupId;

	private String user2Name;

	private LocalHandler lh;

	private BackendHandler backEndHandler;

	private boolean isLoading;

	private boolean mLoadedAllMessages;

	private boolean mIsInited;

	private Context mContext;

	private TextView mSendButtonTV;

	private TextView mReturnButtonTV;

	private EditText mMessageET;

	private ImageView mLoadingImg;

	private TextView mUserTitleTV;

	private ImageView mMoreFeatureIV;

	private View mAdditionFeatureContainer;

	private ImageView mSelectImageButtonIV;

	private ImageView mAudioSpeakerIV;

	private MessageReceiver receiver = new MessageReceiver();

	private ChatService mChat = new ChatService();

	private User local;
	private User remote;

	private Conversation mCurrentConv;

	private boolean isStopped;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_contact_message);
		mMessagesContainer = (LinearLayout) findViewById(R.id.conversation_message_list);

		mScrollView = (ItemScrollView) findViewById(R.id.conversation_message_list_scroll_view);
		mScrollView.setScrollListener(scrollListener);

		mSendButtonTV = (TextView) findViewById(R.id.message_send);
		// mSendButtonTV.setOnClickListener(sendMessageListener);
		mSendButtonTV.setOnTouchListener(sendMessageButtonListener);

		mMessageET = (EditText) findViewById(R.id.message_text);
		mMessageET.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				scrollToBottom();

			}

		});
		mReturnButtonTV = (TextView) findViewById(R.id.contact_detail_return_button);
		// mReturnButtonTV.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// mReturnButtonTV.setEnabled(false);
		// InputMethodManager imm = (InputMethodManager)
		// getSystemService(Context.INPUT_METHOD_SERVICE);
		// imm.hideSoftInputFromWindow(mMessageET.getWindowToken(), 0);
		// finish();
		// }
		//
		// });
		mReturnButtonTV.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View view, MotionEvent mv) {
				if (mv.getAction() == MotionEvent.ACTION_UP) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mMessageET.getWindowToken(), 0);
					finish();
				}
				return true;
			}

		});

		mMoreFeatureIV = (ImageView) findViewById(R.id.contact_message_plus);
		mMoreFeatureIV.setOnClickListener(moreFeatureButtonListenr);

		mSelectImageButtonIV = (ImageView) findViewById(R.id.contact_message_send_image_button);
		mSelectImageButtonIV.setOnClickListener(selectImageButtonListener);

		mAudioSpeakerIV = (ImageView) findViewById(R.id.contact_message_speaker);
		// TODO hidden button of send audio message
		mAudioSpeakerIV.setVisibility(View.GONE);

		mAdditionFeatureContainer = findViewById(R.id.contact_message_sub_feature_ly_inner);

		mUserTitleTV = (TextView) findViewById(R.id.message_user_title);

		user1Id = this.getIntent().getLongExtra("user1id", 0);
		user2Id = this.getIntent().getLongExtra("user2id", 0);
		groupId = this.getIntent().getLongExtra("gid", 0);
		user2Name = this.getIntent().getStringExtra("user2Name");

		local = GlobalHolder.getInstance().getUser(user1Id);
		remote = GlobalHolder.getInstance().getUser(user2Id);
		if (remote != null && remote.getName() != null) {
			user2Name = remote.getName();
		}

		lh = new LocalHandler();

		HandlerThread thread = new HandlerThread("back-end");
		thread.start();
		synchronized (thread) {
			while (!thread.isAlive()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			backEndHandler = new BackendHandler(thread.getLooper());
		}

		if (groupId != 0) {
			mCurrentConv = GlobalHolder.getInstance().findConversationByType(
					Conversation.TYPE_GROUP, groupId);
		} else {
			mCurrentConv = GlobalHolder.getInstance().findConversationByType(
					Conversation.TYPE_CONTACT, user2Id);
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(JNIService.JNI_BROADCAST_NEW_MESSAGE);
		filter.addCategory(JNIService.JNI_BROADCAST_CATEGROY);
		registerReceiver(receiver, filter);

		if (mCurrentConv != null) {
			notificateConversationUpdate(null, null);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mIsInited && !mLoadedAllMessages) {
			android.os.Message m = android.os.Message.obtain(lh,
					START_LOAD_MESSAGE);
			lh.sendMessageDelayed(m, 500);
		}

		if (user1Id == 0 || user2Id == 0) {
			Toast.makeText(this, R.string.error_contact_messag_invalid_user_id,
					Toast.LENGTH_SHORT).show();
		}
		mUserTitleTV.setText(user2Name);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.cancel(PublicIntent.MESSAGE_NOTIFICATION_ID);
		GlobalHolder.getInstance().CURRENT_CONVERSATION = mCurrentConv;
		isStopped = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (pending) {
			pending = false;
			scrollToBottom();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		isStopped = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(receiver);
		cleanCache();
		GlobalHolder.getInstance().CURRENT_CONVERSATION = null;
		;
	}

	private void scrollToBottom() {
		if (mMessagesContainer != null
				&& mMessagesContainer.getChildCount() > 0) {
			mScrollView.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScrollView.scrollTo(
							0,
							mMessagesContainer.getChildAt(
									mMessagesContainer.getChildCount() - 1)
									.getBottom());
				}
			}, 500);
		}
	}

	private void cleanCache() {
		for (int i = 0; i < mMessagesContainer.getChildCount(); i++) {
			View v = mMessagesContainer.getChildAt(i);
			if (v instanceof MessageBodyView) {
				((MessageBodyView) v).recycle();
			}
		}
	}

	private OnTouchListener sendMessageButtonListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View anchor, MotionEvent mv) {
			int action = mv.getAction();
			if (action == MotionEvent.ACTION_DOWN
					|| action == MotionEvent.ACTION_HOVER_ENTER) {
			} else if (action == MotionEvent.ACTION_UP) {
				doSendMessage();
			}
			return true;
		}

	};

	private OnClickListener moreFeatureButtonListenr = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mMoreFeatureIV.getTag() == null
					|| mMoreFeatureIV.getTag().equals("plus")) {
				mMoreFeatureIV.setImageResource(R.drawable.message_minus);
				mMoreFeatureIV.setTag("minus");
				mAdditionFeatureContainer.setVisibility(View.VISIBLE);
			} else {
				mMoreFeatureIV.setImageResource(R.drawable.message_plus);
				mMoreFeatureIV.setTag("plus");
				mAdditionFeatureContainer.setVisibility(View.GONE);
			}
		}

	};

	private static final int SELECT_PICTURE = 1;

	private OnClickListener selectImageButtonListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(
					Intent.createChooser(intent, "Select Picture"),
					SELECT_PICTURE);

		}

	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_PICTURE) {
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				if (filePath == null) {
					Toast.makeText(mContext,
							R.string.error_contact_messag_invalid_image_path,
							Toast.LENGTH_SHORT).show();
					return;
				}
				VImageMessage vim = new VImageMessage(local, remote, filePath,
						false);
				saveMessageToDB(vim);
				Message.obtain(lh, SEND_MESSAGE, vim).sendToTarget();
				addMessageToContainer(vim);

				notificateConversationUpdate(null, null);
			}
		}
	}

	private void saveMessageToDB(VMessage vm) {
		ContentValues cv = new ContentValues();
		cv.put(ContentDescriptor.Messages.Cols.FROM_USER_ID, user1Id);
		cv.put(ContentDescriptor.Messages.Cols.TO_USER_ID, user2Id);
		cv.put(ContentDescriptor.Messages.Cols.MSG_CONTENT, vm.getText());
		cv.put(ContentDescriptor.Messages.Cols.MSG_TYPE, vm.getType()
				.getIntValue());
		cv.put(ContentDescriptor.Messages.Cols.SEND_TIME, vm.getFullDateStr());
		Uri uri = getContentResolver().insert(
				ContentDescriptor.Messages.CONTENT_URI, cv);
		vm.setId(Long.parseLong(uri.getLastPathSegment()));
	}

	private void doSendMessage() {
		String content = mMessageET.getEditableText().toString();
		if (content == null || content.equals("")) {
			return;
		}
		content = removeEmoji(content);
		if (remote == null) {
			remote = new User(user2Id);
		}
		VMessage m = new VMessage(local, remote, content);
		m.mGroupId = groupId;
		saveMessageToDB(m);

		mMessageET.setText("");

		Message.obtain(lh, SEND_MESSAGE, m).sendToTarget();
		addMessageToContainer(m);
		// make offset
		offset++;

		notificateConversationUpdate(m.getText(), m.getDateTimeStr());

	}

	// FIXME optimize code
	private String removeEmoji(String content) {
		byte[] bys = new byte[] { -16, -97 };
		byte[] bc = content.getBytes();
		byte[] copy = new byte[bc.length];
		int j = 0;
		for (int i = 0; i < bc.length; i++) {
			if (i < bc.length - 2 && bys[0] == bc[i] && bys[1] == bc[i + 1]) {
				i += 3;
				continue;
			}
			copy[j] = bc[i];
			j++;
		}
		return new String(copy, 0, j);
	}

	private void notificateConversationUpdate(String content, String date) {
		Intent i = new Intent(PublicIntent.REQUEST_UPDATE_CONVERSATION);
		i.addCategory(PublicIntent.DEFAULT_CATEGORY);
		if (groupId == 0) {
			i.putExtra("extId", user2Id);
			i.putExtra("type", Conversation.TYPE_CONTACT);
		} else {
			i.putExtra("type", Conversation.TYPE_GROUP);
			i.putExtra("extId", groupId);
		}
		i.putExtra("date", date);
		i.putExtra("content", content);
		i.putExtra("noti", false);
		mContext.sendBroadcast(i);
	}

	private void addMessageToContainer(VMessage msg) {
		// Add message to container
		MessageBodyView mv = new MessageBodyView(mContext, msg, true);
		mv.setCallback(listener);
		mMessagesContainer.addView(mv);
		mScrollView.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	private ScrollViewListener scrollListener = new ScrollViewListener() {

		@Override
		public void onScrollBottom(ItemScrollView scrollView, int x, int y,
				int oldx, int oldy) {

		}

		@Override
		public void onScrollTop(ItemScrollView scrollView, int x, int y,
				int oldx, int oldy) {
			if (isLoading || mLoadedAllMessages) {
				return;
			}
			isLoading = true;
			android.os.Message m = android.os.Message.obtain(lh,
					START_LOAD_MESSAGE);
			lh.sendMessageDelayed(m, 500);
		}

	};

	private MessageBodyView.ClickListener listener = new MessageBodyView.ClickListener() {

		@Override
		public void onMessageClicked(VMessage v) {
			if (v.getType() == VMessage.MessageType.IMAGE) {
				Intent i = new Intent();
				i.addCategory(PublicIntent.DEFAULT_CATEGORY);
				i.setAction(PublicIntent.START_VIDEO_IMAGE_GALLERY);
				i.putExtra("uid1", user1Id);
				i.putExtra("uid2", user2Id);
				i.putExtra("cid", v.getId());
				mContext.startActivity(i);
			}
		}

	};

	private List<VMessage> loadMessages() {

		String selection = null;
		String[] args = null;

		if (this.groupId == 0) {
			selection = "(" + ContentDescriptor.Messages.Cols.FROM_USER_ID
					+ "=? and " + ContentDescriptor.Messages.Cols.TO_USER_ID
					+ "=? ) or " + "("
					+ ContentDescriptor.Messages.Cols.FROM_USER_ID + "=? and "
					+ ContentDescriptor.Messages.Cols.TO_USER_ID + "=? ) and "
					+ ContentDescriptor.Messages.Cols.GROUP_ID + "=0 ";
			args = new String[] { user1Id + "", user2Id + "", user2Id + "",
					user1Id + "" };
		} else if (this.groupId != 0) {
			selection = "(" + ContentDescriptor.Messages.Cols.GROUP_ID
					+ "=? ) ";
			args = new String[] {this.groupId + "" };
		}

		Cursor mCur = this.getContentResolver().query(
				ContentDescriptor.Messages.CONTENT_URI,
				ContentDescriptor.Messages.Cols.ALL_CLOS,
				selection,
				args,
				ContentDescriptor.Messages.Cols.SEND_TIME + " desc "
						+ " limit " + BATCH_COUNT + " offset " + offset);

		if (mCur.getCount() == 0) {
			mCur.close();
			mLoadedAllMessages = true;
			return null;
		}
		List<VMessage> array = new ArrayList<VMessage>();
		int count = 0;
		while (mCur.moveToNext()) {
			VMessage m = extractMsg(mCur);
			array.add(m);
			count++;
			offset++;
			if (count > BATCH_COUNT) {
				break;
			}
		}
		mCur.close();

		mIsInited = true;
		return array;
	}

	private boolean pending = false;

	private void queryAndAddMessage(final int mid) {
		Uri uri = ContentUris.withAppendedId(
				ContentDescriptor.Messages.CONTENT_URI, mid);

		Cursor mCur = this.getContentResolver().query(uri,
				ContentDescriptor.Messages.Cols.ALL_CLOS, null, null, null);
		MessageBodyView mv = null;
		while (mCur.moveToNext()) {
			VMessage m = extractMsg(mCur);
			if ((groupId == m.mGroupId && groupId !=0)
					|| (m.getUser().getmUserId() == user2Id && m.mGroupId == 0)) {
				mv = new MessageBodyView(this, m, true);
				mv.setCallback(listener);
				mMessagesContainer.addView(mv);
			}
		}
		mCur.close();
		if (mv != null) {
			final MessageBodyView fmv = mv;
			if (!isStopped) {
				mScrollView.post(new Runnable() {
					@Override
					public void run() {
						mScrollView.scrollTo(0, fmv.getBottom());
					}
				});
			} else {
				pending = true;
			}
		}
	}

	private VMessage extractMsg(Cursor cur) {
		if (cur.isClosed()) {
			throw new RuntimeException(" cursor is closed");
		}
		DateFormat dp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				Locale.getDefault());

		int id = cur.getInt(0);
		long localUserId = cur.getLong(1);
		long remoteUserId = cur.getLong(3);
		// msg_content column
		String content = cur.getString(5);
		// message type
		int type = cur.getInt(6);
		// date time
		String dateString = cur.getString(7);

		long groupId = cur.getLong(9);

		User lo = GlobalHolder.getInstance().getUser(localUserId);
		User ro = GlobalHolder.getInstance().getUser(remoteUserId);
		VMessage vm = null;
		if (type == VMessage.MessageType.TEXT.getIntValue()) {
			vm = new VMessage(lo, ro, content, localUserId == user2Id);
		} else {
			vm = new VImageMessage(lo, ro, content.split("\\|")[4],
					localUserId == user2Id);
		}
		vm.setId(id);
		vm.mGroupId = groupId;
		try {
			vm.setDate(dp.parse(dateString));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return vm;

	}

	class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (JNIService.JNI_BROADCAST_NEW_MESSAGE.equals(intent.getAction())) {
				Message.obtain(lh, QUERY_NEW_MESSAGE,
						intent.getExtras().get("mid")).sendToTarget();
			}
		}

	}

	class BackendHandler extends Handler {

		public BackendHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_MESSAGE:
				List<VMessage> array = loadMessages();
				android.os.Message.obtain(lh, END_LOAD_MESSAGE, array)
						.sendToTarget();
				break;
			}
		}

	}

	class LocalHandler extends Handler {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case START_LOAD_MESSAGE:
				if (mLoadingImg == null) {
					mLoadingImg = new ImageView(mContext);
					mLoadingImg.setImageResource(R.drawable.loading);
				}
				mMessagesContainer.addView(mLoadingImg, 0);

				android.os.Message.obtain(backEndHandler, LOAD_MESSAGE)
						.sendToTarget();
				break;
			case LOAD_MESSAGE:
				loadMessages();
				android.os.Message.obtain(this, END_LOAD_MESSAGE)
						.sendToTarget();
				break;
			case END_LOAD_MESSAGE:
				mMessagesContainer.removeView(mLoadingImg);
				final List<VMessage> array = (List<VMessage>) msg.obj;
				MessageBodyView fir = null;
				for (int i = 0; array != null && i < array.size(); i++) {
					MessageBodyView mv = new MessageBodyView(mContext,
							array.get(i), true);
					mv.setCallback(listener);
					if (fir == null) {
						fir = mv;
					}
					mMessagesContainer.addView(mv, 0);
				}

				if (fir != null) {
					final MessageBodyView firt = fir;
					mScrollView.post(new Runnable() {
						@Override
						public void run() {
							mScrollView.scrollTo(0, firt.getBottom());
						}
					});
				}

				isLoading = false;
				break;
			case SEND_MESSAGE:
				mChat.sendVMessage((VMessage) msg.obj,
						Message.obtain(this, SEND_MESSAGE_DONE));
				// mService.sendMessage((VMessage) msg.obj,
				// Message.obtain(this, SEND_MESSAGE_DONE));
				break;
			case QUERY_NEW_MESSAGE:
				if (msg.obj == null || "".equals(msg.obj.toString())) {
					break;
				}
				queryAndAddMessage(Integer.parseInt(msg.obj.toString()));
				break;
			}
		}

	}

}
