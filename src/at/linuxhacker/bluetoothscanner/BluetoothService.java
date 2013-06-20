package at.linuxhacker.bluetoothscanner;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class BluetoothService extends Service {

	private static final String TAG = BluetoothService.class.getSimpleName();
	private static boolean SERVICE_STATUS = false;
	private static final int NOTIFICATION_ID = 1;
	private static boolean DOING_NOTIFICATION = false;
	private static boolean INITIAL_RECEIVER_REGISTRATION = true;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		//Toast.makeText(this, "Service Created", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onCreate");
	}
	
	@Override
	public void onDestroy() {
		SERVICE_STATUS = false;
		Toast.makeText(this, "Service Stopped",  Toast.LENGTH_SHORT).show();
		createStatusBarNotification(getResources().
				getString(R.string.notification_stop_scanning));
		Log.d(TAG, "onDestroy");
	}

	@Override
	public void onStart(Intent intnet, int startid) {
		SERVICE_STATUS = true;
		
		Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "OnStart");
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				while (SERVICE_STATUS==true) {
					Log.d(TAG, "Scanning");
					createStatusBarNotification(getResources().
							getString(R.string.notification_scanning));
					bluetoothScanner();
					try {
						Thread.sleep(getIntValueFromPreferences(
								"edittext_checkinterval")*1000); //value in seconds
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();
		Log.d(TAG, "Thread wurde gestartet");
	}
	
	private void createStatusBarNotification(CharSequence text)
	{
		
		NotificationManager notificationManager = (NotificationManager) 
				getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = text;
		long when = System.currentTimeMillis();
		Context context = this;
		
		CharSequence contentTitle = getResources().getString(R.string.app_name);
		CharSequence contentText = text;
		
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
		Notification notification = new Notification(icon,tickerText,when);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		notificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	private void bluetoothScanner() {
	    //Create Bluetooth Reference
		final BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        
        //Check for Bluetooth Support
        if(bluetooth != null)
        {
        	//BroadcastReceiver
            final BroadcastReceiver mReceiver = new BroadcastReceiver() 
            { 
            	public void onReceive(Context context, Intent intent) 
                {
            		SharedPreferences prefs = PreferenceManager
            				.getDefaultSharedPreferences(getBaseContext());
            		String macadress = prefs.getString("edittext_macadress", "false");
                    String action = intent.getAction();
                    
                    // When discovery finds a device 
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) 
                    {
                    	// Get the BluetoothDevice object from the Intent 
                    	BluetoothDevice device = intent.getParcelableExtra(
                    			BluetoothDevice.EXTRA_DEVICE);
                    	String deviceTag = device.getAddress() + 
                    			device.getName();
                    	
                    	//If Address matches String Pattern, call Function
                    	if (deviceTag.toLowerCase().contains(macadress.toLowerCase()) )
                    	{
                    		Log.d(TAG, "ERNST" + deviceTag);
                    		handleEvent();
                    	}

                    }
                }

            };
            
            //String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
            if(INITIAL_RECEIVER_REGISTRATION==true){
            	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            	registerReceiver(mReceiver, filter);
            	INITIAL_RECEIVER_REGISTRATION=false;
            }
            
            if (bluetooth.isDiscovering()){
            	bluetooth.cancelDiscovery();
            }
            
            bluetooth.startDiscovery();
        }
    }
	
	private void handleEvent() {
		createStatusBarNotification(getResources().
				getString(R.string.notification_detected));
		
		Runnable o = new Runnable() {
			@Override
			public void run() {
				if (getBooleanValueFromPreferences("checkbox_sms")==true)
					sendSMS();
				try {
					Thread.sleep(getIntValueFromPreferences(
							"edittext_notificationinterval")*1000); //value in seconds
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				DOING_NOTIFICATION=false;
			}
		};
		
		if (DOING_NOTIFICATION==false) {
			DOING_NOTIFICATION=true;
			Thread n = new Thread(o);
			n.start();
		}

	}

	private int getIntValueFromPreferences(String preference)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		int value = Integer.
				parseInt(prefs.getString(preference, "1000"));
		return value;
	}
	
	private boolean getBooleanValueFromPreferences(String preference)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		boolean value = prefs.getBoolean(preference, false);
		return value;
	}
	
	private String getStringValueFromPreferences(String preference)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String value = prefs.getString(preference, "false");
		return value;
	}
	
	private void sendSMS() {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(
				getStringValueFromPreferences("edittext_sms_phonenumber"), null, 
				getStringValueFromPreferences("edittext_sms_message"), null, null);
	}
}
