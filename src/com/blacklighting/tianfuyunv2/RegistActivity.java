package com.blacklighting.tianfuyunv2;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.blacklighting.tianfuyunv2.dompraser.UserParser;
import com.blacklighting.tianfuyunv2.internet.ApiAddress;
import com.blacklighting.tianfuyunv2.internet.StreamGetter;
import com.blacklighting.tianfuyunv2.models.User;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class RegistActivity extends Activity implements OnClickListener {

	static final int ERRORWHAT = 0, USERWHAT = 1, ERRORRESWHAT = 2;

	EditText emaiE, pwE, comE, nameE, depE, ideE, phE,rpwE;

	MHandler mHandler = new MHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_regist);
		// Show the Up button in the action bar.
		setupActionBar();

		findViewById(R.id.rButton).setOnClickListener(this);

		emaiE = (EditText) findViewById(R.id.rEmail);
		pwE = (EditText) findViewById(R.id.rPasswd);
		comE = (EditText) findViewById(R.id.rCompany);
		nameE = (EditText) findViewById(R.id.rName);
		depE = (EditText) findViewById(R.id.rDep);
		ideE = (EditText) findViewById(R.id.rIde);
		phE = (EditText) findViewById(R.id.rPhone);
		rpwE=(EditText) findViewById(R.id.repatPasswd);
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
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rButton:
			if (!emaiE.getText().toString().contains("@")) {
				Toast.makeText(getApplicationContext(), "无效的电子邮件地址",
						Toast.LENGTH_SHORT).show();
			} else if (pwE.getText().toString().length() < 6) {
				Toast.makeText(getApplicationContext(), "密码最少六位",
						Toast.LENGTH_SHORT).show();
			} else if(!rpwE.getText().toString().equals(pwE.getText().toString())){
				Toast.makeText(getApplicationContext(), "密码不一致",
						Toast.LENGTH_SHORT).show();
			}else if (comE.getText().toString().length() == 0) {
				Toast.makeText(getApplicationContext(), "没有填写公司",
						Toast.LENGTH_SHORT).show();
			} else if(nameE.getText().toString().length()==0){
				Toast.makeText(getApplicationContext(), "没有填写姓名",
						Toast.LENGTH_SHORT).show();				
			}else if(depE.getText().toString().length()==0){
				Toast.makeText(getApplicationContext(), "没有填写部门",
						Toast.LENGTH_SHORT).show();				
			}else if(ideE.getText().toString().length()==0){
				Toast.makeText(getApplicationContext(), "没有填写职位",
						Toast.LENGTH_SHORT).show();				
			}else if(phE.getText().toString().length()<6){
				Toast.makeText(getApplicationContext(), "无效的电话",
						Toast.LENGTH_SHORT).show();				
			}else{
				new RegistThred(emaiE.getText().toString(), pwE.getText()
						.toString(), comE.getText().toString(), nameE.getText()
						.toString(), depE.getText().toString(), ideE.getText()
						.toString(), phE.getText().toString()).start();
			}
			break;
		}
	}

	class RegistThred extends Thread {

		String email, password, company, name, department, identify, phone;

		public RegistThred(String email, String password, String company,
				String name, String department, String identify, String phone) {
			this.email = email;
			this.password = password;
			this.company = company;
			this.name = name;
			this.department = department;
			this.identify = identify;
			this.phone = phone;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();

			Map<String, String> params = new HashMap<String, String>();

			params.put("email", email);
			params.put("password", password);
			params.put("company", company);
			params.put("name", name);
			params.put("department", department);
			params.put("identify", identify);
			params.put("phone", phone);

			try {
				InputStream in = new StreamGetter(ApiAddress.getREISTER_API(),
						params).getIn();
				if(in==null){
					mHandler.sendEmptyMessage(ERRORWHAT);
					return;
				}
				User user = UserParser.parse(in);

				if (user.getName() != null) {
					SharedPreferences sharedPreferences = getSharedPreferences(
							"user", Context.MODE_PRIVATE);
					Editor editor = sharedPreferences.edit();// 获取编辑器
					editor.putString("name", user.getName());
					editor.putString("company", user.getCompanyName());
					editor.putString("companyId", user.getCompanyId());
					editor.putString("department", user.getDepartment());
					editor.putString("phone", user.getPhone());
					editor.putString("token", user.getToken());
					editor.putString("identify", user.getIdentify());
					editor.commit();// 提交修改
					mHandler.sendEmptyMessage(USERWHAT);
				} else {
					mHandler.sendEmptyMessage(ERRORRESWHAT);
				}
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
		WeakReference<RegistActivity> rea;

		public MHandler(RegistActivity rea) {
			this.rea = new WeakReference<RegistActivity>(rea);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			RegistActivity r = rea.get();
			switch (msg.what) {
			case ERRORWHAT:
				Toast.makeText(r, "网络链接错误", Toast.LENGTH_SHORT).show();
				break;
			case ERRORRESWHAT:
				Toast.makeText(r, "公司不存在", Toast.LENGTH_SHORT).show();
				break;
			case USERWHAT:
				Toast.makeText(r, "注册成功！", Toast.LENGTH_SHORT).show();
				Intent userInfo = new Intent(r, UserInfoActivity.class);
				r.startActivity(userInfo);
				break;

			}
		}

	}

}
