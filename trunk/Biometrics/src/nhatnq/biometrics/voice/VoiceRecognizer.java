package nhatnq.biometrics.voice;

import java.io.File;

import nhatnq.biometrics.ScreenVoiceRecognizing;

import thesis.lib.comirva.AudioFeatureExtraction;
import thesis.lib.comirva.TimbreDistributionExtractor;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class VoiceRecognizer {

	private Activity context;
	
	public VoiceRecognizer(Activity base){
		this.context = base;
	}
	
	public void startRecognizing(String voiceFilePath){
		new VoiceRegconizingTask().execute(voiceFilePath);
	}
	
	private class VoiceRegconizingTask extends AsyncTask<String, Integer, Boolean>{
		private ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(context);
			dialog.setMessage("Recognizing...");
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			//TODO Do your work here

			String voiceFile = params[0];
			boolean verify = false;
			File[] voiceInput = {new File(voiceFile)};
			AudioFeatureExtraction tdet1 = new AudioFeatureExtraction(ScreenVoiceRecognizing.trainingSet,new TimbreDistributionExtractor(),  voiceInput);
			double distance = tdet1.run();
			Log.i("size", "result test "+ distance);
			if(distance < 600){
				verify = true;
			}
			return verify;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
			
			//TODO Add your result solution 
			if(result){
				Toast.makeText(context, "recognition success", Toast.LENGTH_SHORT).show();
			}else
				Toast.makeText(context, "recognition fail", Toast.LENGTH_SHORT).show();
			
		}
	}
}
