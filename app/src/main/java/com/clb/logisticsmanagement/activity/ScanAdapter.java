package com.clb.logisticsmanagement.activity;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clb.logisticsmanagement.R;
import com.clb.logisticsmanagement.domain.BleDevice;

import java.util.ArrayList;

public abstract class ScanAdapter extends BaseAdapter {
	private ArrayList<BleDevice> data;		//BleDevice类型的ArrayList
	private Context context;

	//绑定适配器内容
	public ScanAdapter(ArrayList<BleDevice> data, Context context) {
		this.data = data;
		this.context = context;
	}

	/**
	 * 常规操作，Adapter所需要重写的一些方法
	* */
	@Override
	public int getCount() {
		int count = 0;
		if (data != null) {
			count = data.size();
		}
		return count;
	}

	@Override
	public Object getItem(int i) {
		return data.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {

		View ret = null;

		if (view != null) {
			ret = view;
		} else {
			//绑定Item
			LayoutInflater inflater = LayoutInflater.from(context);
			ret = inflater.inflate(R.layout.list_item_scan, viewGroup, false);
		}

		//初始化
		ViewHolder viewHolder = (ViewHolder) ret.getTag();
		if (viewHolder == null) {
			viewHolder = new ViewHolder();
			viewHolder.name = ret.findViewById(R.id.blueName);
			viewHolder.adress = ret.findViewById(R.id.blueAdress);
			viewHolder.layout = ret.findViewById(R.id.scanAdapterLayout);
			ret.setTag(viewHolder);
		}

		//从BleDevice对象导出保存的设备名字和地址
		final BleDevice bleDevice = data.get(i);		//TODO:为何要final

		Log.e("debug_data", data.get(i).getName() + "");

		viewHolder.name.setText(bleDevice.getName());
		viewHolder.adress.setText(bleDevice.getAdress());

		viewHolder.layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				scanOnClick(bleDevice);
			}
		});

		//返回填充好的View对象
		return ret;
	}

	//先声明，方法在ScanActivity实现
	//TODO：为什么要抽象化然后再重写，为何不直接在使用的地方直接声明
	protected abstract void scanOnClick(BleDevice bleDevice);


	//封装起来的三个对象，每当ViewHolder对象，都会顺带新建三个内部对象
	static class ViewHolder {
		public RelativeLayout layout;
		public TextView name;
		public TextView adress;
	}
}
