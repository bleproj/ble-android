package com.ble.ble;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class LoginActivity extends Activity {
	boolean testBool;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		testBool = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	public void clickedButton(View v){
		testBool = true;
		Intent intent = new Intent(this, CheckinActivity.class);
		startActivity(intent);
	}

	/**
	 * Exit instead of showing login screen when pressing the back button from other activity
	 */
	@Override
	protected void onResume(){
		super.onResume();
		if(testBool){
			finish();
		}
	}
}
