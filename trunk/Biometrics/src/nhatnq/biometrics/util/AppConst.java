package nhatnq.biometrics.util;

import android.os.Environment;

public class AppConst {
	/**
	 * Folder hierachy: 
	 * /mnt/sdcard/biometrics: contains .jpg(face image), .amr(voice) files
	 * - ./face : contains private data for face recognition AND
	 * 				+ image (resized): deleted after creating gray-scale image
	 * 				+ image (gray-scale): .pgm files 
	 * - ./voice : contains private data for voice recognition
	 */
	public static final String APP_FOLDER = 
			Environment.getExternalStorageDirectory().getAbsolutePath() + "/biometrics";
	public static final String FACE_FOLDER = APP_FOLDER + "/" + "face";
	public static final String VOICE_FOLDER = APP_FOLDER + "/" + "voice"; 
	public static final String PREFERENCE_NAME = "app_preference";
	
	//Rate: 5:6 ~ width:height of image
	public static final float IMG_X_FACTOR = 1.35f;	// = 0.27*5
	public static final float IMG_Y_FACTOR = 1.62f;	// = 0.27*6
	public static final int IMG_FACE_WIDTH = 95;	// = 19*5
	public static final int IMG_FACE_HEIGHT = 114;	// = 19*6 
}
