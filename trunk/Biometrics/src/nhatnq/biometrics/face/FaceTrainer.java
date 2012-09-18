package nhatnq.biometrics.face;

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

import java.io.File;
import java.io.FileFilter;
import java.io.FilePermission;
import java.util.ArrayList;
import java.util.List;

import nhatnq.biometrics.ScreenTraining;
import nhatnq.biometrics.util.AppConst;
import nhatnq.biometrics.util.AppUtil;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
	
	private Context mBase;
	public FaceTrainer(Context context){
		mBase = context;
	}
	
	/**
	 * Start training task
	 * @param faceImages List of captured images
	 */
	public void saveFaceData(List<String> faceImages){
		new TrainingTask(faceImages).execute();
	}
	
	private List<String> createGrayscaleImages(List<String> imgs){
		List<String> list = new ArrayList<String>();
		for(String imgPath : imgs){
			String imgGrayscale = AppUtil.createGrayscaleImage(imgPath);
			list.add(imgGrayscale);
		}
		
		// Delete all resized face images in ./face folder
		File f;
		for(String s : imgs){
			f = new File(s);
			f.delete(); 
		}
		return list;
	}
	
	private List<String> createResizedImages(List<String> images){
		if(images == null) return null;
		
		List<String> list = new ArrayList<String>(images.size());
		for(String faceImgPath : images){
			BioFaceObject[] objects = BioFaceDetector.detectFaceFromImage(faceImgPath);
			if(objects == null || objects.length == 0) continue;
			
			String resizedFacePath = BioFaceDetector.createResizedFaceImageFromSource(
					BioFaceDetector.getTargetFace(objects), 
					faceImgPath);
			list.add(resizedFacePath);
		}
		return list;
	}
	
	private List<String> fetchPGM(){
		File folder = new File(AppConst.FACE_FOLDER);
		if(! folder.exists()) folder.mkdir();
		
		File[] files = folder.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().toLowerCase().endsWith(".pgm");
			}
		});
		
		List<String> list = new ArrayList<String>(files.length);
		for(File f : files){
			list.add(f.getAbsolutePath());
		}
		return list;
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
			dialog.setMessage("Processing...");
			dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			/**
			 * (1) Detect face from image
			 * (2) Create bitmap contains face
			 * (3) Resize face bitmap and store on Sdcard */
//			images = createResizedImages(images);
			
			/**
			 * (1) Create gray-scale images from resized images
			 * (2) Save face image data into facedata.xml file
			 */
//			images = createGrayscaleImages(images);
			images = fetchPGM();
			printList(images);
			
			return learn();
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if(values[0] == 1){
				dialog.setMessage("Saving...");
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dialog.dismiss();
		}

	    public boolean learn() {
			trainingFaceImgArr = loadFaceImgArray(images, true);
			nTrainFaces = trainingFaceImgArr.length;

			if (nTrainFaces < ScreenTraining.MIN_FACE_IMAGE_CAPTURED) {
				return false;
			}

			doPCA();

			// project the training images onto the PCA subspace
			projectedTrainFaceMat = cvCreateMat(nTrainFaces,
					nEigens,
					CV_32FC1); // 32-bit float, 1 channel

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
				 */
				cvEigenDecomposite(trainingFaceImgArr[i],
						nEigens,
						new PointerPointer(eigenVectArr),
						0, // ioFlags
						null, // userData (Pointer)
						pAvgTrainImg, 
						floatPointer); // coeffs (FloatPointer)

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
		 * and the eigenfaces that represent any image in the given dataset.
		 */
		private void doPCA() {
			CvTermCriteria calcLimit;
			CvSize faceImgSize = new CvSize();

			nEigens = nTrainFaces - 1;
			
			faceImgSize.width(trainingFaceImgArr[0].width());
			faceImgSize.height(trainingFaceImgArr[0].height());
			eigenVectArr = new IplImage[nEigens];
			for (int i = 0; i < nEigens; i++) {
				eigenVectArr[i] = cvCreateImage(faceImgSize,
						IPL_DEPTH_32F, // depth
						1); // channels
			}

			// 1 row, n columns
			eigenValMat = cvCreateMat(1, nEigens, CV_32FC1); // 32-bit float, 1 channel

			pAvgTrainImg = cvCreateImage(faceImgSize, 
					IPL_DEPTH_32F, // depth
					1); // channels

			/**
			 * calcLimit
						Criteria that determine when to stop calculation of eigen objects.
			 */
			calcLimit = cvTermCriteria(CV_TERMCRIT_ITER, // type
					nEigens, // max_iter
					1); // epsilon

			/**
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
			 */

			cvCalcEigenObjects(nTrainFaces,
					new PointerPointer(trainingFaceImgArr), // input
					new PointerPointer(eigenVectArr), // output
					CV_EIGOBJ_NO_CALLBACK, // ioFlags
					0, // ioBufSize - unknown buffer size if 0
					null, // userData
					calcLimit, pAvgTrainImg,
					eigenValMat.data_fl());

			/**
			 * CV_C - the C-norm (maximum of absolute values) of the array is normalized. 
			 * CV_L1 - the L1-norm (sum of absolute values) of the array is normalized. 
			 * CV_L2 - the (Euclidian) L2-norm of the array is normalized. 
			 * CV_MINMAX - the array values are scaled and shifted to the specified range. 
			 */
			cvNormalize(eigenValMat,eigenValMat,1,0,
					CV_L1, // norm_type
					null); // mask
		}
	    
	    /** Stores the training data to the file 'data/facedata.xml'. */
		private void storeTrainingData() {
			printLog("storeTrainingData()");
			
			CvFileStorage fileStorage;
			fileStorage = cvOpenFileStorage(AppConst.FACE_FOLDER + "/facedata.xml",
					null, // memstorage
					CV_STORAGE_WRITE, // flags
					null); // encoding
			
			// Number of persons
//			cvWriteInt(fileStorage, "nPersons", nPersons); 
//
//			for (int i = 0; i < nPersons; i++) {
//				String varname = "personName_" + (i + 1);
//				cvWriteString(fileStorage, 
//						varname,
//						personNames.get(i) + "",
//						0); // quote
//			}
			
			// Number of eigen vectors
			cvWriteInt(fileStorage, "nEigens", nEigens); 
			// Number of trained faces
			cvWriteInt(fileStorage, "nTrainFaces", nTrainFaces); 
			// Matrix [1, nTrainFaces]
//			cvWrite(fileStorage, "trainPersonNumMat", personNumTruthMat, cvAttrList()); 
			// Matrix [1, nEigens]
			cvWrite(fileStorage, "eigenValMat", eigenValMat, cvAttrList()); 
			// Matrix [nTrainFaces, nEigens]
			cvWrite(fileStorage, "projectedTrainFaceMat", projectedTrainFaceMat, cvAttrList()); 
			// An average image
			cvWrite(fileStorage, "avgTrainImg", pAvgTrainImg, cvAttrList());

			for (int i = 0; i < nEigens; i++) {
				String varname = "eigenVect_" + i;
				cvWrite(fileStorage, varname, eigenVectArr[i], cvAttrList()); 
			}

			cvReleaseFileStorage(fileStorage);
		}
		
		/** 
		 * Saves all the eigen-vectors as images, 
		 * so that they can be checked. 
		 */
		private void storeEigenfaceImages() {
			printLog("storeEigenfaceImages()");
			
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
					cvSetImageROI(bigImg, ROI);// Put image into ROI-area
					cvCopy(byteImg, // src
							bigImg, // dst
							null); // mask
					cvResetImageROI(bigImg);
					cvReleaseImage(byteImg);
				}
				cvSaveImage(AppConst.FACE_FOLDER + "/out_eigenfaces.bmp", bigImg);
				cvReleaseImage(bigImg);
			}
		}
		
		/**
		 * Converts the given float image to an unsigned character image.
		 * 
		 * @param srcImg
		 *            the given float image
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
	
	/**
	 * Create array of IplImage from list of image paths
	 * @param images
	 * @param training boolean True if in training mode, false if recognizing mode
	 * @return Array of gray-scale faces
	 */
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
	
	private void printLog(String log){
		Log.e(TAG, log);
	}
	
	void printList(List<String> list){
		for(String s : list){
			printLog("List: "+s);
		}
	}
}
