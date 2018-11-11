package com.clb.logisticsmanagement.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.clb.logisticsmanagement.ble.conn.ConnResult;
import com.clb.logisticsmanagement.ble.conn.NotificationType;
import com.clb.logisticsmanagement.domain.BleDevice;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * 蓝牙连接
 */

public class BleHelper {

	private static BleHelper helper;

	private BleHelper() {
	}

	//r如果为空，则重新初始化
	public static BleHelper getHelper() {
		if (helper == null) {
			helper = new BleHelper();
		}
		return helper;
	}

	//UUID  TODO:三者的作用
	private UUID serviceUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
	private UUID characteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
	private UUID notifycUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	//使用BleListener接口
	private BleListener bleListener;

	//TODO
	private BluetoothGattCharacteristic characteristic = null;

	//TODO：这里conn方法为什么要定义传入Context对象作为参数，网上传入第一个参数是App.app
	public void conn(BleDevice bleDevice, Context context) {
			this.context = context;
		_conn_(bleDevice.getAdress());	//TODO:骚操作
	}

	public void disConn() {
		_dis_conn_();
	}

	//发送数据
	public boolean sendData() {
		if (bluetoothGatt != null) {
			if (characteristic != null) {
				//发送一个byte类型，只有一个元素为01的数组
				characteristic.setValue(new byte[]{01});
				//TODO
				bluetoothGatt.writeCharacteristic(characteristic);
				Log.e("sendData", "is success");
				return true;
			}
		}
		return false;
	}

	//注册蓝牙
	public void resigistBleListener(BleListener bleListener) {
		this.bleListener = bleListener;
	}

	//初始化
	private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private String mac;
	private Context context;
	private BluetoothGatt bluetoothGatt;
	private boolean connFlag = false;

	/**
	 * 两个设备通过BLE通信，首先需要建立GATT连接。这里我们讲的是Android设备作为client端，连接GATT Server。
	 * 连接GATT Server，你需要调用BluetoothDevice的connectGatt()方法。此函数带三个参数：Context、autoConnect(boolean)和BluetoothGattCallback对象。
	* */


	/**
	 * _conn_方法实现步骤:
	 * ①BluetoothDevice localBluetoothDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());	//mac即这里的device.getAddress()
	 * ②bleGatt = localBluetoothDevice.connectGatt(App.app, false,gattCallback);
	 * 这里直接一步到位，并且传入的参数是该Activity的context，和App.app一样
	 */
	private void _conn_(String mac) {
		this.mac = mac;
		// 通过mac地址获取蓝牙对象，或者可以直接用扫描到的对象
		bluetoothGatt = bluetoothAdapter.getRemoteDevice(mac)
				.connectGatt(context, false, gattCallback);
	}

	//断开连接
	private void _dis_conn_() {
		if (bluetoothGatt != null) {
			bluetoothGatt.disconnect();
		}
	}

	//TODO
	public void _enable_notify_(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, NotificationType type) {
		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(notifycUUID);		// 这包数据什么意思，可以不用管，反正是固定这包数据就是了
		if (descriptor == null) return;
		//判断通知的类型
		switch (type) {
			case DISABLE:
				gatt.setCharacteristicNotification(characteristic, false);
				descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
				break;
			case NOTIFICATION:		//通知
				gatt.setCharacteristicNotification(characteristic, true);
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				Log.e("debug_enable", "hadData");
				break;
			case INDICATION:		//禁用
				gatt.setCharacteristicNotification(characteristic, true);
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
				break;
		}
		//TODO
		gatt.writeDescriptor(descriptor);
	}

	//蓝牙连接回调，重写状态改变回调方法
	final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
		@Override
		//status表示原来的状态，newState表示后来的状态，0表示未连接上，2表示已连接设备
		public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
			super.onConnectionStateChange(gatt, status, newState);
			Log.e("debug_ble_state", "status:" + status + "---->newStae:" + newState);
//			Log.e("debug_gatt_call", this.toString());

			//成功
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothGatt.STATE_CONNECTED) {	//连接成功判断
					Log.e("debug_state", "newState:" + newState);
					connFlag = true;
					gatt.discoverServices();	//发现服务，连接到蓝牙后查找可以读写的服务，蓝牙有很多服务的
					onConnectResult(ConnResult.CONN_SUCCESS);
					Log.e("debug_connect", "接连成功");
				} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {		//连接断开判断
					if (connFlag) {
						onConnectResult(ConnResult.CONN_DIS);
						close(gatt);
					} else { //连接超时
						onConnectResult(ConnResult.CONN_FAILURE);		//超时导致失败
					}
				}
			} else {		//其他情况
				if (connFlag) {
					close(gatt);
					onConnectResult(ConnResult.CONN_DIS);		//断开
				} else {
					onConnectResult(ConnResult.CONN_FAILURE);		//失败
				}
			}
		}

		@Override
		//发现服务
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);
			//TODO
			characteristic = gatt.getService(serviceUUID).getCharacteristic(characteristicUUID);		// 先获取BluetoothGattService，再通过BluetoothGattService获取BluetoothGattCharacteristic特征值
			_enable_notify_(gatt, characteristic, NotificationType.NOTIFICATION);		//
			Log.e("debug_discovered", status + "");
			//TODO
			onLoadCharacteristic(gatt);		//
		}

		@Override
		//TODO
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicRead(gatt, characteristic, status);
			onRead(characteristic);
		}

		@Override
		//重写接收数据额的方法
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			//TODO：这里为什么要super引用onCharacteristicChanged（网上并没有这么做）
			super.onCharacteristicChanged(gatt, characteristic);
			byte[] data = characteristic.getValue();
			StringBuilder sb = new StringBuilder();		//因为所接收的数据，化为字符串需要一直改变，所以使用更省内存的StringBuilder
			for (byte b : data) {
				//TODO：当byte向int转型时，需要进行 & 0xff （byte转向int时的补位拓展，对于有符号数，从小扩展大时，需要用&0xff这样方式来确保是按补零扩展）https://www.cnblogs.com/think-in-java/p/5527389.html
				sb.append(String.format("%02x", (b & 0xff)));		//转化为16进制，然后在拼凑在一起成为一个字符串对象		bytesToHexString（）方法据说也是可以将byte数据转化为16进制的字符串
			}
			Log.e("sb",sb.toString());
			//TODO：这里是干嘛的
			onChanged(characteristic);

		}
	};

	//TODO：这个方法为什么要这样声明
	public void onConnectResult(ConnResult connResult) {
		if (bleListener != null) {
			bleListener.onConnectResult(connResult);
		}
	}

	//TODO：这个方法为什么要这样声明，作用是
	public void onLoadCharacteristic(BluetoothGatt gatt) {
		if (bleListener != null) {
			bleListener.onLoadCharacteristic(gatt);
		}
	}

	//ble向手机发送数据
	public void onChanged(BluetoothGattCharacteristic characteristic) {
		if (bleListener != null) {
			Log.e("onChange", "not null");
			bleListener.onChanged(characteristic);
		} else {
			Log.e("nChange", "is null");
		}
	}

	//读取数据的回调
	public void onRead(BluetoothGattCharacteristic characteristic) {

	}

	//关闭
	private void close(BluetoothGatt gatt) {
		if (gatt != null) {
			gatt.close();
			//刷新蓝牙缓存
			refreshCache(context, gatt);
		}
	}

	//刷新蓝牙缓存
	private static void refreshCache(Context context, BluetoothGatt gatt) {
		final BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		final List<BluetoothDevice> devices = manager.getConnectedDevices(BluetoothProfile.GATT);
		Log.e("debug_gat_conn_device", devices.size() + "");
		for (BluetoothDevice b : devices) {
			Log.e("debug_gat_conn_device", b.getAddress());
		}
		try {
			//TODO
			BluetoothGatt localBluetoothGatt = gatt;
			Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
			if (localMethod != null) {
				localMethod.invoke(gatt, new Object[0]);
				Log.e("debug_gatt_refresh", "刷新缓存");
			}
		} catch (Exception localException) {
			Log.e("log", "An exception occured while refreshing device");
		}
	}

	//设置监听回调
	//TODO：这个接口到底实现了什么
	public static interface BleListener {

		void onConnectResult(ConnResult connResult);

		void onLoadCharacteristic(BluetoothGatt gatt);

		void onChanged(BluetoothGattCharacteristic characteristic);
	}

}
