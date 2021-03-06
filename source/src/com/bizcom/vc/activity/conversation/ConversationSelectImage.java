package com.bizcom.vc.activity.conversation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.V2.jni.util.V2Log;
import com.bizcom.util.BitmapUtil;
import com.bizcom.util.DensityUtils;
import com.bizcom.vc.application.GlobalConfig;
import com.bizcom.vo.FileInfoBean;
import com.v2tech.R;

public class ConversationSelectImage extends Activity {

	private static final String TAG = "ConversationSelectImage";
	protected static final int CANCEL_SELECT_PICTURE = 0;
	private static final int SCROLL_STATE_TOUCH_SCROLL = 1;
	private static final int UPDATE_BITMAP = 3;
	protected static final int SCAN_SDCARD = 4;

	private static final int DEFAULT_PATH = 0x0001;
	private int DEFAULT_PATH_INDEX;
	private int SDCARD_PATH_INDEX;
	private String SDCARD_ROOT_NAME;
	private String SDCARD_ROOT;

	private RelativeLayout buttomTitle;
	private LinearLayout buttomDivider;
	private LinearLayout loading;
	private TextView backButton;
	private TextView finishButton;
	private TextView title;
	private GridView gridViews;
	private ListView listViews;
	private ImageListAdapter imageAdapter;
	private ImageClassifyAdapter classifyAdapter;
	private ArrayList<String> pictresClassify;
	private ArrayList<FileInfoBean> pictures;
	private Context mContext;
	private long remoteID;
	private boolean isClassify = true;
	private boolean isBack;
	private HashMap<String, ArrayList<FileInfoBean>> listMap;
	private String[][] selectArgs = {
			{ String.valueOf(MediaStore.Images.Media.INTERNAL_CONTENT_URI),
					"image/png" },
			{ String.valueOf(MediaStore.Images.Media.INTERNAL_CONTENT_URI),
					"image/jpeg" },
			{ String.valueOf(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
					"image/png" },
			{ String.valueOf(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
					"image/jpeg" } };
	private final int LRU_MAX_MEMORY = (int) ((Runtime.getRuntime().maxMemory()) / 8);
	private LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(
			LRU_MAX_MEMORY) {

		@Override
		protected void entryRemoved(boolean evicted, String key,
				Bitmap oldValue, Bitmap newValue) {
			if (key != null) {
				if (lruCache != null) {
					Bitmap bm = lruCache.remove(key);
					if (bm != null && !bm.isRecycled()) {
						bm.recycle();
						bm = null;
					}
				}
			}
		}
	};

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case UPDATE_BITMAP:
				ViewHolder holder = (ViewHolder) ((Object[]) msg.obj)[0];
				Bitmap bt = (Bitmap) ((Object[]) msg.obj)[1];
				FileInfoBean fb = (FileInfoBean) ((Object[]) msg.obj)[2];
				if (!bt.isRecycled()) {
					holder.fileIcon.setImageBitmap(bt);
				} else {
					startLoadBitmap(holder, fb);
				}
				break;
			case SCAN_SDCARD:
				loading.setVisibility(View.GONE);
				classifyAdapter = new ImageClassifyAdapter();
				listViews.setAdapter(classifyAdapter);
				break;
			}
		}
	};
	protected int isLoading;
	private ExecutorService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectfile);
		findview();
		init();
		setListener();
		initPath();
		mContext = this;
	}

	@Override
	public void onBackPressed() {
		if (isBack) {
			listViews.setVisibility(View.VISIBLE);
			gridViews.setVisibility(View.GONE);
			classifyAdapter = new ImageClassifyAdapter();
			listViews.setAdapter(classifyAdapter);
			isBack = false;
			isClassify = true;
		} else {
			setResult(Activity.RESULT_CANCELED);
			super.onBackPressed();
		}
	}

	private void initPath() {
		boolean sdExist = android.os.Environment.MEDIA_MOUNTED
				.equals(android.os.Environment.getExternalStorageState());
		if (sdExist) {
			String sdPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
			String[] split = sdPath.split("/");
			SDCARD_PATH_INDEX = split.length;
			SDCARD_ROOT = split[1];
			SDCARD_ROOT_NAME = split[2];
			V2Log.d(TAG, "sdcard path : " + sdPath);
			V2Log.d(TAG, "sdcard SDCARD_ROOT_NAME : " + SDCARD_ROOT_NAME);
		}

		// String defPath = getApplicationContext().getFilesDir().getParent();
		// String[] split = defPath.split("/");
		// V2Log.d(TAG, "internal path : " + defPath);
		// V2Log.d(TAG, "internal root name : " + split[split.length - 1]);
		DEFAULT_PATH_INDEX = 2;
	}

	private void findview() {

		title = (TextView) findViewById(R.id.ws_common_activity_title_content);
		title.setText(R.string.conversation_select_image_file_title);
		backButton = (TextView) findViewById(R.id.ws_common_activity_title_left_button);
		backButton.setText(R.string.common_return_name);
		finishButton = (TextView) findViewById(R.id.ws_common_activity_title_right_button);
		finishButton.setText(R.string.conversation_select_file_cannel);

		buttomTitle = (RelativeLayout) findViewById(R.id.activity_selectfile_buttom);
		buttomDivider = (LinearLayout) findViewById(R.id.ws_selectFile_buttom_divider);
		gridViews = (GridView) findViewById(R.id.selectfile_gridview);
		listViews = (ListView) findViewById(R.id.selectfile_lsitview);
		loading = (LinearLayout) findViewById(R.id.selectfile_loading);
	}

	private void init() {

		title.setText(R.string.conversation_select_file_entry_picture);
		listViews.setVisibility(View.VISIBLE);
		gridViews.setVisibility(View.GONE);
		finishButton.setVisibility(View.INVISIBLE);
		buttomTitle.setVisibility(View.GONE);
		buttomDivider.setVisibility(View.GONE);
		service = Executors.newCachedThreadPool();
		pictresClassify = new ArrayList<String>();
		pictures = new ArrayList<FileInfoBean>();
		listMap = new HashMap<String, ArrayList<FileInfoBean>>();
		remoteID = getIntent().getLongExtra("uid", -1);
		new Thread(new Runnable() {

			@Override
			public void run() {
				loading.setVisibility(View.VISIBLE);
				for (int i = 0; i < selectArgs.length; i++) {

					initPictures(Uri.parse(selectArgs[i][0]), selectArgs[i][1]);
				}

				handler.sendEmptyMessage(SCAN_SDCARD);
			}
		}).start();
	}

	private void initPictures(Uri uri, String select) {

		ContentResolver resolver = getContentResolver();
		String[] projection = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE };
		String selection = MediaStore.Images.Media.MIME_TYPE + "=?";
		String[] selectionArgs = { select };
		String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";
		Cursor cursor = null;
		try {
			cursor = resolver.query(uri, projection, selection, selectionArgs,
					sortOrder);
			if (cursor != null) {
				FileInfoBean bean = null;
				ArrayList<FileInfoBean> currentList = null;
				while (cursor.moveToNext()) {
					bean = new FileInfoBean();
					String filePath = cursor.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					bean.filePath = filePath;
					bean.fileSize = Long.valueOf(cursor.getString(cursor
							.getColumnIndex(MediaStore.Images.Media.SIZE)));
					bean.fileName = cursor
							.getString(cursor
									.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
					bean.fileType = 1;
					if (TextUtils.isEmpty(filePath))
						continue;

					String parentName;
					String[] s = filePath.split("/");
					V2Log.i(TAG, "file path : " + filePath);
					V2Log.i(TAG, "arr size : " + s.length);

					int type = 0;
					if (!SDCARD_ROOT.equals(s[1]))
						type = DEFAULT_PATH;

					if (type == DEFAULT_PATH) {
						if (DEFAULT_PATH_INDEX < s.length)
							parentName = s[DEFAULT_PATH_INDEX];
						else
							continue;
						V2Log.i(TAG, "index name : " + s[DEFAULT_PATH_INDEX]);
					} else {
						if (SDCARD_PATH_INDEX <= s.length
								&& SDCARD_ROOT_NAME.equals(s[2])) {
							if (SDCARD_PATH_INDEX == s.length - 1)
								parentName = "root";
							else
								parentName = s[SDCARD_PATH_INDEX];
						} else
							continue;
						V2Log.i(TAG, "index name : " + s[SDCARD_PATH_INDEX]);
					}
					V2Log.i(TAG, "parentName name : " + parentName);
					V2Log.i("------------------------");
					if (listMap.containsKey(parentName)) {
						currentList = listMap.get(parentName);
						if (currentList == null)
							currentList = new ArrayList<FileInfoBean>();
					} else {
						pictresClassify.add(parentName);
						currentList = new ArrayList<FileInfoBean>();
					}
					currentList.add(bean);
					listMap.put(parentName, currentList);
					bean = null;
				}
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

	private void setListener() {

		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		gridViews.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

				isLoading = scrollState;
				if (scrollState == 0) {
					imageAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});

		gridViews.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int size = (int) (pictures.get(position).fileSize / (double) 1048576);
				if (size > 3) {
					Toast.makeText(
							getApplicationContext(),
							R.string.conversation_select_file_entry_pictures_limited,
							Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent();
					intent.putExtra("checkedImage",
							pictures.get(position).filePath);
					setResult(100, intent);
					ConversationSelectImage.super.onBackPressed();
				}
			}
		});

		listViews.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				isClassify = false;
				String classifyName = pictresClassify.get(position);
				pictures = listMap.get(classifyName);
				listViews.setVisibility(View.GONE);
				gridViews.setVisibility(View.VISIBLE);
				imageAdapter = new ImageListAdapter();
				gridViews.setAdapter(imageAdapter);
				isBack = true;
			}
		});
	}

	class ImageClassifyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return pictresClassify.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {

				holder = new ViewHolder();
				convertView = View.inflate(ConversationSelectImage.this,
						R.layout.activity_selectfile_image_adapter, null);
				holder.fileIcon = (ImageView) convertView
						.findViewById(R.id.ws_selectFile_iamge_icon);
				holder.fileName = (TextView) convertView
						.findViewById(R.id.ws_selectFile_iamge_name);
				convertView.setTag(holder);
			} else {

				holder = (ViewHolder) convertView.getTag();
			}

			LayoutParams para = holder.fileIcon.getLayoutParams();
			para.width = DensityUtils.dip2px(mContext, 100);
			para.height = DensityUtils.dip2px(mContext, 100);
			holder.fileIcon.setLayoutParams(para);

			String classifyName = pictresClassify.get(position);
			ArrayList<FileInfoBean> arrayList = listMap.get(classifyName);
			FileInfoBean fb = arrayList.get(0);
			holder.fileName.setText(classifyName + "( " + arrayList.size()
					+ " )");
			fb.fileName = fb.filePath
					.substring(fb.filePath.lastIndexOf("/") + 1);
			Bitmap bit = lruCache.get(fb.fileName);
			if (bit == null || bit.isRecycled()) {

				if (isLoading != SCROLL_STATE_TOUCH_SCROLL && isLoading != 1)
					// 开始加载图片
					startLoadBitmap(holder, fb);
				else
					// 加载中显示的图片
					holder.fileIcon.setImageResource(R.drawable.ic_launcher);

			} else {

				holder.fileIcon.setImageBitmap(bit);
			}
			return convertView;
		}
	}

	class ImageListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return pictures.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			final ViewHolder holder;
			if (convertView == null) {

				holder = new ViewHolder();
				convertView = View.inflate(ConversationSelectImage.this,
						R.layout.activity_imagefile_adapter, null);
				holder.fileIcon = (ImageView) convertView
						.findViewById(R.id.selectfile_adapter_image);
				holder.fileCheck = (CheckBox) convertView
						.findViewById(R.id.selectfile_adapter_check);
				convertView.setTag(holder);
			} else {

				holder = (ViewHolder) convertView.getTag();
			}
			holder.fileCheck.setVisibility(View.INVISIBLE);
			LayoutParams para = holder.fileIcon.getLayoutParams();

			Configuration conf = getResources().getConfiguration();
			if (conf.smallestScreenWidthDp >= 600) {
				para.height = GlobalConfig.SCREEN_WIDTH / 3;//
			} else {
				para.height = GlobalConfig.SCREEN_HEIGHT / 4;//
			}
			para.width = (GlobalConfig.SCREEN_WIDTH - 20) / 3;// 一屏显示3列
			holder.fileIcon.setLayoutParams(para);

			if (pictures.size() <= 0) {
				V2Log.e(TAG, "error mFileLists size zero");
				Toast.makeText(
						getApplicationContext(),
						R.string.conversation_select_file_entry_picture_anomaly,
						Toast.LENGTH_SHORT).show();
				finish();
			}

			FileInfoBean fb = pictures.get(position);
			if (TextUtils.isEmpty(fb.fileName)) {
				if (TextUtils.isEmpty(fb.filePath)) {
					holder.fileIcon.setImageResource(R.drawable.ic_launcher);
					V2Log.e(TAG, "error that this file name is vaild!");
					return convertView;
				} else {
					fb.fileName = fb.filePath.substring(fb.filePath
							.lastIndexOf("/") + 1);
				}
			}
			Bitmap bit = lruCache.get(fb.fileName);
			if (bit == null || bit.isRecycled()) {

				if (isLoading != SCROLL_STATE_TOUCH_SCROLL && isLoading != 1)
					// 开始加载图片
					startLoadBitmap(holder, fb);
				else
					// 加载中显示的图片
					holder.fileIcon.setImageResource(R.drawable.ic_launcher);

			} else {

				holder.fileIcon.setImageBitmap(bit);
			}
			return convertView;
		}

	}

	class ViewHolder {

		public ImageView fileIcon;
		public CheckBox fileCheck;
		public TextView fileName;
	}

	public void startLoadBitmap(final ViewHolder holder, final FileInfoBean fb) {
		service.execute(new Runnable() {

			@Override
			public void run() {
				try {

					Bitmap bitmap = null;
					if (isClassify)
						bitmap = BitmapUtil.getImageThumbnail(fb.filePath, 100,
								100);
					else
						bitmap = BitmapUtil.getCompressedBitmap(fb.filePath);

					if (fb.fileName == null && bitmap != null) {
						if (!bitmap.isRecycled()) {
							bitmap.recycle();
							bitmap = null;
						}
						return;
					}

					if (bitmap == null) {
						V2Log.e(TAG, "get null when loading " + fb.fileName
								+ " picture.");
						return;
					}

					lruCache.put(fb.fileName, bitmap);
					Message.obtain(handler, UPDATE_BITMAP,
							new Object[] { holder, bitmap, fb }).sendToTarget();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
