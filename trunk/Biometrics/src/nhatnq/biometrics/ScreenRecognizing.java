package nhatnq.biometrics;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenRecognizing extends Activity {
	private static final String TAG = ScreenRecognizing.class.getCanonicalName();
	private static final int REQ_CAMERA_CAPTURE = 7;
    private String mImagePath, extraImagePath;
	private Spinner mThresholdSpn;
	private TextView mTvStatus;
	private ImageView mIvFace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_recognizing);
		
		mThresholdSpn = (Spinner) findViewById(R.id.spinner_threshold);
		mIvFace = (ImageView) findViewById(R.id.imgFace);
		mTvStatus = (TextView) findViewById(R.id.tvStatus);
		
		Button bt;
		bt = (Button) findViewById(R.id.btnCapture);
		bt.setOnClickListener(ClickButtonHandler);
		bt = (Button) findViewById(R.id.btnRecognize);
		bt.setOnClickListener(ClickButtonHandler);
	}
	
	View.OnClickListener ClickButtonHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnCapture:
				callCameraImageCapturer();
				break;
			case R.id.btnRecognize:
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
	
	public static String getImageNameAtThisTime(){
		Calendar cal = Calendar.getInstance();
		return "Face_"+DateFormat.format("ddMMyyyy_hhmmss", cal).toString() +".jpg";
	}
}
