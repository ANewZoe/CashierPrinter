package com.putasealon.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.putasealon.R;
import com.putasealon.print.CollectMoneyPrint;

import org.json.JSONException;

import java.util.List;

/*
 * 打印日志列表
 */
@SuppressLint("InflateParams")
public class PrintListAdapter extends BaseAdapter {
	private List<Stamp> printList;
	private ListView print_list;
	public CollectMoneyPrint cmp;// 收银打印
	private int CURRENT_LISTVIEW_ITEM_POSITION = 0;// 用于记录当前listview的位置
	private Context context;

	public PrintListAdapter(List<Stamp> printList, ListView print_list, CollectMoneyPrint cmp, Context context) {
		super();
		this.print_list = print_list;
		this.printList = printList;
		this.cmp = cmp;
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (null == printList) {
			return 0;
		}
		return printList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (null == printList) {
			return 0;
		}
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		if (null == printList) {
			return 0;
		}
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		PrintTextview ptv;
		if (null == convertView) {
			convertView = LayoutInflater.from(context).inflate(R.layout.print_list_item, null);
			ptv = new PrintTextview();
			ptv.print_item_textview = (TextView) convertView.findViewById(R.id.print_item_textview);
			ptv.detailed_item_textview = (TextView) convertView.findViewById(R.id.detailed_item_textview);
			ptv.onceprint_item_textview = (TextView) convertView.findViewById(R.id.onceprint_item_textview);
			ptv.shrink_item_textview = (TextView) convertView.findViewById(R.id.shrink_item_textview);
			convertView.setTag(ptv);
		}
		final PrintTextview ptvon = (PrintTextview) convertView.getTag();
		if (!printList.get(position).isSpread()) {
			ptvon.onceprint_item_textview.setText("");
			ptvon.shrink_item_textview.setText("");
			ptvon.detailed_item_textview.setText("查看详细››");
		} else {
			ptvon.onceprint_item_textview.setText("打印");
			ptvon.shrink_item_textview.setText("隐藏");
			ptvon.detailed_item_textview.setText("");
		}

		if (printList.get(position).getCharacteristic() == Stamp.SUBSTANCE) {
			try {
				ptvon.print_item_textview.setText(printList.get(position).getPuts());
				// 查看详细
				ptvon.detailed_item_textview.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						printList.get(position).setSpread(true);
						printList.get(position).setPuts("正在打印数据:" + printList.get(position).getJson());
						CURRENT_LISTVIEW_ITEM_POSITION = print_list.getFirstVisiblePosition();// 获得当前listview显示的位置
						notifyDataSetChanged();
						print_list.smoothScrollToPosition(position);
					}
				});
				// 隐藏
				ptvon.shrink_item_textview.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try {
							printList.get(position).setSpread(false);
							printList.get(position).setPuts(
									"正在打印数据:{idx:'" + printList.get(position).getJson().getString("idx") + "'...");
							notifyDataSetChanged();
							print_list.setSelection(CURRENT_LISTVIEW_ITEM_POSITION);// 回到原来的位置
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				// 打印
				ptvon.onceprint_item_textview.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try {
							if (null != printList.get(position).getJson()) {
								if (!printList.get(position).getJson().isNull("mode_type")) {
									cmp.printJson = printList.get(position).getJson();
									cmp.commPrint();
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (printList.get(position).getCharacteristic() == Stamp.BEAR) {
			ptvon.print_item_textview.setText(printList.get(position).getPuts());
			ptvon.detailed_item_textview.setText("");
		}
		return convertView;
	}

	public void addPrintString(Stamp stamp) {
		if (this.printList.size() > 300) {
			this.printList.remove(0);
		}
		this.printList.add(stamp);
		this.notifyDataSetChanged();
		print_list.smoothScrollToPosition(getCount() - 1);// 移动到尾部
	}

	private static class PrintTextview {
		private TextView print_item_textview;// 内容
		private TextView detailed_item_textview;// 查看详细
		private TextView onceprint_item_textview;// 打印
		private TextView shrink_item_textview;// 隐藏
	}
}
