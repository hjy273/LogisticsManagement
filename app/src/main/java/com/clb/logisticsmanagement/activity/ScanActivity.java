package com.clb.logisticsmanagement.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clb.logisticsmanagement.R;
import com.clb.logisticsmanagement.ble.BleHelper;
import com.clb.logisticsmanagement.ble.ScanHelper;
import com.clb.logisticsmanagement.ble.conn.ConnResult;
import com.clb.logisticsmanagement.domain.BleDevice;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * 蓝牙的扫描和连接，完成后返回给显示界面
 */

public class ScanActivity extends RootActivity implements View.OnClickListener, BleHelper.BleListener {

	private ImageView leftImage;
	private ImageView rightImage;
	private TextView centerTitle;
	private ListView listView;
	private ScanAdapter adapter;
	ArrayList<BleDevice> list = new ArrayList<>();
	HashSet<String> hashSet = new HashSet<>();

	private BleHelper bleHelper = BleHelper.getHelper();
	private ScanHelper scanHelper = ScanHelper.getScanHelper();
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_scan);
		initView();

		adapter = new ScanAdapter(list, this) {
			@Override
			protected void scanOnClick(final BleDevice bleDevice) {
				Toast.makeText(ScanActivity.this, "正在尝试连接，请等候", Toast.LENGTH_SHORT).show();
				progressDialog.show();
				bleHelper.conn(bleDevice, ScanActivity.this);
			}
		};

		scanHelper.startScan(new ScanHelper.ScanListener() {
			@Override
			public void parserDevice(final BluetoothDevice bluetoothDevice, byte[] data, int rssi) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!hashSet.contains(bluetoothDevice.getAddress())) {
							hashSet.add(bluetoothDevice.getAddress());
							Log.e("debug_scan_name", bluetoothDevice.getName());
							list.add(new BleDevice(bluetoothDevice.getName(), bluetoothDevice.getAddress()));
							adapter.notifyDataSetChanged();
						}
					}
				});
			}
		});
		listView.setAdapter(adapter);
	}

	//初始化
	private void initView() {
		leftImage = findViewById(R.id.leftImage);
		rightImage = findViewById(R.id.rightImage);
		centerTitle = findViewById(R.id.centerTitle);
		listView = findViewById(R.id.listView);

		centerTitle.setText("扫描蓝牙设备");

		//设置标题左右图片
		leftImage.setImageResource(R.mipmap.goback);
		rightImage.setImageResource(R.mipmap.reflash);

		//设置监听
		leftImage.setOnClickListener(this);
		rightImage.setOnClickListener(this);
		bleHelper.resigistBleListener(this);

		//设置连接间隔
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setMessage("正在连接设备，请等候....");

	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
			case R.id.leftImage:
				finish();
				break;
			case R.id.rightImage:
				//重新扫描
				reFlashBle();
				break;
			default:
				Toast.makeText(this, "上传", Toast.LENGTH_SHORT).show();
				break;
		}

	}

	//重新扫描
	private void reFlashBle() {

		list.clear();
		hashSet.clear();
		scanHelper.startScan(new ScanHelper.ScanListener() {
			@Override
			public void parserDevice(final BluetoothDevice bluetoothDevice, byte[] data, int rssi) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!hashSet.contains(bluetoothDevice.getAddress())) {
							hashSet.add(bluetoothDevice.getAddress());
							list.add(new BleDevice(bluetoothDevice.getName(), bluetoothDevice.getAddress()));
							adapter.notifyDataSetChanged();
						}
					}
				});
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		scanHelper.stopScan();
	}

	@Override
	public void onConnectResult(final ConnResult connResult) {            //todo  内部方法外部变量
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (connResult == ConnResult.CONN_DIS) {
					Toast.makeText(ScanActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
				}
				if (connResult == ConnResult.CONN_FAILURE) {
					Toast.makeText(ScanActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
				}
//				if (connResult == ConnResult.CONN_SUCCESS) {
//					Toast.makeText(ScanActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
//				}
			}
		});
	}

	@Override
	public void onLoadCharacteristic(BluetoothGatt gatt) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(ScanActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
				progressDialog.cancel();
				setResult(999);
				finish();
			}
		});
	}

	@Override
	public void onChanged(BluetoothGattCharacteristic characteristic) {

	}
}
