package com.v2tech.view.conference;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.v2tech.R;
import com.v2tech.logic.User;
import com.v2tech.util.V2Log;

public class ContactUserView extends LinearLayout {

	private User mUser;

	private ImageView mPhotoIV;
	private TextView mUserNameTV;
	private TextView mUserSignatureTV;
	private ImageView mStatusIV;
	private CheckBox mCheckbox;

	private RelativeLayout contentContainer;

	private int padding = 0;

	public ContactUserView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ContactUserView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ContactUserView(Context context) {
		super(context);
	}

	public ContactUserView(Context context, User u) {
		super(context);
		initData(u);
	}

	public void initData(User u) {
		if (u == null || u.getmUserId() <= 0) {
			throw new RuntimeException("Invalid user data");
		}
		this.mUser = u;

		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.conf_create_contacts_user_view, null, false);

		contentContainer = (RelativeLayout) view
				.findViewById(R.id.contact_user_view_root);

		mPhotoIV = (ImageView) view.findViewById(R.id.contact_user_img);
		if (u.getAvatarBitmap() != null) {
			mPhotoIV.setImageBitmap(u.getAvatarBitmap());
		}
		mUserNameTV = (TextView) view.findViewById(R.id.contact_user_name);
		mUserSignatureTV = (TextView) view
				.findViewById(R.id.contact_user_signature);

		mUserNameTV.setText(mUser.getName());
		mUserSignatureTV.setText(mUser.getSignature() == null ? "" : mUser
				.getSignature());

		mCheckbox = (CheckBox) view.findViewById(R.id.conf_create_contact_view_ck);
		mStatusIV = (ImageView) view.findViewById(R.id.contact_user_status_iv);
		updateStatus(u.getmStatus());

		this.setPadding((u.getFirstBelongsGroup() == null ? 2 : u
				.getFirstBelongsGroup().getLevel() + 1) * 5, this
				.getPaddingTop(), this.getPaddingRight(), this
				.getPaddingRight());

		padding = (u.getFirstBelongsGroup() == null ? 2 : u
				.getFirstBelongsGroup().getLevel() + 1) * 35;

		contentContainer.setPadding(padding, contentContainer.getPaddingTop(),
				contentContainer.getPaddingRight(),
				contentContainer.getPaddingRight());

		this.addView(view, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

	}

	
	public User getUser() {
		return this.mUser;
	}

	public void updateStatus(User.Status st) {
		switch (st) {
		case ONLINE:
			mStatusIV.setImageResource(R.drawable.online);
			break;
		case LEAVE:
			mStatusIV.setImageResource(R.drawable.leave);
			break;
		case BUSY:
			mStatusIV.setImageResource(R.drawable.busy);
			break;
		case DO_NOT_DISTURB:
			mStatusIV.setImageResource(R.drawable.do_not_distrub);
			break;
		default:
			break;
		}
		if (st == User.Status.OFFLINE || st == User.Status.HIDDEN) {
			mStatusIV.setVisibility(View.GONE);
		} else {
			mStatusIV.setVisibility(View.VISIBLE);
		}
	}

	public void updateAvatar(Bitmap bt) {
		if (bt == null) {
			V2Log.w(" Invalid bitmap");
			return;
		}
		mPhotoIV.setImageBitmap(bt);
	}

	public void updateSign() {
		mUserSignatureTV.setText(this.mUser.getSignature());
		mUserNameTV.setText(mUser.getName());
	}

	public void removePadding() {
		contentContainer.setPadding(0, contentContainer.getPaddingTop(),
				contentContainer.getPaddingRight(),
				contentContainer.getPaddingRight());
		this.setPadding(0, this.getPaddingTop(), this.getPaddingRight(),
				this.getPaddingRight());
	}

	public void setPadding() {
		contentContainer.setPadding(padding, contentContainer.getPaddingTop(),
				contentContainer.getPaddingRight(),
				contentContainer.getPaddingRight());
	}
	
	public void updateChecked() {
		this.mCheckbox.setChecked(!this.mCheckbox.isChecked());
	}
	
	public boolean isChecked() {
		return this.mCheckbox.isChecked();
	}

}
