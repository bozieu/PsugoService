package com.gvg.psugoservice;


import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	String deviceID;
	String phoneNumber;
	String accuracy;
	String provider;
	String longitude;
	String latitude;
	
	private LocationManager locationManager;
	Location lastLocation = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		
	    // Get the location manager
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 60*1000, 10, myLocationListener);
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
		if(accuracy != null){
			TextView text = (TextView) findViewById(R.id.accuracy);
			text.setText(accuracy);
		}
	}
	
    LocationListener myLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        	if(lastLocation == null){
        		lastLocation = location;
            	updateLocationGUI(location);
        	}
        	if (location.getAccuracy() <  lastLocation.getAccuracy() || lastLocation.getTime() + 5 * 60 * 1000 > location.getTime()){
        		lastLocation = location;
            	updateLocationGUI(location);
        	}
        }            
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
    
	public void updateLocationGUI(Location location){
		TextView text = (TextView) findViewById(R.id.provider);
		provider = location.getProvider();
		text.setText(provider);
		
		text = (TextView) findViewById(R.id.accuracy);
		accuracy = String.valueOf(location.getAccuracy());
		text.setText(accuracy);

		text = (TextView) findViewById(R.id.longitude);
		longitude = String.valueOf(location.getLongitude());
		text.setText(longitude);

		text = (TextView) findViewById(R.id.latitude);
		latitude = String.valueOf(location.getLatitude());
		text.setText(latitude);
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
