package nhatnq.biometrics.face;

import android.graphics.PointF;

public class BioFaceObject {
	public PointF midPoint;
	public float eyeDistance;
	public float confidence;
	
	public BioFaceObject(PointF midPoint, float eyeDistance, float confidence){
		this.midPoint = midPoint;
		this.eyeDistance = eyeDistance;
		this.confidence = confidence;
	}
}
