package com.clb.logisticsmanagement.domain;

import java.io.Serializable;

/**
 * 数据保存的类
 * 保存每个被搜索到的设备的信息
 */

public class BleDevice implements Serializable {
	private String name;
	private String adress;

	//保存名字和地址
	public BleDevice(String name, String adress) {
		this.name = name;
		this.adress = adress;

		setName(name);
		setAdress(adress);

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAdress() {
		return adress;
	}

	public void setAdress(String adress) {
		this.adress = adress;
	}

	@Override
	//TODO：这个的作用是干嘛的，重写toString方法有何作用
	public String toString() {
		return "BleDevice{" +
				"name='" + name + '\'' +
				", adress='" + adress + '\'' +
				'}';
	}
}
