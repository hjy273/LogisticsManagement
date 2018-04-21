package com.clb.logisticsmanagement.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clb.logisticsmanagement.R;
import com.clb.logisticsmanagement.ble.BleHelper;
import com.clb.logisticsmanagement.ble.conn.ConnResult;

/**
 * 显示从蓝牙接收的数据和提交数据给后台
 */

public class MainActivity extends RootActivity implements View.OnClickListener, BleHelper.BleListener {

	//标题栏
	private ImageView rightImage;
	private TextView centerTitle;

	//中间显示的文本
	private TextView oneText;
	private TextView twoText;
	private TextView threeText;

	//提交按钮
	private Button submitButton;
	private Button goonButton;

	//回调的请求码
	public static final int RECODE = 111;

	private static BleHelper bleHelper = BleHelper.getHelper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//隐藏标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_main);
		initView();
	}

	private void initView() {
		rightImage = findViewById(R.id.rightImage);
		centerTitle = findViewById(R.id.centerTitle);
		submitButton = findViewById(R.id.submit);
		//发送数据
		goonButton = findViewById(R.id.goon);
		//温度，湿度，物品状况
		oneText = findViewById(R.id.oneText);
		twoText = findViewById(R.id.twoText);
		threeText = findViewById(R.id.threeText);

		//设置标题左右图片
		rightImage.setImageResource(R.mipmap.scan);

		//点击进入扫描界面
		rightImage.setOnClickListener(this);
		//标题中间的内容
		centerTitle.setText("物流管理系统");

		submitButton.setOnClickListener(this);
		goonButton.setOnClickListener(this);
		bleHelper.resigistBleListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.rightImage:
				startActivityForResult(new Intent(this, ScanActivity.class), RECODE);
				break;
			case  R.id.goon:
				bleHelper.sendData();
			default:
				Toast.makeText(this, "(＾－＾)", Toast.LENGTH_SHORT).show();
				break;
		}
	}

	//从扫描activity回调的
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RECODE) {
			if (resultCode == 999) {
				bleHelper.sendData();
				Toast.makeText(this, "开始接受数据,请等候", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		bleHelper.resigistBleListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bleHelper.disConn();
	}

	//蓝牙数据的回调
	@Override
	public void onConnectResult(ConnResult connResult) {
	}

	@Override
	public void onLoadCharacteristic(BluetoothGatt gatt) {
	}

	@Override
	public void onChanged(final BluetoothGattCharacteristic characteristic) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				byte[] data = characteristic.getValue();
				StringBuilder sb = new StringBuilder();
				for (byte b : data) {
					sb.append(String.format("%02x", (b & 0xff)));//todo 百度
				}
				//情况
				String state = sb.toString().substring(1,2) + sb.toString().substring(3,4);
				Log.e("state1",state);

				//判断物品状况
				int state_num = Integer.parseInt(state);
				if(state_num == 0){state = "损坏";}
				if (state_num == 1){state = "完好";}
				if(state_num != 1&&state_num != 0){state = "未知错误";}

				//温度
				String Temperature = sb.toString().substring(5,6) + sb.toString().substring(7,8);

				//绝对湿度
				String Humidity = sb.toString().substring(9,10) + sb.toString().substring(11);

				Log.e("state", state);
				Log.e("Temperature", Temperature);
				Log.e("humidity", Humidity);

				if (data.length > 0) {
					oneText.setText(Temperature +"℃");
					twoText.setText(Humidity + "%");
					threeText.setText(state);
				}
			}
		});
	}
}
