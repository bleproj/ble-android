package com.ble.ble;

import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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
	private String option;
	private String accessToken;
	private HttpClient client;
	private HttpPost post;
	private HttpResponse response;
	private static final String url = "http://bleserver.broccomoped.se/api/checkins";
	JSONObject json;
	StringEntity se;
	HttpEntity entity;
	Intent intent;
	SharedPreferences prefs;
	String userName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_checkin);
		intent = getIntent();
		uuid = intent.getStringExtra("UUID");
		option = intent.getStringExtra("option");
		TextView t = (TextView)findViewById(R.id.textView1);
		if(option.equals("checkout")){
			t.setText("Checking out");
		} else {
			t.setText("Checking in");
		}
		send();
	}
	
	public void send(){
		new Thread() {
			public void run() {
				int resultCode = -1;
				client = new DefaultHttpClient();
				post = new HttpPost(url);
				prefs = getSharedPreferences("Credentials",0);
				accessToken = prefs.getString("access_token", "");
				userName = prefs.getString("username", "");
				json = new JSONObject();
				try {
					//Make sure JSON stuff matches the API when it's done
					//Token
					json.put("username", userName);
					json.put("access_token", accessToken);
					//UUID of ble device to check in against
					json.put("uuid", uuid);
					//option, check in or out
					json.put("option", option);
					se = new StringEntity(json.toString());
					se.setContentEncoding("UTF-8");
					se.setContentType("application/json");
					post.setEntity(se);
					response = client.execute(post);
					// Get hold of the response entity
					entity = response.getEntity();
					if (entity != null) {  					  
						String retSrc = EntityUtils.toString(entity);
						System.out.println(retSrc);
						// parsing JSON
						JSONObject result = new JSONObject(retSrc); //Convert String to JSON Object

						// Storing each json item in variable
						resultCode = result.getInt("code");
					}
				} catch (Exception e){
					e.printStackTrace();
				}
				setStatusAndExit(resultCode);
			}
		}.start();
	}
	
	public void setStatusAndExit(int resultCode){
		if (resultCode == 200){
			setResult(1, intent);
		} else {
			setResult(2, intent);
		}
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.send_checkin, menu);
		return true;
	}

}
