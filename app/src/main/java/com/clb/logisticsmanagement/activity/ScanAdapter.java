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
	private ArrayList<BleDevice> data;
	private Context context;

	public ScanAdapter(ArrayList<BleDevice> data, Context context) {
		this.data = data;
		this.context = context;
	}

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
			LayoutInflater inflater = LayoutInflater.from(context);
			ret = inflater.inflate(R.layout.list_item_scan, viewGroup, false);
		}

		ViewHolder viewHolder = (ViewHolder) ret.getTag();
		if (viewHolder == null) {
			viewHolder = new ViewHolder();
			viewHolder.name = ret.findViewById(R.id.blueName);
			viewHolder.adress = ret.findViewById(R.id.blueAdress);
			viewHolder.layout = ret.findViewById(R.id.scanAdapterLayout);
			ret.setTag(viewHolder);
		}

		final BleDevice bleDevice = data.get(i);

		Log.e("debug_data", data.get(i).getName() + "");

		viewHolder.name.setText(bleDevice.getName());
		viewHolder.adress.setText(bleDevice.getAdress());

		viewHolder.layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				scanOnClick(bleDevice);
			}
		});

		return ret;
	}

	protected abstract void scanOnClick(BleDevice bleDevice);


	static class ViewHolder {
		public RelativeLayout layout;
		public TextView name;
		public TextView adress;
	}
}
