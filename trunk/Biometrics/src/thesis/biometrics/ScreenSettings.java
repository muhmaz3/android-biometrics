package thesis.biometrics;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ScreenSettings extends PreferenceActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.screen_preference);
	};
}
