package com.yugiri.tools;

import android.graphics.Bitmap;
import android.widget.ArrayAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by gilang on 06/09/2015.
 */
public class AndroidEqualizer {

	public static ArrayList getGrayScale(int[] pixels, int width, int height){
		ArrayList<ArrayList> ret = new ArrayList();
		if (pixels.length > 0) {
			for (int i=0; i<height; i++) {
				ArrayList tempGray  = new ArrayList();
				for (int j=0; j<width; j++) {
					int rgb = pixels[i * width + j];

					int r = (rgb >> 16) & 0xFF;
					int g = (rgb >> 8) & 0xFF;
					int b = (rgb & 0xFF);

					int grayLevel = (r + g + b) / 3;

					tempGray.add(grayLevel);
				}
				ret.add(tempGray);
			}
		}
		return ret;
	}

	private static ArrayList getColorFreq  (ArrayList<ArrayList> input){
		ArrayList ret = new ArrayList<>();
		for(int i=0 ; i<256 ; i++){
			ret.add(0);
		}
		for(int i=0;i<input.size();i++){
			ArrayList temp = input.get(i);
			//nambahin kemunculan warna
			for(int j=0;j<temp.size();j++){
				int col = (int) temp.get(j);
				ret.set(col, ((int)ret.get(col))+1);
			}
		}
		return ret;
	}

	private static ArrayList getCumulativeFreq(ArrayList colorFreq,int adjust){
		ArrayList ret = new ArrayList<>();
		int cumulative = adjust;
		for(int i=0;i<colorFreq.size();i++){
			cumulative += (int)colorFreq.get(i);
			ret.add(cumulative);
		}
		return ret;
	}

	private static ArrayList getFeq(int cumulative){
		int feq = cumulative / 256;
		int rem = cumulative % 256;
		ArrayList ret = new ArrayList<>();
		for(int i=0 ; i<256 ; i++){
			if(rem>0){
				ret.add(feq+1);
				rem--;
			}
			else{
				ret.add(feq);
			}
		}
		return ret;
	}

	public static ArrayList getLookUpTable(ArrayList<ArrayList> grayScale,int adjust){
		ArrayList colorFreq = getColorFreq(grayScale);
		ArrayList cumulativeFreq = getCumulativeFreq(colorFreq, adjust);
		ArrayList feq = getFeq((int) (cumulativeFreq.get(cumulativeFreq.size() - 1)));
		ArrayList cumulativeFeq = getCumulativeFreq(feq,0);

		for(int i=0;i<cumulativeFeq.size();i++){
			//System.out.println(cumulativeFeq.get(i));
		}

		ArrayList ret = new ArrayList();

		int indexCumulativeFeq = 0 ;
		for(int i=0;i<cumulativeFreq.size();i++){
			while((int)cumulativeFreq.get(i) > (int)cumulativeFeq.get(indexCumulativeFeq)){
				indexCumulativeFeq++;
			}
			ret.add(indexCumulativeFeq);
		}
		return ret;
	}

	public static Bitmap getEqualizedBitmap(Bitmap bitmap,int adjust){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		ArrayList<ArrayList> grayScale = getGrayScale(pixels, width, height);

		ArrayList lookUpTable = getLookUpTable(grayScale,adjust);

		for(int i=0;i<lookUpTable.size();i++){
			System.out.println(lookUpTable.get(i));
		}

		for(int i=0;i<grayScale.size();i++){
			ArrayList temp = grayScale.get(i);
			for(int j=0;j<temp.size();j++){

				int map = (int)lookUpTable.get((int)temp.get(j));
				pixels[i * width + j] = map << 16 | map << 8 | map;
			}
		}
		ret.setPixels(pixels, 0, width, 0, 0, width, height);
		return ret;
	}

	public static Bitmap getGrayScaledBitmap(Bitmap bitmap){
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		ArrayList<ArrayList> grayScale = getGrayScale(pixels, width, height);

		for(int i=0;i<grayScale.size();i++){
			ArrayList temp = grayScale.get(i);
			for(int j=0;j<temp.size();j++){
				int map = (int)temp.get(j);
				pixels[i * width + j] = map << 16 | map << 8 | map;
			}
		}
		ret.setPixels(pixels, 0, width, 0, 0, width, height);
		return ret;
	}

}
