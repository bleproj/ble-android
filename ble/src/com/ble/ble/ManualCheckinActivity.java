package com.ble.ble;

import java.util.Vector;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;


public class ManualCheckinActivity extends Activity implements IBeaconConsumer{
	static final int CHECKIN_SUCCESS = 1;
	static final int CHECKIN_FAIL = 2;
	ListView listView;
	BluetoothAdapter bta;
	ArrayAdapter<String> adapter;
	Vector<String> uuids;
	ProgressBar p;
	Vector<Region> regions;
	static final int CHECKIN_REQUEST = 1;
    
    //iBeacon stuff
    private IBeaconManager iBeaconManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manual_checkin);
		//this.setTitle("Scanning");
		bta = BluetoothAdapter.getDefaultAdapter();
		p = (ProgressBar) findViewById(R.id.progressBar1);
		p.setVisibility(8);

		
		//Check if device supports bluetooth low energy
		if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
			iBeaconManager = IBeaconManager.getInstanceForApplication(this);
			iBeaconManager.bind(this);
			
			regions = new Vector<Region>(1,1);
			uuids = new Vector<String>(1,1);
			//Set regions, should be taken from database
			//for regions in database .....
			regions.add(new Region("M7012E", "23542266-18D1-4FE4-B4A1-23F8195B9D39", 1, null));
		} else {
			finish();
		}
		
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.listView1);

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written

        adapter = new ArrayAdapter<String>(this,
          android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter); 
        
        // ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {

              @Override
              public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {
            	  Intent intent = new Intent(getApplicationContext(), SendCheckinActivity.class);
                
            	  // ListView Clicked item value
            	  String event = (String) listView.getItemAtPosition(position);

            	  intent.putExtra("UUID", uuids.get(position));
            	  intent.putExtra("event", event);
            	  intent.putExtra("option", "checkin");
            	  startActivityForResult(intent,CHECKIN_REQUEST);
              }

         });
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	    this.registerReceiver(mReceiver, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manual_checkin, menu);
		return true;
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mReceiver);
		iBeaconManager.unBind(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode <= CHECKIN_REQUEST){
			if(resultCode == CHECKIN_SUCCESS){
				Toast.makeText(getApplicationContext(),
						"Checkin done\n"+data.getStringExtra("event"), Toast.LENGTH_LONG)
						.show();
				finish();
			} else {
				Toast.makeText(getApplicationContext(),
						"Checkin failed, please try again\n"+data.getStringExtra("event"), Toast.LENGTH_LONG)
						.show();
			}
		}
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
    //action: 1=add, 2 = remove
    private void logToDisplay(final String line, final int action, final String uuid) {
        runOnUiThread(new Runnable() {
            public void run() {
            	if(action == 1){
            		adapter.add(line);
            		uuids.add(uuid);
            		myToast();
            	} else {
                    adapter.remove(line);
                    uuids.remove(uuid);
            	}
            }
        });
}
    
    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
        @Override
        public void didEnterRegion(Region region) {
        	logToDisplay(region.getUniqueId(),1,region.getProximityUuid());
        }

        @Override
        public void didExitRegion(Region region) {
        	logToDisplay(region.getUniqueId(),2,region.getProximityUuid());
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
    
    public void myToast(){
    	Toast.makeText(getApplicationContext(),
                "Found it :D" , Toast.LENGTH_LONG)
                .show();
    }
}
