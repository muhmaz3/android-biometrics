package android.biometrics;

import android.app.Application;
import android.biometrics.util.AppUtil;
import android.util.Log;

public class Biometrics extends Application {
//	public static boolean isFirstTime = true;
	
	@Override 
	public void onCreate() {
		super.onCreate();
		AppUtil.createAppDirectory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.i(Biometrics.class.getName(), "Biometrics app terminated");
	}
    
    
}