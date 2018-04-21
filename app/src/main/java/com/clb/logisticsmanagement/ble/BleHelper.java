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

	public static BleHelper getHelper() {
		if (helper == null) {
			helper = new BleHelper();
		}
		return helper;
	}

	private UUID serviceUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
	private UUID characteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
	private UUID notifycUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	private BleListener bleListener;

	private BluetoothGattCharacteristic characteristic = null;

	public void conn(BleDevice bleDevice, Context context) {
		this.context = context;
		_conn_(bleDevice.getAdress());
	}

	public void disConn() {
		_dis_conn_();
	}

	//发送数据
	public boolean sendData() {
		if (bluetoothGatt != null) {
			if (characteristic != null) {
				characteristic.setValue(new byte[]{01});
				bluetoothGatt.writeCharacteristic(characteristic);
				Log.e("sendData", "is success");
				return true;
			}
		}
		return false;
	}

	public void resigistBleListener(BleListener bleListener) {
		this.bleListener = bleListener;
	}

	private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private String mac;
	private Context context;
	private BluetoothGatt bluetoothGatt;
	private boolean connFlag = false;

	private void _conn_(String mac) {
		this.mac = mac;
		bluetoothGatt = bluetoothAdapter.getRemoteDevice(mac)
				.connectGatt(context, false, gattCallback);
	}

	private void _dis_conn_() {
		if (bluetoothGatt != null) {
			bluetoothGatt.disconnect();
		}
	}


	public void _enable_notify_(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, NotificationType type) {
		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(notifycUUID);
		if (descriptor == null) return;
		switch (type) {
			case DISABLE:
				gatt.setCharacteristicNotification(characteristic, false);
				descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
				break;
			case NOTIFICATION:
				gatt.setCharacteristicNotification(characteristic, true);
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				Log.e("debug_enable", "hadData");
				break;
			case INDICATION:
				gatt.setCharacteristicNotification(characteristic, true);
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
				break;
		}
		gatt.writeDescriptor(descriptor);
	}

	//蓝牙连接回调
	final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
			super.onConnectionStateChange(gatt, status, newState);
			Log.e("debug_ble_state", "status:" + status + "---->newStae:" + newState);
//			Log.e("debug_gatt_call", this.toString());

			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothGatt.STATE_CONNECTED) {
					Log.e("debug_state", "newState:" + newState);
					connFlag = true;
					gatt.discoverServices();
					onConnectResult(ConnResult.CONN_SUCCESS);
					Log.e("debug_connect", "接连成功");
				} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
					if (connFlag) {
						onConnectResult(ConnResult.CONN_DIS);
						close(gatt);
					} else { //连接超时
						onConnectResult(ConnResult.CONN_FAILURE);
					}
				}
			} else {
				if (connFlag) {
					close(gatt);
					onConnectResult(ConnResult.CONN_DIS);
				} else {
					onConnectResult(ConnResult.CONN_FAILURE);
				}
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);
			characteristic = gatt.getService(serviceUUID).getCharacteristic(characteristicUUID);
			_enable_notify_(gatt, characteristic, NotificationType.NOTIFICATION);
			Log.e("debug_discovered", status + "");
			onLoadCharacteristic(gatt);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicRead(gatt, characteristic, status);
			onRead(characteristic);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
			byte[] data = characteristic.getValue();
			StringBuilder sb = new StringBuilder();
			for (byte b : data) {
				sb.append(String.format("%02x", (b & 0xff)));//todo 百度
			}
			Log.e("sb",sb.toString());
			onChanged(characteristic);


		}
	};

	public void onConnectResult(ConnResult connResult) {
		if (bleListener != null) {
			bleListener.onConnectResult(connResult);
		}
	}

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

	private void close(BluetoothGatt gatt) {
		if (gatt != null) {
			gatt.close();
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
	public static interface BleListener {

		void onConnectResult(ConnResult connResult);

		void onLoadCharacteristic(BluetoothGatt gatt);

		void onChanged(BluetoothGattCharacteristic characteristic);
	}

}
