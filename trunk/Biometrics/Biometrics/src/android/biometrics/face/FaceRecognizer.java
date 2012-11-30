package android.biometrics.face;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_32SC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_STORAGE_READ;
import static com.googlecode.javacv.cpp.opencv_core.cvAttrList;
import static com.googlecode.javacv.cpp.opencv_core.cvOpenFileStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvReadByName;
import static com.googlecode.javacv.cpp.opencv_core.cvReadIntByName;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseFileStorage;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_legacy.cvEigenDecomposite;

import java.io.File;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.biometrics.R;
import android.biometrics.ScreenFaceRecognizing;
import android.biometrics.util.AppConst;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.javacpp.FloatPointer;
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacpp.PointerPointer;
import com.googlecode.javacv.cpp.opencv_core.CvFileStorage;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class FaceRecognizer {
	private static final String TAG = FaceRecognizer.class.getCanonicalName();
	public static String recognizingImagePath;

	/** the test face image array */
	// IplImage[] testFaceImgArr;
	/** the person number array */
	// CvMat personNumTruthMat;
	/** the number of persons */
	// int nPersons;
	/** the person names */
	// List<String> personNames = new ArrayList<String>();

	/** the number of eigenvalues */
	int nEigens = 0;
	/** the number of training faces */
	private int nTrainFaces = 0;
	/** eigen-vectors */
	IplImage[] eigenVectArr;
	/** eigenvalues */
	CvMat eigenValMat;
	/** the average image */
	IplImage pAvgTrainImg;
	/** the projected training faces */
	CvMat projectedTrainFaceMat;
	
	enum Distance{
		Euclidian, Mahalanobis
	}
	private Activity mBase;

	public FaceRecognizer(Activity base) {
		mBase = base;
	}

	public void recognize(String imagePath) {
		new RecognizingTask().execute(imagePath);
	}

	private class RecognizingTask extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog dialog;
		private String faceObjectPath;
		private String srcImagePath;
		private float resultConfidence;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(mBase);
			dialog.setMessage(mBase.getString(R.string.txt_recognizing));
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			srcImagePath = params[0];
			
			/** faceObjectPath: path of image that just contains detected face
			 * 					and it was resized. 
			 */
			faceObjectPath = FaceHelper.preProcessFaceImage(srcImagePath);
			if(faceObjectPath == null){
				Log.e(TAG, "@Error when processing face image at "+srcImagePath);
				return null;
			}

			recognizingImagePath = faceObjectPath;
			return recognizeFace();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
			
			TextView tv = (TextView)mBase.findViewById(R.id.tvStatus);
			DecimalFormat df = new DecimalFormat("#.####");
			tv.setText(mBase.getString(R.string.recognize_face_confidence, ""+df.format(resultConfidence)));
			
			Toast toast = Toast.makeText(mBase, null, Toast.LENGTH_LONG);
			LayoutInflater inflater = LayoutInflater.from(mBase);
			View view = inflater.inflate(R.layout.view_toast_result, null);
			tv = (TextView)view.findViewById(R.id.text);
			if(result == null){
				view.setBackgroundColor(Color.rgb(221, 0, 0));
				tv.setText(mBase.getString(R.string.toast_can_not_detect_face));
			}else if(result){
				view.setBackgroundColor(Color.rgb(0, 221, 119));
				tv.setText(mBase.getString(R.string.recognize_success));
			}else{
				view.setBackgroundColor(Color.rgb(221, 0, 0));
				tv.setText(mBase.getString(R.string.recognize_failed));
			}
			
			toast.setView(view);
			toast.show();
			
			/**
			 * Delete all face, because we do not need anymore
			 */
			File f;
			f = new File(srcImagePath);
			f.delete();
			f = new File(faceObjectPath);
			f.delete();
		}

		public boolean recognizeFace() {
			IplImage faceObjectImage = cvLoadImage(faceObjectPath, CV_LOAD_IMAGE_GRAYSCALE);
			FloatPointer projectedTestFace = new FloatPointer(nEigens);
			
			loadTrainingData();
			
			cvEigenDecomposite(faceObjectImage, 
					nEigens, 
					new PointerPointer(eigenVectArr), 
					0,
					null,
					pAvgTrainImg, 
					projectedTestFace);

			float confidence = 0.0f;
			final FloatPointer pConfidence = new FloatPointer(confidence);
			int iNearest = findNearestNeighbor(projectedTestFace, 
					new FloatPointer(pConfidence), Distance.Mahalanobis);

			resultConfidence = pConfidence.get();
//			int nearest = trainPersonNumMat.data_i().get(iNearest);

			printLog("Confidence = " + resultConfidence);
			printLog("Confidence threshold = " + ScreenFaceRecognizing.threshold);

			return resultConfidence >= ScreenFaceRecognizing.threshold;
		}

		/**
		 * Opens the training data from the file 'data/facedata.xml'.
		 * 
		 * @param pTrainPersonNumMat
		 * @return the person numbers during training, or null if not successful
		 */
		private CvMat loadTrainingData() {
			CvMat pTrainPersonNumMat = null;
			CvFileStorage fileStorage;
			int i;

			fileStorage = cvOpenFileStorage(
					AppConst.FACE_DATA_FILE_PATH, 
					null, CV_STORAGE_READ, null);
			if (fileStorage == null) {
				printLog(".loadTrainingData():: Cannot open database file '/facedata.xml'");
				return null;
			}

			nEigens = cvReadIntByName(fileStorage, null, "nEigens", 0);
			nTrainFaces = cvReadIntByName(fileStorage, null, "nTrainFaces", 0);

			Pointer pointer;
//			pointer = cvReadByName(fileStorage, null, "trainPersonNumMat", cvAttrList());
//			pTrainPersonNumMat = new CvMat(pointer);

			pointer = cvReadByName(fileStorage, null, "eigenValMat", cvAttrList());
			eigenValMat = new CvMat(pointer);

			pointer = cvReadByName(fileStorage, null, "projectedTrainFaceMat", cvAttrList());
			projectedTrainFaceMat = new CvMat(pointer);

			pointer = cvReadByName(fileStorage, null, "avgTrainImg", cvAttrList());
			pAvgTrainImg = new IplImage(pointer);
			
			eigenVectArr = new IplImage[nTrainFaces];
			for (i = 0; i < nEigens; i++) {
				String varname = "eigenVect_" + i;
				pointer = cvReadByName(fileStorage, null, varname, cvAttrList());
				eigenVectArr[i] = new IplImage(pointer);
			}

			cvReleaseFileStorage(fileStorage);

			return pTrainPersonNumMat;
		}

		/**
		 * Find the most likely person based on a detection. Returns the index,
		 * and stores the confidence value into pConfidence.
		 * 
		 * @param projectedTestFace
		 *            the projected test face
		 * @param pConfidencePointer
		 *            a pointer containing the confidence value
		 * @param iTestFace
		 *            the test face index
		 * @return the index
		 */
		private int findNearestNeighbor(FloatPointer projectedTestFace,
				FloatPointer pConfidencePointer, Distance distanceFormula) {
			double leastDistSq = Double.MAX_VALUE;
			int iNearest = 0;

			for (int iTrain = 0; iTrain < nTrainFaces; iTrain++) {
				double distSq = 0;

				for (int i = 0; i < nEigens; i++) {
					float projectedTrainFaceDistance = (float) projectedTrainFaceMat
							.get(iTrain, i);
					float d_i = projectedTestFace.get(i) - projectedTrainFaceDistance;
					if(distanceFormula == Distance.Euclidian){
						distSq += d_i * d_i;
					}else if(distanceFormula == Distance.Mahalanobis){
						distSq += d_i * d_i / eigenValMat.data_fl().get(i);
					}
				}
				
				if (distSq < leastDistSq) {
					leastDistSq = distSq;
					iNearest = iTrain;
				}
			}

			float pConfidence = (float) (1.0f - 
					Math.sqrt(leastDistSq / (nTrainFaces * nEigens)) / 255.0f);		
			pConfidencePointer.put(pConfidence);

			return iNearest;
		}

	}

	/**
	 * Get a string representation of the given float pointer.
	 * 
	 * @param floatPointer the given float pointer
	 * @return a string representation of the given float pointer
	 */
	private String floatPointerToString(final FloatPointer floatPointer) {
		final StringBuilder stringBuilder = new StringBuilder();
		boolean isFirst = true;
		stringBuilder.append('[');
		for (int i = 0; i < floatPointer.capacity(); i++) {
			if (isFirst) {
				isFirst = false;
			} else {
				stringBuilder.append(", ");
			}
			stringBuilder.append(floatPointer.get(i));
		}
		stringBuilder.append(']');

		return stringBuilder.toString();
	}

	/**
	 * Returns a string representation of the given one-channel CvMat object.
	 * 
	 * @param cvMat the given CvMat object
	 * @return a string representation of the given CvMat object
	 */
	private String oneChannelCvMatToString(final CvMat cvMat) {
		if (cvMat.channels() != 1) {
			throw new RuntimeException(
					"illegal argument - CvMat must have one channel");
		}

		final int type = cvMat.maskedType();
		StringBuilder s = new StringBuilder("[ ");
		for (int i = 0; i < cvMat.rows(); i++) {
			for (int j = 0; j < cvMat.cols(); j++) {
				if (type == CV_32FC1 || type == CV_32SC1) {
					s.append(cvMat.get(i, j));
				} else {
					throw new RuntimeException(
							"illegal argument - CvMat must have one channel and type of float or signed integer");
				}
				if (j < cvMat.cols() - 1) {
					s.append(", ");
				}
			}
			if (i < cvMat.rows() - 1) {
				s.append("\n  ");
			}
		}
		s.append(" ]");
		return s.toString();
	}

	private void printLog(String log) {
		Log.e(TAG, log);
	}
}
