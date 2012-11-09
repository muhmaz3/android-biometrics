package nhatnq.biometrics;

import nhatnq.biometrics.util.AppUtil;
import android.app.Application;
import android.util.Log;

public class Biometrics extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(Biometrics.class.getName(), "Biometrics app created");
		AppUtil.createAppDirectory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(Biometrics.class.getName(), "Biometrics app terminated");
	}
    
    
}