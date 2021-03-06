package com.bizcom.vc.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bizcom.vc.application.GlobalHolder;
import com.bizcom.vc.widget.ContactUserView;
import com.bizcom.vo.User;
import com.v2tech.R;

public class CreateConfOrCrowdAdapter extends BaseAdapter {

	private static final int PAD_LAYOUT = 1;
	private static final int PHONE_LAYOUT = 0;
	
	private Context mContext;
	private int landLayout;
	private List<User> mUserListArray;

	public CreateConfOrCrowdAdapter(Context mContext , List<User> mUserListArray , int landLayout) {
		this.mUserListArray = mUserListArray;
		this.landLayout = landLayout;
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		return mUserListArray.size();
	}

	@Override
	public Object getItem(int position) {
		return mUserListArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mUserListArray.get(position).getmUserId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewTag tag = null;
		User user = mUserListArray.get(position);
		if (convertView == null) {
			tag = new ViewTag();

			if (landLayout == PAD_LAYOUT) {
				convertView = new ContactUserView(mContext, user, false);
				tag.headIcon = ((ContactUserView) convertView).getmPhotoIV();
				tag.name = ((ContactUserView) convertView).getmUserNameTV();
			} else {
				convertView = getAttendeeView(tag, user);
			}
			convertView.setTag(tag);

		} else {
			tag = (ViewTag) convertView.getTag();
		}

		updateView(tag, user);
		return convertView;
	}

	private void updateView(ViewTag tag, User user) {

		if (user.getAvatarBitmap() != null) {
			tag.headIcon.setImageBitmap(user.getAvatarBitmap());
		} else {
			tag.headIcon.setImageResource(R.drawable.avatar);
		}

		tag.name.setText(user.getDisplayName());
	}

	private View getAttendeeView(ViewTag tag, final User u) {
		final LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER);

		ImageView iv = new ImageView(mContext);
		tag.headIcon = iv;
		if (u.getAvatarBitmap() != null) {
			iv.setImageBitmap(u.getAvatarBitmap());
		} else {
			iv.setImageResource(R.drawable.avatar);
		}
		ll.addView(iv, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		TextView tv = new TextView(mContext);
		tag.name = tv;
		
		tv.setText(u.getDisplayName());
		tv.setEllipsize(TruncateAt.END);
		tv.setSingleLine(true);
		tv.setTextSize(8);
		tv.setMaxWidth(80);
		ll.addView(tv, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		ll.setPadding(5, 5, 5, 5);
		return ll;
	}
	
	class ViewTag {
		ImageView headIcon;
		TextView name;
	}
}
