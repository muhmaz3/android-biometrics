package android.biometrics.face;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_L1;
import static com.googlecode.javacv.cpp.opencv_core.CV_STORAGE_WRITE;
import static com.googlecode.javacv.cpp.opencv_core.CV_TERMCRIT_ITER;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvAttrList;
import static com.googlecode.javacv.cpp.opencv_core.cvConvertScale;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMat;
import static com.googlecode.javacv.cpp.opencv_core.cvMinMaxLoc;
import static com.googlecode.javacv.cpp.opencv_core.cvNormalize;
import static com.googlecode.javacv.cpp.opencv_core.cvOpenFileStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvRect;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseFileStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_core.cvTermCriteria;
import static com.googlecode.javacv.cpp.opencv_core.cvWrite;
import static com.googlecode.javacv.cpp.opencv_core.cvWriteInt;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_legacy.CV_EIGOBJ_NO_CALLBACK;
import static com.googlecode.javacv.cpp.opencv_legacy.cvCalcEigenObjects;
import static com.googlecode.javacv.cpp.opencv_legacy.cvEigenDecomposite;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.biometrics.R;
import android.biometrics.ScreenFaceTraining;
import android.biometrics.ScreenVoiceTraining;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.content.Intent;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.googlecode.javacpp.FloatPointer;
import com.googlecode.javacpp.PointerPointer;
import com.googlecode.javacv.cpp.opencv_core.CvFileStorage;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.CvTermCriteria;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class FaceTrainer {
	private static final String TAG = FaceTrainer.class.getCanonicalName();
	
	/** the number of training faces */
	private int nTrainFaces = 0;
	/** the training face image array */
	IplImage[] trainingFaceImgArr;
	/** the number of eigenvalues */
	int nEigens = 0;
	/** eigen-vectors */
	IplImage[] eigenVectArr;
	/** eigenvalues */
	CvMat eigenValMat;
	/** the average image */
	IplImage pAvgTrainImg;
	/** the projected training faces */
	CvMat projectedTrainFaceMat;
	
	private Activity mBase;
	

	public FaceTrainer(Activity context){
		mBase = context;
	}
	
	/**
	 * Main method for training from image files
	 * @param faceImages List of image file paths in application folder
	 */
	public void train(List<String> faceImages){
		new TrainingTask(faceImages).execute();
	}
	
	/**
	 * Image pre-processing for all images in folder
	 * @param list List of image files
	 * @return List of processed image files
	 */
	private static List<String> processOriginalImages(List<String> list){
		if(list == null || list.size()==0) return null;
		
		printLog(".processOriginalImages with " + list.size() +" images");
		String pgmPath;
		List<String> finalPaths = new ArrayList<String>();
		for(String singlePath : list){
			pgmPath = FaceHelper.preProcessFaceImage(singlePath);
			if(pgmPath != null) finalPaths.add(pgmPath);
			else Log.e(TAG, "@ Error when processing face image at "+singlePath);
		}
		
		return finalPaths;
	}
		
	private class TrainingTask extends AsyncTask<String, Integer, Boolean>{
    	List<String> images;
		private ProgressDialog dialog;
    	
		public TrainingTask(List<String> list){
			images = list;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(mBase);
			dialog.setMessage(mBase.getString(R.string.txt_processing));
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(String... arg0) {	
			//TODO remove block comment to run 
/*			images = processOriginalImages(images);
			if(images == null || images.size() == 0){
				printLog("Problem when extracting face from images.");
				return false;
			}
			
			return learn();
			
*/			return true;
			}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if(values[0] == 1){
				dialog.setMessage(mBase.getString(R.string.txt_saving));
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
			if(! result){
				Toast.makeText(mBase, mBase.getString(R.string.train_failed), 
						Toast.LENGTH_LONG).show();
			}else{
				AppUtil.savePreference(mBase, AppConst.KEY_FACE_TRAINED, true);
				Intent intent;
				intent = new Intent(mBase, ScreenVoiceTraining.class);
				mBase.startActivityForResult(intent, AppConst.REQ_TRAIN_VOICE);
			}

			mBase.finish();
		}

	    public boolean learn() {
			trainingFaceImgArr = loadFaceImgArray(images, true);
			nTrainFaces = trainingFaceImgArr.length;

			if (nTrainFaces < AppConst.MIN_FACE_IMAGE_CAPTURED) {
				return false;
			}

			doPCA();

			// project the training images onto the PCA subspace
			projectedTrainFaceMat = cvCreateMat(nTrainFaces,
					nEigens,
					CV_32FC1);

			for (int i1 = 0; i1 < nTrainFaces; i1++) {
				for (int j1 = 0; j1 < nEigens; j1++) {
					projectedTrainFaceMat.put(i1, j1, 0.0);
				}
			}

			final FloatPointer floatPointer = new FloatPointer(nEigens);
			for (int i = 0; i < nTrainFaces; i++) {
				/**
				 * The function cvEigenDecomposite calculates all decomposition coefficients 
				 * for the input object using the previously calculated eigen objects basis 
				 * and the averaged object. Depending on ioFlags parameter it may be used either 
				 * in direct access or callback mode.
				 * 
				 * + eigenVectArr: we got this in PCA
				 * + pAvgTrainImg: we got this in PCA
				 * + projectedTrainFaceMat: we will get its value after this method
				 */
				cvEigenDecomposite(trainingFaceImgArr[i],
						nEigens,
						new PointerPointer(eigenVectArr),
						0,
						null,
						pAvgTrainImg, 
						floatPointer);
			
				for (int j1 = 0; j1 < nEigens; j1++) {
					projectedTrainFaceMat.put(i, j1, floatPointer.get(j1));
				}
			}
			
			publishProgress(1);

			storeTrainingData();

			storeEigenfaceImages();
			
			return true;
		}
	    
	    /**
		 * Does the Principal Component Analysis, finding the average image 
		 * and the eigen-faces that represent any image in the given dataset.
		 */
		private void doPCA() {
			CvTermCriteria calcLimit;
			CvSize faceImgSize = new CvSize();

			nEigens = nTrainFaces - 1;
			
			faceImgSize.width(trainingFaceImgArr[0].width());
			faceImgSize.height(trainingFaceImgArr[0].height());
			eigenVectArr = new IplImage[nEigens];
			
			for (int i = 0; i < nEigens; i++) {
				eigenVectArr[i] = cvCreateImage(faceImgSize, IPL_DEPTH_32F, 1);
			}

			eigenValMat = cvCreateMat(1, nEigens, CV_32FC1);
			pAvgTrainImg = cvCreateImage(faceImgSize, IPL_DEPTH_32F, 1);

			/**
			 * calcLimit Criteria that determine when to stop calculation of eigen objects.
			 */
			calcLimit = cvTermCriteria(CV_TERMCRIT_ITER, nEigens, 1);
		
			/**
			 * Read [3] for more details
			 * After calling this, we receive:
			 * + eigenVectArr: contains an array of floating-point image
			 * + pAvgTrainImg: average image from training faces
			 * + eigenValMat: matrix of eigen values
			 */
			cvCalcEigenObjects(nTrainFaces,
					trainingFaceImgArr,
					eigenVectArr,
					CV_EIGOBJ_NO_CALLBACK,
					0,
					null,
					calcLimit, 
					pAvgTrainImg,
					eigenValMat.data_fl());
			
			// Normalize every row in matrix here
			cvNormalize(eigenValMat, eigenValMat, 
					128*Math.sqrt(nEigens+1), 
					(-1)*128*Math.sqrt(nEigens+1), CV_L1, null);
		}
	    
	    /** Stores the training data to the file '/face_data.xml'. */
		private void storeTrainingData() {
			CvFileStorage fileStorage;
			fileStorage = cvOpenFileStorage(AppConst.FACE_DATA_FILE_PATH,
					null,
					CV_STORAGE_WRITE,
					null);
			
//			cvWriteInt(fileStorage, "nPersons", nPersons); 
//
//			for (int i = 0; i < nPersons; i++) {
//				String varname = "personName_" + (i + 1);
//				cvWriteString(fileStorage, 
//						varname,
//						personNames.get(i) + "",
//						0); // quote
//			}
			
			cvWriteInt(fileStorage, "nEigens", nEigens); 
			cvWriteInt(fileStorage, "nTrainFaces", nTrainFaces); 
			// Matrix [1, nTrainFaces]
//			cvWrite(fileStorage, "trainPersonNumMat", personNumTruthMat, cvAttrList()); 
			// Matrix [1, nEigens]
			cvWrite(fileStorage, "eigenValMat", eigenValMat, cvAttrList()); 
			// Matrix [nTrainFaces, nEigens]
			cvWrite(fileStorage, "projectedTrainFaceMat", projectedTrainFaceMat, cvAttrList()); 

			cvWrite(fileStorage, "avgTrainImg", pAvgTrainImg, cvAttrList());

			for (int i = 0; i < nEigens; i++) {
				String varname = "eigenVect_" + i;
				cvWrite(fileStorage, varname, eigenVectArr[i], cvAttrList()); 
			}

			cvReleaseFileStorage(fileStorage);
			
			printLog("storeTrainingData(): DONE");
		}
		
		/** 
		 * Saves all the eigen-vectors as images, just for testing, preview algorithm */
		private void storeEigenfaceImages() {
			cvSaveImage(AppConst.FACE_FOLDER + "/out_averageImage.bmp", pAvgTrainImg);

			// Create a large image made of many eigenface images.
			// Must also convert each eigenface image to a normal 8-bit UCHAR image
			// instead of a 32-bit float image.

			if (nEigens > 0) {
				int COLUMNS = 8; // Put up to 8 images on a row.
				int nCols = Math.min(nEigens, COLUMNS);
				int nRows = 1 + (nEigens / COLUMNS);
				int w = eigenVectArr[0].width();
				int h = eigenVectArr[0].height();
				CvSize size = cvSize(nCols * w, nRows * h);
				
				final IplImage bigImg = cvCreateImage(size, IPL_DEPTH_8U, 1);
				for (int i = 0; i < nEigens; i++) {
					IplImage byteImg = convertFloatImageToUcharImage(eigenVectArr[i]);

					int x = w * (i % COLUMNS);
					int y = h * (i / COLUMNS);
					CvRect ROI = cvRect(x, y, w, h);
					// Region of Interest (ROI)
					cvSetImageROI(bigImg, ROI);
					cvCopy(byteImg, bigImg, null);
					cvResetImageROI(bigImg);
					cvReleaseImage(byteImg);
				}
				cvSaveImage(AppConst.FACE_FOLDER + "/out_eigenfaces.bmp", bigImg);
				cvReleaseImage(bigImg);
				
				printLog("storeEigenfaceImages(): DONE");
			}
		}
		
		/**
		 * Converts the given float image to an unsigned character image.
		 * 
		 * @param srcImg the given float image
		 * @return the unsigned character image
		 */
		private IplImage convertFloatImageToUcharImage(IplImage srcImg) {
			IplImage dstImg;
			if ((srcImg != null) && (srcImg.width() > 0 && srcImg.height() > 0)) {
				// Spread the 32bit floating point pixels to fit within 8bit pixel
				// range.
				CvPoint minloc = new CvPoint();
				CvPoint maxloc = new CvPoint();
				double[] minVal = new double[1];
				double[] maxVal = new double[1];
				cvMinMaxLoc(srcImg, minVal, maxVal, minloc, maxloc, null);
				// Deal with NaN and extreme values, since the DFT seems to give
				// some NaN results.
				if (minVal[0] < -1e30) {
					minVal[0] = -1e30;
				}
				if (maxVal[0] > 1e30) {
					maxVal[0] = 1e30;
				}
				if (maxVal[0] - minVal[0] == 0.0f) {
					maxVal[0] = minVal[0] + 0.001; // remove potential divide by
													// zero errors.
				}
				dstImg = cvCreateImage(cvSize(srcImg.width(), srcImg.height()), 8,
						1);
				cvConvertScale(srcImg, dstImg, 255.0 / (maxVal[0] - minVal[0]),
						-minVal[0] * 255.0 / (maxVal[0] - minVal[0]));
				return dstImg;
			}
			return null;
		}
	    
	}

	private IplImage[] loadFaceImgArray(List<String> images, boolean training) {
		if(images == null || images.size() == 0) return null;
		
		IplImage[] faceImgArr;
		int nFaces = images.size();
		faceImgArr = new IplImage[nFaces];

		for (int index = 0; index < images.size(); index++) {
			faceImgArr[index] = cvLoadImage(images.get(index), CV_LOAD_IMAGE_GRAYSCALE);
		}

		return faceImgArr;
	}
	
	private static void printLog(String log){
		Log.e(TAG, log);
	}
	
	static void printList(List<String> list){
		for(String s : list){
			printLog("List: "+s);
		}
	}

	/***************************
	 * OPENCV METHOD REFERENCE
	 ***************************/
	
	/** [1]::cvCreateImage
	 * 
	   IplImage* cvCreateImage(CvSize size, int depth, int channels)
			Creates an image header and allocates the image data.
			Parameters:	
				size – Image width and height
				depth – Bit depth of image elements. See IplImage for valid depths.
				channels – Number of channels per pixel. See IplImage for details. 
					This function only creates images with interleaved channels.
	 */
	
	/** [2]::cvNormalize
	 * 
	 cvNormalize(CvMat source, CvMat dest, 
	 				double newMax, double newMin, int normType, CvArr mask)
	 * Norm type
	 * CV_C - the C-norm (maximum of absolute values) of the array is normalized. 
	 * CV_L1 - the L1-norm (sum of absolute values) of the array is normalized. 
	 * CV_L2 - the (Euclidian) L2-norm of the array is normalized. 
	 * CV_MINMAX - the array values are scaled and shifted to the specified range. 
	 */
	
	/** [3]::cvCalcEigenObjects
	 * 
	 * The function cvCalcEigenObjects calculates orthor-normal eigen basis and 
	 * the averaged object for a group of the input objects. Depending on ioFlags 
	 * parameter it may be used either in direct access or callback mode. Depending 
	 * on the parameter calcLimit, calculations are finished either after first 
	 * calcLimit.max_iter dominating eigen objects are retrieved or if the ratio of the 
	 * current eigenvalue to the largest eigenvalue comes down to calcLimit.epsilon threshold. 
	 * The value calcLimit -> type must be CV_TERMCRIT_NUMB, CV_TERMCRIT_EPS, or CV_TERMCRIT_NUMB 
	 * | CV_TERMCRIT_EPS . 
	 * The function returns the real values calcLimit->max_iter and calcLimit->epsilon .
	 * The function also calculates the averaged object, which must be created previously. Calculated 
	 * eigen objects are arranged according to the corresponding eigenvalues in the descending order.
	 * The parameter eigVals may be equal to NULL, if eigenvalues are not needed.
	 * 
	Purpose: The function calculates an orthonormal eigen basis and a mean (averaged)
			object for a group of input objects (images, vectors, etc.).
	Context:
	Parameters: nObjects  - number of source objects
  		input     - pointer either to array of input objects
  					or to read callback function (depending on ioFlags)
		output    - pointer either to output eigen objects
        or to write callback function (depending on ioFlags)
			ioFlags   - input/output flags (see Notes)
			ioBufSize - input/output buffer size
			userData  - pointer to the structure which contains all necessary
        			data for the callback functions
			calcLimit - determines the calculation finish conditions
			avg       - averaged object (has the same size as ROI)
			eigVals   - pointer to corresponding eigen values (array of <nObjects>
          			elements in descending order)
	Notes: 1. input/output data (that is, input objects and eigen ones) may either
		be allocated in the RAM or be read from/written to the HDD (or any
		other device) by read/write callback functions. It depends on the
		value of ioFlags paramater, which may be the following:
		CV_EIGOBJ_NO_CALLBACK, or 0;
		CV_EIGOBJ_INPUT_CALLBACK;
		CV_EIGOBJ_OUTPUT_CALLBACK;
		CV_EIGOBJ_BOTH_CALLBACK, or
        CV_EIGOBJ_INPUT_CALLBACK | CV_EIGOBJ_OUTPUT_CALLBACK.
		The callback functions as well as the user data structure must be
		developed by the user.
		2. If ioBufSize = 0, or it's too large, the function dermines buffer size itself.
		3. Depending on calcLimit parameter, calculations are finished either if
				eigenfaces number comes up to certain value or the relation of the
			current eigenvalue and the largest one comes down to certain value
			(or any of the above conditions takes place). The calcLimit->type value
			must be CV_TERMCRIT_NUMB, CV_TERMCRIT_EPS or
			CV_TERMCRIT_NUMB | CV_TERMCRIT_EPS. The function returns the real
			values calcLimit->max_iter and calcLimit->epsilon.
		4. eigVals may be equal to NULL (if you don't need eigen values in further).
	 */

}
