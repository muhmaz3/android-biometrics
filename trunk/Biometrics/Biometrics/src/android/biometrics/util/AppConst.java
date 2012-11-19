package android.biometrics.util;

import android.os.Environment;

public class AppConst {
	/**
	 * Folder hierachy: 
	 * /mnt/sdcard/biometrics: contains .jpg(face image), .wav(voice)
	 * - ./face : contains private data for face recognition AND
	 * 				+ image (resized): deleted after creating gray-scale image
	 * 				+ image (gray-scale): .pgm files 
	 * - ./voice : contains private data for voice recognition
	 */
	public static final String APP_FOLDER = 
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/biometrics";
	public static final String FACE_FOLDER = APP_FOLDER + "/" + "face";
	public static final String VOICE_FOLDER = APP_FOLDER + "/" + "voice"; 
	public static final String FACE_DATA_FILE_PATH = FACE_FOLDER + "/" + "face_data.xml";
	public static final String TRAING_VOICE_FILE_PATH = VOICE_FOLDER + "/" + "voice_data.txt";
	public static final String PREFERENCE_NAME = "app_preference";
	
	public static final float IMG_X_FACTOR = 1.19f;	// = 0.17*7
	public static final float IMG_Y_FACTOR = 1.36f;	// = 0.17*8
	public static final int IMG_FACE_WIDTH = 98;	// = 14*7
	public static final int IMG_FACE_HEIGHT = 112;	// = 14*8 
	
	public static final String KEY_FACE_TRAINED = "face_trained";
	public static final String KEY_VOICE_TRAINED = "voice_trained";
	
	public static final int THRESHOLD_VOICE = 600;
}
