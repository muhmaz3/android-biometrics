package android.biometrics.util;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.Calendar;

import android.biometrics.R;
import android.biometrics.face.FaceHelper;
import android.biometrics.voice.VoiceHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.widget.ImageView;

public class AppUtil {

	public static void savePreference(Context context, String key, boolean value){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public static void savePreference(Context context, String key, String value){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static int getRecognitionMode(Context context){
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return Integer.parseInt(pref.getString(
				context.getString(R.string.pref_recognition_mode_key), 
				""+AppConst.RECOGNITION_MODE_JUST_FACE)
		);
	}
	
	public static boolean isTrained(Context context, String key){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getBoolean(key, false);
	}
	
	/**
	 * Get sample size for image resize rate
	 * 
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
	 * 
	 * @param imgPath
	 *            Path of image file
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
	 * 
	 * @param imgPath
	 *            Path of image file
	 * @param iv
	 *            ImageView will be applied
	 * @param reqWidth
	 *            Width of resized image
	 * @param reqHeight
	 *            Height of resized image
	 */
	public static void loadBitmapFromPath(String imgPath, ImageView iv,
			int reqWidth, int reqHeight) {
		if (imgPath != null && ((new File(imgPath)).exists())) {
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
				if (iv != null) {
					iv.setImageBitmap(result);
				}
			}
		}
	}

	/**
	 * Get image file path from uri of that image
	 * 
	 * @param context
	 * @param contentUri
	 *            Uri of image that you picked via Gallery
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
				&& resolvedPath.contains(Environment.getExternalStorageDirectory().getPath())) {
			resolvedPath = "/mnt"
					+ resolvedPath.substring(
							resolvedPath.indexOf(Environment.getExternalStorageDirectory().getPath()));
		} else
			return null;

		return resolvedPath;
	}

	/**
	 * Create directory in sdcard using folder name and other sub-folder
	 * 
	 * @param folder
	 *            Folder name
	 * @param rest
	 *            Sub-folder name
	 * @return true if success
	 */
	public static boolean createDirectory(File folder, String rest) {
		if (!folder.exists())
			folder.mkdir();
		String[] tokens = rest.split("/");
		File newFolder = null, parent = folder;
		for (int i = 0; i < tokens.length; i++) {
			newFolder = new File(parent, tokens[i]);
			newFolder.mkdir();
			parent = newFolder;
			if (!newFolder.exists())
				return false;
		}
		if (newFolder.exists()) {
			File[] files = newFolder.listFiles();
			int len = files.length;
			for (int i = 0; i < len; i++)
				files[i].delete();
		}
		return true;
	}

	/**
	 * Create folder contains application data
	 * 
	 * @return true if success, false if can not create application folder
	 */
	public static boolean createAppDirectory() {
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
	 * Clear all samples in application folder and data file in training process
	 * 
	 * @param FaceOrVoice
	 *            true if clear face images, false if clear voice samples
	 */
	public static void clearTrainingSampleFromSdcard(boolean FaceOrVoice) {
		File f = new File(AppConst.APP_FOLDER);
		final String filterAttribute;
		if (FaceOrVoice) {
			filterAttribute = FaceHelper.JPG_EXTENSION;
		} else {
			filterAttribute = VoiceHelper.VOICE_EXTENSION;
		}
		clearTrainingData(FaceOrVoice);
		
		File[] files = f.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().toLowerCase().endsWith(filterAttribute);
			}
		});

		if(files == null) return;
		for (File ff : files) {
			ff.delete();
		}
	}

	public static void clearTrainingData(boolean FaceOrVoice){
		String filepath;
		if(FaceOrVoice){
			filepath = AppConst.FACE_DATA_FILE_PATH;
		}else{
			filepath = AppConst.VOICE_DATA_FILE_PATH;
		}
		File face_data = new File(filepath);
		if(face_data.exists()) face_data.delete();
	}
	
	/**
	 * Whether or not your SDcard is ready for using
	 * 
	 * @return true if SDcard is available
	 */
	public static boolean isSDCardAvailable() {
		String state = Environment.getExternalStorageState();
		return state.equals(Environment.MEDIA_MOUNTED);
	}

	public static String generateVoiceNameAtThisTime() {
		Calendar cal = Calendar.getInstance();
		return DateFormat.format("yyyyMMdd_kkmmss", cal).toString()
				+ VoiceHelper.VOICE_EXTENSION;
	}
	
	public static String generateFaceNameAtThisTime() {
		Calendar cal = Calendar.getInstance();
		return DateFormat.format("yyyyMMdd_kkmmss", cal).toString()
				+ FaceHelper.JPG_EXTENSION;
	}

	public static String generateGrayscaleFaceNameAtThisTime() {
		Calendar cal = Calendar.getInstance();
		return DateFormat.format("yyyyMMdd_kkmmss", cal).toString() 
				+"_gs"+ FaceHelper.JPG_EXTENSION;
	}

	public static String generateHEImageNameAtThisTime() {
		Calendar cal = Calendar.getInstance();
		return DateFormat.format("yyyyMMdd_kkmmss", cal).toString()
				+"_he" + FaceHelper.PNG_EXTENSION;
	}

	public static String generateDesaturationImageNameAtThisTime() {
		Calendar cal = Calendar.getInstance();
		return DateFormat.format("yyyyMMdd_kkmmss", cal).toString()
				+"_disa"+ FaceHelper.PNG_EXTENSION;
	}

	public static String formatNumber2String(int num) {
		if (num < 10)
			return "0" + num;
		else
			return "" + num;
	}

	public static String getHENameFromJPGName(String originalPath){
		File f = new File(originalPath);
        String name = f.getName().replace(FaceHelper.JPG_EXTENSION, "_he" + FaceHelper.JPG_EXTENSION);
        return AppConst.FACE_FOLDER+"/"+name;
	}
	
	public static String getPGMNameFromHEName(String originalPath){
        return originalPath.replace(FaceHelper.JPG_EXTENSION, "_pgm" + FaceHelper.PGM_EXTENSION);
	}

}
