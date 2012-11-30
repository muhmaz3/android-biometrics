package android.biometrics;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class ScreenWelcome extends Activity {
	private static final String TAG = ScreenWelcome.class.getCanonicalName();
	private static final int DEFAULT_SPLASH_TIME = 2000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_welcome);
		
//		ImageView iv;
//		iv = (ImageView) findViewById(R.id.logo_face);
//		iv.setOnClickListener(OnClickHandler);
//		iv = (ImageView) findViewById(R.id.logo_voice);
//		iv.setOnClickListener(OnClickHandler);
		
		Timer timer = new Timer();
		timer.schedule(task, DEFAULT_SPLASH_TIME);
	}
	
	View.OnClickListener OnClickHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = null;
			switch (v.getId()) {
			case R.id.logo_face:
				intent = new Intent(ScreenWelcome.this, ScreenFaceTraining.class);
				break;
			case R.id.logo_voice:
				intent = new Intent(ScreenWelcome.this, ScreenVoiceTraining.class);
				break;
			}
			
			startActivity(intent);
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 7 && resultCode == RESULT_OK){
			chooseNextScreen();
		}
	};
	
	private TimerTask task = new TimerTask() {
		
		@Override
		public void run() {
			chooseNextScreen();
		}
	};
	
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
		finish();
	}
}
