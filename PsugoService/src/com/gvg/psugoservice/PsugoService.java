package com.gvg.psugoservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.gvg.psugoservice.MyLocation.LocationResult;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;


public class PsugoService extends Service {

	private static final String TAG = "PsugoService";
	private static final String serverAddress= "http://psugo.primature.ht/";
	private static final String localisationService = "PsugoSoapServer/localisation";
	private static final String newApkService = "PsugoSoapServer/newApk";

	private static final String downloadDirectory = "Download/";

	double lat = -1;
	double lng = -1;;
	boolean firstTime = true;
	String phoneNumber;
	String deviceID;
	String simID;
	protected ServerTask serverTask;
	int serverInterval = -1; 
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		if(firstTime == true){
			Toast.makeText(this, "Psugo Service Created", Toast.LENGTH_LONG).show();
			serverTask = new ServerTask(this);
	        serverTask.execute( this );
		}
		
		LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location location){
		        //Got the location!
		    	lat = location.getLatitude();
		    	lng = location.getLongitude();
				serverInterval = -1;
		    }
		};
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(this, locationResult);
		firstTime = false;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Toast.makeText(this, "Psugo Service Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");
		serverInterval = -1; 
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "Psugo Service Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		serverTask.cancel(true);
	}

	protected class ServerTask extends AsyncTask<Context, Integer, String>
	{
		private static final int ERROR = -1;
		private static final int UNKNOWN = 0;
		private static final int OK = 1;

		PsugoService parent;
		
		public ServerTask(PsugoService p){
			parent = p;
		}

		// -- called from the publish progress 
		// -- notice that the datatype of the second param gets passed to this method
		@Override
		protected void onProgressUpdate(Integer... values) 
		{
			super.onProgressUpdate(values);
		}

		@Override
		protected String doInBackground(Context... arg0) {
			int serverState = UNKNOWN;
			TelephonyManager tm;
			int timeElapsed = 0;
			Time time = new Time();
			long now;
			long lastTime;
			double last_lat = -1;
			double last_lng = -1;
			
			time.setToNow();
			lastTime = time.toMillis(true);
			
			while(true){
				time.setToNow();
				now = time.toMillis(true);
				if((now - serverInterval) > lastTime){
					lastTime = now;
					tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
					phoneNumber = tm.getLine1Number();
					deviceID = tm.getDeviceId();
					simID = tm.getSimSerialNumber();
					
					// Pour des tests avec  l'émulateur
					if (deviceID.equals("000000000000000")){
						phoneNumber = "123-4567";
						simID = "abc123efg";
						lat = 12.3456;
						lng = 45.6789;		
					}
					if(phoneNumber == null || phoneNumber.isEmpty())
						phoneNumber = deviceID;
					
					if(phoneNumber != null && !phoneNumber.isEmpty() && !(lat == last_lat && lng == last_lng) && lat != 0 && lng != 0){
						last_lat = lat;
						last_lng = lng;
						// Create data
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
						nameValuePairs.add(new BasicNameValuePair("t", phoneNumber));
						nameValuePairs.add(new BasicNameValuePair("c", "lat:" + Double.toString(lat) + " lng:" + Double.toString(lng)));
						HTTPPost post = new HTTPPost(serverAddress+localisationService);
						if(post.Send(nameValuePairs) == -1)
							serverState =  ERROR;
						else
							serverState =  OK;
						
						serverInterval = 60*60*1000; // one hour
					}
					else{
						Log.d(TAG, "lat, lng or phone number invalid ");
						serverInterval = 120*1000; // 2 minutes
					}
					if(phoneNumber != null)
					{
						String apkName = newApk(phoneNumber);
						if(apkName != null){
							if(UpdateApk(serverAddress + downloadDirectory + apkName))
								apkInstall(phoneNumber, apkName);
						}
					}
				}
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (isCancelled()) break;
			}
			Log.d(TAG, "DoInBackground cancelled!!!");
			return "0";
		}
	}

	public String newApk( String phoneNumber){
		String apkName = null;
		int serverState = 0;

		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(serverAddress+newApkService);

		try {
			// Create data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("t", phoneNumber));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			apkName = rd.readLine();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		return apkName;
	}

	public boolean UpdateApk(String apkurl){
		try {
			URL url = new URL(apkurl);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.connect();

			String PATH = Environment.getExternalStorageDirectory() + "/download/";
			File file = new File(PATH);
			file.mkdirs();
			File outputFile = new File(file, "app.apk");
			FileOutputStream fos = new FileOutputStream(outputFile);

			InputStream is = c.getInputStream();

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len1);
			}
			fos.close();
			is.close();

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/" + "app.apk")), "application/vnd.android.package-archive");
			startActivity(intent);

		} catch (IOException e) {
			Log.d(TAG, "Update error!");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void apkInstall( String phoneNumber, String apkName){
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(serverAddress+newApkService);

		try {
			// Create data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("t", phoneNumber));
			nameValuePairs.add(new BasicNameValuePair("a", apkName));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
}
