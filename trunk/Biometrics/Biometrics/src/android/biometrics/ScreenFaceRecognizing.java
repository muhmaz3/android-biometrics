
package android.biometrics;

import java.io.File;

import android.app.Activity;
import android.biometrics.face.FaceHelper;
import android.biometrics.face.FaceRecognizer;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ScreenFaceRecognizing extends Activity {
	private String extraImagePath;
	private ImageView mIvFace;

	private static String lastDirectory = 
			Environment.getExternalStorageDirectory().getAbsolutePath();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_face_recognizing);

		mIvFace = (ImageView) findViewById(R.id.ivFace);
		
		Button bt;
		bt = (Button) findViewById(R.id.btnCapture);
		bt.setOnClickListener(ClickButtonHandler);
		bt = (Button) findViewById(R.id.btnRecognize);
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
				if(! AppUtil.isFaceTrained(getBaseContext())){
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
		menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST+3, Menu.NONE, "View lastest confidence");
		return true;
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case Menu.FIRST:
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setDataAndType(Uri.parse(lastDirectory), "*/*");
			startActivityForResult(intent, AppConst.REQ_CAMERA_CAPTURE);
			break;
		case Menu.FIRST + 1:
			intent = new Intent(ScreenFaceRecognizing.this, ScreenFaceGrayscaleDisplay.class);
			startActivity(intent);
			break;
		case Menu.FIRST + 2:
			AppUtil.deleteTrainingFiles(getApplicationContext());
			finish();
			intent = new Intent(ScreenFaceRecognizing.this, ScreenFaceTraining.class);
			startActivity(intent);
			break;
		case Menu.FIRST + 3:
			float faceConfidence = AppUtil.getPreference(getApplicationContext(), 
					AppConst.CONFIDENT_FACE_CALCULATED);
			if(faceConfidence != AppConst.DEFAULT_FACE_THRESHOLD){
				Toast.makeText(getBaseContext(), "Lastest confidence: "+faceConfidence, 
						Toast.LENGTH_LONG).show();
			}
			break;
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == AppConst.REQ_CAMERA_CAPTURE && resultCode == RESULT_OK){
			lastDirectory = (new File(extraImagePath)).getParent();
			Bitmap bm = FaceHelper.processBitmap4Display(extraImagePath);
			mIvFace.setImageBitmap(bm);
			bm = null;

		}else{
			extraImagePath = null;
			// Remove Toast
		}

	}
	
	private void callCameraImageCapturer() {
		File imageFile = new File(AppConst.FACE_FOLDER, AppUtil.generateFaceNameAtThisTime());

		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
		extraImagePath = imageFile.getAbsolutePath();

		startActivityForResult(intent, AppConst.REQ_CAMERA_CAPTURE);
	}

	private void recognize(){
		FaceRecognizer recog = new FaceRecognizer(ScreenFaceRecognizing.this);
		recog.recognize(extraImagePath);
	}
	
}
