package com.blacklighting.tianfuyunv2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blacklighting.tianfuyunv2.dompraser.NewsListParser;
import com.blacklighting.tianfuyunv2.internet.ApiAddress;
import com.blacklighting.tianfuyunv2.internet.StreamGetter;
import com.blacklighting.tianfuyunv2.models.NewsListIteam;

/**
 * @author liuyajun
 * 
 */
public class AssociationDirectoryActivity extends Activity {
	public static final int ERRORWHAT = 0, DIRWHAT = 1;
	String leavel;
	ListView dirListView;
	List<NewsListIteam> dirList;
	MHandler mHandler = new MHandler(this);
	MAdapter mAdapter;
	private SQLiteDatabase db;
	ProgressBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_association_directory);
		// Show the Up button in the action bar.
		setupActionBar();
		leavel = getIntent().getStringExtra("leavel");
		setTitle(leavel);
		dirListView = (ListView) findViewById(R.id.dirList);
		progress = (ProgressBar) findViewById(R.id.dirProgressBar);

		dirList = new ArrayList<NewsListIteam>();
		mAdapter = new MAdapter(dirList, getApplicationContext());
		dirListView.setAdapter(mAdapter);

		db = openOrCreateDatabase("cache.db", Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS cache (_id INTEGER PRIMARY KEY ,category1 VARCHAR,category2 VARCHAR,period VARCHAR, title VARCHAR, image VARCHAR,createTime VARCHAR, realImage BLOB, hasRead VARCHAR,source VARCHAR,content VARCHAR)");

		dirListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				TextView title = (TextView) arg1.findViewById(R.id.dirTitle);
				title.setTextColor(Color.GRAY);

				ContentValues cv = new ContentValues();
				cv.put("hasRead", "1");
				db.update("cache", cv, "_id = ?",
						new String[] { dirList.get(arg2).getId() });
				Intent i = new Intent(AssociationDirectoryActivity.this,
						PassageActivity.class);
				i.putExtra("id", dirList.get(arg2).getId());
				startActivity(i);

			}
		});

		new GetDirListThread().start();
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

	class GetDirListThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				Map<String, String> params = new HashMap<String, String>();
				params.put("category", "协会名录");
				params.put("category2", leavel);
				InputStream in = new StreamGetter(ApiAddress.getSEARCH_API(),
						params).getIn();
				dirList.addAll(NewsListParser.parse(in));

				for (NewsListIteam n : dirList) {
					Cursor c = db.rawQuery(
							"select  *  from cache where _id = ?",
							new String[] { n.getId() });
					if (!c.moveToFirst()) {
						ContentValues p = new ContentValues();
						p.put("_id", n.getId());
						p.put("category1", "产品推荐");
						p.put("category2", leavel);
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
						db.insert("cache", null, p);
					} else {
						int index = c.getColumnIndex("hasRead");
						String result = c.getString(index);
						n.setHasChecked(result == null ? false : result
								.equals("1"));
					}
				}

				mHandler.sendEmptyMessage(DIRWHAT);

			} catch (MalformedURLException e) {
				e.printStackTrace();
				mHandler.sendEmptyMessage(ERRORWHAT);
			} catch (IOException e) {
				e.printStackTrace();
				mHandler.sendEmptyMessage(ERRORWHAT);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				mHandler.sendEmptyMessage(ERRORWHAT);
			} catch (SAXException e) {
				e.printStackTrace();
				mHandler.sendEmptyMessage(ERRORWHAT);
			}
		}
	}

	static class MHandler extends Handler {
		WeakReference<AssociationDirectoryActivity> act;

		MHandler(AssociationDirectoryActivity act) {
			this.act = new WeakReference<AssociationDirectoryActivity>(act);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			AssociationDirectoryActivity ac = act.get();
			switch (msg.what) {
			case ERRORWHAT:
				ac.progress.setVisibility(View.GONE);
				ac.dirListView.setVisibility(View.VISIBLE);
				ac.mAdapter.notifyDataSetChanged();
				Toast.makeText(ac, "网络链接错误,已经加载本地缓存", Toast.LENGTH_SHORT)
						.show();
				break;
			case DIRWHAT:
				ac.progress.setVisibility(View.GONE);
				ac.dirListView.setVisibility(View.VISIBLE);
				ac.mAdapter.notifyDataSetChanged();
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
				convertView = listContainer.inflate(R.layout.dir_row, null);
				listItemView.newsImage = (ImageView) convertView
						.findViewById(R.id.dirImage);
				listItemView.title = (TextView) convertView
						.findViewById(R.id.dirTitle);
				convertView.setTag(listItemView);
			} else {
				listItemView = (ListItemView) convertView.getTag();
			}

			// convertView.setOnClickListener(new OnClickListener() {
			//
			// @Override
			// public void onClick(View arg0) {
			// Intent i = new Intent(AssociationDirectoryActivity.this,
			// PassageActivity.class);
			// i.putExtra("passageId", passages.get(p).getId());
			// startActivity(i);
			// }
			// });

			listItemView.title.setText(passages.get(arg0).getTitle());

			if (passages.get(arg0).isHasChecked()) {
				listItemView.title.setTextColor(Color.GRAY);
			} else {
				listItemView.title.setTextColor(Color.BLACK);
			}

			if (passages.get(arg0).getImage() != null) {
				listItemView.newsImage.setImageBitmap(passages.get(arg0)
						.getRealImag());
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
