package com.v2tech.view.conversation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.spoledge.aacplayer.Decoder;
import com.v2tech.R;
import com.v2tech.util.StorageUtil;
import com.v2tech.util.V2Log;
import com.v2tech.view.bo.ConversationNotificationObject;

public class ConversationSelectFile extends Activity {

	protected static final String TAG = "ConversationSelectFile";
	private String mCurrentPath = StorageUtil.getSdcardPath();
	private TextView backButton;
	private TextView finishButton;
	private TextView titleText;
	private TextView selectedFileSize;
	private TextView sendButton;

	private ListView filesList;
	private GridView filesGrid;

	private int totalSize = 0;

	private ArrayList<FileInfoBean> mFileLists;
	private ArrayList<FileInfoBean> mFolderLists;
	private ArrayList<FileInfoBean> mCheckedList;
	private ArrayList<String> mCheckedNameList;
	private LruCache<String, Bitmap> bitmapLru;

	private FileListAdapter adapter;
	private ImageListAdapter imageAdapter;
	private ConversationNotificationObject cov;
	private String type;
	private int mScreenHeight;
	private int mScreenWidth;
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			loading.setVisibility(View.GONE);
			imageAdapter = new ImageListAdapter();
			filesGrid.setAdapter(imageAdapter);
		};
	};
	private LinearLayout loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectfile);
		findview();
		init();
		setListener();
		Display display = getWindowManager().getDefaultDisplay();
		mScreenHeight = display.getHeight();
		mScreenWidth = display.getWidth();
	}

	private void findview() {

		titleText = (TextView) findViewById(R.id.selectfile_title);
		selectedFileSize = (TextView) findViewById(R.id.selectfile_entry_size);
		sendButton = (TextView) findViewById(R.id.selectfile_message_send);
		backButton = (TextView) findViewById(R.id.selectfile_back);
		finishButton = (TextView) findViewById(R.id.selectfile_finish);
		loading = (LinearLayout) findViewById(R.id.selectfile_loading);

		filesGrid = (GridView) findViewById(R.id.selectfile_gridview);
		filesList = (ListView) findViewById(R.id.selectfile_lsitview);

	}

	private void init() {

		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int mCacheSize = maxMemory / 8;
		bitmapLru = new LruCache<String, Bitmap>(mCacheSize);
		mCheckedNameList = new ArrayList<String>();
		Intent intent = getIntent();
		cov = intent.getParcelableExtra("obj");
		mCheckedList = intent.getParcelableArrayListExtra("checkedFiles");

		type = intent.getStringExtra("type");
		if ("image".equals(type)) {
			titleText.setText("图片");
			filesList.setVisibility(View.GONE);
			filesGrid.setVisibility(View.VISIBLE);
			new Thread(new Runnable() {

				@Override
				public void run() {
					loading.setVisibility(View.VISIBLE);
					getSdcardImages();
					handler.sendEmptyMessage(0);

				}
			}).start();

		} else if ("file".equals(type)) {
			titleText.setText("文件");
			filesGrid.setVisibility(View.GONE);
			filesList.setVisibility(View.VISIBLE);

			updateFileItems(mCurrentPath);
			adapter = new FileListAdapter();
			filesList.setAdapter(adapter);
		}

		if (mCheckedList.size() > 0) {
			sendButton.setClickable(true);
			sendButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					reutrnConversationView();
					mCheckedList.clear();
					mCheckedNameList.clear();
				}
			});
			sendButtonBackground();
			for (FileInfoBean bean : mCheckedList) {

				mCheckedNameList.add(bean.fileName);
				totalSize += bean.fileSize;
			}
			selectedFileSize.setText("已选" + getFileSize(totalSize));
			sendButton.setText("发送(" + mCheckedList.size() + ")");
		}
	}

	private void sendButtonBackground() {
		sendButton
				.setBackgroundResource(R.drawable.conversation_selectfile_send_able);
	}

	private void setListener() {

		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				V2Log.d(TAG, "当前文件路径：" + mCurrentPath);
				if (StorageUtil.getSdcardPath()
						.equals(mCurrentPath.substring(0,
								mCurrentPath.lastIndexOf("/")))) {
					backButton.setText("返回");
				}

				if (StorageUtil.getSdcardPath().equals(mCurrentPath)) {

					// 如果已经是顶级路径，则结束掉当前界面
					Intent intent = new Intent(ConversationSelectFile.this,
							ConversationSelectFileEntry.class);
					intent.putExtra("obj", cov);
					intent.putParcelableArrayListExtra("checkedFiles",
							mCheckedList);
					startActivity(intent);
					mCheckedList.clear();
					mFileLists.clear();
					mCheckedNameList.clear();
					if ("file".equals(type)) {

						mFolderLists.clear();
					}
					finish();
					return;
				}
				File file = new File(mCurrentPath);
				mCurrentPath = file.getParent();
				updateFileItems(file.getParent());
				adapter.notifyDataSetChanged();
			}
		});

		finishButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mCheckedList.clear();
				mCheckedNameList.clear();
				reutrnConversationView();
			}

		});

		filesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				if (position >= mFolderLists.size()) { // 文件

					notifyListChange(view, position);

				} else { // position小于文件夹集合mFolderLists的长度，则就是文件夹

					FileInfoBean bean = mFolderLists.get(position);
					backButton.setText("上一级");
					mCurrentPath = bean.filePath;
					V2Log.d(TAG, "当前文件路径：" + mCurrentPath);
					updateFileItems(bean.filePath);
				}
				adapter.notifyDataSetChanged();
			}

		});

		filesGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				notifyListChange(view, position);
				imageAdapter.notifyDataSetChanged();
			}
		});

	}

	/**
	 * @description:通过contentprovider获得sd卡上的图片
	 * @return:void
	 */
	private void getSdcardImages() {

		if (mFileLists == null) {
			mFileLists = new ArrayList<FileInfoBean>();
		} else {
			mFileLists.clear();
		}
		// 指定要查询的uri资源
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		// 获取ContentResolver
		ContentResolver contentResolver = getContentResolver();
		// 查询的字段
		String[] projection = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE };
		// 条件
		String selection = MediaStore.Images.Media.MIME_TYPE + "=?";
		// 条件值(這裡的参数不是图片的格式，而是标准，所有不要改动)
		String[] selectionArgs = { "image/jpeg" };
		// 排序
		String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";
		// 查询sd卡上的图片
		Cursor cursor = contentResolver.query(uri, projection, selection,
				selectionArgs, sortOrder);
		if (cursor != null) {
			FileInfoBean bean = null;
			cursor.moveToFirst();
			while (cursor.moveToNext()) {
				bean = new FileInfoBean();
				// 获得图片uri
				bean.filePath = cursor.getString(cursor
						.getColumnIndex(MediaStore.Images.Media.DATA));
				bean.fileSize = Long.valueOf(cursor.getString(cursor
						.getColumnIndex(MediaStore.Images.Media.SIZE)));
				bean.fileName = cursor.getString(cursor
						.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
				mFileLists.add(bean);
			}
			// 关闭cursor
			cursor.close();
		}
	}

	/**
	 * 根据文件路径更新当前ListView
	 * 
	 * @param path
	 */
	private void updateFileItems(String path) {

		if (mFileLists == null) {
			mFileLists = new ArrayList<FileInfoBean>();
			mFolderLists = new ArrayList<FileInfoBean>();
		} else {
			mFileLists.clear();
			mFolderLists.clear();
		}

		File[] files = folderScan(path);
		if (files == null) {
			return;
		}

		FileInfoBean file = null;
		File currentFile;
		for (int i = 0; i < files.length; i++) {

			if (files[i].isHidden() && !files[i].canRead()) {
				continue;
			}

			currentFile = files[i];
			file = new FileInfoBean();

			file.fileName = currentFile.getName();
			file.filePath = currentFile.getAbsolutePath();
			file.fileSize = currentFile.length();
			if (currentFile.isDirectory()) {
				mFolderLists.add(file);
				file.isDir = true;
			} else {
				mFileLists.add(file);
				file.isDir = false;
			}
		}
	}

	/**
	 * 获得当前路径的所有文件
	 * 
	 * @param path
	 * @return
	 */
	private File[] folderScan(String path) {

		File file = new File(path);
		File[] files = file.listFiles();
		return files;
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

	class FileListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mFileLists.size() + mFolderLists.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;
			if (convertView == null) {

				holder = new ViewHolder();
				convertView = View.inflate(ConversationSelectFile.this,
						R.layout.activity_selectfile_adapter, null);
				holder.fileIcon = (ImageView) convertView
						.findViewById(R.id.selectfile_adapter_icon);
				holder.fileFolderName = (TextView) convertView
						.findViewById(R.id.selectfile_adapter_folderName);
				holder.fileCheck = (CheckBox) convertView
						.findViewById(R.id.selectfile_adapter_checkbox);
				holder.fileArrow = (ImageView) convertView
						.findViewById(R.id.selectfile_adapter_arrow);
				convertView.setTag(holder);
			} else {

				holder = (ViewHolder) convertView.getTag();
			}

			if (position >= mFolderLists.size()) {

				if (holder.fileCheck.isChecked()) {
					holder.fileCheck.setChecked(true);
				} else {
					holder.fileCheck.setChecked(false);
				}
				holder.fileCheck.setVisibility(View.VISIBLE);
				holder.fileIcon.setVisibility(View.GONE);
				holder.fileArrow.setVisibility(View.GONE);
				holder.fileFolderName.setText(mFileLists.get(position
						- mFolderLists.size()).fileName);
			} else {
				holder.fileFolderName
						.setText(mFolderLists.get(position).fileName);
				holder.fileCheck.setVisibility(View.GONE);
				holder.fileIcon.setVisibility(View.VISIBLE);
				holder.fileArrow.setVisibility(View.VISIBLE);
			}

			return convertView;
		}
	}

	class ImageListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mFileLists.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;
			if (convertView == null) {

				holder = new ViewHolder();
				convertView = View.inflate(ConversationSelectFile.this,
						R.layout.activity_imagefile_adapter, null);
				holder.fileIcon = (ImageView) convertView
						.findViewById(R.id.selectfile_adapter_image);
				holder.fileCheck = (CheckBox) convertView
						.findViewById(R.id.selectfile_adapter_check);
				convertView.setTag(holder);
			} else {

				holder = (ViewHolder) convertView.getTag();
			}

			LayoutParams para = holder.fileIcon.getLayoutParams();
			para.height = mScreenHeight / 4;// 一屏幕显示8行
			para.width = (mScreenWidth - 20) / 3;// 一屏显示两列
			holder.fileIcon.setLayoutParams(para);

			try {

				Bitmap bit = bitmapLru.get(mFileLists.get(position).fileName);
				if (bit == null) {

					bitmapLru.put(mFileLists.get(position).fileName,
							handlerImage(mFileLists.get(position).filePath));
				}
				holder.fileIcon.setImageBitmap(bitmapLru.get(mFileLists
						.get(position).fileName));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (mCheckedNameList.contains(mFileLists.get(position).fileName)) {

				holder.fileCheck.setVisibility(View.VISIBLE);

			} else {
				holder.fileCheck.setVisibility(View.INVISIBLE);
			}

			if (holder.fileCheck.isChecked()) {
				holder.fileCheck.setChecked(true);
				holder.fileCheck.setVisibility(View.VISIBLE);
			} else {
				holder.fileCheck.setChecked(false);
				holder.fileCheck.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}
	}

	class ViewHolder {

		public CheckBox fileCheck;
		public TextView fileFolderName;
		public ImageView fileArrow;
		public ImageView fileIcon;

	}

	private void reutrnConversationView() {
		mFileLists.clear();
		if(mFolderLists != null){
			
			mFolderLists.clear();
		}
		Intent intent = new Intent(ConversationSelectFile.this,
				ConversationView.class);
		intent.putParcelableArrayListExtra("checkedFiles", mCheckedList);
		intent.putExtra("obj", cov);
		startActivity(intent);
		finish();
	}

	/**
	 * item点击事件，来更新List显示，并添加到集合
	 */
	private void notifyListChange(View view, int position) {
		// 文件
		FileInfoBean bean = null;
		CheckBox button;

		if ("image".equals(type)) {

			bean = mFileLists.get(position);
			button = (CheckBox) view
					.findViewById(R.id.selectfile_adapter_check);
		} else {

			bean = mFileLists.get(position - mFolderLists.size());
			button = (CheckBox) view
					.findViewById(R.id.selectfile_adapter_checkbox);
		}

		// 判断当前item被选择的状态
		if (button.isChecked()) {
			button.setChecked(false);
			mCheckedList.remove(bean);
			totalSize -= bean.fileSize;
			if (mCheckedList.size() == 0) {
				sendButton.setBackgroundResource(R.drawable.button_bg_noable);
				sendButton.setClickable(false);
			}
		} else {
			// 如果当前item没有被选中，则进一步判断一下当前mCheckedList长度是否为0，如果为0则变为可点击
			if (mCheckedList.size() == 0) {

				sendButton.setClickable(true);
				sendButtonBackground();
				sendButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						reutrnConversationView();
						mCheckedList.clear();
						mCheckedNameList.clear();
					}
				});
			}
			button.setChecked(true);
			mCheckedList.add(bean);
			mCheckedNameList.add(bean.fileName);
			totalSize += bean.fileSize;
		}
		selectedFileSize.setText("已选 " + getFileSize(totalSize));
		sendButton.setText("发送(" + mCheckedList.size() + ")");

	}

	/**
	 * 对图片进行压缩
	 * 
	 * @param path
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Bitmap handlerImage(String path) throws FileNotFoundException,
			IOException {

		Options options = new Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false;

		int width = options.outWidth;
		int height = options.outHeight;

		int widthScale = width / mScreenWidth;
		int heightScale = height / mScreenHeight;

		int scale = 1;
		if (widthScale > heightScale)
			scale = widthScale;
		else
			scale = heightScale;

		if (scale < 0) {
			scale = 1;
		}

		options.inSampleSize = scale;
		options.inInputShareable = true;// 。当系统内存不够时候图片自动被回收
		return BitmapFactory.decodeFile(path, options);
	}
}