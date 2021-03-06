package com.bizcom.vc.activity.conversation;

import java.math.BigDecimal;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bizcom.vo.FileInfoBean;
import com.v2tech.R;

public class ConversationSelectFileEntry extends Activity implements
		OnClickListener {

	private static final int CANCEL = 0;
	private static final int NORMAL_SELECT_FILE = 1;
	private static final int SEND_SELECT_FILE = 3;

	private RelativeLayout entryImage;
	private RelativeLayout entryFile;
	private TextView backKey;
	private ArrayList<FileInfoBean> mCheckedList;

	private TextView selectedFileSize;
	private TextView sendButton;
	private long totalSize;
	private int lastSize; // 通过该变量来判断是否要累加totalSize
	private long uid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectfile_entry);

		findview();
		mCheckedList = new ArrayList<FileInfoBean>();
		uid = getIntent().getLongExtra("uid", -1);
	}

	private void findview() {

		entryImage = (RelativeLayout) findViewById(R.id.selectfile_entry_image);
		entryFile = (RelativeLayout) findViewById(R.id.selectfile_entry_file);
		backKey = (TextView) findViewById(R.id.selectfile_back);

		selectedFileSize = (TextView) findViewById(R.id.selectfile_entry_size);
		sendButton = (TextView) findViewById(R.id.selectfile_message_send);
		sendButton.setBackgroundResource(R.drawable.button_bg_noable);
		entryImage.setOnClickListener(this);
		entryFile.setOnClickListener(this);
		backKey.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		Intent intent = new Intent(this, ConversationSelectFile.class);
		intent.putParcelableArrayListExtra("checkedFiles", mCheckedList);
		switch (v.getId()) {
		case R.id.selectfile_entry_image:
			intent.putExtra("type", "image");
			intent.putExtra("uid", uid);
			startActivityForResult(intent, NORMAL_SELECT_FILE);
			break;
		case R.id.selectfile_entry_file:
			intent.putExtra("type", "file");
			intent.putExtra("uid", uid);
			startActivityForResult(intent, NORMAL_SELECT_FILE);
			break;
		case R.id.selectfile_back:
			setResult(1000);
			mCheckedList.clear();
			onBackPressed();
			break;
		case R.id.selectfile_message_send:
			Intent sendIntent = new Intent();
			sendIntent
					.putParcelableArrayListExtra("checkedFiles", mCheckedList);
			setResult(1000, sendIntent);
			onBackPressed();
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case NORMAL_SELECT_FILE:
			mCheckedList = data.getParcelableArrayListExtra("checkedFiles");
			if (mCheckedList != null && mCheckedList.size() > 0
					&& mCheckedList.size() != lastSize) {

				totalSize = 0;
				lastSize = mCheckedList.size();
				sendButton.setClickable(true);
				sendButton.setOnClickListener(this);
				sendButton.setTextColor(Color.WHITE);
				sendButton
						.setBackgroundResource(R.drawable.conversation_selectfile_send_able);
				for (FileInfoBean bean : mCheckedList) {

					totalSize += bean.fileSize;
				}
				sendButton.setText(String.format(
						getResources().getString(
								R.string.conversation_select_file_send),
						mCheckedList.size()));
				selectedFileSize.setText(getResources().getString(
						R.string.conversation_select_file_entry_chosen)
						+ getFileSize(totalSize));
				sendButton.setText(String.format(
						getResources().getString(
								R.string.conversation_select_file_entry_send),
						mCheckedList.size()));

			} else if (mCheckedList != null && mCheckedList.size() == 0) {
				lastSize = 0;
				sendButton.setClickable(false);
				sendButton.setOnClickListener(this);
				sendButton.setTextColor(Color.GRAY);
				sendButton.setBackgroundResource(R.drawable.button_bg_noable);
				selectedFileSize
						.setText(R.string.conversation_select_file_entry_selected);
				sendButton
						.setText(R.string.conversation_select_file_entry_send_out);
			}
			break;
		case CANCEL:
			setResult(1000);
			onBackPressed();
			break;
		case SEND_SELECT_FILE:
			mCheckedList = data.getParcelableArrayListExtra("checkedFiles");
			Intent sendIntent = new Intent();
			sendIntent
					.putParcelableArrayListExtra("checkedFiles", mCheckedList);
			setResult(1000, sendIntent);
			onBackPressed();
			break;
		default:
			break;
		}
	}

	/**
	 * 获取文件大小
	 * 
	 * @param totalSpace
	 * @return
	 */
	private static String getFileSize(long totalSpace) {

		BigDecimal filesize = new BigDecimal(totalSpace);
		BigDecimal megabyte = new BigDecimal(1024 * 1024);
		float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
				.floatValue();
		if (returnValue > 1)
			return (returnValue + "MB");
		BigDecimal kilobyte = new BigDecimal(1024);
		returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
				.floatValue();
		return (returnValue + "  KB ");
	}

}
