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
	public static final String VOICE_DATA_FILE_PATH = VOICE_FOLDER + "/" + "voice_data.txt";
	public static final String VOICE_DATA_FILE_NAME = "voice_data.txt";
	public static final String FACE_DATA_FILE_NAME = "face_data.xml";
	
	public static final float IMG_X_FACTOR = 1.19f;	// = 0.17*7
	public static final float IMG_Y_FACTOR = 1.36f;	// = 0.17*8
	public static final int IMG_FACE_WIDTH = 98;	// = 14*7
	public static final int IMG_FACE_HEIGHT = 112;	// = 14*8 
	
//	public static final String KEY_FACE_TRAINED = "face_trained";
//	public static final String KEY_VOICE_TRAINED = "voice_trained";

	public static final String KEY_RECOGNITION_MODE = "recognition_mode";
	public static final int RECOGNITION_MODE_JUST_FACE = 0;
	public static final int RECOGNITION_MODE_JUST_VOICE = 1;
	public static final int RECOGNITION_MODE_FACE_FIRST = 2;
	public static final int RECOGNITION_MODE_VOICE_FIRST = 3;
	public static final int RECOGNITION_MODE_BOTH = 4;
	
	public static final float DEFAULT_VOICE_THRESHOLD = 600.0f;
	public static final float DEFAULT_FACE_THRESHOLD = 0.7f;
	public static final String VOICE_THRESHOLD = "Voice_threshold";
	public static final float VOICE_INCREASE_THRESHOLD_VALUE = 50.0f;
	public static final int DEFAULT_MIN_VOICE_RECORD = 7; //7 seconds
	
	public static final String CONFIDENT_FACE_CALCULATED = "Face Confident calculated";
	public static final String CONFIDENT_VOICE_CALCULATED = "Voice Confident calculated";

	public static final int MIN_FACE_IMAGE_CAPTURED	= 2;
	
	public static final int MIN_VOICE_SAMPLE = 2;
	public static final int REQ_RECORD_VOICE = 1;
	public static final int REQ_CAMERA_CAPTURE = 2;
	public static final int REQ_TRAIN_VOICE = 3;
	public static final int REQ_RECOGNIZE_VOICE = 4;
	public static final int REQ_RECOGNIZE_FACE = 5;
}
