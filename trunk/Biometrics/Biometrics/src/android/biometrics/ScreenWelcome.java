package android.biometrics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ScreenWelcome extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_welcome);
		
		ImageView iv;
		iv = (ImageView) findViewById(R.id.logo_face);
		iv.setOnClickListener(OnClickHandler);
		iv = (ImageView) findViewById(R.id.logo_voice);
		iv.setOnClickListener(OnClickHandler);
	}
	
	View.OnClickListener OnClickHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = null;
			switch (v.getId()) {
			case R.id.logo_face:
				intent = new Intent(ScreenWelcome.this, ScreenFaceTraining.class);
				break;
			case R.id.logo_voice:
				intent = new Intent(ScreenWelcome.this, ScreenVoiceTraining.class);
				break;
			}
			
			startActivity(intent);
		}
	};
}
