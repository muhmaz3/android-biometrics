package thesis.biometrics;

import java.util.ArrayList;

import thesis.biometrics.util.FileHelper;
import thesis.biometrics.voice.VoiceRecognizer;
import thesis.lib.comirva.audio.PointList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class ScreenVoiceRecognizing extends Activity {
	
	private ImageView imageView;
	private static final int REQ_RECORD_VOICE = 7;
	public static ArrayList<PointList> trainingSet;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_voice_recognizing);
		
		new LoadTrainFile().execute();
		
		imageView = (ImageView)findViewById(R.id.btnRecord);
		imageView.setOnClickListener(OnClickButtonHandler);
		imageView = (ImageView)findViewById(R.id.btnRecognize);
		imageView.setOnClickListener(OnClickButtonHandler);
		
		
	}
	/*
	 * Handle click bottom Image
	 * 
	 */
View.OnClickListener OnClickButtonHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnRecord:
				/**
				 * TODO You have to show another screen or design a small layout (in center space) 
				 * for recording voice
				 */
				if (trainingSet.size() == 0) {
					Toast.makeText(ScreenVoiceRecognizing.this, "Please training before recognizing!", Toast.LENGTH_SHORT).show();
				}
				Intent intent = new Intent(ScreenVoiceRecognizing.this, ScreenVoiceRecording.class);
				startActivityForResult(intent, REQ_RECORD_VOICE);
				
				break;
			case R.id.btnRecognize:
				/*
				 * TODO If you want
				 */
				break;
			}
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(requestCode == REQ_RECORD_VOICE && resultCode==RESULT_OK){
    		String newVoicePath = data.getExtras().getString("path");
//    		String newVoiceDuration = data.getExtras().getString("duration");
    		
    		VoiceRecognizer recognizer = new VoiceRecognizer(ScreenVoiceRecognizing.this);
    		recognizer.startRecognizing(newVoicePath);
    		
    	}
	}
	
	private class LoadTrainFile extends AsyncTask<String, Integer, Boolean>
	{
		private ProgressDialog dialog;
    			
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(ScreenVoiceRecognizing.this);
			dialog.setMessage("Loading training file...");
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			
			trainingSet = FileHelper.ReadFileTrainToPointList();
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if(values[0] == 1){
				dialog.setMessage("Loading...");
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			Toast.makeText(ScreenVoiceRecognizing.this, "Loading finish!", Toast.LENGTH_SHORT).show();
			dialog.dismiss();
		}
	}

}
