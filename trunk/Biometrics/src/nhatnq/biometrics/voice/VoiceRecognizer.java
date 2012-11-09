package nhatnq.biometrics.voice;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class VoiceRecognizer {

	private Activity mBase;
	
	public VoiceRecognizer(Activity base){
		this.mBase = base;
	}
	
	public void startRecognizing(String voiceFilePath){
		new VoiceRegconizingTask().execute(voiceFilePath);
	}
	
	private class VoiceRegconizingTask extends AsyncTask<String, Integer, Boolean>{
		private ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(mBase);
			dialog.setMessage("Processing...");
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			//TODO Do your work here
			String voiceFile = params[0];
			return null;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
			
			//TODO Add your result solution 
			if(result == null){
				
			}else if(result){
				
			}else{
				
			}
		}
	}
}
