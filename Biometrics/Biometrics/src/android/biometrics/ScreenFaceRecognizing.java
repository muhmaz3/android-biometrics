
package android.biometrics;

import java.io.File;

import android.app.Activity;
import android.biometrics.face.FaceHelper;
import android.biometrics.face.FaceRecognizer;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class ScreenFaceRecognizing extends Activity {
//	private static final String TAG = ScreenFaceRecognizing.class.getCanonicalName();
	private static final int REQ_CAMERA_CAPTURE = 7;
    private String extraImagePath;
	private Spinner mThresholdSpn;
	private ImageView mIvFace;
	public static float threshold;  
	public static boolean result = false;
	private static String lastDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_face_recognizing);
		
		mThresholdSpn = (Spinner) findViewById(R.id.spinner_threshold);
		mThresholdSpn.setVisibility(View.GONE);
		mIvFace = (ImageView) findViewById(R.id.ivFace);
		
		ImageView bt;
		bt = (ImageView) findViewById(R.id.btnCapture);
		bt.setOnClickListener(ClickButtonHandler);
		bt = (ImageView) findViewById(R.id.btnRecognize);
		bt.setOnClickListener(ClickButtonHandler);
		
		if(savedInstanceState != null){
        	extraImagePath = savedInstanceState.getString("extra_image_path");
        }
	}
	
	protected void onResume() {
		super.onResume();
		
		if(extraImagePath != null){
			Bitmap bm = FaceHelper.processBitmap4Display(extraImagePath);
			mIvFace.setImageBitmap(bm);
			bm = null;
    	}
	};
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(extraImagePath != null){
			outState.putString("extra_image_path", extraImagePath);
		}
	}
	
	View.OnClickListener ClickButtonHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnCapture:
				if (!AppUtil.isSDCardAvailable()) {
					Toast.makeText(ScreenFaceRecognizing.this,
							getString(R.string.toast_no_sdcard_available),
							Toast.LENGTH_SHORT).show();
					break;
				}
				
				callCameraImageCapturer();
				break;
			case R.id.btnRecognize:
				if(! AppUtil.isTrained(ScreenFaceRecognizing.this, AppConst.KEY_FACE_TRAINED)){
					Toast.makeText(ScreenFaceRecognizing.this, 
							getString(R.string.not_train_yet), 
							Toast.LENGTH_LONG).show();
					return;
				}
				if(extraImagePath == null){
					Toast.makeText(ScreenFaceRecognizing.this, 
							getString(R.string.toast_please_capture_your_face), 
							Toast.LENGTH_LONG).show();
					return;
				}
				
				recognize();
				break;
			}
		}
	};
	
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
//		menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST, Menu.NONE, "Pick Gallery");
//		menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST+1, Menu.NONE, "View gray-scale");
		menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST+2, Menu.NONE, "Clear training data");
		return true;
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case Menu.FIRST:
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setDataAndType(Uri.parse(lastDirectory), "*/*");
			startActivityForResult(intent, 77);
			break;
		case Menu.FIRST + 1:
			intent = new Intent(ScreenFaceRecognizing.this, ScreenFaceGrayscaleDisplay.class);
			startActivity(intent);
			break;
		case Menu.FIRST + 2:
			AppUtil.clearTrainingData(true);
			finish();
			intent = new Intent(ScreenFaceRecognizing.this, ScreenFaceTraining.class);
			startActivity(intent);
			break;
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQ_CAMERA_CAPTURE && resultCode == RESULT_OK){
			lastDirectory = (new File(extraImagePath)).getParent();
			Bitmap bm = FaceHelper.processBitmap4Display(extraImagePath);
			mIvFace.setImageBitmap(bm);
			bm = null;
		}else{
			extraImagePath = null;
		}
	}
	
	private void callCameraImageCapturer() {
		File imageFile = new File(AppConst.FACE_FOLDER, AppUtil.generateFaceNameAtThisTime());

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
		extraImagePath = imageFile.getAbsolutePath();

		startActivityForResult(intent, REQ_CAMERA_CAPTURE);
	}

	private void recognize(){
		threshold = getConfidenceThreshold();
		
		FaceRecognizer recog = new FaceRecognizer(ScreenFaceRecognizing.this);
		recog.recognize(extraImagePath);
	}
	
	private float getConfidenceThreshold(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		return Float.parseFloat(pref.getString(
				getString(R.string.pref_confidence_threshold_key), 
				""+AppConst.DEFAULT_FACE_THRESHOLD));
	}
	
}
