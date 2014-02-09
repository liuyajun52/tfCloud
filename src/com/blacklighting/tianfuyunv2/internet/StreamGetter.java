/**
 * 
 */
package com.blacklighting.tianfuyunv2.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import android.util.Log;

/**
 * @author Liu Yajun@blacklighting UESTC
 * 
 */
public class StreamGetter {
	String url;
	Map<String, String> params;
	InputStream in = null;
	OutputStream out = null;

	public StreamGetter(String url, Map<String, String> params) {
		this.url = url;
		this.params = params;
	}

	public InputStream getIn() throws MalformedURLException, IOException {
		if (in != null)
			;
		else {

			HttpURLConnection conn = null;
			byte[] data = getRequestData(params, "UTF-8").toString().getBytes();// 获得请求体

			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(5000);
			conn.setDoInput(true); // 打开输入流，以便从服务器获取数据
			conn.setDoOutput(true); // 打开输出流，以便向服务器提交数据
			conn.setRequestMethod("POST"); // 设置以Post方式提交数据

			conn.setRequestProperty("Content-Length",
					String.valueOf(data.length));
			// 获得输出流，向服务器写入数据
			out = conn.getOutputStream();
			out.write(data);

			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				in = conn.getInputStream();
			} else {
				Log.v("ERROR", "链接错误");
				throw new IOException();
			}

		}

		return in;
	}

	public OutputStream getOut() throws MalformedURLException, IOException {
		if (out != null)
			;
		else {

			HttpURLConnection conn = null;
			byte[] data = getRequestData(params, "UTF-8").toString().getBytes();// 获得请求体

			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(5000);
			conn.setDoInput(true); // 打开输入流，以便从服务器获取数据
			conn.setDoOutput(true); // 打开输出流，以便向服务器提交数据
			conn.setRequestMethod("POST"); // 设置以Post方式提交数据

			conn.setRequestProperty("Content-Length",
					String.valueOf(data.length));
			// 获得输出流，向服务器写入数据
			out = conn.getOutputStream();
			out.write(data);

			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				in = conn.getInputStream();
			} else {
				Log.v("ERROR", "链接错误");
				throw new IOException();
			}

		}

		return out;
	}

	public StringBuffer getRequestData(Map<String, String> params, String encode) {

		StringBuffer stringBuffer = new StringBuffer(); // 存储封装好的请求体信息
		try {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				stringBuffer.append(entry.getKey()).append("=")
						.append(URLEncoder.encode(entry.getValue(), encode))
						.append("&");
			}
			stringBuffer.deleteCharAt(stringBuffer.length() - 1); // 删除最后的一个"&"
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringBuffer;
	}

}
