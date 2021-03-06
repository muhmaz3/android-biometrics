package android.biometrics.face;

import java.io.File;

import android.biometrics.util.AppConst;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Build;
import android.util.Log;

public class BioFaceDetector {
	private static final String TAG = BioFaceDetector.class.getCanonicalName();
	private static final int MAX_FACES = 3;
	
	/**
	 * Get biggest face in the image, then, get detected bitmap from that face images
	 * @param imageFilePath Image path that contains faces
	 * @return Biggest face in image
	 */
	public static Bitmap getTargetFace(String imageFilePath){
		File file = new File(imageFilePath);
		if( file.exists()) Log.e(TAG, "@getTargetFace from "+imageFilePath);
		else{
			Log.e(TAG, "@getTargetFace: not found image at "+imageFilePath);
			return null;
		}
		
		Bitmap bm = BitmapFactory.decodeFile(imageFilePath);
		Bitmap cloneBm;
		if(Build.VERSION.SDK_INT < 14){
			cloneBm = bm; 
		}else{
			cloneBm = bm.copy(Bitmap.Config.RGB_565, false);		
			// OR
//			BitmapFactory.Options opts = new BitmapFactory.Options();
//			opts.inPreferredConfig = Bitmap.Config.RGB_565;
//			cloneBm = BitmapFactory.decodeFile(imageFilePath, opts);
		}
		bm = null;
		
		FaceDetector detector = new FaceDetector(
				cloneBm.getWidth(), cloneBm.getHeight(), MAX_FACES);
		Face[] faces = new Face[MAX_FACES];
		int nFaceFound = detector.findFaces(cloneBm, faces);
		if(nFaceFound < 1){
			Log.e("BioFaceDetector", ".No faces found in image = "+imageFilePath);
			return null;
		}
		
		float confidence, eyeDistance;
		BioFaceObject[] objects = new BioFaceObject[nFaceFound];
		for(int i = 0; i < nFaceFound; i++){
			Face face = faces[i];
			confidence = face.confidence();
			eyeDistance = face.eyesDistance();
			PointF point = new PointF();
			face.getMidPoint(point);
			
			BioFaceObject obj = new BioFaceObject(point, eyeDistance, confidence);
			objects[i] = obj;
		}
		int expectedIndex = 0;
		for(int i = 1; i < nFaceFound; i++){
			if(objects[i].eyeDistance > objects[expectedIndex].eyeDistance){ 
				expectedIndex = i;
			}
		}
		
		return getFace(objects[expectedIndex], cloneBm);
	}
	
	private static Bitmap getFace(BioFaceObject object, Bitmap bm){
		float xFactor, yFactor, wFactor, hFactor;
		xFactor = AppConst.IMG_X_FACTOR;
		yFactor = AppConst.IMG_Y_FACTOR;
		wFactor = 2 * xFactor;
		hFactor = 2 * yFactor;
		int x, y, w, h;
		x = (int)(object.midPoint.x - (object.eyeDistance * xFactor));
		if(x < 0) x = 0;
		
		y = (int)(object.midPoint.y - (object.eyeDistance * yFactor));
		if(y < 0) y = 0;
		
		w = (int)(wFactor * object.eyeDistance);
		if(w + x > bm.getWidth()) w = bm.getWidth()-x;
		
		h = (int)(hFactor * object.eyeDistance);
		if(h + y > bm.getHeight()) h = bm.getHeight() - y;
		
		// Extract small bitmap (contains detected face) from captured image
		Bitmap newBm = Bitmap.createBitmap(bm, x, y, w, h);
		// Resize bitmap with expected ratio
		newBm = Bitmap.createScaledBitmap(newBm, 
				AppConst.IMG_FACE_WIDTH, AppConst.IMG_FACE_HEIGHT, false);
		return newBm;
	}
	
	/**
	 * Create new image contains object's face and store in './face' folder 
	 * @param object BioFaceObject contains face information
	 * @param srcPath Image path for face detection
	 * @return Image path after resizing, in './face' folder
	 */
	public static Bitmap extractFaceBitmap(BioFaceObject object, String srcPath){		
		Bitmap bm = BitmapFactory.decodeFile(srcPath);
		return getFace(object, bm);
	}

}
