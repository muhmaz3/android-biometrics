package nhatnq.biometrics.voice;

import java.io.IOException;
import java.util.Calendar;

import nhatnq.biometrics.util.AppConst;
import android.media.MediaRecorder;
import android.text.format.DateFormat;

public class VoiceHelper {
	private static final String TAG = VoiceHelper.class.getCanonicalName();
	public static final String VOICE_EXTENSION = ".3gp";
	private MediaRecorder mVoiceRecorder;
	private String mVoiceFile;
	
	/**
	 * Start capturing voice file using system recorder
	 * More details: http://developer.android.com/guide/topics/media/audio-capture.html
	 * @return
	 */
	public void startVoiceRecorder(){
		String outputAudio = 
				AppConst.APP_FOLDER + "/"+generateAudioNameAtThisTime();
		mVoiceRecorder = new MediaRecorder();
		mVoiceRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mVoiceRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mVoiceRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mVoiceRecorder.setOutputFile(outputAudio);
	    try {
	    	mVoiceRecorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    mVoiceRecorder.start();	    
	    
	    mVoiceFile = outputAudio;
	}
	
	public String stopVoiceRecoder(){
		if(mVoiceRecorder != null){
			mVoiceRecorder.stop();
			mVoiceRecorder.reset();
			
			return mVoiceFile;
		}else return null;
	}
	
	public void releaseVoiceRecorder(){
		if(mVoiceRecorder != null){
			mVoiceRecorder.release();
			mVoiceRecorder = null;
		}
	}
	
	/**
	 * Generate new file name of audio file
	 * @return Name of audio file, current extension is .AMR
	 */
	private static String generateAudioNameAtThisTime(){
		return "AUD_"+DateFormat.format("yyyyMMdd_kkmmss", Calendar.getInstance()).toString()
				+VOICE_EXTENSION;
	}
}
