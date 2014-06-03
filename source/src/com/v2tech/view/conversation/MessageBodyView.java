package com.v2tech.view.conversation;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.v2tech.R;
import com.v2tech.util.V2Log;
import com.v2tech.vo.VImageMessage;
import com.v2tech.vo.VMessage;

public class MessageBodyView extends LinearLayout {

	private VMessage mMsg;

	private ImageView mHeadIcon;
	private LinearLayout mContentContainer;
	private ImageView mArrowIV;
	private TextView timeTV;

	private View rootView;

	private boolean isShowTime;

	private LinearLayout mLocalMessageContainter;

	private LinearLayout mRemoteMessageContainter;

	private TextView contentTV;
	private ImageView mImageIV;

	private Handler localHandler;
	private Runnable popupWindowListener = null;
	private PopupWindow pw;
	
	private ClickListener callback;
	
	
	public interface ClickListener {
		public void onMessageClicked(VMessage v);
	}

	public MessageBodyView(Context context, VMessage m) {
		this(context, m, false);
	}

	public MessageBodyView(Context context, VMessage m, boolean isShowTime) {
		super(context);
		this.mMsg = m;

		rootView = LayoutInflater.from(context).inflate(R.layout.message_body,
				null, false);
		this.isShowTime = isShowTime;
		this.localHandler = new Handler();
		initData();
	}

	private void initData() {
		if (rootView == null) {
			V2Log.e(" root view is Null can not initialize");
			return;
		}
		timeTV = (TextView) rootView.findViewById(R.id.message_body_time_text);
		if (isShowTime && mMsg.getDate() != null) {
			timeTV.setText(mMsg.getDateTimeStr());
		} else {
			timeTV.setVisibility(View.GONE);
		}

		mLocalMessageContainter = (LinearLayout) rootView
				.findViewById(R.id.message_body_local_user_ly);

		mRemoteMessageContainter = (LinearLayout) rootView
				.findViewById(R.id.message_body_remote_ly);

		if (!mMsg.isLocal()) {
			mHeadIcon = (ImageView) rootView
					.findViewById(R.id.conversation_message_body_icon_local);
			if (mMsg.getUser() != null && mMsg.getUser().getAvatarBitmap() != null) {
				mHeadIcon.setImageBitmap(mMsg.getUser().getAvatarBitmap());
			}
			mContentContainer = (LinearLayout) rootView
					.findViewById(R.id.messag_body_content_ly_local);
			mArrowIV = (ImageView) rootView
					.findViewById(R.id.message_body_arrow_left);
			mArrowIV.bringToFront();
			mLocalMessageContainter.setVisibility(View.VISIBLE);
			mRemoteMessageContainter.setVisibility(View.GONE);
		} else {
			mHeadIcon = (ImageView) rootView
					.findViewById(R.id.conversation_message_body_icon_remote);
			mContentContainer = (LinearLayout) rootView
					.findViewById(R.id.messag_body_content_ly_remote);
			mArrowIV = (ImageView) rootView
					.findViewById(R.id.message_body_arrow_right);
			mArrowIV.bringToFront();
			mLocalMessageContainter.setVisibility(View.GONE);
			mRemoteMessageContainter.setVisibility(View.VISIBLE);
			if (mMsg.getUser() != null && mMsg.getUser().getAvatarBitmap() != null) {
				mHeadIcon.setImageBitmap(mMsg.getUser().getAvatarBitmap());
			}
		}

		if (mMsg.getType() == VMessage.MessageType.TEXT) {
			contentTV = new TextView(this.getContext());
			contentTV.setText(mMsg.getText());
			contentTV.setTextColor(Color.GRAY);
			contentTV.setHighlightColor(Color.GRAY);
			mContentContainer.addView(contentTV, new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
		} else if (mMsg.getType() ==VMessage.MessageType.IMAGE) {
			mImageIV = new ImageView(this.getContext());
			Bitmap bm = ((VImageMessage)mMsg).getCompressedBitmap();
			mImageIV.setImageBitmap(bm);
			mContentContainer.addView(mImageIV,new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
		}

		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		this.addView(rootView, ll);

		if (mMsg.getType() == VMessage.MessageType.IMAGE) {
			mContentContainer.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (callback !=null) {
						callback.onMessageClicked(mMsg);
					}
				}
				
			}) ;
			
		} else {
			mContentContainer.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View anchor) {
					updateSelectedBg(true);
					if (mMsg.getType() == VMessage.MessageType.TEXT) {
						showPopupWindow(anchor);
					} else {
						updateSelectedBg(false);
					}
					if (callback !=null) {
						callback.onMessageClicked(mMsg);
					}
					return false;
				}

			});
		}
		

	}
	
	


	@Override
	public void setOverScrollMode(int overScrollMode) {
		// TODO Auto-generated method stub
		super.setOverScrollMode(overScrollMode);
	}

	@Override
	public void setWillNotDraw(boolean willNotDraw) {
		// TODO Auto-generated method stub
		super.setWillNotDraw(willNotDraw);
	}

	@Override
	@ExportedProperty(category = "drawing")
	public boolean willNotCacheDrawing() {
		// TODO Auto-generated method stub
		return super.willNotCacheDrawing();
	}

	@Override
	@ExportedProperty(category = "drawing")
	public boolean willNotDraw() {
		// TODO Auto-generated method stub
		return super.willNotDraw();
	}

	private void updateSelectedBg(boolean selected) {
		if (selected) {
			if (!mMsg.isLocal()) {
				mContentContainer
						.setBackgroundResource(R.drawable.message_body_bg_selected);
				mArrowIV.setImageResource(R.drawable.message_body_arrow_left_selected);
			}
		} else {
			if (!mMsg.isLocal()) {
				mContentContainer
						.setBackgroundResource(R.drawable.message_body_bg);
				mArrowIV.setImageResource(R.drawable.message_body_arrow_left);
			}
		}
	}

	private void showPopupWindow(final View anchor) {

		popupWindowListener = new Runnable() {

			@Override
			public void run() {
				if (!anchor.isShown()) {
					return;
				}
				if (pw == null) {
					LayoutInflater inflater = (LayoutInflater) getContext()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(
							R.layout.message_selected_pop_up_window, null);

					pw = new PopupWindow(view,
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT, true);
					pw.setBackgroundDrawable(new ColorDrawable(
							Color.TRANSPARENT));
					pw.setFocusable(true);
					pw.setTouchable(true);
					pw.setOutsideTouchable(true);

					TextView tv = (TextView) view
							.findViewById(R.id.contact_message_pop_up_item_copy);
					tv.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							ClipboardManager clipboard = (ClipboardManager) getContext()
									.getSystemService(Context.CLIPBOARD_SERVICE);
							ClipData clip = ClipData.newPlainText("label",
									mMsg.getText());
							clipboard.setPrimaryClip(clip);
							pw.dismiss();
							Toast.makeText(getContext(),
									R.string.contact_message_copy_message,
									Toast.LENGTH_SHORT).show();
						}

					});

				}
				int offsetX = (anchor.getMeasuredWidth() - 50) / 2;
				int offsetY = -(anchor.getMeasuredHeight() + 60);
			
				int[] location = new int[2];
				anchor.getLocationInWindow(location);
				if (location[1] <= 0) {
					Rect r = new Rect();
					anchor.getDrawingRect(r);
					Rect r1 = new Rect();
					anchor.getGlobalVisibleRect(r1);
					pw.showAtLocation((View)anchor.getParent(), Gravity.NO_GRAVITY, r1.left + (r.right - r.left)/2, r1.top );
				} else {
					pw.showAsDropDown(anchor, offsetX, offsetY);
				}
				updateSelectedBg(false);
				popupWindowListener = null;

			}

		};

		localHandler.postDelayed(popupWindowListener, 200);

	}

	public VMessage getItem() {
		return this.mMsg;
	}

	
	public void setCallback(ClickListener cl) {
		this.callback = cl;
	}
	
	public void recycle() {
		if (this.mMsg.getType() == VMessage.MessageType.IMAGE) {
			((VImageMessage)this.mMsg).recycle();
		}
	}

}
