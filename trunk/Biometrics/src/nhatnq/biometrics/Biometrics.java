package nhatnq.biometrics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Biometrics extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ImageView ivTraining = (ImageView) findViewById(R.id.imageTraining);
        ivTraining.setOnClickListener(ClickImageHandler);
        
        ImageView ivRecognizing = (ImageView) findViewById(R.id.imageRecognizing);
        ivRecognizing.setOnClickListener(ClickImageHandler);
    }
    
    View.OnClickListener ClickImageHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = null;
			switch (v.getId()) {
			case R.id.imageTraining:
				intent = new Intent(Biometrics.this, ScreenTraining.class);
				break;
			case R.id.imageRecognizing:
				intent = new Intent(Biometrics.this, ScreenRecognizing.class);
				break;
			}
			
			startActivity(intent);
		}
	};
    
    
}