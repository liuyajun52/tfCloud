package com.blacklighting.tianfuyunv2;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blacklighting.tianfuyunv2.dompraser.PassageListParser;
import com.blacklighting.tianfuyunv2.internet.ApiAddress;
import com.blacklighting.tianfuyunv2.internet.StreamGetter;
import com.blacklighting.tianfuyunv2.models.PassageListIteam;
import com.blacklighting.tianfuyunv2.push.PushServer;

public class MainActivity extends Activity implements OnClickListener {
	private boolean isFirstIn;
	private static final String SHAREDPREFERENCES_NAME = "first_pref";
	final static int GUIDEWHAT = 0, ERRORWHAT = 1, ADWHAT = 2, IMAGEWHAT = 3;
	private Handler mHandler;
	ViewPager adsViewPage;
	List<View> ads;
	MyViewPagerAdapter adapter;
	TextView counter2;
	static boolean hasAds = false;
	private long exitTime = 0;
	private SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		adsViewPage = (ViewPager) findViewById(R.id.viewpager);
		counter2 = (TextView) findViewById(R.id.adsCounter2);

		ads = new ArrayList<View>();
		adapter = new MyViewPagerAdapter(ads);
		adsViewPage.setAdapter(adapter);

		adsViewPage.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				counter2.setText("" + (arg0 + 1) + "/" + ads.size());
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		ImageButton zxxxB = (ImageButton) findViewById(R.id.zxxxButton);
		ImageButton hyxhB = (ImageButton) findViewById(R.id.hyxhButton);
		ImageButton cyfwB = (ImageButton) findViewById(R.id.cyfwButton);
		ImageButton cdrjB = (ImageButton) findViewById(R.id.cdrjButton);
		ImageButton downloadB = (ImageButton) findViewById(R.id.collectionButton);
		ImageButton loginB = (ImageButton) findViewById(R.id.loginButton);
		ImageButton settingB = (ImageButton) findViewById(R.id.settingButton);
		ImageButton searchB = (ImageButton) findViewById(R.id.searchButton);

		zxxxB.setOnClickListener(this);
		hyxhB.setOnClickListener(this);
		cyfwB.setOnClickListener(this);
		cdrjB.setOnClickListener(this);
		downloadB.setOnClickListener(this);
		loginB.setOnClickListener(this);
		settingB.setOnClickListener(this);
		searchB.setOnClickListener(this);

		mHandler = new MHandler(this);

		// 读取SharedPreferences中需要的数据
		// 使用SharedPreferences来记录程序的使用次数
		SharedPreferences preferences = getSharedPreferences(
				SHAREDPREFERENCES_NAME, MODE_PRIVATE);

		// 取得相应的值，如果没有该值，说明还未写入，用true作为默认值
		isFirstIn = preferences.getBoolean("isFirstIn", true);

		SettingActivity.loadImage = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext())
				.getBoolean("loagImagWithoutWifi", true);
		if (isFirstIn) {
			mHandler.sendEmptyMessage(0);
			startService(new Intent(MainActivity.this,PushServer.class));
		}

		db = openOrCreateDatabase("cache.db", Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS cache (_id INTEGER PRIMARY KEY ,category1 VARCHAR,category2 VARCHAR,period VARCHAR, title VARCHAR, image VARCHAR,createTime VARCHAR, realImage BLOB, hasRead VARCHAR,source VARCHAR,content VARCHAR)");

		
		if (!hasAds) {
			new GetAdThread().start();
		}
	}

	static class MHandler extends Handler {

		private WeakReference<MainActivity> mActivity;

		MHandler(MainActivity mActivity) {
			this.mActivity = new WeakReference<MainActivity>(mActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case GUIDEWHAT:
				Intent guide = new Intent(mActivity.get(), GuideActivity.class);
				mActivity.get().startActivity(guide);

				break;
			case ERRORWHAT:
				Toast.makeText(mActivity.get(), "网络连接错误", Toast.LENGTH_SHORT)
						.show();
				// break; //特意注释掉，不要慌张，因为要要加载缓存
			case ADWHAT:
				@SuppressWarnings("unchecked")
				List<PassageListIteam> passgaes = (List<PassageListIteam>) msg
						.getData().getSerializable("ads");
				LayoutInflater inflat = mActivity.get().getLayoutInflater();

				for (int i = 0; i < passgaes.size(); i++) {
					final PassageListIteam p = passgaes.get(i);
					View ad = inflat.inflate(R.layout.ad, null);
					TextView title = (TextView) ad.findViewById(R.id.adTitle);
					title.setText(p.getTitle());

					if (p.getImage() != null) {
						ImageView im = (ImageView) ad
								.findViewById(R.id.adImage);
						im.setImageBitmap(p.getRealImag());
					}

					TextView position = (TextView) ad
							.findViewById(R.id.position);
					position.setText((i + 1) + "/" + passgaes.size());
					ad.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent ps = new Intent(mActivity.get(),
									PassageActivity.class);
							ps.putExtra("id", p.getId());
							mActivity.get().startActivity(ps);
						}
					});
					mActivity.get().ads.add(ad);

				}
				mActivity.get().counter2
						.setText("" + 1 + "/" + passgaes.size());
				mActivity.get().adapter.notifyDataSetChanged();

				break;
			case IMAGEWHAT:
				Bundle data = msg.getData();
				int index = data.getInt("index");
				byte[] imageByte = data.getByteArray("bytearray");
				ImageView image = (ImageView) mActivity.get().ads.get(index)
						.findViewById(R.id.adImage);
				image.setImageBitmap(BitmapFactory.decodeByteArray(imageByte,
						0, imageByte.length));
				break;
			default:
				break;
			}

		}

	}

	class GetAdThread extends Thread {

		@Override
		public void run() {
			super.run();
			StreamGetter mGetter = new StreamGetter(
					ApiAddress.getACTIVITY_API(), null);
			try {
				InputStream in = mGetter.getIn();
				List<PassageListIteam> passages = PassageListParser.parse(in);

				Message msg = new Message();
				msg.what = ADWHAT;
				Bundle data = new Bundle();
				data.putSerializable("ads", (Serializable) passages);
				msg.setData(data);
				mHandler.sendMessage(msg);

				for (PassageListIteam p : passages) {
					Cursor c = db.rawQuery(
							"select  *  from cache where _id = ?",
							new String[] { p.getId() });

					if (!c.moveToFirst()) {
						ContentValues cv = new ContentValues();
						cv.put("_id", p.getId());
						cv.put("category1", "首页新闻");
						cv.put("title", p.getTitle());
						cv.put("createTime", p.getCreateTime());
						cv.put("image", p.getImage());

						if (p.getImage() != null && !p.getImage().isEmpty()) {
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							p.getRealImag().compress(Bitmap.CompressFormat.PNG,
									100, os);
							cv.put("realImage", os.toByteArray());
						}
						db.insert("cache", null, cv);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				List<PassageListIteam> passages2 = new ArrayList<PassageListIteam>();
				Cursor a = db.rawQuery(
						"select * from cache where category1 = ?",
						new String[] { "首页新闻" });

				while (a.moveToNext()) {
					PassageListIteam p = new PassageListIteam();
					p.setId(a.getString(a.getColumnIndex("_id")));
					p.setTitle(a.getString(a.getColumnIndex("title")));
					p.setImage(a.getString(a.getColumnIndex("image")));

					if (p.getImage() != null && !p.getImage().isEmpty()) {
						byte[] in = a.getBlob(a.getColumnIndex("realImage"));
						p.setRealImag(BitmapFactory.decodeByteArray(in, 0,
								in.length));
					}
					passages2.add(p);
				}
				Message msg = new Message();
				msg.what = ERRORWHAT;
				Bundle data = new Bundle();
				data.putSerializable("ads", (Serializable) passages2);
				msg.setData(data);
				mHandler.sendMessage(msg);
			}

		}
	}

	class MyViewPagerAdapter extends PagerAdapter {
		private List<View> mListViews;

		public MyViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;// 构造方法，参数是我们的页卡，这样比较方便。
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));// 删除页卡
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) { // 这个方法用来实例化页卡
			container.addView(mListViews.get(position), 0);// 添加页卡
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();// 返回页卡的数量
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;// 官方提示这样写
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.zxxxButton:
			Intent zxxx = new Intent(this, NewsActivity.class);
			startActivity(zxxx);
			break;
		case R.id.cdrjButton:
			Intent cdrj = new Intent(this, MagazineListActivity.class);
			startActivity(cdrj);
			break;
		case R.id.cyfwButton:
			Intent cyfw = new Intent(this, ProductServerActivity.class);
			startActivity(cyfw);
			break;
		case R.id.loginButton:
			SharedPreferences preferences = getSharedPreferences("user",
					MODE_PRIVATE);
			if (preferences.contains("name")) {
				Intent userInfo = new Intent(this, UserInfoActivity.class);
				startActivity(userInfo);
			} else {
				Intent login = new Intent(this, LoginActivity.class);
				startActivity(login);
			}
			break;
		case R.id.settingButton:
			Intent setting = new Intent(this, SettingActivity.class);
			startActivity(setting);
			break;
		case R.id.collectionButton:
			Intent collection = new Intent(this, CollectionActivity.class);
			startActivity(collection);
			break;
		case R.id.hyxhButton:
			Intent hyxh = new Intent(this, CommActivity.class);
			startActivity(hyxh);
			break;
		case R.id.searchButton:
			Intent search = new Intent(this, SearchActivity.class);
			startActivity(search);
			break;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void exit() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(getApplicationContext(), "再按一次退出程序",
					Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			finish();
		}
	}
}
