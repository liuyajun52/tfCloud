package com.blacklighting.tianfuyunv2;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blacklighting.tianfuyunv2.dompraser.MagazineCategoryListParser;
import com.blacklighting.tianfuyunv2.internet.ApiAddress;
import com.blacklighting.tianfuyunv2.internet.StreamGetter;
import com.blacklighting.tianfuyunv2.models.MagazineCategory;
import com.blacklighting.tianfuyunv2.models.Passage;

public class MagazineActivity extends Activity implements View.OnClickListener {
	public static final int ERRORWHAT = 0, CATEGORYWHAT = 1;
	List<MagazineCategory> categories;
	Passage bw, sm;
	ExpandableListView categoryList;
	ExpandableAdapter adapter;
	MHandler mHandler = new MHandler(this);
	String period, magazineT;
	byte[] imageCover;
	Button bwButton, smButton;

	ImageView cover;
	TextView title;
	private SQLiteDatabase dbcache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_magazine);
		// Show the Up button in the action bar.
		setupActionBar();

		// 获取杂志Intent信息
		period = getIntent().getStringExtra("period");
		magazineT = getIntent().getStringExtra("magazineT");
		imageCover = getIntent().getByteArrayExtra("cover");

		cover = (ImageView) findViewById(R.id.magazineC);
		title = (TextView) findViewById(R.id.magazineT);
		bwButton = (Button) findViewById(R.id.bwButton);
		smButton = (Button) findViewById(R.id.smButton);

		bwButton.setOnClickListener(this);
		smButton.setOnClickListener(this);

		if (imageCover != null) {
			cover.setImageBitmap(BitmapFactory.decodeByteArray(imageCover, 0,
					imageCover.length));
		}
		title.setText(magazineT);

		dbcache = openOrCreateDatabase("cache.db", Context.MODE_PRIVATE, null);
		dbcache.execSQL("CREATE TABLE IF NOT EXISTS cache (_id INTEGER PRIMARY KEY ,category1 VARCHAR,category2 VARCHAR,period VARCHAR, title VARCHAR, image VARCHAR,createTime VARCHAR, realImage BLOB, hasRead VARCHAR,source VARCHAR,content VARCHAR)");

		categories = new ArrayList<MagazineCategory>();
		categoryList = (ExpandableListView) findViewById(R.id.magazineExpandableListView);
		adapter = new ExpandableAdapter(categories, this);
		categoryList.setAdapter(adapter);

		categoryList.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView arg0, View arg1,
					int arg2, int arg3, long arg4) {
				TextView title = (TextView) arg1
						.findViewById(R.id.magazinePassageTitle);
				title.setTextColor(Color.GRAY);
				categories.get(arg2).getPassages().get(arg3)
						.setHasChecked(true);

				ContentValues cv = new ContentValues();
				cv.put("hasRead", "1");
				dbcache.update(
						"cache",
						cv,
						"_id = ?",
						new String[] { categories.get(arg2).getPassages()
								.get(arg3).getId() });

				Intent i = new Intent(MagazineActivity.this,
						PassageActivity.class);
				i.putExtra("id", categories.get(arg2).getPassages().get(arg3)
						.getId());
				if (categories.get(arg2).getPassages().get(arg3).getImage() != null
						&& !categories.get(arg2).getPassages().get(arg3)
								.getImage().isEmpty()
						&& categories.get(arg2).getPassages().get(arg3)
								.getRealImag() != null) {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					categories.get(arg2).getPassages().get(arg3).getRealImag()
							.compress(Bitmap.CompressFormat.PNG, 100, os);
					i.putExtra("realImage", os.toByteArray());
				}
				startActivity(i);
				return false;
			}
		});
		new GetMagazineCategoriesThread().start();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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

	class GetMagazineCategoriesThread extends Thread {

		@Override
		public void run() {
			super.run();
			Map<String, String> params = new HashMap<String, String>();
			params.put("period", period);
			StreamGetter mGetter = new StreamGetter(
					ApiAddress.getMAGAZINE_BY_PERIOD_API(), params);
			try {
				InputStream in = mGetter.getIn();
				categories.addAll(MagazineCategoryListParser.parser(in));
				for (int i = 0; i < categories.size(); i++) {
					if (categories.get(i).getName().equals("编委")) {
						bw = categories.get(i).getPassages().get(0);
						categories.remove(i--);
						continue;
					} else if (categories.get(i).getName().equals("声明")) {
						sm = categories.get(i).getPassages().get(0);
						categories.remove(i--);
						continue;
					}

					for (Passage n : categories.get(i).getPassages()) {
						Cursor c = dbcache.rawQuery(
								"select  *  from cache where _id = ?",
								new String[] { n.getId() });
						if (!c.moveToFirst()) {
							ContentValues p = new ContentValues();
							p.put("_id", n.getId());
							p.put("category1", "成都软件");
							p.put("category2", categories.get(i).getName());
							p.put("period", period);
							p.put("title", n.getTitle());
							p.put("createTime", n.getCreateTime());
							if (n.getImage() != null) {
								p.put("image", n.getImage());
								ByteArrayOutputStream os = new ByteArrayOutputStream();
								n.getRealImag().compress(
										Bitmap.CompressFormat.PNG, 100, os);
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

				}
				mHandler.sendEmptyMessage(CATEGORYWHAT);
			} catch (Exception e) {
				e.printStackTrace();
				Cursor cat = dbcache
						.rawQuery(
								"select  distinct category2  from cache where category1=?  and period = ?",
								new String[] { "成都软件", period });

				while (cat.moveToNext()) {
					MagazineCategory category = new MagazineCategory();
					category.setName(cat.getString(cat
							.getColumnIndex("category2")));

					Cursor passages = dbcache
							.rawQuery(
									"select * from cache where category1=?  and category2 = ? and period = ?",
									new String[] { "成都软件", category.getName(),
											period });
					List<Passage> ps = new ArrayList<Passage>();

					while (passages.moveToNext()) {
						Passage p = new Passage();
						p.setId(passages.getString(passages
								.getColumnIndex("_id")));
						p.setTitle(passages.getString(passages
								.getColumnIndex("title")));
						p.setCreateTime(passages.getString(passages
								.getColumnIndex("createTime")));
						p.setImage(passages.getString(passages
								.getColumnIndex("image")));

						if (p.getImage() != null && !p.getImage().isEmpty()) {
							byte[] in = passages.getBlob(passages
									.getColumnIndex("realImage"));
							p.setRealImag(BitmapFactory.decodeByteArray(in, 0,
									in.length));
						}
						p.setHasChecked(passages.getString(
								passages.getColumnIndex("hasRead")).equals("1"));
						ps.add(p);
					}
					category.setPassages(ps);
					categories.add(category);
				}

				mHandler.sendEmptyMessage(ERRORWHAT);
			}

		}
	}

	static class MHandler extends Handler {
		WeakReference<MagazineActivity> magazineActivity;

		public MHandler(MagazineActivity magazineActivity) {
			this.magazineActivity = new WeakReference<MagazineActivity>(
					magazineActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			MagazineActivity ma = magazineActivity.get();
			switch (msg.what) {
			case ERRORWHAT:
				Toast.makeText(ma, "网络连接错误，已经加载本地缓存", Toast.LENGTH_SHORT)
						.show();
				ma.adapter.notifyDataSetChanged();
				magazineActivity.get().findViewById(R.id.magazineProgressBar)
						.setVisibility(View.GONE);
				magazineActivity.get().categoryList.setVisibility(View.VISIBLE);
				break;
			case CATEGORYWHAT:
				ma.adapter.notifyDataSetChanged();
				magazineActivity.get().findViewById(R.id.magazineProgressBar)
						.setVisibility(View.GONE);
				magazineActivity.get().categoryList.setVisibility(View.VISIBLE);
				break;
			}
		}

	}

	class ExpandableAdapter extends BaseExpandableListAdapter {
		private List<MagazineCategory> categories;
		private LayoutInflater inflater;

		public ExpandableAdapter(List<MagazineCategory> categories,
				Context context) {
			this.categories = categories;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return categories.get(groupPosition).getPassages()
					.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(final int groupPosition,
				final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			View childView;
			if (convertView == null) {
				childView = inflater.inflate(R.layout.magazine_son, null);
			} else {
				childView = convertView;
			}

			// childView=inflater.inflate(R.layout.magazine_son, null);
			ImageView passageImage = (ImageView) childView
					.findViewById(R.id.magazinePassageImage);
			TextView passageTitle = (TextView) childView
					.findViewById(R.id.magazinePassageTitle);
			passageTitle.setText(categories.get(groupPosition).getPassages()
					.get(childPosition).getTitle());
			passageImage.setImageBitmap(categories.get(groupPosition)
					.getPassages().get(childPosition).getRealImag());

			if (categories.get(groupPosition).getPassages().get(childPosition)
					.isHasChecked()) {
				passageTitle.setTextColor(Color.GRAY);
			} else {
				passageTitle.setTextColor(Color.BLACK);
			}
			return childView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return categories.get(groupPosition).getPassages().size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return categories.get(groupPosition).getName();
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
			String categoryName = categories.get(groupPosition).getName();
			View parentView = inflater.inflate(R.layout.magazine_father, null);
			TextView category = (TextView) parentView
					.findViewById(R.id.magazineCategory);
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

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.bwButton:
			if (bw != null) {
				Intent bwIntent = new Intent(this, PassageActivity.class);
				bwIntent.putExtra("id", bw.getId());
				startActivity(bwIntent);
			}else{
				Toast.makeText(getApplicationContext(), "该期杂志没有编委栏目", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.smButton:
			if (sm != null) {
				Intent smIntent = new Intent(this, PassageActivity.class);
				smIntent.putExtra("id", sm.getId());
				startActivity(smIntent);
			}else{
				Toast.makeText(getApplicationContext(), "该期杂志没有声明栏目", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}
}
