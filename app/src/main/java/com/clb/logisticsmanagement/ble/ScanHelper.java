package com.clb.logisticsmanagement.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 蓝牙扫描
 */

public class ScanHelper {

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private ScanHelper() {
    }

    private static ScanHelper scanHelper = new ScanHelper();

    public static ScanHelper getScanHelper() {
        return scanHelper;
    }

    private ScanListener scanListener;

    //扫描蓝牙
    public void startScan(ScanListener scanListener) {
        Log.e("debug_scan", "开始扫描");
        this.scanListener = scanListener;
        stopScan();
        bluetoothAdapter.startLeScan(scanCallback);

        //定时器5秒后停止扫描
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopScan();
            }
        }, 5000);
    }

    //停止扫描
    public void stopScan() {
        Log.e("debug_scan", "停止扫描");
        bluetoothAdapter.stopLeScan(scanCallback);
    }

    //蓝牙连接回调
    final BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            String name = bluetoothDevice.getName();
            //如果以下都没有，没必要回调了
            if (name == null || bluetoothDevice == null || scanListener == null)
                return;
            Log.e("debug_scan", "bleName:" + name);
            //搜索到的设备列表
            scanListener.parserDevice(bluetoothDevice, bytes, i);
        }
    };

    //
    public interface ScanListener {
        //接口内方法亦抽象
        void parserDevice(BluetoothDevice bluetoothDevice, byte[] data, int rssi);
    }

}
