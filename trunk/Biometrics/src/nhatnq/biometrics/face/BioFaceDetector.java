package nhatnq.biometrics.face;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import nhatnq.biometrics.util.AppConst;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.util.Log;

public class BioFaceDetector {
	private static final int MAX_FACES = 3;
	
	/**
	 * Get all faces from an image
	 * @param imgPath Path of image
	 * @return <b>Array of BioFaceObject</b> that contains face's information for create face image
	 * or <b><i>null</i></b> if no faces found
	 */
	public static BioFaceObject[] detectFaceFromImage(String imgPath){
		Bitmap bm = BitmapFactory.decodeFile(imgPath);
		FaceDetector detector = new FaceDetector(
				bm.getWidth(), bm.getHeight(), MAX_FACES);
		Face[] faces = new Face[MAX_FACES];
		int nFaceFound = detector.findFaces(bm, faces);
		if(nFaceFound < 1){
			Log.e("BioFaceDetector", ".No faces found in image = "+imgPath);
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
		
		return objects;
	}
	
	/**
	 * Get biggest face in the image, in case of captured multi-face in image ^^
	 * @param objects List of face object detected before
	 * @return Biggest face in image
	 */
	public static BioFaceObject getTargetFace(BioFaceObject[] objects){
		int len = objects.length;
		int expectedIndex = 0;
		for(int i = 1; i < len; i++){
			if(objects[i].eyeDistance > objects[expectedIndex].eyeDistance){ 
				expectedIndex = i;
			}
		}
		
		return objects[expectedIndex];
	}
	
	/**
	 * Create new image contains object's face and store in './face' folder 
	 * @param object BioFaceObject contains face information
	 * @param srcPath Image path for face detection
	 * @return Image path after resizing, in './face' folder
	 */
	public static String createResizedFaceImageFromSource(BioFaceObject object, String srcPath){		
		File f = new File(srcPath);
		String filename = AppConst.FACE_FOLDER + "/" + f.getName().replace(
				".jpg", "_"+AppConst.IMG_FACE_WIDTH+"x"+AppConst.IMG_FACE_HEIGHT +".jpg");
		Bitmap bm = null;
		try {
			bm = BitmapFactory.decodeFile(srcPath);
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
			
			f = new File(filename);
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(filename);
			boolean res = newBm.compress(CompressFormat.JPEG, 100, fos);
			if(! res) return null;
			else return filename;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}

}
