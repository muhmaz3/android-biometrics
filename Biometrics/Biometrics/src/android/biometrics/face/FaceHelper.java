package android.biometrics.face;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_OTSU;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvEqualizeHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.biometrics.util.AppUtil;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * Image histogram equalization
 * Author: Bostjan Cigan (http://zerocool.is-a-geek.net)
 */

public class FaceHelper {
	private static String TAG = FaceHelper.class.getName();
	public static final String PGM_EXTENSION = ".pgm";
	public static final String JPG_EXTENSION = ".jpg";
	public static final String PNG_EXTENSION = ".png";
	
	public static Bitmap processBitmap4Display(String path){
    	try {
			ExifInterface exif = new ExifInterface(path);
			int rotate = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 
					ExifInterface.ORIENTATION_NORMAL);
			int degree = 0;
			if(rotate == ExifInterface.ORIENTATION_ROTATE_90) degree = 90;
			else if(rotate == ExifInterface.ORIENTATION_ROTATE_180) degree = 180;
			else if(rotate == ExifInterface.ORIENTATION_ROTATE_270) degree = 270;
			
//			Log.e(TAG, "BM path:"+path);
//			Log.e(TAG, "BM data:"+exif.getAttribute(ExifInterface.TAG_DATETIME));
//			Log.e(TAG, "BM focal length:"+exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
//			Log.e(TAG, "BM length:"+exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
//			Log.e(TAG, "BM width:"+exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
//			Log.e(TAG, "BM orientation:"+exif.getAttribute(ExifInterface.TAG_ORIENTATION));
			//2 ExifInterface.ORIENTATION_FLIP_HORIZONTAL
			//4 ExifInterface.ORIENTATION_FLIP_VERTICAL
			//1 ExifInterface.ORIENTATION_NORMAL
			//3 ExifInterface.ORIENTATION_ROTATE_180
			//8 ExifInterface.ORIENTATION_ROTATE_270
			//6 ExifInterface.ORIENTATION_ROTATE_90
			//5 ExifInterface.ORIENTATION_TRANSPOSE
			//7 ExifInterface.ORIENTATION_TRANSVERSE
			//0 ExifInterface.ORIENTATION_UNDEFINED
			
			if(degree != 0) return FaceHelper.rotateBitmap(path, degree);
			else return BitmapFactory.decodeFile(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return null;
    }
	
	/****************************************
	 * Histogram Equalization on face image *
	 ****************************************/
    
    public static String runHistogramEqualization(String inputPath) {
    	File f = new File(inputPath);
    	if(! f.exists()) return null;
    	
        Bitmap original = BitmapFactory.decodeFile(inputPath);
        Bitmap equalized = histogramEqualization(original);
        
        String ending = inputPath.substring(inputPath.lastIndexOf(".")+1);
        String outputPath = inputPath.replace("."+ending, AppUtil.generateHEImageNameAtThisTime());
        try{
        	f = new File(outputPath);
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            equalized.compress(CompressFormat.PNG, 100, fos);
        }catch(IOException e){
        	e.printStackTrace();
        	return null;
        }
        
        return outputPath;
    }
    
    private static Bitmap histogramEqualization(Bitmap original) {
        int red, green, blue, alpha;
        int newPixel = 0;
 
        // Get the Lookup table for histogram equalization
        ArrayList<int[]> histLUT = histogramEqualizationLUT(original);
        int WIDTH = original.getWidth();
        int HEIGHT = original.getHeight();
        Bitmap histogramEQ = Bitmap.createBitmap(WIDTH, HEIGHT, Config.ARGB_8888);

        for(int i = 0; i < WIDTH; i++) {
            for(int j = 0; j < HEIGHT; j++) {
            	int color = original.getPixel(i, j);
            	alpha = Color.alpha(color);
            	red = Color.red(color);
            	green = Color.green(color);
            	blue = Color.blue(color);
            	
                // Set new pixel values using the histogram lookup table
                red = histLUT.get(0)[red];
                green = histLUT.get(1)[green];
                blue = histLUT.get(2)[blue];
 
                newPixel = Color.argb(alpha, red, green, blue);

                histogramEQ.setPixel(i, j, newPixel);
            }
        }
 
        return histogramEQ;
    }
 
    // Get the histogram equalization lookup table for separate R, G, B channels
    private static ArrayList<int[]> histogramEqualizationLUT(Bitmap input) {
        // Get an image histogram - calculated values by R, G, B channels
        ArrayList<int[]> imageHist = imageHistogram(input);
 
        // Create the lookup table
        ArrayList<int[]> imageLUT = new ArrayList<int[]>();
 
        // Fill the lookup table
        int[] rhistogram = new int[256];
        int[] ghistogram = new int[256];
        int[] bhistogram = new int[256];
 
        for(int i=0; i<rhistogram.length; i++) rhistogram[i] = 0;
        for(int i=0; i<ghistogram.length; i++) ghistogram[i] = 0;
        for(int i=0; i<bhistogram.length; i++) bhistogram[i] = 0;
 
        long sumr = 0;
        long sumg = 0;
        long sumb = 0;
 
        float scale_factor = (float) (255.0 / (input.getWidth() * input.getHeight()));
 
        for(int i=0; i<rhistogram.length; i++) {
            sumr += imageHist.get(0)[i];
            int valr = (int) (sumr * scale_factor);
            if(valr > 255) {
                rhistogram[i] = 255;
            }
            else rhistogram[i] = valr;
 
            sumg += imageHist.get(1)[i];
            int valg = (int) (sumg * scale_factor);
            if(valg > 255) {
                ghistogram[i] = 255;
            }
            else ghistogram[i] = valg;
 
            sumb += imageHist.get(2)[i];
            int valb = (int) (sumb * scale_factor);
            if(valb > 255) {
                bhistogram[i] = 255;
            }
            else bhistogram[i] = valb;
        }
 
        imageLUT.add(rhistogram);
        imageLUT.add(ghistogram);
        imageLUT.add(bhistogram);
 
        return imageLUT;
    }
 
    // Return an ArrayList containing histogram values for separate R, G, B channels
    private static ArrayList<int[]> imageHistogram(Bitmap input) {
        int[] rhistogram = new int[256];
        int[] ghistogram = new int[256];
        int[] bhistogram = new int[256];
 
        for(int i=0; i<rhistogram.length; i++) rhistogram[i] = 0;
        for(int i=0; i<ghistogram.length; i++) ghistogram[i] = 0;
        for(int i=0; i<bhistogram.length; i++) bhistogram[i] = 0;
        
        for(int i=0; i<input.getWidth(); i++) {
            for(int j=0; j<input.getHeight(); j++) {
            	int color = input.getPixel(i, j);
            	int red, green, blue;
//                red = new Color(input.getPixel(i, j)).getRed();
//                green = new Color(input.getRGB (i, j)).getGreen();
//                blue = new Color(input.getRGB (i, j)).getBlue();
            	red = Color.red(color);
            	green = Color.green(color);
            	blue = Color.blue(color);
            	
                // Increase the values of colors
                rhistogram[red]++; ghistogram[green]++; bhistogram[blue]++;
 
            }
        }
 
        ArrayList<int[]> hist = new ArrayList<int[]>();
        hist.add(rhistogram);
        hist.add(ghistogram);
        hist.add(bhistogram);
 
        return hist;
    }
    
    /*****************************************
     * Pre-processing Face Image for 1 image *
     *****************************************/
    
    public static String preProcessFaceImage(String imgPath){ 	
    	Bitmap targetFace = BioFaceDetector.getTargetFace(imgPath);
		if(targetFace != null){
			/* Resized RGB to GrayScale */
			targetFace = convertRgbToGrayscaleBitmap(targetFace);
			/* GrayScale to HQ-GrayScale */
			String jpgPath = createGrayscaleViaHistogramEqualization(targetFace, imgPath);
			/* HQ-GrayScale to PGM */
			String pgmPath = convertGrayscaleToPgmImage(jpgPath);
			
			return pgmPath;
		}else return null;
    }
    
    //path is useful for creating grayscale image name
    public static String createGrayscaleViaHistogramEqualization(Bitmap source, String path) {
    	if(source == null) return null;
    	Log.e("HistogramEQ", ".createGrayscaleViaHistogramEqualization...");
    	
        Bitmap equalized = histogramEqualization(source);
        
        String outputName = AppUtil.getHENameFromJPGName(path);
        try{
        	File f = new File(outputName);
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            equalized.compress(CompressFormat.JPEG, 100, fos);
            fos.close();
        }catch(IOException e){
        	e.printStackTrace();
        	return null;
        }
        
        return outputName;
    }
    
    public static void histogramEqualizeUsingJavaCV(String src, String dest){
    	IplImage img = cvLoadImage(src, 0);
    	IplImage out = cvCreateImage( cvGetSize(img), IPL_DEPTH_8U, 1 );

    	cvEqualizeHist( img, out );
    	cvSaveImage(dest, out);
    	
    	cvReleaseImage( img );
    	cvReleaseImage( out );
    }
    
    public static Bitmap convertRgbToGrayscaleBitmapViaDesaturation(Bitmap srcBitmap) {
		Log.e(TAG, ".createGrayscaleBitmapViaDesaturation");
        int alpha, red, green, blue;
        int newPixel;
        int[] pixel = new int[3];
     
        Bitmap original = srcBitmap;
        Bitmap des = Bitmap.createBitmap(
        		original.getWidth(), original.getHeight(), Config.ARGB_8888);
        int[] desLUT = new int[511];
        for(int i=0; i<desLUT.length; i++) desLUT[i] = (int) (i / 2);
     
        for(int i=0; i<original.getWidth(); i++) {
            for(int j=0; j<original.getHeight(); j++) {
            	int color = original.getPixel(i, j);
                alpha = Color.alpha(color);
                red = Color.red(color);
                green = Color.green(color);
                blue = Color.blue(color);
     
                pixel[0] = red;
                pixel[1] = green;
                pixel[2] = blue;
                int newval = (int) (findMax(pixel) + findMin(pixel));
                newval = desLUT[newval];
                newPixel = Color.argb(alpha, red, green, blue);
                des.setPixel(i, j, newPixel);
            }
        }
        
        return des;
    }  
    
    public static String convertRgbToGrayscaleImageViaDesaturation(String path) {
        int alpha, red, green, blue;
        int newPixel;
     
        Bitmap original = BitmapFactory.decodeFile(path);
        Bitmap des = Bitmap.createBitmap(
        		original.getWidth(), original.getHeight(), Config.ARGB_8888);
   
        for(int i=0; i<original.getWidth(); i++) {
            for(int j=0; j<original.getHeight(); j++) {
            	int color = original.getPixel(i, j);
            	alpha = Color.alpha(color);
            	red = Color.red(color);
            	green = Color.green(color);
            	blue = Color.blue(color);
     
                red = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
                newPixel = Color.argb(alpha, red, green, blue);
                des.setPixel(i, j, newPixel);
            }
        }
        
        String ending = path.substring(path.lastIndexOf("."));
        String newPath = path.replace(ending, AppUtil.generateDesaturationImageNameAtThisTime());
        try{
        	File f = new File(newPath);f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            des.compress(CompressFormat.PNG, 100, fos);
        }catch(IOException e){
        	e.printStackTrace();
        }
        return newPath;
    }
        
    public static Bitmap convertRgbToGrayscaleBitmap(Bitmap src) {
    	Log.e("HistogramEQ", ".convertRgbToGrayscaleBitmap...");
	    final double GS_RED = 0.299;
	    final double GS_GREEN = 0.587;
	    final double GS_BLUE = 0.114;

	    Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
	    int A, R, G, B;
	    int pixel;
	    int width = src.getWidth();
	    int height = src.getHeight();

	    for(int x = 0; x < width; ++x) {
	        for(int y = 0; y < height; ++y) {
	            pixel = src.getPixel(x, y);
	            A = Color.alpha(pixel);
	            R = Color.red(pixel);
	            G = Color.green(pixel);
	            B = Color.blue(pixel);

	            R = G = B = (int)(GS_RED * R + GS_GREEN * G + GS_BLUE * B);
	            bmOut.setPixel(x, y, Color.argb(A, R, G, B));
	        }
	    }
	    return bmOut;
	}

    /**
     * Create a PGM image from Grayscale image
     * Ex: /mnt/sdcard/biometrics/a.jpg -> /mnt/sdcard/biometrics/face/a.pgm
     * @param imgSource Path of grayscale image
     * @param originalPath Path of original image file (captured from camera)
     * @return Path of pgm image
     */
	public static String convertGrayscaleToPgmImage(String hePath){
		Log.e(AppUtil.class.getName(), ".convertGrayscaleToPgmImage...");
		IplImage src = cvLoadImage(hePath);
		
		IplImage grayscale = cvCreateImage(new CvSize(src.width(), src.height()), IPL_DEPTH_8U, 1);
		cvCvtColor(src, grayscale, CV_RGB2GRAY);
		
		//@link http://stackoverflow.com/questions/1585535/convert-rgb-to-black-white-in-opencv
		IplImage im_bw = cvCreateImage(cvGetSize(grayscale), IPL_DEPTH_8U,1);
		cvThreshold(grayscale, im_bw, 128, 255, CV_THRESH_BINARY | CV_THRESH_OTSU);

		String destPath = AppUtil.getPGMNameFromHEName(hePath);
		
		int res = cvSaveImage(destPath, grayscale);
		
		cvReleaseImage(src);
		cvReleaseImage(grayscale);
		cvReleaseImage(im_bw);
		
		if(res == -1) return null;
		else return destPath;
	}
    
    /**
     * Rotate bitmap got from imgPath, then, save it back to sdcard
     * @param imgPath Path of image file
     * @param degree Rotation degree [90, 180, or 270]
     * @return Rotated bitmap
     */
    public static Bitmap rotateBitmap(String imgPath, int degree){
    	File file = new File(imgPath);
    	if(file == null || !file.exists()) return null;
    	
    	Bitmap src = BitmapFactory.decodeFile(imgPath);
        int width = src.getWidth();
        int height = src.getHeight();
        int newWidth = Math.min(width, height);
        int newHeight = newWidth;
        if(degree == 180){
        	newWidth = width/4;
        	newHeight = height/4;
        }else{
        	newWidth = height/4;
        	newHeight = width/4;
        }

        // calculate the scale - in this case = 0.4f
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.setRotate(degree, (float)(width/2), (float)(height/2));
//        matrix.postScale(scaleWidth, scaleHeight);
//        matrix.postRotate(degree);
        

        Log.e(TAG, "@rotateBitmap:nw="+newWidth+", nh="+newHeight
        		+", sw"+scaleWidth+", sh="+scaleHeight+", degree="+degree);
        Bitmap bmResult = Bitmap.createBitmap(
        		src, 0, 0, width, height, matrix, true);
        bmResult = Bitmap.createScaledBitmap(bmResult, newWidth, newHeight, true);
        saveBitmap(bmResult, imgPath);
        return bmResult;
    }
    
    public static void saveBitmap(Bitmap bm, String path){
    	try{
    		File f = new File(path);
    		if(! f.exists()) f.createNewFile();
    		
    		FileOutputStream fos = new FileOutputStream(f);
    		bm.compress(CompressFormat.JPEG, 100, fos);
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }

    /**
     * Create a PNG image from PGM image, save in the same folder
     * The name of PGM image has a tail.
     * Ex:/mnt/sdcard/a.pgm -> /mnt/sdcard/a_pgm2gs.png 
     * @param path Path of pgm image
     * @return Path of png image
     */
    public static String convertPgmToGrayscalePNG(String path){
		IplImage originalImg = cvLoadImage(path, CV_LOAD_IMAGE_GRAYSCALE);
		String imgName = path.replace(".pgm", "_pgm2gs.png");
		
		cvSaveImage(imgName, originalImg);
		return imgName;
	}
    
    private static int findMin(int[] pixel) {
        int min = pixel[0];
        for(int i=0; i<pixel.length; i++) {
            if(pixel[i] < min)
                    min = pixel[i];
        }
        return min;
    }
     
    private static int findMax(int[] pixel) {
        int max = pixel[0];
        for(int i=0; i<pixel.length; i++) {
            if(pixel[i] > max)
                    max = pixel[i];
        }
        return max;
    }
}
