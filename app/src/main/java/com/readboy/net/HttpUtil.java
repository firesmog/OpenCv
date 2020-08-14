package com.readboy.net;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.View;

import com.readboy.bean.newexam.RectangleInfo;
import com.readboy.log.LogUtils;
import com.readboy.myopencvcamera.TakePhotoActivity;
import com.readboy.util.AnimatorUtil;
import com.readboy.util.BitmapUtils;
import com.readboy.util.HandleImgUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * Http请求工具类
 */
public class HttpUtil {
	private HttpUtil() {

	}

	public static String doPost(String url, Map<String, String> header, String body,int type) {
		String result = "";
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			// 设置 url
			URL realUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
			// 设置 header
			for (String key : header.keySet()) {
				connection.setRequestProperty(key, header.get(key));
			}
			// 设置请求 body
			connection.setDoOutput(true);
			connection.setDoInput(true);

			//设置连接超时和读取超时时间
			connection.setConnectTimeout(20000);
			connection.setReadTimeout(20000);
			try {
				out = new PrintWriter(connection.getOutputStream());
				// 保存body
				out.print(body);
				// 发送body
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				// 获取响应body
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while ((line = in.readLine()) != null) {
					result += line;
				}
			} catch (Exception e) {
				LogUtils.e(e.getMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.e(e.getMessage());
//			return null;
		}
		return result;
	}


	/**
	 * 发送post请求
	 * 
	 * @param url
	 * @param header
	 * @param body
	 * @return
	 */
	public static Observable<String> doPost(final String url, final Map<String, String> header, final String body) {

		Observable<String>  observable = Observable.create(new ObservableOnSubscribe<String>() {
			// 1. 创建被观察者 & 生产事件
			@Override
			public void subscribe(ObservableEmitter<String> emitter) {
				String result = "";
				BufferedReader in = null;
				PrintWriter out = null;
				try {
					// 设置 url
					URL realUrl = new URL(url);
					HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
					// 设置 header
					for (String key : header.keySet()) {
						connection.setRequestProperty(key, header.get(key));
					}
					// 设置请求 body
					connection.setDoOutput(true);
					connection.setDoInput(true);

					//设置连接超时和读取超时时间
					connection.setConnectTimeout(20000);
					connection.setReadTimeout(20000);
					try {
						out = new PrintWriter(connection.getOutputStream());
						// 保存body
						out.print(body);
						// 发送body
						out.flush();
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						// 获取响应body
						in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						String line;
						while ((line = in.readLine()) != null) {
							result += line;
						}
					} catch (Exception e) {
						LogUtils.e(e.getMessage());
					}
				} catch (Exception e) {
					e.printStackTrace();
					LogUtils.e(e.getMessage());
//			return null;
				}
				emitter.onNext(result);
				emitter.onComplete();
			}
		});
		return observable;
	}

	/**
	 * 发送get请求
	 * 
	 * @param url
	 * @param header
	 * @return
	 */
	public static String doGet(String url, Map<String, String> header) {
		String result = "";
		BufferedReader in = null;
		try {
			// 设置 url
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			// 设置 header
			for (String key : header.keySet()) {
				connection.setRequestProperty(key, header.get(key));
			}
			// 设置请求 body
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			return null;
		}
		return result;
	}
}
