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
 * Created by Liber on 2018/3/11.
 */

public class RootActivity extends Activity {

    //蓝牙适配器
    protected BluetoothAdapter bluetoothAdapter;
    protected BluetoothManager bluetoothManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //是否打开蓝牙
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "   请打开手机蓝牙", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "   请打开手机蓝牙", Toast.LENGTH_SHORT).show();
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
            }
        }
    }
}
