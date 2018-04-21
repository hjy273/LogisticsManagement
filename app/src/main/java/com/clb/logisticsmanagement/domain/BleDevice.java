package com.clb.logisticsmanagement.domain;

import java.io.Serializable;

/**
 * 数据保存的类
 */

public class BleDevice implements Serializable {
	private String name;
	private String adress;


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
	public String toString() {
		return "BleDevice{" +
				"name='" + name + '\'' +
				", adress='" + adress + '\'' +
				'}';
	}
}
