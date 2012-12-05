package android.biometrics.voice;

import java.io.File;
import java.util.ArrayList;

import lib.comirva.AudioFeatureExtraction;
import lib.comirva.TimbreDistributionExtractor;
import lib.comirva.audio.PointList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.biometrics.R;
import android.biometrics.ScreenFaceRecognizing;
import android.biometrics.ScreenVoiceRecognizing;
import android.biometrics.combination.BiometricsCombinator;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class VoiceRecognizer {
	private static final String TAG = VoiceRecognizer.class.getCanonicalName();
	private Activity context;
	private ArrayList<PointList> trainingSet;
	float resultConfident;
	
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
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(context);
			dialog.setMessage(context.getString(R.string.txt_recognizing));
			dialog.setCancelable(false);
			dialog.show();		
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			String voiceFile = params[0];
			File[] voiceInput = {new File(voiceFile)};
			
			// Load training data
			trainingSet = VoiceHelper.readVoiceDataToPointList(context);
			AudioFeatureExtraction tdet1 = new AudioFeatureExtraction(
					trainingSet,new TimbreDistributionExtractor(),  voiceInput);
			
			resultConfident = (float) tdet1.run();
			Log.e(TAG, "Distance result is "+ resultConfident);
			
			return true;		
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
			Toast toast = Toast.makeText(context, null, Toast.LENGTH_LONG);
			LayoutInflater inflater = LayoutInflater.from(context);
			View view = inflater.inflate(R.layout.view_toast_result, null);
			TextView tv = (TextView)view.findViewById(R.id.text);
			
			int mode = AppUtil.getRecognitionMode(context.getApplicationContext());	
			switch (mode) {
			case AppConst.RECOGNITION_MODE_BOTH:
				// TODO continue code to combine 2 threshold for recognize
				float faceConfidenceVal = AppUtil.getPreference(context.getApplicationContext(), 
						AppConst.CONFIDENT_FACE_CALCULATED);
				boolean okay = BiometricsCombinator.combine(faceConfidenceVal, resultConfident);
				if(okay){
					view.setBackgroundColor(Color.rgb(0, 221, 119));
					tv.setText(context.getString(R.string.recognize_success));
				}else{
					view.setBackgroundColor(Color.rgb(221, 0, 0));
					tv.setText(context.getString(R.string.recognize_failed));
				}
				break;
			
			case AppConst.RECOGNITION_MODE_FACE_FIRST:				
			case AppConst.RECOGNITION_MODE_JUST_VOICE:
				
				//TODO Get threshold
				int threshold = 0;
				
				if(resultConfident < threshold){
					view.setBackgroundColor(Color.rgb(0, 221, 119));
					tv.setText(context.getString(R.string.recognize_success));
				}else{
					view.setBackgroundColor(Color.rgb(221, 0, 0));
					tv.setText(context.getString(R.string.recognize_failed));
				}
				break;
			case AppConst.RECOGNITION_MODE_VOICE_FIRST:
				if(resultConfident < ScreenVoiceRecognizing.threshold){
					AppUtil.savePreference(context.getApplicationContext(),AppConst.CONFIDENT_FACE_CALCULATED, resultConfident);
					Intent intent = new Intent(context, ScreenFaceRecognizing.class);
					context.startActivity(intent);
				}else{
					view.setBackgroundColor(Color.rgb(221, 0, 0));
					tv.setText(context.getString(R.string.recognize_failed));
				}				
				break;
			default:
				break;
			}

			toast.setView(view);
			toast.show();
			context.finish();
		}
		
	}
}
