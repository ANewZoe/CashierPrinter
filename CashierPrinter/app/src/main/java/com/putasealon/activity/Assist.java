package com.putasealon.activity;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class Assist {
	public static List<Activity> activity_list = new ArrayList<Activity>();// activity集合
	public static String serviceIp = "";
	public static String sid = "";
	public static int ipcount = 0;

	/*
	 * 添加activity
	 */
	public static void addActivity(Activity activity) {
		System.out.println("添加" + activity.getLocalClassName());
		activity_list.add(activity);
	}

	/*
	 * 销毁单个activity
	 */
	public static void removeActivity(Activity activity) {
		for (int i = 0; i < activity_list.size(); i++) {
			if (activity_list.get(i) == activity) {
				activity_list.remove(i);
				System.out.println("移除" + activity.getLocalClassName());
				break;
			}
		}

	}

	/*
	 * 销毁所有activity
	 */
	public static void destroySallActivity() {
		for (int i = 0; i < activity_list.size(); i++) {
			System.out.println("销毁" + activity_list.get(i).getLocalClassName());
			activity_list.get(i).finish();
		}
	}

}
