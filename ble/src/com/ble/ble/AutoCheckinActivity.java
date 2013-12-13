package com.ble.ble;

import java.util.Vector;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;

public class AutoCheckinActivity extends Activity implements IBeaconConsumer{
	static final int CHECKIN_REQUEST = 1;
	static final int CHECKIN_SUCCESS = 1;
	static final int CHECKIN_FAIL = 2;
	BluetoothAdapter bta;
	boolean hasBLE = false;
	Vector<Region> regions;
	public Handler timeHandler;
	private boolean active;
    
    //iBeacon stuff
    private IBeaconManager iBeaconManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_checkin);

		bta = BluetoothAdapter.getDefaultAdapter();
		timeHandler = new Handler();
		
		//Check if device supports bluetooth low energy
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
			hasBLE = true;
			iBeaconManager = IBeaconManager.getInstanceForApplication(this);
			iBeaconManager.bind(this);
			
			regions = new Vector<Region>(1,1);
			//Set regions, should be taken from database
			//for regions in database .....
			regions.add(new Region("M7012E", "23542266-18D1-4FE4-B4A1-23F8195B9D39", 1, null));
		} else {
			Toast.makeText(this, "Your phone does not support bluetooth low energy", Toast.LENGTH_LONG).show();
			finish();
		}
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	    this.registerReceiver(mReceiver, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.auto_checkin, menu);
		return true;
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mReceiver);
		iBeaconManager.unBind(this);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		active = true;
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		active = false;
	}
    
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
        @Override
        public void didEnterRegion(Region region) {
        	checkin(region.getUniqueId(), region.getProximityUuid(),"checkin").run();
        }

        @Override
        public void didExitRegion(Region region) {
        	checkin(region.getUniqueId(), region.getProximityUuid(),"checkout").run();
        }

		@Override
		public void didDetermineStateForRegion(int arg0, Region arg1) {
		}
        });

        try {
        	for(Region region : regions){
        		iBeaconManager.startMonitoringBeaconsInRegion(region);
        	}
        } catch (RemoteException e) {   }

    }
    
    public void myToast(String msg){
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    public Runnable checkin(final String event, final String uuid, final String option){
    	return new Runnable() {
            public void run() {
            	Intent intent;
            	if (active){
            		intent = new Intent(getApplicationContext(), SendCheckinActivity.class);
            	} else {
            		intent = new Intent(getApplicationContext(), CheckinService.class);
            	}
            	intent.putExtra("UUID", uuid);
            	intent.putExtra("option", option);
            	intent.putExtra("event", event);
            	if (active){
            		startActivityForResult(intent,CHECKIN_REQUEST);
            	} else {
            		intent.putExtra("receiver", resRec);
            		startService(intent);
            	}
            }
    	};
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode <= CHECKIN_REQUEST){
			if(resultCode == CHECKIN_SUCCESS){
				Toast.makeText(getApplicationContext(),
						"Checkin done\n"+data.getStringExtra("event"), Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(getApplicationContext(),
						"Checkin failed, will try again in 60 seconds.\n"+data.getStringExtra("event"), Toast.LENGTH_LONG)
						.show();
				timeHandler.postDelayed(checkin(data.getStringExtra("event"), data.getStringExtra("UUID"),data.getStringExtra("option")), 60000L);
			}
		}
	}
	
	public void minimizeButton(View v){
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}
	
	public void backButton(View v){
		finish();
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        final String action = intent.getAction();

	        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
	            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
	                                                 BluetoothAdapter.ERROR);
	            switch (state) {
	            case BluetoothAdapter.STATE_OFF:
	            	finish();
	                break;
	            case BluetoothAdapter.STATE_TURNING_OFF:
	            	finish();
	            	break;
	            }
	        }
	    }
	};
	
	private final ResultReceiver resRec = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == 200){
            	Toast.makeText(getApplicationContext(),
						"Checkin done\n"+resultData.getString("event"), Toast.LENGTH_LONG)
						.show();
            } else {
            	Toast.makeText(getApplicationContext(),
						"Checkin failed, will try again in 60 seconds.\n"+resultData.getString("event"), Toast.LENGTH_LONG)
						.show();
				timeHandler.postDelayed(checkin(resultData.getString("event"), resultData.getString("UUID"),resultData.getString("option")), 60000L);
            }
        }
    };
}
