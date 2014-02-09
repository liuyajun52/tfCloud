package com.blacklighting.tianfuyunv2;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blacklighting.tianfuyunv2.dompraser.NewsListParser;
import com.blacklighting.tianfuyunv2.internet.ApiAddress;
import com.blacklighting.tianfuyunv2.internet.StreamGetter;
import com.blacklighting.tianfuyunv2.models.NewsListIteam;

public class NewsActivity extends Activity {

	static final int NEWSLISTWHAT = 0, ERRORWHAT = 1;
	MHandler mHandler = new MHandler(this);
	static List<NewsListIteam> news;
	static MAdapter adapter;
	ListView newsList;
	private SQLiteDatabase db;
	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);
		// Show the Up button in the action bar.
		setupActionBar();
		newsList = (ListView) findViewById(R.id.newsList);
		news = new ArrayList<NewsListIteam>();
		adapter = new MAdapter(news, this);
		newsList.setAdapter(adapter);

		db = openOrCreateDatabase("cache.db", Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS cache (_id INTEGER PRIMARY KEY ,category1 VARCHAR,category2 VARCHAR,period VARCHAR, title VARCHAR, image VARCHAR,createTime VARCHAR, realImage BLOB, hasRead VARCHAR,source VARCHAR,content VARCHAR)");

		sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		newsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				TextView title = (TextView) arg1.findViewById(R.id.newsTitle);
				title.setTextColor(Color.GRAY);

				ContentValues cv = new ContentValues();
				cv.put("hasRead", "1");
				db.update("cache", cv, "_id = ?", new String[] { news.get(arg2)
						.getId() });
				news.get(arg2).setHasChecked(true);
				Intent i = new Intent(NewsActivity.this, PassageActivity.class);
				i.putExtra("id", news.get(arg2).getId());
				if (news.get(arg2).getImage() != null
						&& !news.get(arg2).getImage().isEmpty()
						&& news.get(arg2).getRealImag() != null) {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					news.get(arg2).getRealImag()
							.compress(Bitmap.CompressFormat.PNG, 100, os);
					i.putExtra("realImage", os.toByteArray());
				}
				startActivity(i);
			}
		});
		new GetNewsListThread().start();
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

	class GetNewsListThread extends Thread {

		@Override
		public void run() {
			super.run();
			StreamGetter mGetter = new StreamGetter(
					ApiAddress.getACTIVITIES_API(), null);
			try {
				InputStream in = mGetter.getIn();
				news.addAll(NewsListParser.parse(in));
				mHandler.sendEmptyMessage(NEWSLISTWHAT);

				Editor editor = sp.edit();
				editor.putInt("sinceId", Integer.parseInt(news.get(0).getId()));
				editor.commit();

				for (NewsListIteam n : news) {
					Cursor c = db.rawQuery(
							"select  *  from cache where _id = ?",
							new String[] { n.getId() });
					if (!c.moveToFirst()) {
						ContentValues p = new ContentValues();
						p.put("_id", n.getId());
						p.put("category1", "最新消息");
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
						try {
							db.insert("cache", null, p);
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else {
						int index = c.getColumnIndex("hasRead");
						String result = c.getString(index);
						n.setHasChecked(result == null ? false : result
								.equals("1"));
					}
				}

			} catch (Exception e) {
				e.printStackTrace();

				Cursor passageCache = db.rawQuery(
						"select * from cache where category1 = ? order by _id desc",
						new String[] { "最新消息" });

				while (passageCache.moveToNext()) {
					NewsListIteam n = new NewsListIteam();
					n.setId(passageCache.getString(passageCache
							.getColumnIndex("_id")));
					n.setTitle(passageCache.getString(passageCache
							.getColumnIndex("title")));
					n.setCreateTime(passageCache.getString(passageCache
							.getColumnIndex("createTime")));
					n.setImage(passageCache.getString(passageCache
							.getColumnIndex("image")));

					if (n.getImage() != null && !n.getImage().isEmpty()) {
						byte[] in = passageCache.getBlob(passageCache
								.getColumnIndex("realImage"));
						n.setRealImag(BitmapFactory.decodeByteArray(in, 0,
								in.length));
					}
					n.setHasChecked(passageCache.getString(
							passageCache.getColumnIndex("hasRead")).equals("1"));
					news.add(n);
				}
				mHandler.sendEmptyMessage(ERRORWHAT);
			}

		}
	}

	static class MHandler extends Handler {

		private WeakReference<NewsActivity> mActivity;

		MHandler(NewsActivity mActivity) {
			this.mActivity = new WeakReference<NewsActivity>(mActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ERRORWHAT:
				Toast.makeText(mActivity.get(), "网络连接错误,已经加载本地缓存",
						Toast.LENGTH_SHORT).show();
				mActivity.get().findViewById(R.id.newsProgressBar)
						.setVisibility(View.GONE);
				mActivity.get().newsList.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
				break;
			case NEWSLISTWHAT:
				mActivity.get().findViewById(R.id.newsProgressBar)
						.setVisibility(View.GONE);
				mActivity.get().newsList.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
				break;
			default:
				break;
			}

		}

	}

	class MAdapter extends BaseAdapter {
		Context context;
		private LayoutInflater listContainer;
		List<NewsListIteam> passages;

		MAdapter(List<NewsListIteam> passages, Context context) {
			this.context = context;
			this.passages = passages;
			listContainer = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return passages.size();
		}

		@Override
		public Object getItem(int arg0) {
			return passages.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View convertView, ViewGroup parent) {
			ListItemView listItemView = null;
			if (convertView == null) {
				listItemView = new ListItemView();
				convertView = listContainer.inflate(R.layout.news_row, null);
				listItemView.newsImage = (ImageView) convertView
						.findViewById(R.id.newsImage);
				listItemView.date = (TextView) convertView
						.findViewById(R.id.newsTime);
				listItemView.title = (TextView) convertView
						.findViewById(R.id.newsTitle);
				convertView.setTag(listItemView);
			} else {
				listItemView = (ListItemView) convertView.getTag();
			}

			listItemView.title.setText(passages.get(arg0).getTitle());
			listItemView.date.setText(passages.get(arg0).getCreateTime());
			if (passages.get(arg0).getImage() != null
					&& !passages.get(arg0).getId().isEmpty()
					&& passages.get(arg0).getRealImag() != null) {
				listItemView.newsImage.setImageBitmap(passages.get(arg0)
						.getRealImag());
			} else {
				listItemView.newsImage
						.setImageResource(R.drawable.news_default);
			}

			if (passages.get(arg0).isHasChecked()) {
				listItemView.title.setTextColor(Color.GRAY);
			} else {
				listItemView.title.setTextColor(Color.BLACK);
			}
			return convertView;
		}

		public final class ListItemView { // 自定义控件集合
			public ImageView newsImage;
			public TextView date;
			public TextView title;
		}

	}

}
