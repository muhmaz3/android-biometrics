package android.biometrics;

import java.util.ArrayList;

import lib.comirva.audio.PointList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.biometrics.voice.VoiceHelper;
import android.biometrics.voice.VoiceRecognizer;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class ScreenVoiceRecognizing extends Activity {

	private static final int REQ_RECORD_VOICE = 7;
	private ArrayList<PointList> trainingSet;
	private String voicePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_voice_recognizing);

		ImageView imageView;
		imageView = (ImageView) findViewById(R.id.btnRecord);
		imageView.setOnClickListener(OnClickButtonHandler);
		imageView = (ImageView) findViewById(R.id.btnRecognize);
		imageView.setOnClickListener(OnClickButtonHandler);
		
		new LoadTrainFile().execute();
	}

	View.OnClickListener OnClickButtonHandler = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnRecord:
				if (trainingSet == null || trainingSet.size() == 0) {
					Toast.makeText(ScreenVoiceRecognizing.this, 
							getString(R.string.not_train_yet), 
							Toast.LENGTH_LONG).show();
					return;
				}
//				if(! AppUtil.isTrained(ScreenVoiceRecognizing.this, AppConst.KEY_VOICE_TRAINED)){
//					Toast.makeText(ScreenVoiceRecognizing.this, 
//							getString(R.string.not_train_yet), 
//							Toast.LENGTH_LONG).show();
//					return;
//				}
				
				Intent intent = new Intent(ScreenVoiceRecognizing.this,
						ScreenVoiceRecording.class);
				startActivityForResult(intent, REQ_RECORD_VOICE);

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
		recognizer.setTrainingSet(trainingSet);
		recognizer.recognize(voicePath);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_RECORD_VOICE && resultCode == RESULT_OK) {
			voicePath = data.getExtras().getString("path");
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
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
		}
	}

}
