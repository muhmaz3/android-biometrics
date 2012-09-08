package nhatnq.biometrics;

import nhatnq.biometrics.ui.FaceView;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ScreenFaceDetect extends Activity {
	
	FaceView faceView; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String imgPath = ScreenTraining.mImagePath;
		
		faceView = new FaceView(this, imgPath);
		setContentView(faceView);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Save");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals("Save")){
			
		}
		return true;
	}
	
	
}
