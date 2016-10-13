package com.putasealon.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.example.putasealon.R;

@SuppressLint("InflateParams")
public class Keyboard_Grid_Adapter extends BaseAdapter {
	private Context context;
	private EditText iplocation_edittext;
	String[] keyboardSr = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ".", "a", "s", "d", "f", "g", "h", "j",
			"k", "l", "q", "w", "e", "r", "u", "o", "p", "z", "c", "v", "b", "n", "m", "i", "t", "x", "/", "*", "@",
			"#", "%", "←", };

	public Keyboard_Grid_Adapter(Context context, EditText iplocation_edittext) {
		super();
		this.context = context;
		this.iplocation_edittext = iplocation_edittext;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return keyboardSr.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return keyboardSr[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (null == convertView) {
			convertView = LayoutInflater.from(context).inflate(R.layout.keyboard_grid_item, null);
		}
		Button item_button = (Button) convertView.findViewById(R.id.item_button);
		item_button.setText(keyboardSr[position]);
		item_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if ("←".equals(((Button) v).getText().toString().trim())) {
					String ips = iplocation_edittext.getText().toString().trim();
					if (ips.length() > 0) {
						iplocation_edittext.setText(ips.substring(0, ips.length() - 1));
					}
				} else if ("清空".equals(((Button) v).getText().toString().trim())) {
					iplocation_edittext.setText("");
				} else {
					String ips = iplocation_edittext.getText().toString().trim();
					iplocation_edittext.setText(ips + ((Button) v).getText().toString().trim());
				}
			}
		});
		return convertView;
	}

}
