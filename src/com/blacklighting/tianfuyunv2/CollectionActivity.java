package com.blacklighting.tianfuyunv2;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blacklighting.tianfuyunv2.models.Passage;

public class CollectionActivity extends Activity {
	SQLiteDatabase db;
	ListView list;
	DataBaseAdapter adapter;
	List<Passage> passages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collection);
		// Show the Up button in the action bar.
		setupActionBar();
		list = (ListView) findViewById(R.id.collectionList);
		db = openOrCreateDatabase("tianfuyun.db", Context.MODE_PRIVATE, null);
		db.execSQL("CREATE TABLE IF NOT EXISTS collection (_id INTEGER PRIMARY KEY , title VARCHAR, image VARCHAR,createTime VARCHAR, realImage BLOB, context VARCHAR)");

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				String passageId = passages.get(arg2).getId();
				Intent i = new Intent(CollectionActivity.this,
						PassageActivity.class);
				i.putExtra("id", passageId);
				startActivity(i);

			}
		});

		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				Builder b = new Builder(CollectionActivity.this);
				b.setTitle("是否从收藏夹中移除")
						.setNegativeButton("取消", null)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {				
										String title = passages.get(arg2).getTitle();
										passages.remove(arg2);
										adapter.notifyDataSetChanged();
										String[] titles = new String[1];
										titles[0] = title;
										db.delete("collection", "title=?", titles);
										Toast.makeText(getApplicationContext(), "已经从收藏夹中删除",
												Toast.LENGTH_SHORT).show();}
								});
				b.create().show();

				return true;
			}
		});
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		passages = new ArrayList<Passage>();
		Cursor c = db.query("collection", null, null, null, null, null, "_id desc");
		while (c.moveToNext()) {
			Passage passage = new Passage();
			passage.setId(c.getString(0));
			passage.setTitle(c.getString(1));
			passage.setImage(c.getString(2));
			passage.setCreateTime(c.getString(3));
			byte[] in = c.getBlob(4);
			if (in != null) {
				passage.setRealImag(BitmapFactory.decodeByteArray(in, 0,
						in.length));
			}
			passages.add(passage);
		}
		adapter = new DataBaseAdapter(passages, this);
		list.setAdapter(adapter);
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

	class DataBaseAdapter extends BaseAdapter {
		Context context;
		List<Passage> passages;

		public DataBaseAdapter(List<Passage> passages, Context context) {
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
			passageTitle.setTextColor(Color.GRAY);
			passageTime.setText(passages.get(arg0).getCreateTime());
			if (passages.get(arg0).getImage() != null
					&& passages.get(arg0).getRealImag() != null) {
				passageImage.setImageBitmap(passages.get(arg0).getRealImag());
			}
			return arg1;
		}

	}
}
