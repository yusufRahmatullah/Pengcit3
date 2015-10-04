package com.yugiri.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gilang on 03/10/2015.
 */
public class ImageGrid extends Box {

	private List<ImageGrid> regions;
	public boolean[] gridArray;

	public ImageGrid(){
		super();
		regions = new ArrayList<>();
	}

	public ImageGrid(int minX, int maxX, int minY, int maxY, int splitNumber){
		super(minX, maxX, minY, maxY);
		float regionWidth = width / splitNumber;
		float regionHeight = height / splitNumber;
		int lastX = minX;
		int lastY = minY;
		if(splitNumber > 0) {
			regions = new ArrayList<>();
			for (int i = 0; i < splitNumber; i++) {
				for (int j = 0; j < splitNumber; j++) {
					float tempX = lastX + regionWidth;
					float tempY = lastY + regionHeight;
					if(tempX > maxX)
						tempX = maxX;
					if(tempY > maxY)
						tempY = maxY;
					ImageGrid region = new ImageGrid(lastX, (int) (tempX), lastY, (int)
							(tempY), -1);
					regions.add(region);
					lastX = (int)tempX;
				}
				lastY += regionHeight;
				if(lastY > maxY)
					lastY = maxY;
				lastX = minX;
			}
		}
		gridArray = new boolean[splitNumber*splitNumber];
	}

	public List<ImageGrid> getRegions(){
		return regions;
	}

	public ImageGrid getRegion(int position){
		try {
			return regions.get(position);
		}catch (Exception e){
			return null;
		}
	}

	public int getRegionCount(){
		try{
			return  regions.size();
		}catch (Exception e){
			return 0;
		}
	}

	public int getGridDimension(){
		try {
			return (int) Math.sqrt((double) regions.size());
		}catch (Exception e){
			return 0;
		}
	}

	public void printDebug(){
		for(boolean b : gridArray)
			System.out.println(b);
	}

	public void printArray(){
		for(boolean b : gridArray){
			if(b)
				System.out.print(1 + ", ");
			else
				System.out.print(0 + ", ");
		}
		System.out.println();
	}
}
