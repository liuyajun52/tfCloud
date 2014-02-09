/**
 * 
 */
package com.blacklighting.tianfuyunv2.push;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.blacklighting.tianfuyunv2.NewsActivity;
import com.blacklighting.tianfuyunv2.R;
import com.blacklighting.tianfuyunv2.dompraser.PassageListParser;
import com.blacklighting.tianfuyunv2.internet.ApiAddress;
import com.blacklighting.tianfuyunv2.internet.StreamGetter;
import com.blacklighting.tianfuyunv2.models.PassageListIteam;

/**
 * @author Liu Yajun@blacklighting UESTC
 * 
 */
public class PushServer extends Service {
	private Notification mNotification;
	private NotificationManager mManager;
	SharedPreferences sp;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sp = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		initNotifiManager();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new PollingThread().start();
		return super.onStartCommand(intent, flags, startId);

	}

	// 初始化通知栏配置
	private void initNotifiManager() {
		mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotification = new Notification();
		mNotification.icon = R.drawable.ic_launcher;
		mNotification.tickerText = "推送新闻";
		mNotification.defaults |= Notification.DEFAULT_SOUND;
		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
	}

	// 弹出Notification
	@SuppressWarnings("deprecation")
	private void showNotification() {
		mNotification.when = System.currentTimeMillis();
		// Navigator to the new activity when click the notification title
		Intent i = new Intent(this, NewsActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);  
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mNotification.setLatestEventInfo(this,
				getResources().getString(R.string.app_name), "您有新的推送信息",
				pendingIntent);
		mManager.notify(0, mNotification);
	}

	class PollingThread extends Thread {
		boolean isNotStop = true;

		public PollingThread() {
			// TODO Auto-generated constructor stub

		}

		@Override
		public void run() {
			try {
				while (isNotStop) {

					isNotStop = sp.getBoolean("push", true);
					if (!isNotStop) {
						break;
					}
//					sleep(2);
					sleep(3*60*60*1000);
					int sinceId = sp.getInt("sinceId", 0);
					Map<String, String> params = new HashMap<String, String>();
					params.put("sinceId", "" + sinceId);
					InputStream in = new StreamGetter(
							ApiAddress.getACTIVITIES_API(), params).getIn();
					List<PassageListIteam> passages = PassageListParser
							.parse(in);
					if (passages.size() != 0) {
						showNotification();
					}
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
	}

}
