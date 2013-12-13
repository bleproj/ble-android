package com.ble.ble;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.ResultReceiver;
import android.widget.Toast;

public class CheckinService extends IntentService {
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
	
	public CheckinService() {
		super(CheckinService.class.getName());
		// TODO Auto-generated constructor stub
	}

	@Override
    protected void onHandleIntent(Intent workIntent) {
		intent = workIntent;
		uuid = intent.getStringExtra("UUID");
		option = intent.getStringExtra("option");
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
				showResultAndExit(resultCode);
			}
		}.start();
	}
	
	public void showResultAndExit(int resultCode){
		ResultReceiver rec = intent.getParcelableExtra("receiver");
		rec.send(resultCode, intent.getExtras());
		stopSelf();
	}
}
