package android.biometrics;

import android.app.Activity;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.biometrics.voice.ExtAudioRecorder;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenVoiceRecording extends Activity {

	ExtAudioRecorder audioRedorder;
	private ImageView ivPlay;
	private String voicePath;
	private TextView txtStatus, txtDuration;
	long startTime;
	String durationTime;
	boolean isRunThread = false;

	Thread myThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_voice_recording);

		ivPlay = (ImageView) findViewById(R.id.play);
		ivPlay.setOnClickListener(OnClickHandler);

		txtStatus = (TextView) findViewById(R.id.status);
		txtDuration = (TextView) findViewById(R.id.duration);

		Button bt;
		bt = (Button) findViewById(R.id.save);
		bt.setOnClickListener(OnClickHandler);
		bt = (Button) findViewById(R.id.cancel);
		bt.setOnClickListener(OnClickHandler);
	}

	View.OnClickListener OnClickHandler = new View.OnClickListener() {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.play:
				ivPlay.setEnabled(false);
				txtStatus.setText("Recording...");

				voicePath = AppConst.VOICE_FOLDER + "/"+ AppUtil.generateVoiceNameAtThisTime();
				// Set time when start record
				startTime = System.currentTimeMillis();

				Runnable runnable = new CountDownRunner();
				myThread = new Thread(runnable);
				isRunThread = true;
				myThread.start();
				audioRedorder = ExtAudioRecorder.getInstanse(false); // Uncompressed
																		// recording
																		// (WAV)
				audioRedorder.setOutputFile(voicePath);
				audioRedorder.prepare();
				audioRedorder.start();

				break;

			case R.id.save:
				if (!isRunThread) {
					Toast.makeText(ScreenVoiceRecording.this,
							"Please record voice before 'Save'",
							Toast.LENGTH_SHORT).show();
				} else {
					txtStatus.setText("Tap micro icon to record");
					txtDuration.setText("00:00");
					isRunThread = false;
					audioRedorder.stop();
					audioRedorder.release();

					// Push back result with the path of new voice sample
					Intent data = new Intent();
					data.putExtra("path", voicePath);
					data.putExtra("duration", durationTime);
					setResult(RESULT_OK, data);
					finish();
				}
				break;
			case R.id.cancel:
				setResult(RESULT_CANCELED);
				finish();
				break;
			}
		}

	};

	public void doWork() {
		runOnUiThread(new Runnable() {
			public void run() {
				if (isRunThread) {
					try {
						long lenthTime = (System.currentTimeMillis() - startTime) / 1000;
						// Log.i("size", lenthTime +"");
						int minutes = (int) lenthTime / 60;
						int seconds = (int) lenthTime % 60;
						String min, second;
						if (minutes < 10)
							min = "0" + minutes;
						else
							min = String.valueOf(minutes);

						if (seconds < 10)
							second = "0" + seconds;
						else
							second = String.valueOf(seconds);
						durationTime = min + ":" + second;
						txtDuration.setText(durationTime);
					} catch (Exception e) {
					}
				}
			}
		});
	}

	class CountDownRunner implements Runnable {
		
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					doWork();
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (Exception e) {
				}
			}
		}
	}
}
