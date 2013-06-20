package at.linuxhacker.bluetoothscanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final int REQUEST_ENABLE_BT = 1;
	
	Button buttonStart, buttonStop;
	TextView out;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		buttonStart = (Button) findViewById(R.id.buttonStart);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		out = (TextView) findViewById(R.id.out);
		
		buttonStart.setOnClickListener(this);
		buttonStop.setOnClickListener(this);
	}

	//Inflate Options Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		//return super.onCreateOptionsMenu(menu);
		return true;
	}
	
	//React to selected Option
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_config:
			startActivity(new Intent(this, MenuConfig.class));
			return true;
		case R.id.menu_alarm:
			startActivity(new Intent(this, MenuAlarm.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.buttonStart:
			this.startBluetooth();
			Log.d(TAG, "onClick: starting service");
			startService(new Intent(this, BluetoothService.class));
			break;
		case R.id.buttonStop:
			Log.d(TAG, "onClick: stopping service");
			stopService(new Intent(this, BluetoothService.class));
			break;
		}
	}

	private void startBluetooth() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
		if (!mBluetoothAdapter.isEnabled()) {
		        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); 
		        }
		}
	
}
