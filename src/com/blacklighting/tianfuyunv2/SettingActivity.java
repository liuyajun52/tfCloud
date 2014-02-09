package com.blacklighting.tianfuyunv2;

import com.blacklighting.tianfuyunv2.push.PushServer;

import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

/**
 * @author liuyajun
 *
 */
public class SettingActivity extends PreferenceActivity {
	public static Boolean loadImage=true;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Show the Up button in the action bar.
		setupActionBar();
		addPreferencesFromResource(R.xml.setting);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		sp.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences arg0,
					String arg1) {
				if (arg1.equals("loagImagWithoutWifi")) {
					Boolean loagImagWithoutWifi = arg0.getBoolean(
							"loagImagWithoutWifi", true);
					loadImage=loagImagWithoutWifi;
				} else if (arg1.equals("push")) {
					Boolean loagImagWithoutWifi = arg0.getBoolean("push", true);
					if(loagImagWithoutWifi){
						startService(new Intent(SettingActivity.this,PushServer.class));
					}
				}

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

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		
		String key = preference.getKey();
		
		if(key.equals("update")){
			Builder builder=new Builder(SettingActivity.this);
			builder.setTitle("软件更新");
			builder.setMessage("已经是最新版本");
			builder.setPositiveButton("确定", null);
			builder.create().show();
		}else if(key.equals("about")){
			startActivity(new Intent(this,AboutActivity.class));
		}
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	
	
}
