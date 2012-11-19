package android.biometrics.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.util.Log;
import android.view.View;

/**
 * @author Put by Nhat Nguyen
 * Detect face and draw a border rectangle onto image 
 */

public class FaceView extends View {
	private static final int NUM_FACES = 3; // max is 64

	private FaceDetector arrayFaces;
	private FaceDetector.Face getAllFaces[] = new FaceDetector.Face[NUM_FACES];
	private FaceDetector.Face getFace = null;
	
	public PointF eyesMidPts[] = new PointF[NUM_FACES];
	public float  eyesDistance[] = new float[NUM_FACES];
	
	private Bitmap sourceImage;
	
	private Paint tmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint pOuterBullsEye = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint pInnerBullsEye = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private int picWidth, picHeight;
	private float xRatio, yRatio;
	
	public FaceView(Context context, String imgPath) {
		super(context);
		
		pInnerBullsEye.setStyle(Paint.Style.FILL);
		pInnerBullsEye.setColor(Color.RED);
		
		pOuterBullsEye.setStyle(Paint.Style.STROKE);
		pOuterBullsEye.setColor(Color.GREEN);
		
		tmpPaint.setStyle(Paint.Style.STROKE);
		tmpPaint.setTextAlign(Paint.Align.CENTER);
		
		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inPreferredConfig = Bitmap.Config.RGB_565;
		
//		sourceImage = BitmapFactory.decodeFile(imgPath);
		sourceImage = rotateBitmap90(imgPath);
		Log.e("BITMAP_4_FACEVIEW", "W= "+sourceImage.getWidth()+", H="+sourceImage.getHeight());

		picWidth = sourceImage.getWidth();
		picHeight = sourceImage.getHeight();
		
		arrayFaces = new FaceDetector( picWidth, picHeight, NUM_FACES );
		arrayFaces.findFaces(sourceImage, getAllFaces);
		
		for (int i = 0; i < getAllFaces.length; i++)
		{
			getFace = getAllFaces[i];
			try {
				PointF eyesMP = new PointF();
				getFace.getMidPoint(eyesMP);
				eyesDistance[i] = getFace.eyesDistance();
				eyesMidPts[i] = eyesMP;
			}catch (Exception e){
				Log.e("Face", i + " is null");
			}
		
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		xRatio = getWidth()*1.0f / picWidth;
		yRatio = getHeight()*1.0f / picHeight;
		canvas.drawBitmap( sourceImage, null , new Rect(0,0,getWidth(),getHeight()),tmpPaint);
		for (int i = 0; i < eyesMidPts.length; i++){
			if (eyesMidPts[i] != null){
				pOuterBullsEye.setStrokeWidth(eyesDistance[i] /8);
//				canvas.drawCircle(eyesMidPts[i].x*xRatio, eyesMidPts[i].y*yRatio, eyesDistance[i] / 2 
//						, pOuterBullsEye);
//				canvas.drawCircle(eyesMidPts[i].x*xRatio, eyesMidPts[i].y*yRatio, eyesDistance[i] / 6 
//						, pInnerBullsEye);
				
				canvas.drawCircle(eyesMidPts[i].x*xRatio, eyesMidPts[i].y*yRatio, eyesDistance[i] / 6 
						, pInnerBullsEye);
				float radius = eyesDistance[i];
				canvas.drawRect(eyesMidPts[i].x - radius-radius, eyesMidPts[i].y - radius, 
						eyesMidPts[i].x + radius, eyesMidPts[i].y + radius+radius, 
						pOuterBullsEye);
				Log.e("onDraw....", "MidPoint.x="+eyesMidPts[i].x+", MidPoint.y="+eyesMidPts[i].y);
				Log.e("onDraw....", "EyeDistance="+eyesDistance[i]);
			}
		}
	}

	protected void saveFace(){
		
	}
	
	public static Bitmap rotateBitmap90(String imgPath){
		Bitmap src = BitmapFactory.decodeFile(imgPath);
		int w = src.getWidth();
		int h = src.getHeight();Log.e("BITMAP", "W= "+w+", H="+h);
		int nw = 480, nh = 600;
		float sx = nw*1.0f / w;
		float sy = nh*1.0f / h;Log.e("BITMAP", "scaleX= "+sx+", scaleY="+sy);
		
		Matrix matrix = new Matrix();
		matrix.postScale(sx, sy);
		matrix.postRotate(90);
		
		Bitmap newBm = Bitmap.createBitmap(src, 0, 0, nw, nh, matrix, false);
		w = newBm.getWidth();
		h = newBm.getHeight();Log.e("BITMAP", "NEW::W= "+w+", H="+h);
		return newBm;
	}
	
	public Bitmap extractFaceFromImage(Bitmap source, float eyeDistance, PointF eyeCenter){
		Bitmap bm;
		int originalH = source.getHeight();
		int originalW = source.getWidth();
		float radius = eyeDistance * 2;
		float baseX = eyeCenter.x - radius;
		float baseY = eyeCenter.y - radius;
		bm = Bitmap.createBitmap(source, (int)baseX, (int)baseY, 
				(int)(originalW-2*baseX), (int)(originalH-2*baseY));
		return bm;
	}
	
}

