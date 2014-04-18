package com.blacklighting.tianfuyunv2;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blacklighting.tianfuyunv2.dompraser.PassageListParser;
import com.blacklighting.tianfuyunv2.internet.ApiAddress;
import com.blacklighting.tianfuyunv2.internet.StreamGetter;
import com.blacklighting.tianfuyunv2.models.PassageListIteam;

public class ProductServerActivity extends Activity {
	public static final int ERRORWHAT = 4;
	String[] categoryArray = { "政策法规", "软件企业", "商务信息", "产品推荐","协会名录" ,};
	String[] leavel = { "理事长单位", "副理事长单位", "常务理事单位", "理事单位", "普通会员单位" };
	List<List<PassageListIteam>> categoryList;
	MHandler mHandler = new MHandler(this);
	ExpandableListView list;
	ExpandableAdapter adapter;
	private SQLiteDatabase db;
	private SQLiteDatabase dbcache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_server);
		// Show the Up button in the action bar.
		setupActionBar();

		// 打开收藏夹数据库

		db = openOrCreateDatabase("tianfuyun.db", Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS collection (_id INTEGER PRIMARY KEY , title VARCHAR, image VARCHAR,createTime VARCHAR, realImage BLOB)");
		dbcache = openOrCreateDatabase("cache.db", Context.MODE_PRIVATE, null);
		dbcache.execSQL("CREATE TABLE IF NOT EXISTS cache (_id INTEGER PRIMARY KEY ,category1 VARCHAR,category2 VARCHAR,period VARCHAR, title VARCHAR, image VARCHAR,createTime VARCHAR, realImage BLOB, hasRead VARCHAR,source VARCHAR,content VARCHAR)");

		categoryList = new ArrayList<List<PassageListIteam>>();
		list = (ExpandableListView) findViewById(R.id.productServerExpandableListView);
		adapter = new ExpandableAdapter(categoryList, this);
		list.setAdapter(adapter);

		for (int i = 0; i < categoryArray.length - 1; i++) {
			categoryList.add(new ArrayList<PassageListIteam>());
			new GetProductServerListThread(i).start();
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class GetProductServerListThread extends Thread {
		int index;

		public GetProductServerListThread(int index) {
			this.index = index;
		}

		@Override
		public void run() {
			super.run();
			try {
				Map<String, String> params = new HashMap<String, String>();
				params.put("category", categoryArray[index]);
				InputStream in = new StreamGetter(ApiAddress.getSEARCH_API(),
						params).getIn();
				categoryList.get(index).addAll(PassageListParser.parse(in));
				mHandler.sendEmptyMessage(index);
				for (PassageListIteam n : categoryList.get(index)) {
					Cursor c = dbcache.rawQuery(
							"select  *  from cache where _id = ?",
							new String[] { n.getId() });
					if (!c.moveToFirst()) {
						ContentValues p = new ContentValues();
						p.put("_id", n.getId());
						p.put("category1", categoryArray[index]);
						p.put("title", n.getTitle());
						p.put("createTime", n.getCreateTime());
						if (n.getImage() != null) {
							p.put("image", n.getImage());
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							n.getRealImag().compress(Bitmap.CompressFormat.PNG,
									100, os);
							p.put("realImage", os.toByteArray());
						} else {
							p.put("image", "");
							p.put("realImage", new byte[64]);
						}
						p.put("hasRead", "0");
						dbcache.insert("cache", null, p);
					} else {
						int index = c.getColumnIndex("hasRead");
						String result = c.getString(index);
						n.setHasChecked(result == null ? false : result
								.equals("1"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();

				Cursor passageCache = dbcache.rawQuery(
						"select * from cache where category1 = ?",
						new String[] { categoryArray[index] });

				while (passageCache.moveToNext()) {
					PassageListIteam p = new PassageListIteam();
					p.setId(passageCache.getString(passageCache
							.getColumnIndex("_id")));
					p.setTitle(passageCache.getString(passageCache
							.getColumnIndex("title")));
					p.setCreateTime(passageCache.getString(passageCache
							.getColumnIndex("createTime")));
					p.setImage(passageCache.getString(passageCache
							.getColumnIndex("image")));

					if (p.getImage() != null && p.getImage().isEmpty()) {
						byte[] in = passageCache.getBlob(passageCache
								.getColumnIndex("realImage"));
						p.setRealImag(BitmapFactory.decodeByteArray(in, 0,
								in.length));
					}
					p.setHasChecked(passageCache.getString(
							passageCache.getColumnIndex("hasRead")).equals("1"));
					categoryList.get(index).add(p);
				}

				mHandler.sendEmptyMessage(ERRORWHAT);
			}
		}
	}

	static class MHandler extends Handler {
		private WeakReference<ProductServerActivity> mActivity;

		public MHandler(ProductServerActivity mActivity) {
			this.mActivity = new WeakReference<ProductServerActivity>(mActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == ERRORWHAT) {
				Toast.makeText(mActivity.get(), "网络连接错误，已经加载本地缓存",
						Toast.LENGTH_SHORT).show();
				mActivity.get().adapter.notifyDataSetChanged();
			} else {
				mActivity.get().adapter.notifyDataSetChanged();
			}

		}
	}

	class ExpandableAdapter extends BaseExpandableListAdapter {
		private List<List<PassageListIteam>> categories;
		private LayoutInflater inflater;
		private Context context;

		public ExpandableAdapter(List<List<PassageListIteam>> categories,
				Context context) {
			this.categories = categories;
			this.context = context;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			if (groupPosition < categories.size()) {
				return categories.get(groupPosition).get(childPosition);
			} else {
				return leavel[childPosition];
			}
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(final int groupPosition,
				final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {

			final String id, title, createTime, image;
			final Bitmap realImage;

			View childView = inflater.inflate(R.layout.product_son, null);
			;

			if (groupPosition < categories.size()) {

				ImageView passageImage = (ImageView) childView
						.findViewById(R.id.productServerPassageImage);
				TextView passageTitle = (TextView) childView
						.findViewById(R.id.productServerPassageTitle);
				TextView passageTime = (TextView) childView
						.findViewById(R.id.productServerPassageTime);

				id = categories.get(groupPosition).get(childPosition).getId();
				title = categories.get(groupPosition).get(childPosition)
						.getTitle();
				createTime = categories.get(groupPosition).get(childPosition)
						.getCreateTime();
				image = categories.get(groupPosition).get(childPosition)
						.getImage();
				realImage = categories.get(groupPosition).get(childPosition)
						.getRealImag();

				passageTitle.setText(title);
				passageTime.setText(createTime);
				if (categoryList.get(groupPosition).get(childPosition)
						.getImage() != null
						&& !categories.get(groupPosition).get(childPosition)
								.getImage().isEmpty()) {
					passageImage.setImageBitmap(realImage);
				} else {
					passageImage.setImageResource(R.drawable.news_default);
				}
				if (categoryList.get(groupPosition).get(childPosition)
						.isHasChecked()) {
					passageTitle.setTextColor(Color.GRAY);
				} else {
					passageTitle.setTextColor(Color.BLACK);
				}

				childView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						TextView title = (TextView) arg0
								.findViewById(R.id.productServerPassageTitle);
						title.setTextColor(Color.GRAY);
						categoryList.get(groupPosition).get(childPosition)
								.setHasChecked(true);

						ContentValues cv = new ContentValues();
						cv.put("hasRead", "1");
						dbcache.update("cache", cv, "_id = ?",
								new String[] { categoryList.get(groupPosition)
										.get(childPosition).getId() });

						Intent i = new Intent(context, PassageActivity.class);
						i.putExtra(
								"id",
								categoryList.get(groupPosition)
										.get(childPosition).getId());
						if (categoryList.get(groupPosition).get(childPosition)
								.getImage() != null
								&& !categoryList.get(groupPosition)
										.get(childPosition).getImage()
										.isEmpty()
								&& categoryList.get(groupPosition)
										.get(childPosition).getRealImag() != null) {
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							categoryList
									.get(groupPosition)
									.get(childPosition)
									.getRealImag()
									.compress(Bitmap.CompressFormat.PNG, 100,
											os);
							i.putExtra("realImage", os.toByteArray());
						}
						// i.putExtra("passageId", id);
						context.startActivity(i);
					}
				});

				childView.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {

						Builder builder = new Builder(context);
						builder.setTitle("是否加入收藏夹？")
								.setPositiveButton("确认",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												// stub
												ContentValues passage = new ContentValues();
												passage.put("_id", id);
												passage.put("title", title);
												passage.put("createTime",
														createTime);
												passage.put("image", image);
												if (realImage != null) {
													ByteArrayOutputStream os = new ByteArrayOutputStream();
													realImage
															.compress(
																	Bitmap.CompressFormat.PNG,
																	100, os);
													passage.put("realImage",
															os.toByteArray());
												} else {
													passage.put("realImage",
															new byte[1024]);
												}
												db.replace("collection", null,
														passage);
											}
										}).setNegativeButton("取消", null);

						builder.create().show();
						return true;
					}
				});
			} else {
				TextView passageTitle = (TextView) childView
						.findViewById(R.id.productServerPassageTitle);
				passageTitle.setText(leavel[childPosition]);
				// childView.setBackgroundColor(Color.WHITE);
				childView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent dir = new Intent(context,
								AssociationDirectoryActivity.class);
						dir.putExtra("leavel", leavel[childPosition]);
						context.startActivity(dir);
					}
				});
			}
			return childView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if (groupPosition < categories.size()) {
				return categories.get(groupPosition).size();
			} else {
				return leavel.length;
			}
		}

		@Override
		public Object getGroup(int groupPosition) {
			return categoryArray[groupPosition];
		}

		@Override
		public int getGroupCount() {
			return categories.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			String categoryName = categoryArray[groupPosition];
			View parentView = inflater.inflate(R.layout.product_father, null);
			TextView category = (TextView) parentView
					.findViewById(R.id.productServerCategory);
			category.setText(categoryName);
			return parentView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}

	}

}
