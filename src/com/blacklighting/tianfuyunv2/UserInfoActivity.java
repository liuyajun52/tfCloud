package com.blacklighting.tianfuyunv2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class UserInfoActivity extends Activity implements OnClickListener {
	TextView userName;
	TextView userCompany;
	TextView userDepartment;
	TextView userIdentify;
	TextView usePhone;
	Button logoutButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_info);
		// Show the Up button in the action bar.
		setupActionBar();

		userName = (TextView) findViewById(R.id.userEmail);
		userCompany = (TextView) findViewById(R.id.userCompany);
		userDepartment = (TextView) findViewById(R.id.userDepartment);
		userIdentify = (TextView) findViewById(R.id.userIdentify);
		usePhone = (TextView) findViewById(R.id.userPhone);
		logoutButton = (Button) findViewById(R.id.logoutButton);
		logoutButton.setOnClickListener(this);

		SharedPreferences sharedPreferences = getSharedPreferences("user",
				Context.MODE_PRIVATE);

		userName.setText(sharedPreferences.getString("name", ""));
		userCompany.setText(sharedPreferences.getString("company", ""));
		userDepartment.setText(sharedPreferences.getString("department", ""));
		userIdentify.setText(sharedPreferences.getString("identify", ""));
		usePhone.setText(sharedPreferences.getString("phone", ""));
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.logoutButton:
			SharedPreferences sharedPreferences = getSharedPreferences("user",
					Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			editor.clear();
			editor.commit();
			finish();
			break;
		}
	}

}
