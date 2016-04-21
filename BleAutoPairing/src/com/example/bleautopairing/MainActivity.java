package com.example.bleautopairing;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */
	private static final String TAG = "BTPairingDemo";
	Button btnSearch, btnDis, btnExit;
	ToggleButton tbtnSwitch;
	ListView lvBTDevices;
	ArrayAdapter<String> adtDevices;
	List<String> lstDevices = new ArrayList<String>();
	BluetoothAdapter btAdapt;
	private static final String RemoteControlName = "GhicGamePad";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnSearch = (Button) this.findViewById(R.id.btnSearch);
		btnSearch.setOnClickListener(new ClickEvent());
		btnExit = (Button) this.findViewById(R.id.btnExit);
		btnExit.setOnClickListener(new ClickEvent());
		tbtnSwitch = (ToggleButton) this.findViewById(R.id.tbtnSwitch);
		tbtnSwitch.setOnClickListener(new ClickEvent());

		lvBTDevices = (ListView) this.findViewById(R.id.lvDevices);
		adtDevices = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, lstDevices);
		lvBTDevices.setAdapter(adtDevices);
		lvBTDevices.setOnItemClickListener(new ItemClickEvent());

		btAdapt = BluetoothAdapter.getDefaultAdapter();

		if (btAdapt.isEnabled()) {
			tbtnSwitch.setChecked(true);
		} else {
			tbtnSwitch.setChecked(false);
		}

		registerReceiver(searchDevices, initIntentFilter());
	}

	private final BroadcastReceiver searchDevices = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			BluetoothDevice device = null;
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() == BluetoothDevice.BOND_NONE) {
					Log.d(TAG, "Found: " + device.getName() + " Address: "
							+ device.getAddress());
					String str = "	未配对|" + device.getName() + "|"
							+ device.getAddress();
					Log.d(TAG, str);
					if (lstDevices.indexOf(str) == -1)// 防止重复添加
					{
						lstDevices.add(str);
					}

					if (device.getName().equals(RemoteControlName)) {
						Log.d(TAG, ">>>>>>>>>>>>>>>>");
						BluetoothDevice btDev = btAdapt.getRemoteDevice(device
								.getAddress());
						btAdapt.cancelDiscovery();
						try {
							if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
								ClsUtils.createBond(btDev.getClass(), btDev);
								Toast.makeText(
										MainActivity.this,
										getResources().getString(
												R.string.RemoteAutoPairing)
												+ getResources().getString(
														R.string.bleRemoteName),
										Toast.LENGTH_LONG).show();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						adtDevices.notifyDataSetChanged();
					}
				}
			} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				Log.d(TAG, "....................");
				device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				switch (device.getBondState()) {
				case BluetoothDevice.BOND_BONDING:
					Log.d(TAG, "Pairing......");
					break;
				case BluetoothDevice.BOND_BONDED:
					if (device.getName().equals(RemoteControlName)) {
						Log.d(TAG, ">>>>>>>>>>>>>>>>");
						Toast.makeText(
								MainActivity.this,
								getResources().getString(
										R.string.SuccessPairing)
										+ getResources().getString(
												R.string.bleRemoteName),
								Toast.LENGTH_LONG).show();
					}
					Log.d(TAG, "Finish Pairing" + device.getName());
					//
					String oldstr = "	未配对|" + device.getName() + "|"
							+ device.getAddress();
					lstDevices.remove(oldstr);
					String str = "	已配对|" + device.getName() + "|"
							+ device.getAddress();
					if (lstDevices.indexOf(str) == -1)// 防止重复添加
						lstDevices.add(str);
					Log.d(TAG, str);
					adtDevices.notifyDataSetChanged();
					//
					break;
				case BluetoothDevice.BOND_NONE:
					Log.d(TAG, "Cancel Pairing");
					//
					oldstr = "	已配对|" + device.getName() + "|"
							+ device.getAddress();
					lstDevices.remove(oldstr);
					str = "	未配对|" + device.getName() + "|"
							+ device.getAddress();
					if (lstDevices.indexOf(str) == -1)// 防止重复添加
						lstDevices.add(str);
					Log.d(TAG, str);
					adtDevices.notifyDataSetChanged();
					//
				default:
					break;
				}
			}

		}
	};

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(searchDevices);
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	class ItemClickEvent implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if (btAdapt.isDiscovering())
				btAdapt.cancelDiscovery();
			String str = lstDevices.get(arg2);
			String[] values = str.split("\\|");
			String address = values[2];
			Log.e(TAG, "address: " + values[2]);
			BluetoothDevice btDev = btAdapt.getRemoteDevice(address);
			btAdapt.cancelDiscovery();// Cancel discovery because it will slow
										// down the connection
			try {
				if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
					Log.d(TAG, ">>>>>>>>>>>>>>>>");
					btAdapt.cancelDiscovery();
					try {
						ClsUtils.createBond(btDev.getClass(), btDev);
						Toast.makeText(
								MainActivity.this,
								MainActivity.this.getResources().getString(
										R.string.RemoteRequest)
										+ " "
										+ getResources().getString(
												R.string.bleRemoteName),
								Toast.LENGTH_LONG).show();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
					Log.d(TAG, ">>>>>>>>>>>>>>>>");
					ClsUtils.removeBond(btDev.getClass(), btDev);
					Toast.makeText(
							MainActivity.this,
							MainActivity.this.getResources().getString(
									R.string.unRemoteRequest),
							Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class ClickEvent implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (v == btnSearch) {
				if (btAdapt.getState() == BluetoothAdapter.STATE_OFF) {
					Toast.makeText(
							MainActivity.this,
							MainActivity.this.getResources().getString(
									R.string.openBluetooth), Toast.LENGTH_LONG)
							.show();
					return;
				}
				if (btAdapt.isDiscovering())
					btAdapt.cancelDiscovery();
				lstDevices.clear();
				Object[] lstDevice = btAdapt.getBondedDevices().toArray();
				for (int i = 0; i < lstDevice.length; i++) {
					BluetoothDevice device = (BluetoothDevice) lstDevice[i];
					String str = "	已配对|" + device.getName() + "|"
							+ device.getAddress();
					lstDevices.add(str);
					adtDevices.notifyDataSetChanged();
				}
				setTitle(MainActivity.this.getResources().getString(
						R.string.LocalDevice)
						+ btAdapt.getAddress());
				btAdapt.startDiscovery();
			} else if (v == tbtnSwitch) {// 本机蓝牙启动/关闭
				if (tbtnSwitch.isChecked() == true) {
					btAdapt.enable();
				} else if (tbtnSwitch.isChecked() == false) {
					btAdapt.disable();
				}
			} else if (v == btnExit) {
				MainActivity.this.finish();
			}
		}
	}

	// Intent filter and broadcast receive to handle Bluetooth on event.
	private IntentFilter initIntentFilter() {
		IntentFilter intent = new IntentFilter();
		intent.addAction(BluetoothDevice.ACTION_FOUND);
		intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		return intent;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
