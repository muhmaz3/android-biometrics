package android.biometrics;

import java.util.ArrayList;

import lib.comirva.audio.PointList;
import android.app.Activity;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.biometrics.voice.VoiceRecognizer;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ScreenVoiceRecognizing extends Activity {
	
	private ArrayList<PointList> trainingSet;
	private String voicePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_voice_recognizing);

		Button imageView;
		imageView = (Button) findViewById(R.id.btnRecord);
		imageView.setOnClickListener(OnClickButtonHandler);
		imageView = (Button) findViewById(R.id.btnRecognize);
		imageView.setOnClickListener(OnClickButtonHandler);
		
//		new LoadTrainFile().execute();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST, Menu.NONE, "View lastest distance");
		return true;
	};
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			float voiceDistance = AppUtil.getPreference(getApplicationContext(), 
					AppConst.CONFIDENT_VOICE_CALCULATED);
			if(voiceDistance != AppConst.DEFAULT_VOICE_THRESHOLD){
				Toast.makeText(getBaseContext(), "Lastest distance: "+voiceDistance, 
						Toast.LENGTH_LONG).show();
			}
			break;
		}
		return true;
	};

	View.OnClickListener OnClickButtonHandler = new View.OnClickListener() {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnRecord:
				if (!AppUtil.isVoiceTrained(ScreenVoiceRecognizing.this)) {
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
		//TODO Remove this, load training set now more to VoiceRecognizer
//		recognizer.setTrainingSet(trainingSet);
		recognizer.recognize(voicePath);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == AppConst.REQ_RECORD_VOICE && resultCode == RESULT_OK) {
			voicePath = data.getExtras().getString("path");
			
			//Just start recognizing when pressing button
//			recognize();
		}else{
//			Toast.makeText(this, getString(R.string.recording_fail),Toast.LENGTH_SHORT ).show();
			voicePath = null;
		}
//		if (requestCode == AppConst.REQ_RECOGNIZE_FACE && resultCode == RESULT_OK) {
//			this.finish();
//		}	
	}

	// TODO Delete this task also !
	/*private class LoadTrainFile extends AsyncTask<String, Integer, Boolean> {
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
			*//** TODO Internal/external storage
			 * Now we read voice data from internal storage
			 *//*
//			trainingSet = VoiceHelper.readVoiceDataToPointList();
			trainingSet = VoiceHelper.readVoiceDataToPointList(getBaseContext());
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
			Log.i("size", "threshold " + AppUtil.getPreference(ScreenVoiceRecognizing.this, AppConst.VOICE_THRESHOLD));
		}
	}*/

}
