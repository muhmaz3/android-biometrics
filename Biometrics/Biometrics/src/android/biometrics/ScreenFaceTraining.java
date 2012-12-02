package android.biometrics;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.biometrics.face.FaceHelper;
import android.biometrics.face.FaceTrainer;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class ScreenFaceTraining extends Activity {
	private static final String TAG = ScreenFaceTraining.class.getCanonicalName();
	
	private ImageView mIvFace;
	private Gallery mGallery;
	private GalleryApdapter mAdapter;
	private List<String> mCapturedFaceImages;
    public String extraImagePath;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_face_training);
		
		mIvFace = (ImageView) findViewById(R.id.ivFace);
		mIvFace.setScaleType(ScaleType.FIT_CENTER);
		
		ImageView bt;
		bt = (ImageView) findViewById(R.id.btnCapture);
		bt.setOnClickListener(OnClickButtonHandler);
		bt = (ImageView) findViewById(R.id.btnSave);
		bt.setOnClickListener(OnClickButtonHandler);
		
		bt = (ImageView) findViewById(R.id.btnSettings);
		bt.setOnClickListener(OnClickButtonHandler);
		bt = (ImageView) findViewById(R.id.btnModeRecognizing);
		bt.setOnClickListener(OnClickButtonHandler);
		
		mGallery = (Gallery) findViewById(R.id.gallery);
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> adapter, View v, int pos,
					long id) {
				String src = (String)adapter.getItemAtPosition(pos);
	    		Bitmap bm;
	    		bm = FaceHelper.processBitmap4Display(src);
				if(bm != null){
	    			mIvFace.setImageBitmap(bm);
	    			bm = null;
	    		}
			}
			
		});
		
		View emptyView = getLayoutInflater().inflate(R.layout.view_face_empty, null);
		ViewGroup parent = (ViewGroup)mGallery.getParent();
		parent.addView(emptyView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//		mGallery.setEmptyView(emptyView);
		
		fetchLocalFaceImages();
    	mAdapter = new GalleryApdapter(this);
		mGallery.setAdapter(mAdapter);
		
		if(savedInstanceState != null){
        	extraImagePath = savedInstanceState.getString("extra_image_path");
        }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(extraImagePath != null){
			Log.e(TAG, "onSaveInstanceState:extra->"+extraImagePath);
			outState.putString("extra_image_path", extraImagePath);
		}
	}
    
    protected void onResume() {
    	super.onResume();
    	Log.e(TAG, "onResume");
    	if(mCapturedFaceImages != null && mCapturedFaceImages.size() >= 1){
    		Bitmap bm = BitmapFactory.decodeFile(mCapturedFaceImages.get(mCapturedFaceImages.size()-1));
    		mIvFace.setImageBitmap(bm);
    		bm = null;
    	}
    	
    	mAdapter.notifyDataSetChanged();
    };
     
    View.OnClickListener OnClickButtonHandler = new View.OnClickListener() {
		
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnCapture:
				if (!AppUtil.isSDCardAvailable()) {
					Toast.makeText(ScreenFaceTraining.this,
							getString(R.string.toast_no_sdcard_available),
							Toast.LENGTH_SHORT).show();
					break;
				}
				callCameraImageCapturer();
				break;
			case R.id.btnSave:
				if (!AppUtil.isSDCardAvailable()) {
					Toast.makeText(ScreenFaceTraining.this,
							getString(R.string.toast_no_sdcard_available),
							Toast.LENGTH_SHORT).show();
					break;
				}
				
				train();
				break;
			case R.id.btnSettings:
				Intent intent = new Intent(ScreenFaceTraining.this, ScreenSettings.class);
				startActivity(intent);
				break;
			case R.id.btnModeRecognizing:
				Intent intent1 = new Intent(ScreenFaceTraining.this, ScreenFaceRecognizing.class);
				startActivity(intent1);
				break;
			}
		}
	};
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST, Menu.NONE, getString(R.string.clear_all_faces));
		return true;
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			AppUtil.clearTrainingSampleFromSdcard(true);
			AppUtil.savePreference(this, AppConst.KEY_FACE_TRAINED, false);
			
			mIvFace.setImageResource(R.drawable.ic_face_smile);
			
			mCapturedFaceImages.clear();
			mAdapter.notifyDataSetChanged();
			break;
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		/**
		 * You do not have to care about losing image file path,
		 * because we're just testing with Samsung device(level 9 & level 14).
		 * Just push image path in extra, that is real image path later.
		 */
		
		if(requestCode == AppConst.REQ_CAMERA_CAPTURE && resultCode == RESULT_OK){
			if(extraImagePath != null){
	    		Bitmap bm = FaceHelper.processBitmap4Display(extraImagePath);
	    		if(bm != null){
	    			mIvFace.setImageBitmap(bm);
	    			bm = null;
	    		}
	    		
	    		mCapturedFaceImages.add(extraImagePath);
	    		mAdapter.notifyDataSetChanged();
	    	}
		}
		
	
	}
	
	private class GalleryApdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		
		public GalleryApdapter(Context context){
			mInflater = LayoutInflater.from(context);
		}
		
		public int getCount() {
			return mCapturedFaceImages.size();
		}

		public String getItem(int arg0) {
			return mCapturedFaceImages.get(arg0);
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(int pos, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.view_item_gallery, null);
			}

			String src = getItem(pos);
			Bitmap bm = FaceHelper.decodeBitmapResized(src, 72, 72);
			bm = Bitmap.createScaledBitmap(bm, 64, 64, false);
			if(bm != null){
				((ImageView)convertView.findViewById(R.id.imageview)).setImageBitmap(bm);
    			bm = null;
    		}
			
			return convertView;
		}
		
	}
	
	private void fetchLocalFaceImages(){
		File folder = new File(AppConst.APP_FOLDER);
		if(!folder.exists()) AppUtil.createAppDirectory();
		
		File[] files = folder.listFiles(new FileFilter() {
			
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().toLowerCase().endsWith(FaceHelper.JPG_EXTENSION);
			}
		});
		mCapturedFaceImages = new ArrayList<String>();
		
		if(files == null) return;
		
		for(File f : files){
			mCapturedFaceImages.add(f.getAbsolutePath());
			Log.i(TAG, ".Local face = " + f.getAbsolutePath());
		}
	}
	
	private void callCameraImageCapturer() {
		File imageFile = new File(AppConst.APP_FOLDER, AppUtil.generateFaceNameAtThisTime());

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
		extraImagePath = imageFile.getAbsolutePath();
		
		Log.e(TAG, "callCameraImageCapturer->"+extraImagePath);
		startActivityForResult(intent, AppConst.REQ_CAMERA_CAPTURE);
	}
	
	private void train(){
		if(mCapturedFaceImages.size() < AppConst.MIN_FACE_IMAGE_CAPTURED){
			Toast.makeText(this, getString(R.string.toast_not_enough_faces, AppConst.MIN_FACE_IMAGE_CAPTURED), 
					Toast.LENGTH_LONG).show();
			return;
		}
		FaceTrainer trainer = new FaceTrainer(ScreenFaceTraining.this);
		trainer.train(mCapturedFaceImages);
	}
	
	/**
	 * @author Nhat Nguyen
	 * Don't care about 2 tasks below, I use those in other job ^_^
	 */

	
	class CollectPGMFileTask extends AsyncTask<String, Void, Void>{
		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(ScreenFaceTraining.this);
			dialog.setMessage("Processing...");
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(String... params) {
			String path = params[0];
			File f = new File(path);
			File tf = f.getParentFile().getParentFile();
			File[] tfs = tf.listFiles(new FileFilter() {
				
				public boolean accept(File pathname) {
					return !pathname.getName().endsWith(".txt");
				}
			});
			
			for(File ff : tfs){
				File[] ffs = ff.listFiles(new FileFilter() {
					
					public boolean accept(File pathname) {
						return pathname.getName().endsWith(".pgm");
					}
				});
				
				try{
					File txt = new File(ff.getAbsolutePath() +".txt");
					if(txt.exists()) txt.delete();
					txt.createNewFile();
					
					FileWriter w = new FileWriter(txt);
					for(File fff : ffs){
						w.append(fff.getAbsolutePath()+"\n");
					}
					w.flush();
					w.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.dismiss();
		}
	}
	
	class GeneratePNGFileTask extends AsyncTask<String, Void, Void>{
		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(ScreenFaceTraining.this);
			dialog.setMessage("Processing...");
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(String... params) {
			String path = params[0];
			File f = new File(path);
			File tf = f.getParentFile().getParentFile();
			File[] tfs = tf.listFiles(new FileFilter() {
				
				public boolean accept(File pathname) {
					return ! pathname.getName().endsWith(".txt");
				}
			});
			
			for(File ff : tfs){
				File[] ffs = ff.listFiles(new FileFilter() {
					
					public boolean accept(File pathname) {
						return pathname.getName().endsWith(".pgm");
					}
				});
				File pngFolder = new File(tf.getAbsolutePath()+"/png"+ff.getName().substring(ff.getName().length()-2));
				pngFolder.mkdir();
				for(File fff : ffs){
					String destPath = pngFolder.getAbsolutePath()+"/"+fff.getName().replace(".pgm", ".png");
					createGrayscaleFromPGM(fff.getAbsolutePath(), destPath);
				}
			}
			return null;
		}
		
		private String createGrayscaleFromPGM(String path, String destPath){
//			Log.e(TAG, "@createGrayscaleFromPGM: "+path);
			IplImage originalImg = cvLoadImage(path, CV_LOAD_IMAGE_GRAYSCALE);
//			String imgName = path.replace(".pgm", ".png");
			
			cvSaveImage(destPath, originalImg);
			return destPath;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.dismiss();
		}
	}
	
}
