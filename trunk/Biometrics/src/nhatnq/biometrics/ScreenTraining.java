package nhatnq.biometrics;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import nhatnq.biometrics.face.BioFaceDetector;
import nhatnq.biometrics.util.AppUtil;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ScreenTraining extends Activity {
	private static final String TAG = ScreenTraining.class.getCanonicalName();
	private static final int REQ_CAMERA_CAPTURE = 7;
	private ImageView mIvFace;
    private String mImagePath, extraImagePath;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_training);
		
		mIvFace = (ImageView) findViewById(R.id.image);
		Button bt;
		bt = (Button) findViewById(R.id.action_capture);
		bt.setOnClickListener(OnClickButtonHandler);
		bt = (Button) findViewById(R.id.action_detect);
		bt.setOnClickListener(OnClickButtonHandler);
		bt = (Button) findViewById(R.id.action_save);
		bt.setOnClickListener(OnClickButtonHandler);
		
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
    
    protected void onStart() {
    	super.onStart();Log.e(TAG, "onStart()");
    	if(mImagePath != null){
    		AppUtil.loadBitmapFromPath(mImagePath, mIvFace, 320, 480);
    	}
    };
//    
//    protected void onResume() {
//    	super.onResume();Log.e(TAG, "onResume()");
//    };
//    
//    protected void onRestart() {
//    	super.onRestart();Log.e(TAG, "onRestart()");
//    };
//    
//    protected void onPause() {
//    	super.onPause();Log.e(TAG, "onPause()");
//    };
//    
//    protected void onStop() {
//    	super.onStop();Log.e(TAG, "onStop()");
//    };
//    
//    protected void onDestroy() {
//    	super.onDestroy();Log.e(TAG, "onDestroy()");
//    };
    
    View.OnClickListener OnClickButtonHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.action_capture:
				callCameraImageCapturer();
				break;
			case R.id.action_detect:
				detectFaceFromImage();
				break;
			case R.id.action_save:
				
				break;
			}
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK && requestCode == REQ_CAMERA_CAPTURE){
			System.gc();
			mImagePath = null;
			
			String menufacturerBrand = Build.BRAND.toLowerCase();
			if (Build.VERSION.SDK_INT == 8
					|| menufacturerBrand.contains("rockchip")
					|| menufacturerBrand.contains("lenovo")
					|| menufacturerBrand.contains("fih")
					|| menufacturerBrand.contains("viewsonic")
					|| (menufacturerBrand.contains("motorola") && Build.MODEL
							.toLowerCase().contains("xt882"))) {
				mImagePath = extraImagePath; 
			}else{
				if(data == null){
					Log.e(TAG, "REQ_CAMERA_IMAGE_CAPTURER:: return NULL intent");
					String[] projection = { 
							MediaStore.Images.ImageColumns._ID,
							MediaStore.Images.ImageColumns.DATA};

					String sort = MediaStore.Images.ImageColumns._ID + " DESC";
					Cursor myCursor = getContentResolver().query(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							projection, null, null, sort);
					long imageId = 0l;
					try {
						myCursor.moveToFirst();
						imageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(
								MediaStore.Images.ImageColumns._ID));
					} finally {
						myCursor.close();
					}
					
					Uri uriLargeImage = Uri.withAppendedPath(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							String.valueOf(imageId));
					if (uriLargeImage != null) {
						mImagePath = AppUtil.getFilePathFromUri(this, uriLargeImage);
					} else {
						Log.i(TAG, "REQ_CAMERA_IMAGE_CAPTURER:: retrieve new uri but still NULL");			
					}
				}else{ 
					if (data.getData() != null) {
						mImagePath = AppUtil.getFilePathFromUri(this, data.getData());
					} else {
						Log.i(TAG, "REQ_CAMERA_IMAGE_CAPTURER:: return NULL uri");
						String[] projection = { 
								MediaStore.Images.ImageColumns._ID,
								MediaStore.Images.ImageColumns.DATA};

						String sort = MediaStore.Images.ImageColumns._ID + " DESC";
						Cursor myCursor = getContentResolver().query(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								projection, null, null, sort);
						long imageId = 0l;
						try {
							myCursor.moveToFirst();
							imageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(
									MediaStore.Images.ImageColumns._ID));
						} finally {
							myCursor.close();
						}
						
						Uri uriLargeImage = Uri.withAppendedPath(
								MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								String.valueOf(imageId));
						if (uriLargeImage != null) {
							mImagePath = AppUtil.getFilePathFromUri(this, uriLargeImage);
						} else {
							Log.i(TAG, "REQ_CAMERA_IMAGE_CAPTURER:: retrieve new uri but still NULL");			
						}
					}
				}
			}
			
//			if(mImagePath != null){
//				AppUtil.loadBitmapFromPath(mImagePath, mIvFace, 320, 480);
//			}
		}
	};
	
	protected void callCameraImageCapturer() {
		if(Environment.getExternalStorageState() == Environment.MEDIA_REMOVED){
			Toast.makeText(this, "Please insert SDCard into your phone", Toast.LENGTH_SHORT).show();
			return;
		}

		String sFile = String.format("%s/%s/", Environment
				.getExternalStorageDirectory().getAbsolutePath(), "biometrics");
		File folder = new File(sFile);
		if (!folder.exists())
			folder.mkdir();
		
		File imageFile = new File(folder, getImageNameAtThisTime());

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String menufacturerBrand = Build.BRAND.toLowerCase();
		if (Build.VERSION.SDK_INT == 8
				|| menufacturerBrand.contains("rockchip")
				|| menufacturerBrand.contains("lenovo")
				|| menufacturerBrand.contains("fih")
				|| menufacturerBrand.contains("viewsonic")
				|| (menufacturerBrand.contains("motorola") && Build.MODEL
						.toLowerCase().contains("xt882"))) {
			try {
				if (! imageFile.exists())
					imageFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
			extraImagePath = imageFile.getAbsolutePath();
			Log.i(TAG, "MediaStore.EXTRA_OUTPUT -> " + extraImagePath);
		}else
			Log.i(TAG, "MediaStore.EXTRA_OUTPUT -> NONE!");

		startActivityForResult(
				Intent.createChooser(intent, "Capture photo using"), 
				REQ_CAMERA_CAPTURE);
	}
	
	private void detectFaceFromImage(){
		if(mImagePath != null) {
			BioFaceDetector.detectFaceFromImage(mImagePath);
		}else{
			Toast.makeText(getBaseContext(), "Please capture an image...", Toast.LENGTH_SHORT).show();
		}
	}
	
	public static String getImageNameAtThisTime(){
		Calendar cal = Calendar.getInstance();
		return "Face_"+DateFormat.format("ddMMyyyy_hhmmss", cal).toString() +".jpg";
	}
}
