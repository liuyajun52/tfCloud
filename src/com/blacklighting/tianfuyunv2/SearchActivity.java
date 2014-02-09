package com.blacklighting.tianfuyunv2;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.blacklighting.tianfuyunv2.dompraser.NewsListParser;
import com.blacklighting.tianfuyunv2.internet.ApiAddress;
import com.blacklighting.tianfuyunv2.internet.StreamGetter;
import com.blacklighting.tianfuyunv2.models.NewsListIteam;

public class SearchActivity extends Activity implements OnQueryTextListener,
		OnClickListener {
	final static int MOREWHAT = 0, ERRORWHAT = 1;
	SearchView search;
	ListView searchList;
	private SQLiteDatabase db;
	DataBaseAdapter adapter;
	List<NewsListIteam> passages;
	ProgressBar progress;
	TextView loadMore;
	MHandler mHandler = new MHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		// Show the Up button in the action bar.
		setupActionBar();

		search = (SearchView) findViewById(R.id.searchView);
		searchList = (ListView) findViewById(R.id.searchList);
		progress = (ProgressBar) findViewById(R.id.searchProgressBar);
		loadMore = (TextView) findViewById(R.id.loadMore);

		loadMore.setOnClickListener(this);

		passages = new ArrayList<NewsListIteam>();
		adapter = new DataBaseAdapter(passages, SearchActivity.this);
		searchList.setAdapter(adapter);

		searchList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				TextView title = (TextView) arg1.findViewById(R.id.newsTitle);
				title.setTextColor(Color.GRAY);
				passages.get(arg2).setHasChecked(true);
				ContentValues cv = new ContentValues();
				cv.put("hasRead", "1");
				db.update("cache", cv, "_id = ?",
						new String[] { passages.get(arg2).getId() });
				passages.get(arg2).setHasChecked(true);
				Intent i = new Intent(SearchActivity.this,
						PassageActivity.class);
				i.putExtra("id", passages.get(arg2).getId());
				if (passages.get(arg2).getImage() != null
						&& passages.get(arg2).getRealImag() != null) {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					passages.get(arg2).getRealImag()
							.compress(Bitmap.CompressFormat.PNG, 100, os);
					i.putExtra("realImage", os.toByteArray());
				}
				startActivity(i);

			}
		});

		db = openOrCreateDatabase("cache.db", Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS cache (_id INTEGER PRIMARY KEY ,category1 VARCHAR,category2 VARCHAR,period VARCHAR, title VARCHAR, image VARCHAR,createTime VARCHAR, realImage BLOB, hasRead VARCHAR,source VARCHAR,content VARCHAR)");

		search.setOnQueryTextListener(this);
	}

	//
	// @Override
	// protected void onResume() {
	// super.onResume();
	// adapter.notifyDataSetChanged();
	// }

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.search, menu);
	// return true;
	// }

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

	@Override
	public boolean onQueryTextChange(String arg0) {

		passages.clear();
		if (!arg0.isEmpty()) {
			Cursor c = db.rawQuery("select * from cache where title like ? order by _id desc",
					new String[] { "%" + arg0 + "%" });

			while (c.moveToNext()) {
				NewsListIteam passage = new NewsListIteam();
				passage.setId(c.getString(c.getColumnIndex("_id")));
				passage.setTitle(c.getString(c.getColumnIndex("title")));
				passage.setImage(c.getString(c.getColumnIndex("image")));
				passage.setCreateTime(c.getString(c
						.getColumnIndex("createTime")));
				byte[] in = c.getBlob(c.getColumnIndex("realImage"));
				if (in != null) {
					passage.setRealImag(BitmapFactory.decodeByteArray(in, 0,
							in.length));
				}
				passage.setHasChecked(c.getString(c.getColumnIndex("hasRead")) == null ? false
						: c.getString(c.getColumnIndex("hasRead")).equals("1"));
				passages.add(passage);
			}
			adapter.notifyDataSetChanged();
			loadMore.setVisibility(View.VISIBLE);
		} else {
			loadMore.setVisibility(View.GONE);
		}

		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {

		return true;
	}

	class DataBaseAdapter extends BaseAdapter {
		Context context;
		List<NewsListIteam> passages;

		public DataBaseAdapter(List<NewsListIteam> passages, Context context) {
			this.passages = passages;
			this.context = context;
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
		public View getView(int arg0, View arg1, ViewGroup arg2) {

			LayoutInflater inf = LayoutInflater.from(context);
			arg1 = inf.inflate(R.layout.news_row, null);

			TextView passageTitle = (TextView) arg1
					.findViewById(R.id.newsTitle);
			TextView passageTime = (TextView) arg1.findViewById(R.id.newsTime);
			ImageView passageImage = (ImageView) arg1
					.findViewById(R.id.newsImage);

			passageTitle.setText(passages.get(arg0).getTitle());

			if (passages.get(arg0).isHasChecked()) {
				passageTitle.setTextColor(Color.GRAY);
			} else {
				passageTitle.setTextColor(Color.BLACK);
			}

			passageTime.setText(passages.get(arg0).getCreateTime());
			if (passages.get(arg0).getImage() != null
					&& passages.get(arg0).getRealImag() != null) {
				passageImage.setImageBitmap(passages.get(arg0).getRealImag());
			}
			return arg1;
		}

	}

	class GetMoreListThread extends Thread {
		private String keyword;

		public GetMoreListThread(String keyword) {
			this.keyword = keyword;
		}

		@Override
		public void run() {
			super.run();
			Map<String, String> pa = new HashMap<String, String>();
			pa.put("keyword", keyword);
			StreamGetter mGetter = new StreamGetter(ApiAddress.getSEARCH_API(),
					pa);
			try {
				InputStream in = mGetter.getIn();
				List<NewsListIteam> news = NewsListParser.parse(in);
				// mHandler.sendEmptyMessage(NEWSLISTWHAT);

				for (NewsListIteam n : news) {
					Cursor c = db.rawQuery(
							"select  *  from cache where _id = ?",
							new String[] { n.getId() });
					if (!c.moveToFirst()) {
						ContentValues p = new ContentValues();
						p.put("_id", n.getId());
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
						passages.add(n);
					}
				}
				mHandler.sendEmptyMessage(MOREWHAT);
			} catch (Exception e) {
				mHandler.sendEmptyMessage(ERRORWHAT);
			}

		}
	}

	static class MHandler extends Handler {
		private WeakReference<SearchActivity> sat;

		public MHandler(SearchActivity sat) {
			this.sat = new WeakReference<SearchActivity>(sat);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case ERRORWHAT:
				sat.get().progress.setVisibility(View.GONE);
				Toast.makeText(sat.get(), "网络连接错误", Toast.LENGTH_SHORT).show();
				break;
			case MOREWHAT:
				sat.get().progress.setVisibility(View.GONE);
				sat.get().adapter.notifyDataSetChanged();
				Toast.makeText(sat.get(), "已经从服务器加载更多内容", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.loadMore:
			progress.setVisibility(View.VISIBLE);
			v.setVisibility(View.GONE);
			new GetMoreListThread(search.getQuery().toString()).start();
			// Toast.makeText(getApplicationContext(), "已经加载全部",
			// Toast.LENGTH_SHORT).show();
			// progress.setVisibility(View.GONE);

			break;
		}

	}

}
