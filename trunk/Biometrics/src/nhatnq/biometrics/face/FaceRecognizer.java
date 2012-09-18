package nhatnq.biometrics.face;

import static com.googlecode.javacv.cpp.opencv_core.CV_STORAGE_READ;
import static com.googlecode.javacv.cpp.opencv_core.cvAttrList;
import static com.googlecode.javacv.cpp.opencv_core.cvOpenFileStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvReadByName;
import static com.googlecode.javacv.cpp.opencv_core.cvReadIntByName;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseFileStorage;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_legacy.cvEigenDecomposite;

import java.util.List;

import nhatnq.biometrics.ScreenRecognizing;
import nhatnq.biometrics.util.AppConst;
import nhatnq.biometrics.util.AppUtil;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.javacpp.FloatPointer;
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacpp.PointerPointer;
import com.googlecode.javacv.cpp.opencv_core.CvFileStorage;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_32SC1;
//import static com.googlecode.javacv.cpp.opencv_highgui.*;
//import static com.googlecode.javacv.cpp.opencv_legacy.*;

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

	private Context mBase;

	public FaceRecognizer(Context base) {
		mBase = base;
	}

	public void recognizeObjectByFace(String faceImage) {
		new RecognizingTask().execute(faceImage);
	}

	/**
	 * Create array of IplImage from list of image paths
	 * 
	 * @param images
	 * @param training
	 *            boolean True if in training mode, false if recognizing mode
	 * @return Array of gray-scale faces
	 */
	private IplImage[] loadFaceImgArray(List<String> images, boolean training) {
		if (images == null || images.size() == 0)
			return null;

		IplImage[] faceImgArr;
		int nFaces = images.size();
		faceImgArr = new IplImage[nFaces];

		// personNumTruthMat = cvCreateMat(1, nFaces, CV_32SC1); //32-bit
		// unsigned, one channel
		// for (int j1 = 0; j1 < nFaces; j1++) {
		// personNumTruthMat.put(0, j1, 0);
		// }

		// personNames.clear();
		// nPersons = 0;

		for (int index = 0; index < images.size(); index++) {
			// String sPersonName;

			// Check if a new person is being loaded.
			// if (! findPersonByName(personName)) {
			// personNames.add(sPersonName);
			// nPersons ++;
			// printLog(".loadFaceImgArray(): New person -> " + sPersonName +
			// ", nPersons = "
			// + nPersons + "[" + personNames.size()+"]");
			// }else
			// printLog(".loadFaceImgArray(): Old person: ___");
			//
			// personNumTruthMat.put(0, index, 1);
			faceImgArr[index] = cvLoadImage(images.get(index),
					CV_LOAD_IMAGE_GRAYSCALE);
		}

		return faceImgArr;
	}

	private class RecognizingTask extends AsyncTask<String, Void, Boolean> {

		private ProgressDialog dialog;
		private String faceObjectPath, srcImagePath, resizedImagePath;
		private IplImage faceObjectImage;
		private float resultConfidence;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(mBase);
			dialog.setMessage("Processing...");
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			srcImagePath = params[0];

//			BioFaceObject[] objects = BioFaceDetector
//					.detectFaceFromImage(srcImagePath);
//			if (objects == null || objects.length == 0) {
//				return null;
//			}
//
//			resizedImagePath = BioFaceDetector
//					.createResizedFaceImageFromSource(
//							BioFaceDetector.getTargetFace(objects),
//							srcImagePath);
//			faceObjectPath = AppUtil.createGrayscaleImage(resizedImagePath);

			faceObjectPath = srcImagePath;

			/**
			 * For displaying gray-scale
			 **/
			recognizingImagePath = faceObjectPath;
			Log.e(TAG, ".recognizingImage for gray-scale highlight -> "
					+ recognizingImagePath);
			return recognizeFace();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
			String res;
			if (result == null) {
				res = "Can not detect your face in this image, try again...";
			} else if (result) {
				res = "Welcome";
			} else {
				res = "Anonymous guy !!!!!!!!!!! CONFIDENCE is "
						+ resultConfidence;
			}
			Toast.makeText(mBase, res, Toast.LENGTH_LONG).show();

			// File f = new File(srcImagePath);
			// f.delete();
			// f = new File(resizedImagePath);
			// f.delete();
		}

		public boolean recognizeFace() {
			float[] projectedTestFace;
			float confidence = 0.0f;

			faceObjectImage = cvLoadImage(faceObjectPath,
					CV_LOAD_IMAGE_GRAYSCALE);

			CvMat trainPersonNumMat = loadTrainingData();
			if (trainPersonNumMat == null) {// null now
			// return false;
			}

			projectedTestFace = new float[nEigens];
			// double timeFaceRecognizeStart = (double) cvGetTickCount();

			int iNearest;
			cvEigenDecomposite(faceObjectImage, nEigens, new PointerPointer(
					eigenVectArr), 0, // ioFlags
					null, // userData
					pAvgTrainImg, projectedTestFace); // coeffs

			final FloatPointer pConfidence = new FloatPointer(confidence);
//			iNearest = findNearestNeighbor(projectedTestFace, new FloatPointer(
//					pConfidence));
			iNearest = findNearestNeighborViaMahalanobisDistance(projectedTestFace,
					new FloatPointer(pConfidence));
			confidence = pConfidence.get();

			printLog("Confidence = " + confidence);
			printLog("Confidence threshold = " + ScreenRecognizing.threshold);
			resultConfidence = confidence;

			return confidence >= ScreenRecognizing.threshold;
		}

		/**
		 * Opens the training data from the file 'data/facedata.xml'.
		 * 
		 * @param pTrainPersonNumMat
		 * @return the person numbers during training, or null if not successful
		 */
		private CvMat loadTrainingData() {
			printLog(".loadTrainingData()");

			CvMat pTrainPersonNumMat = null; // the person numbers during
												// training
			CvFileStorage fileStorage;
			int i;

			fileStorage = cvOpenFileStorage(AppConst.FACE_FOLDER
					+ "/facedata.xml", null, // memstorage
					CV_STORAGE_READ, null); // encoding
			if (fileStorage == null) {
				printLog(".loadTrainingData():: Cannot open database file '/facedata.xml'");
				return null;
			}

			nEigens = cvReadIntByName(fileStorage, null, "nEigens", 0);
			nTrainFaces = cvReadIntByName(fileStorage, null, "nTrainFaces", 0);

			Pointer pointer;
			// pointer = cvReadByName(fileStorage, null, "trainPersonNumMat",
			// cvAttrList());
			// pTrainPersonNumMat = new CvMat(pointer);

			pointer = cvReadByName(fileStorage, null, "eigenValMat",
					cvAttrList());
			eigenValMat = new CvMat(pointer);

			pointer = cvReadByName(fileStorage, null, "projectedTrainFaceMat",
					cvAttrList());
			projectedTrainFaceMat = new CvMat(pointer);

			pointer = cvReadByName(fileStorage, null, "avgTrainImg",
					cvAttrList());
			pAvgTrainImg = new IplImage(pointer);
			
			 eigenVectArr = new IplImage[nTrainFaces];
			// TODO: Change here 11/9
//			eigenVectArr = new IplImage[nEigens];
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
		private int findNearestNeighbor(float projectedTestFace[],
				FloatPointer pConfidencePointer) {
			printLog(".findNearestNeighbor");

			double leastDistSq = Double.MAX_VALUE;
			int iNearest = 0;

			for (int iTrain = 0; iTrain < nTrainFaces; iTrain++) {
				double distSq = 0;

				for (int i = 0; i < nEigens; i++) {
					float projectedTrainFaceDistance = (float) projectedTrainFaceMat
							.get(iTrain, i);
					float d_i = projectedTestFace[i]
							- projectedTrainFaceDistance;
					printLog("--- projectedTrainFaceMat.get(" + iTrain + "," + i
							+ ")=" + projectedTrainFaceDistance);
					printLog("--- projectedTestFace[i]="+projectedTestFace[i]
							+", projectedTrainFaceDistance="+projectedTrainFaceDistance);
					
					printLog(".mini distance = " + d_i);
					distSq += d_i * d_i; // eigenValMat.data_fl().get(i);
				}

				printLog(".Distance at iTrain = " + iTrain + " is " + distSq);
				printLog(".Least distance = " + leastDistSq);
				if (distSq < leastDistSq) {
					leastDistSq = distSq;
					iNearest = iTrain;
				}
			}

			printLog(".SQRT="
					+ Math.sqrt(leastDistSq / (float) (nTrainFaces * nEigens)));
			printLog(".nTrainFaces = " + nTrainFaces + ", nEigens=" + nEigens);
			float pConfidence = (float) (1.0f - Math.sqrt(leastDistSq
					/ (float) (nTrainFaces * nEigens)) / 255.0f);
			pConfidencePointer.put(pConfidence);

			printLog(".findNearestNeighbor: end, return " + iNearest);
			return iNearest;
		}

	}
	
	private int findNearestNeighborViaMahalanobisDistance(float projectedTestFace[],
			FloatPointer pConfidencePointer) {
		printLog(".findNearestNeighborViaMahalanobisDistance");

		double leastDistSq = Double.MAX_VALUE;
		int iNearest = 0;

		for (int iTrain = 0; iTrain < nTrainFaces; iTrain++) {
			double distSqEuclid = 0, distSqHamalanobis = 0;

			for (int i = 0; i < nEigens; i++) {
				float projectedTrainFaceDistance = (float) projectedTrainFaceMat
						.get(iTrain, i);
				float eigenValue = (float) eigenValMat.get(0, i);
				float d_i = projectedTestFace[i]
						- projectedTrainFaceDistance;
				printLog("--- projectedTrainFaceMat.get(" + iTrain + "," + i
						+ ")=" + projectedTrainFaceDistance);
				printLog("--- projectedTestFace[i]="+projectedTestFace[i]
						+", projectedTrainFaceDistance="+projectedTrainFaceDistance);
				printLog("--- eigenValue = "+eigenValue);
				
				distSqEuclid += d_i * d_i; // eigenValMat.data_fl().get(i);
				printLog(".mini distance Eulidean = " + d_i*d_i);
				printLog("==> currentE="+distSqEuclid);
				
				d_i = projectedTestFace[i] * projectedTrainFaceDistance / ((float)Math.sqrt(eigenValue));
				printLog(".mini distance Hamalanobis = " + d_i);
				distSqHamalanobis += d_i;
				printLog("==> currentH="+distSqHamalanobis);
			}
			distSqHamalanobis = Math.abs(distSqHamalanobis);
			
			printLog(".Distance at iTrain = " + iTrain + ", E=" + distSqEuclid);
			printLog(".Distance at iTrain = " + iTrain + ", H=" +distSqHamalanobis);
			printLog(".Least distance = " + leastDistSq);
			if (distSqHamalanobis < leastDistSq) {
				leastDistSq = distSqHamalanobis;
				iNearest = iTrain;
			}
		}

		printLog(".LEAST DISTANCE is "+leastDistSq);
		printLog(".SQRT="
				+ Math.sqrt(leastDistSq / (float) (nTrainFaces * nEigens)));

		float pConfidence = (float) (1.0f - Math.sqrt(leastDistSq
				/ (float) (nTrainFaces * nEigens)) / 255.0f);
		pConfidencePointer.put(pConfidence);

		printLog(".findNearestNeighbor: end, return " + iNearest);
		return iNearest;
	}
	

	/**
	 * Returns a string representation of the given float pointer.
	 * 
	 * @param floatPointer
	 *            the given float pointer
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
	 * @param cvMat
	 *            the given CvMat object
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
