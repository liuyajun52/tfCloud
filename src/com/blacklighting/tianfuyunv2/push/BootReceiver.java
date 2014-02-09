/**
 * 
 */
package com.blacklighting.tianfuyunv2.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Liu Yajun@blacklighting UESTC
 * 
 */
public class BootReceiver extends BroadcastReceiver {

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 * android.content.Intent)
	 */
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Intent push = new Intent(arg0, PushServer.class);
		arg0.startService(push);
	}

}
