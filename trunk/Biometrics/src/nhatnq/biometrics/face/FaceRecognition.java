package nhatnq.biometrics.face;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_32SC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_L1;
import static com.googlecode.javacv.cpp.opencv_core.CV_STORAGE_READ;
import static com.googlecode.javacv.cpp.opencv_core.CV_STORAGE_WRITE;
import static com.googlecode.javacv.cpp.opencv_core.CV_TERMCRIT_ITER;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_32F;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvAttrList;
import static com.googlecode.javacv.cpp.opencv_core.cvConvertScale;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMat;
import static com.googlecode.javacv.cpp.opencv_core.cvGetTickCount;
import static com.googlecode.javacv.cpp.opencv_core.cvGetTickFrequency;
import static com.googlecode.javacv.cpp.opencv_core.cvMinMaxLoc;
import static com.googlecode.javacv.cpp.opencv_core.cvNormalize;
import static com.googlecode.javacv.cpp.opencv_core.cvOpenFileStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvReadByName;
import static com.googlecode.javacv.cpp.opencv_core.cvReadIntByName;
import static com.googlecode.javacv.cpp.opencv_core.cvReadStringByName;
import static com.googlecode.javacv.cpp.opencv_core.cvRect;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseFileStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_core.cvTermCriteria;
import static com.googlecode.javacv.cpp.opencv_core.cvWrite;
import static com.googlecode.javacv.cpp.opencv_core.cvWriteInt;
import static com.googlecode.javacv.cpp.opencv_core.cvWriteString;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_legacy.CV_EIGOBJ_NO_CALLBACK;
import static com.googlecode.javacv.cpp.opencv_legacy.cvCalcEigenObjects;
import static com.googlecode.javacv.cpp.opencv_legacy.cvEigenDecomposite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nhatnq.biometrics.ui.ScrollingTextView;
import nhatnq.biometrics.util.Const;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.javacpp.FloatPointer;
import com.googlecode.javacpp.Pointer;
import com.googlecode.javacpp.PointerPointer;
import com.googlecode.javacv.cpp.opencv_core.CvFileStorage;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.CvTermCriteria;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class FaceRecognition{
    
	private static final String TAG = FaceRecognition.class.getName();
	ArrayList<FaceItem> faces;
	int[] faceRecognitionResult;
	String testingFileName = null;
	String trainingFileName = null;
	
	enum MODE{
		TRAINING, RECOGNIZING
	}
	MODE mode;
	
	/** the number of training faces */
	private int nTrainFaces = 0;
	/** the training face image array */
	IplImage[] trainingFaceImgArr;
	/** the test face image array */
	IplImage[] testFaceImgArr;
	/** the person number array */
	CvMat personNumTruthMat;
	/** the number of persons */
	int nPersons;
	/** the person names */
	List<String> personNames = new ArrayList<String>();
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

	private File appFolder;
	
	private void init(){
		appFolder = new File( Environment.getExternalStorageDirectory() 
    			+ "/" + Const.APP_FOLDER);
        trainingFileName = appFolder.getAbsolutePath() 
        		+ "/data/javacv/training_3per_27img.txt";
	}
    
    private int collectFaces(final String filename, boolean isTraining) {
		printLog("MAIN", ":::collectFaces()["+isTraining+"] at " + filename);
		
		BufferedReader imgListFile;
		
		int nFaces = 0;
		try {  
			imgListFile = new BufferedReader(new FileReader(filename));

			while (true) {
				final String line = imgListFile.readLine();
				if (line == null || line.equals("")) {
					break;
				}
				nFaces++; 
			}
			
			imgListFile = new BufferedReader(new FileReader(filename));
			
//			if(isTraining)
//				Utils.createDirectory(appFolder, "data/preview/train");
//			else
//				Utils.createDirectory(appFolder, "data/preview/test");
			
			for (int iFace = 0; iFace < nFaces; iFace++) {
				String imgFilename;
				String personName;
				String shortImgName;
				int personNumber;

				final String line = imgListFile.readLine();
				if (line.equals("")) {
					break;
				}
				final String[] tokens = line.split(" ");
				personNumber = Integer.parseInt(tokens[0]);
				personName = tokens[1];
				imgFilename = tokens[2];
				File f = new File(imgFilename);
				shortImgName = f.getName().toLowerCase();
				f = null;
				generateRGBImage(formatNumberInHundred(iFace+1)+"-"+personNumber+"-"+shortImgName, 
						personName, imgFilename, isTraining);
			}

			imgListFile.close();

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		printLog("MAIN", ":::collectFaces()[" + isTraining +"]:: collected "+ nFaces+ " faces.");
		return nFaces;
	}
    
    private String formatNumberInHundred(int number){
    	String prefix="";
    	if(number<10)
    		prefix = "00";
    	else if(number >= 10 && number < 100)
    		prefix = "0";
    	return prefix + number;
    }
	
	private void generateRGBImage(String number, String owner, String path, boolean isTraining){
		// Load image from image path
		IplImage originalImg = cvLoadImage(path, CV_LOAD_IMAGE_GRAYSCALE);
		// Save preview image of this gray-scale
		File f = new File(path);
		String imgName = f.getName().toLowerCase().replace(".pgm", "");
		String imgSavedName;
		if(isTraining)
			imgSavedName = appFolder.getAbsolutePath() 
			+ "/data/preview/train/"+number+"_"+owner+"_"+imgName+".bmp";
		else
			imgSavedName = appFolder.getAbsolutePath() 
			+ "/data/preview/test/"+number+"_"+owner+"_"+imgName+".bmp";
		
		cvSaveImage(imgSavedName, originalImg);
		  
//		File created = new File(imgSavedName);
//		if(created.exists())
//			printLog("generateRGBImage", "Save RGB Image DONE!");
//		else
//			printLog("generateRGBImage", "Save RGB Image FAILED!");
//		int w = eigenVectArr[0].width();
//		int h = eigenVectArr[0].height();
//		CvSize size = cvSize(w, h);
//		IplImage newGrayScaleImg = cvCreateImage(size, IPL_DEPTH_8U, 1);
//		cvReleaseImage(newGrayScaleImg);
//		
//		CvSize faceImgSize = new CvSize();
//		faceImgSize.width(trainingFaceImgArr[0].width());
//		faceImgSize.height(trainingFaceImgArr[0].height());
//		eigenVectArr = new IplImage[nEigens];
//		for (int i = 0; i < nEigens; i++) {
//			eigenVectArr[i] = cvCreateImage(faceImgSize, IPL_DEPTH_32F, 1); 
//		}
	}
    
    private boolean getFacesFromPreview(boolean isTraining){  
    	printLog("MAIN", "getFacesFromPreview()["+isTraining+"]");
    	
    	if(! appFolder.exists()){
    		appFolder.mkdir();
    		printLog("getFacesFromPreview()", "make new directory ...");
    		return false;
    	}
    	
    	faces = new ArrayList<FaceItem>();
    	String containedFolder;
    	if(isTraining)
    		containedFolder = appFolder.getAbsolutePath() +"/data/preview/train";
    	else
    		containedFolder = appFolder.getAbsolutePath() +"/data/preview/test";
    	File imgFolder = new File(containedFolder);
    	if(! imgFolder.exists() || !imgFolder.isDirectory())
    		return false;
    	
    	File[] files = imgFolder.listFiles();
    	if(files == null){
    		printLog("getFacesFromPreview()", "no files found!");
    		return false;
    	}
    	
    	int size = files.length;
    	faceRecognitionResult = new int[size];
    	for(int i =0; i < size; i++)
    		faceRecognitionResult[i] = -1;
    	
    	// Down to for loop because of .listFiles() method of File class (revert order)
    	for(int i = size-1; i >= 0; i--){
    		if(! files[i].isDirectory()){
    			String[] tokens = files[i].getName().split("_");
    			FaceItem face = new FaceItem(tokens[0].split("-")[0]+"["+tokens[0].split("-")[2]+"]", 
    					tokens[1], files[i].getAbsolutePath());
    			face.result = faceRecognitionResult[i];
    			faces.add(face);
    		}
    	}
    	return true;
    }
    
    private void doTrainingProcess(){
    	new TrainingTask().execute();
    }
    
    private void doRecognitionProcess(){
    	new RecognitionTask().execute();
    }
    
    static class ViewHolder{
    	ImageView faceImage;
    	ScrollingTextView faceName;
    	TextView faceNumber;
    	ImageView faceResult;
    }
    
    class FaceItem{
    	String number;
    	String name;
    	String path;
    	int result;
    	public FaceItem(String number, String name, String path){
    		this.number = number;
    		this.name = name;
    		this.path = path;
    		result = -1;
    	}
    }
    
    private boolean findPersonByName(String name){
    	int size = personNames.size();
    	for(int i = 0; i < size; i++)
    		if(faces.get(i).name.equals(name))
    			return true;
    	return false;
    }
    
    private IplImage[] loadFaceImgArray(final String filename, boolean training) {
		printLog("MAIN", ":::loadFaceImgArray() from "+filename);
		
		IplImage[] faceImgArr;
		BufferedReader imgListFile;
		String imgFilename;
		int iFace = 0;
		int nFaces = 0;
		int i;
		try {  
			imgListFile = new BufferedReader(new FileReader(filename));

			while (true) {
				final String line = imgListFile.readLine();
				if (line == null || line.equals("")) {
					break;
				}
				nFaces++; 
			}

			imgListFile = new BufferedReader(new FileReader(filename));

			faceImgArr = new IplImage[nFaces];
			
			personNumTruthMat = cvCreateMat(1, nFaces, CV_32SC1); //32-bit unsigned, one channel
			for (int j1 = 0; j1 < nFaces; j1++) {
				personNumTruthMat.put(0, j1, 0);
			}

			personNames.clear();
			nPersons = 0;

			for (iFace = 0; iFace < nFaces; iFace++) {
				String personName;
				String sPersonName;
				int personNumber;

				// read person number (beginning with 1), their name and the
				// image filename.
				final String line = imgListFile.readLine();
				if (line.equals("")) {
					break;
				}
				final String[] tokens = line.split(" ");
				personNumber = Integer.parseInt(tokens[0]);
				personName = tokens[1];
				imgFilename = tokens[2];
				sPersonName = personName;

				// Check if a new person is being loaded.
				if (! findPersonByName(personName)) {
					personNames.add(sPersonName);
					nPersons ++;
					printLog("loadFaceImgArray()", "New person: " + sPersonName + ", nPersons = " 
								+ nPersons + "[" + personNames.size()+"]");
				}else
					printLog("loadFaceImgArray()", "Old person: ___");

				personNumTruthMat.put(0, iFace, personNumber); 
				
				faceImgArr[iFace] = cvLoadImage(imgFilename, CV_LOAD_IMAGE_GRAYSCALE);

				if (faceImgArr[iFace] == null) {
					throw new RuntimeException("Can't load image from "
							+ imgFilename);
				}
			}

			imgListFile.close();

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		printLog("loadFaceImgArray()", "Loaded " + nFaces + " face(s) from " + nPersons + " person(s).");
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("People: ");
		if (nPersons > 0) {
			stringBuilder.append("<").append(personNames.get(0)).append(">");
		}
		for (i = 1; i < nPersons && i < personNames.size(); i++) {
			stringBuilder.append(", <").append(personNames.get(i)).append(">");
		}
		printLog("loadFaceImgArray()", "Data: " + stringBuilder.toString());

		return faceImgArr;
	}
        
    private class TrainingTask extends AsyncTask<Void, Integer, Boolean>{
    	
		@Override
		protected Boolean doInBackground(Void... arg0) {
			return learn(appFolder.getAbsolutePath() + "/data/javacv/training_3per_27img.txt");
		}
		
		/**
		 * Trains from the data in the given training text index file, and store the
		 * trained data into the file 'data/facedata.xml'.
		 * 
		 * @param trainingFileName   
		 *            the given training text index file
		 */
	    public boolean learn(final String trainingFileName) {
	    	printLog("MAIN", "learn() from file '" + trainingFileName + "'");
	    	
			int i;
			trainingFaceImgArr = loadFaceImgArray(trainingFileName, true);
			nTrainFaces = trainingFaceImgArr.length;

			if (nTrainFaces < 3) {
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

//			if (nTrainFaces < 5) {
//				printLog("learn()", "projectedTrainFaceMat contents:\n" +
//							oneChannelCvMatToString(projectedTrainFaceMat));
//			}

			final FloatPointer floatPointer = new FloatPointer(nEigens);
			for (i = 0; i < nTrainFaces; i++) {
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

//				if (nTrainFaces < 5) {
//					printLog("learn()", "Float pointer: " + floatPointerToString(floatPointer)); 
//				}
				for (int j1 = 0; j1 < nEigens; j1++) {
					projectedTrainFaceMat.put(i, j1, floatPointer.get(j1));
				}
			}
//			if (nTrainFaces < 5) {
//				printLog("learn()", "projectedTrainFaceMat after cvEigenDecomposite:\n" 
//			+ projectedTrainFaceMat);
//			}

			storeTrainingData();

			storeEigenfaceImages();
			
			return true;
		}
		
	    /**
		 * Does the Principal Component Analysis, finding the average image 
		 * and the eigenfaces that represent any image in the given dataset.
		 */
		private void doPCA() {
			printLog("MAIN", "doPCA()");
			
			int i;
			CvTermCriteria calcLimit;
			CvSize faceImgSize = new CvSize();

			nEigens = nTrainFaces - 1;
			
			faceImgSize.width(trainingFaceImgArr[0].width());
			faceImgSize.height(trainingFaceImgArr[0].height());
			eigenVectArr = new IplImage[nEigens];
			for (i = 0; i < nEigens; i++) {
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
			 * The function cvCalcEigenObjects calculates orthonormal eigen basis and 
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
			printLog("doPCA()", "===cvCalcEigenObjects()");
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
			printLog("doPCA()", "===cvNormalize()");
			cvNormalize(eigenValMat,eigenValMat,1,0,CV_L1, // norm_type
							null); // mask
		}
	    
		/** Stores the training data to the file 'data/facedata.xml'. */
		private void storeTrainingData() {
			printLog("MAIN", "storeTrainingData()");
			publishProgress(2);
			
			CvFileStorage fileStorage;
			fileStorage = cvOpenFileStorage(appFolder.getAbsolutePath() + "/facedata.xml",
					null, // memstorage
					CV_STORAGE_WRITE, // flags
					null); // encoding
			
			// Number of persons
			cvWriteInt(fileStorage, "nPersons", nPersons); 

			for (int i = 0; i < nPersons; i++) {
				String varname = "personName_" + (i + 1);
				cvWriteString(fileStorage, 
						varname,
						personNames.get(i) + "",
						0); // quote
			}
			
			// Number of eigen vectors
			cvWriteInt(fileStorage, "nEigens", nEigens); 
			// Number of trained faces
			cvWriteInt(fileStorage, "nTrainFaces", nTrainFaces); 
			// Matrix [1, nTrainFaces]
			cvWrite(fileStorage, "trainPersonNumMat", personNumTruthMat, cvAttrList()); 
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
			printLog("MAIN", "storeEigenfaceImages()");
			
			cvSaveImage(appFolder.getAbsolutePath() + "/out_averageImage.bmp", pAvgTrainImg);

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
				cvSaveImage(appFolder.getAbsolutePath() + "/out_eigenfaces.bmp", bigImg);
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
    
    private class RecognitionTask extends AsyncTask<Void, Void, Boolean>{
    	
		@Override
		protected Boolean doInBackground(Void... arg0) {
	    	String fileName = testingFileName;
			return recognizeFileList(fileName);
		}
		
		/**
		 * Recognizes the face in each of the test images given, and compares the
		 * results with the truth.
		 * 
		 * @param szFileTest
		 *            the index file of test images
		 */
		public boolean recognizeFileList(final String szFileTest) {
			printLog("MAIN", "recognizeFileList()");
			
			int i = 0;
			int nTestFaces = 0; 
			CvMat trainPersonNumMat; // the person numbers during training
			float[] projectedTestFace;
			int result = -1;
			int nCorrect = 0;
			int nWrong = 0;
			double timeFaceRecognizeStart;
			double tallyFaceRecognizeTime;
			float confidence = 0.0f;

			testFaceImgArr = loadFaceImgArray(szFileTest, false);
			nTestFaces = testFaceImgArr.length;

			trainPersonNumMat = loadTrainingData();
			if (trainPersonNumMat == null) {
				return false;
			}

			projectedTestFace = new float[nEigens];
			timeFaceRecognizeStart = (double) cvGetTickCount(); 
			
			faceRecognitionResult = new int[nTestFaces];
			for (i = 0; i < nTestFaces; i++) {
				int iNearest;
				int nearest;
				int truth;

				cvEigenDecomposite(testFaceImgArr[i], 
						nEigens, 
						new PointerPointer(eigenVectArr),
						0, // ioFlags
						null, // userData
						pAvgTrainImg, 
						projectedTestFace); // coeffs

//				printLog("recognizeFileList()", "ProjectedTestFace = " + floatArrayToString(projectedTestFace));

				final FloatPointer pConfidence = new FloatPointer(confidence);
				iNearest = findNearestNeighbor(projectedTestFace, new FloatPointer(
						pConfidence));
				confidence = pConfidence.get();
				truth = personNumTruthMat.data_i().get(i);
				nearest = trainPersonNumMat.data_i().get(iNearest);
				printLog("recognizeFileList()", "iNearest="+iNearest+", confidence="
						+confidence+",truth="+truth+",nearest="+nearest);
				if (nearest == truth) {
					result = 1;
					nCorrect++;
				} else {
					result = 0;
					nWrong++;
				}
				faceRecognitionResult[i] = result;
			}
			tallyFaceRecognizeTime = (double) cvGetTickCount()
					- timeFaceRecognizeStart;
			if (nCorrect + nWrong > 0) {
				printLog("@@@recognizeFileList()", "Accuracy: " + (nCorrect * 100 / (nCorrect + nWrong))
									+ "% out of " + (nCorrect + nWrong) + " tests.");
				printLog("@@@recognizeFileList()", "Total time: " + 
						(tallyFaceRecognizeTime / (cvGetTickFrequency() * 1000.0 * (nCorrect + nWrong))));
			}
			
			int listSize = faceRecognitionResult.length;
			//printLog("@@@recognizeFileList()", "Faces list size="+faces.size() +", result len="+listSize);
			for(int j = 0; j < listSize; j++)
				faces.get(j).result = faceRecognitionResult[j];
			
			return true;
		}
		
		/**
		 * Opens the training data from the file 'data/facedata.xml'.
		 * 
		 * @param pTrainPersonNumMat
		 * @return the person numbers during training, or null if not successful
		 */
		private CvMat loadTrainingData() {
			printLog("MAIN", "loadTrainingData()");
			
			CvMat pTrainPersonNumMat = null; // the person numbers during training
			CvFileStorage fileStorage;
			int i;

			fileStorage = cvOpenFileStorage(appFolder.getAbsolutePath() + "/facedata.xml",
					null, // memstorage
					CV_STORAGE_READ,
					null); // encoding
			if (fileStorage == null) {
				printLog("loadTrainingData()", "Cannot open database file '/facedata.xml'");
				return null;
			}

			personNames.clear();
			nPersons = cvReadIntByName(fileStorage,null, "nPersons", 0); 
			
			if (nPersons == 0) {
				printLog("loadTrainingData()", "===No people found in the training database '/facedata.xml'.");
				return null;
			} else {
				printLog("loadTrainingData()", "===Found " + nPersons +" person(s) in database");
			}

			for (i = 0; i < nPersons; i++) {
				String sPersonName;
				String varname = "personName_" + (i + 1);
				sPersonName = cvReadStringByName(fileStorage,null, varname, "");
				personNames.add(sPersonName);
			}

			nEigens = cvReadIntByName(fileStorage, null, "nEigens", 0); 
			nTrainFaces = cvReadIntByName(fileStorage, null, "nTrainFaces", 0); 
			Pointer pointer = cvReadByName(fileStorage, null, "trainPersonNumMat", cvAttrList()); 
			pTrainPersonNumMat = new CvMat(pointer);

			pointer = cvReadByName(fileStorage, null, "eigenValMat", cvAttrList()); 
			eigenValMat = new CvMat(pointer);

			pointer = cvReadByName(fileStorage, null, 
					"projectedTrainFaceMat", cvAttrList()); 
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

			printLog("loadTrainingData()", "Loaded " + nTrainFaces 
					+ " nTrainFaces from " + nPersons + " person(s).");
			
			final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("People: ");
			if (nPersons > 0) {
				stringBuilder.append("<").append(personNames.get(0)).append(">");
			}
			for (i = 1; i < nPersons; i++) {
				stringBuilder.append(", <").append(personNames.get(i)).append(">");
			}

			printLog("loadTrainingData()", "Data: " + stringBuilder.toString());

			return pTrainPersonNumMat;
		}

		/**
		 * Find the most likely person based on a detection. Returns the index, and
		 * stores the confidence value into pConfidence.
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
			double leastDistSq = Double.MAX_VALUE;
			int i = 0;
			int iTrain = 0;
			int iNearest = 0;

			for (iTrain = 0; iTrain < nTrainFaces; iTrain++) {
				double distSq = 0;

				for (i = 0; i < nEigens; i++) {
					float projectedTrainFaceDistance = (float) projectedTrainFaceMat
							.get(iTrain, i);
					float d_i = projectedTestFace[i] - projectedTrainFaceDistance;
					distSq += d_i * d_i; // eigenValMat.data_fl().get(i);
				}

				if (distSq < leastDistSq) {
					leastDistSq = distSq;
					iNearest = iTrain;
				}
			}

			// Return the confidence level based on the Euclidean distance,
			// so that similar images should give a confidence between 0.5 to 1.0,
			// and very different images should give a confidence between 0.0 to
			// 0.5.
			float pConfidence = (float) (1.0f - Math.sqrt(leastDistSq
					/ (float) (nTrainFaces * nEigens)) / 255.0f);
			pConfidencePointer.put(pConfidence);

			return iNearest;
		}
    }
    
	/**
	 * Returns a string representation of the given float array.
	 * 
	 * @param floatArray
	 *            the given float array
	 * @return a string representation of the given float array
	 */
	
	/*private String floatArrayToString(final float[] floatArray) {
		final StringBuilder stringBuilder = new StringBuilder();
		boolean isFirst = true;
		stringBuilder.append('[');
		for (int i = 0; i < floatArray.length; i++) {
			if (isFirst) {
				isFirst = false;
			} else {
				stringBuilder.append(", ");
			}
			stringBuilder.append(floatArray[i]);
		}
		stringBuilder.append(']');

		return stringBuilder.toString();
	}*/

	/**
	 * Returns a string representation of the given float pointer.
	 * 
	 * @param floatPointer
	 *            the given float pointer
	 * @return a string representation of the given float pointer
	 */
	
	/*private String floatPointerToString(final FloatPointer floatPointer) {
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
	}*/

	/**
	 * Returns a string representation of the given one-channel CvMat object.
	 * 
	 * @param cvMat
	 *            the given CvMat object
	 * @return a string representation of the given CvMat object
	 */
	public String oneChannelCvMatToString(final CvMat cvMat) {
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
	
	private void printLog(String tag, String log){
		Log.e(tag, log);
	}
    
}