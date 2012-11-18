package nhatnq.biometrics.voice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nhatnq.biometrics.util.AppUtil;
import nhatnq.biometrics.util.FileHelper;
import thesis.lib.comirva.AudioFeatureExtractor;
import thesis.lib.comirva.TimbreDistributionExtractor;
import thesis.lib.comirva.audio.AudioFeature;
import thesis.lib.sound.sampled.AudioInputStream;
import thesis.lib.sound.sampled.AudioSystem;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class VoiceTrainer {
	
	static ArrayList<AudioFeatureExtractor> arrayFeatureExtractionTrainSet = new ArrayList<AudioFeatureExtractor>();
	private Context mBase;
	AudioFeatureExtractor featureExtractor = new TimbreDistributionExtractor();
	
	public VoiceTrainer(Context context){
		mBase = context;
	}
	/**
	 * Start new thread to process training voice
	 * @param fileNamePath list voice record
	 * @return 
	 */
	public void saveVoiceData(List<String> fileNamePath)
	{
		new TrainingTask(fileNamePath).execute();
	}
	private class TrainingTask extends AsyncTask<String, Integer, Boolean>
	{
		List<String> Voice;
		private ProgressDialog dialog;
    	
		public TrainingTask(List<String> list){
			Voice = list;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(mBase);
			dialog.setMessage("Processing...");
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			File[] trainingSet = new File[Voice.size()]; 
			for(int i = 0 ; i< Voice.size(); i++){
				File temp = new File(Voice.get(i));
				Log.i("size", Voice.get(i));
				trainingSet[i] = temp;
			}
			
			for(int i = 0; i < trainingSet.length; i++)
		    {
		       try
		       {
		    	 //open audio file as stream
		         File is = trainingSet[i];
		         
		         //extract the feature from the audio file
		         AudioInputStream in = AudioSystem.getAudioInputStream(is);
		         
		         AudioFeature audioFeature = (AudioFeature) featureExtractor.calculate(in);
		         Log.i("size", "lengKmean " + featureExtractor.getKmean().getDimension());
		         FileHelper.WriteTrainingSetToFile(featureExtractor);
		         in.close();
		         arrayFeatureExtractionTrainSet.add(featureExtractor);
		       }
		       catch (Exception e)
		       {
		         System.out.println("An error occurred while processing:'" + trainingSet[i].getAbsolutePath() + "';");
		         e.printStackTrace();
		       }
		    }
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if(values[0] == 1){
				dialog.setMessage("Saving...");
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			
			//Clear voice sample in SD card
			AppUtil.clearTrainingSampleFromSdcard(false);
			dialog.dismiss();
		}
	}
}
