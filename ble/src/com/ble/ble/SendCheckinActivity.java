package com.ble.ble;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class SendCheckinActivity extends Activity {
	private String uuid;
	private int option;
	private SharedPreferences accessToken;
	private HttpClient client;
	private HttpPost post;
	private HttpResponse response;
	private static final String url = "http://bleserver.broccomoped.se/api/checkin";
	JSONObject json;
	StringEntity se;
	HttpEntity entity;

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
		if(send()){
			setResult(1, intent);
		} else {
			setResult(2, intent);
		}
	}
	
	public boolean send(){
		client = new DefaultHttpClient();
		post = new HttpPost(url);
		accessToken = getSharedPreferences("Token",0);
		json = new JSONObject();
		try {
			//Make sure JSON stuff matches the API when it's done
			//Token
			json.put("token", accessToken);
			//UUID of ble device to check in against
			json.put("UUID", uuid);
			//option, check in or out
			json.put("option", option);
			se = new StringEntity(json.toString());
			se.setContentEncoding("UTF-8");
			se.setContentType("application/json");
			post.setEntity(se);

			response = client.execute(post);
			// Get hold of the response entity
			entity = response.getEntity();
			//Add check for the response too see if it succeeded or not
		} catch (Exception e){
			return false;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.send_checkin, menu);
		return true;
	}

}
