package nhatnq.biometrics.util;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_OTSU;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.widget.ImageView;

import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class AppUtil {
	
	/**
	 * Create an gray-scale image from an RGB image
	 * @param imgSource Path of RGB image
	 * @return Path of gray-scale image
	 */
	public static String createGrayscaleImage(String imgSource){
		IplImage src = cvLoadImage(imgSource);
		// Create gray-scale image from original image
		IplImage grayscale = cvCreateImage(new CvSize(src.width(), src.height()), IPL_DEPTH_8U, 1);
		cvCvtColor(src, grayscale, CV_RGB2GRAY);
		// Convert to binary image
		//@link http://stackoverflow.com/questions/1585535/convert-rgb-to-black-white-in-opencv
		IplImage im_bw = cvCreateImage(cvGetSize(grayscale),IPL_DEPTH_8U,1);
		cvThreshold(grayscale, im_bw, 128, 255, CV_THRESH_BINARY | CV_THRESH_OTSU);
		
		// Generate file name for gray-scale image
		String destName = imgSource.substring(imgSource.lastIndexOf("/")+1).replace("jpg", "pgm");
		String destPath = AppConst.FACE_FOLDER + "/" + destName;
		// Save loaded gray-scale image into SDcard
		int res = cvSaveImage(destPath, grayscale);
		if(res == -1) return null;
		else return destPath;
	}
	
	/**
	 * Get sample size for image resize rate
	 * @param option
	 * @param requestW
	 * @param requestH
	 * @return
	 */
	private static int getSampleSize(BitmapFactory.Options option,
			int requestW, int requestH) {
		final int height = option.outHeight;
		final int width = option.outWidth;
		int inSampleSize = 1;

		if (height > requestH || width > requestW) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) requestH);
			} else {
				inSampleSize = Math.round((float) width / (float) requestW);
			}
		}
		return inSampleSize;
	}
	
	public static Bitmap decodeBitmapResized(String imgPath, int requestWidth,
			int requestHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imgPath, options);

		options.inSampleSize = getSampleSize(options, requestWidth,
				requestHeight);
		options.inJustDecodeBounds = false;
		
		return BitmapFactory.decodeFile(imgPath, options);
	}
	
	public static void loadBitmapFromPath(String imgPath, ImageView iv,
			int reqWidth, int reqHeight) {
		if(imgPath != null && ((new File(imgPath)).exists())){
			new BDBitmapLoader(iv).execute(imgPath, String.valueOf(reqWidth),
					String.valueOf(reqHeight));
		}
	}

	/**
	 * Task for apply image for ImageView
	 * @author oliver
	 *
	 */
	private static class BDBitmapLoader extends AsyncTask<String, Void, Bitmap> {
		WeakReference<ImageView> mIvReference;

		public BDBitmapLoader(ImageView iv) {
			mIvReference = new WeakReference<ImageView>(iv);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			String imagePath = params[0];
			int reqWidth = Integer.parseInt(params[1]);
			int reqHeight = Integer.parseInt(params[2]);
			return decodeBitmapResized(imagePath, reqWidth, reqHeight);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (result != null && mIvReference != null) {
				ImageView iv = mIvReference.get();
				if (iv != null){
					iv.setImageBitmap(result);
				}
			}
		}
	}
	
	/**
	 * Get image file path from Uri, Uri was returned after picking
	 * @param context
	 * @param contentUri
	 * @return
	 */
	public static String getFilePathFromUri(Context context, Uri contentUri) {
		String resolvedPath = Uri.decode(contentUri.toString());
		if (resolvedPath == null || resolvedPath.equals(""))
			return null;

		if (resolvedPath.startsWith("content://media/external")
				|| resolvedPath
						.startsWith("content://com.android.gallery3d.provider")) { // new
																					// ROM
			String data_column_name = MediaStore.Images.Media.DATA;
			if (resolvedPath.startsWith("content://media/external/video"))
				data_column_name = MediaStore.Video.Media.DATA;
			String[] proj = { data_column_name };
			Cursor cursor = context.getContentResolver().query(contentUri,
					proj, null, null, null);
			cursor.moveToFirst();
			resolvedPath = cursor.getString(0);
			cursor.close();
			if (resolvedPath == null || resolvedPath.equals("")) {
				return null;
			}
		} else if (resolvedPath.contains("/mnt")) {
			resolvedPath = resolvedPath.substring(resolvedPath.indexOf("/mnt"));
		} else if (!resolvedPath.contains("/mnt")
				&& resolvedPath.contains("/sdcard")) {
			resolvedPath = "/mnt"
					+ resolvedPath.substring(resolvedPath.indexOf("/sdcard"));
		} else
			return null;

		return resolvedPath;
	}
	
	public static boolean createDirectory(File folder, String rest){
		if(! folder.exists()) folder.mkdir();
		String[] tokens = rest.split("/");
		File newFolder = null, parent = folder;
		for(int i = 0; i < tokens.length; i++){
			newFolder = new File(parent, tokens[i]);
			newFolder.mkdir();
			parent = newFolder;
			if(! newFolder.exists())
				return false;
		}
		if(newFolder.exists()){
			File[] files = newFolder.listFiles();
			int len = files.length;
			for(int i = 0; i < len; i++)
				files[i].delete();
		}
		return true;
	}
	
	/**
	 * Create folder contains application data
	 * @return true if success, false if can not create application folder
	 */
	public static boolean createAppDirectory(){
		File home = new File(AppConst.APP_FOLDER);
		home.mkdir();
		File f;
		f = new File(AppConst.FACE_FOLDER);
		f.mkdir();
		f = new File(AppConst.VOICE_FOLDER);
		f.mkdir();
		return true;
	}
	
	/**
	 * Clear all samples in application folder
	 * @param FaceOrVoice true if clear face images, false if clear voice samples
	 */
	public static void clearTrainingSampleFromSdcard(boolean FaceOrVoice){
		File f = new File(AppConst.APP_FOLDER);
		final String filterAttribute = FaceOrVoice ? ".jpg" : ".amr";
		String[] images = f.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File arg0, String name){
				if(name.toLowerCase().endsWith(filterAttribute)) return true;
				return false;
			}
		});
		
		String path;
		File ff;
		for(String s : images){
			path = AppConst.APP_FOLDER + "/" + s;
			ff = new File(path);
			ff.delete();
		}
	}
	
	public static boolean isSDCardAvailable(){
		String state = Environment.getExternalStorageState();
		return state.equals(Environment.MEDIA_MOUNTED);
	}

	public static String generateFaceNameAtThisTime(){
		Calendar cal = Calendar.getInstance();
		return "Face_"+DateFormat.format("ddMMyyyy_kkmmss", cal).toString() +".jpg";
	}
	
	public static String generateGrayscaleFaceNameAtThisTime(){
		Calendar cal = Calendar.getInstance();
		return "Face_8U_"+DateFormat.format("ddMMyyyy_kkmmss", cal).toString() +".jpg";
	}
	
	public static String generateVoiceNameAtThisTime(){
		Calendar cal = Calendar.getInstance();
		return "Voice_"+DateFormat.format("ddMMyyyy_kkmmss", cal).toString() +".amr";
	}
	
	/**
	 * After face detection process, create gray-scale face image
	 * and save into application folder 
	 * @param face Captured face
	 * @return true if saved, false if otherwise
	 */
	public static boolean saveFaceImage(Bitmap face){
		String filename = generateFaceNameAtThisTime();
		try {
			File f = new File(filename);
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(filename);
			return face.compress(CompressFormat.JPEG, 100, fos);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
}
