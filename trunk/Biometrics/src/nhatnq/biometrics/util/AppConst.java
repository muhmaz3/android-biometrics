package nhatnq.biometrics.util;

import android.os.Environment;

public class AppConst {
	/**
	 * Folder hierachy: 
	 * /mnt/sdcard/biometrics: contains .jpg(face image), .amr(voice)
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
	
	//Ratio: 5:6 ~ width:height of image
//	public static final float IMG_X_FACTOR = 1.15f;	// = 0.23*5 (tested 1.35)
//	public static final float IMG_Y_FACTOR = 1.38f;	// = 0.23*6 (tested 1.62)
//	public static final int IMG_FACE_WIDTH = 95;	// = 19*5
//	public static final int IMG_FACE_HEIGHT = 114;	// = 19*6
	
	public static final float IMG_X_FACTOR = 1.19f;	// = 0.17*7
	public static final float IMG_Y_FACTOR = 1.36f;	// = 0.17*8
	public static final int IMG_FACE_WIDTH = 98;	// = 14*7
	public static final int IMG_FACE_HEIGHT = 112;	// = 14*8 
}
