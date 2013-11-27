package com.ble.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class CheckinActivity extends Activity {
	BluetoothAdapter bta;
	static final int REQUEST_ENABLE_BT = 1;
	static final int REQUEST_ENABLE_BT_MANUAL = 2;
	static final int REQUEST_ENABLE_BT_AUTO = 3;
	Intent enableBtIntent;
	Intent startManual;
	Intent startAuto;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_checkin);
		bta = BluetoothAdapter.getDefaultAdapter();
		if(bta == null){
			Toast.makeText(this, "Your device does not support bluetooth :(", Toast.LENGTH_LONG).show();
			this.finish();
		}
		if(!bta.isEnabled()){
			enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.checkin, menu);
		return true;
	}
	
	/**
	 * Runs automatically when user responds to start bluetooth request
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode <= REQUEST_ENABLE_BT_AUTO){
			if(resultCode != RESULT_OK){
				Toast.makeText(this, "You need to have bluetooth enabled to be able to check in.", Toast.LENGTH_LONG).show();
			} else {
				if(requestCode == REQUEST_ENABLE_BT_MANUAL){
					startActivity(startManual);
				}
				if(requestCode == REQUEST_ENABLE_BT_AUTO){
					startActivity(startAuto);
				}
			}
		}
	}

	public void manualButton(View v){
		startManual = new Intent(this, ManualCheckinActivity.class);
		if(!bta.isEnabled()){
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_MANUAL);
		} else {
			startActivity(startManual);
		}
	}
	
	public void autoButton(View v){
		startAuto = new Intent(this, AutoCheckinActivity.class);
		if(!bta.isEnabled()){
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_AUTO);
		} else {
			startActivity(startAuto);
		}
	}
}
