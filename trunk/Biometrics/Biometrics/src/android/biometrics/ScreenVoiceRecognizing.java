package android.biometrics;

import java.util.ArrayList;

import lib.comirva.audio.PointList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.biometrics.voice.VoiceHelper;
import android.biometrics.voice.VoiceRecognizer;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class ScreenVoiceRecognizing extends Activity {

	private ArrayList<PointList> trainingSet;
	private String voicePath;
	int mode;
	public static int threshold;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_voice_recognizing);
		
		mode = AppUtil.getRecognitionMode(getApplicationContext());
			
		
		ImageView imageView;
		imageView = (ImageView) findViewById(R.id.btnRecord);
		imageView.setOnClickListener(OnClickButtonHandler);
		imageView = (ImageView) findViewById(R.id.btnRecognize);
		imageView.setOnClickListener(OnClickButtonHandler);
		
		new LoadTrainFile().execute();
	}

	View.OnClickListener OnClickButtonHandler = new View.OnClickListener() {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnRecord:
				if (trainingSet == null || trainingSet.size() == 0) {
					Toast.makeText(ScreenVoiceRecognizing.this, 
							getString(R.string.not_train_yet), 
							Toast.LENGTH_LONG).show();
					return;
				}				
				Intent intent = new Intent(ScreenVoiceRecognizing.this,
						ScreenVoiceRecording.class);
				startActivityForResult(intent, AppConst.REQ_RECORD_VOICE);

				break;
			case R.id.btnRecognize:
				if(voicePath == null){
					Toast.makeText(ScreenVoiceRecognizing.this, 
							getString(R.string.toast_please_record_your_voice), 
							Toast.LENGTH_LONG).show();
					return;
				}
				
				recognize();
				break;
			}
		}
	};

	private void recognize(){
		VoiceRecognizer recognizer = new VoiceRecognizer(ScreenVoiceRecognizing.this);
		threshold = getConfidenceThreshold();
		recognizer.setTrainingSet(trainingSet);
		recognizer.recognize(voicePath);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == AppConst.REQ_RECORD_VOICE && resultCode == RESULT_OK) {
			voicePath = data.getExtras().getString("path");
			recognize();
		}else{
			Toast.makeText(this, getString(R.string.recording_fail),Toast.LENGTH_SHORT ).show();
			voicePath = null;
		}
		if (requestCode == AppConst.REQ_RECOGNIZE_FACE && resultCode == RESULT_OK) {
			this.finish();
		}	
	}

	private class LoadTrainFile extends AsyncTask<String, Integer, Boolean> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(ScreenVoiceRecognizing.this);
			dialog.setMessage(getString(R.string.txt_loading));
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			trainingSet = VoiceHelper.readVoiceDataToPointList();
			if (trainingSet == null)
				return false;
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
			if(!result){
				Toast.makeText(ScreenVoiceRecognizing.this, R.string.txt_load_voice_train_fail, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private int getConfidenceThreshold(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		return Integer.parseInt(pref.getString(
				getString(R.string.pref_voice_distance_threshold_key), 
				""+AppConst.DEFAULT_VOICE_THRESHOLD));
	}

}
