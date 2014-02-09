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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;

import com.blacklighting.tianfuyunv2.dompraser.MagazineListParser;
import com.blacklighting.tianfuyunv2.internet.ApiAddress;
import com.blacklighting.tianfuyunv2.internet.StreamGetter;
import com.blacklighting.tianfuyunv2.models.MagazineListIteam;

public class MagazineListActivity extends Activity {

	public static final int ERRORWHAT = 0;
	public static final int MAGAZINELISTWHAT = 1;
	List<MagazineListIteam> magazineList;
	MHandler mHandler = new MHandler(this);
	static MAdapter adapter;
	ListView magazineListView;
	private SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mgazine_list);
		// Show the Up button in the action bar.
		setupActionBar();

		magazineListView = (ListView) findViewById(R.id.magazineList);

		magazineList = new ArrayList<MagazineListIteam>();

		adapter = new MAdapter(magazineList, this);
		magazineListView.setAdapter(adapter);

		magazineListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(getApplicationContext(),
						MagazineActivity.class);
				i.putExtra("period", magazineList.get(arg2).getPeriod());
				i.putExtra("magazineT", magazineList.get(arg2).getName());
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				if (magazineList.get(arg2).getRealImag() != null) {
					magazineList.get(arg2).getRealImag()
							.compress(Bitmap.CompressFormat.PNG, 100, os);
					i.putExtra("cover", os.toByteArray());
				}
				startActivity(i);

			}
		});

		db = openOrCreateDatabase("cache.db", Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS magazine (_id INTEGER PRIMARY KEY , name VARCHAR, image VARCHAR,realImage BLOB)");

		new GetMagazineListThread().start();
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

	class GetMagazineListThread extends Thread {

		@Override
		public void run() {
			super.run();
			StreamGetter mGetter = new StreamGetter(
					ApiAddress.getALL_MAGAZINE(), null);
			try {
				InputStream in = mGetter.getIn();
				magazineList.addAll(MagazineListParser.parse(in));
				mHandler.sendEmptyMessage(MAGAZINELISTWHAT);
				for (MagazineListIteam m : magazineList) {
					ContentValues ma = new ContentValues();
					ma.put("_id", m.getPeriod());
					ma.put("name", m.getName());
					ma.put("image", m.getImage());

					if (m.getImage() != null) {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						m.getRealImag().compress(Bitmap.CompressFormat.PNG,
								100, os);
						ma.put("realImage", os.toByteArray());
					}
					db.replace("magazine", null, ma);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Cursor magazineCache = db.query("magazine", null, null, null,
						null, null, "_id desc");

				while (magazineCache.moveToNext()) {
					MagazineListIteam m = new MagazineListIteam();
					m.setPeriod(magazineCache.getString(magazineCache
							.getColumnIndex("_id")));
					m.setName(magazineCache.getString(magazineCache
							.getColumnIndex("name")));
					m.setImage(magazineCache.getString(magazineCache
							.getColumnIndex("image")));

					if (m.getImage() != null || !m.getImage().isEmpty()) {
						byte[] in = magazineCache.getBlob(magazineCache
								.getColumnIndex("realImage"));
						m.setRealImag(BitmapFactory.decodeByteArray(in, 0,
								in.length));
					}
					magazineList.add(m);
				}

				mHandler.sendEmptyMessage(ERRORWHAT);
			}

		}
	}

	static class MHandler extends Handler {

		private WeakReference<MagazineListActivity> mActivity;

		MHandler(MagazineListActivity mgazineListActivity) {
			this.mActivity = new WeakReference<MagazineListActivity>(
					mgazineListActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case ERRORWHAT:
				Toast.makeText(mActivity.get(), "网络连接错误，已经加载本地缓存",
						Toast.LENGTH_SHORT).show();
				mActivity.get().findViewById(R.id.magazineListProgressBar)
						.setVisibility(View.GONE);
				mActivity.get().magazineListView.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
				break;
			case MAGAZINELISTWHAT:
				mActivity.get().findViewById(R.id.magazineListProgressBar)
						.setVisibility(View.GONE);
				mActivity.get().magazineListView.setVisibility(View.VISIBLE);
				adapter.notifyDataSetChanged();
				break;
			}

		}

	}

	class MAdapter extends BaseAdapter {
		Context context;
		private LayoutInflater listContainer;
		List<MagazineListIteam> magazines;

		MAdapter(List<MagazineListIteam> magazines, Context context) {
			this.context = context;
			this.magazines = magazines;
			listContainer = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return magazines.size();
		}

		@Override
		public Object getItem(int arg0) {
			return magazines.get(arg0);
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
				if (arg0 == 0) {
					convertView = listContainer.inflate(
							R.layout.last_magazine_row, null);
					listItemView.image = (ImageView) convertView
							.findViewById(R.id.lastMagazineCover);
					listItemView.name = (TextView) convertView
							.findViewById(R.id.lastMagazineTitle);
				} else {
					convertView = listContainer.inflate(
							R.layout.magazine_list_row, null);
					listItemView.image = (ImageView) convertView
							.findViewById(R.id.magazineCover);
					listItemView.name = (TextView) convertView
							.findViewById(R.id.magazineName);
					listItemView.peroid = (TextView) convertView
							.findViewById(R.id.magazinePeriod);
				}

				convertView.setTag(listItemView);
			} else {
				listItemView = (ListItemView) convertView.getTag();
			}

			listItemView.name.setText(magazines.get(arg0).getName());
			// listItemView.peroid.setText(magazines.get(arg0).getPeriod());
			listItemView.image
					.setImageBitmap(magazines.get(arg0).getRealImag());
			return convertView;
		}

		public final class ListItemView { // 自定义控件集合
			public ImageView image;;
			public TextView name;
			public TextView peroid;
		}

	}

}
