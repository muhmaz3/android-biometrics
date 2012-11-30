package android.biometrics;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ScreenSettings extends PreferenceActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.screen_preference);
	};
	
	@Override
	public void onBackPressed() {
		Biometrics.isFirstTime = false;
		setResult(RESULT_OK);
		super.onBackPressed();
	}
}
