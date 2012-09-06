package nhatnq.biometrics.util;

import android.os.Environment;

public class Const {
	public static final String APP_FOLDER = "FaceRecognition";
	public static final String PREFERENCE_NAME = "FacePreference";
	public static final String PREVIEW_TRAINING = Environment.getExternalStorageDirectory() 
			+ "/"+APP_FOLDER + "/data/preview/train";
	public static final String PREVIEW_TESTING = Environment.getExternalStorageDirectory() 
			+ "/"+APP_FOLDER + "/data/preview/test";
	public static final String DATA_FOLDER = Environment.getExternalStorageDirectory() 
			+ "/"+APP_FOLDER + "/data/javacv";
}
