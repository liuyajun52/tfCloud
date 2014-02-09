package com.blacklighting.tianfuyunv2;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.blacklighting.tianfuyunv2.dompraser.PassageParser;
import com.blacklighting.tianfuyunv2.internet.ApiAddress;
import com.blacklighting.tianfuyunv2.internet.StreamGetter;
import com.blacklighting.tianfuyunv2.models.Passage;

public class PassageActivity extends Activity {
	final static int ERRORWHAT = 0, PASSAGEWHAT = 1;
	MHandler mHandler = new MHandler(this);
	TextView title, source, time;
	WebView content;
	Passage passage;
	byte[] realImage;
	String passageId;
	private SQLiteDatabase db;
	boolean hasInDb;
	private SQLiteDatabase dbcache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_passage);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		// intentPassage=(Passage)
		// getIntent().getBundleExtra("passage").getSerializable(null);
		// passageId = intentPassage.getId();

		passageId = getIntent().getStringExtra("id");
		realImage = getIntent().getByteArrayExtra("realImage");
		title = (TextView) findViewById(R.id.passageTitle);
		source = (TextView) findViewById(R.id.passageSource);
		time = (TextView) findViewById(R.id.passageCreateTime);
		content = (WebView) findViewById(R.id.pssageContent);

		db = openOrCreateDatabase("tianfuyun.db", Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS collection (_id INTEGER PRIMARY KEY , title VARCHAR, image VARCHAR,createTime VARCHAR, realImage BLOB, context VARCHAR)");
		dbcache = openOrCreateDatabase("cache.db", Context.MODE_PRIVATE, null);
		dbcache.execSQL("CREATE TABLE IF NOT EXISTS cache (_id INTEGER PRIMARY KEY ,category1 VARCHAR,category2 VARCHAR,period VARCHAR, title VARCHAR, image VARCHAR,createTime VARCHAR, realImage BLOB, hasRead VARCHAR,source VARCHAR,content VARCHAR)");

		Cursor c = db.rawQuery("select * from collection where _id=?",
				new String[] { passageId });
		hasInDb = (c.getCount() != 0);
		new GetPassageThread().start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.passage, menu);

		if (hasInDb) {
			MenuItem item = menu.findItem(R.id.collecMenu);
			item.setIcon(android.R.drawable.btn_star_big_on);
			item.setTitle("取消收藏");
		} else {
			MenuItem item = menu.findItem(R.id.collecMenu);
			item.setIcon(android.R.drawable.btn_star_big_off);
			item.setTitle("收藏");
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.collecMenu:
			if (hasInDb) {
				Builder b = new Builder(PassageActivity.this);
				b.setTitle("是否从收藏夹中移除")
						.setNegativeButton("取消", null)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										db.delete("collection", "_id=?",
												new String[] { passageId });
										Toast.makeText(getApplicationContext(),
												"已经从收藏夹中删除", Toast.LENGTH_SHORT)
												.show();
										hasInDb = false;
										item.setIcon(android.R.drawable.btn_star_big_off);
										item.setTitle("收藏");
									}
								});
				b.create().show();

			} else {
				Builder b = new Builder(PassageActivity.this);
				b.setTitle("是否加入收藏夹")
						.setNegativeButton("取消", null)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										ContentValues p = new ContentValues();
										p.put("_id", passageId);
										p.put("title", passage.getTitle());
										p.put("createTime",
												passage.getCreateTime());
										if (passage.getImage() != null) {
											p.put("image", passage.getImage());
											p.put("realImage", realImage);
										} else {
											p.put("image", "");
											p.put("realImage", new byte[64]);
										}
										p.put("context", passage.getContent());
										db.replace("collection", null, p);

										Toast.makeText(getApplicationContext(),
												"添加成功", Toast.LENGTH_SHORT)
												.show();
										hasInDb = true;
										item.setIcon(android.R.drawable.btn_star_big_on);
										item.setTitle("取消收藏");
									}
								});
				b.create().show();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class GetPassageThread extends Thread {

		@Override
		public void run() {
			super.run();
			Map<String, String> params = new HashMap<String, String>();
			params.put("passageId", passageId);
			StreamGetter getter = new StreamGetter(ApiAddress.getPASSAGE_API(),
					params);
			try {
				InputStream in = getter.getIn();
				passage = PassageParser.paerser(in);
				mHandler.sendEmptyMessage(PASSAGEWHAT);

				ContentValues c = new ContentValues();
				c.put("source", passage.getSource());
				c.put("content", passage.getContent());
				dbcache.update("cache", c, "_id=?", new String[] { passageId });
			} catch (Exception e) {
				passage = new Passage();
				Cursor p = dbcache.rawQuery("select * from cache where _id =?",
						new String[] { passageId });
				if (p.moveToFirst()) {
					passage.setTitle(p.getString(p.getColumnIndex("title")));
					passage.setCreateTime(p.getString(p
							.getColumnIndex("createTime")));
					passage.setSource(p.getString(p.getColumnIndex("source")));
					passage.setContent(p.getString(p.getColumnIndex("content")));
				}
				mHandler.sendEmptyMessage(ERRORWHAT);
				e.printStackTrace();
			}

		}
	}

	static class MHandler extends Handler {
		WeakReference<PassageActivity> mActivity;

		public MHandler(PassageActivity mActivity) {
			this.mActivity = new WeakReference<PassageActivity>(mActivity);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			PassageActivity mact = mActivity.get();
			switch (msg.what) {
			case ERRORWHAT:
				mact.title.setText(mact.passage.getTitle());
				mact.source.setText(mact.passage.getSource());
				mact.time.setText(mact.passage.getCreateTime());
				mact.content.loadDataWithBaseURL(ApiAddress.getBaseURL(),
						mact.passage.getContent(), null, "UTF-8", null);
//				Toast.makeText(mact, "网络连接错误，已经加载本地缓存，注意，如果您还为阅读该文章，文章内容将无法显示",
//						Toast.LENGTH_SHORT).show();
				break;
			case PASSAGEWHAT:
				mact.title.setText(mact.passage.getTitle());
				mact.source.setText(mact.passage.getSource());
				mact.time.setText(mact.passage.getCreateTime());
				mact.content.loadDataWithBaseURL(ApiAddress.getBaseURL(),
						mact.passage.getContent(), null, "UTF-8", null);
				break;
			default:
				break;
			}
		}

	}

}
