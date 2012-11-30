package android.biometrics.voice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lib.comirva.AudioFeatureExtractor;
import lib.comirva.TimbreDistributionExtractor;
import lib.comirva.audio.AudioFeature;
import lib.sound.sampled.AudioInputStream;
import lib.sound.sampled.AudioSystem;
import android.app.Activity;
import android.app.ProgressDialog;
import android.biometrics.R;
import android.biometrics.ScreenFaceTraining;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class VoiceTrainer {
	private static final String TAG = VoiceTrainer.class.getCanonicalName();
//	private List<AudioFeatureExtractor> arrayFeatureExtractionTrainSet = 
//			new ArrayList<AudioFeatureExtractor>();
	private Activity mBase;
	private AudioFeatureExtractor featureExtractor = new TimbreDistributionExtractor();

	public VoiceTrainer(Activity context) {
		mBase = context;
	}

	public void train(List<String> fileNamePath) {
		new TrainingTask(fileNamePath).execute();
	}

	private class TrainingTask extends AsyncTask<String, Integer, Boolean> {
		List<String> voicePaths;
		private ProgressDialog dialog;

		public TrainingTask(List<String> list) {
			voicePaths = list;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(mBase);
			dialog.setMessage(mBase.getString(R.string.txt_processing));
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(String... arg0) {
			int size = voicePaths.size();
			List<File> trainingSet = new ArrayList<File>();
			for (int i = 0; i < size; i++) {
				File temp = new File(voicePaths.get(i));
				if (!temp.exists())
					continue;

				Log.i(TAG, "Voice path:" + temp.getAbsolutePath());
				trainingSet.add(temp);
			}

			AudioInputStream ais;
			for (File file : trainingSet) {
				try {
					// extract the feature from the audio file
					ais = AudioSystem.getAudioInputStream(file);

					 AudioFeature audioFeature = (AudioFeature)
							 featureExtractor.calculate(ais);
					Log.i(TAG, "Working at " + file.getAbsolutePath());
					Log.i(TAG, "Kmean::dimension: "
							+ featureExtractor.getKmean().getDimension());
					VoiceHelper.writeTrainingSetToFile(featureExtractor);
					ais.close();

					// TODO static field or not, depends on
//					arrayFeatureExtractionTrainSet.add(featureExtractor);
				} catch (Exception e) {
					Log.e(TAG,
							"Problem while processing:"+ file.getAbsolutePath());
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (values[0] == 1) {
				dialog.setMessage(mBase.getString(R.string.txt_saving));
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
			if(!result){
				Toast.makeText(mBase, mBase.getString(R.string.train_failed), 
						Toast.LENGTH_LONG).show();
			}else{
				AppUtil.savePreference(mBase, AppConst.KEY_VOICE_TRAINED, true);
			}
			
			int recogMode = AppUtil.getRecognitionMode(mBase);
			switch (recogMode) {
			case AppConst.RECOGNITION_MODE_VOICE_FIRST:
				Intent intent = new Intent(mBase, ScreenFaceTraining.class);
				mBase.startActivity(intent);
				break;
			}
			
			mBase.finish();
		}
	}
}
