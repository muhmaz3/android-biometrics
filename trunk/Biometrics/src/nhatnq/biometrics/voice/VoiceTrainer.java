package nhatnq.biometrics.voice;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class VoiceTrainer {
	
	private Activity mBase;
	
	public VoiceTrainer(Activity base){
		this.mBase = base;
	}
	
	public void startTraining(List<String> voicePaths){
		new VoiceTrainingTask(voicePaths).execute();
	}
	
	private class VoiceTrainingTask extends AsyncTask<String, Integer, Boolean>{
		private ProgressDialog dialog;
		private List<String> voicePaths;
		
		public VoiceTrainingTask(List<String> list){
			this.voicePaths = list;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(mBase);
			dialog.setMessage("Processing...");
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			
			for(String path : voicePaths){
				//TODO Working with every voice file here
			}
			
			/**
			 * Before saving data, call this to notify user that you are saving data
			 */
			publishProgress(1);
			
			//TODO Put your code to saving data here
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if(values[0] == 1){
				dialog.setMessage("Saving...");
			}
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
