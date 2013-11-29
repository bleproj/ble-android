package com.ble.ble;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class SendCheckinActivity extends Activity {
	private String uuid;
	private int option; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_checkin);
		Intent intent = getIntent();
		uuid = intent.getStringExtra("UUID");
		option = intent.getIntExtra("option", 1);
		if(option == 2){
			TextView t = (TextView)findViewById(R.id.textView1);
			t.setText("Checking out");
		}
		Toast.makeText(this, uuid, Toast.LENGTH_LONG).show();
		setResult(2, intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.send_checkin, menu);
		return true;
	}

}
