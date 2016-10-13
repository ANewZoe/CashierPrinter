package com.putasealon.thread;

import android.os.Handler;

import com.putasealon.activity.Assist;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

//获取打印字符串线程
public class PrintString extends Thread {
	private String url = "";
	private String printString;
	private Handler psHandler;
	public static final int SUCCESS = 0;
	public int visit = 0;

	public PrintString(Handler psHandler, String url) {
		super();
		this.url = url;
		this.psHandler = psHandler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		while (true) {
			if (visit == 0) {
				visit = 1;
				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = null;
				try {
					String s = "";
					if (Assist.ipcount == 1) {
						s = String.format(url, Assist.serviceIp + "/eat_bendi");
					} else if (Assist.ipcount == 0) {
						s = String.format(url, "9981it.com" + "/eat_online");
						s += "&sid=" + Assist.sid;
					}
					response = httpclient.execute(new HttpPost(s));
					if (response.getStatusLine().getStatusCode() == 200) {
						printString = EntityUtils.toString(response.getEntity());
						psHandler.sendEmptyMessage(PrintString.SUCCESS);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					visit = 0;
					e.printStackTrace();
				} finally {
					if (null != httpclient) {
						httpclient.getConnectionManager().shutdown();
					}
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public String getPrintString() {

		return printString;
	}

}
