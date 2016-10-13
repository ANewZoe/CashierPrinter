package com.putasealon.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

import com.example.putasealon.R;
import com.putasealon.print.CollectMoneyPrint;

import java.util.ArrayList;
import java.util.List;

/*
 * 主界面
 */
public class MainActivity extends Activity implements OnClickListener {

	public static String ipXmlFiles = "serviceFile";
	private SharedPreferences spf;
	private List<Stamp> printList;
	private PrintListAdapter plAdapter;
	private ListView print_list;
	private Button set_button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_activity);
		initsIp();
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		Assist.addActivity(MainActivity.this);
		CollectMoneyPrint cmp = null;
		set_button = (Button) this.findViewById(R.id.set_button);
		set_button.setOnClickListener(this);
		printList = new ArrayList<Stamp>();
		//打印信息
		print_list = (ListView) this.findViewById(R.id.print_list);
		plAdapter = new PrintListAdapter(printList, print_list, cmp, MainActivity.this);
		print_list.setAdapter(plAdapter);
		cmp = new CollectMoneyPrint(plAdapter);
		plAdapter.cmp = cmp;
		cmp.cmpInitPrint();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == set_button) {
			Intent intent = new Intent(MainActivity.this, Set_Activity.class);
			MainActivity.this.startActivity(intent);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == 4) {
			exitDialog();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void exitDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
		dialog.setTitle("温馨提示");
		dialog.setMessage("确定要退出程序？");
		dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		dialog.setPositiveButton("退出", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				System.exit(0);
				Assist.destroySallActivity();
			}
		});
		dialog.show();
	}

	// 初始化服务端ip地址ַ
	private void initsIp() {
		spf = this.getSharedPreferences(MainActivity.ipXmlFiles, Activity.TRIM_MEMORY_COMPLETE);
		if (spf.getInt("ipcount", 0) == 0) {
			Assist.ipcount = 0;
		} else if (spf.getInt("ipcount", 0) == 1) {
			Assist.ipcount = 1;
		}
		Assist.serviceIp = spf.getString("serviceip", null);
		Assist.sid = spf.getString("sid", null);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Assist.removeActivity(MainActivity.this);
	}

}
