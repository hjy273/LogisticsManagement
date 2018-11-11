package com.clb.logisticsmanagement.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

/**
 * 权限
 */

public class RootActivity extends Activity {

    //蓝牙适配器
    protected BluetoothAdapter bluetoothAdapter;
    protected BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //初始化
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //是否打开蓝牙
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "   请打开手机蓝牙", Toast.LENGTH_SHORT).show();
            //弹出请求是否打开蓝牙
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
        }
    }

    @Override
    //回调，检查时候打开蓝牙成功，否则继续请求打开
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "   请打开手机蓝牙", Toast.LENGTH_SHORT).show();
                //请求打开蓝牙
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
            }
        }
    }
}
