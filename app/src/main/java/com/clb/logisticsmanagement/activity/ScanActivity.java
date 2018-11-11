package com.clb.logisticsmanagement.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
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

	ArrayList<BleDevice> list = new ArrayList<>();// ArrayList 是一个类。 ArrayList 继承并实现了List。而list的类型是BleDevice
	HashSet<String> hashSet = new HashSet<>();// 集合的意思，是同种对象的集合。<String>说明这种对象都是String类型的对象。Set本身是接口，所以需要实现，可以这样定义。HashSet是无序的，而TreeSet是有序的

	private BleHelper bleHelper = BleHelper.getHelper();
	private ScanHelper scanHelper = ScanHelper.getScanHelper();
	//TODO：作用是什么,为什么要用这个设置间隔
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_scan);
		initView();

		//TODO:蓝牙扫描列表
		adapter = new ScanAdapter(list, this) {
			@Override
			protected void scanOnClick(final BleDevice bleDevice) {
				Toast.makeText(ScanActivity.this, "正在尝试连接，请等候", Toast.LENGTH_SHORT).show();
				progressDialog.show();		//弹出对话框
				bleHelper.conn(bleDevice, ScanActivity.this);
			}
		};

		//进来立刻扫描  TODO:此处代码与重新扫描部分重复了，应该进行封装避免代码重复
		Scan();
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

		//TODO：设置连接间隔
		//弹出对话框，显示正在连接设备
		progressDialog = new ProgressDialog(this);//初始化
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

	//扫描方法的封装
	private void Scan(){
		//扫描
		scanHelper.startScan(new ScanHelper.ScanListener() {
			@Override
			//原方法没有实体，现在重写
			public void parserDevice(final BluetoothDevice bluetoothDevice, byte[] data, int rssi) {
				//TODO：为何要在主线程跑
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						//判断集合内是否有这个地址，无则添加到集合内
						if (!hashSet.contains(bluetoothDevice.getAddress())) {
							hashSet.add(bluetoothDevice.getAddress());
							list.add(new BleDevice(bluetoothDevice.getName(), bluetoothDevice.getAddress()));
							//notifyDataSetChanged方法通过一个外部的方法控制如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
							// 所以一定要在主线程跑这个
							adapter.notifyDataSetChanged();
						}
					}
				});
			}
		});
	}

	//重新扫描
	private void reFlashBle() {

		//清理集合列表
		list.clear();
		hashSet.clear();
		//扫描
		Scan();

	}

	@Override
	//TODO:onDestroy（）和finish（）的区别
	//finish函数仅仅把当前Activity退出了，系统只是将最上面的Activity移出了栈，并没有及时的调用onDestory（）方法，其占用的资源也没有被及时释放。因为移出了栈，所以当你点击手机上面的“back”按键的时候，也不会再找到这个Activity。
	// 在Activity的生命周期中，onDestory()方法是他生命的最后一步，资源空间等就被回收了。当重新进入此Activity的时候，必须重新创建，执行onCreate()方法。

	//因为搜索的功能比较耗费系统资源，所以不用的时候一定要释放
	protected void onDestroy() {
		super.onDestroy();
		scanHelper.stopScan();
	}

	@Override
	public void onConnectResult(final ConnResult connResult) {            //TODO:内部类方法访问外部类的变量,参考笔记《局部内部类和匿名内部类的特点和作用》：方法中的内部类只允许访问方法中的final局部变量和方法的final参数列表
		runOnUiThread(new Runnable() {
			@Override
			public void run() {														//TODO:使用枚举通常会比使用静态常量要消耗两倍以上的内存，在Android开发当中我们应当尽可能地不使用枚举。
				if (connResult == ConnResult.CONN_DIS) {				//TODO：为什么要用接口定义的常量，为什么要用enum枚举类型的变量，有什么特别的作用吗
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
				//对话弹框消失
				progressDialog.cancel();
				//设置结果码
				setResult(999);
				finish();
			}
		});
	}

	@Override
	public void onChanged(BluetoothGattCharacteristic characteristic) {

	}
}
