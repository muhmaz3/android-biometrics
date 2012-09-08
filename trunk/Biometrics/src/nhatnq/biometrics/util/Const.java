package nhatnq.biometrics.util;

import android.os.Environment;

public class Const {
	public static final String APP_FOLDER = 
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/biometrics";
	public static final String PREFERENCE_NAME = "app_preference";
	public static final String FACE_DATA_FILE = "face.dat";
	public static final String VOICE_DATA_FILE = "voice.dat"; 
}
