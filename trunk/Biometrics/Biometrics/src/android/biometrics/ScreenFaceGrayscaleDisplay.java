package android.biometrics;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.biometrics.face.FaceRecognizer;
import android.biometrics.util.AppConst;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class ScreenFaceGrayscaleDisplay extends Activity {
	private static final String TAG = ScreenFaceGrayscaleDisplay.class.getCanonicalName();
	String[] images;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_face_grayscale_display); 
		
		File[] files = getData();
		images = convertGrayscale2RGB(files);
		
		GridView gridview = (GridView) findViewById(R.id.grayscale_gridview);
		GrayscaleArapter adapter = new GrayscaleArapter(this);
		gridview.setAdapter(adapter);
		
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String s = (String) arg0.getAdapter().getItem(arg2);
				Toast.makeText(getBaseContext(), ""+s, Toast.LENGTH_LONG).show();
			}
			
		});
	}
	
	private File[] getData(){
		File f = new File(AppConst.FACE_FOLDER);
		return f.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				return filename.toLowerCase().endsWith(".pgm") ? true : false;
			}
		});
	}
	
	private String[] convertGrayscale2RGB(File[] files){
		File f = new File(AppConst.FACE_FOLDER+"/gray2rgb");
		f.mkdir();
		
		String[] result = new String[files.length];
		for(int i = 0; i < files.length; i++){
			result[i] = generateRGBImage(files[i].getAbsolutePath());
		}
		return result;
	}

	private String generateRGBImage(String path){
		// Load image from image path
		IplImage originalImg = cvLoadImage(path, CV_LOAD_IMAGE_GRAYSCALE);
		// Save preview image of this gray-scale
		File f = new File(path);
		File ff = new File(AppConst.FACE_FOLDER+"/gray2rgb");
		
		String imgName = f.getName().replace(".pgm", ".png");
		String imgSavedName;
		imgSavedName = ff.getAbsolutePath() + "/" + imgName;
		
		cvSaveImage(imgSavedName, originalImg);
		return imgSavedName;
	}
	
	class GrayscaleArapter extends BaseAdapter{
		
		private LayoutInflater mInflater;
		
		public GrayscaleArapter(Context context){
			mInflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getCount() {
			return images.length;
		}

		@Override
		public String getItem(int arg0) {
			return images[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.view_item_grayscale, null);
			}
			
			String src = getItem(pos);
			ImageView iv = (ImageView) convertView.findViewById(R.id.grayscale_img);
			Bitmap bm = BitmapFactory.decodeFile(src);
			iv.setImageBitmap(bm);
			bm = null;
			
			Log.e(TAG, ".Position="+pos);
			Log.e(TAG, ".Src="+src+", recognizing:"+FaceRecognizer.recognizingImagePath);
			if(src.equals(FaceRecognizer.recognizingImagePath)){
				Log.e(TAG, ".getView at "+pos+", highlight here...");
				View v = convertView.findViewById(R.id.bright_view);
				v.setVisibility(View.VISIBLE);
			}
			return convertView;
		}
		
	}
}
