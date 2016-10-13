package com.putasealon.thread;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

///发送打印结果
public class HttpThread extends Thread {
	private String url = "";
	private PrintString ps;

	public HttpThread(String url, PrintString ps) {
		super();
		this.url = url;
		this.ps = ps;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse request = null;
		try {
			request = httpclient.execute(new HttpPost(url));
			if (request.getStatusLine().getStatusCode() == 200) {
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ps.visit = 0;
			if (null != httpclient) {
				httpclient.getConnectionManager().shutdown();
			}
		}
	}
}
