package at.linuxhacker.bluetoothscanner;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MenuConfig extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.menu_config);
	}
}