package android.biometrics.voice;

import java.io.File;
import java.util.ArrayList;

import lib.comirva.AudioFeatureExtraction;
import lib.comirva.TimbreDistributionExtractor;
import lib.comirva.audio.PointList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.biometrics.R;
import android.biometrics.util.AppConst;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class VoiceRecognizer {
	private static final String TAG = VoiceRecognizer.class.getCanonicalName();
	private Activity context;
	private ArrayList<PointList> trainingSet;
	
	public VoiceRecognizer(Activity base){
		this.context = base;
	}
	
	public void setTrainingSet(ArrayList<PointList> trainingSet){
		this.trainingSet = trainingSet;
	}
	
	public void recognize(String voiceFilePath){
		new VoiceRegconizingTask().execute(voiceFilePath);
	}
	
	private class VoiceRegconizingTask extends AsyncTask<String, Integer, Boolean>{
		private ProgressDialog dialog;
		private int distanceThreshold;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(context);
			dialog.setMessage(context.getString(R.string.txt_recognizing));
			dialog.show();
			
			distanceThreshold = getDistanceThreshold();
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			String voiceFile = params[0];
			File[] voiceInput = {new File(voiceFile)};
			AudioFeatureExtraction tdet1 = new AudioFeatureExtraction(
					trainingSet,new TimbreDistributionExtractor(),  voiceInput);
			
			double distance = tdet1.run();
			Log.e(TAG, "Distance result is "+ distance);
			
			if(distance < distanceThreshold) 
				return true;
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();

			Toast toast = Toast.makeText(context, null, Toast.LENGTH_LONG);
			LayoutInflater inflater = LayoutInflater.from(context);
			View view = inflater.inflate(R.layout.view_toast_result, null);
			TextView tv = (TextView)view.findViewById(R.id.text);
			if(result){
				view.setBackgroundColor(Color.rgb(0, 221, 119));
				tv.setText(context.getString(R.string.recognize_success));
			}else{
				view.setBackgroundColor(Color.rgb(221, 0, 0));
				tv.setText(context.getString(R.string.recognize_failed));
			}
			
			toast.setView(view);
			toast.show();
		}
		
		private int getDistanceThreshold(){
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			return Integer.parseInt(pref.getString(
					context.getString(R.string.pref_voice_distance_threshold_key), 
					String.valueOf(AppConst.DEFAULT_VOICE_THRESHOLD)));
		}
	}
}
