package com.putasealon.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.example.putasealon.R;

/*
 * 设置界面
 */
public class Set_Activity extends Activity implements OnClickListener {
	private SharedPreferences spf;
	private Editor editor;
	private Button changeip_button, exit_button, clean_button, return_set_button;
	private EditText iplocation_edittext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_activity);
		initview();

	}

	private void initview() {
		// TODO Auto-generated method stub
		spf = this.getSharedPreferences(MainActivity.ipXmlFiles, Activity.TRIM_MEMORY_COMPLETE);
		editor = spf.edit();
		Assist.addActivity(Set_Activity.this);
		changeip_button = (Button) this.findViewById(R.id.changeip_button);
		clean_button = (Button) this.findViewById(R.id.clean_button);
		exit_button = (Button) this.findViewById(R.id.exit_button);
		return_set_button = (Button) this.findViewById(R.id.return_set_button);
		changeip_button.setOnClickListener(this);
		clean_button.setOnClickListener(this);
		exit_button.setOnClickListener(this);
		return_set_button.setOnClickListener(this);
		iplocation_edittext = (EditText) this.findViewById(R.id.iplocation_edittext);
		iplocation_edittext.setEnabled(false);
		RadioGroup ip_radiogroup = (RadioGroup) this.findViewById(R.id.ip_radiogroup);
		ip_radiogroup.setOnCheckedChangeListener(RadioGroupListener);
		//键盘键
		GridView keyboard_gridview = (GridView) this.findViewById(R.id.keyboard_gridview);
		if (0 == spf.getInt("ipcount", 0)) {
			((RadioButton) ip_radiogroup.getChildAt(0)).setChecked(true);
			iplocation_edittext.setHint("当前餐厅ID：" + Assist.sid);
		} else if (1 == spf.getInt("ipcount", 0)) {
			((RadioButton) ip_radiogroup.getChildAt(1)).setChecked(true);
			iplocation_edittext.setHint("当前服务端IP地址：" + Assist.serviceIp);
		}
		Keyboard_Grid_Adapter kgAdapter = new Keyboard_Grid_Adapter(Set_Activity.this, iplocation_edittext);
		keyboard_gridview.setAdapter(kgAdapter);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == changeip_button) {
			if (!"".equals(iplocation_edittext.getText().toString().trim())) {
				if (Assist.ipcount == 0) {
					editor.putString("sid", iplocation_edittext.getText().toString().trim());
					editor.commit();
					Assist.sid = spf.getString("sid", null);
					iplocation_edittext.setText("");
					iplocation_edittext.setHint("当前餐厅ID：" + Assist.sid);
				} else if (Assist.ipcount == 1) {
					editor.putString("serviceip", iplocation_edittext.getText().toString().trim());
					editor.commit();
					Assist.serviceIp = spf.getString("serviceip", null);
					iplocation_edittext.setText("");
					iplocation_edittext.setHint("当前服务端IP地址：" + Assist.serviceIp);
				}
			}
		} else if (v == clean_button) {
			iplocation_edittext.setText("");
		} else if (v == exit_button) {
			exitDialog();
		} else if (v == return_set_button) {
			Set_Activity.this.finish();
		}
	}

	OnCheckedChangeListener RadioGroupListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			if (checkedId == R.id.sid_radiobutton) {
				Assist.ipcount = 0;
				editor.putInt("ipcount", 0);
				editor.commit();
				iplocation_edittext.setHint("当前餐厅ID：" + Assist.sid);
			} else if (checkedId == R.id.setviceip_radiobutton) {
				Assist.ipcount = 1;
				editor.putInt("ipcount", 1);
				editor.commit();
				iplocation_edittext.setHint("当前服务端IP地址：" + Assist.serviceIp);
			}

		}
	};

	private void exitDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(Set_Activity.this);
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
				Assist.destroySallActivity();
				System.exit(0);

			}
		});
		dialog.show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Assist.removeActivity(Set_Activity.this);
	}
}
