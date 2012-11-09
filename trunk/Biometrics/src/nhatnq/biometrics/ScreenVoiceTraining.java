package nhatnq.biometrics;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nhatnq.biometrics.face.FaceHelper;
import nhatnq.biometrics.face.FaceTrainer;
import nhatnq.biometrics.util.AppConst;
import nhatnq.biometrics.util.AppUtil;
import nhatnq.biometrics.voice.VoiceHelper;
import nhatnq.biometrics.voice.VoiceTrainer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

public class ScreenVoiceTraining extends Activity {

	private static final String TAG = ScreenVoiceTraining.class.getCanonicalName();
	private static final int MIN_VOICE_SAMPLE = 2;
	private static final int REQ_VOICE_RECORDER = 8;
	
	private Gallery mGallery;
	private GalleryApdapter mAdapter;
	private List<String> mVoicePaths;
	private MediaPlayer mPlayer;
	private VoiceHelper mVoiceHelper;
	
	/**
	 * Change screen layout if you want, it depends on you.
	 * Notice that, this is application for algorithm testing, 
	 * not real application, so, you need to supply useful UI item
	 * for viewing easier.
	 */
	/**
	 * This is not okay, I've just help you some code
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_voice_training);
		
		ImageView bt;
		bt = (ImageView) findViewById(R.id.btnRecord);
		bt.setOnClickListener(OnClickButtonHandler);
		bt = (ImageView) findViewById(R.id.btnSave);
		bt.setOnClickListener(OnClickButtonHandler);
		
		bt = (ImageView) findViewById(R.id.btnSettings);
		bt.setOnClickListener(OnClickButtonHandler);
		bt = (ImageView) findViewById(R.id.btnModeRecognizing);
		bt.setOnClickListener(OnClickButtonHandler);
		
		mGallery = (Gallery) findViewById(R.id.gallery);
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int pos,
					long id) {
				String src = (String)adapter.getItemAtPosition(pos);
				//TODO Show an audio player in center space
//				playVoiceSample(src);
			}
			
		});
		
		fetchLocalVoices();
    	mAdapter = new GalleryApdapter(this);
		mGallery.setAdapter(mAdapter);
	}
	    
    View.OnClickListener OnClickButtonHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnRecord:
				/**
				 * TODO You have to show another screen or design a small layout (in center space) 
				 * for recording voice
				 */
//				recordVoice();
				break;
			case R.id.btnSave:
				saveVoice();
				break;
			case R.id.btnSettings:
				Intent intent = new Intent(ScreenVoiceTraining.this, ScreenSettings.class);
				startActivity(intent);
				break;
			case R.id.btnModeRecognizing:
				Intent intent1 = new Intent(ScreenVoiceTraining.this, ScreenVoiceRecognizing.class);
				startActivity(intent1);
				break;
			}
		}
	};
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST, Menu.NONE, "Clear training voice files");
		return true;
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			AppUtil.clearTrainingSampleFromSdcard(true);
			mVoicePaths.clear();
			mAdapter.notifyDataSetChanged();
			break;
		}
		return true;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(mPlayer != null){
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	private class GalleryApdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		
		public GalleryApdapter(Context context){
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return mVoicePaths.size();
		}

		@Override
		public String getItem(int arg0) {
			return mVoicePaths.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.view_item_gallery, null);
			}

			String src = getItem(pos);
//			Bitmap bm = processBitmap4Display(src);
			Bitmap bm = BitmapFactory.decodeFile(src);
			bm = Bitmap.createScaledBitmap(bm, 64, 64, false);
			if(bm != null){
//    			AppUtil.loadBitmapFromPath(extraImagePath, mIvFace, 320, 240);
				((ImageView)convertView.findViewById(R.id.imageview)).setImageBitmap(bm);
    			bm = null;
    		}
			
			return convertView;
		}
		
	}
	
	private void fetchLocalVoices(){
		File folder = new File(AppConst.APP_FOLDER);
		if(!folder.exists()) AppUtil.createAppDirectory();

		File[] files = folder.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				//Filter file name depends on VOICE_EXTENSION, you can modify
				// VOICE_EXTENSION constant if you use other format. But, take care.
				if(pathname.getAbsolutePath().toLowerCase().endsWith(
						VoiceHelper.VOICE_EXTENSION))
					return true;
				return false;
			}
		});
		mVoicePaths = new ArrayList<String>();
		
		if(files == null) return;
		
		for(File f : files){
			mVoicePaths.add(f.getAbsolutePath());
			Log.i(TAG, ".Local voice = " + f.getAbsolutePath());
		}
	}
		
	private void recordVoice(){
		if(mVoiceHelper == null){
			mVoiceHelper = new VoiceHelper();
		}
		mVoiceHelper.startVoiceRecorder();
	}
	
	private String stopRecordVoice(){
		return mVoiceHelper.stopVoiceRecoder();
	}
	
	private void playVoiceSample(String path){
		if(mPlayer != null) mPlayer.reset();
		else mPlayer = new MediaPlayer();
		
		mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		try {
			mPlayer.setDataSource(path);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void pauseVoicePlayer(){
		if(mPlayer!=null) mPlayer.pause();
	}
	
	private void stopVoicePlayer(){
		if(mPlayer!=null && mPlayer.isPlaying()){
			mPlayer.stop();
		}
	}
	
	private void saveVoice(){
		if(mVoicePaths.size() < MIN_VOICE_SAMPLE){
			Toast.makeText(this, "Please record 2 voices at least!!!", Toast.LENGTH_LONG).show();
			return;
		}
		
		VoiceTrainer trainer = new VoiceTrainer(ScreenVoiceTraining.this);
		trainer.startTraining(mVoicePaths);
	}
}
