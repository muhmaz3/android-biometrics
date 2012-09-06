package nhatnq.biometrics.util;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

public class AppUtil {
	
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
}
