package nhatnq.biometrics.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
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

public class AppUtil {
	
	
	
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
	
	/**
	 * Get bitmap after resizing from local file
	 * @param imgPath Path of image file
	 * @param requestWidth
	 * @param requestHeight
	 * @return Resized bitmap
	 */
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
	
	/**
	 * Apply image source for an ImageView
	 * @param imgPath Path of image file
	 * @param iv ImageView will be applied
	 * @param reqWidth Width of resized image
	 * @param reqHeight Height of resized image
	 */
	public static void loadBitmapFromPath(String imgPath, ImageView iv,
			int reqWidth, int reqHeight) {
		if(imgPath != null && ((new File(imgPath)).exists())){
			new BDBitmapLoader(iv).execute(imgPath, String.valueOf(reqWidth),
					String.valueOf(reqHeight));
		}
	}

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
	 * Get image file path from uri of that image
	 * @param context
	 * @param contentUri Uri of image that you picked via Gallery
	 * @return Path of picked image
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
	
	/**
	 * Create directory in sdcard using folder name and other sub-folder
	 * @param folder Folder name
	 * @param rest Sub-folder name
	 * @return true if success
	 */
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
		File[] images = f.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().toLowerCase().endsWith(filterAttribute);
			}
		});
		
		for(File ff : images){
			ff.delete();
		}
	}
	
	/**
	 * Whether or not your SDcard is ready for using
	 * @return true if SDcard is available
	 */
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
	
	public static String generateHEImageNameAtThisTime(){
		Calendar cal = Calendar.getInstance();
		return "_he_"+DateFormat.format("ddMMyyyy_kkmmss", cal).toString() +".png";
	}
	
	public static String generateDesaturationImageNameAtThisTime(){
		Calendar cal = Calendar.getInstance();
		return "_disa_"+DateFormat.format("ddMMyyyy_kkmmss", cal).toString() +".png";
	}
	
	public static String generateFinalFaceImageNameAtThisTime(){
		Calendar cal = Calendar.getInstance();
		return DateFormat.format("ddMMyyyy_kkmmss", cal).toString() +".jpg";
	}
	
	public static String generateTimeStringAtThisTime(){
		Calendar cal = Calendar.getInstance();
		return DateFormat.format("ddMMyyyy_kkmmss", cal).toString();
	}
	
	public static String formatNumber2String(int num){
    	if(num < 10) return "0"+num;
    	else return ""+num;
    }
	
	/**
	 * Save face bitmap to SDcard
	 * @param face Captured face bitmap
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
