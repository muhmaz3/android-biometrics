package android.biometrics;

import android.app.Activity;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ScreenWelcome extends Activity {
//	private static final String TAG = ScreenWelcome.class.getCanonicalName();
//	private static final int DEFAULT_SPLASH_TIME = 2000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_welcome_new); 
		ImageView img_Setting, img_Train, img_Recognize;
		
		img_Setting = (ImageView)findViewById(R.id.btn_main_setting);
		img_Setting.setOnClickListener(OnClickHandler);
		
		img_Recognize = (ImageView)findViewById(R.id.btn_main_Recognize);
		img_Recognize.setOnClickListener(OnClickHandler);

		img_Train =(ImageView) findViewById(R.id.btn_main_train);
		img_Train.setOnClickListener(OnClickHandler);
		
		// Check training set is exists?
		if(AppUtil.isFaceVoiceTrained(this)){
			img_Train.setVisibility(View.GONE);
			img_Recognize.setVisibility(View.VISIBLE);
		}else{
			img_Train.setVisibility(View.VISIBLE);
//			img_Recognize.setVisibility(View.GONE);
		}
	}

	View.OnClickListener OnClickHandler = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = null;
			int recogMode = AppUtil.getRecognitionMode(getApplicationContext());
			switch (v.getId()) {
			case R.id.btn_main_train:
				intent = new Intent(ScreenWelcome.this, ScreenVoiceTraining.class);
				break;
			case R.id.btn_main_Recognize:
				switch (recogMode) {
				case AppConst.RECOGNITION_MODE_JUST_FACE:
					intent = new Intent(ScreenWelcome.this, ScreenFaceRecognizing.class);
					break;
				case AppConst.RECOGNITION_MODE_JUST_VOICE:
					intent = new Intent(ScreenWelcome.this, ScreenVoiceRecognizing.class);
					break;
				case AppConst.RECOGNITION_MODE_FACE_FIRST:
					intent = new Intent(ScreenWelcome.this, ScreenFaceRecognizing.class);
					break;
				case AppConst.RECOGNITION_MODE_VOICE_FIRST:
					intent = new Intent(ScreenWelcome.this, ScreenVoiceRecognizing.class);
					break;
				case AppConst.RECOGNITION_MODE_BOTH:
					intent = new Intent(ScreenWelcome.this, ScreenFaceRecognizing.class);
					break;

				default:
					break;
				}				
				break;
			case R.id.btn_main_setting:
				intent = new Intent(ScreenWelcome.this, ScreenSettings.class);
				break;
			}
			
			startActivity(intent);
		}
	};
	
	/*protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 7 && resultCode == RESULT_OK){
			chooseNextScreen();
		}
	};*/
	
	/*private TimerTask task = new TimerTask() {
		
		@Override
		public void run() {
			chooseNextScreen();
		}
	};*/
	/*
	private void chooseNextScreen(){
		Intent intent = null;
		if(Biometrics.isFirstTime){
			intent = new Intent(ScreenWelcome.this, ScreenSettings.class);
			startActivityForResult(intent, 7);
			return;
		}
		int recogMode = AppUtil.getRecognitionMode(getApplicationContext());
		boolean isFaceTrainned = AppUtil.isTrained(getApplicationContext(), 
				AppConst.KEY_FACE_TRAINED);
		boolean isVoiceTrainned = AppUtil.isTrained(getApplicationContext(), 
				AppConst.KEY_VOICE_TRAINED);
		
		switch (recogMode) {
		case AppConst.RECOGNITION_MODE_JUST_FACE:
			Log.e(TAG, "...case RECOGNITION_MODE_JUST_FACE");
			if(isFaceTrainned &&
					((new File(AppConst.FACE_DATA_FILE_PATH)).exists())){
				intent = new Intent(ScreenWelcome.this, ScreenFaceRecognizing.class);
			}else{
				intent = new Intent(ScreenWelcome.this, ScreenFaceTraining.class);
			}
			break;
		case AppConst.RECOGNITION_MODE_JUST_VOICE:
			Log.e(TAG, "...case RECOGNITION_MODE_JUST_VOICE");
			if(isVoiceTrainned &&
					(new File(AppConst.VOICE_DATA_FILE_PATH)).exists()){
				intent = new Intent(ScreenWelcome.this, ScreenVoiceRecognizing.class);
			}else{
				intent = new Intent(ScreenWelcome.this, ScreenVoiceTraining.class);
			}
			break;
		case AppConst.RECOGNITION_MODE_FACE_FIRST:
			Log.e(TAG, "...case RECOGNITION_MODE_FACE_FIRST");
			if(isFaceTrainned && isVoiceTrainned &&
					(new File(AppConst.FACE_DATA_FILE_PATH)).exists() &&
					(new File(AppConst.VOICE_DATA_FILE_PATH)).exists()){
				intent = new Intent(ScreenWelcome.this, ScreenFaceRecognizing.class);
			}else{
				intent = new Intent(ScreenWelcome.this, ScreenFaceTraining.class);
			}
		case AppConst.RECOGNITION_MODE_VOICE_FIRST:
			Log.e(TAG, "...case RECOGNITION_MODE_VOICE_FIRST");
			if(isFaceTrainned && isVoiceTrainned &&
					(new File(AppConst.FACE_DATA_FILE_PATH)).exists() &&
					(new File(AppConst.VOICE_DATA_FILE_PATH)).exists()){
				intent = new Intent(ScreenWelcome.this, ScreenVoiceRecognizing.class);
			}else{
				intent = new Intent(ScreenWelcome.this, ScreenVoiceTraining.class);
			}
		case AppConst.RECOGNITION_MODE_BOTH:
			Log.e(TAG, "...case RECOGNITION_MODE_BOTH");
			if(isFaceTrainned && isVoiceTrainned &&
					((new File(AppConst.FACE_DATA_FILE_PATH)).exists()) &&
					((new File(AppConst.VOICE_DATA_FILE_PATH)).exists())){
				intent = new Intent(ScreenWelcome.this, ScreenFaceRecognizing.class);
			}else{
				intent = new Intent(ScreenWelcome.this, ScreenFaceTraining.class);
			}
			break;
		}

		startActivity(intent);
//		finish();
	}
	*/
}
