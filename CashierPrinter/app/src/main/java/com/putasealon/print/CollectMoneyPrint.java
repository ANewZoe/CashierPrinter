package com.putasealon.print;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.putasealon.activity.Assist;
import com.putasealon.activity.PrintListAdapter;
import com.putasealon.activity.Stamp;
import com.putasealon.thread.HttpThread;
import com.putasealon.thread.PrintString;
import com.zj.wfsdk.WifiCommunication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * 收银打印
 */
@SuppressLint("HandlerLeak")
public class CollectMoneyPrint {

	private WifiCommunication wificomm;
	private revMsgThread revThread;
	private PrintString cmps;
	public JSONObject printJson = null;
	private PrintListAdapter plAdapter;
	private boolean isopenMoney;// 是否开钱箱
	private static final int WFPRINTER_REVMSG = 0x06;

	private int commsum = 0;

	public CollectMoneyPrint(PrintListAdapter plAdapter) {
		super();
		this.plAdapter = plAdapter;
	}

	public void cmpInitPrint() {
		// TODO Auto-generated method stub
		wificomm = new WifiCommunication(handler);

		cmps = new PrintString(cmpHandler, "http://%s/eat.php?m=Home&c=printserver&a=get_print_info");
		cmps.start();
	}

	// 连接打印机
	public void commPrint() {
		if (commsum == 0) {
			commsum = 1;
			try {
				wificomm.initSocket(printJson.getString("ip_adress"), 9100);// 参数一IP地址，参数二端口
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				commsum = 0;
				e.printStackTrace();
			}
		}
	}

	// 打印桌面单（80mm）
	private void printZhuomian80mm() throws Exception {
		byte[] jtail = new byte[3];// 打印并换行n*0.125毫米
		jtail[0] = 0x1B;
		jtail[1] = 0x4A;
		jtail[2] = (byte) 40;
		byte[] tail = new byte[3];// 打印并换行
		tail[0] = 0x0A;
		tail[1] = 0x0D;
		byte[] mcmd = new byte[3];// 对齐方式
		mcmd[0] = 0x1B;
		mcmd[1] = 0x61;
		byte[] cmd = new byte[3];// 设置字体（倍高倍宽粗体）
		cmd[0] = 0x1B;
		cmd[1] = 0x21;
		byte[] icmd = new byte[4];// 初始化打印机
		icmd[0] = 0x1B;
		icmd[1] = 0x40;
		wificomm.sndByte(icmd);

		byte[] tcmd = new byte[4];// 检测是否有纸指令
		tcmd[0] = 0x10;
		tcmd[1] = 0x04;
		tcmd[2] = 0x04;
		wificomm.sndByte(tcmd);
		tcmd[0] = 0x1B;
		tcmd[1] = 0x42;
		tcmd[2] = 0x04;
		tcmd[3] = 0x01;
		wificomm.sndByte(tcmd); // 蜂鸣器鸣叫
		wificomm.sendMsg("#:" + printJson.getString("idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行

		mcmd[2] = 1;
		wificomm.sndByte(mcmd);// 居中
		cmd[2] |= 0x08;
		cmd[2] |= 0x10;
		cmd[2] |= 0x20;
		wificomm.sndByte(cmd);
		wificomm.sendMsg(printJson.getString("table_idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		mcmd[2] = 0;
		wificomm.sndByte(mcmd);
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg(
				printJson.getString("shop_name") + " / " + printJson.getString("consumer_num") + "人" + "\n就餐时间："
						+ printJson.getString("order_time")
						+ "\n------------------------------------------------菜名                                        份量\n------------------------------------------------\n",
				"gbk");
		cmd[2] |= 0x10;
		wificomm.sndByte(cmd);
		// 菜单信息
		if (!printJson.isNull("item_list")) {
			JSONArray jsonarray = printJson.getJSONArray("item_list");
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject json = jsonarray.getJSONObject(i);
				Thread.sleep(50); // 每发一次延时5毫秒
				wificomm.sendMsg(getDescribe80mm(json.getString("item_name"), "x" + json.getString("item_num")), "gbk");
				wificomm.sndByte(jtail);// 打印并进纸
			}
		}
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg("------------------------------------------------" + "\n" + printJson.getString("cashier")
				+ "\n" + printJson.getString("order_number") + "\n************************************************"
				+ "\nPowered by 八十一科技\n", "gbk");
		tcmd[0] = 0x1D;
		tcmd[1] = 0x56;
		tcmd[2] = 0x42;
		tcmd[3] = 90;
		wificomm.sndByte(tcmd); // 切刀指令
	}

	// 打印收银单(80mm)
	private void printShouyin80mm() throws Exception {
		byte[] jtail = new byte[3];// 打印并换行n*0.125毫米
		jtail[0] = 0x1B;
		jtail[1] = 0x4A;
		jtail[2] = (byte) 40;
		byte[] tail = new byte[3];// 打印并换行
		tail[0] = 0x0A;
		tail[1] = 0x0D;
		byte[] mcmd = new byte[3];// 对齐方式
		mcmd[0] = 0x1B;
		mcmd[1] = 0x61;
		byte[] cmd = new byte[3];// 设置字体（倍高倍宽粗体）
		cmd[0] = 0x1B;
		cmd[1] = 0x21;
		byte[] icmd = new byte[4];// 初始化打印机
		icmd[0] = 0x1B;
		icmd[1] = 0x40;
		wificomm.sndByte(icmd);

		byte[] tcmd = new byte[4];// 检测是否有纸指令
		tcmd[0] = 0x10;
		tcmd[1] = 0x04;
		tcmd[2] = 0x04;
		wificomm.sndByte(tcmd);
		tcmd[0] = 0x1B;
		tcmd[1] = 0x42;
		tcmd[2] = 0x04;
		tcmd[3] = 0x01;
		wificomm.sndByte(tcmd); // 蜂鸣器鸣叫
		wificomm.sendMsg("#:" + printJson.getString("idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		// 收银单/结账单
		mcmd[2] = 1;
		wificomm.sndByte(mcmd);// 居中
		cmd[2] |= 0x08;
		cmd[2] |= 0x10;
		cmd[2] |= 0x20;
		wificomm.sndByte(cmd);
		wificomm.sendMsg(printJson.getString("table_idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		mcmd[2] = 0;
		wificomm.sndByte(mcmd);
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg(
				printJson.getString("shop_name") + " / " + printJson.getString("consumer_num") + "人" + "\n就餐时间："
						+ printJson.getString("order_time")
						+ "\n------------------------------------------------菜名             份量     原价   会员价   实行价\n------------------------------------------------\n",
				"gbk");
		// 菜单信息
		if (!printJson.isNull("item_list")) {
			JSONArray jsonarray = printJson.getJSONArray("item_list");
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject json = jsonarray.getJSONObject(i);
				Thread.sleep(50); // 每发一次延时5毫秒
				wificomm.sendMsg(getName80mm(json.getString("item_name")) + getSum80mm(json.getString("item_num"))
						+ getPrice80mm(json.getString("item_price")) + " "
						+ getPrice80mm(json.getString("item_member_price")) + " "
						+ getPrice80mm(json.getString("item_action_price")), "gbk");
				wificomm.sndByte(jtail);// 打印并进纸
			}
		}

		wificomm.sendMsg("------------------------------------------------\n", "gbk");
		cmd[2] |= 0x10;
		wificomm.sndByte(cmd);
		// 描述信息
		if (!printJson.isNull("costlist")) {
			JSONObject jsonobject = printJson.getJSONObject("costlist");
			if (!jsonobject.isNull("list")) {
				JSONArray jar = jsonobject.getJSONArray("list");
				for (int i = 0; i < jar.length(); i++) {
					JSONObject jb = jar.getJSONObject(i);
					if (!"".equals(jb.getString("name")) && !"".equals(jb.getString("money"))) {
						Thread.sleep(50); // 每发一次延时5毫秒
						wificomm.sendMsg(getDescribe80mm(jb.getString("name"), jb.getString("money")), "gbk");
						wificomm.sndByte(jtail);// 打印并进纸
					}
				}
			}

		}

		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg("------------------------------------------------" + "\n" + printJson.getString("cashier")
				+ "\n" + printJson.getString("order_number") + "\n************************************************"
				+ "\nPowered by 八十一科技\n", "gbk");
		tcmd[0] = 0x1D;
		tcmd[1] = 0x56;
		tcmd[2] = 0x42;
		tcmd[3] = 90;
		wificomm.sndByte(tcmd); // 切刀指令
	}

	// 打印结账单(80mm)
	private void printJiezhang80mm() throws Exception {
		byte[] jtail = new byte[3];// 打印并换行n*0.125毫米
		jtail[0] = 0x1B;
		jtail[1] = 0x4A;
		jtail[2] = (byte) 40;
		byte[] tail = new byte[3];// 打印并换行
		tail[0] = 0x0A;
		tail[1] = 0x0D;
		byte[] mcmd = new byte[3];// 对齐方式
		mcmd[0] = 0x1B;
		mcmd[1] = 0x61;
		byte[] cmd = new byte[3];// 设置字体（倍高倍宽粗体）
		cmd[0] = 0x1B;
		cmd[1] = 0x21;
		byte[] icmd = new byte[4];// 初始化打印机
		icmd[0] = 0x1B;
		icmd[1] = 0x40;
		wificomm.sndByte(icmd);

		byte[] tcmd = new byte[4];// 检测是否有纸指令
		tcmd[0] = 0x10;
		tcmd[1] = 0x04;
		tcmd[2] = 0x04;
		wificomm.sndByte(tcmd);
		tcmd[0] = 0x1B;
		tcmd[1] = 0x42;
		tcmd[2] = 0x04;
		tcmd[3] = 0x01;
		wificomm.sndByte(tcmd); // 蜂鸣器鸣叫
		wificomm.sendMsg("#:" + printJson.getString("idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		// 收银单/结账单
		mcmd[2] = 1;
		wificomm.sndByte(mcmd);// 居中
		cmd[2] |= 0x08;
		cmd[2] |= 0x10;
		cmd[2] |= 0x20;
		wificomm.sndByte(cmd);
		wificomm.sendMsg(printJson.getString("table_idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		mcmd[2] = 0;
		wificomm.sndByte(mcmd);
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg(printJson.getString("shop_name") + " / " + printJson.getString("consumer_num") + "人"
				+ "\n就餐时间：" + printJson.getString("order_time"), "gbk");
		if (!"null".equals(printJson.getString("item_list")) && null != printJson.getString("item_list")
				&& !printJson.isNull("item_list")) {
			wificomm.sendMsg(
					"\n------------------------------------------------菜名             份量     原价   会员价   实行价\n------------------------------------------------\n",
					"gbk");
			// 菜单信息
			JSONArray jsonarray = printJson.getJSONArray("item_list");
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject json = jsonarray.getJSONObject(i);
				Thread.sleep(50); // 每发一次延时5毫秒
				wificomm.sendMsg(getName80mm(json.getString("item_name")) + getSum80mm(json.getString("item_num"))
						+ getPrice80mm(json.getString("item_price")) + " "
						+ getPrice80mm(json.getString("item_member_price")) + " "
						+ getPrice80mm(json.getString("item_action_price")), "gbk");
				wificomm.sndByte(jtail);// 打印并进纸
			}
		}
		wificomm.sendMsg("------------------------------------------------\n", "gbk");
		cmd[2] |= 0x10;
		wificomm.sndByte(cmd);
		// 描述信息
		if (!printJson.isNull("costlist")) {
			JSONObject jsonobject = printJson.getJSONObject("costlist");
			if (!jsonobject.isNull("list")) {
				JSONArray jar = jsonobject.getJSONArray("list");
				for (int i = 0; i < jar.length(); i++) {
					JSONObject jb = jar.getJSONObject(i);
					if (!"".equals(jb.getString("name")) && !"".equals(jb.getString("money"))) {
						Thread.sleep(50); // 每发一次延时5毫秒
						wificomm.sendMsg(getDescribe80mm(jb.getString("name"), jb.getString("money")), "gbk");
						wificomm.sndByte(jtail);// 打印并进纸
					}
				}
			}

		}
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg("------------------------------------------------" + "\n" + printJson.getString("cashier")
				+ "\n" + printJson.getString("order_number") + "\n************************************************"
				+ "\nPowered by 八十一科技\n", "gbk");
		tcmd[0] = 0x1D;
		tcmd[1] = 0x56;
		tcmd[2] = 0x42;
		tcmd[3] = 90;
		wificomm.sndByte(tcmd); // 切刀指令
	}

	// 打印收银交接单(80mm)
	private void printSy_report80mm() throws Exception {
		byte[] jtail = new byte[3];// 打印并换行n*0.125毫米
		jtail[0] = 0x1B;
		jtail[1] = 0x4A;
		jtail[2] = (byte) 40;
		byte[] tail = new byte[3];// 打印并换行
		tail[0] = 0x0A;
		tail[1] = 0x0D;
		byte[] mcmd = new byte[3];// 对齐方式
		mcmd[0] = 0x1B;
		mcmd[1] = 0x61;
		byte[] cmd = new byte[3];// 设置字体（倍高倍宽粗体）
		cmd[0] = 0x1B;
		cmd[1] = 0x21;
		byte[] icmd = new byte[4];// 初始化打印机
		icmd[0] = 0x1B;
		icmd[1] = 0x40;
		wificomm.sndByte(icmd);

		byte[] tcmd = new byte[4];// 检测是否有纸指令
		tcmd[0] = 0x10;
		tcmd[1] = 0x04;
		tcmd[2] = 0x04;
		wificomm.sndByte(tcmd);
		tcmd[0] = 0x1B;
		tcmd[1] = 0x42;
		tcmd[2] = 0x04;
		tcmd[3] = 0x01;
		wificomm.sndByte(tcmd); // 蜂鸣器鸣叫
		wificomm.sendMsg("#:" + printJson.getString("idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		mcmd[2] = 1;
		wificomm.sndByte(mcmd);// 居中
		wificomm.sendMsg(printJson.getString("shop_name") + "\n", "gbk");
		cmd[2] |= 0x08;
		cmd[2] |= 0x10;
		cmd[2] |= 0x20;
		wificomm.sndByte(cmd);
		wificomm.sendMsg(printJson.getString("title"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		mcmd[2] = 0;
		wificomm.sndByte(mcmd);// 左对齐
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg("打单时间：" + printJson.getString("order_time"), "gbk");
		wificomm.sendMsg("\n------------------------------------------------\n", "gbk");
		cmd[2] |= 0x10;
		wificomm.sndByte(cmd);
		// 描述信息
		if (!printJson.isNull("costlist")) {
			JSONObject jsonobject = printJson.getJSONObject("costlist");
			if (!jsonobject.isNull("list")) {
				JSONArray jar = jsonobject.getJSONArray("list");
				for (int i = 0; i < jar.length(); i++) {
					JSONObject jb = jar.getJSONObject(i);
					if (!"".equals(jb.getString("name")) && !"".equals(jb.getString("money"))) {
						Thread.sleep(50); // 每发一次延时5毫秒
						wificomm.sendMsg(getDescribe80mm(jb.getString("name"), jb.getString("money")), "gbk");
						wificomm.sndByte(jtail);// 打印并进纸
					}
				}
			}
		}

		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg("------------------------------------------------" + "\n" + printJson.getString("cashier")
				+ "\n************************************************" + "\nPowered by 八十一科技\n", "gbk");
		tcmd[0] = 0x1D;
		tcmd[1] = 0x56;
		tcmd[2] = 0x42;
		tcmd[3] = 90;
		wificomm.sndByte(tcmd); // 切刀指令

	}

	// 58mm
	// 打印桌面单（58mm）
	private void printZhuomian58mm() throws Exception {
		byte[] jtail = new byte[3];// 打印并换行n*0.125毫米
		jtail[0] = 0x1B;
		jtail[1] = 0x4A;
		jtail[2] = (byte) 40;
		byte[] tail = new byte[3];// 打印并换行
		tail[0] = 0x0A;
		tail[1] = 0x0D;
		byte[] mcmd = new byte[3];// 对齐方式
		mcmd[0] = 0x1B;
		mcmd[1] = 0x61;
		byte[] cmd = new byte[3];// 设置字体（倍高倍宽粗体）
		cmd[0] = 0x1B;
		cmd[1] = 0x21;
		byte[] cmd1 = new byte[3];// 设置汉字字符打印模式组合（倍高倍宽粗体）
		cmd1[0] = 0x1C;
		cmd1[1] = 0x21;
		byte[] icmd = new byte[4];// 初始化打印机
		icmd[0] = 0x1B;
		icmd[1] = 0x40;
		wificomm.sndByte(icmd);

		byte[] tcmd = new byte[4];// 检测是否有纸指令
		tcmd[0] = 0x10;
		tcmd[1] = 0x04;
		tcmd[2] = 0x04;
		wificomm.sndByte(tcmd);
		wificomm.sendMsg("\n#:" + printJson.getString("idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行

		mcmd[2] = 1;
		wificomm.sndByte(mcmd);// 居中
		cmd[2] |= 0x08;
		cmd[2] |= 0x10;
		cmd[2] |= 0x20;
		wificomm.sndByte(cmd);
		cmd1[2] |= 0x04;
		cmd1[2] |= 0x08;
		wificomm.sndByte(cmd1);// 设置汉字倍高倍宽粗体模式
		wificomm.sendMsg(printJson.getString("table_idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		mcmd[2] = 0;
		wificomm.sndByte(mcmd);
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x00;
		wificomm.sndByte(cmd1);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg(
				printJson.getString("shop_name") + " / " + printJson.getString("consumer_num") + "人" + "\n就餐时间："
						+ printJson.getString("order_time")
						+ "\n--------------------------------菜名                        份量\n--------------------------------\n",
				"gbk");
		cmd[2] |= 0x10;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x08;
		wificomm.sndByte(cmd1);
		// 菜单信息
		if (!printJson.isNull("item_list")) {
			JSONArray jsonarray = printJson.getJSONArray("item_list");
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject json = jsonarray.getJSONObject(i);
				Thread.sleep(50); // 每发一次延时5毫秒
				wificomm.sendMsg(getDescribe58mm(json.getString("item_name"), "x" + json.getString("item_num")), "gbk");
				wificomm.sndByte(jtail);// 打印并进纸
			}
		}
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x00;
		wificomm.sndByte(cmd1);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg("--------------------------------" + "\n" + printJson.getString("cashier") + "\n"
				+ printJson.getString("order_number") + "\n********************************"
				+ "\nPowered by 八十一科技\n\n\n\n", "gbk");
	}

	// 打印收银单(58mm)
	private void printShouyin58mm() throws Exception {
		byte[] jtail = new byte[3];// 打印并换行n*0.125毫米
		jtail[0] = 0x1B;
		jtail[1] = 0x4A;
		jtail[2] = (byte) 40;
		byte[] tail = new byte[3];// 打印并换行
		tail[0] = 0x0A;
		tail[1] = 0x0D;
		byte[] mcmd = new byte[3];// 对齐方式
		mcmd[0] = 0x1B;
		mcmd[1] = 0x61;
		byte[] cmd = new byte[3];// 设置字体（倍高倍宽粗体）
		cmd[0] = 0x1B;
		cmd[1] = 0x21;
		byte[] cmd1 = new byte[3];// 设置汉字字符打印模式组合（倍高倍宽粗体）
		cmd1[0] = 0x1C;
		cmd1[1] = 0x21;
		byte[] icmd = new byte[4];// 初始化打印机
		icmd[0] = 0x1B;
		icmd[1] = 0x40;
		wificomm.sndByte(icmd);

		byte[] tcmd = new byte[4];// 检测是否有纸指令
		tcmd[0] = 0x10;
		tcmd[1] = 0x04;
		tcmd[2] = 0x04;
		wificomm.sndByte(tcmd);
		wificomm.sendMsg("\n#:" + printJson.getString("idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		// 收银单/结账单
		mcmd[2] = 1;
		wificomm.sndByte(mcmd);// 居中
		cmd[2] |= 0x08;
		cmd[2] |= 0x10;
		cmd[2] |= 0x20;
		wificomm.sndByte(cmd);
		cmd1[2] |= 0x04;
		cmd1[2] |= 0x08;
		wificomm.sndByte(cmd1);// 设置汉字倍高倍宽粗体模式
		wificomm.sendMsg(printJson.getString("table_idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		mcmd[2] = 0;
		wificomm.sndByte(mcmd);
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x00;
		wificomm.sndByte(cmd1);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg(
				printJson.getString("shop_name") + " / " + printJson.getString("consumer_num") + "人" + "\n就餐时间："
						+ printJson.getString("order_time")
						+ "\n--------------------------------\n菜名       份量     原价  实行价\n--------------------------------",
				"gbk");
		// 菜单信息
		if (!printJson.isNull("item_list")) {
			JSONArray jsonarray = printJson.getJSONArray("item_list");
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject json = jsonarray.getJSONObject(i);
				Thread.sleep(50); // 每发一次延时5毫秒
				wificomm.sendMsg(getName58mm(json.getString("item_name")) + getSum58mm(json.getString("item_num"))
						+ getPrice58mm(json.getString("item_price"))
						+ getPrice58mm(json.getString("item_action_price")), "gbk");
				wificomm.sndByte(jtail);// 打印并进纸
			}
		}
		wificomm.sendMsg("--------------------------------\n", "gbk");
		cmd[2] |= 0x10;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x08;
		wificomm.sndByte(cmd1);
		// 描述信息
		if (!printJson.isNull("costlist")) {
			JSONObject jsonobject = printJson.getJSONObject("costlist");
			if (!jsonobject.isNull("list")) {
				JSONArray jar = jsonobject.getJSONArray("list");
				for (int i = 0; i < jar.length(); i++) {
					JSONObject jb = jar.getJSONObject(i);
					if (!"".equals(jb.getString("name")) && !"".equals(jb.getString("money"))) {
						Thread.sleep(50); // 每发一次延时5毫秒
						wificomm.sendMsg(getDescribe58mm(jb.getString("name"), jb.getString("money")), "gbk");
						wificomm.sndByte(jtail);// 打印并进纸
					}
				}
			}

		}

		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x00;
		wificomm.sndByte(cmd1);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg("--------------------------------" + "\n" + printJson.getString("cashier") + "\n"
				+ printJson.getString("order_number") + "\n********************************"
				+ "\nPowered by 八十一科技\n\n\n\n", "gbk");
	}

	// 打印结账单(58mm)
	private void printJiezhang58mm() throws Exception {
		byte[] jtail = new byte[3];// 打印并换行n*0.125毫米
		jtail[0] = 0x1B;
		jtail[1] = 0x4A;
		jtail[2] = (byte) 40;
		byte[] tail = new byte[3];// 打印并换行
		tail[0] = 0x0A;
		tail[1] = 0x0D;
		byte[] mcmd = new byte[3];// 对齐方式
		mcmd[0] = 0x1B;
		mcmd[1] = 0x61;
		byte[] cmd = new byte[3];// 设置字体（倍高倍宽粗体）
		cmd[0] = 0x1B;
		cmd[1] = 0x21;
		byte[] cmd1 = new byte[3];// 设置汉字字符打印模式组合（倍高倍宽粗体）
		cmd1[0] = 0x1C;
		cmd1[1] = 0x21;
		byte[] icmd = new byte[4];// 初始化打印机
		icmd[0] = 0x1B;
		icmd[1] = 0x40;
		wificomm.sndByte(icmd);

		byte[] tcmd = new byte[4];// 检测是否有纸指令
		tcmd[0] = 0x10;
		tcmd[1] = 0x04;
		tcmd[2] = 0x04;
		wificomm.sndByte(tcmd);
		wificomm.sendMsg("\n#:" + printJson.getString("idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		// 收银单/结账单
		mcmd[2] = 1;
		wificomm.sndByte(mcmd);// 居中
		cmd[2] |= 0x08;
		cmd[2] |= 0x10;
		cmd[2] |= 0x20;
		wificomm.sndByte(cmd);
		cmd1[2] |= 0x04;
		cmd1[2] |= 0x08;
		wificomm.sndByte(cmd1);
		wificomm.sendMsg(printJson.getString("table_idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		mcmd[2] = 0;
		wificomm.sndByte(mcmd);
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x00;
		wificomm.sndByte(cmd1);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg(printJson.getString("shop_name") + " / " + printJson.getString("consumer_num") + "人"
				+ "\n就餐时间：" + printJson.getString("order_time"), "gbk");
		if (!"null".equals(printJson.getString("item_list")) && null != printJson.getString("item_list")
				&& !printJson.isNull("item_list")) {
			wificomm.sendMsg(
					"\n--------------------------------\n菜名       份量     原价  实行价\n--------------------------------",
					"gbk");
			// 菜单信息
			JSONArray jsonarray = printJson.getJSONArray("item_list");
			for (int i = 0; i < jsonarray.length(); i++) {
				JSONObject json = jsonarray.getJSONObject(i);
				Thread.sleep(50); // 每发一次延时5毫秒
				wificomm.sendMsg(getName58mm(json.getString("item_name")) + getSum58mm(json.getString("item_num"))
						+ getPrice58mm(json.getString("item_price"))
						+ getPrice58mm(json.getString("item_action_price")), "gbk");
				wificomm.sndByte(jtail);// 打印并进纸
			}
		}
		wificomm.sendMsg("--------------------------------\n", "gbk");
		cmd[2] |= 0x10;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x08;
		wificomm.sndByte(cmd1);
		// 描述信息
		if (!printJson.isNull("costlist")) {
			JSONObject jsonobject = printJson.getJSONObject("costlist");
			if (!jsonobject.isNull("list")) {
				JSONArray jar = jsonobject.getJSONArray("list");
				for (int i = 0; i < jar.length(); i++) {
					JSONObject jb = jar.getJSONObject(i);
					if (!"".equals(jb.getString("name")) && !"".equals(jb.getString("money"))) {
						Thread.sleep(50); // 每发一次延时5毫秒
						wificomm.sendMsg(getDescribe58mm(jb.getString("name"), jb.getString("money")), "gbk");
						wificomm.sndByte(jtail);// 打印并进纸
					}
				}
			}
		}

		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x00;
		wificomm.sndByte(cmd1);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg("--------------------------------" + "\n" + printJson.getString("cashier") + "\n"
				+ printJson.getString("order_number") + "\n********************************"
				+ "\nPowered by 八十一科技\n\n\n\n", "gbk");
	}

	// 打印收银交接单(58mm)
	private void printSy_report58mm() throws Exception {
		byte[] jtail = new byte[3];// 打印并换行n*0.125毫米
		jtail[0] = 0x1B;
		jtail[1] = 0x4A;
		jtail[2] = (byte) 40;
		byte[] tail = new byte[3];// 打印并换行
		tail[0] = 0x0A;
		tail[1] = 0x0D;
		byte[] mcmd = new byte[3];// 对齐方式
		mcmd[0] = 0x1B;
		mcmd[1] = 0x61;
		byte[] cmd = new byte[3];// 设置字体（倍高倍宽粗体）
		cmd[0] = 0x1B;
		cmd[1] = 0x21;
		byte[] cmd1 = new byte[3];// 设置汉字字符打印模式组合（倍高倍宽粗体）
		cmd1[0] = 0x1C;
		cmd1[1] = 0x21;
		byte[] icmd = new byte[4];// 初始化打印机
		icmd[0] = 0x1B;
		icmd[1] = 0x40;
		wificomm.sndByte(icmd);

		byte[] tcmd = new byte[4];// 检测是否有纸指令
		tcmd[0] = 0x10;
		tcmd[1] = 0x04;
		tcmd[2] = 0x04;
		wificomm.sndByte(tcmd);
		wificomm.sendMsg("\n#:" + printJson.getString("idx"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		mcmd[2] = 1;
		wificomm.sndByte(mcmd);// 居中
		wificomm.sendMsg(printJson.getString("shop_name") + "\n", "gbk");
		cmd[2] |= 0x08;
		cmd[2] |= 0x10;
		cmd[2] |= 0x20;
		wificomm.sndByte(cmd);
		cmd1[2] |= 0x04;
		cmd1[2] |= 0x08;
		wificomm.sndByte(cmd1);
		wificomm.sendMsg(printJson.getString("title"), "gbk");
		wificomm.sndByte(tail);// 打印并换行
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x00;
		wificomm.sndByte(cmd1);
		mcmd[2] = 0;
		wificomm.sndByte(mcmd);// 左对齐
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg("打单时间：" + printJson.getString("order_time"), "gbk");
		wificomm.sendMsg("\n--------------------------------\n", "gbk");
		cmd[2] |= 0x10;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x08;
		wificomm.sndByte(cmd1);
		// 描述信息
		if (!printJson.isNull("costlist")) {
			JSONObject jsonobject = printJson.getJSONObject("costlist");
			if (!jsonobject.isNull("list")) {
				JSONArray jar = jsonobject.getJSONArray("list");
				for (int i = 0; i < jar.length(); i++) {
					JSONObject jb = jar.getJSONObject(i);
					if (!"".equals(jb.getString("name")) && !"".equals(jb.getString("money"))) {
						Thread.sleep(50); // 每发一次延时5毫秒
						wificomm.sendMsg(getDescribe58mm(jb.getString("name"), jb.getString("money")), "gbk");
						wificomm.sndByte(jtail);// 打印并进纸
					}
				}
			}
		}
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x00;
		wificomm.sndByte(cmd1);
		Thread.sleep(50); // 每发一次延时5毫秒
		wificomm.sendMsg("--------------------------------" + "\n" + printJson.getString("cashier")
				+ "\n********************************" + "\nPowered by 八十一科技\n\n\n\n", "gbk");
	}

	// 无数据
	private void NullData58mm() {

		byte[] jtail = new byte[3];// 打印并换行n*0.125毫米
		jtail[0] = 0x1B;
		jtail[1] = 0x4A;
		jtail[2] = (byte) 40;
		byte[] tail = new byte[3];// 打印并换行
		tail[0] = 0x0A;
		tail[1] = 0x0D;
		byte[] mcmd = new byte[3];// 对齐方式
		mcmd[0] = 0x1B;
		mcmd[1] = 0x61;
		byte[] cmd = new byte[3];// 设置字体（倍高倍宽粗体）
		cmd[0] = 0x1B;
		cmd[1] = 0x21;
		byte[] cmd1 = new byte[3];// 设置汉字字符打印模式组合（倍高倍宽粗体）
		cmd1[0] = 0x1C;
		cmd1[1] = 0x21;
		byte[] icmd = new byte[4];// 初始化打印机
		icmd[0] = 0x1B;
		icmd[1] = 0x40;
		wificomm.sndByte(icmd);

		byte[] tcmd = new byte[4];// 检测是否有纸指令
		tcmd[0] = 0x10;
		tcmd[1] = 0x04;
		tcmd[2] = 0x04;
		wificomm.sndByte(tcmd);
		mcmd[2] = 1;
		wificomm.sndByte(mcmd);// 居中
		cmd[2] |= 0x08;
		cmd[2] |= 0x10;
		cmd[2] |= 0x20;
		wificomm.sndByte(cmd);
		cmd1[2] |= 0x04;
		cmd1[2] |= 0x08;
		wificomm.sndByte(cmd1);
		wificomm.sendMsg("无数据\n\n\n\n", "gbk");
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		cmd1[2] = 0x00;
		wificomm.sndByte(cmd1);
		mcmd[2] = 0;
		wificomm.sndByte(mcmd);// 左对齐
	}

	// 无数据
	private void NullData80mm() {

		byte[] jtail = new byte[3];// 打印并换行n*0.125毫米
		jtail[0] = 0x1B;
		jtail[1] = 0x4A;
		jtail[2] = (byte) 40;
		byte[] tail = new byte[3];// 打印并换行
		tail[0] = 0x0A;
		tail[1] = 0x0D;
		byte[] mcmd = new byte[3];// 对齐方式
		mcmd[0] = 0x1B;
		mcmd[1] = 0x61;
		byte[] cmd = new byte[3];// 设置字体（倍高倍宽粗体）
		cmd[0] = 0x1B;
		cmd[1] = 0x21;
		byte[] icmd = new byte[4];// 初始化打印机
		icmd[0] = 0x1B;
		icmd[1] = 0x40;
		wificomm.sndByte(icmd);

		byte[] tcmd = new byte[4];// 检测是否有纸指令
		tcmd[0] = 0x10;
		tcmd[1] = 0x04;
		tcmd[2] = 0x04;
		wificomm.sndByte(tcmd);
		mcmd[2] = 1;
		wificomm.sndByte(mcmd);// 居中
		cmd[2] |= 0x08;
		cmd[2] |= 0x10;
		cmd[2] |= 0x20;
		wificomm.sndByte(cmd);
		wificomm.sendMsg("无数据\n\n\n\n", "gbk");
		cmd[2] = 0x00;
		wificomm.sndByte(cmd);
		mcmd[2] = 0;
		wificomm.sndByte(mcmd);// 左对齐
	}

	// 开钱箱
	private void openMoney() {
		byte[] omcmd = new byte[5];
		omcmd[0] = 0x1B;
		omcmd[1] = 0x70;
		omcmd[2] = 0x00;
		omcmd[3] = 0x40;
		omcmd[4] = 0x50;
		wificomm.sndByte(omcmd);
	}

	// 打印
	public void print() {
		try {
			isopenMoney = false;
			if (!"1".equals(printJson.getString("money_box"))) {
				Stamp stamp = new Stamp();
				stamp.setSpread(false);
				stamp.setJson(printJson);
				stamp.setPuts("正在打印数据:{idx:'" + printJson.getString("idx") + "'...");
				stamp.setCharacteristic(Stamp.SUBSTANCE);
				// 打印58mm纸宽格式
				if ("0".equals(printJson.getString("printer_type"))) {
					plAdapter.addPrintString(stamp);
					if ("sy_report".equals(printJson.getString("mode_type"))) {// 收银交班单
						printSy_report58mm();
					} else if ("zhuomian".equals(printJson.getString("mode_type"))
							|| "chuancai".equals(printJson.getString("mode_type"))
							|| "yuding".equals(printJson.getString("mode_type"))) {// 桌面单/传菜单/呼叫服务
						printZhuomian58mm();
					} else if ("jiezhang".equals(printJson.getString("mode_type"))) {// 结账单
						printJiezhang58mm();
					} else if ("shouyin".equals(printJson.getString("mode_type"))) {// 收银单
						printShouyin58mm();
					} else {
						// 无数据
						NullData58mm();
					}
				} else if ("1".equals(printJson.getString("printer_type"))) {// 打印80mm纸宽格式
					plAdapter.addPrintString(stamp);
					if ("sy_report".equals(printJson.getString("mode_type"))) {// 收银交班单
						printSy_report80mm();
					} else if ("zhuomian".equals(printJson.getString("mode_type"))
							|| "chuancai".equals(printJson.getString("mode_type"))
							|| "yuding".equals(printJson.getString("mode_type"))) {// 桌面单/传菜单/呼叫服务
						printZhuomian80mm();
					} else if ("jiezhang".equals(printJson.getString("mode_type"))) {// 结账单
						printJiezhang80mm();
					} else if ("shouyin".equals(printJson.getString("mode_type"))) {// 收银单
						printShouyin80mm();
					} else {
						// 无数据
						NullData80mm();
					}
				}

			} else {
				Stamp stamp = new Stamp();
				stamp.setSpread(false);
				stamp.setPuts("正在打开钱箱。。。。。。");
				stamp.setCharacteristic(Stamp.BEAR);
				isopenMoney = true;
				plAdapter.addPrintString(stamp);
				// 开钱箱
				openMoney();
			}
			Stamp stamp = new Stamp();
			stamp.setSpread(false);
			stamp.setPuts("打印数据成功！");
			stamp.setCharacteristic(Stamp.BEAR);
			plAdapter.addPrintString(stamp);
			// 发送打印结果
			if (Assist.ipcount == 1) {
				new HttpThread(
						"http://" + Assist.serviceIp + "/eat_bendi/eat.php?m=Home&c=printserver&a=get_print_re&idx="
								+ printJson.getString("idx") + "&sta=1",
						cmps).start();
			} else if (Assist.ipcount == 0) {
				new HttpThread("http://9981it.com/eat_online/eat.php?m=Home&c=printserver&a=get_print_re&idx="
						+ printJson.getString("idx") + "&sta=1&sid=" + Assist.sid, cmps).start();
			}

		} catch (Exception e) {
			cmps.visit = 0;
			e.printStackTrace();
		}
		wificomm.close();
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				// 连接成功时
				case WifiCommunication.WFPRINTER_CONNECTED:
					Stamp stamp = new Stamp();
					stamp.setSpread(false);
					stamp.setPuts("连接打印机成功！");
					stamp.setCharacteristic(Stamp.BEAR);
					plAdapter.addPrintString(stamp);
					revThread = new revMsgThread();
					revThread.start();
					try {
						Thread.sleep(50);
						print();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				// 断开连接时
				case WifiCommunication.WFPRINTER_DISCONNECTED:
					Stamp stamp1 = new Stamp();
					stamp1.setSpread(false);
					stamp1.setPuts("打印机断开！");
					stamp1.setCharacteristic(Stamp.BEAR);
					plAdapter.addPrintString(stamp1);
					revThread.interrupt();
					commsum = 0;
					break;
				// 发送数据失败
				case WifiCommunication.SEND_FAILED:
					// 发送打印结果
					try {
						if (isopenMoney) {
							Stamp stamp2 = new Stamp();
							stamp2.setSpread(false);
							stamp2.setJson(printJson);
							stamp2.setPuts("打开钱箱失败！");
							stamp2.setCharacteristic(Stamp.BEAR);
							plAdapter.addPrintString(stamp2);
						} else {
							Stamp stamp2 = new Stamp();
							stamp2.setSpread(false);
							stamp2.setPuts("打印数据失败！");
							stamp2.setCharacteristic(Stamp.BEAR);
							plAdapter.addPrintString(stamp2);
						}
						if (Assist.ipcount == 1) {
							new HttpThread("http://" + Assist.serviceIp
									+ "/eat_bendi/eat.php?m=Home&c=printserver&a=get_print_re&idx="
									+ printJson.getString("idx") + "&sta=0", cmps).start();
						} else if (Assist.ipcount == 0) {
							new HttpThread("http://9981it.com/eat_bendi/eat.php?m=Home&c=printserver&a=get_print_re&idx="
									+ printJson.getString("idx") + "&sta=0&sid=" + Assist.sid, cmps).start();
						}

						revThread.interrupt();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						cmps.visit = 0;
						e.printStackTrace();
					}
					wificomm.close();
					break;
				// 连接WIFI打印机错误
				case WifiCommunication.WFPRINTER_CONNECTEDERR:
					Stamp stamp2 = new Stamp();
					stamp2.setSpread(false);
					stamp2.setPuts("连接打印机错误！");
					stamp2.setCharacteristic(Stamp.BEAR);
					plAdapter.addPrintString(stamp2);
					if (null != cmps) {
						cmps.visit = 0;
					}
					commsum = 0;
					break;
				// 打印机没有纸
				case WifiCommunication.WFPRINTER_REVMSG:
					Stamp stamp3 = new Stamp();
					stamp3.setSpread(false);
					stamp3.setPuts("打印机没有纸！");
					stamp3.setCharacteristic(Stamp.BEAR);
					plAdapter.addPrintString(stamp3);
					if (null != cmps) {
						cmps.visit = 0;
					}
					break;
				default:
					break;
			}
		};
	};

	// 打印机线程，连接上打印机时创建，关闭打印机时退出
	class revMsgThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				Message mes = new Message();
				int revData;
				while (true) {
					revData = wificomm.revByte();// 非阻塞单个字节接收数据，如需改成非阻塞接收字符串请参考手册
					if (revData != -1) {
						mes = handler.obtainMessage(WFPRINTER_REVMSG);
						mes.obj = revData;
						handler.sendMessage(mes);
					}
					Thread.sleep(20);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 获得打印数据时打印
	Handler cmpHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case PrintString.SUCCESS:
					try {
						printJson = new JSONObject(cmps.getPrintString());
						commPrint();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						if (null != cmps) {
							cmps.visit = 0;
						}
						e.printStackTrace();
					}
					break;
				default:
					break;
			}
		};
	};

	// 80mm
	// 计算打印菜名自动换行限制菜名的长度为8个汉字的长度，一个汉字的长度为两个空格的长度(80mm)
	private String getName80mm(String name) {
		int c = 0;// 字符串长度
		String name1 = "";
		for (int i = 0; i < name.length(); i++) {
			// 如果长度为8个汉字的长度就换行
			if (isChinese(name.charAt(i))) {
				// 是中文长度加2
				c += 2;
			} else {
				// 不是中长度加1
				c += 1;
			}
			if (c > 16) {
				name1 += "\n";
				c = 0;
				if (isChinese(name.charAt(i))) {
					// 是中文长度加2
					c += 2;
				} else {
					// 不是中长度加1
					c += 1;
				}
			}
			name1 += name.charAt(i);
		}
		// 长度不够的用空格补齐，多出一个空格为菜名与数量之间的间隔
		for (int i = 0; i < 17 - c; i++) {
			name1 += " ";
		}
		return name1;
	}

	// 计算截取打印分量，限制长度为5个空格(80mm)
	private String getSum80mm(String s) {
		s = "x" + s;
		int sum = 0;
		for (int i = 0; i < s.length(); i++) {
			if (isChinese(s.charAt(i))) {
				sum += 2;
			} else {
				sum += 1;
			}
			if (sum > 5) {
				s = s.substring(0, i);
				sum = 5;
				break;
			}
		}
		for (int k = 0; k < 5 - sum; k++) {
			s += " ";
		}
		return s;
	}

	// 价格(80mm)
	private String getPrice80mm(String s) {
		int sum = 0;
		for (int i = 0; i < s.length(); i++) {
			if (isChinese(s.charAt(i))) {
				sum += 2;
			} else {
				sum += 1;
			}
			if (sum > 8) {
				s = s.substring(0, i);
				sum = 8;
				break;
			}
		}
		for (int j = 0; j < 8 - sum; j++) {
			s = " " + s;
		}

		return s;
	}

	// 计算描述信息换行(80mm)
	private String getDescribe80mm(String name, String money) {
		// name
		String name1 = "";
		int cn1 = 0;// 字符串长度
		for (int i = 0; i < name.length(); i++) {
			if (isChinese(name.charAt(i))) {
				// 是中文
				cn1 += 2;
			} else {
				// 不是中
				cn1 += 1;
			}
			if (cn1 > 32) {
				name1 += "\n";
				cn1 = 0;
				if (isChinese(name.charAt(i))) {
					// 是中文
					cn1 += 2;

				} else {
					// 不是中
					cn1 += 1;
				}
			}
			name1 += name.charAt(i);
		}
		for (int i = 0; i < 33 - cn1; i++) {
			name1 += " ";
		}
		// money
		int cm1 = 0;
		for (int i = 0; i < money.length(); i++) {
			if (isChinese(money.charAt(i))) {
				cm1 += 2;
			} else {
				cm1 += 1;
			}
			if (cm1 > 15) {
				money = money.substring(0, i);
				cm1 = 15;
				break;
			}
		}
		for (int i = 0; i < 15 - cm1; i++) {
			money = " " + money;
		}
		return name1 + money;
	}

	// 58mm
	// 计算打印菜名自动换行限制菜名的长度为8个汉字的长度，一个汉字的长度为两个空格的长度
	private String getName58mm(String name) {
		int c = 0;// 字符串长度
		String name1 = "";
		for (int i = 0; i < name.length(); i++) {
			// 如果长度为8个汉字的长度就换行
			if (isChinese(name.charAt(i))) {
				// 是中文长度加2
				c += 2;
			} else {
				// 不是中长度加1
				c += 1;
			}
			if (c > 10) {
				name1 += "\n";
				c = 0;
				if (isChinese(name.charAt(i))) {
					// 是中文长度加2
					c += 2;
				} else {
					// 不是中长度加1
					c += 1;
				}
			}
			name1 += name.charAt(i);
		}
		// 长度不够的用空格补齐，多出一个空格为菜名与数量之间的间隔
		for (int i = 0; i < 11 - c; i++) {
			name1 += " ";
		}
		return name1;
	}

	// 计算截取打印分量
	private String getSum58mm(String s) {
		s = "x" + s;
		int sum = 0;
		for (int i = 0; i < s.length(); i++) {
			if (isChinese(s.charAt(i))) {
				sum += 2;
			} else {
				sum += 1;
			}
			if (sum > 5) {
				s = s.substring(0, i);
				sum = 5;
				break;
			}
		}
		for (int k = 0; k < 5 - sum; k++) {
			s += " ";
		}
		return s;
	}

	// 价格
	private String getPrice58mm(String price) {
		int sum = 0;
		for (int i = 0; i < price.length(); i++) {
			if (isChinese(price.charAt(i))) {
				sum += 2;
			} else {
				sum += 1;
			}
			if (sum > 8) {
				price = price.substring(0, i);
				sum = 8;
				break;
			}
		}
		for (int j = 0; j < 8 - sum; j++) {
			price = " " + price;
		}

		return price;
	}

	// 计算描述信息换行
	private String getDescribe58mm(String name, String money) {
		// name
		String name1 = "";
		int cn1 = 0;// 字符串长度
		for (int i = 0; i < name.length(); i++) {
			if (isChinese(name.charAt(i))) {
				// 是中文
				cn1 += 2;
			} else {
				// 不是中
				cn1 += 1;
			}
			if (cn1 > 20) {
				name1 += "\n";
				cn1 = 0;
				if (isChinese(name.charAt(i))) {
					// 是中文
					cn1 += 2;

				} else {
					// 不是中
					cn1 += 1;
				}
			}
			name1 += name.charAt(i);
		}
		for (int i = 0; i < 20 - cn1; i++) {
			name1 += " ";
		}
		// money
		int cm1 = 0;
		for (int i = 0; i < money.length(); i++) {
			if (isChinese(money.charAt(i))) {
				cm1 += 2;
			} else {
				cm1 += 1;
			}
			if (cm1 > 12) {
				money = money.substring(0, i);
				cm1 = 12;
				break;
			}
		}
		for (int i = 0; i < 12 - cm1; i++) {
			money = " " + money;
		}
		return name1 + money;
	}

	// 判断是否为中文
	public boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

}
