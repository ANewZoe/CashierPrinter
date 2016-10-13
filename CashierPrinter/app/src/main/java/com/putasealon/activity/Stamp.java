package com.putasealon.activity;

import org.json.JSONObject;

/*
 * 打印记录封装类
 */
public class Stamp {
	public static final int BEAR = 0;// 实行结果
	public static final int SUBSTANCE = 1;// 打印的内容
	private String puts;//打印的字符串
	private int characteristic;//标识是打印内容还是实行结果
	private boolean isSpread;//  是否为展开状态
	private JSONObject json;// 打印小票的json数据

	public boolean isSpread() {
		return isSpread;
	}

	public void setSpread(boolean isSpread) {
		this.isSpread = isSpread;
	}

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}

	public String getPuts() {
		return puts;
	}

	public void setPuts(String puts) {
		this.puts = puts;
	}

	public int getCharacteristic() {
		return characteristic;
	}

	public void setCharacteristic(int characteristic) {
		this.characteristic = characteristic;
	}
}
