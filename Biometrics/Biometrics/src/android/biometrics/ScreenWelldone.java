package android.biometrics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ScreenWelldone extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_welldone);
		
		Button bt = (Button) findViewById(R.id.button_done);
		bt.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(ScreenWelldone.this, ScreenWelcome.class);
				startActivity(intent);
			}
		});
	}
}
