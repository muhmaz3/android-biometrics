package android.biometrics.voice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import lib.comirva.AudioFeatureExtractor;
import lib.comirva.KMeansClustering;
import lib.comirva.audio.Matrix;
import lib.comirva.audio.PointList;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;

public class VoiceHelper {
	public static final String VOICE_EXTENSION = ".wav";
	
	/**
	 * Write voice training file into SDCard
	 * @param featureExtractor
	 * @return true if write okay, false if otherwise
	 */
	public static boolean writeTrainingSetToFile(AudioFeatureExtractor featureExtractor){
		if(featureExtractor == null || !AppUtil.isSDCardAvailable()){
			return false;
		}
		
		//Now create the file in the above directory and write the contents into it
		File file = new File(AppConst.VOICE_DATA_FILE_PATH);
		File folder = new File(AppConst.VOICE_FOLDER);
		try {
			if(! folder.exists()) AppUtil.createAppDirectory();
			file.createNewFile();
			
			FileOutputStream fOut = new FileOutputStream(file, true);		
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
			KMeansClustering km = featureExtractor.getKmean();
			
			for (int k=0; k<16; k++) {
				Matrix kmVector = km.getMean(k);
			    for (int j=0; j < 20; j++) {
					osw.write(Double.toString(kmVector.get(j, 0)) + " ");
			    }
			    osw.write("\n");
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
	
	/**
	 * Read voice data from training file
	 * @return List of PointList
	 */
	public static ArrayList<PointList> readVoiceDataToPointList(){		
		File file = new File(AppConst.VOICE_DATA_FILE_PATH);
		ArrayList<PointList> arrayPointList = new ArrayList<PointList>();
		
		if (file.exists()) {
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line;
				PointList pl = null;
				int lineNum = 0;
				
				//Traverse every line
				while( (line = reader.readLine()) != null){
					String[] splitted = line.split(" ");
					if(splitted.length < 20) continue;
					
					lineNum ++;
					if(lineNum % 16 == 1){
						/**
						 * In every block of 16, at first entry, create new PointList 
						 */
						pl = new PointList(20);
					}
					
					double[] point = new double[20];
					for (int j = 0; j < 20; j++) {
						point[j] = Double.parseDouble(splitted[j]);
					}
					pl.add(point);
					
					if(lineNum % 16 == 0){
						/**
						 * In every block of 16, at last entry, add this PointList into final List 
						 */
						arrayPointList.add(pl);
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		
		return arrayPointList;
	}
}
