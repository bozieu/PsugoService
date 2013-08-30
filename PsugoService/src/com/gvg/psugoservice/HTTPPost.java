package com.gvg.psugoservice;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;
//commentaire hervé

public class HTTPPost {
	HttpClient httpclient;
	HttpPost httppost;

	public HTTPPost(String serverAddress) {
		// Create a new HttpClient and Post Header
		httpclient = new DefaultHttpClient();
		httppost = new HttpPost(serverAddress);
	}

	public int Send(List<NameValuePair> data) {
		try {
			httppost.setEntity(new UrlEncodedFormEntity(data));
			// Execute HTTP Post Request
			httpclient.execute(httppost);
		} catch (Exception e) {
			Log.e("error", "HTTPPost: " + e.getLocalizedMessage());
			return -1;
		}
		return 1;
	}
}
