package com.gvg.psugoservice;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	String deviceID;
	String phoneNumber;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		phoneNumber = tm.getLine1Number();
		if(phoneNumber != null){
			TextView text = (TextView) findViewById(R.id.phone_number);
			text.setText(phoneNumber);
		}
		
		deviceID = tm.getDeviceId();
		if(deviceID != null){
			TextView text = (TextView) findViewById(R.id.phone_id);
			text.setText(deviceID);
		}
	}
	
	//start the service
	public void onClickStartServie(View V)
	{
		//start the service from here //MyService is your service class name
		startService(new Intent(this, PsugoService.class));
	}
	//Stop the started service
	public void onClickStopService(View V)
	{
		//Stop the running service from here//MyService is your service class name
		//Service will only stop if it is already running.
		stopService(new Intent(this, PsugoService.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
