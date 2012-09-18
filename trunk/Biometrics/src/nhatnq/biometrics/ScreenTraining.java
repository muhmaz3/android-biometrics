package nhatnq.biometrics;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import nhatnq.biometrics.face.FaceTrainer;
import nhatnq.biometrics.util.AppConst;
import nhatnq.biometrics.util.AppUtil;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenTraining extends Activity {
	public static final int MIN_FACE_IMAGE_CAPTURED = 2; 
	private static final String TAG = ScreenTraining.class.getCanonicalName();
	private static final int REQ_CAMERA_CAPTURE = 7;
	private ImageView mIvFace;
	private Gallery mGallery;
	private GalleryApdapter mAdapter;
	private List<String> mCapturedFaceImages;
    public String extraImagePath;
    public static String mImagePath;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_training);
		
		mIvFace = (ImageView) findViewById(R.id.ivFace);
		mIvFace.setScaleType(ScaleType.FIT_CENTER);
		
		Button bt;
		bt = (Button) findViewById(R.id.btnCapture);
		bt.setOnClickListener(OnClickButtonHandler);
		bt = (Button) findViewById(R.id.btnDetect);
		bt.setOnClickListener(OnClickButtonHandler);
		bt = (Button) findViewById(R.id.btnSave);
		bt.setOnClickListener(OnClickButtonHandler);
		
		mGallery = (Gallery) findViewById(R.id.gallery);
		mGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int pos,
					long id) {
				String src = (String)adapter.getItemAtPosition(pos);
				Bitmap bm = BitmapFactory.decodeFile(src);
	    		mIvFace.setImageBitmap(bm);
	    		bm = null;
			}
			
		});
		
		fetchLocalFaceImages();
    	mAdapter = new GalleryApdapter(this);
		mGallery.setAdapter(mAdapter);
		
		if(savedInstanceState != null){
        	extraImagePath = savedInstanceState.getString("extra_image_path");
        	if(extraImagePath != null){
        		mImagePath = extraImagePath;
        	}else{
        		mImagePath = savedInstanceState.getString("image_path");
        	}
        }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mImagePath != null){
			Log.e(TAG, "onSaveInstanceState:img_path->"+mImagePath);
			outState.putString("image_path", mImagePath);
		}
		if(extraImagePath != null){
			Log.e(TAG, "onSaveInstanceState:extra->"+extraImagePath);
			outState.putString("extra_image_path", extraImagePath);
		}
	}
    
    protected void onResume() {
    	super.onResume();

    	if(extraImagePath != null){
    		AppUtil.loadBitmapFromPath(extraImagePath, mIvFace, 320, 240);
    		
    		mCapturedFaceImages.add(extraImagePath);
    		mAdapter.notifyDataSetChanged();
    	}

		TextView emptyView = new TextView(this);
		emptyView.setText("No face(s) found");
		emptyView.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		((ViewGroup)(mGallery.getParent())).addView(emptyView);
		mGallery.setEmptyView(emptyView);
    };
    
    View.OnClickListener OnClickButtonHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnCapture:
				callCameraImageCapturer();
				break;
			case R.id.btnDetect:
				break;
			case R.id.btnSave:
				saveFace();
				break;
			}
		}
	};
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST, Menu.NONE, "Clear training faces");
		return true;
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			AppUtil.clearTrainingSampleFromSdcard(true);
			break;
		default:
			break;
		}
		return true;
	}
	
	private class GalleryApdapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		
		public GalleryApdapter(Context context){
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return mCapturedFaceImages.size();
		}

		@Override
		public String getItem(int arg0) {
			return mCapturedFaceImages.get(arg0);
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
			
			/**
			 * Definitely the face image exists (load from local directory was okay)
			 */
			String src = getItem(pos);
			Bitmap bm;
			bm = AppUtil.decodeBitmapResized(src, 64, 48);
			((ImageView)convertView.findViewById(R.id.imageview)).setImageBitmap(bm);
			bm = null;
			
			return convertView;
		}
		
	}
	
	private void fetchLocalFaceImages(){
		File folder = new File(AppConst.APP_FOLDER);
		if(!folder.exists()) AppUtil.createAppDirectory();
		String[] faceImgs = folder.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				if(filename.toLowerCase().endsWith(".jpg"))
					return true;
				return false;
			}
		});
		mCapturedFaceImages = new ArrayList<String>();
		
		if(faceImgs == null) return;
		
		for(String s : faceImgs){
			mCapturedFaceImages.add(AppConst.APP_FOLDER + "/"+s);
			Log.i(TAG, ".Local face = " + s);
		}
	}
	
	private void callCameraImageCapturer() {
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)){
			Toast.makeText(this, "Please insert SDCard into your phone", Toast.LENGTH_SHORT).show();
			return;
		}
		
		File imageFile = new File(AppConst.APP_FOLDER, AppUtil.generateFaceNameAtThisTime());

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
		extraImagePath = imageFile.getAbsolutePath();
		
		startActivityForResult(intent, REQ_CAMERA_CAPTURE);
	}
	
	private void saveFace(){
		if(mCapturedFaceImages.size() < MIN_FACE_IMAGE_CAPTURED){
			Toast.makeText(this, "Please capture 2 images at least!!!", Toast.LENGTH_LONG).show();
			return;
		}
		FaceTrainer trainer = new FaceTrainer(ScreenTraining.this);
		trainer.saveFaceData(mCapturedFaceImages);
	}
	
}
