package nhatnq.biometrics.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import thesis.lib.comirva.AudioFeatureExtractor;
import thesis.lib.comirva.KMeansClustering;
import thesis.lib.comirva.audio.Matrix;
import thesis.lib.comirva.audio.PointList;

public class FileHelper {

	/*
	 * Write File training to Sd card
	 * @param AudioFeatureExtractor
	 * @return boolean (Pass/fail)
	 */
	public static boolean WriteTrainingSetToFile(AudioFeatureExtractor featureExtractor){
		if(featureExtractor == null || !AppUtil.isSDCardAvailable()){
			return false;
		}
		//Now create the file in the above directory and write the contents into it
		File file = new File(AppConst.TRAING_VOICE_FILE_PATH);
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(file, true);		
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
			KMeansClustering km = featureExtractor.getKmean();
			
			for (int k=0;k<16;k++) {
				Matrix kmVector = km.getMean(k);
			    for (int j=0;j<20;j++) {
					osw.write(Double.toString(kmVector.get(j, 0)) + " ");
			        System.out.print(kmVector.get(j,0) + " ");
			    }
			    osw.write("\n");
			    System.out.println();
			}     
			osw.write("\n");
			osw.flush();
			osw.close();
			fOut.close();
		}catch(IOException e){
			return false;
		}
		return true;
	}
	/*
	 * Read file training in SD card
	 * @return ArrayList<PointList> 
	 */
	public static ArrayList<PointList> ReadFileTrainToPointList(){
		ArrayList<PointList> arrayPointList = new ArrayList<PointList>();		
		File file = new File(AppConst.TRAING_VOICE_FILE_PATH);
		int numberOfKMean = 0; //dem so KMeans trong txt, 16 vector Kmean ~ 1 numberOfKMean
		if (file.exists()) { //check file kmean truoc khi read
			try {
				Scanner sc = new Scanner(file);
				while (sc.hasNextDouble()) { //moi vong while se duyet qua 16 vector KMean
					numberOfKMean++;
					PointList pl = new PointList(20);
					for (int k=0;k<16;k++) {
						double[] point = new double[20];
						for (int j=0;j<20;j++) {
							point[j] = sc.nextDouble();
						}
						pl.add(point);
					}
					arrayPointList.add(pl);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return arrayPointList;
	}
}
