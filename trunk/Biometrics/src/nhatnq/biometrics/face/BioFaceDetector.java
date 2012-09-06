package nhatnq.biometrics.face;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;

public class BioFaceDetector {
	private static final int IMG_FACE_HEIGHT = 160;
	private static final int IMG_FACE_WIDTH = 160;
	private static final int MAX_FACES = 7;
	
	public static void detectFaceFromImage(String imgPath){
		FaceDetector detector = new FaceDetector(IMG_FACE_WIDTH, IMG_FACE_HEIGHT, MAX_FACES);
		Bitmap bm = BitmapFactory.decodeFile(imgPath);
		Face[] faces = new Face[MAX_FACES];
		int nFaceFound = detector.findFaces(bm, faces);
		for(int i = 0; i < nFaceFound; i++){
			Face face = faces[i];
			float confidence = face.confidence();
			float eyedistance = face.eyesDistance();
			PointF point = new PointF();
			face.getMidPoint(point);
		}
	}
}
